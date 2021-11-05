package org.aksw.simba.lemming.metrics.single.nodetriangles;

import grph.Grph;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.metricselection.NodeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.IntSetUtil;

import javax.annotation.Nonnull;

public class NodeTriangleMetric extends AbstractMetric implements SingleValueMetric{
	
	public NodeTriangleMetric(){
		super("#nodetriangles");
	}

	@Override
	public UpdatableMetricResult apply(ColouredGraph graph) {
		
		NodeTriangleMetricSelection selector = new NodeTriangleMetricSelection();
		SingleValueMetric nodeTriangleMetric = selector.getMinComplexityMetric(graph);
		
		//get number of edge triangles
		return new SingleValueMetricResult(this.name, nodeTriangleMetric.apply(graph).getResult());
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

		int numberOfCommon = MetricUtils.getVerticesInCommon(grph, headId, tailId).size();

		//the previous result could be maintained except for 2 cases:
		double newResult = previousResult.getResult();

		//1.case: remove an edge, and number of edges between head and tail is 1
		// -> new metric = old metric - number of common vertices
		if(numEdgesBetweenVertices==1 && graphOperation){
			newResult = newResult - numberOfCommon;

		//2.case: add an edge, and number of edges between head and tail is 0
		// -> new metric = old metric + number of common vertices
		}else if (numEdgesBetweenVertices==0 && (!graphOperation)){
			newResult = newResult + numberOfCommon;
		}

		return new SingleValueMetricResult(previousResult.getMetricName(), newResult);
	}
}
