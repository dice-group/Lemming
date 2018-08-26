package org.aksw.simba.lemming.metrics.metricselection;

import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

/**
 * @author DANISH AHMED on 8/24/2018
 */
public class Complexity {
    public enum NodeMetrics {
        FORWARD, EDGE_ITERATOR, NODE_ITERATOR, NODE_ITERATOR_CORE, MULTITHREADED_NODE_NEIGHBORS;
    }
    public static double computeNodeMetricsComplexity(int edges, int vertices, NodeMetrics nodeMetrics) {
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
        return 0.0;
    }
}
