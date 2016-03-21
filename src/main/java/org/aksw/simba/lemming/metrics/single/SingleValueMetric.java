package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.metrics.Metric;

/**
 * A metric that comprises a single double value.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface SingleValueMetric extends Metric {

    /**
     * Returns the value of the metric if it has been applied to a graph before
     * using {@link #apply(org.aksw.simba.lemming.ColouredGraph)}.
     * 
     * @return the value of the metric
     */
    public double getValue();
}
