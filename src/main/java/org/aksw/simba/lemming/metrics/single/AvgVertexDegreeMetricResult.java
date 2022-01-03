package org.aksw.simba.lemming.metrics.single;

/**
 * The class stores the sum of the vertex degrees and the number of vertices,
 * which is used for computing the average vertex degrees metric in multiple
 * iterations.
 * 
 * @author Atul
 *
 */
public class AvgVertexDegreeMetricResult extends SingleValueMetricResult {

    /**
     * Variable for storing sum of vertex degrees
     */
    private double sumVertexDeg = 0.0; 

    /**
     * Variable for storing number of vertices.
     */
    private double numberOfVertices = 0.0; 

    /**
     * Initialization calls super class with the name of metric and metric value.
     * 
     * @param metricName
     *            - Name of the metric.
     * @param result
     *            - Metric value.
     */
    public AvgVertexDegreeMetricResult(String metricName, double result) {
        super(metricName, result);
    }

    /**
     * Returns the sum of vertex degrees computed for add an edge operation.
     * 
     * @return - double value for sum.
     */
    public double getSumVertexDeg() {
        return sumVertexDeg;
    }

    /**
     * Update the sum of vertex degrees computed for add an edge operation.
     * 
     * @param sumVertexDeg
     *            - double value for sum
     */
    public void setSumVertexDeg(double sumVertexDeg) {
        this.sumVertexDeg = sumVertexDeg;
    }

    /**
     * Returns the number of vertices for the graph.
     * 
     * @return - double value for number of vertices.
     */
    public double getNumberOfVertices() {
        return numberOfVertices;
    }

    /**
     * Updates the number of vertices for the graph.
     * 
     * @param numberOfVertices
     *            - double value for number of vertices.
     */
    public void setNumberOfVertices(double numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
    }
}
