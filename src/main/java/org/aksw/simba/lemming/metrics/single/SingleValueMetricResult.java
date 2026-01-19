package org.aksw.simba.lemming.metrics.single;

/**
 * The class stores the metric name and the result which can be reused.
 * 
 * @author Atul
 *
 */
public class SingleValueMetricResult implements UpdatableMetricResult {

    /**
     * The name of the metric
     */
    protected String metricName;
    /**
     * The value of the metric
     */
    protected double result;

    /**
     * Initialize metric name and result.
     * 
     * @param metricName - metric name string.
     * @param result     - metric value.
     */
    public SingleValueMetricResult(String metricName, double result) {
        this.metricName = metricName;
        this.result = result;
    }

    /**
     * Returns the value of the metric.
     * 
     * @return the value of the metric.
     */
    @Override
    public double getResult() {
        return result;
    }

    /**
     * Returns the name of the metric.
     * 
     * @return the name of the metric.
     */
    @Override
    public String getMetricName() {
        return metricName;
    }

    /**
     * Set the name of the metric.
     * 
     * @param metricName - the name of the metric
     */
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     * Set the value of the metric.
     * 
     * @param result - the value of the metric
     */
    public void setResult(double result) {
        this.result = result;
    }
}