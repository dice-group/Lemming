package org.aksw.simba.lemming.metrics.metricselection;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardMetric;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DANISH AHMED on 8/27/2018
 */
public class NodeTriangleMetricSelection {

    public SingleValueMetric getMinComplexityMetric(ColouredGraph graph) {
        int edges = graph.getGraph().getNumberOfEdges();
        int vertices = graph.getGraph().getNumberOfVertices();

        List<TriangleMetric> nodeTriangleMetrics = new ArrayList<>();
        nodeTriangleMetrics.add(new ForwardMetric());
        nodeTriangleMetrics.add(new EdgeIteratorMetric());
        nodeTriangleMetrics.add(new NodeIteratorMetric());
        nodeTriangleMetrics.add(new NodeIteratorCoreMetric());
        nodeTriangleMetrics.add(new MultiThreadedNodeNeighborTrianglesMetric());

        double minComplexity = Integer.MAX_VALUE;
        TriangleMetric minNodeMetric = null;

        for (TriangleMetric metric : nodeTriangleMetrics) {
            double complexity = metric.calculateComplexity(edges, vertices);
            if (complexity < minComplexity) {
                minComplexity = complexity;
                minNodeMetric = null;
                minNodeMetric = metric;
            }
        }
        return minNodeMetric;
    }
}
