package org.aksw.simba.lemming.metrics.single;

public class StdDevVertexDegreeMetricResult extends SimpleMetricResult {
	/**
	 * The class stores the average, variance and the number of vertices, which is
	 * used for computing the standard deviation vertex degrees metric in multiple
	 * iterations.
	 * 
	 * @author Pranav
	 *
	 */

	private double varianceVertexInDegrees = 0.0; // Variable for storing variance

	private double varianceVertexOutDegrees = 0.0; // Variable for storing variance

	private double avgVertexInDegrees = 0.0; // Variable for storing average of vertices.

	private double avgVertexOutDegrees = 0.0; // Variable for storing average of vertices.

	private double numberOfVertices = 0.0; // Variable for storing number of vertices.

	/**
	 * Initialization calls super class with the name of metric and metric value.
	 * 
	 * @param metricName - Name of the metric.
	 * @param result     - Metric value.
	 */
	public StdDevVertexDegreeMetricResult(String metricName, double result) {
		super(metricName, result);
	}

	/**
	 * 
	 * Returns the variance of in degrees computed after any edge operation
	 * 
	 * @return Double - variance of in degrees
	 */
	public double getVarianceVertexInDegree() {
		return varianceVertexInDegrees;
	}

	/**
	 * 
	 * Updates the variance of in degrees for the graph.
	 * 
	 * @param variance - variance of in degrees
	 */
	public void setVarianceVertexInDegree(double variance) {
		this.varianceVertexInDegrees = variance;
	}

	/**
	 * 
	 * Returns the average of in degrees computed after any edge operation
	 * 
	 * @return Double - average of in degrees
	 */
	public double getAvgVertexInDegree() {
		return avgVertexInDegrees;
	}

	/**
	 * 
	 * Updates the average of in degrees for the graph.
	 * 
	 * @param avg - average of in degrees
	 */
	public void setAvgVertexInDegree(double avg) {
		this.avgVertexInDegrees = avg;
	}

	/**
	 * @return
	 */
	public double getVarianceVertexOutDegree() {
		return varianceVertexOutDegrees;
	}

	/**
	 * Updates the variance of out degrees for the graph.
	 * 
	 * @param variance - variance of out degrees
	 */
	public void setVarianceVertexOutDegree(double variance) {
		this.varianceVertexOutDegrees = variance;
	}

	/**
	 * @return
	 */
	public double getAvgVertexOutDegree() {
		return avgVertexOutDegrees;
	}

	/**
	 * Updates the average of out degrees for the graph.
	 * 
	 * @param avg - average of out degrees
	 */
	public void setAvgVertexOutDegree(double avg) {
		this.avgVertexOutDegrees = avg;
	}

	/**
	 * Returns the number of vertices for the graph.
	 * 
	 * @return - double value for number of vertices.
	 */
	public double getNumberOfVertices() {
		return numberOfVertices;
	}

	/**
	 * Updates the number of vertices for the graph.
	 * 
	 * @param numberOfVertices - value for number of vertices.
	 */
	public void setNumberOfVertices(double numberOfVertices) {
		this.numberOfVertices = numberOfVertices;
	}

}
