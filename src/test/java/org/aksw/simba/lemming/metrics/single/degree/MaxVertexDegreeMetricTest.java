package org.aksw.simba.lemming.metrics.single.degree;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.junit.Assert;
import org.junit.Test;

public class MaxVertexDegreeMetricTest {

    @Test
    public void test1(){
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph1();
        double value = metric.apply(graph);
        Assert.assertEquals(1.0, value);

        graph = removeEdge(graph, 0);
        value = metric.update(graph, 1,-1);
        Assert.assertEquals(1.0, value);

        graph = addEdge(graph, 0, 2);
        value = metric.update(graph, 2, +1);
        Assert.assertEquals(2.0, value);

        metric = new MaxVertexDegreeMetric(Grph.DIRECTION.out);
        graph = buildGraph1();
        value = metric.apply(graph);
        Assert.assertEquals(2.0, value);

        graph = removeEdge(graph, 0);
        value = metric.update(graph, 0,-1);
        Assert.assertEquals(1.0, value);

        graph = addEdge(graph, 0, 2);
        value = metric.update(graph, 0, +1);
        Assert.assertEquals(2.0, value);
    }

    @Test
    public void test2(){
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph2();
        double value = metric.apply(graph);
        Assert.assertEquals(2.0, value);

        graph = removeEdge(graph, 1);
        value = metric.update(graph, 1,-1);
        Assert.assertEquals(1.0, value);


        graph = addEdge(graph, 1, 1);
        value = metric.update(graph, 1, +1);
        Assert.assertEquals(2.0, value);

        metric = new MaxVertexDegreeMetric(Grph.DIRECTION.out);
        graph = buildGraph2();
        value = metric.apply(graph);
        Assert.assertEquals(3.0, value);

        graph = removeEdge(graph, 1);
        value = metric.update(graph, 0,-1);
        Assert.assertEquals(2.0, value);


        graph = addEdge(graph, 0, 0);
        value = metric.update(graph, 0, +1);
        Assert.assertEquals(3.0, value);
    }

    private ColouredGraph buildGraph1(){
        Grph graph = new InMemoryGrph();
        int v1 = graph.addVertex();
        int v2 = graph.addVertex();
        int v3 = graph.addVertex();
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v3);
        return new ColouredGraph(graph, null, null);
    }

    private ColouredGraph buildGraph2(){
        Grph graph = new InMemoryGrph();
        int v1 = graph.addVertex();
        int v2 = graph.addVertex();
        graph.addDirectedSimpleEdge(v1, v1);
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v2);
        return new ColouredGraph(graph, null, null);
    }

    private ColouredGraph removeEdge(ColouredGraph graph, int edge){
        Grph grph = graph.getGraph();
        grph.removeEdge(edge);
        return new ColouredGraph(grph, null, null);
    }

    private ColouredGraph addEdge(ColouredGraph graph, int tail, int head){
        Grph grph = graph.getGraph();
        grph.addDirectedSimpleEdge(tail, head);
        return new ColouredGraph(grph, null, null);
    }
}
