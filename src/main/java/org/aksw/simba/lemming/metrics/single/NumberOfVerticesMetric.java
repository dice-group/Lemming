package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

/**
 * This metric is the number of vertices of the graph.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class NumberOfVerticesMetric extends AbstractMetric implements SingleValueMetric {

    public NumberOfVerticesMetric() {
        super("#vertices");
    }

    @Override
    public double apply(IColouredGraph graph) {
        return applyUpdatable(graph).getResult();
    }

    @Override
    public UpdatableMetricResult applyUpdatable(IColouredGraph iColouredGraph) {
        SingleValueMetricResult result = new SingleValueMetricResult(getName(), iColouredGraph.getNumberOfVertices());
        return result;
    }

    @Override
    public UpdatableMetricResult update(IColouredGraph iColouredGraph, TripleBaseSingleID triple,
            Operation graphOperation, UpdatableMetricResult previousResult) {
        ((SingleValueMetricResult) previousResult).setResult(iColouredGraph.getNumberOfVertices());
        return previousResult;
    }

}
