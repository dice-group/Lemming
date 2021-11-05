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
     * @param triple         - edge on which graph operation is performed.
     * @param graph          - input graph.
     * @param graphOperation - Enum indicating graph operation. ("ADD" for adding an
     *                       edge and "REMOVE" for removing an edge)
     * @param previousResult - UpdatableMetricResult object containing the previous
     *                       computed results.
     * @return
     */
    @Override
    public UpdatableMetricResult update(ColouredGraph graph, TripleBaseSingleID triple, Operation graphOperation,
            UpdatableMetricResult previousResult) {
        // Need to compute MaxVertexInDegree metric or MaxVertexOutDegree

        int vertexID = direction == DIRECTION.in ? triple.headId : triple.tailId;
        // For MaxVertexInDegree, need to use triple.headId else triple.tailId
        int updateVertexDegree = graphOperation == Operation.ADD ? 1 : -1;
        // variable to track remove an edge or add an edge operation.

        double metVal; // metric value

        SingleValueMetricResult metricResultTempObj = new SingleValueMetricResult(getName(), Double.NaN);
        if (previousResult instanceof SingleValueMetricResult) {
            metricResultTempObj.setResult(((SingleValueMetricResult) previousResult).getResult());
        }
        metVal = metricResultTempObj.getResult();

        // Get the current candidate set
        if (Double.isNaN(metVal)) {
            // metVal = apply(graph);
            metVal = apply(graph); // apply the metric and get the value
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
        if (direction == DIRECTION.in) {
            return graph.getGraph().getInEdgeDegree(vertexID);
        } else {
            return graph.getGraph().getOutEdgeDegree(vertexID);
        }
    }

}
