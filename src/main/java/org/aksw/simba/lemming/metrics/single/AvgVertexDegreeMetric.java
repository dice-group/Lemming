package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import com.carrotsearch.hppc.IntArrayList;

/**
 * This metric determines the average degree of outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 * @deprecated since it can be calculated automatically by the refinement engine
 *             using the number of vertices and the number of edges.
 */
@Deprecated
public class AvgVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

    public AvgVertexDegreeMetric() {
        super("avgDegree");
    }

    protected AvgVertexDegreeMetric(String name) {
        super(name);
    }

    @Override
    public double apply(ColouredGraph graph) {
        return calculateAvg(graph.getGraph().getAllInEdgeDegrees());
    }

    protected double calculateAvg(IntArrayList degrees) {
        double sum = 0;
        for (int i = 0; i < degrees.elementsCount; ++i) {
            sum += degrees.buffer[i];
        }
        return sum / degrees.elementsCount;
    }

}
