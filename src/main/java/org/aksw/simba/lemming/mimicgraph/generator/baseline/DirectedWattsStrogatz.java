package org.aksw.simba.lemming.mimicgraph.generator.baseline;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.util.Constants;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import grph.DefaultIntSet;
import grph.Grph;
import grph.algo.topology.RandomNewmanWattsStrogatzTopologyGenerator;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import toools.collections.primitive.IntCursor;

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
public class DirectedWattsStrogatz implements IGenerator {

	private Random rnd;

	private double k;

	private double p;

	public void compute(Grph g) {

		// get undirected graph
		int n = g.getVertices().size();
		g.ring();
		connectToKClosestNeighbors(g, k);

		// if the network is a clique
		if (k < n / 2) {
			for (int u : g.getVertices().toIntArray()) {
				for (int e : g.getEdgesIncidentTo(u).toIntArray()) {
					// if the vertex is not connected to all vertices, there's
					// room for a shortcut
					if (g.getVertexDegree(u, Grph.TYPE.vertex, Grph.DIRECTION.in_out) < n - 1) {
						int v = g.getTheOtherVertex(e, u);
						if (rnd.nextDouble() < p) {
							int w;
							do {
								w = g.getVertices().pickRandomElement(rnd);
							} while (w == u || g.areVerticesAdjacent(u, w));
							g.addUndirectedSimpleEdge(u, w);
							g.removeEdge(e);
						}
					}
				}
			}
		}

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

	private void connectToKClosestNeighbors(Grph graph, double k) {

		int noVertices = graph.getVertices().getGreatest() + 1;
		
		// assign expected contributions of each vertex
		int[] edgeContributions = assignEdges(noVertices, k);

		// for each vertex, connect to right neighbours and then to right side
		for (IntCursor v : IntCursor.fromFastUtil(graph.getVertices())) {
			int expectedContribution = edgeContributions[v.value];

			IntSet[] neighbours = getKClosestNeighbours(noVertices, v.value, k);
			IntSet leftNe = neighbours[0];
			IntSet rightNe = neighbours[1];

			// right side neighbours first
			for (IntCursor n : IntCursor.fromFastUtil(rightNe)) {
				// check if connection already exists before adding it
				if (expectedContribution>0 && graph.getEdgesConnecting(v.value, n.value).isEmpty()) {
					expectedContribution--;
					graph.addUndirectedSimpleEdge(v.value, n.value);
				}
			}
			
			// left side now
			for (IntCursor n : IntCursor.fromFastUtil(leftNe)) {
				// check if connection already exists before adding it
				if (expectedContribution>0 && graph.getEdgesConnecting(v.value, n.value).isEmpty()) {
					expectedContribution--;
					graph.addUndirectedSimpleEdge(v.value, n.value);
				}

			}
		}
	}

	/**
	 * Assign the edge contributions randomly to each vertex.
	 * 
	 * @param vertices Total number of vertices
	 * @param k        Average degree
	 * @return Array with each edge contributions expected from each vertex.
	 */
	public int[] assignEdgesRandomly(int vertices, double k) {
		double edgesNo = vertices * k;
		int roundEdges = (int) Math.floor(edgesNo);
		double leftover = edgesNo - roundEdges;
		int[] contributions = new int[vertices];

		// for each expected edge, increase the contribution
		for (int i = 0; i < roundEdges; i++) {
			int vertex = rnd.nextInt(vertices);
			contributions[vertex]++;
		}

		// create an extra edge with leftover probability
		double rd = rnd.nextDouble();
		if (rd < leftover) {
			int vertex = rnd.nextInt(vertices);
			contributions[vertex]++;
		}
		return contributions;
	}

	/**
	 * Assign the edge contributions randomly to each vertex.
	 * 
	 * @param vertices Total number of vertices
	 * @param k        Average degree
	 * @return Array with each edge contributions expected from each vertex.
	 */
	public int[] assignEdges(int vertices, double k) {
		int roundK = (int) Math.floor(k);
		int[] contributions = new int[vertices];

		// assign floored K to each vertex
		for (int i = 0; i < vertices; i++) {
			contributions[i] = roundK;
		}

		// create extra edges with leftover randomly
		int leftoverEdges = (int) Math.floor((k - roundK) * vertices) ;
		for (int i = 0; i < leftoverEdges; i++) {
			int vertex = rnd.nextInt(vertices);
			contributions[vertex] ++;
		}

		return contributions;
	}

	/**
	 * Retrieves a vertex's left and right-side neighbours based on Math.ceil(k/2)
	 * assigned to each side. It assumes a ring lattice
	 * 
	 * @param noVertices Number of vertices in the graph
	 * @param v          Given vertex of interest
	 * @param k          Number of neighbours
	 * @return An array with the left and right-side neighbours
	 */
	public IntSet[] getKClosestNeighbours(int noVertices, int v, double k) {
		if (v > noVertices) {
			throw new IllegalArgumentException("Invalid vertex. Should be between [0, noVertices[.");
		}

		// get maximum number of neighbours required around a vertex
		int maxK = (int) Math.ceil(k / 2);

		// get maxK around the vertex
		int left = v - maxK < 0 ? v + noVertices - maxK : v - maxK;
		int right = v + maxK >= noVertices ? v - noVertices + maxK : v + maxK;

		// store left and right neighbours separately so we can sample from them
		// [left, v[
		Set<Integer> leftNeigh = getNeighbours(left, v - 1, noVertices);
		// ]v, right]
		Set<Integer> rightNeigh = getNeighbours(v + 1, right, noVertices);

		// prepare results
		IntSet[] neighbours = new IntSet[2];
		neighbours[0] = new IntArraySet(leftNeigh);
		neighbours[1] = new IntArraySet(rightNeigh);
		return neighbours;
	}

	/**
	 * Returns neighbours between 2 vertices in a ring.
	 * 
	 * @param lower
	 * @param upper
	 * @param max
	 * @return
	 */
	private IntSet getNeighbours(int lower, int upper, int max) {
		IntSet range = new DefaultIntSet(Constants.DEFAULT_SIZE);
		if (lower <= upper) {
			for (int i = lower; i <= upper; i++) {
				range.add(i);
			}
		} else {
			for (int i = lower; i < max; i++) {
				range.add(i);
			}
			for (int i = 0; i <= upper; i++) {
				range.add(i);
			}
		}
		return range;
	}

	public void setK(int k) {
		if (k < 0)
			throw new IllegalArgumentException("k must be >= 0");

		this.k = k;
	}

	public double getP() {
		return p;
	}

	public void setP(double p) {
		if (p < 0 || p > 1)
			throw new IllegalArgumentException("invalid probability: " + p);

		this.p = p;
	}

	public Random getPRNG() {
		return rnd;
	}

	public void setPRNG(Random rnd) {
		if (rnd == null)
			throw new NullPointerException();

		this.rnd = rnd;
	}

	/**
	 * Computes a directed WS graph.
	 * 
	 * @param g The Graph
	 * @param k Number of neighbours
	 * @param p Probability of rewiring
	 */
	public static void compute(Grph g, int k, double p, long seed) {
		DirectedWattsStrogatz tg = new DirectedWattsStrogatz();
		tg.setPRNG(new Random(seed));
		tg.setK(k);
		tg.setP(p);
		tg.compute(g);
	}

	@Override
	public Grph generateGraph(int noVertices, double avgDegree, long seed) {
		Grph g = new InMemoryGrph();
		g.addNVertices(noVertices);
		DirectedWattsStrogatz.compute(g, (int) Math.floor(avgDegree), 0.5, seed);
		return g;
	}
	
	public static void main(String[] args) {
		Grph g = new InMemoryGrph();
		g.addNVertices(7);
		DirectedWattsStrogatz.compute(g, 4, 0.5, 123);
	}

}
