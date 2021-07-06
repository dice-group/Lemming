package org.aksw.simba.lemming.metrics.single;

/**
 * The class stores the metric name and the result which can be reused.
 * 
 * @author Atul
 *
 */
public class SimpleMetricResult implements UpdatableMetricResult {

	protected String metricName;
	protected double result;

	/**
	 * Initialize metric name and result.
	 * 
	 * @param metricName - metric name string.
	 * @param result     - metric value.
	 */
	public SimpleMetricResult(String metricName, double result) {
		this.metricName = metricName;
		this.result = result;

	}

	@Override
	public double getResult() {
		return result;
	}

	@Override
	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public void setResult(double result) {
		this.result = result;
	}
}