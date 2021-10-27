package org.aksw.simba.lemming.metrics.single.edgetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.metricselection.EdgeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.result.EdgeTriangleMetricResult;
import org.aksw.simba.lemming.metrics.single.result.SingleValueMetricResult;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

public class EdgeTriangleMetric extends AbstractMetric implements SingleValueMetric{

	public EdgeTriangleMetric() {
		super("#edgetriangles");
	}
	
	@Override
	public SingleValueMetricResult apply(ColouredGraph graph) {

		EdgeTriangleMetricSelection selector = new EdgeTriangleMetricSelection();
		SingleValueMetric edgeTriangleMetric = selector.getMinComplexityMetric(graph);
		
		//get number of edge triangles
		return edgeTriangleMetric.apply(graph);
	}

	public EdgeTriangleMetricResult update(ColouredGraph graph, TripleBaseSingleID triple, EdgeTriangleMetricResult result){

	}

}
