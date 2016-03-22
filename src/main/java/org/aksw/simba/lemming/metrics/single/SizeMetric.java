package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

/**
 * This metric is the size of the graph.
 * @author jsaveta
 */
public class SizeMetric extends AbstractMetric implements SingleValueMetric{

    public SizeMetric() {
        super("size");
    }

    @Override
    public double apply(ColouredGraph graph) {
        return graph.getGraph().getSize();
    }
    
}
