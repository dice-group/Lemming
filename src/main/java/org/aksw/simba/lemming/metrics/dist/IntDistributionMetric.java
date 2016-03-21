package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.Metric;

/**
 * <p>
 * A {@link Metric} that comprises a distribution. The sample space of the
 * distribution is an int array while the values are stored as double array.
 * </p>
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface IntDistributionMetric extends Metric {

    /**
     * Applies the metric to the given graph.
     * 
     * @param graph
     *            the graph for which the metric should be calculated.
     * @return the distribution determined by the metric.
     */
    public IntDistribution apply(ColouredGraph graph);
}
