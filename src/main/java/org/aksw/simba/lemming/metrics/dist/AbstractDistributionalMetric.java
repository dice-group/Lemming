package org.aksw.simba.lemming.metrics.dist;

public abstract class AbstractDistributionalMetric implements DistributionalMetric {

    protected String name;
    protected Object sampleSpace[];
    protected double distribution[];

    public AbstractDistributionalMetric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object[] getSampleSpace() {
        return sampleSpace;
    }

    @Override
    public double[] getDistribution() {
        return distribution;
    }
}
