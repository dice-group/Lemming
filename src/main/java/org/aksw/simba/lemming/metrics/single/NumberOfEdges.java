package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

public class NumberOfEdges extends AbstractSingleValueMetric implements SingleValueMetric {

    public NumberOfEdges() {
        super("#edges");
    }

    @Override
    public void apply(ColouredGraph graph) {
        value = graph.getGraph().getNumberOfEdges();
    }

}
