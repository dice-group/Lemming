package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

import org.aksw.simba.lemming.util.ColouredGraphConverter;
import org.junit.Assert;

public abstract class NumberOfTrianglesMetricTest {
    
    private static final double DOUBLE_COMPARISON_DELTA = 0.000001;

    protected SingleValueMetric metric;
    protected ColouredGraph graph;
    protected int expectedNumOfTriangles;

    public NumberOfTrianglesMetricTest(String graphFile, int expectedNumOfTriangles) {
        this.graph = ColouredGraphConverter.convertFileToGraph(graphFile);
        this.expectedNumOfTriangles = expectedNumOfTriangles;
    }

    /**
     * This is a test template for all NumberOfTrianglesMetrics.
     */
    public void test() {
        Assert.assertNotNull(graph);
        double actualNumOfTriangles = metric.apply(graph);
        Assert.assertEquals(expectedNumOfTriangles, actualNumOfTriangles, DOUBLE_COMPARISON_DELTA);
    }

}
