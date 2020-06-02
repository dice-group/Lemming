package org.aksw.simba.lemming.metrics.single.nodetriangles.ayz;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.matrix.MatrixMultiplicationNumberOfTrianglesMetric;
import org.aksw.simba.lemming.util.Constants;


/**
 * This class models an algorithm for counting the amount of node triangles in a given graph. This
 * is done using the so called ayz algorithm proposed by Alon, Yuster and Zwick in their work
 * "Finding and Counting Given Length Cycles".
 *
 * @see <a href=
 *      "https://www.researchgate.net/publication/225621879_Finding_and_Counting_Given_Length_Cycles">https://www.researchgate.net/publication/225621879_Finding_and_Counting_Given_Length_Cycles</a>).
 *
 * @author Tanja Tornede
 * https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/metrics/single/triangle/AyzNumberOfTrianglesMetric.java
 *
 */
public class ListingAyzMetric extends AbstractMetric implements SingleValueMetric {


    public ListingAyzMetric() {
        super("#nodetriangles");
    }


    @Override
    public double apply(ColouredGraph graph) {
        double delta = 3.0;
        double threshold = Math.pow(graph.getGraph().getNumberOfEdges(), (delta - 1) / (delta + 1));
        IntSet highDegreeVertices = new DefaultIntSet(Constants.DEFAULT_SIZE); 
        		//IntSets.from();
        for (int vertex:graph.getVertices()) {
            if (graph.getGraph().getVertexDegree(vertex) > threshold) {
                highDegreeVertices.add(vertex);
            }
        }

        double numberOfTriangles = 0;
        numberOfTriangles += countTrianglesViaNodeIterator(graph, highDegreeVertices);
        numberOfTriangles += countTrianglesViaMatrixMultiplication(graph, highDegreeVertices);

        return numberOfTriangles;
    }


    private double countTrianglesViaNodeIterator(ColouredGraph graph, IntSet highDegreeVertices) {
        NodeIteratorMetric nodeIterator = new NodeIteratorMetric();
        return nodeIterator.calculateTriangles(graph, highDegreeVertices);
    }


    private double countTrianglesViaMatrixMultiplication(ColouredGraph graph, IntSet highDegreeVertices) {
        ColouredGraph subgraph = new ColouredGraph(graph.getGraph().getSubgraphInducedByVertices(highDegreeVertices),
                graph.getVertexPalette(), graph.getEdgePalette());
        MatrixMultiplicationNumberOfTrianglesMetric matrixMultiplication = new MatrixMultiplicationNumberOfTrianglesMetric();
        return matrixMultiplication.apply(subgraph);
    }

}
