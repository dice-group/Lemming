package org.aksw.simba.lemming.metrics.single.nodetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import grph.DefaultIntSet;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import org.aksw.simba.lemming.util.IntSetUtil;


/**
 * This class models an algorithm for counting the amount of node triangles in a given graph. This
 * is done using the so called node-iterator algorithm explained by Schank and Wagner in their work
 * "Finding, Counting and Listing all Triangles in Large Graphs, An Experimental Study".
 *
 * @see <a href=
 *      "https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study">https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study</a>
 *
 * @author Tanja Tornede
 * https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/metrics/single/triangle/NodeIteratorNumberOfTrianglesMetric.java
 *
 */
public class NodeIteratorMetric extends AbstractMetric implements TriangleMetric {
    public NodeIteratorMetric() {
        super("#nodetriangles");
    }

    /**
     * Count triangles on in a given graph, and the triangles which are formed by the given node set vertices are not
     * taken into account.
     * @param graph an instance of {@link ColouredGraph}.
     * @param vertices a set of nodes, and it is a subset of nodes from the given graph.
     * @return Amount of triangles
     */
    public int calculateTriangles(ColouredGraph graph, IntSet vertices) {
        Grph grph = getUndirectedGraph(graph.getGraph());
        IntSet visitedVertices = new DefaultIntSet(grph.getNumberOfVertices());
        int numberOfTriangles = 0;

        for (int vertex:grph.getVertices()) {
            if(vertices.contains(vertex)){
                continue;
            }
            IntSet neighbors = IntSetUtil.difference(IntSetUtil.union(grph.getInNeighbors(vertex), grph.getOutNeighbors(vertex)),
                    visitedVertices);
            for (int neighbor1:neighbors) {
                IntSet neighbors1 = IntSetUtil
                        .difference(IntSetUtil.union(grph.getInNeighbors(neighbor1), grph.getOutNeighbors(neighbor1)), visitedVertices);
                for (int neighbor2:neighbors) {
                    if (vertex != neighbor1 && vertex != neighbor2 && neighbor1 < neighbor2 && neighbors1.contains(neighbor2)) {
                        numberOfTriangles++;
                    }
                }
            }
            visitedVertices.add(vertex);
        }
        return numberOfTriangles;
    }

    @Override
    public double apply(ColouredGraph graph) {
        IntSet vertices = IntSets.EMPTY_SET;
        return calculateTriangles(graph, vertices);
    }

    private Grph getUndirectedGraph(Grph graph) {
        Grph undirectedGraph = new InMemoryGrph();
        for (int e : graph.getEdges()) {
            int sourceNode = graph.getOneVertex(e);
            int targetNode = graph.getTheOtherVertex(e, sourceNode);
            undirectedGraph.addUndirectedSimpleEdge(sourceNode, targetNode);
        }
        return undirectedGraph;
    }

    @Override
    public double calculateComplexity(int edges, int vertices) {
        return vertices * Math.pow(edges, 2);
    }
}

