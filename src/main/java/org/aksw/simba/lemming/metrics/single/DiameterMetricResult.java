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
     * The path between the end nodes of the diameter
     */
    private ArrayListPath diameterPath;
    /**
     * Number of paths in the graph that have the same length as the diameter
     */
    private int numberOfDiameterPaths;

    /**
     * @param metricName
     * @param result
     */
    public DiameterMetricResult(String metricName, double result) {
        super(metricName, result);
    }

    public void setDiameterPath(ArrayListPath nodesInDiameter) {
        this.diameterPath = nodesInDiameter;
    }

    public ArrayListPath getDiameterPath() {
        return this.diameterPath;
    }

    public void setCountOfDiameters(int n) {
        this.numberOfDiameterPaths = n;
    }

    public int getCountOfDiameters() {
        return this.numberOfDiameterPaths;
    }
}
