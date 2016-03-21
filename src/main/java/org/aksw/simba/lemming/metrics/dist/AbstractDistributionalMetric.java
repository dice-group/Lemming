package org.aksw.simba.lemming.metrics.dist;

/**
 * 
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public abstract class AbstractDistributionalMetric implements DistributionalMetric {

    /**
     * The name of the metric
     */
    protected String name;

    /**
     * The sample space of the distribution
     */
    protected Object sampleSpace[];

    /**
     * The values of the distribution.
     */
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
