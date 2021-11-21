package org.aksw.simba.lemming.metrics.single;

/**
 * The class stores the candidate's set and their metric values, which is used
 * for computing the max vertex degrees for different metrics.
 * 
 * @author Atul
 *
 */
public class MaxVertexDegreeMetricResult extends SingleValueMetricResult {


    private int vertexID = -1;
    // Variable for storing vertex id having maximum degree

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
    public int getVertexID() {
        return vertexID;
    }

    /**
     * Updates the vertex having maximum degree.
     * 
     * @param vertexID
     */
    public void setVertexID(int vertexID) {
        this.vertexID = vertexID;
    }

}
