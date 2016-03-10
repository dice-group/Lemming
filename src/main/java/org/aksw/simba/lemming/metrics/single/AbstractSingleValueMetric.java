package org.aksw.simba.lemming.metrics.single;

public abstract class AbstractSingleValueMetric implements SingleValueMetric {

    protected String name;
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
