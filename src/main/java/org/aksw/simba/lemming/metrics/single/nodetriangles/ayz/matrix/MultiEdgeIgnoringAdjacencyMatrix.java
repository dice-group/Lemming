package org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.matrix;

import java.util.Arrays;

import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;

import grph.Grph;
import toools.math.IntMatrix;

/*
* Code reference:
* https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/metrics/single/triangle/matrix/MultiEdgeIgnoringAdjacencyMatrix.java
*
* */


public class MultiEdgeIgnoringAdjacencyMatrix {

    private IntMatrix internalMatrix;
    private IntIntMap verticesToIndicesMap;


    public MultiEdgeIgnoringAdjacencyMatrix(Grph graph) {

        int[] vertices = graph.getVertices().toIntArray();
        Arrays.sort(vertices);

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


    public static IntMatrix power(MultiEdgeIgnoringAdjacencyMatrix adjacencyMatrix, int k) {
        IntMatrix intMatrix = adjacencyMatrix.internalMatrix;
        IntMatrix r = new IntMatrix(intMatrix.toIntArray());

        for (int i = 1; i < k; i++) {
            r = IntMatrix.multiplication(r, intMatrix);
        }

        return r;
    }

}
