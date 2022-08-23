package org.aksw.simba.lemming.metrics.single.nodetriangles;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import grph.DefaultIntSet;
import grph.Grph;
import it.unimi.dsi.fastutil.ints.IntSet;

import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

/**
 * This class models an algorithm for counting the amount of node triangles in a
 * given graph. This is done using the so called node-iterator-core algorithm
 * explained by Schank and Wagner in their work "Finding, Counting and Listing
 * all Triangles in Large Graphs, An Experimental Study".
 *
 * @see <a href=
 *      "https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study">https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study</a>).
 *
 * @author Tanja Tornede
 *         https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/metrics/single/triangle/NodeIteratorCoreNumberOfTrianglesMetric.java
 *
 */
public class NodeIteratorCoreMetric extends AbstractMetric implements TriangleMetric {
    public NodeIteratorCoreMetric() {
        super("#nodetriangles");
    }

    public NodeIteratorCoreMetric(Boolean calculateClusteringCoefficient) {
        super("node-iterator-core #node triangles");
    }

    @Override
    public double apply(IColouredGraph graph) {
        Grph grph = graph.getGraph();

        int[] degrees = new int[grph.getVertices().size()];
        IntSet visitedVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
        for (int vertex : grph.getVertices()) {
            degrees[vertex] = IntSetUtil.union(grph.getInNeighbors(vertex), grph.getOutNeighbors(vertex)).size();
        }

        int numberOfTriangles = 0;
        int vertexWithMinimumDegree = getNodeWithMinimumDegree(degrees);
        while (vertexWithMinimumDegree < Integer.MAX_VALUE && visitedVertices.size() < grph.getVertices().size() - 2) {
            int triangleCount = 0;
            IntSet neighbors = IntSetUtil.difference(IntSetUtil.union(grph.getInNeighbors(vertexWithMinimumDegree),
                    grph.getOutNeighbors(vertexWithMinimumDegree)), visitedVertices);
            for (int neighbor1 : neighbors) {
                IntSet neighbors1 = IntSetUtil.difference(
                        IntSetUtil.union(grph.getInNeighbors(neighbor1), grph.getOutNeighbors(neighbor1)),
                        visitedVertices);
                for (int neighbor2 : neighbors) {
                    if (vertexWithMinimumDegree != neighbor1 && vertexWithMinimumDegree != neighbor2
                            && neighbor1 < neighbor2 && neighbors1.contains(neighbor2)) {
                        numberOfTriangles++;
                        triangleCount++;
//                        numberOfTriangles = numberOfTriangles + IntSets.union(graph.getOutEdges(neighbor1.value), graph.getInEdges(neighbor1.value));
                    }
                }
            }
            visitedVertices.add(vertexWithMinimumDegree);

            degrees[vertexWithMinimumDegree] = 0;
            for (int vertex : neighbors) {
                if (degrees[vertex] > 0) {
                    degrees[vertex]--;
                }
            }
            vertexWithMinimumDegree = getNodeWithMinimumDegree(degrees);
        }
        return numberOfTriangles;
    }

    private int getNodeWithMinimumDegree(int[] degrees) {
        int nodeWithMinimumDegree = Integer.MAX_VALUE;
        int minimalDegree = Integer.MAX_VALUE;

        for (int vertex = 0; vertex < degrees.length; vertex++) {
            if (degrees[vertex] > 0 && degrees[vertex] < minimalDegree) {
                minimalDegree = degrees[vertex];
                nodeWithMinimumDegree = vertex;
            }
        }
        return nodeWithMinimumDegree;
    }

    @Override
    public double calculateComplexity(int edges, int vertices) {
        return (vertices - 2) * Math.pow((edges / vertices), 2);
    }
}
