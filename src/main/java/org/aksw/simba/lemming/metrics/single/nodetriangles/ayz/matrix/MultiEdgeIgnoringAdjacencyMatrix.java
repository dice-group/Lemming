package org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.matrix;

import java.util.Arrays;

import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;

import grph.Grph;
import toools.math.IntMatrix;

/**
 * MultiEdgeIgnoringAdjacencyMatrix is used to build  a undirected adjacency matrix for a given directed {@link Grph}.
 * If there's an edge (v1, v2) and v1 &ne; v2 in the graph, then enter 1 in corresponding positions (v1, v2) and (v2, v1), otherwise
 * enter 0. So MultiEdgeIgnoringAdjacencyMatrix is a symmetric (0,1)-matrix whose entries on diagonal are 0s.
 *
 * Code reference:
 * https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/metrics/single/triangle/matrix/MultiEdgeIgnoringAdjacencyMatrix.java
 *
 */
public class MultiEdgeIgnoringAdjacencyMatrix {

    private IntMatrix internalMatrix;
    private IntIntMap verticesToIndicesMap;


    public MultiEdgeIgnoringAdjacencyMatrix(Grph graph) {

        int[] vertices = graph.getVertices().toIntArray();

        verticesToIndicesMap = new IntIntOpenHashMap();

        int index = 0;
        for (int v : vertices) {
            verticesToIndicesMap.put(v, index);
            index++;
        }

        internalMatrix = new IntMatrix(graph.getNumberOfVertices(), graph.getNumberOfVertices());
        for (int e: graph.getEdges()) {
            int sourceNode = graph.getOneVertex(e);
            int targetNode = graph.getTheOtherVertex(e, sourceNode);
            if (sourceNode != targetNode) {
                internalMatrix.set(verticesToIndicesMap.get(sourceNode), verticesToIndicesMap.get(targetNode), 1);
                internalMatrix.set(verticesToIndicesMap.get(targetNode), verticesToIndicesMap.get(sourceNode), 1);
            }
        }
    }

    /**
     * Calculate m<sup>k</sup>, where m is a matrix.
     * @param adjacencyMatrix an instance of MultiEdgeIgnoringAdjacencyMatrix.
     * @param k exponent
     * @return a matrix = adacentcyMatrix<sup>k</sup>
     */
    public static IntMatrix power(MultiEdgeIgnoringAdjacencyMatrix adjacencyMatrix, int k) {
        IntMatrix intMatrix = adjacencyMatrix.internalMatrix;
        IntMatrix r = new IntMatrix(intMatrix.toIntArray());

        for (int i = 1; i < k; i++) {
            r = IntMatrix.multiplication(r, intMatrix);
        }
        return r;
    }

}
