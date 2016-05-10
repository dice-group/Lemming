package org.aksw.simba.lemming.grph;

import grph.Grph;
import grph.GrphAlgorithm;
import grph.algo.MultiThreadProcessing;
import grph.algo.search.GraphSearchListener;
import grph.algo.search.GraphSearchListener.DECISION;
import grph.algo.search.SearchResult;
import toools.collections.IntQueue;
import toools.collections.IntQueue.ACCESS_MODE;
import toools.set.IntSet;

/**
 * This diameter algorithm is not based on a diameter matrix and, thus, needs
 * less memory than the standard diameter algorithm (
 * {@link grph.algo.distance.DistanceMatrixBasedDiameterAlgorithm}) used in the
 * {@link Grph} class.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class DiameterAlgorithm extends GrphAlgorithm<Integer> {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer compute(Grph graph) {
		if (graph.isNull()) {
			throw new IllegalStateException("cannot compute the diameter of a null graph");
		}
		if (graph.isTrivial()) {
			return 0;
		}
		if (graph.isConnected()) {
			if (graph.getVertices().size() == 2) {
				return 1;
			} else {
				return performSearch(graph);
			}
		} else {
			throw new IllegalStateException("cannot compute the diameter of a non-connected graph");
		}
	}

	protected int performSearch(final Grph g) {
		// computes and cache
		// we need to do that otherwise multiple threads will call it
		// simultaneously
		g.getOutNeighborhoods();

		return performSearch(g, g.getVertices());
	}

	protected int performSearch(final Grph g, IntSet sources) {
		return performSearch(g, Grph.DIRECTION.out, sources);
	}

	protected int performSearch(final Grph g, final Grph.DIRECTION d, IntSet sources) {
		final int[] lengths = new int[sources.getGreatest() + 1];

		new MultiThreadProcessing(g.getVertices()) {

			@Override
			protected void run(int threadID, int source) {
				lengths[source] = performSearchInThread(g, source, d, null);
			}

		};

		int max = 0;
		for (int i = 0; i < lengths.length; ++i) {
			if (lengths[i] > max) {
				max = lengths[i];
			}
		}

		return max;
	}

	protected int performSearchInThread(Grph graph, int source, Grph.DIRECTION direction, GraphSearchListener listener) {
		assert graph != null;
		assert graph.getVertices().contains(source);
		int[][] adj = graph.getNeighbors(direction);
		int n = graph.getVertices().getGreatest() + 1;
		SearchResult r = new SearchResult(n);
		// r.source = source;
		r.distances[source] = 0;
		r.visitOrder.add(source);

		if (listener != null) {
			listener.searchStarted();
		}

		IntQueue queue = new IntQueue();
		queue.add(source);

		while (queue.getSize() > 0) {
			int v = queue.extract(ACCESS_MODE.QUEUE);
			int d = r.distances[v];

			if (listener != null) {
				if (listener.vertexFound(v) == DECISION.STOP) {
					break;
				}
			}

			for (int neighbor : adj[v]) {
				// if this vertex was not yet visited
				if (r.distances[neighbor] == -1) {
					r.predecessors[neighbor] = v;
					r.distances[neighbor] = d + 1;
					queue.add(neighbor);
					r.visitOrder.add(neighbor);
				}
			}
		}

		if (listener != null) {
			listener.searchCompleted();
		}

		return r.maxDistance();
	}

}
