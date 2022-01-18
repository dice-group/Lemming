package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * This metric determines the average degree of outgoing edges in the graph.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class AvgVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

    public AvgVertexDegreeMetric() {
        super("avgDegree");
    }

    protected AvgVertexDegreeMetric(String name) {
        super(name);
    }

    @Override
    public double apply(IColouredGraph graph) {
        return applyUpdatable(graph).getResult();
    }

    protected double calculateAvg(IntArrayList degrees) {
        double sum = 0;
        for (int i = 0; i < degrees.size(); ++i) {
            sum += degrees.getInt(i);
        }
        return sum / degrees.size();
    }

    /**
     * Returns metric results that can be reused for further computations. Here, the
     * metric result object is initialized. Storing the Sum of degree of Vertices
     * and Number of vertices which can be used to compute the average vertex degree
     * metric.
     *
     * @param graph - input graph.
     * @return - metric result.
     */
    @Override
    public UpdatableMetricResult applyUpdatable(IColouredGraph graph) {

        AvgVertexDegreeMetricResult metricResultTempObj = new AvgVertexDegreeMetricResult(getName(), Double.NaN);

        double sum = 0;

        // Computing the Avg Vertex Degree Metric for the first time
        IntArrayList getmMapVerticesinDegree = graph.getAllInEdgeDegrees();
        for (Integer vertexDegree : getmMapVerticesinDegree) { // Compute sum in iteration
            sum += vertexDegree;
        }
        double numberOfVertices = getmMapVerticesinDegree.size();

        // Set values in Temporary object
        metricResultTempObj.setSumVertexDeg(sum);
        metricResultTempObj.setNumberOfVertices(numberOfVertices);

        sum = sum / numberOfVertices; // Compute Metric value
        metricResultTempObj.setResult(sum);

        return metricResultTempObj;
    }

    /**
     * The method contains logic that computes the average vertex degree metric
     * efficiently. It will update the previously stored sum value to compute the
     * new value for the metric.
     *
     * @param triple         - edge on which graph operation is performed.
     * @param graph          - input graph.
     * @param graphOperation - Enum indicating graph operation. ("ADD" for adding an
     *                       edge and "REMOVE" for removing an edge)
     * @param previousResult - UpdatableMetricResult object containing the previous
     *                       computed results.
     * @return UpdatableMetricResult object with updated values that can be used in
     *         further computations
     */
    @Override
    public UpdatableMetricResult update(IColouredGraph graph, TripleBaseSingleID triple, Operation graphOperation,
            UpdatableMetricResult previousResult) {

        if (previousResult == null) {
            return applyUpdatable(graph);
        }

        AvgVertexDegreeMetricResult metricResultTempObj = new AvgVertexDegreeMetricResult(getName(), Double.NaN);

        int updateVertexDegree = graphOperation == Operation.ADD ? 1 : -1;

        double sum = ((AvgVertexDegreeMetricResult) previousResult).getSumVertexDeg() + updateVertexDegree;
        // Get the previous computed sum and add 1/subtract 1 to previous sum since edge
        // is added.
        double numberOfVertices = ((AvgVertexDegreeMetricResult) previousResult).getNumberOfVertices();

        // Set values in Temporary object
        metricResultTempObj.setSumVertexDeg(sum);
        metricResultTempObj.setNumberOfVertices(numberOfVertices);

        sum = sum / numberOfVertices; // Compute Metric value
        metricResultTempObj.setResult(sum);

        return metricResultTempObj;
    }

}
