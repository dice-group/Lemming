package org.aksw.simba.lemming.metrics.single.degree;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.junit.Assert;
import org.junit.Test;

public class MaxVertexDegreeMetricTest extends MetricTest {

    @Test
    public void test1(){
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph1();
        double value = metric.apply(graph);
        Assert.assertEquals(1.0, value);

        graph = removeEdge(graph, 0);
        value = metric.recompute(graph, 1,-1, false);
        Assert.assertEquals(1.0, value);
        value = metric.recompute(graph, 1,-1, true);
        Assert.assertEquals(1.0, value);

        graph = addEdge(graph, 0, 2);
        value = metric.recompute(graph, 2, +1, false);
        Assert.assertEquals(2.0, value);
        Assert.assertEquals(1.0, metric.getCachedMaximumInDegree());
        value = metric.recompute(graph, 2, +1, true);
        Assert.assertEquals(2.0, value);
        Assert.assertEquals(2.0, metric.getCachedMaximumInDegree());

        metric = new MaxVertexDegreeMetric(Grph.DIRECTION.out);
        graph = buildGraph1();
        value = metric.apply(graph);
        Assert.assertEquals(2.0, value);

        graph = removeEdge(graph, 0);
        value = metric.recompute(graph, 0,-1, false);
        Assert.assertEquals(1.0, value);
        Assert.assertEquals(2.0, metric.getCachedMaximumOutDegree());

        value = metric.recompute(graph, 0,-1, true);
        Assert.assertEquals(1.0, value);
        Assert.assertEquals(1.0, metric.getCachedMaximumOutDegree());

        graph = addEdge(graph, 0, 2);
        value = metric.recompute(graph, 0, +1, false);
        Assert.assertEquals(2.0, value);
        Assert.assertEquals(1.0, metric.getCachedMaximumOutDegree());

        value = metric.recompute(graph, 0, +1, true);
        Assert.assertEquals(2.0, value);
        Assert.assertEquals(2.0, metric.getCachedMaximumOutDegree());
    }

    @Test
    public void test2(){
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph2();
        double value = metric.apply(graph);
        Assert.assertEquals(2.0, value);

        graph = removeEdge(graph, 1);
        value = metric.recompute(graph, 1,-1, false);
        Assert.assertEquals(1.0, value);
        Assert.assertEquals(2.0, metric.getCachedMaximumInDegree());
        value = metric.recompute(graph, 1,-1, true);
        Assert.assertEquals(1.0, value);
        Assert.assertEquals(1.0, metric.getCachedMaximumInDegree());


        graph = addEdge(graph, 1, 1);
        value = metric.recompute(graph, 1, +1, false);
        Assert.assertEquals(2.0, value);
        Assert.assertEquals(1.0, metric.getCachedMaximumInDegree());
        value = metric.recompute(graph, 1, +1, true);
        Assert.assertEquals(2.0, value);
        Assert.assertEquals(2.0, metric.getCachedMaximumInDegree());


        metric = new MaxVertexDegreeMetric(Grph.DIRECTION.out);
        graph = buildGraph2();
        value = metric.apply(graph);
        Assert.assertEquals(3.0, value);

        graph = removeEdge(graph, 1);
        value = metric.recompute(graph, 0,-1, false);
        Assert.assertEquals(2.0, value);
        Assert.assertEquals(3.0, metric.getCachedMaximumOutDegree());
        value = metric.recompute(graph, 0,-1, true);
        Assert.assertEquals(2.0, value);
        Assert.assertEquals(2.0, metric.getCachedMaximumOutDegree());

        graph = addEdge(graph, 0, 0);
        value = metric.recompute(graph, 0, +1, false);
        Assert.assertEquals(3.0, value);
        Assert.assertEquals(2.0, metric.getCachedMaximumOutDegree());
        value = metric.recompute(graph, 0, +1, true);
        Assert.assertEquals(3.0, value);
        Assert.assertEquals(3.0, metric.getCachedMaximumOutDegree());
    }
}
