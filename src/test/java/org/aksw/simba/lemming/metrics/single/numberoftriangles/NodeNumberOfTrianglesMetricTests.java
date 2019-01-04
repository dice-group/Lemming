package org.aksw.simba.lemming.metrics.single.numberoftriangles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.nodetriangles.DuolionMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.ListingAyzMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardNodeTriangleMetric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class NodeNumberOfTrianglesMetricTests extends NumberOfTrianglesMetricTest {

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
        super(graphFile, expectedTriangles);
    }

    @Test
    public void ListingAyzMetric() {
        this.metric = new ListingAyzMetric();
        test();
    }

    @Test
    public void forwardMetric() {
        this.metric = new ForwardNodeTriangleMetric();
        test();
    }

    @Test
    public void nodeIteratorCoreMetric() {
        this.metric = new NodeIteratorCoreMetric();
        test();
    }

    @Test
    public void nodeIteratorMetric() {
        this.metric = new NodeIteratorMetric();
        test();
    }

    @Test
    public void multiThreadedNodeNeighborTrianglesMetric() {
        this.metric = new MultiThreadedNodeNeighborTrianglesMetric();
        test();
    }

    @Test
    public void edgeIteratorMetric() {
        this.metric = new EdgeIteratorMetric();
        test();
    }

    @Test
    public void duolionMetric() {
        Assert.assertNotNull(graph);

        final double edgeSurvivalProbability = 0.9;
        DuolionMetric metric = new DuolionMetric(new ForwardNodeTriangleMetric(), edgeSurvivalProbability,
                new Random().nextLong());
        double countedTriangles = metric.apply(graph);

        double range = 0.25;
        double minRange = expectedTriangles - (expectedTriangles * range);
        double maxRange = expectedTriangles + (expectedTriangles * range);
        Assert.assertTrue(countedTriangles >= minRange && countedTriangles <= maxRange);
    }
}
