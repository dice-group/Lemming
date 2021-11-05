package org.aksw.simba.lemming.metrics.single.edgetriangles;

import grph.Grph;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.metricselection.EdgeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.IntSetUtil;

import javax.annotation.Nonnull;

public class EdgeTriangleMetric extends AbstractMetric implements SingleValueMetric{

	public EdgeTriangleMetric() {
		super("#edgetriangles");
	}
	
	@Override
	public UpdatableMetricResult apply(ColouredGraph graph) {
		
		EdgeTriangleMetricSelection selector = new EdgeTriangleMetricSelection();
		SingleValueMetric edgeTriangleMetric = selector.getMinComplexityMetric(graph);
		
		//get number of edge triangles
		double result = edgeTriangleMetric.apply(graph).getResult();
		return new SingleValueMetricResult(this.name, result);
	}

	//@Override //TODO: parameters' form not determined, especially the graphOperation
	public UpdatableMetricResult update(@Nonnull TripleBaseSingleID triple, @Nonnull ColouredGraph graph, boolean graphOperation,
										@Nonnull UpdatableMetricResult previousResult){

		Grph grph = graph.getGraph();
		IntSet verticesConnectedToRemovingEdge = grph.getVerticesIncidentToEdge(triple.edgeId);

		int headId = verticesConnectedToRemovingEdge.size() > 1 ? verticesConnectedToRemovingEdge.toIntArray()[1]
				: verticesConnectedToRemovingEdge.toIntArray()[0];
		int tailId = verticesConnectedToRemovingEdge.toIntArray()[0];

		int numEdgesBetweenVertices = IntSetUtil.intersection(grph.getEdgesIncidentTo(tailId), grph.getEdgesIncidentTo(headId)).size();

		int change = graphOperation ? -1 : 1 ;
		int differenceOfSubGraph = calculateDifferenceOfSubGraphEdge(grph, headId, tailId, numEdgesBetweenVertices, change);
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
	private int calculateDifferenceOfSubGraphEdge(Grph grph, int headId, int tailId, int numEdgesBetweenVertices, int change) {
		int oldSubGraphEdgeTriangles = 0;
		int newSubGraphTriangles = 0;
		int newNumEdgesBetweenVertices = numEdgesBetweenVertices + change;

		for (int vertex : MetricUtils.getVerticesInCommon(grph, headId, tailId)) {
			int numEdgesFromHead = IntSetUtil.intersection(grph.getEdgesIncidentTo(headId),
					grph.getEdgesIncidentTo(vertex)).size();
			int numEdgesFromTail = IntSetUtil.intersection(grph.getEdgesIncidentTo(tailId),
					grph.getEdgesIncidentTo(vertex)).size();
			int mul = numEdgesFromHead * numEdgesFromTail;
			oldSubGraphEdgeTriangles += (mul * numEdgesBetweenVertices);
			newSubGraphTriangles += (mul * newNumEdgesBetweenVertices);
		}

		return change==-1 ? (oldSubGraphEdgeTriangles-newSubGraphTriangles) : (newSubGraphTriangles-oldSubGraphEdgeTriangles);
	}

}
