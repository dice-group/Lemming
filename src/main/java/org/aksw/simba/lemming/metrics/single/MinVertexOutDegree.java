package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

/**
 * This metric is the lowest degree of outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MinVertexOutDegree extends AbstractSingleValueMetric implements SingleValueMetric {

    public MinVertexOutDegree() {
        super("minOutDegree");
    }

    @Override
    public void apply(ColouredGraph graph) {
        value = graph.getGraph().getMinOutVertexDegrees();
    }

}
