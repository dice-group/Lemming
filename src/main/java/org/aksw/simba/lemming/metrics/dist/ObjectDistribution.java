package org.aksw.simba.lemming.metrics.dist;

import java.util.Arrays;

/**
 * <p>
 * A distribution of objects. The sample space of the distribution is
 * represented as an array of objects while the values are stored as double
 * array. Both arrays have the same size and the value of the i-th object of the
 * sample space is the i-th value in the values array.
 * </p>
 * <p>
 * Note that the distribution does not have to be normalized.
 * </p>
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 * @param <T>
 */
public class ObjectDistribution<T> {

    /**
     * The sample space of the distribution.
     */
    public T[] sampleSpace;

    /**
     * The values of the distribution.
     */
    public double[] values;

    /**
     * Constructor that creates a new {@link #values} array using the size of
     * the given sample space array.
     * 
     * @param sampleSpace
     *            the sample space of this distribution.
     */
    public ObjectDistribution(T[] sampleSpace) {
        this.sampleSpace = sampleSpace;
        this.values = new double[sampleSpace.length];
    }

    /**
     * Constructor. Note that it is assumed that both arrays have the same
     * length.
     * 
     * @param sampleSpace
     *            the sample space of this distribution.
     * @param values
     *            the values of this distribution.
     */
    public ObjectDistribution(T[] sampleSpace, double[] values) {
        this.sampleSpace = sampleSpace;
        this.values = values;
    }

    public T[] getSampleSpace() {
        return sampleSpace;
    }

    public void setSampleSpace(T[] sampleSpace) {
        this.sampleSpace = sampleSpace;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }
    
    /**
     * 
     * @return the length of the distribution
     */
    public int getLength() {
    	return sampleSpace.length;
    }
    
    /**
     * 
     * @return True if the distribution has no entries
     */
    public boolean isEmpty() {
    	return sampleSpace.length > 0 ? false: true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(sampleSpace);
        result = prime * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObjectDistribution<?> other = (ObjectDistribution<?>) obj;
        if (!Arrays.equals(sampleSpace, other.sampleSpace))
            return false;
        if (!Arrays.equals(values, other.values))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Distribution[");
        for (int i = 0; i < sampleSpace.length; ++i) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(sampleSpace[i]);
            builder.append("=>");
            builder.append(values[i]);
        }
        builder.append(']');
        return builder.toString();
    }

}
