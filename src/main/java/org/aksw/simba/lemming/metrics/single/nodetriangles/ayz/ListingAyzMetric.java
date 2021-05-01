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
 * @see <a>https://i11www.iti.kit.edu/extra/publications/sw-fclt-05_t.pdf</a>
 *
 * @author Tanja Tornede
 *
 */
public class ListingAyzMetric extends AbstractMetric implements SingleValueMetric {


    public ListingAyzMetric() {
        super("#nodetriangles");
    }


    @Override
    public double apply(ColouredGraph graph) {

        double r = 2.376;
        double threshold = Math.pow(graph.getGraph().getNumberOfEdges(), (r - 1) / (r + 1));
        IntSet highDegreeVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);

        for (int vertex:graph.getVertices()) {
            if (graph.getGraph().getVertexDegree(vertex) > threshold) {
                highDegreeVertices.add(vertex);
            }
        }

        //NodeIterator performed on lowDegreeVertices (deference set of highDegreeVertices),
        // matrix multiplication induced subgraph of highDegreeVertices
        double numberOfTriangles = 0;
        numberOfTriangles += countTrianglesViaNodeIterator(graph, highDegreeVertices);
        numberOfTriangles += countTrianglesViaMatrixMultiplication(graph, highDegreeVertices);

        return numberOfTriangles;
    }


    private double countTrianglesViaNodeIterator(ColouredGraph graph, IntSet vertices) {
        NodeIteratorMetric nodeIterator = new NodeIteratorMetric();
        return nodeIterator.calculateTriangles(graph, vertices);
    }


    private double countTrianglesViaMatrixMultiplication(ColouredGraph graph, IntSet vertices) {
        ColouredGraph subgraph = new ColouredGraph(graph.getGraph().getSubgraphInducedByVertices(vertices),
                graph.getVertexPalette(), graph.getEdgePalette());
        MatrixMultiplicationNumberOfTrianglesMetric matrixMultiplication = new MatrixMultiplicationNumberOfTrianglesMetric();
        return matrixMultiplication.apply(subgraph);
    }

}
