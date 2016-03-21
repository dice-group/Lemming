package org.aksw.simba.lemming.metrics.dist.multi;

import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.Metric;
import org.aksw.simba.lemming.metrics.dist.IntDistribution;

/**
 * <p>
 * A {@link Metric} that comprises a set of distributions. The sample space of
 * the single distributions are int arrays while their values are stored as
 * double array.
 * </p>
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface MultipleIntDistributionMetric<T> extends Metric {

    /**
     * Applies the metric to the given graph.
     * 
     * @param graph
     *            the graph for which the metric should be calculated.
     * @return a mapping of keys to distributions determined by the metric.
     */
    public Map<T, IntDistribution> apply(ColouredGraph graph);
}
