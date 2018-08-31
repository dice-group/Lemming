package org.aksw.simba.lemming.metrics.metricselection;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

/**
 * @author DANISH AHMED on 8/27/2018
 */
public interface ComplexityAwareMetric {
    public double calculateComplexity(int edges, int vertices);
}
