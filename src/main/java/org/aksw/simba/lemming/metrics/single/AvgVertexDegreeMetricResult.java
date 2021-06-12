package org.aksw.simba.lemming.metrics.single;

/**
 *  The class stores the sum of the vertex degrees and the number of vertices, which is used
 * for computing the average vertex degrees metric in multiple iterations.
 * @author Atul
 *
 */
public class AvgVertexDegreeMetricResult extends SimpleMetricResult {

	private double sumVertexDegAddEdge = 0.0; // Variable for storing sum of vertex degrees for Add an Edge
	private double sumVertexDegRemEdge = 0.0; // Variable for storing sum of Vertex degrees for Remove an Edge

	private double sumVertexDegAddEdgeTemp = 0.0; // Temp variable for storing sum of vertex degrees for Add an Edge
	private double sumVertexDegRemEdgeTemp = 0.0; // Temp variable for storing sum of Vertex degrees for Remove an Edge

	private double numberOfVertices = 0.0; // Variable for storing number of vertices.

	/** Initialization calls super class with the name of metric and metric value.
	 * @param metricName - Name of the metric.
	 * @param result - Metric value.
	 */
	public AvgVertexDegreeMetricResult(String metricName, double result) {
		super(metricName, result);
	}

	/** Returns the sum of vertex degrees computed for add an edge operation.
	 * @return - double value for sum.
	 */
	public double getSumVertexDegAddEdge() {
		return sumVertexDegAddEdge;
	}

	/** Update the sum of vertex degrees computed for add an edge operation.
	 * @param sumVertexDegAddEdge - double value for sum
	 */
	public void setSumVertexDegAddEdge(double sumVertexDegAddEdge) {
		this.sumVertexDegAddEdge = sumVertexDegAddEdge;
	}

	/** Returns the sum of vertex degrees computed for remove an edge operation.
	 * @return - double value for sum.
	 */
	public double getSumVertexDegRemEdge() {
		return sumVertexDegRemEdge;
	}

	/** Update the sum of vertex degrees computed for remove an edge operation.
	 * @param sumVertexDegRemEdge - double value for sum
	 */
	public void setSumVertexDegRemEdge(double sumVertexDegRemEdge) {
		this.sumVertexDegRemEdge = sumVertexDegRemEdge;
	}

	/** Returns the temporarily stored sum of vertex degrees computed for add an edge operation.
	 * @return - double value for sum
	 */
	public double getSumVertexDegAddEdgeTemp() {
		return sumVertexDegAddEdgeTemp;
	}

	/** Update the temporarily stored sum of vertex degrees computed for add an edge operation.
	 * @param sumVertexDegAddEdgeTemp
	 */
	public void setSumVertexDegAddEdgeTemp(double sumVertexDegAddEdgeTemp) {
		this.sumVertexDegAddEdgeTemp = sumVertexDegAddEdgeTemp;
	}

	/** Returns the temporarily stored sum of vertex degrees computed for remove an edge operation.
	 * @return - double value for sum
	 */
	public double getSumVertexDegRemEdgeTemp() {
		return sumVertexDegRemEdgeTemp;
	}

	/** Update the temporarily stored sum of vertex degrees computed for remove an edge operation.
	 * @param sumVertexDegRemEdgeTemp - double value for sum.
	 */
	public void setSumVertexDegRemEdgeTemp(double sumVertexDegRemEdgeTemp) {
		this.sumVertexDegRemEdgeTemp = sumVertexDegRemEdgeTemp;
	}

	/** Returns the number of vertices for the graph.
	 * @return - double value for number of vertices.
	 */
	public double getNumberOfVertices() {
		return numberOfVertices;
	}

	/** Updates the number of vertices for the graph.
	 * @param numberOfVertices - double value for number of vertices.
	 */
	public void setNumberOfVertices(double numberOfVertices) {
		this.numberOfVertices = numberOfVertices;
	}
}
