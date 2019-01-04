package org.aksw.simba.lemming.metrics.metricselection;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.forward.ForwardEdgeTriangleMetric;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DANISH AHMED on 8/27/2018
 */
public class EdgeTriangleMetricSelection{

    public SingleValueMetric getMinComplexityMetric(ColouredGraph graph) {
        int edges = graph.getGraph().getNumberOfEdges();
        int vertices = graph.getGraph().getNumberOfVertices();

        List<TriangleMetric> edgeTriangleMetrics = new ArrayList<>();
        edgeTriangleMetrics.add(new ForwardEdgeTriangleMetric());
        edgeTriangleMetrics.add(new EdgeIteratorMetric());
        edgeTriangleMetrics.add(new EdgeNumberOfSimpleTrianglesMetric());
        edgeTriangleMetrics.add(new NodeIteratorMetric());
        edgeTriangleMetrics.add(new MultiThreadedNodeNeighborsCommonEdgesMetric());

        double minComplexity = Integer.MAX_VALUE;
        TriangleMetric minEdgeMetric = null;

        for (TriangleMetric metric : edgeTriangleMetrics) {
            double complexity = metric.calculateComplexity(edges, vertices);
            if (complexity < minComplexity) {
                minComplexity = complexity;
                minEdgeMetric = null;
                minEdgeMetric = metric;
            }
        }
        return minEdgeMetric;
    }
}
