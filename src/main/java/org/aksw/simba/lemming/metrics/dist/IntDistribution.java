package org.aksw.simba.lemming.metrics.dist;

import java.util.Arrays;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * <p>
 * A distribution of int values. The sample space of the distribution is
 * represented as an int array while the values are stored as double array. Both
 * arrays have the same size and the value of the i-th int of the sample space
 * is the i-th value in the values array.
 * </p>
 * <p>
 * Note that the distribution does not have to be normalized.
 * </p>
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class IntDistribution {

    /**
     * The sample space of the distribution.
     */
    public int[] sampleSpace;

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
    public IntDistribution(int[] sampleSpace) {
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
    public IntDistribution(int[] sampleSpace, double[] values) {
        this.sampleSpace = sampleSpace;
        this.values = values;
    }

    public int[] getSampleSpace() {
        return sampleSpace;
    }

    public void setSampleSpace(int[] sampleSpace) {
        this.sampleSpace = sampleSpace;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
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
        IntDistribution other = (IntDistribution) obj;
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

    public static IntDistribution fromMap(IntIntOpenHashMap counts) {
        double values[] = new double[counts.assigned];
        int keys[] = counts.keys().toArray();
        Arrays.sort(keys);
        for (int j = 0; j < keys.length; ++j) {
            values[j] = counts.get(keys[j]);
        }
        return new IntDistribution(keys, values);
    }

}
