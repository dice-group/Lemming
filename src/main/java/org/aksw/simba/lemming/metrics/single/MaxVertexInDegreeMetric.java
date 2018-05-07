package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

public class MaxVertexInDegreeMetric extends AbstractMetric implements
		SingleValueMetric {

	public MaxVertexInDegreeMetric() {
		super("maxInDegree");
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return "maxInDegree";
	}

	@Override
	public double apply(ColouredGraph graph) {
		return graph.getGraph().getMaxInVertexDegrees();
	}

}
