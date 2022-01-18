/**
 * 
 */
package org.aksw.simba.lemming.metrics.single;

import grph.path.ArrayListPath;

/**
 * @author Pranav
 *
 */
public class DiameterMetricResult extends SingleValueMetricResult {

    /**
     * The information on the nodes and the edges contributing to the diameter
     */
    private ArrayListPath diameterPath;

    /**
     * @param metricName
     * @param result
     */
    public DiameterMetricResult(String metricName, double result) {
        super(metricName, result);
    }

    /**
     * Set the diameter path
     * 
     * @param nodesInDiameter - list of nodes and edges
     */
    public void setDiameterPath(ArrayListPath d) {
        this.diameterPath = d;
    }

    /**
     * Fetch the current diameter path
     * 
     * @return ArrayListPath - diameter path
     */
    public ArrayListPath getDiameterPath() {
        return this.diameterPath;
    }
}
