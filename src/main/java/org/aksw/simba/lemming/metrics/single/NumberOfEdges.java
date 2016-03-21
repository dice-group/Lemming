package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

/**
 * This metric is the number of edges of the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class NumberOfEdges extends AbstractSingleValueMetric implements SingleValueMetric {

    public NumberOfEdges() {
        super("#edges");
    }

    @Override
    public void apply(ColouredGraph graph) {
        value = graph.getGraph().getNumberOfEdges();
    }

}
