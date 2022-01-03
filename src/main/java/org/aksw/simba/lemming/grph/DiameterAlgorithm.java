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

    private ArrayListPath diameter = null;

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

    protected int performSearch(final ColouredGraph g, IntSet sources) {
        return performSearch(g, Grph.DIRECTION.out, sources);
    }

    protected int performSearch(final ColouredGraph g, final Grph.DIRECTION d, IntSet sources) {
        final int[] lengths = new int[Collections.max(sources) + 1];
        final ArrayListPath[] paths = new ArrayListPath[Collections.max(sources) + 1];
        new MultiThreadProcessing(g.getVertices()) {

            @Override
            protected void run(int threadID, int source) {
                paths[source] = performSearchInThread(g, source, d, null);
                try {
                    lengths[source] = paths[source].getLength();
                    // System.out
                    // .println("Source: " + source + "\nLength: " + lengths[source] + "\nPath: " +
                    // paths[source]);
                } catch (NullPointerException e) {
//                    System.out.println("No outgoing edges from this node " + source);
//                    System.out.println(g.getOutEdgeDegree(source));
                    return;
                }
            }
        };

        int max = 0;
        for (int i = 0; i < lengths.length; ++i) {
            if (lengths[i] > max) {
                max = lengths[i];
                this.diameter = paths[i];
            }
        }

        return max;
    }

    public ArrayListPath performSearchInThread(IColouredGraph graph, int source, Grph.DIRECTION direction,
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

    public ArrayListPath computeShorterDiameter(IColouredGraph graph, TripleBaseSingleID triple, ArrayListPath path) {
        IntQueue queue = new IntQueue();
        SearchResult search = new SearchResult((int) graph.getNumberOfVertices());
        ArrayListPath shorterPath = new ArrayListPath();
        shorterPath.setSource(path.getSource());
        boolean shorterPathFound = false;
        if (path.getSource() != triple.tailId) {
            queue.add(path.getSource());
            search.distances[path.getSource()] = 0;
            search.visitOrder.add(path.getSource());
        }
        int sourceToTailLength = 0;
        while (queue.getSize() > 0 && sourceToTailLength < path.getLength()) {
            int source = queue.extract(ACCESS_MODE.QUEUE);
            boolean endNodeReached = false;
            for (int node : graph.getOutNeighbors(source)) {
                if (search.distances[node] == -1) {
                    search.predecessors[node] = source;
                    search.distances[node] = search.distances[source] + 1;
                    queue.add(node);
                    search.visitOrder.add(node);
                }
                if (node == triple.tailId) {
                    endNodeReached = true;
                    break;
                }
            }
            sourceToTailLength++;
            if (endNodeReached) {
                queue = new IntQueue();
                break;
            }
        }
        // The new edge added is not incident to the destination of the diameter
        if (triple.headId != path.getDestination()) {
            queue.add(triple.headId);
            search.distances[triple.headId] = search.distances[triple.tailId] + 1;
            search.visitOrder.add(triple.headId);
        }
        int headToDestinationLength = 0;
        while (queue.getSize() > 0 && headToDestinationLength < path.getLength() - search.distances[triple.headId]) {
            int source = queue.extract(ACCESS_MODE.QUEUE);
            boolean endNodeReached = false;
            for (int node : graph.getOutNeighbors(source)) {
                if (search.distances[node] == -1) {
                    search.predecessors[node] = source;
                    search.distances[node] = search.distances[source] + 1;
                    queue.add(node);
                    search.visitOrder.add(node);
                }
                if (node == path.getDestination()) { // shortest path has been found between the new edge
                    // head and the existing diameter destination
                    endNodeReached = true;
                    break;
                }
            }
            headToDestinationLength++;
            if (endNodeReached) {
                break;
            }
        }

        return (shorterPathFound) ? shorterPath : path;
    }

    public ArrayListPath getDiameterPath() {
        return this.diameter;
    }
}
