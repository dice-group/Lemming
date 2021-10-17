package org.aksw.simba.lemming.metrics.single.degree;

import grph.Grph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.junit.Assert;
import org.junit.Test;

public class StdDevVertexDegreeTest extends MetricTest{

    @Test
    public void test1(){
        StdDevVertexDegree metric = new StdDevVertexDegree(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph1();
        double value = metric.apply(graph);
        Assert.assertEquals(Math.sqrt(2.0/9), value, 0.00001);
        Assert.assertEquals(3, metric.getCachedNumOfVertices());
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(2.0, metric.getCachedSumOfSquare());

        graph = removeEdge(graph, 0);
        value = metric.recompute(graph, 1, -1, false);
        Assert.assertEquals(Math.sqrt(2.0/9), value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(2.0, metric.getCachedSumOfSquare());
        value = metric.recompute(graph, 1, -1, true);
        Assert.assertEquals(Math.sqrt(2.0/9), value, 0.00001);
        Assert.assertEquals(1.0, metric.getCachedSum());
        Assert.assertEquals(1.0, metric.getCachedSumOfSquare());

        graph = addEdge(graph, 0, 2);
        value = metric.recompute(graph, 2, 1, false);
        Assert.assertEquals(Math.sqrt(8.0/9), value, 0.00001);
        Assert.assertEquals(1.0, metric.getCachedSum());
        Assert.assertEquals(1.0, metric.getCachedSumOfSquare());
        value = metric.recompute(graph, 2, 1, true);
        Assert.assertEquals(Math.sqrt(8.0/9), value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(4.0, metric.getCachedSumOfSquare());

        metric = new StdDevVertexDegree(Grph.DIRECTION.out);
        graph = buildGraph1();
        value = metric.apply(graph);
        Assert.assertEquals(Math.sqrt(8.0/9), value, 0.00001);
        Assert.assertEquals(3, metric.getCachedNumOfVertices());
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(4.0, metric.getCachedSumOfSquare());

        graph = removeEdge(graph, 0);
        value = metric.recompute(graph, 0, -1, false);
        Assert.assertEquals(Math.sqrt(2.0/9), value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(4.0, metric.getCachedSumOfSquare());
        value = metric.recompute(graph, 0, -1, true);
        Assert.assertEquals(Math.sqrt(2.0/9), value, 0.00001);
        Assert.assertEquals(1.0, metric.getCachedSum());
        Assert.assertEquals(1.0, metric.getCachedSumOfSquare());

        graph = addEdge(graph, 1, 0);
        value = metric.recompute(graph, 1, 1, false);
        Assert.assertEquals(Math.sqrt(2.0/9), value, 0.00001);
        Assert.assertEquals(1.0, metric.getCachedSum());
        Assert.assertEquals(1.0, metric.getCachedSumOfSquare());
        value = metric.recompute(graph, 1, 1, true);
        Assert.assertEquals(Math.sqrt(2.0/9), value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(2.0, metric.getCachedSumOfSquare());
    }

    @Test
    public void test2(){
        StdDevVertexDegree metric = new StdDevVertexDegree(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph2();
        double value = metric.apply(graph);
        Assert.assertEquals(0.5, value, 0.00001);
        Assert.assertEquals(2, metric.getCachedNumOfVertices());
        Assert.assertEquals(3.0, metric.getCachedSum());
        Assert.assertEquals(5.0, metric.getCachedSumOfSquare());

        graph = removeEdge(graph, 0);
        value = metric.recompute(graph, 0, -1, false);
        Assert.assertEquals(1.0, value, 0.00001);
        Assert.assertEquals(3.0, metric.getCachedSum());
        Assert.assertEquals(5.0, metric.getCachedSumOfSquare());
        value = metric.recompute(graph, 0, -1, true);
        Assert.assertEquals(1.0, value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(4.0, metric.getCachedSumOfSquare());

        graph = addEdge(graph, 0, 1);
        value = metric.recompute(graph, 1, 1, false);
        Assert.assertEquals(1.5, value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(4.0, metric.getCachedSumOfSquare());
        value = metric.recompute(graph, 1, 1, true);
        Assert.assertEquals(1.5, value, 0.00001);
        Assert.assertEquals(3.0, metric.getCachedSum());
        Assert.assertEquals(9.0, metric.getCachedSumOfSquare());

        metric = new StdDevVertexDegree(Grph.DIRECTION.out);
        graph = buildGraph2();
        value = metric.apply(graph);
        Assert.assertEquals(1.5, value, 0.00001);
        Assert.assertEquals(2, metric.getCachedNumOfVertices());
        Assert.assertEquals(3.0, metric.getCachedSum());
        Assert.assertEquals(9.0, metric.getCachedSumOfSquare());

        graph = removeEdge(graph, 0);
        value = metric.recompute(graph, 0, -1, false);
        Assert.assertEquals(1.0, value, 0.00001);
        Assert.assertEquals(3.0, metric.getCachedSum());
        Assert.assertEquals(9.0, metric.getCachedSumOfSquare());
        value = metric.recompute(graph, 0, -1, true);
        Assert.assertEquals(1.0, value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(4.0, metric.getCachedSumOfSquare());

        graph = addEdge(graph, 0, 1);
        value = metric.recompute(graph, 0, 1, false);
        Assert.assertEquals(1.5, value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        Assert.assertEquals(4.0, metric.getCachedSumOfSquare());
        value = metric.recompute(graph, 0, 1, true);
        Assert.assertEquals(1.5, value, 0.00001);
        Assert.assertEquals(3.0, metric.getCachedSum());
        Assert.assertEquals(9.0, metric.getCachedSumOfSquare());
    }
}
