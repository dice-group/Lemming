package org.aksw.simba.lemming.metrics.single;

/**
 * Abstract implementation of a {@link SingleValueMetric}.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public abstract class AbstractSingleValueMetric implements SingleValueMetric {

    /**
     * Name of the metric.
     */
    protected String name;
    
    /**
     * Value of the metric.
     */
    protected double value;

    public AbstractSingleValueMetric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public double getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append("=");
        builder.append(getValue());
        return builder.toString();
    }
}
