package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.SingleValueClusteringCoefficientMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DANISH AHMED on 8/10/2018
 */
public class EdgeModificationTest extends NumberOfTrianglesMetricTest {

    @Test
    public void edgeAdditionToGraph() {
        List<SingleValueMetric> nodeTriangleMetrics = new ArrayList<>();
        nodeTriangleMetrics.add(new MultiThreadedNodeNeighborTrianglesMetric());

        List<SingleValueMetric> edgeTriangleMetrics = new ArrayList<>();
        edgeTriangleMetrics.add(new MultiThreadedNodeNeighborsCommonEdgesMetric());

        Config metricConfiguration = new Config();
        metricConfiguration.setNodeTriangleMetric(nodeTriangleMetrics);
        metricConfiguration.setEdgeTriangleMetric(edgeTriangleMetrics);

        ColouredGraph graph = getColouredGraph("graph1.n3");
        EdgeModification edgeModification = new EdgeModification(graph, metricConfiguration);
//        edgeModification.addEdgeToGraph(2, 1, new BitSet());
        edgeModification.removeEdgeFromGraph(1);
    }
}
