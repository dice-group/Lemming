package org.aksw.simba.lemming.metrics.single;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * The class stores the candidate's set and their metric values, which is used
 * for computing the max vertex degrees for different metrics.
 * 
 * @author Atul
 *
 */
public class MaxVertexDegreeMetricResult extends SimpleMetricResult {

    private double maxVertexDegree = 0.0;
    // Variable for storing max degree

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
     * Returns the current max vertex degree.
     * 
     * @return - max vertex degree.
     */
    public double getMaxVertexDegree() {
        return maxVertexDegree;
    }

    /**
     * Update the  max vertex degree metric values.
     * 
     * @param maxVertexDegreeTemp
     *            - - Input vertex degree metric value that needs to be updated.
     */
    public void setMaxVertexDegree( double maxVertexDegreeTemp) {
        maxVertexDegree = maxVertexDegreeTemp;
    }

}
