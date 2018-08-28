package org.aksw.simba.lemming.metrics.metricselection;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardMetric;

/**
 * @author DANISH AHMED on 8/27/2018
 */
public class NodeTriangleMetric implements ComplexityAwareMetric {
    @Override
    public double calculateComplexity(Metrics nodeMetrics, ColouredGraph graph) {
        int edges = graph.getGraph().getNumberOfEdges();
        int vertices = graph.getGraph().getNumberOfVertices();

        switch (nodeMetrics) {
            case FORWARD:
                return (edges * Math.sqrt(edges));
            case EDGE_ITERATOR:
                return (Math.pow(edges, 3) / Math.pow(vertices, 3));
            case NODE_ITERATOR:
                return vertices * Math.pow(edges, 2);
            case NODE_ITERATOR_CORE:
                return (vertices - 2) * Math.pow((edges/vertices), 2);
            case MULTITHREADED_NODE_NEIGHBORS:
                return (Math.pow(edges, 2) / Math.pow(vertices, 2));
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public SingleValueMetric getMinComplexityMetric(ColouredGraph graph) {
        double minComplexity = Integer.MAX_VALUE;
        Metrics minNodeMetric = null;

        for (Metrics nodeMetrics : Metrics.values()) {
            double complexity = calculateComplexity(nodeMetrics, graph);
            if (complexity < minComplexity) {
                minComplexity = complexity;
                minNodeMetric = nodeMetrics;
            }
        }

        assert minNodeMetric != null;
        switch (minNodeMetric) {
            case FORWARD:
                return new ForwardMetric();
            case EDGE_ITERATOR:
                return new EdgeIteratorMetric();
            case NODE_ITERATOR:
                return new NodeIteratorMetric();
            case NODE_ITERATOR_CORE:
                return new NodeIteratorCoreMetric();
            case MULTITHREADED_NODE_NEIGHBORS:
                return new MultiThreadedNodeNeighborTrianglesMetric();
        }
        return null;
    }
}
