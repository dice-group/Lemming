package org.aksw.simba.lemming.metrics.single.numberoftriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.forward.ForwardMetric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class EdgeNumberOfTrianglesMetricTests extends NumberOfTrianglesMetricTest {

    private int expectedTriangles;
    private static final double DOUBLE_COMPARISON_DELTA = 0.000001;
    private ColouredGraph graph;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testParams = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            testParams.add(new Object[] {"graph1.n3", 1});
//            testParams.add(new Object[] {"graph_loop.n3", 2});
//            testParams.add(new Object[] {"graph_loop_2.n3", 5});
//            testParams.add(new Object[] {"email-Eu-core.n3", 489286});
        }
        return testParams;
    }

    public EdgeNumberOfTrianglesMetricTests(String graphFile, int expectedTriangles) {
        super();
        this.expectedTriangles = expectedTriangles;
        if (this.graph != null)
            this.graph = null;

        this.graph = getColouredGraph(graphFile);
    }

    @Test
    public void NodeIteratorTest() {
        Assert.assertNotNull(graph);

        NodeIteratorMetric metric = new NodeIteratorMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void EdgeNumberOfSimpleTrianglesTest() {
        Assert.assertNotNull(graph);

        EdgeNumberOfSimpleTrianglesMetric metric = new EdgeNumberOfSimpleTrianglesMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void NumberOfTrianglesTest() {
        Assert.assertNotNull(graph);

        NumberOfTrianglesMetric metric = new NumberOfTrianglesMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void ForwardMetricTest() {
        Assert.assertNotNull(graph);

        ForwardMetric metric = new ForwardMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void EdgeIteratorMetricTest() {
        Assert.assertNotNull(graph);

        EdgeIteratorMetric metric = new EdgeIteratorMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }
}
