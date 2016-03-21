package org.aksw.simba.lemming.metrics;

/**
 * Abstract implementation of a {@link Metric}.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public abstract class AbstractMetric implements Metric {

    /**
     * Name of the metric.
     */
    protected String name;

    public AbstractMetric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
