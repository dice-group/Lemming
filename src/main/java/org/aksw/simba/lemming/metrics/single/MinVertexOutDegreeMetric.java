package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

/**
 * This metric is the lowest degree of outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MinVertexOutDegreeMetric extends AbstractMetric implements SingleValueMetric {

    public MinVertexOutDegreeMetric() {
        super("minOutDegree");
    }

    @Override
    public double apply(IColouredGraph graph) {
        return graph.getGraph().getMinOutVertexDegrees();
    }

}
