package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.VertexDegrees;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class StdDevVertexDegree extends AvgVertexDegreeMetric {

	protected DIRECTION direction;

	public StdDevVertexDegree(DIRECTION direction) {
		super(direction == DIRECTION.in ? "stdDevInDegree" : "stdDevOutDegree");
		this.direction = direction;
	}

	@Override
	public double apply(ColouredGraph graph) {
		IntArrayList degrees = null;
		if (direction == DIRECTION.in) {
			degrees = graph.getGraph().getAllInEdgeDegrees();
		} else {
			degrees = graph.getGraph().getAllOutEdgeDegrees();
		}
		return calculateStdDev(degrees, calculateAvg(degrees));
	}

	protected double calculateStdDev(IntArrayList degrees, double avg) {
		double temp, sum = 0;
		for (int i = 0; i < degrees.size(); ++i) {
			temp = avg - degrees.getInt(i);
			temp *= temp;
			sum += temp;
		}
		return Math.sqrt(sum / degrees.size());
	}

	/**
	 * The method calculates the StdDev of an array of updated degrees.
	 * 
	 * @param triple         - edge on which graph operation is performed.
	 * @param graph          - input graph.
	 * @param graphOperation - boolean value indicating graph operation. ("true" for
	 *                       adding an edge and "false" for removing an edge)
	 * @param previousResult - UpdatableMetricResult object containing the previous
	 *                       computed results.
	 * @param mVertexDegrees - Object of VertexDegrees class which contains degrees
	 *                       of all the nodes
	 * @return SimpleMetricResult object.
	 */
	@Override
	public SimpleMetricResult update(TripleBaseSingleID triple, ColouredGraph graph, boolean graphOperation,
			UpdatableMetricResult previousResult, VertexDegrees mVertexDegrees) {
		SimpleMetricResult newMetricResult = new SimpleMetricResult(this.name, 0);
		int[] degreesArray;
		IntArrayList degreesList;
		if (this.direction == DIRECTION.in) {
			degreesArray = mVertexDegrees.getMapVerticesInDegree();
			degreesList = new IntArrayList(degreesArray);
			newMetricResult.setResult(calculateStdDev(degreesList, calculateAvg(degreesList)));
		} else if (this.direction == DIRECTION.out) {
			degreesArray = mVertexDegrees.getMapVerticesOutDegree();
			degreesList = new IntArrayList(degreesArray);
			newMetricResult.setResult(calculateStdDev(degreesList, calculateAvg(degreesList)));
		} else {
			// If metric is other than stdDevInDegree and stdDevOutDegree then apply the
			// metric
			newMetricResult = (SimpleMetricResult) applyUpdatable(graph);
		}

		return newMetricResult;
	}
}
