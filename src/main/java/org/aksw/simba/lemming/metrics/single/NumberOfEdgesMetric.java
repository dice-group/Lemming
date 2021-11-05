package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

/**
 * This metric is the number of edges of the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class NumberOfEdgesMetric extends AbstractMetric implements SingleValueMetric {

    public NumberOfEdgesMetric() {
        super("#edges");
    }

    @Override
    public UpdatableMetricResult apply(ColouredGraph graph) {
        return new SingleValueMetricResult(this.name, graph.getGraph().getNumberOfEdges());
    }

}
