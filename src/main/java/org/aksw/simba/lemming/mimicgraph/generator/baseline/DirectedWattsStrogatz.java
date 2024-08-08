package org.aksw.simba.lemming.mimicgraph.generator.baseline;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Random;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
@Component("WS")
@Scope(value = "prototype")
public class DirectedWattsStrogatz extends RandomNewmanWattsStrogatzTopologyGenerator implements IGenerator {

	@Override
	public void compute(Grph g) {
		// manually disabling print stmts since called library has them
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		}));
		
		Random rnd = getPRNG();

		// get undirected graph
		super.compute(g);

		// replace all undirected connections
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
	public static void compute(Grph g, int k, double p, long seed) {
		RandomNewmanWattsStrogatzTopologyGenerator tg = new DirectedWattsStrogatz();
		tg.setPRNG(new Random(seed));
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

	@Override
	public Grph generateGraph(int noVertices, double avgDegree, long seed) {
		Grph g = new InMemoryGrph();
		g.addNVertices(noVertices);
		DirectedWattsStrogatz.compute(g, 4, 0.5, seed);
		return g;
	}

}
