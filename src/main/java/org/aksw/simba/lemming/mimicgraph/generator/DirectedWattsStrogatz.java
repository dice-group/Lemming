package org.aksw.simba.lemming.mimicgraph.generator;

import grph.Grph;
import grph.algo.topology.RandomNewmanWattsStrogatzTopologyGenerator;
import it.unimi.dsi.fastutil.ints.IntSet;
import toools.collections.primitive.IntCursor;

/**
 * 
 * Watts-Strogatz adapted from
 * {@link RandomNewmanWattsStrogatzTopologyGenerator}, It produces a directed
 * small-world graph.
 * 
 * @author Alexandra Silva
 */
public class DirectedWattsStrogatz extends RandomNewmanWattsStrogatzTopologyGenerator {

	/** Number of neighbours */
	private int k = 1;

	/** Probability of rewiring */
	private double p = 0.5;

	
	/**
	 * FIXME
	 */
	@Override
	public void compute(Grph g) {
		int n = g.getVertices().size();

		// generate a ring and connect each node to k neighbours
		ring(g);
		connectToKClosestNeighbors(g);

		// rewire each edge with a probability of p
		// skip if the network is a clique
		if (k < n / 2) {
			for (int u : g.getVertices().toIntArray()) {
				for (int e : g.getOutEdges(u)) {
//				for (int e : g.getEdgesIncidentTo(u).toIntArray()) {
					// if the vertex is not connected to all vertices, there's
					// room for a shortcut
					if (g.getVertexDegree(u, Grph.TYPE.vertex, Grph.DIRECTION.in_out) < n - 1) {
						int v = g.getTheOtherVertex(e, u);

						if (getPRNG().nextDouble() < getP()) {
							int w;
							do {
								w = g.getVertices().pickRandomElement(getPRNG());

							} while (w == u || g.areVerticesAdjacent(u, w));

							// rewire from u - v to u - w
							// TODO random direction?
							g.addUndirectedSimpleEdge(u, w);
							g.removeEdge(e);
						}
					}
				}
			}
		}
	}

	/**
	 * FIXME
	 * 
	 * @param graph
	 */
	public void connectToKClosestNeighbors(Grph graph) {
		IntSet[] neighbors = new IntSet[graph.getVertices().getGreatest() + 1];

		for (IntCursor v : IntCursor.fromFastUtil(graph.getVertices())) {
			neighbors[v.value] = graph.getKClosestNeighbors(v.value, k, null);
		}

		for (IntCursor v : IntCursor.fromFastUtil(graph.getVertices())) {
			for (IntCursor n : IntCursor.fromFastUtil(neighbors[v.value])) {
				if (graph.getEdgesConnecting(v.value, n.value).isEmpty()) {
					graph.addDirectedSimpleEdge(v.value, n.value);
				}
			}
		}
	}

	/**
	 * 
	 * TODO Create ring with random direction
	 * 
	 * @param graph
	 * @param vertices
	 * @param directed
	 * @return
	 */
	public static IntSet ring(Grph graph) {
		// as many edges as vertices
//		IntSet edges = new SelfAdaptiveIntSet(vertices.size());
//
//		if (vertices.size() > 1)
//		{
//			IntIterator i = vertices.iterator();
//			int predecessor = i.nextInt();
//			int first = predecessor;
//
//			while (i.hasNext())
//			{
//				int a = i.nextInt();
//				edges.add(graph.addSimpleEdge(predecessor, a, directed));
//				predecessor = a;
//			}
//
//			edges.add(graph.addSimpleEdge(predecessor, first, directed));
//		}

		return null;
	}

	public static void compute(Grph g, int k, double p) {
		RandomNewmanWattsStrogatzTopologyGenerator tg = new DirectedWattsStrogatz();
		tg.setK(k);
		tg.setP(p);
		tg.compute(g);
	}

}
