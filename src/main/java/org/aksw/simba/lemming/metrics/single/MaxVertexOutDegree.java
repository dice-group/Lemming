package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

public class MaxVertexOutDegree extends AbstractSingleValueMetric implements SingleValueMetric {

    public MaxVertexOutDegree() {
        super("maxOutDegree");
    }

    @Override
    public void apply(ColouredGraph graph) {
        value = graph.getGraph().getMaxOutVertexDegrees();
    }

}
