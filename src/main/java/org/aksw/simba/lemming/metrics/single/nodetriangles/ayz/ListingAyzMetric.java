package org.aksw.simba.lemming.metrics.single.nodetriangles.ayz;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.matrix.MatrixMultiplicationNumberOfTrianglesMetric;

/**
 * This class models an algorithm for counting the amount of node triangles in a given graph. This
 * is done using the so called ayz algorithm proposed by Alon, Yuster and Zwick in their work
 * "Finding and Counting Given Length Cycles".
 *
 * @see <a href=
 *      "https://www.researchgate.net/publication/225621879_Finding_and_Counting_Given_Length_Cycles">https://www.researchgate.net/publication/225621879_Finding_and_Counting_Given_Length_Cycles</a>
 *
 * @see <a href=
 *      "https://i11www.iti.kit.edu/extra/publications/sw-fclt-05_t.pdf">https://i11www.iti.kit.edu/extra/publications/sw-fclt-05_t.pdf</a>
 *
 * @author Tanja Tornede, Zun Wang
 *
 */
public class ListingAyzMetric extends AbstractMetric implements SingleValueMetric {

    /**
     * &gamma; is so-called matrix multiplication exponent. According to the publication "Finding, Counting and Listing
     * all Triangles in Large Graph", if use an adjacency matrix for triangle counting algorithm, then the runtime of
     * the algorithm is &Theta;(n<sup>&gamma;</sup>) with &gamma; &le; 2.376.
     */
    private static final double GAMMA = 2.376;
    /**
     * This is used to split the node set into low degree vertices V<sub>low</sub>={v &isin; V : d(v) &le; &beta;} and
     * high degree vertices V<sub>high</sub> = V \ V<sub>low</sub>, where &beta;=m<sup>EXPONENT</sup> and m is number of
     * edges.
     */
    private static final double EXPONENT = (GAMMA-1)/(GAMMA+1);

    public ListingAyzMetric() {
        super("#nodetriangles");
    }

    @Override
    public double apply(ColouredGraph graph) {

        double threshold = Math.pow(graph.getGraph().getNumberOfEdges(), EXPONENT);
        IntSet highDegreeVertices = new DefaultIntSet(graph.getVertices().size());

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

    /**
     * Perform the {@link NodeIteratorMetric} to count the number of triangles on the nodes set V \ V<sub>subset</sub>
     * from a given {@link ColouredGraph}.
     * @param graph an instance of {@link ColouredGraph}.
     * @param vertices subset V<sub>subset</sub> &sube; V, where V is nodes set of the given graph.
     * @return Number of triangles on the nodes set V\vertices.
     */
    private double countTrianglesViaNodeIterator(ColouredGraph graph, IntSet vertices) {
        NodeIteratorMetric nodeIterator = new NodeIteratorMetric();
        return nodeIterator.calculateTriangles(graph, vertices);
    }

    /**
     * Perform the {@link MatrixMultiplicationNumberOfTrianglesMetric} to count the number of triangles on the induced
     * subgraph of nodes subset V<sub>subset</sub> from a given {@link ColouredGraph}.
     * @param graph an instance of {@link ColouredGraph}.
     * @param vertices subset V<sub>subset</sub> &sube; V, where V is nodes set of the given graph.
     * @return Number of triangles on the induced subgraph.
     */
    private double countTrianglesViaMatrixMultiplication(ColouredGraph graph, IntSet vertices) {
        ColouredGraph subgraph = new ColouredGraph(graph.getGraph().getSubgraphInducedByVertices(vertices),
                graph.getVertexPalette(), graph.getEdgePalette());
        MatrixMultiplicationNumberOfTrianglesMetric matrixMultiplication = new MatrixMultiplicationNumberOfTrianglesMetric();
        return matrixMultiplication.apply(subgraph);
    }

}
