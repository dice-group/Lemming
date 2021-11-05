package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.VertexDegrees;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import grph.Grph.DIRECTION;

/**
 * This metric is the highest degree of in or outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MaxVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

    protected DIRECTION direction;

    public MaxVertexDegreeMetric(DIRECTION direction) {
        super(direction == DIRECTION.in ? "maxInDegree" : "maxOutDegree");
        this.direction = direction;
    }

    @Override
    public UpdatableMetricResult apply(ColouredGraph graph) {
        if (direction == DIRECTION.in) {
            return new SingleValueMetricResult(this.name, graph.getGraph().getMaxInEdgeDegrees());
        } else {
            return new SingleValueMetricResult(this.name, graph.getGraph().getMaxOutEdgeDegrees());
        }
    }

    /**
     * The method checks if we need to compute in degree or out-degree and then
     * calls the metricComputationMaxDegree with correct parameters.
     * 
     * @param triple
     *            - edge on which graph operation is performed.
     * @param graph
     *            - input graph.
     * @param graphOperation
     *            - boolean value indicating graph operation. ("true" for adding an
     *            edge and "false" for removing an edge)
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @return
     */
    @Override
    public UpdatableMetricResult update(TripleBaseSingleID triple, ColouredGraph graph, boolean graphOperation,
            UpdatableMetricResult previousResult, VertexDegrees mVertexDegrees) {
        UpdatableMetricResult newMetricResult;

        if (direction == DIRECTION.in) {

            if (graphOperation) { // graphOperation is true then add an edge otherwise its remove an edge
                newMetricResult = metricComputationMaxDegree(graph, DIRECTION.in,
                        triple.headId, triple, 1, previousResult, mVertexDegrees);
            } else {
                newMetricResult = metricComputationMaxDegree(graph, DIRECTION.in,
                        triple.headId, triple, -1, previousResult, mVertexDegrees);
            }

        } else {
            if (graphOperation) {
                newMetricResult = metricComputationMaxDegree(graph, DIRECTION.out,
                        triple.tailId, triple, 1, previousResult, mVertexDegrees);
            } else {
                newMetricResult = metricComputationMaxDegree(graph, DIRECTION.out,
                        triple.tailId, triple, -1, previousResult, mVertexDegrees);
            }

        }

        return newMetricResult;
    }

    /**
     * The method contains logic that reduces the number of calls to apply method
     * for the max vertex degree metric.
     * @param graph
     *            - input graph.
     * @param direction
     *            - this is in or out based on the operation.
     * @param vertexID
     *            - The vertex that is modified.
     * @return
     */
    private UpdatableMetricResult metricComputationMaxDegree(ColouredGraph graph, 
            DIRECTION direction, int vertexID, TripleBaseSingleID triple, int updateVertexDegree,
            UpdatableMetricResult previousResult, VertexDegrees mVertexDegrees) {
        double metVal;

        MaxVertexDegreeMetricResult metricResultTempObj = new MaxVertexDegreeMetricResult(getName(), 0.0);

        if (previousResult instanceof MaxVertexDegreeMetricResult) {
            // Set previously stored maps
            metricResultTempObj
                    .setCandidatesMetricSet(((MaxVertexDegreeMetricResult) previousResult).getmMapCandidatesMetric());
            metricResultTempObj.setMaxVertexDegree(
                    ((MaxVertexDegreeMetricResult) previousResult).getMaxVertexDegree());
        }

        IntSet intSetTemp = metricResultTempObj.getmMapCandidatesMetric();
        // Get the current candidate set

        if (intSetTemp.size() == 0) { // Initially the Candidate set will be empty, hence need to call the apply
                                      // method and store the candidates

            metVal = apply(graph).getResult(); // apply the metric and get the value

            IntSet maxDegreeVertices;
            maxDegreeVertices = mVertexDegrees.getVerticesForDegree((int) metVal, direction);
            // Get the vertex with the metric value
            intSetTemp.addAll(maxDegreeVertices); // store the vertex with metric value in candidate set

            // Store the metric value for later use
            metricResultTempObj.setCandidatesMetricSet(intSetTemp);
            metricResultTempObj.setMaxVertexDegree(metVal);

        } else {

            if (intSetTemp.contains(vertexID)) { // The Edge for vertex in candidate set is modified
                metVal = metricResultTempObj.getMaxVertexDegree();

                if (intSetTemp.size() == 1) {
                    // If there is only single vertex in the candidate list then update the max
                    // degree value

                    if (updateVertexDegree > 0) {
                        metVal = metVal + updateVertexDegree;
                    } else {
                        metVal = apply(graph).getResult(); // apply the metric and get the value
                        IntSet maxDegreeVertices;
                        maxDegreeVertices = mVertexDegrees.getVerticesForDegree((int) metVal, direction);
                        // Get the vertex with the metric value

                        // store the vertex with metric value in candidate set
                        metricResultTempObj.setCandidatesMetricSet(maxDegreeVertices);

                    }
                    metricResultTempObj.setMaxVertexDegree(metVal);
                    // Store the metric value for later use

                } else {

                    if (updateVertexDegree > 0) {
                        // The other vertices that exist in the candidate set can be removed since the
                        // max degree will be increased

                        //metricResultTempObj.setmMapCandidatesMetric(new IntOpenHashSet());

                        IntSet candidate = new IntOpenHashSet();
                        candidate.add(vertexID);
                        metricResultTempObj.setCandidatesMetricSet(candidate);

                        metVal = metVal + updateVertexDegree;
                        metricResultTempObj.setMaxVertexDegree(metVal);
                    } else {
                        // The current vertex can be removed from the candidate set since
                        // its degree is reduced
                        // and the previous max degree value can be used.
                        IntSet candidates = new IntOpenHashSet();
                        IntIterator iterator = intSetTemp.iterator();
                        while (iterator.hasNext()) {
                            int tempTripleId = iterator.nextInt();
                            if (vertexID != tempTripleId)
                                candidates.add(iterator.nextInt());
                        }
                        metricResultTempObj.setCandidatesMetricSet( candidates);
                    }
                }

            } else { // If Edge for vertex in candidate set is not modified then we can use the
                     // previously stored values
                metVal = metricResultTempObj.getMaxVertexDegree();
                int inVertexDegreeTemp;
                inVertexDegreeTemp = mVertexDegrees.getVertexDegree(vertexID, direction);

                if (inVertexDegreeTemp == metVal) {
                    // If vertex has a degree similar to metric value previously stored, add the
                    // vertex in candidate set

                    IntSet candidates = new IntOpenHashSet();
                    candidates.add(inVertexDegreeTemp);
                    IntIterator iterator = intSetTemp.iterator();
                    while (iterator.hasNext()) {
                        candidates.add(iterator.nextInt());
                    }
                    metricResultTempObj.setCandidatesMetricSet(candidates);

                }

            }
        }
        metricResultTempObj.setResult(metVal);// Set the new computed metric value as result

        return metricResultTempObj;
    }


}
