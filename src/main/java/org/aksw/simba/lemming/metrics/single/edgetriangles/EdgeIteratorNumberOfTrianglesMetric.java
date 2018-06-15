package org.aksw.simba.lemming.metrics.single.edgetriangles;

import com.carrotsearch.hppc.cursors.IntCursor;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import toools.set.IntSet;
import toools.set.IntSets;

/**
 * @author DANISH AHMED on 6/13/2018
 */
public class EdgeIteratorNumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

    private IntSet highDegreeVertices;
    private IntSet[] edges;


    public EdgeIteratorNumberOfTrianglesMetric() {
        super("edge-iterator #edge triangles");
        this.highDegreeVertices = IntSets.emptySet;
    }


    public EdgeIteratorNumberOfTrianglesMetric(IntSet highDegreeVertices) {
        this();
        this.highDegreeVertices = highDegreeVertices;
    }


    @Override
    public double apply(ColouredGraph graph) {
        edges = new IntSet[graph.getGraph().getNumberOfVertices()];
        IntSet visitedVertices = IntSets.from(new int[] {});
        Grph grph = getUndirectedGraph(graph.getGraph());

        for (int i = 0; i < edges.length; ++i) {
            edges[i] = grph.getOutEdges(i);
            edges[i].addAll(grph.getInEdges(i));
        }

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
                            numberOfTriangles = numberOfTriangles +
                                    (IntSets.intersection(edges[vertex.value], edges[neighbor1.value]).size() *
                                            IntSets.intersection(edges[neighbor1.value], edges[neighbor2.value]).size() *
                                            IntSets.intersection(edges[neighbor2.value], edges[vertex.value]).size());
                        }
                    }
                }
            }
            visitedVertices.add(vertex.value);
        }
        return numberOfTriangles;
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

}