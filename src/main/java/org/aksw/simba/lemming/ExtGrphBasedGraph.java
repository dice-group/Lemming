package org.aksw.simba.lemming;

import org.dice_research.ldcbench.graph.Graph;
import org.dice_research.ldcbench.graph.GrphBasedGraph;

import grph.Grph;

/**
 * An Extendable class of {@link GrphBasedGraph}.
 * <p>
 * As of yet, needed mainly to retrieve the {@link Grph} object from
 * {@link GrphBasedGraph} after the graph generation process
 * 
 * @author Alexandra Silva
 *
 */
public class ExtGrphBasedGraph extends GrphBasedGraph {

	public ExtGrphBasedGraph() {
		super();
	}

	public ExtGrphBasedGraph(Graph other) {
		super(other);
	}

	public Grph getGrph() {
		return super.graph;
	}
}
