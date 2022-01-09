package org.aksw.simba.lemming.metrics.single;

import grph.Grph.DIRECTION;

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
     * Variable for storing vertex id having maximum degree
     */
    private int maxVertexID = -1;
    
    /**
     * Variable for storing vertex id having minimum degree
     */
    private int minVertexID = -1;

    /**
     * Variable for storing direction of metric
     */
    private DIRECTION direction = null;
    
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

    /**
     * Returns the id of the vertex having minimum degree.
     * @return - Integer value
     */
    public int getMinVertexID() {
        return minVertexID;
    }

    /**
     * Updates the id of the vertex having minimum degree.
     * @param minVertexID - vertex id
     */
    public void setMinVertexID(int minVertexID) {
        this.minVertexID = minVertexID;
    }

    /**
     * Returns the direction of metric result.
     * @return - direction enum
     */
    public DIRECTION getDirection() {
        return direction;
    }

    /**
     * Updates the direction of metric result.
     * @param direction - direction enum
     */
    public void setDirection(DIRECTION direction) {
        this.direction = direction;
    }
    /** 
     * Returns the number of vertices that have the maximum degree.
     * @return - Integer value
     */
    public int getNumOfVerticesWithMaxDeg() {
        return numOfVerticesWithMaxDeg;
    }

    /**
     * Updates the number of vertices that have maximum degree.
     * @param numOfVerticesWithMaxDeg
     */
    public void setNumOfVerticesWithMaxDeg(int numOfVerticesWithMaxDeg) {
        this.numOfVerticesWithMaxDeg = numOfVerticesWithMaxDeg;
    }
    
}
