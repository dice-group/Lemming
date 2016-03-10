package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.metrics.Metric;

public interface DistributionalMetric extends Metric {

    public Object[] getSampleSpace();

    public double[] getDistribution();
}
