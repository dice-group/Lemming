package org.aksw.simba.lemming.metrics.single.edgetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.metricselection.EdgeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

public class EdgeTriangleMetric extends AbstractMetric implements SingleValueMetric{

	public EdgeTriangleMetric() {
		super("#edgetriangles");
	}
	
	@Override
	public double apply(ColouredGraph graph) {
		
		EdgeTriangleMetricSelection selector = new EdgeTriangleMetricSelection();
		SingleValueMetric edgeTriangleMetric = selector.getMinComplexityMetric(graph);
		
		//get number of edge triangles
		return edgeTriangleMetric.apply(graph);
	}

}
