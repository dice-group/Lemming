package org.aksw.simba.lemming.metrics.single;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import grph.Grph;
import grph.Grph.DIRECTION;
import grph.in_memory.InMemoryGrph;

@RunWith(Parameterized.class)
public class DegreeTest {
    
    public static final double DELTA = 0.00001;

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        Grph graph;
        int v1, v2, v3;

        graph = new InMemoryGrph();
        v1 = graph.addVertex();
        v2 = graph.addVertex();
        v3 = graph.addVertex();
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v3);
        testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 1, 2, 2.0 / 3.0 });
        graph = new InMemoryGrph();
        v1 = graph.addVertex();
        v2 = graph.addVertex();
        v3 = graph.addVertex();
        graph.addDirectedSimpleEdge(v2, v1);
        graph.addDirectedSimpleEdge(v3, v1);
        testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 2, 1, 2.0 / 3.0 });
        graph = new InMemoryGrph();
        v1 = graph.addVertex();
        v2 = graph.addVertex();
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v2);
        testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 3, 3, 1.5 });

        return testConfigs;
    }

    private ColouredGraph graph;
    private int expectedMaxInDegree;
    private int expectedMaxOutDegree;
    private double expectedAvgDegree;

    public DegreeTest(ColouredGraph graph, Integer expectedMaxInDegree, Integer expectedMaxOutDegree, Double expectedAvgDegree) {
        this.graph = graph;
        this.expectedMaxInDegree = expectedMaxInDegree;
        this.expectedMaxOutDegree = expectedMaxOutDegree;
        this.expectedAvgDegree = expectedAvgDegree;
    }

    @Test
    public void run() {
        SingleValueMetric metric;
        double value;
        metric = new MaxVertexDegreeMetric(DIRECTION.in);
        value = metric.apply(graph);
        Assert.assertEquals(expectedMaxInDegree, value, DELTA);
        metric = new MaxVertexDegreeMetric(DIRECTION.out);
        value = metric.apply(graph);
        Assert.assertEquals(expectedMaxOutDegree, value, DELTA);
        metric = new AvgVertexDegreeMetric();
        value = metric.apply(graph);
        Assert.assertEquals(expectedAvgDegree, value, DELTA);
    }

}
