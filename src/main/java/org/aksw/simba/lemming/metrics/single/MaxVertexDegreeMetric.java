package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import grph.Grph.DIRECTION;

/**
 * This metric is the highest degree of in or outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MaxVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

    protected DIRECTION direction;

    public MaxVertexDegreeMetric(DIRECTION direction) {
        super(direction == DIRECTION.in ? "maxInDegree" : "maxOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {
        if (direction == DIRECTION.in) {
            return graph.getGraph().getMaxInEdgeDegrees();
        } else {
            return graph.getGraph().getMaxOutEdgeDegrees();
        }
    }

}
