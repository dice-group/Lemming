package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.Metric;

import java.util.List;

/**
 * @author DANISH AHMED on 8/10/2018
 */
public interface SingleValueClusteringCoefficientMetric extends Metric {

    /**
     * Applies the metric to the given graph.
     *
     * @param graph the graph for which the metric should be calculated.
     * @return the value of the metric.
     */
    public double apply(IColouredGraph graph);

    public List<Double> getClusteringCoefficient();
}
