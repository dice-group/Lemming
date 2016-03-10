package org.aksw.simba.lemming.metrics;

import org.aksw.simba.lemming.ColouredGraph;

public interface Metric {

    public String getName();

    public void apply(ColouredGraph graph);
}
