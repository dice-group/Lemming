package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

/**
 * This metric is the number of vertices of the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class NumberOfVertices extends AbstractSingleValueMetric implements SingleValueMetric {

    public NumberOfVertices() {
        super("#vertices");
    }

    @Override
    public void apply(ColouredGraph graph) {
        value = graph.getGraph().getNumberOfVertices();
    }

}
