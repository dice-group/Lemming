package org.aksw.simba.lemming.metrics.single.edgetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.metricselection.EdgeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.IntSetUtil;

import javax.annotation.Nonnull;

public class EdgeTriangleMetric extends AbstractMetric implements SingleValueMetric{

	public EdgeTriangleMetric() {
		super("#edgetriangles");
	}
	
	@Override
	public double apply(ColouredGraph graph) {
		
		EdgeTriangleMetricSelection selector = new EdgeTriangleMetricSelection();
		SingleValueMetric edgeTriangleMetric = selector.getMinComplexityMetric(graph);

		return edgeTriangleMetric.apply(graph);
	}

	/**
	 * @param graph   the given graph is not modified!
	 */
	@Override
	public UpdatableMetricResult update(@Nonnull ColouredGraph graph, @Nonnull TripleBaseSingleID triple, @Nonnull Operation opt,
										@Nonnull UpdatableMetricResult previousResult){

		int headId = triple.headId;
		int tailId = triple.tailId;

		//if headId = tailId, result is not change.
		if(headId == tailId){
			return previousResult;
		}

		int change = opt==Operation.REMOVE ? -1 : 1 ;
		int numEdgesBetweenVertices = IntSetUtil.intersection(graph.getEdgesIncidentTo(tailId), graph.getEdgesIncidentTo(headId)).size();

		int differenceOfSubGraph = calculateDifferenceOfSubGraphEdge(graph, headId, tailId, numEdgesBetweenVertices, change);
		double newResult = previousResult.getResult() + change*differenceOfSubGraph;
		newResult = newResult >= 0 ? newResult: 0;

		return new SingleValueMetricResult(previousResult.getMetricName(), newResult);
	}

	/**
	 * It is used to calculate the difference of edge triangles in subgraph after removing or adding an edge
	 * @param headId head of the modified edge
	 * @param tailId tail of the modified edge
	 * @param numEdgesBetweenVertices number of the edges between head and tail
	 * @param change removing an edge, then -1, add an edge, then +1
	 * @return the difference of edge triangles in subgraph after removing or adding an edge
	 */
	private int calculateDifferenceOfSubGraphEdge(ColouredGraph graph, int headId, int tailId, int numEdgesBetweenVertices, int change) {
		int oldSubGraphEdgeTriangles = 0;
		int newSubGraphTriangles = 0;
		int newNumEdgesBetweenVertices = numEdgesBetweenVertices + change;

		for (int vertex : MetricUtils.getVerticesInCommon(graph, headId, tailId)) {
			int numEdgesFromHead = IntSetUtil.intersection(graph.getEdgesIncidentTo(headId),
					graph.getEdgesIncidentTo(vertex)).size();
			int numEdgesFromTail = IntSetUtil.intersection(graph.getEdgesIncidentTo(tailId),
					graph.getEdgesIncidentTo(vertex)).size();
			int mul = numEdgesFromHead * numEdgesFromTail;
			oldSubGraphEdgeTriangles += (mul * numEdgesBetweenVertices);
			newSubGraphTriangles += (mul * newNumEdgesBetweenVertices);
		}

		return change==-1 ? (oldSubGraphEdgeTriangles-newSubGraphTriangles) : (newSubGraphTriangles-oldSubGraphEdgeTriangles);
	}

}
