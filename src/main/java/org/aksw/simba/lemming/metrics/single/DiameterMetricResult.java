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

    private ArrayListPath diameterPath;

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
}
