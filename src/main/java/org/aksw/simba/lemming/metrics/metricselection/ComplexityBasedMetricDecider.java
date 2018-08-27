package org.aksw.simba.lemming.metrics.metricselection;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardMetric;

/**
 * @author DANISH AHMED on 8/24/2018
 */
public class ComplexityBasedMetricDecider {
    private ColouredGraph graph;
    private int numEdges;
    private int numVertices;

    public ComplexityBasedMetricDecider(ColouredGraph graph) {
        this.graph = graph;
        this.numEdges = this.graph.getGraph().getNumberOfEdges();
        this.numVertices = this.graph.getGraph().getNumberOfVertices();
    }

    public ColouredGraph getGraph() {
        return graph;
    }

    public SingleValueMetric getMinComplexityForNodeMetric() {
        double minComplexity = Integer.MAX_VALUE;
        Complexity.NodeMetrics minNodeMetric = null;

        for (Complexity.NodeMetrics nodeMetrics : Complexity.NodeMetrics.values()) {
            double complexity = Complexity.computeNodeMetricsComplexity(this.numEdges, this.numVertices, nodeMetrics);
            if (complexity < minComplexity) {
                minComplexity = complexity;
                minNodeMetric = nodeMetrics;
            }
        }

        System.out.println(minNodeMetric.toString());

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

    public SingleValueMetric getMinComplexityForEdgeMetric() {
        double minComplexity = Integer.MAX_VALUE;
        Complexity.EdgeMetrics minEdgeMetric = null;

        for (Complexity.EdgeMetrics edgeMetrics : Complexity.EdgeMetrics.values()) {
            double complexity = Complexity.computeEdgeMetricsComplexity(this.numEdges, this.numVertices, edgeMetrics);
            if (complexity < minComplexity) {
                minComplexity = complexity;
                minEdgeMetric = edgeMetrics;
            }
        }

        System.out.println(minEdgeMetric.toString());

        switch (minEdgeMetric) {
            case FORWARD:
                return new org.aksw.simba.lemming.metrics.single.edgetriangles.forward.ForwardMetric();
            case EDGE_ITERATOR:
                return new org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorMetric();
            case EDGE_NUMBER_OF_SIMPLE_TRIANGLES:
                return new EdgeNumberOfSimpleTrianglesMetric();
            case NODE_ITERATOR:
                return new org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric();
            case MULTITHREADED_NODE_NEIGHBORS_COMMON_EDGES:
                return new MultiThreadedNodeNeighborsCommonEdgesMetric();
        }
        return null;
    }
}
