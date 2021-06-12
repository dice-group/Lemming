package org.aksw.simba.lemming.metrics.single;

public class AvgVertexDegreeMetricResult extends SimpleMetricResult {

	private double sumVertexDegAddEdge = 0.0; // Variable for storing sum of vertex degrees for Add an Edge
	private double sumVertexDegRemEdge = 0.0; // Variable for storing sum of Vertex degrees for Remove an Edge

	private double sumVertexDegAddEdgeTemp = 0.0; // Temp variable for storing sum of vertex degrees for Add an Edge
	private double sumVertexDegRemEdgeTemp = 0.0; // Temp variable for storing sum of Vertex degrees for Remove an Edge

	private double numberOfVertices = 0.0; // Variable for storing number of vertices.

	public AvgVertexDegreeMetricResult(String metricName, double result) {
		super(metricName, result);
	}

	public double getSumVertexDegAddEdge() {
		return sumVertexDegAddEdge;
	}

	public void setSumVertexDegAddEdge(double sumVertexDegAddEdge) {
		this.sumVertexDegAddEdge = sumVertexDegAddEdge;
	}

	public double getSumVertexDegRemEdge() {
		return sumVertexDegRemEdge;
	}

	public void setSumVertexDegRemEdge(double sumVertexDegRemEdge) {
		this.sumVertexDegRemEdge = sumVertexDegRemEdge;
	}

	public double getSumVertexDegAddEdgeTemp() {
		return sumVertexDegAddEdgeTemp;
	}

	public void setSumVertexDegAddEdgeTemp(double sumVertexDegAddEdgeTemp) {
		this.sumVertexDegAddEdgeTemp = sumVertexDegAddEdgeTemp;
	}

	public double getSumVertexDegRemEdgeTemp() {
		return sumVertexDegRemEdgeTemp;
	}

	public void setSumVertexDegRemEdgeTemp(double sumVertexDegRemEdgeTemp) {
		this.sumVertexDegRemEdgeTemp = sumVertexDegRemEdgeTemp;
	}

	public double getNumberOfVertices() {
		return numberOfVertices;
	}

	public void setNumberOfVertices(double numberOfVertices) {
		this.numberOfVertices = numberOfVertices;
	}
}
