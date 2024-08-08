package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.Random;

import grph.Grph;
import grph.algo.topology.RandomNewmanWattsStrogatzTopologyGenerator;
import grph.in_memory.InMemoryGrph;

/**
 * 
 * Watts-Strogatz model.
 * 
 * It generates an undirected graph from
 * {@link RandomNewmanWattsStrogatzTopologyGenerator} and the edge directions
 * are added randomly afterwards.
 * 
 * @author Alexandra Silva
 */
public class DirectedWattsStrogatz extends RandomNewmanWattsStrogatzTopologyGenerator {

	@Override
	public void compute(Grph g) {
		// get undirected graph
		super.compute(g);

		// replace all undirected connections
		Random rnd = getPRNG();
		for (int e : g.getEdges()) {
			int v1 = g.getOneVertex(e);
			int v2 = g.getTheOtherVertex(e, v1);

			// assign directions randomly
			if (rnd.nextBoolean()) {
				g.addDirectedSimpleEdge(v1, v2);
			} else {
				g.addDirectedSimpleEdge(v2, v1);
			}
			
			// remove undirected edge
			g.removeEdge(e);
		}
	}

	/**
	 * Computes a directed WS graph.
	 * 
	 * @param g The Graph
	 * @param k Number of neighbours
	 * @param p Probability of rewiring
	 */
	public static void compute(Grph g, int k, double p) {
		RandomNewmanWattsStrogatzTopologyGenerator tg = new DirectedWattsStrogatz();
		tg.setK(k);
		tg.setP(p);
		tg.compute(g);
	}

	public static void main(String[] args) {
		Grph g = new InMemoryGrph();
		g.addNVertices(15);
		DirectedWattsStrogatz.compute(g, 4, 0.5);
		g.display();
	}

}
