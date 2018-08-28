package org.aksw.simba.lemming.metrics.metricselection;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

/**
 * @author DANISH AHMED on 8/27/2018
 */
public interface ComplexityAwareMetric {
    public enum Metrics {
        FORWARD, EDGE_ITERATOR, NODE_ITERATOR, NODE_ITERATOR_CORE, MULTITHREADED_NODE_NEIGHBORS,
        EDGE_NUMBER_OF_SIMPLE_TRIANGLES, MULTITHREADED_NODE_NEIGHBORS_COMMON_EDGES;
    }
    public double calculateComplexity(Metrics metric, ColouredGraph graph);

    public SingleValueMetric getMinComplexityMetric(ColouredGraph graph);
}
