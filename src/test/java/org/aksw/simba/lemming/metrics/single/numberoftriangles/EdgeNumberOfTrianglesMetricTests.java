package org.aksw.simba.lemming.metrics.single.numberoftriangles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.forward.ForwardEdgeTriangleMetric;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EdgeNumberOfTrianglesMetricTests extends NumberOfTrianglesMetricTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testParams = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            testParams.add(new Object[] {"graph1.n3", 1});
            testParams.add(new Object[] {"graph_loop.n3", 2});
            testParams.add(new Object[] {"graph_loop_2.n3", 5});
            testParams.add(new Object[] {"email-Eu-core.n3", 489286});
        }
        return testParams;
    }

    public EdgeNumberOfTrianglesMetricTests(String graphFile, int expectedTriangles) {
        super(graphFile, expectedTriangles);
    }

    @Test
    public void nodeIteratorMetric() {
        this.metric = new NodeIteratorMetric();
        test();
    }

    @Test
    public void edgeNumberOfSimpleTrianglesMetric() {
        this.metric = new EdgeNumberOfSimpleTrianglesMetric();
        test();
    }

    @Test
    public void multiThreadedNodeNeighborsCommonEdgesMetric() {
        this.metric = new MultiThreadedNodeNeighborsCommonEdgesMetric();
        test();
    }

    @Test
    public void forwardMetric() {
        this.metric = new ForwardEdgeTriangleMetric();
        test();
    }

    @Test
    public void edgeIteratorMetric() {
        this.metric = new EdgeIteratorMetric();
        test();
    }
}
