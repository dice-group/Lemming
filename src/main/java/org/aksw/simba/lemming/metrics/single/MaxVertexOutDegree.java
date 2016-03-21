package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

/**
 * This metric is the highest degree of outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MaxVertexOutDegree extends AbstractSingleValueMetric implements SingleValueMetric {

    public MaxVertexOutDegree() {
        super("maxOutDegree");
    }

    @Override
    public void apply(ColouredGraph graph) {
        value = graph.getGraph().getMaxOutVertexDegrees();
    }

}
