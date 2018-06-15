package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.nodetriangles.DuolionNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.AyzNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardNumberOfTriangleMetric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@RunWith(Parameterized.class)
public class NodeNumberOfTrianglesMetricTests extends NumberOfTrianglesMetricTest{
    private int expectedTriangles;
    private static final double DOUBLE_COMPARISON_DELTA = 0.000001;
    private ColouredGraph graph;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<>();
        testConfigs.add(new Object[] { "graph1.n3", 1 });
        testConfigs.add(new Object[] { "graph_loop.n3", 1 });
        testConfigs.add(new Object[] { "graph_loop_2.n3", 3 });
        testConfigs.add(new Object[] { "email-Eu-core.n3", 105461 });

        return testConfigs;
    }

    public NodeNumberOfTrianglesMetricTests(String graphFile, int expectedTriangles) {
        super();
        this.expectedTriangles = expectedTriangles;
        if (this.graph != null)
            this.graph = null;

        this.graph = getColouredGraph(graphFile);
    }

    @Test
    public void AyzNumberOfTrianglesTest() {
        Assert.assertNotNull(graph);

        final double delta = 3.0;
        AyzNumberOfTrianglesMetric metric = new AyzNumberOfTrianglesMetric(delta);
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void ForwardNumberOfTriangleTest() {
        Assert.assertNotNull(graph);

        ForwardNumberOfTriangleMetric metric = new ForwardNumberOfTriangleMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void NodeIteratorCoreNumberOfTrianglesTest() {
        Assert.assertNotNull(graph);

        NodeIteratorCoreNumberOfTrianglesMetric metric = new NodeIteratorCoreNumberOfTrianglesMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void NodeIteratorNumberOfTrianglesTest() {
        Assert.assertNotNull(graph);

        NodeIteratorNumberOfTrianglesMetric metric = new NodeIteratorNumberOfTrianglesMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void NumberOfSimpleTrianglesTest() {
        Assert.assertNotNull(graph);

        NumberOfSimpleTrianglesMetric metric = new NumberOfSimpleTrianglesMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void DuolionNumberOfTrianglesTest() {
        Assert.assertNotNull(graph);

        final double edgeSurvivalProbability = 0.9;
        DuolionNumberOfTrianglesMetric metric = new DuolionNumberOfTrianglesMetric(new ForwardNumberOfTriangleMetric(), edgeSurvivalProbability, new Random().nextLong());
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }
}
