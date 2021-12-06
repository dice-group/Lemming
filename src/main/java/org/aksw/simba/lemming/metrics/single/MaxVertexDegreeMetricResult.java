package org.aksw.simba.lemming.metrics.single;

/**
 * The class stores the candidate's set and their metric values, which is used
 * for computing the max vertex degrees for different metrics.
 * 
 * @author Atul
 *
 */
public class MaxVertexDegreeMetricResult extends SingleValueMetricResult {


    private int maxVertexID = -1;
    // Variable for storing vertex id having maximum degree
    
    private int minVertexID = -1;
    // Variable for storing vertex id having minimum degree

    /**
     * Initialization calls super class with the name of metric and metric value.
     * 
     * @param metricName
     *            - Name of the metric.
     * @param result
     *            - Metric value.
     */
    public MaxVertexDegreeMetricResult(String metricName, double result) {
        super(metricName, result);
    }

    /**
     * Returns the vertex having maximum degree.
     * 
     * @return
     */
    public int getMaxVertexID() {
        return maxVertexID;
    }

    /**
     * Updates the vertex having maximum degree.
     * 
     * @param vertexID
     */
    public void setMaxVertexID(int vertexID) {
        this.maxVertexID = vertexID;
    }

    public int getMinVertexID() {
        return minVertexID;
    }

    public void setMinVertexID(int minVertexID) {
        this.minVertexID = minVertexID;
    }

}
