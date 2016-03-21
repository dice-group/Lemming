package org.aksw.simba.lemming.metrics;

import org.aksw.simba.lemming.ColouredGraph;

/**
 * A metric describes a certain feature of a graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface Metric {

    /**
     * Returns the name of the metric.
     * 
     * @return the name of the metric.
     */
    public String getName();

    /**
     * Applies the metric to the given graph.
     * 
     * @param graph
     *            the graph for which the metric should be calculated.
     */
    public void apply(ColouredGraph graph);
}
