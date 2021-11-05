package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
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
    public double apply(ColouredGraph graph) {
        if (direction == DIRECTION.in) {
            return graph.getGraph().getMaxInEdgeDegrees();
        } else {
            return graph.getGraph().getMaxOutEdgeDegrees();
        }
    }

    /**
     * The method checks if we need to compute in degree or out-degree and then
     * calls the metricComputationMaxDegree with correct parameters.
     * 
     * @param triple
     *            - edge on which graph operation is performed.
     * @param metric
     *            - input metric which needs to be computed.
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
    public UpdatableMetricResult update(ColouredGraph graph, TripleBaseSingleID triple,  Operation graphOperation,
            UpdatableMetricResult previousResult
    // , VertexDegrees mVertexDegrees
    ) {
        UpdatableMetricResult newMetricResult;

        if (direction == DIRECTION.in) {

            if (graphOperation == Operation.ADD) { // graphOperation is true then add an edge otherwise its remove an edge
                newMetricResult = metricComputationMaxDegree(graph, DIRECTION.in, triple.headId, triple, 1,
                        previousResult);
            } else {
                newMetricResult = metricComputationMaxDegree(graph, DIRECTION.in, triple.headId, triple, -1,
                        previousResult);
            }

        } else {
            if (graphOperation == Operation.ADD) {
                newMetricResult = metricComputationMaxDegree(graph, DIRECTION.out, triple.tailId, triple, 1,
                        previousResult);
            } else {
                newMetricResult = metricComputationMaxDegree(graph, DIRECTION.out, triple.tailId, triple, -1,
                        previousResult);
            }

        }

        return newMetricResult;
    }

    /**
     * The method contains logic that reduces the number of calls to apply method
     * for the max vertex degree metric.
     * 
     * @param metric
     *            - metric which should be calculated.
     * @param graph
     *            - input graph.
     * @param metricName
     *            - can be "RemoveAnEdge" or "AddAnEdge" indicating how the edge is
     *            modified.
     * @param direction
     *            - this is in or out based on the operation.
     * @param vertexID
     *            - The vertex that is modified.
     * @return
     */
    private UpdatableMetricResult metricComputationMaxDegree(ColouredGraph graph, DIRECTION direction, int vertexID,
            TripleBaseSingleID triple, int updateVertexDegree, UpdatableMetricResult previousResult
    // ,VertexDegrees mVertexDegrees
    ) {
        double metVal;

        SimpleMetricResult metricResultTempObj = new SimpleMetricResult(getName(), 0.0);
        if (previousResult instanceof SimpleMetricResult) {
            metricResultTempObj.setResult(((SimpleMetricResult) previousResult).getResult());
        }
        metVal = metricResultTempObj.getResult();

        // Get the current candidate set
        if (metVal == 0.0) {
            metVal = apply(graph);
        } else {

            int changedDegree = getChangedDegree(graph, vertexID, direction);
            int degree = changedDegree - updateVertexDegree;
            if (updateVertexDegree == -1) {
                if (degree == metVal // && mVertexDegrees.getDegreeCount(degree, direction) == 0
                ) {
                    metVal = apply(graph);
                }
            } else {
                if (degree == metVal) {
                    metVal = changedDegree;
                }
            }

        }
        metricResultTempObj.setResult(metVal);// Set the new computed metric value as result

        return metricResultTempObj;
    }
    
    private int getChangedDegree(ColouredGraph graph, int vertexID, DIRECTION direction) {
        if(direction == DIRECTION.in) {
            return graph.getGraph().getInEdgeDegree(vertexID);
        }else {
            return graph.getGraph().getOutEdgeDegree(vertexID);
        }
    }

}
