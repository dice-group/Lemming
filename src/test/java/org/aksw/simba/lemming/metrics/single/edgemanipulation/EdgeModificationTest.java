package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.junit.Assert;
import org.junit.Test;

import grph.Grph;
import toools.set.IntSet;

/**
 * @author DANISH AHMED on 8/10/2018
 */
public class EdgeModificationTest extends NumberOfTrianglesMetricTest {
    
    public EdgeModificationTest() {
        super("graph_loop_2.n3", 0);
    }

    @Test
    public void edgeRemoval() {
        Assert.assertNotNull(graph);

        EdgeModification edgeModification = new EdgeModification(graph,
                new MultiThreadedNodeNeighborTrianglesMetric(),
                new MultiThreadedNodeNeighborsCommonEdgesMetric());

        edgeModification.removeEdgeFromGraph(1);
        int removeNodeTri = edgeModification.getNewNodeTriangles();
        int removeEdgeTri = edgeModification.getNewEdgeTriangles();

        Assert.assertEquals(2, removeNodeTri);
        Assert.assertEquals(3, removeEdgeTri);
    }

    @Test
    public void edgeAddition() {
        Assert.assertNotNull(graph);

        EdgeModification edgeModification = new EdgeModification(graph,
                new MultiThreadedNodeNeighborTrianglesMetric(),
                new MultiThreadedNodeNeighborsCommonEdgesMetric());

        edgeModification.addEdgeToGraph(0, 2, graph.getEdgeColour(1));
        int addEdgeNodeTri = edgeModification.getNewNodeTriangles();
        int addEdgeEdgeTri = edgeModification.getNewEdgeTriangles();

        Assert.assertEquals(3, addEdgeNodeTri);
        Assert.assertEquals(7, addEdgeEdgeTri);
    }


    @Test
    public void demo() {
        EdgeModification edgeModification = new EdgeModification(graph,
                new MultiThreadedNodeNeighborTrianglesMetric(),
                new MultiThreadedNodeNeighborsCommonEdgesMetric());

        int initialGraphNodeTri = edgeModification.getOldNodeTriangles();
        int initialGraphEdgeTri = edgeModification.getOldEdgeTriangles();
        System.out.println("IniGraph:");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + initialGraphNodeTri + "\nET:\t" + initialGraphEdgeTri);

         //Before removing edge, you have to get vertices it is connected to, so you can revert back graph
        int edgeId = 0;
        Grph grph = graph.getGraph();
        IntSet verticesConnectedToRemovingEdge = grph.getVerticesIncidentToEdge(1);

         //Removing edge
        com.carrotsearch.hppc.BitSet edgeColor = graph.getEdgeColour(1);
        edgeModification.removeEdgeFromGraph(1);
        int removeNodeTri = edgeModification.getNewNodeTriangles();
        int removeEdgeTri = edgeModification.getNewEdgeTriangles();
        System.out.println();
        System.out.println("RemovingEdge Graph:");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + removeNodeTri + "\nET:\t" + removeEdgeTri);

         //reverting graph to original state by adding vertices
        edgeId = edgeModification.addEdgeToGraph(verticesConnectedToRemovingEdge.toIntArray()[0],
                verticesConnectedToRemovingEdge.toIntArray()[1], edgeColor);
        System.out.println();
        System.out.println("After reverting Graph (adding edge " + edgeId + "):");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + edgeModification.getNewNodeTriangles() + "\nET:\t" + edgeModification.getNewEdgeTriangles());

         //Adding Edge to vertices
        edgeId = edgeModification.addEdgeToGraph(2, 1, new com.carrotsearch.hppc.BitSet());
        int addEdgeNodeTri = edgeModification.getNewNodeTriangles();
        int addEdgeEdgeTri = edgeModification.getNewEdgeTriangles();
        System.out.println();
        System.out.println("AddingEdge Graph (edge " + edgeId + "):");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + addEdgeNodeTri + "\nET:\t" + addEdgeEdgeTri);

        edgeModification.removeEdgeFromGraph(edgeId);
        removeNodeTri = edgeModification.getNewNodeTriangles();
        removeEdgeTri = edgeModification.getNewEdgeTriangles();
        System.out.println();
        System.out.println("RemovingEdge Graph:");
        System.out.println("NumV:\t" +edgeModification.getGraph().getGraph().getNumberOfVertices());
        System.out.println("NumE:\t" +edgeModification.getGraph().getGraph().getNumberOfEdges());
        System.out.println("NT:\t" + removeNodeTri + "\nET:\t" + removeEdgeTri);
    }
}
