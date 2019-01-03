package org.aksw.simba.lemming.metrics.single.nodetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.metricselection.NodeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

public class NodeTriangleMetric extends AbstractMetric implements SingleValueMetric{
	
	public NodeTriangleMetric(){
		super("#nodetriangles");
	}

	@Override
	public double apply(ColouredGraph graph) {
		
		NodeTriangleMetricSelection selector = new NodeTriangleMetricSelection();
		SingleValueMetric nodeTriangleMetric = selector.getMinComplexityMetric(graph);
		
		//get number of edge triangles
		return nodeTriangleMetric.apply(graph);
	}

}
