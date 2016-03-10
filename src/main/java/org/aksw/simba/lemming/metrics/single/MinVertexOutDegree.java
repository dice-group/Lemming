package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

public class MinVertexOutDegree extends AbstractSingleValueMetric implements SingleValueMetric {

    public MinVertexOutDegree() {
        super("minOutDegree");
    }

    @Override
    public void apply(ColouredGraph graph) {
        value = graph.getGraph().getMinOutVertexDegrees();
    }

}
