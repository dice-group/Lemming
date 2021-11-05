package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
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
    public double apply(ColouredGraph graph) {
        return calculateAvg(graph.getGraph().getAllInEdgeDegrees());
    }

    protected double calculateAvg(IntArrayList degrees) {
        double sum = 0;
        for (int i = 0; i < degrees.size(); ++i) {
            sum += degrees.getInt(i);
        }
        return sum / degrees.size();
    }

    /**
     * The method contains logic that computes the average vertex degree metric
     * efficiently. If the metric is computed for the first time then it uses the
     * degrees stored in VertexDegrees else it will update the previously stored sum
     * value.
     * 
     * @param triple
     *            - edge on which graph operation is performed.
     * @param graph
     *            - input graph.
     * @param graphOperation
     *            - Enum indicating graph operation. ("ADD" for adding an
     *            edge and "REMOVE" for removing an edge)
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @return UpdatableMetricResult object with updated values that can be used in
     *         further computations
     */
    @Override
    public UpdatableMetricResult update(ColouredGraph graph, TripleBaseSingleID triple,  Operation graphOperation,
            UpdatableMetricResult previousResult) {

        AvgVertexDegreeMetricResult metricResultTempObj = new AvgVertexDegreeMetricResult(getName(), Double.NaN);
        if (previousResult instanceof AvgVertexDegreeMetricResult) {
            // Copying previously computed values in temporary variables
            metricResultTempObj.setSumVertexDeg(((AvgVertexDegreeMetricResult) previousResult).getSumVertexDeg());
            metricResultTempObj
                    .setNumberOfVertices(((AvgVertexDegreeMetricResult) previousResult).getNumberOfVertices());
        }
        
        int updateVertexDegree = graphOperation == Operation.ADD ? 1 : -1; 
        // variable w track remove an edge or add an edge operation.

        double sum = 0;
        double numberOfVertices = 1;
        
        if (metricResultTempObj.getSumVertexDeg() == Double.NaN) {
            // Computing the Avg Vertex Degree Metric for the first time

            IntArrayList getmMapVerticesinDegree = graph.getGraph().getAllInEdgeDegrees();
            for (Integer vertexDegree: getmMapVerticesinDegree) { // Compute sum in iteration
                sum += vertexDegree;
            }
            numberOfVertices = getmMapVerticesinDegree.size();

        } else { // Re-using the previously computed values
            sum = metricResultTempObj.getSumVertexDeg() + updateVertexDegree;
            // Get the previous computed sum and add 1/subtract 1 to previous sum since edge is added.
            numberOfVertices = metricResultTempObj.getNumberOfVertices();
        }

        // Set values in Temporary object
        metricResultTempObj.setSumVertexDeg(sum);
        metricResultTempObj.setNumberOfVertices(numberOfVertices);

        sum = sum / numberOfVertices; // Compute Metric value
        metricResultTempObj.setResult(sum);

        return metricResultTempObj;
    }

}
