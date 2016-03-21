package org.aksw.simba.lemming.metrics;

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
    
}
