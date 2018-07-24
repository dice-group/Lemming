package org.aksw.simba.lemming.metrics;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.AvgClusteringCoefficientMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author DANISH AHMED on 7/19/2018
 */
@RunWith(Parameterized.class)
public class AverageClusteringCoefficient extends NumberOfTrianglesMetricTest {
    private double expectedAvgCC;
    private ColouredGraph graph;
    private List<SingleValueMetric> metrics = new ArrayList<>();

    private static final double DOUBLE_COMPARISON_DELTA = 0.000001;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<>();
        testConfigs.add(new Object[] { "graph1.n3", (1/3.0) });
        testConfigs.add(new Object[] { "graph_loop.n3", (7/9.0) });
        testConfigs.add(new Object[] { "graph_loop_2.n3", (3.5/5) });
        testConfigs.add(new Object[] { "email-Eu-core.n3", 0.36942123976001284 });
        return testConfigs;
    }

    public AverageClusteringCoefficient(String graphFile, double expectedAvgCC) {
        super();
        this.expectedAvgCC = expectedAvgCC;
        this.metrics.add(new AvgClusteringCoefficientMetric());

        if (this.graph != null)
            this.graph = null;

        this.graph = getColouredGraph(graphFile);
    }

    private double calculateAvgCC(List<Double> clusteringCoefficient) {
        double ccSum = 0.0;
        for (double cc : clusteringCoefficient)
            ccSum += cc;
        return (ccSum / this.graph.getGraph().getNumberOfVertices());
    }

    @Test
    public void averageClusteringCoefficientMetric() {
        List<ObjectDoubleOpenHashMap<String>> vectors = new ArrayList<>();
        vectors.add(MetricUtils.calculateGraphMetrics(this.graph, metrics));

        Assert.assertEquals(expectedAvgCC,
                vectors.get(0).get(metrics.get(0).getName()),
                DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void nodeIteratorAvgCC() {
        NodeIteratorMetric metric = new NodeIteratorMetric(true);
        metric.apply(graph);
        List<Double> clusteringCoefficient = metric.getClusteringCoefficient();
        Assert.assertEquals(expectedAvgCC,
                calculateAvgCC(clusteringCoefficient),
                DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void nodeIteratorCoreAvgCC() {
        NodeIteratorCoreMetric metric = new NodeIteratorCoreMetric(true);
        metric.apply(graph);
        List<Double> clusteringCoefficient = metric.getClusteringCoefficient();
        Assert.assertEquals(expectedAvgCC,
                calculateAvgCC(clusteringCoefficient),
                DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void multiThreadedNodeNeighborTrianglesAvgCC() {
        MultiThreadedNodeNeighborTrianglesMetric metric = new MultiThreadedNodeNeighborTrianglesMetric(true);
        metric.apply(graph);
        List<Double> clusteringCoefficient = metric.getClusteringCoefficient();
        Assert.assertEquals(expectedAvgCC,
                calculateAvgCC(clusteringCoefficient),
                DOUBLE_COMPARISON_DELTA);
    }
}
