package org.aksw.simba.lemming.metrics.single;

/**
 * The class stores the average, variance and the number of vertices, which is
 * used for computing the standard deviation vertex degrees metric in multiple
 * iterations.
 * 
 * @author Pranav
 *
 */
public class StdDevVertexDegreeMetricResult extends SingleValueMetricResult {

    /**
     * Variable for storing variance
     */
    private double varianceVertexDegrees = 0.0;

    /**
     * Variable for storing average of vertices.
     */
    private double avgVertexDegrees = 0.0;

    /**
     * Variable for storing number of vertices.
     */
    private double numberOfVertices = 0.0;

    /**
     * Initialization calls super class with the name of metric and metric value.
     * 
     * @param metricName - Name of the metric.
     * @param result     - Metric value.
     */
    public StdDevVertexDegreeMetricResult(String metricName, double result) {
        super(metricName, result);
    }

    /**
     * 
     * Returns the variance of degrees computed after any edge operation
     * 
     * @return Double - variance of degrees
     */
    public double getVarianceVertexDegree() {
        return varianceVertexDegrees;
    }

    /**
     * 
     * Updates the variance of degrees for the graph.
     * 
     * @param variance - variance of degrees
     */
    public void setVarianceVertexDegree(double variance) {
        this.varianceVertexDegrees = variance;
    }

    /**
     * 
     * Returns the average of degrees computed after any edge operation
     * 
     * @return Double - average of degrees
     */
    public double getAvgVertexDegree() {
        return avgVertexDegrees;
    }

    /**
     * 
     * Updates the average of degrees for the graph.
     * 
     * @param avg - average of degrees
     */
    public void setAvgVertexDegree(double avg) {
        this.avgVertexDegrees = avg;
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
     * @param numberOfVertices - value for number of vertices.
     */
    public void setNumberOfVertices(double numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
    }

}
