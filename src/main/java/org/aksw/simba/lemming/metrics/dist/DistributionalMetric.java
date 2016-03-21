package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.metrics.Metric;

/**
 * <p>
 * A {@link Metric} that comprises a distribution. The sample space of the
 * distribution is represented as an array of objects while the values are
 * stored as double array. Both arrays have the same size and the value of the
 * i-th object of the sample space is the i-th value in the values array.
 * </p>
 * <p>
 * Note that the distribution does not have to be normalized.
 * </p>
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface DistributionalMetric extends Metric {

    /**
     * Returns the sample space of the distribution. If this metric does not
     * have been applied to a graph the return value of this method is not
     * defined.
     * 
     * @return the sample space of the distribution
     */
    public Object[] getSampleSpace();

    /**
     * Returns the values of the distribution. If this metric does not have been
     * applied to a graph the return value of this method is not defined.
     * 
     * @return the values of the distribution
     */
    public double[] getDistribution();
}
