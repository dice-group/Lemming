package org.aksw.simba.lemming.metrics.single.edgetriangles;

import grph.DefaultIntSet;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntSet;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import org.aksw.simba.lemming.util.IntSetUtil;

/**
 * @author DANISH AHMED on 6/13/2018
 */
public class NodeIteratorMetric extends AbstractMetric implements TriangleMetric {
    public NodeIteratorMetric() {
        super("#edgetriangles");
    }

    @Override
    public double apply(IColouredGraph graph) {
        IntSet[] edges = new IntSet[graph.getGraph().getNumberOfVertices()];

        Grph grph = getUndirectedGraph(graph.getGraph());

        for (int i = 0; i < edges.length; ++i) {
            edges[i] = grph.getOutEdges(i);
            edges[i].addAll(grph.getInEdges(i));
        }

        int numberOfTriangles = 0;
        IntSet vertGrph = graph.getVertices();
        IntSet visitedVertices = new DefaultIntSet(vertGrph.size());
        for (int vertex : vertGrph) {
            IntSet neighbors = IntSetUtil.difference(
                    IntSetUtil.union(grph.getInNeighbors(vertex), grph.getOutNeighbors(vertex)), visitedVertices);
            for (int neighbor1 : neighbors) {
                IntSet neighbors1 = IntSetUtil.difference(
                        IntSetUtil.union(grph.getInNeighbors(neighbor1), grph.getOutNeighbors(neighbor1)),
                        visitedVertices);
                for (int neighbor2 : neighbors) {
                    if (vertex != neighbor1 && vertex != neighbor2 && neighbor1 < neighbor2
                            && neighbors1.contains(neighbor2)) {
                        numberOfTriangles = numberOfTriangles
                                + (IntSetUtil.intersection(edges[vertex], edges[neighbor1]).size()
                                        * IntSetUtil.intersection(edges[neighbor1], edges[neighbor2]).size()
                                        * IntSetUtil.intersection(edges[neighbor2], edges[vertex]).size());
                    }
                }
            }
            visitedVertices.add(vertex);
        }
        return numberOfTriangles;
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
        return vertices * Math.pow(edges, 2) * (edges / (double) vertices);
    }
}