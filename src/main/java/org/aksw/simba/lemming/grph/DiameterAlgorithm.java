package org.aksw.simba.lemming.grph;

import java.util.Collections;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import grph.Grph;
import grph.GrphAlgorithm;
import grph.algo.MultiThreadProcessing;
import grph.algo.search.GraphSearchListener;
import grph.algo.search.GraphSearchListener.DECISION;
import grph.algo.search.SearchResult;
import grph.path.ArrayListPath;
import it.unimi.dsi.fastutil.ints.IntSet;
import toools.collections.primitive.IntQueue;
import toools.collections.primitive.IntQueue.ACCESS_MODE;

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

    /**
     * The path between the end nodes of the diameter
     */
    private ArrayListPath diameterPath = null;

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
                return performSearch(new ColouredGraph(graph, null, null));
                // TODO: What is the use of vertexPalette and edgePalette in ColouredGraph ?
            }
        } else {
            throw new IllegalStateException("cannot compute the diameter of a non-connected graph");
        }
    }

    protected int performSearch(final ColouredGraph g) {
        // computes and cache
        // we need to do that otherwise multiple threads will call it
        // simultaneously
        g.getOutNeighborhoodsArray();

        return performSearch(g, g.getVertices());
    }

    public int performSearch(final IColouredGraph g, IntSet sources) {
        return performSearch(g, Grph.DIRECTION.out, sources);
    }

    protected int performSearch(final IColouredGraph g, final Grph.DIRECTION d, IntSet sources) {
        final ArrayListPath[] paths = new ArrayListPath[Collections.max(sources) + 1];
        new MultiThreadProcessing(g.getVertices()) {

            @Override
            protected void run(int threadID, int source) {
                paths[source] = performSearchInThread(g, source, d, null);
            }
        };

        int max = 0;
        for (int i = 0; i < paths.length; ++i) {
            int len = 0;
            try {
                len = paths[i].getLength();
            } catch (NullPointerException e) {
                // The nodes with no in-degree will not have any path terminating in them. Their
                // path will be null and will raise this exception
            }
            if (len > max) {
                max = len;
                this.diameterPath = paths[i];
            }
        }

        return max;
    }

    protected ArrayListPath performSearchInThread(IColouredGraph graph, int source, Grph.DIRECTION direction,
            GraphSearchListener listener) {
        assert graph != null;
        assert graph.getVertices().contains(source);
        int[][] adj = graph.getNeighbors(direction);
        int n = (int) graph.getNumberOfVertices();
        ArrayListPath path = null;
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

                if (r.distances[neighbor] == r.maxDistance()) {
                    path = r.computePathTo(neighbor);
                }
            }
        }

        if (listener != null) {
            listener.searchCompleted();
        }
        return path;
    }

    /**
     * Compute the shortest path between given two nodes in the graph. This method
     * is used twice: 1. To compute the shortest path from an endpoint of the
     * diameter to the tail of the newly added edge 2. To compute the shortest path
     * from the head of the newly added to the other endpoint of the diameter
     * 
     * @param graph          - {@link IColouredGraph} object in which the path is
     *                       searched
     * @param search         - {@link SearchResult} object containing basic
     *                       information as the start of the search or the distances
     *                       computed is previous search
     * @param source         - Starting node
     * @param destination    - Ending node
     * @param maxLengthLimit - The length of the previous path, upper limit to the
     *                       length of the new path
     * @return search - {@link SearchResult} object containing the new path
     *         information
     */
    private SearchResult searchShortestPath(IColouredGraph graph, SearchResult search, int source, int destination,
            int maxLengthLimit) {
        // Queue to access neighbors in breadth-first manner
        IntQueue queue = new IntQueue();
        int subPathLength = 0;
        boolean endNodeReached = false;
        if (source != destination) { // If source and destination are the same node then there is no need to start
                                     // the search
            queue.add(source);
        }
        while (queue.getSize() > 0 && subPathLength < maxLengthLimit && !endNodeReached) {
            int startNode = queue.extract(ACCESS_MODE.QUEUE);
            for (int node : graph.getOutNeighbors(startNode)) {
                if (search.distances[node] == -1) {
                    // update search results
                    search.predecessors[node] = startNode;
                    search.distances[node] = search.distances[startNode] + 1;
                    queue.add(node);
                    search.visitOrder.add(node);
                }
                if (node == destination) {
                    endNodeReached = true;
                    break;
                }
            }
            subPathLength++; // Length from the source node
        }
        return search;
    }

    /**
     * Method to check if the addition of a triple to the graph reduces the length
     * of the diameter. If yes, the method computes the shorter diameter path using
     * {@code searchShortestPath()} method. Else return the previous diameter.
     * 
     * @param graph  - {@link IColouredGraph} object containing a diameter
     * @param triple - New triple added to the graph object
     * @param path   - Previously computed diameter path
     * @return int - If a shorter diameter exists return its length or else the
     *         previous diameter length
     */
    public int computeShorterDiameter(IColouredGraph graph, TripleBaseSingleID triple, ArrayListPath path) {
        SearchResult search = new SearchResult((int) graph.getNumberOfVertices());
        // initialize the search object with the starting node of the diameter path.
        // The path is stored in reverse in ArrayListPath object, hence the source and
        // destination of the path are swapped.
        search.distances[path.getDestination()] = 0;
        search.visitOrder.add(path.getDestination());
        // Compute shortest path between start node and tail of the new edge
        search = searchShortestPath(graph, search, path.getDestination(), triple.tailId, path.getLength());
        // enter the new edge information in the search object
        search.distances[triple.headId] = search.distances[triple.tailId] + 1;
        search.predecessors[triple.headId] = triple.tailId;
        search.visitOrder.add(triple.headId);
        // Compute shortest path between head of the new edge and end node
        search = searchShortestPath(graph, search, triple.headId, path.getSource(),
                path.getLength() - search.distances[triple.headId]);
        return (search.maxDistance() < path.getLength() && search.distances[path.getSource()] > 0)
                ? search.maxDistance()
                : path.getLength();
    }

    public ArrayListPath getDiameterPath() {
        return this.diameterPath;
    }
    
}
