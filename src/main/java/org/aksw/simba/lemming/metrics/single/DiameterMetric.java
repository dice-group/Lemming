package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphDecorator;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import grph.path.ArrayListPath;

/**
 * This metric is the diameter of the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class DiameterMetric extends AbstractMetric implements SingleValueMetric {

    public DiameterMetric() {
        super("diameter");
    }

    @Override
    public double apply(ColouredGraph graph) {
        try {
            return applyUpdatable(new ColouredGraphDecorator(graph)).getResult();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public UpdatableMetricResult applyUpdatable(ColouredGraphDecorator graph) {
        DiameterMetricResult metricResult = new DiameterMetricResult(getName(), Double.NaN);
        metricResult.setResult(graph.getDiameter());
        metricResult.setDiameterPath(graph.getNodesInDiameter());
        metricResult.setCountOfDiameters(graph.getCountOfDiameterPaths());
        return metricResult;
    }

    public UpdatableMetricResult update(ColouredGraphDecorator graph, TripleBaseSingleID triple,
            Operation graphOperation, UpdatableMetricResult previousResult) {
        DiameterMetricResult metricResult = ((DiameterMetricResult) previousResult);
        ArrayListPath oldPath = metricResult.getDiameterPath();
        int count = metricResult.getCountOfDiameters();
        if (graphOperation == Operation.ADD) {
            ArrayListPath newPath = graph.computeShorterDiameter(oldPath);
            if (count == 1) {
                // There was only one diameter and now it's length has been reduced
                metricResult.setResult(newPath.getLength());
                metricResult.setDiameterPath(newPath);
            } else if (newPath.getLength() < oldPath.getLength() && count > 1) {
                // There are other diameters of the same length so the result doesn't change but
                // now the path is different
                metricResult.setCountOfDiameters(count - 1);
                metricResult.setDiameterPath(graph.computeAlternateDiameter(oldPath.getDestination()));
            }
        } else if (oldPath.containsVertex(triple.headId) && oldPath.containsVertex(triple.tailId)
                && graphOperation == Operation.REMOVE) {
            metricResult = (DiameterMetricResult) applyUpdatable(graph);
        }
        return metricResult;

    }

}
