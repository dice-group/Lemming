package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

public class NumberOfVertices extends AbstractSingleValueMetric implements SingleValueMetric {

    public NumberOfVertices() {
        super("#vertices");
    }

    @Override
    public void apply(ColouredGraph graph) {
        value = graph.getGraph().getNumberOfVertices();
    }

}
