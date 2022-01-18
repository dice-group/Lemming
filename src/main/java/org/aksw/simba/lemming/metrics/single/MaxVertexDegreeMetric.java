package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
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
    public double apply(IColouredGraph graph) {
        return applyUpdatable(graph).getResult();
    }

    /**
     * Returns metric results that can be reused for further computations. Here, the
     * metric result object is initialized. Storing the maximum vertex degree and
     * the number of vertices having the maximum degree.
     * 
     * @param graph - input graph.
     * @return - metric result.
     */
    @Override
    public UpdatableMetricResult applyUpdatable(IColouredGraph graph) {
        MaxVertexDegreeMetricResult metricResultTempObj = new MaxVertexDegreeMetricResult(getName(), Double.MIN_VALUE);

        IntSet vertices = graph.getVertices();

        int numOfVerticesWithMaxDegree = 0;// Variable to track number of vertices with maximum degree.

        for (int vertex : vertices) {
            int degree = getChangedDegree(graph, vertex, direction);
            if (degree == metricResultTempObj.getResult()) {
                metricResultTempObj.setResult(degree);
                numOfVerticesWithMaxDegree++;
            } else if (degree > metricResultTempObj.getResult()) {
                metricResultTempObj.setResult(degree);
                numOfVerticesWithMaxDegree = 1;
            }

        }

        metricResultTempObj.setNumOfVerticesWithMaxDeg(numOfVerticesWithMaxDegree);
        return metricResultTempObj;
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
    public UpdatableMetricResult update(IColouredGraph graph, TripleBaseSingleID triple, Operation graphOperation,
            UpdatableMetricResult previousResult) {

        if (previousResult == null) {
            return applyUpdatable(graph);
        }

        MaxVertexDegreeMetricResult metricResultTempObj = new MaxVertexDegreeMetricResult(getName(),
                ((MaxVertexDegreeMetricResult) previousResult).getResult());
        metricResultTempObj.setNumOfVerticesWithMaxDeg(
                ((MaxVertexDegreeMetricResult) previousResult).getNumOfVerticesWithMaxDeg());

        // Need to compute MaxVertexInDegree metric or MaxVertexOutDegree
        int vertexID = direction == DIRECTION.in ? triple.headId : triple.tailId;
        // For MaxVertexInDegree, need to use triple.headId else triple.tailId
        int updateVertexDegree = graphOperation == Operation.ADD ? 1 : -1;
        // variable to track remove an edge or add an edge operation.

        int changedDegree = getChangedDegree(graph, vertexID, direction);
        int degree = changedDegree - updateVertexDegree;
        if (updateVertexDegree == -1) { // Remove an edge
            if (degree == ((MaxVertexDegreeMetricResult) previousResult).getResult()
                    && ((MaxVertexDegreeMetricResult) previousResult).getNumOfVerticesWithMaxDeg() - 1 == 0) {
                // If degree of a vertex is equal to maximum vertex and there is only one vertex
                // with maximum degree
                // then recalculate
                return applyUpdatable(graph);
            } else if (degree == ((MaxVertexDegreeMetricResult) previousResult).getResult()) {
                // If degree of a vertex is equal to maximum vertex and there are mpre than one
                // vertex with maximum degree
                // then reuse previous result and update the number of vertices with maximum
                // degree
                metricResultTempObj.setNumOfVerticesWithMaxDeg(
                        ((MaxVertexDegreeMetricResult) previousResult).getNumOfVerticesWithMaxDeg() - 1);
                metricResultTempObj.setResult(((MaxVertexDegreeMetricResult) previousResult).getResult());
            }
        } else { // Add an edge
            if (degree == ((MaxVertexDegreeMetricResult) previousResult).getResult()) {
                // If degree of a vertex is equal to maximum vertex then max vertex degree is
                // changed.
                metricResultTempObj.setResult(changedDegree);
                metricResultTempObj.setNumOfVerticesWithMaxDeg(1);
            }
        }

        return metricResultTempObj;

    }

    private int getChangedDegree(IColouredGraph graph, int vertexID, DIRECTION direction) {
        if (direction == DIRECTION.in) {
            return graph.getInEdgeDegree(vertexID);
        } else {
            return graph.getOutEdgeDegree(vertexID);
        }
    }

}
