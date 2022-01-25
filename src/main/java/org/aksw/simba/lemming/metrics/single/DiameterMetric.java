package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.IColouredGraph;
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
    public double apply(IColouredGraph graph) {
        try {
            return graph.getDiameter();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    @Override
    public UpdatableMetricResult applyUpdatable(IColouredGraph graph) {
        DiameterMetricResult metricResult = new DiameterMetricResult(getName(), Double.NaN);
        metricResult.setResult(graph.getDiameter());
        metricResult.setDiameterPath(graph.getDiameterPath());
        return metricResult;
    }

    @Override
    public UpdatableMetricResult update(IColouredGraph graph, TripleBaseSingleID triple, Operation graphOperation,
            UpdatableMetricResult previousResult) {
        if (previousResult == null) {
            return applyUpdatable(graph);
        }
        DiameterMetricResult metricResult = ((DiameterMetricResult) previousResult);
        ArrayListPath oldPath = metricResult.getDiameterPath();
        if (graphOperation == Operation.ADD) {
            int newPathLength = graph.computeShorterDiameter(triple, oldPath);
            if (newPathLength < oldPath.getLength()) {
                // The diameter length has been reduced
                metricResult = (DiameterMetricResult) applyUpdatable(graph);
            }
        } else if (oldPath.containsVertex(triple.headId) && oldPath.containsVertex(triple.tailId)
                && graphOperation == Operation.REMOVE) {
            metricResult = (DiameterMetricResult) applyUpdatable(graph);
        }
        return metricResult;

    }

}
