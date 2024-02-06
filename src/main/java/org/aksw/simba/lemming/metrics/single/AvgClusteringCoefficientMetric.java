package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.springframework.stereotype.Component;

/**
 * This metric is the average clustering coefficient of the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
@Component("avgClusterCoefficient")
public class AvgClusteringCoefficientMetric extends AbstractMetric implements SingleValueMetric {

    public AvgClusteringCoefficientMetric() {
        super("avgClusterCoefficient");
    }

    @Override
    public double apply(IColouredGraph graph) {
        return graph.getGraph().getAverageClusteringCoefficient();
    }

}
