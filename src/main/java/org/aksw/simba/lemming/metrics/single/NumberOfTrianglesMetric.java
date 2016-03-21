package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

/**
 * This metric is the number of triangles of the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class NumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

    public NumberOfTrianglesMetric() {
        super("#triangles");
    }

    @Override
    public double apply(ColouredGraph graph) {
        return graph.getGraph().getNumberOfTriangles();
    }

}
