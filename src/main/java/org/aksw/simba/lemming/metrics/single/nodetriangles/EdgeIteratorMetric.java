package org.aksw.simba.lemming.metrics.single.nodetriangles;

import com.carrotsearch.hppc.cursors.IntCursor;
import grph.Grph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import toools.set.IntSet;
import toools.set.IntSets;

import java.util.*;

/**
 * @author DANISH AHMED on 6/28/2018
 */
public class EdgeIteratorMetric extends AbstractMetric implements SingleValueMetric {

    private ColouredGraph graph;
    private int trianglesSum = 0;
    private IntSet edges[];
    private IntSet vertexNeighbors[];

    public EdgeIteratorMetric() {
        super("#edgeIterator");
    }

    @Override
    public double apply(ColouredGraph graph) {
        this.graph = graph;
        edges = new IntSet[graph.getGraph().getNumberOfEdges()];
        vertexNeighbors = new IntSet[graph.getGraph().getNumberOfVertices()];
        return countTriangles();
    }

    protected double countTriangles() {
        HashSet<String> visitedV = new HashSet<>();

        int triangleCount = 0;
        Grph grph = graph.getGraph();

        for (int i = 0; i < edges.length; ++i) {
            edges[i] = grph.getVerticesIncidentToEdge(i);
        }

        for (int i = 0; i < vertexNeighbors.length; i++) {
            vertexNeighbors[i] = grph.getInNeighbors(i);
            vertexNeighbors[i].addAll(grph.getOutNeighbors(i));
        }

        for (int i = 0; i < edges.length; i++) {
            int[] verticesConnectedToEdge = edges[i].toIntArray();

            // An edge will always have only two vertices
            // In case of loop edge, it will have only 1 vertex
            if (verticesConnectedToEdge.length == 2) {
                IntSet verticesInCommon = IntSets.intersection(
                        vertexNeighbors[verticesConnectedToEdge[0]],
                        vertexNeighbors[verticesConnectedToEdge[1]]);

                for (IntCursor vertex : verticesInCommon) {
                    if (vertex.value == verticesConnectedToEdge[0]
                            || vertex.value == verticesConnectedToEdge[1])
                        continue;

                    int[] vertices = {vertex.value, verticesConnectedToEdge[0], verticesConnectedToEdge[1]};
                    Arrays.sort(vertices);
                    if (visitedV.contains(Arrays.toString(vertices)))
                        continue;

                    visitedV.add(Arrays.toString(vertices));
                    triangleCount++;
                }
            }
        }
        return triangleCount;
    }
}
