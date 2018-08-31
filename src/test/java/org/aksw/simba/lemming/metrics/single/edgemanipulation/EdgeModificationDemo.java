package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import grph.Grph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.SingleValueClusteringCoefficientMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.junit.Test;
import toools.set.IntSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DANISH AHMED on 8/10/2018
 */
public class EdgeModificationDemo extends NumberOfTrianglesMetricTest {

    @Test
    public void edgeAdditionToGraph() {
        ColouredGraph graph = getColouredGraph("graph_loop_2.n3");
        EdgeModification edgeModification = new EdgeModification(graph,
                new MultiThreadedNodeNeighborTrianglesMetric(),
                new MultiThreadedNodeNeighborsCommonEdgesMetric());

        int initialGraphNodeTri = edgeModification.getOldNodeTriangles();
        int initialGraphEdgeTri = edgeModification.getOldEdgeTriangles();
        System.out.println("IniGraph:");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + initialGraphNodeTri + "\nET:\t" + initialGraphEdgeTri);

        /* Before removing edge, you have to get vertices it is connected to, so you can revert back graph */
        Grph grph = graph.getGraph();
        IntSet verticesConnectedToRemovingEdge = grph.getVerticesIncidentToEdge(1);

        /* Removing edge */
        edgeModification.removeEdgeFromGraph(1);
        int removeNodeTri = edgeModification.getNewNodeTriangles();
        int removeEdgeTri = edgeModification.getNewEdgeTriangles();
        System.out.println();
        System.out.println("RemovingEdge Graph:");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + removeNodeTri + "\nET:\t" + removeEdgeTri);

        /* reverting graph to original state by adding vertices*/
        edgeModification.addEdgeToGraph(verticesConnectedToRemovingEdge.toIntArray()[0],
                verticesConnectedToRemovingEdge.toIntArray()[1], graph.getEdgeColour(1));
        System.out.println();
        System.out.println("After reverting Graph:");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + edgeModification.getNewNodeTriangles() + "\nET:\t" + edgeModification.getNewEdgeTriangles());

        /* Adding Edge to vertices */
        edgeModification.addEdgeToGraph(2, 1, graph.getEdgeColour(1));
        int addEdgeNodeTri = edgeModification.getNewNodeTriangles();
        int addEdgeEdgeTri = edgeModification.getNewEdgeTriangles();
        System.out.println();
        System.out.println("AddingEdge Graph:");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + addEdgeNodeTri + "\nET:\t" + addEdgeEdgeTri);
    }
}
