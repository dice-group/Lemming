package org.aksw.simba.lemming.metrics.single.degree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.junit.Assert;
import org.junit.Test;

public class AvgVertexDegreeMetricTest extends MetricTest{

    @Test
    public void test1(){
        AvgVertexDegreeMetric metric = new AvgVertexDegreeMetric();
        ColouredGraph graph = buildGraph1();
        double value = metric.apply(graph);
        Assert.assertEquals(2.0/3, value, 0.00001);
        Assert.assertEquals(3, metric.getCachedNumOfVertices());
        Assert.assertEquals(2.0, metric.getCachedSum());

        graph = removeEdge(graph, 0);
        value = metric.recompute(-1, false);
        Assert.assertEquals(1.0/3, value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        value = metric.recompute(-1, true);
        Assert.assertEquals(1.0/3, value, 0.00001);
        Assert.assertEquals(1.0, metric.getCachedSum());

        graph = addEdge(graph, 0, 2);
        value = metric.recompute(1, false);
        Assert.assertEquals(2.0/3, value, 0.00001);
        Assert.assertEquals(1.0, metric.getCachedSum());
        value = metric.recompute(1, true);
        Assert.assertEquals(2.0/3, value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
    }

    @Test
    public void test2(){
        AvgVertexDegreeMetric metric = new AvgVertexDegreeMetric();
        ColouredGraph graph = buildGraph2();
        double value = metric.apply(graph);
        Assert.assertEquals(3.0/2, value, 0.00001);
        Assert.assertEquals(2, metric.getCachedNumOfVertices());
        Assert.assertEquals(3.0, metric.getCachedSum());

        graph = removeEdge(graph, 1);
        value = metric.recompute(-1, false);
        Assert.assertEquals(2.0/2, value, 0.00001);
        Assert.assertEquals(3.0, metric.getCachedSum());
        value = metric.recompute(-1, true);
        Assert.assertEquals(2.0/2, value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());

        graph = addEdge(graph, 1, 1);
        value = metric.recompute(1, false);
        Assert.assertEquals(3.0/2, value, 0.00001);
        Assert.assertEquals(2.0, metric.getCachedSum());
        value = metric.recompute(1, true);
        Assert.assertEquals(3.0/2, value, 0.00001);
        Assert.assertEquals(3.0, metric.getCachedSum());
    }
}
