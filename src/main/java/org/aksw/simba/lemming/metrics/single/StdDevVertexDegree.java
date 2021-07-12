package org.aksw.simba.lemming.metrics.single;

import java.util.ArrayList;
import java.util.List;

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
	 * The method calculates the StdDev of an array of updated degrees. It fetches
	 * the previous variance and mean and uses formula described in the link to
	 * calculate new StdDev Value. If previously variance, average were not
	 * calculated, it will calculate them once
	 * 
	 * {@link https://math.stackexchange.com/q/3112715}
	 * 
	 * @param triple         - edge on which graph operation is performed.
	 * @param graph          - input graph.
	 * @param graphOperation - boolean value indicating graph operation. ("true" for
	 *                       adding an edge and "false" for removing an edge)
	 * @param previousResult - UpdatableMetricResult object containing the previous
	 *                       computed results.
	 * @param mVertexDegrees - Object of VertexDegrees class which contains degrees
	 *                       of all the nodes
	 * @return UpdatableMetricResult object.
	 */
	@Override
	public UpdatableMetricResult update(TripleBaseSingleID triple, ColouredGraph graph, boolean graphOperation,
			UpdatableMetricResult previousResult, VertexDegrees mVertexDegrees) {

		StdDevVertexDegreeMetricResult metricResultObj = new StdDevVertexDegreeMetricResult(getName(), 0.0);
		if (previousResult instanceof StdDevVertexDegreeMetricResult) {
			// Copying previously computed values in temporary variables
			metricResultObj.setVarianceVertexInDegree(
					((StdDevVertexDegreeMetricResult) previousResult).getVarianceVertexInDegree());
			metricResultObj
					.setAvgVertexInDegree(((StdDevVertexDegreeMetricResult) previousResult).getAvgVertexInDegree());
			metricResultObj.setVarianceVertexOutDegree(
					((StdDevVertexDegreeMetricResult) previousResult).getVarianceVertexOutDegree());
			metricResultObj
					.setAvgVertexOutDegree(((StdDevVertexDegreeMetricResult) previousResult).getAvgVertexOutDegree());
			metricResultObj
					.setNumberOfVertices(((StdDevVertexDegreeMetricResult) previousResult).getNumberOfVertices());
		}

		double avg = 0;
		double variance = 0;
		double numberOfVertices = 1;
		double oldDegree = 0;

		if (this.direction == DIRECTION.in) { // Calculate Std Dev of in degrees
			if (graphOperation) { // If Edge is added
				if (metricResultObj.getAvgVertexInDegree() == 0.0) {
					// no previous result is found
					List<Double> listOfValues = computeValuesForFirstTime(mVertexDegrees);
					// get values of average and variance from the list
					numberOfVertices = listOfValues.get(0);
					avg = listOfValues.get(1);
					variance = listOfValues.get(2);
					metricResultObj.setAvgVertexOutDegree(avg);
				} else {
					// get values of average and variance from previous result
					numberOfVertices = metricResultObj.getNumberOfVertices();
					avg = metricResultObj.getAvgVertexInDegree();
					variance = metricResultObj.getVarianceVertexInDegree();
					oldDegree = mVertexDegrees.getVertexInDegree(triple.headId);
					variance = computeVarianceFromPreviousResult(numberOfVertices, avg, variance, oldDegree,
							graphOperation);
					metricResultObj.setAvgVertexInDegree(avg + (1 / numberOfVertices));
				}
			} else { // If Edge is removed
				if (metricResultObj.getAvgVertexInDegree() == 0.0) {
					// no previous result is found
					List<Double> listOfValues = computeValuesForFirstTime(mVertexDegrees);
					// get values of average and variance from the list
					numberOfVertices = listOfValues.get(0);
					avg = listOfValues.get(1);
					variance = listOfValues.get(2);
					metricResultObj.setAvgVertexInDegree(avg);
				} else {
					// get values of average and variance from previous result
					numberOfVertices = metricResultObj.getNumberOfVertices();
					avg = metricResultObj.getAvgVertexInDegree();
					variance = metricResultObj.getVarianceVertexInDegree();
					oldDegree = mVertexDegrees.getVertexInDegree(triple.headId);
					variance = computeVarianceFromPreviousResult(numberOfVertices, avg, variance, oldDegree,
							graphOperation);
					metricResultObj.setAvgVertexInDegree(avg - (1 / numberOfVertices));
				}
			}
			metricResultObj.setVarianceVertexInDegree(variance);
		} else if (this.direction == DIRECTION.out) { // Calculate Std Dev out degrees
			if (graphOperation) { // If Edge is added
				if (metricResultObj.getAvgVertexOutDegree() == 0.0) {
					List<Double> listOfValues = computeValuesForFirstTime(mVertexDegrees);
					numberOfVertices = listOfValues.get(0);
					avg = listOfValues.get(1);
					variance = listOfValues.get(2);
					metricResultObj.setAvgVertexOutDegree(avg);
				} else {
					numberOfVertices = metricResultObj.getNumberOfVertices();
					avg = metricResultObj.getAvgVertexOutDegree();
					variance = metricResultObj.getVarianceVertexOutDegree();
					oldDegree = mVertexDegrees.getVertexOutDegree(triple.tailId);
					variance = computeVarianceFromPreviousResult(numberOfVertices, avg, variance, oldDegree,
							graphOperation);
					metricResultObj.setAvgVertexOutDegree(avg + (1 / numberOfVertices));
				}
			} else { // If Edge is removed
				if (metricResultObj.getAvgVertexOutDegree() == 0.0) {
					List<Double> listOfValues = computeValuesForFirstTime(mVertexDegrees);
					numberOfVertices = listOfValues.get(0);
					avg = listOfValues.get(1);
					variance = listOfValues.get(2);
					metricResultObj.setAvgVertexOutDegree(avg);
				} else {
					numberOfVertices = metricResultObj.getNumberOfVertices();
					avg = metricResultObj.getAvgVertexOutDegree();
					variance = metricResultObj.getVarianceVertexOutDegree();
					oldDegree = mVertexDegrees.getVertexOutDegree(triple.tailId);
					variance = computeVarianceFromPreviousResult(numberOfVertices, avg, variance, oldDegree,
							graphOperation);
					metricResultObj.setAvgVertexOutDegree(avg - (1 / numberOfVertices));
				}
			}
			metricResultObj.setVarianceVertexOutDegree(variance);
		}

		metricResultObj.setNumberOfVertices(numberOfVertices);
		metricResultObj.setResult(Math.sqrt(variance));
		return metricResultObj;
	}

	/**
	 * This method calculates the StdDev of an array given the number of vertices
	 * and the previous variance and mean are known. It uses the formula mentioned
	 * in the link
	 * 
	 * {@link https://math.stackexchange.com/q/3112715}
	 * 
	 * @param numberOfVertices - total number of nodes in the graph
	 * @param avg              - average of previous set of nodes in the graph
	 * @param variance         - variance of previous set of nodes
	 * @param oldDegree        - the degree which was updated after adding or
	 *                         removing an edge
	 * @param graphOperation   - denotes if an edge was added or removed
	 * @return Double - new variance
	 */
	private double computeVarianceFromPreviousResult(double numberOfVertices, double avg, double variance,
			double oldDegree, boolean graphOperation) {

		double flag = graphOperation ? 1 : -1;
		double newDegree = oldDegree + flag;
		double newAvg = avg + (flag / numberOfVertices);
		return (variance + Math.pow(numberOfVertices, -2)
				+ (Math.pow((newDegree - newAvg), 2) - Math.pow((oldDegree - newAvg), 2)) / numberOfVertices);
	}

	/**
	 * TODO: Check if this can be replaced by apply method (The apply method does
	 * not return average or number of nodes)
	 * 
	 * This method calculates the average and Variance for the first time.
	 * 
	 * @param mVertexDegrees - degrees of all nodes
	 * @return List<Double> - a list containing number of nodes, average and
	 *         variance in that order.
	 */
	private List<Double> computeValuesForFirstTime(VertexDegrees mVertexDegrees) {
		List<Double> listOfValues = new ArrayList<Double>();
		int[] vertexDegrees;

		if (this.direction == DIRECTION.in) {
			vertexDegrees = mVertexDegrees.getMapVerticesInDegree();
		} else {
			vertexDegrees = mVertexDegrees.getMapVerticesOutDegree();
		}
		double sumOfDegrees = 0.0;
		int numberOfVertices = vertexDegrees.length;
		for (int i = 0; i < numberOfVertices; i++) { // Compute sum in iteration
			sumOfDegrees += vertexDegrees[i];
		}
		double averageOfDegrees = sumOfDegrees / numberOfVertices;
		double sumForVariance = 0.0;
		double temp;
		for (int i = 0; i < numberOfVertices; ++i) {
			temp = averageOfDegrees - vertexDegrees[i];
			temp *= temp;
			sumForVariance += temp;
		}

		double variance = sumForVariance / numberOfVertices;

		listOfValues.add((double) numberOfVertices);
		listOfValues.add(averageOfDegrees);
		listOfValues.add(variance);

		return listOfValues;
	}
}
