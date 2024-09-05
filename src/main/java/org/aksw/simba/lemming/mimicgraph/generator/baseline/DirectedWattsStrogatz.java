package org.aksw.simba.lemming.mimicgraph.generator.baseline;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.util.Constants;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import grph.DefaultIntSet;
import grph.Grph;
import grph.algo.topology.RandomNewmanWattsStrogatzTopologyGenerator;
import grph.algo.topology.RingTopologyGenerator;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import toools.collections.primitive.IntCursor;

/**
 * 
 * Generalization for directed Watts-Strogatz graphs.
 * 
 * It generates a directed graph based on
 * {@link RandomNewmanWattsStrogatzTopologyGenerator}. The edge directions are
 * added randomly afterwards.
 * 
 * @author Alexandra Silva
 */
@Component("WS")
@Scope(value = "prototype")
public class DirectedWattsStrogatz implements IGenerator {

	/**
	 * Random sequence generator object
	 */
	private Random rnd;

	/**
	 * Average degree of the graph
	 */
	private double k;

	/**
	 * Probability of rewiring
	 */
	private double p;

	/**
	 * Generate directed Watts Strogatz graph
	 * 
	 * @param g The graph only with the vertices
	 */
	public void compute(Grph g) {

		// get undirected graph
		int n = g.getVertices().size();

		// degree needs to be at least 2 to do the full WS algorithm
		// limit it if less than that
		if (k >= 2) {
			g.ring();
			connectToKClosestNeighbors(g, k);
		} else {
			partialRing(g, k);
		}

		// if the network is a clique
		if (k < n / 2) {
			for (int u : g.getVertices().toIntArray()) {
				for (int e : g.getEdgesIncidentTo(u).toIntArray()) {
					// if the vertex is not connected to all vertices, there's
					// room for a shortcut
					if (g.getVertexDegree(u, Grph.TYPE.vertex, Grph.DIRECTION.in_out) < n - 1) {
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

	/**
	 * Based on {@link RingTopologyGenerator}. It builds a ring where not all
	 * adjacent vertices are connected for k<2.
	 * 
	 * @param graph The graph object
	 * @param k     Desired average degree
	 */
	public void partialRing(Grph graph, double k) {

		// only accept k smaller than 2
		if (k >= 2) {
			throw new IllegalArgumentException("You should use the default ring function instead.");
		}

		// connect randomly if target edges has not been reached
		IntSet vertices = graph.getVertices();
		int numEdges = (int) Math.floor(vertices.size() * k / 2);
		if (vertices.size() > 1) {
			IntIterator i = vertices.iterator();
			int predecessor = i.nextInt();
			int first = predecessor;

			while (i.hasNext()) {
				int a = i.nextInt();

				// if the target number of edges hasn't been reached yet
				// add an edge randomly
				if (graph.getNumberOfEdges() < numEdges && rnd.nextBoolean())
					graph.addUndirectedSimpleEdge(predecessor, a);
				predecessor = a;
			}

			// if the target number of edges hasn't been reached, connect the last one
			if (graph.getNumberOfEdges() < numEdges)
				graph.addUndirectedSimpleEdge(predecessor, first);
		}
	}

	/**
	 * Connects each vertex in the graph with the K closest neighbours. It accepts
	 * odd and partial degrees.
	 * 
	 * @param graph The graph object
	 * @param k     Target average degree
	 */
	private void connectToKClosestNeighbors(Grph graph, double k) {

		int noVertices = graph.getVertices().getGreatest() + 1;

		double center = k / 2;
		double sigma = Math.ceil(k/2);

		// for each vertex, connect to right neighbours and then to right side
		for (IntCursor v : IntCursor.fromFastUtil(graph.getVertices())) {
			// get right and left side neighbours, this will be of the Math.ceil of k!
			IntSet[] neighbours = getKClosestNeighbours(noVertices, v.value, k);
			IntSet leftNe = neighbours[0];
			IntSet rightNe = neighbours[1];

			// no need to sample if degree is even
			int roundedSample1;
			int roundedSample2;
			if (center % 2 == 0) {
				roundedSample1 = (int) center;
				roundedSample2 = (int) center;
			} else {
				double sample1 = rnd.nextGaussian(center, sigma);
				double sample2 = rnd.nextGaussian(center, sigma);
				roundedSample1 = stochasticRound(sample1, rnd);
				roundedSample2 = stochasticRound(sample2, rnd);	
			}

			// connect the right
			for (int i = 0; i < roundedSample1; i++) {
				for (IntCursor n : IntCursor.fromFastUtil(rightNe)) {
					// check if connection already exists before adding it
					if (graph.getEdgesConnecting(v.value, n.value).isEmpty()) {
						graph.addUndirectedSimpleEdge(v.value, n.value);
					}
				}
			}

			// connect the left
			for (int i = 0; i < roundedSample2; i++) {
				for (IntCursor n : IntCursor.fromFastUtil(leftNe)) {
					// check if connection already exists before adding it
					if (graph.getEdgesConnecting(v.value, n.value).isEmpty()) {
						graph.addUndirectedSimpleEdge(v.value, n.value);
					}
				}
			}
		}
	}

	/**
	 * Stochastic rounding of a sample
	 * 
	 * @param x
	 * @param random
	 * @return
	 */
	public static int stochasticRound(double x, Random random) {
		int lower = (int) Math.floor(x);
		int upper = (int) Math.ceil(x);

		double prob = x - lower;
		if (random.nextDouble() < prob) {
			return upper;
		} else {
			return lower;
		}
	}

	/**
	 * Assign the edge contributions based on a distribution centered around k/2
	 * 
	 * @param vertices Total number of vertices
	 * @param k        Average degree
	 * @return Array with each edge contributions expected from each vertex.
	 */
	public int[] assignEdgesKRandom(int vertices, double k) {
		int[] contributions = new int[vertices];
		for (int i = 0; i < vertices; i++) {
			contributions[i] = (int) nextDouble(0, k);
		}
		return contributions;
	}

	public double nextDouble(double lower, double upper) {
		return lower + (upper - lower) * rnd.nextDouble();
	}

	/**
	 * Assign the edge contributions randomly to each vertex based on the expected
	 * number of edges.
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
	 * Assign the edge contributions to each vertex. First we find the lower common
	 * to assign to all, and then we assign randomly based on the unassigned edges.
	 * 
	 * @param vertices Total number of vertices
	 * @param k        Average degree
	 * @return Array with each edge contributions expected from each vertex.
	 */
	public int[] assignEdges(int vertices, double k) {
		int roundK = (int) Math.ceil(k / 2);
		int[] contributions = new int[vertices];

		// assign K to each vertex
		for (int i = 0; i < vertices; i++) {
			contributions[i] = roundK;
		}

		// create extra edges with leftover randomly
		int leftoverEdges = (int) Math.ceil((roundK - (k / 2)) * vertices);
		for (int i = 0; i < leftoverEdges; i++) {
			int vertex = rnd.nextInt(vertices);
			contributions[vertex]++;
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

	public void setK(double k) {
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
	public static void compute(Grph g, double k, double p, long seed) {
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
		DirectedWattsStrogatz.compute(g, avgDegree, 0.5, seed);
		return g;
	}

//	public static void main(String[] args) {
//		DirectedWattsStrogatz tg = new DirectedWattsStrogatz();
//		tg.setPRNG(new Random(123));
////		tg.assignEdgesKRandom(10, 1.5);
//		Grph g = new InMemoryGrph();
//		g.addNVertices(7);
//		DirectedWattsStrogatz.compute(g, 1.2, 0.5, 123);
//	}

}
