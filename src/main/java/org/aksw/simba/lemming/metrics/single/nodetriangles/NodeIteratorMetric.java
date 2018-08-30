package org.aksw.simba.lemming.metrics.single.nodetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import toools.set.IntSet;
import toools.set.IntSets;


/**
 * This class models an algorithm for counting the amount of node triangles in a given graph. This
 * is done using the so called node-iterator algorithm explained by Schank and Wagner in their work
 * "Finding, Counting and Listing all Triangles in Large Graphs, An Experimental Study".
 *
 * @see <a href=
 *      "https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study">https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study</a>).
 *
 * @author Tanja Tornede
 * https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/metrics/single/triangle/NodeIteratorNumberOfTrianglesMetric.java
 *
 */
public class NodeIteratorMetric extends AbstractMetric implements TriangleMetric {
    public NodeIteratorMetric() {
        super("#nodetriangles");
    }

    public int calculateTriangles(ColouredGraph graph, IntSet highDegreeVertices) {
        IntSet visitedVertices = IntSets.from();
        Grph grph = getUndirectedGraph(graph.getGraph());
        int numberOfTriangles = 0;
        for (IntCursor vertex : graph.getVertices()) {
            IntSet neighbors = IntSets.difference(IntSets.union(grph.getInNeighbors(vertex.value), grph.getOutNeighbors(vertex.value)),
                    visitedVertices);
            for (IntCursor neighbor1 : neighbors) {
                IntSet neighbors1 = IntSets
                        .difference(IntSets.union(grph.getInNeighbors(neighbor1.value), grph.getOutNeighbors(neighbor1.value)), visitedVertices);
                for (IntCursor neighbor2 : neighbors) {
                    if (vertex.value != neighbor1.value && vertex.value != neighbor2.value && neighbor1.value < neighbor2.value
                            && neighbors1.contains(neighbor2.value)) {
                        if (!highDegreeVertices.contains(vertex.value) || !highDegreeVertices.contains(neighbor1.value)
                                || !highDegreeVertices.contains(neighbor2.value)) {
                            numberOfTriangles++;
                        }
                    }
                }
            }
            visitedVertices.add(vertex.value);
        }
        return numberOfTriangles;
    }

    @Override
    public double apply(ColouredGraph graph) {
        IntSet highDegreeVertices = IntSets.emptySet;
        return calculateTriangles(graph, highDegreeVertices);
    }

    private Grph getUndirectedGraph(Grph graph) {
        Grph undirectedGraph = new InMemoryGrph();
        for (IntCursor e : graph.getEdges()) {
            int sourceNode = graph.getOneVertex(e.value);
            int targetNode = graph.getTheOtherVertex(e.value, sourceNode);
            undirectedGraph.addUndirectedSimpleEdge(sourceNode, targetNode);
        }
        return undirectedGraph;
    }

    @Override
    public double calculateComplexity(int edges, int vertices) {
        return vertices * Math.pow(edges, 2);
    }
}

