package org.aksw.simba.lemming;


import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GrphBasedGraph;

import grph.Grph;

public class CompatibleGraph extends GrphBasedGraph {

	public CompatibleGraph() {
		super();
	}

	public CompatibleGraph(Graph other) {
		super(other);
	}
	
	public Grph getGrph() {
		return super.graph;
	}
}
