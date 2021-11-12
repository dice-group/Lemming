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
            return graph.getMaxInEdgeDegrees();
        } else {
            return graph.getMaxOutEdgeDegrees();
        }
    }

    /**
     * The method contains logic that computes the maximum vertex degree metric
     * efficiently. It will check the previously stored maximum vertex degree and
     * the vertex on which add or remove an edge graph operation is performed. If
     * the vertex has the same degree as the maximum vertex degree then the metric
     * value will be updated depending upon the graph operation.
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

        double metVal = ((SingleValueMetricResult) previousResult).getResult();
        int changedDegree = getChangedDegree(graph, vertexID, direction);
        int degree = changedDegree - updateVertexDegree;
        if (updateVertexDegree == -1) { // Remove an edge
            if (degree == metVal) {// If degree of a vertex is equal to maximum vertex then apply method is called.
                metVal = apply(graph);
            }
        } else { // Add an edge
            if (degree == metVal) {// If degree of a vertex is equal to maximum vertex then max vertex degree is
                                   // changed.
                metVal = changedDegree;
            }
        }

        return new SingleValueMetricResult(getName(), metVal);

    }

    private int getChangedDegree(ColouredGraph graph, int vertexID, DIRECTION direction) {
        if (direction == DIRECTION.in) {
            return graph.getInEdgeDegree(vertexID);
        } else {
            return graph.getOutEdgeDegree(vertexID);
        }
    }

}
