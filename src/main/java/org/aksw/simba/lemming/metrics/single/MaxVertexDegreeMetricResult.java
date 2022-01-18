package org.aksw.simba.lemming.metrics.single;

/**
 * The class stores the candidate's set and their metric values, which is used
 * for computing the max vertex degrees for different metrics.
 * 
 * @author Atul
 *
 */
public class MaxVertexDegreeMetricResult extends SingleValueMetricResult {

    /**
     * Variable for storing number of vertices with maximum degree
     */
    private int numOfVerticesWithMaxDeg = 0;

    /**
     * Initialization calls super class with the name of metric and metric value.
     * 
     * @param metricName - Name of the metric.
     * @param result     - Metric value.
     */
    public MaxVertexDegreeMetricResult(String metricName, double result) {
        super(metricName, result);
    }

    /**
     * Returns the number of vertices that have the maximum degree.
     * 
     * @return - Integer value
     */
    public int getNumOfVerticesWithMaxDeg() {
        return numOfVerticesWithMaxDeg;
    }

    /**
     * Updates the number of vertices that have maximum degree.
     * 
     * @param numOfVerticesWithMaxDeg
     */
    public void setNumOfVerticesWithMaxDeg(int numOfVerticesWithMaxDeg) {
        this.numOfVerticesWithMaxDeg = numOfVerticesWithMaxDeg;
    }

}
