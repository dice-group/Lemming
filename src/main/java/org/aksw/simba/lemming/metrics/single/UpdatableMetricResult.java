package org.aksw.simba.lemming.metrics.single;

/**
 * Interface for storing previously computed metric results for different
 * metrics.
 * 
 * @author Atul
 *
 */
public interface UpdatableMetricResult {

	/**
	 * Returns the metric result.
	 * 
	 * @return - metric value.
	 */
	public double getResult();

	/**
	 * Returns the name of the metric.
	 * 
	 * @return - metric name.
	 */
	public String getMetricName();
}
