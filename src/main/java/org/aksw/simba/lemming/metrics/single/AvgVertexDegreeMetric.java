package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.VertexDegrees;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * This metric determines the average degree of outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class AvgVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

	public AvgVertexDegreeMetric() {
		super("avgDegree");
	}

	protected AvgVertexDegreeMetric(String name) {
		super(name);
	}

	@Override
	public double apply(ColouredGraph graph) {
		return calculateAvg(graph.getGraph().getAllInEdgeDegrees());
	}

	protected double calculateAvg(IntArrayList degrees) {
		double sum = 0;
		for (int i = 0; i < degrees.size(); ++i) {
			sum += degrees.getInt(i);
		}
		return sum / degrees.size();
	}

	/**
	 * The method contains logic that computes the average vertex degree metric
	 * efficiently. If the metric is computed for the first time then it uses the
	 * degrees stored in VertexDegrees else it will update the previously stored sum
	 * value.
	 * 
	 * @param triple         - edge on which graph operation is performed.
	 * @param metric         - input metric which needs to be computed.
	 * @param graph          - input graph.
	 * @param graphOperation - boolean value indicating graph operation. ("true" for
	 *                       adding an edge and "false" for removing an edge)
	 * @param previousResult - UpdatableMetricResult object containing the previous
	 *                       computed results.
	 * @return UpdatableMetricResult object with updated values that can be used in
	 *         further computations
	 */
	@Override
	public UpdatableMetricResult update(TripleBaseSingleID triple, ColouredGraph graph, boolean graphOperation,
			UpdatableMetricResult previousResult, VertexDegrees mVertexDegrees) {

		AvgVertexDegreeMetricResult metricResultTempObj = new AvgVertexDegreeMetricResult(getName(), 0.0);
		if (previousResult instanceof AvgVertexDegreeMetricResult) {
			// Copying previously computed values in temporary variables
			metricResultTempObj.setSumVertexDeg(((AvgVertexDegreeMetricResult) previousResult).getSumVertexDeg());
			metricResultTempObj
					.setNumberOfVertices(((AvgVertexDegreeMetricResult) previousResult).getNumberOfVertices());
		}

		double sum = 0;
		double numberOfVertices = 1;
		if (graphOperation) { // If Add an Edge

			if (metricResultTempObj.getSumVertexDeg() == 0.0) {
				// Computing the Avg Vertex Degree Metric for the first time

				// Get the Array from VertexDegrees class (Note: This can be replaced with
				// getAllInEdgeDegrees method of Grph package)
				int[] getmMapVerticesinDegree = mVertexDegrees.getMapVerticesInDegree();
				for (int key = 0; key < getmMapVerticesinDegree.length; key++) { // Compute sum in iteration
					sum += getmMapVerticesinDegree[key];
				}
				numberOfVertices = getmMapVerticesinDegree.length;

			} else { // Re-using the previously computed values
				sum = metricResultTempObj.getSumVertexDeg() + 1;
				// Get the previous computed sum and add 1 to previous sum since edge is added.
				numberOfVertices = metricResultTempObj.getNumberOfVertices();
			}

			// Set values in Temporary object
			metricResultTempObj.setSumVertexDeg(sum);
			metricResultTempObj.setNumberOfVertices(numberOfVertices);

		} else { // If Remove an Edge

			if (metricResultTempObj.getSumVertexDeg() == 0.0) {
				// Computing the Avg Vertex Degree Metric for the first time

				int[] getmMapVerticesinDegree = mVertexDegrees.getMapVerticesInDegree();

				for (int key = 0; key < getmMapVerticesinDegree.length; key++) { // Compute sum in iteration
					sum += getmMapVerticesinDegree[key];
				}
				numberOfVertices = getmMapVerticesinDegree.length;

			} else { // Re-using the previously computed values
				sum = metricResultTempObj.getSumVertexDeg() - 1;
				// Get the previous computed sum and subtract 1 to previous sum since edge is
				// removed.

				numberOfVertices = metricResultTempObj.getNumberOfVertices();

			}

			metricResultTempObj.setSumVertexDeg(sum);
			metricResultTempObj.setNumberOfVertices(numberOfVertices);
		}

		sum = sum / numberOfVertices; // Compute Metric value
		metricResultTempObj.setResult(sum);

		return metricResultTempObj;
	}

}
