package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import org.aksw.simba.lemming.metrics.single.SingleValueClusteringCoefficientMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import java.util.List;

/**
 * @author DANISH AHMED on 8/10/2018
 */
public class Config {
    private List<SingleValueMetric> nodeTriMetrics;
    private List<SingleValueMetric> edgeTriMetrics;

    protected void setNodeTriangleMetric(List<SingleValueMetric> nodeTriMetrics) {
        this.nodeTriMetrics = nodeTriMetrics;
    }

    protected void setEdgeTriangleMetric(List<SingleValueMetric> edgeTriMetrics) {
        this.edgeTriMetrics = edgeTriMetrics;
    }

    public List<SingleValueMetric> getNodeTriMetrics() {
        return nodeTriMetrics;
    }

    public List<SingleValueMetric> getEdgeTriMetrics() {
        return edgeTriMetrics;
    }
}
