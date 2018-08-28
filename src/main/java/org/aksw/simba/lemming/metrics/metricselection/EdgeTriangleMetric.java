package org.aksw.simba.lemming.metrics.metricselection;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.forward.ForwardMetric;

/**
 * @author DANISH AHMED on 8/27/2018
 */
public class EdgeTriangleMetric implements ComplexityAwareMetric {
    @Override
    public double calculateComplexity(Metrics edgeMetrics, ColouredGraph graph) {
        int edges = graph.getGraph().getNumberOfEdges();
        int vertices = graph.getGraph().getNumberOfVertices();

        switch (edgeMetrics) {
            case FORWARD:
                return (edges * Math.sqrt(edges) * (edges / (double) vertices));
            case EDGE_ITERATOR:
                return (Math.pow(edges, 4) / Math.pow(vertices, 4));
            case EDGE_NUMBER_OF_SIMPLE_TRIANGLES:
                return (Math.pow(edges, 2) / Math.pow(vertices, 2)) * (edges / (double) vertices);
            case NODE_ITERATOR:
                return vertices * Math.pow(edges, 2) * (edges / (double) vertices);
            case MULTITHREADED_NODE_NEIGHBORS_COMMON_EDGES:
                return (Math.pow(vertices, 2) * edges);
        }
        return Integer.MAX_VALUE;
    }

    public SingleValueMetric getMinComplexityMetric(ColouredGraph graph) {
        double minComplexity = Integer.MAX_VALUE;
        Metrics minEdgeMetric = null;

        for (Metrics edgeMetrics : Metrics.values()) {
            double complexity = calculateComplexity(edgeMetrics, graph);
            if (complexity < minComplexity) {
                minComplexity = complexity;
                minEdgeMetric = edgeMetrics;
            }
        }

        assert minEdgeMetric != null;
        switch (minEdgeMetric) {
            case FORWARD:
                return new ForwardMetric();
            case EDGE_ITERATOR:
                return new EdgeIteratorMetric();
            case EDGE_NUMBER_OF_SIMPLE_TRIANGLES:
                return new EdgeNumberOfSimpleTrianglesMetric();
            case NODE_ITERATOR:
                return new NodeIteratorMetric();
            case MULTITHREADED_NODE_NEIGHBORS_COMMON_EDGES:
                return new MultiThreadedNodeNeighborsCommonEdgesMetric();
        }
        return null;
    }
}
