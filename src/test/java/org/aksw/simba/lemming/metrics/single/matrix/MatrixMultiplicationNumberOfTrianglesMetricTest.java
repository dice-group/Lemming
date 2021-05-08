package org.aksw.simba.lemming.metrics.single.matrix;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import junit.framework.Assert;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.InMemoryPalette;
import org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.matrix.MatrixMultiplicationNumberOfTrianglesMetric;
import org.junit.Test;

public class MatrixMultiplicationNumberOfTrianglesMetricTest {

    ColourPalette vertexPalette = new InMemoryPalette();
    ColourPalette edgePalette = new InMemoryPalette();

    /**
     * Test the graph with V = {0, 1, 2}, E = {(0,1), (1,2), (2,1)}, number of triangles should be 1.0.
     */
    @Test
    public void metricTest(){

        ColouredGraph colouredGraph = new ColouredGraph(createGraph(), vertexPalette, edgePalette);
        MatrixMultiplicationNumberOfTrianglesMetric metric = new MatrixMultiplicationNumberOfTrianglesMetric();
        double numOfTriangles = metric.apply(colouredGraph);
        if(numOfTriangles != 1.0){
            System.out.println("Expected number of triangles is not equal to actual number: ");
            System.out.println("expected number: " + 1.0);
            System.out.println("actual number: " + numOfTriangles);
        }
        Assert.assertEquals(1.0, numOfTriangles);
    }

    private Grph createGraph(){
        Grph graph = new InMemoryGrph();
        int v1 = 0, v2 = 1, v3 = 2;
        graph.addDirectedSimpleEdge(v2, v3);
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v3, v1);
        return graph;
    }
}
