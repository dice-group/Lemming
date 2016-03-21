package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.Metric;

/**
 * <p>
 * A {@link Metric} that comprises a distribution. The sample space of the
 * distribution is represented as an array of objects while the values are
 * stored as double array.
 * </p>
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface ObjectDistributionMetric<T> extends Metric {

    /**
     * Applies the metric to the given graph.
     * 
     * @param graph
     *            the graph for which the metric should be calculated.
     * @return the distribution determined by the metric.
     */
    public ObjectDistribution<T> apply(ColouredGraph graph);
}
