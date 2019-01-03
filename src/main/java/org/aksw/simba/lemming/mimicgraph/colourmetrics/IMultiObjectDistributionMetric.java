package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.Metric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;

public interface IMultiObjectDistributionMetric<T> extends Metric{

	/**
     * Applies the metric to the given graph.
     * 
     * @param graph
     *            the graph for which the metric should be calculated.
     * @return a mapping of keys to distributions determined by the metric.
     */
    public Map<T, ObjectDistribution<T>> apply(ColouredGraph graph);	
}