package org.aksw.simba.lemming.metrics.single.nodetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphDecorator;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.metricselection.NodeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.IntSetUtil;

import javax.annotation.Nonnull;

public class NodeTriangleMetric extends AbstractMetric implements SingleValueMetric {

    public NodeTriangleMetric() {
        super("#nodetriangles");
    }

    @Override
    public double apply(ColouredGraph graph) {

        NodeTriangleMetricSelection selector = new NodeTriangleMetricSelection();
        SingleValueMetric nodeTriangleMetric = selector.getMinComplexityMetric(graph);

        return nodeTriangleMetric.apply(graph);
    }

    /**
     * @param graph the given graph is not modified!
     */
    @Override
    public UpdatableMetricResult update(@Nonnull ColouredGraphDecorator graph, @Nonnull TripleBaseSingleID triple,
            @Nonnull Operation opt, @Nonnull UpdatableMetricResult previousResult) {

        int headId = triple.headId;
        int tailId = triple.tailId;

        // if headId = tailId, result is not change.
        if (headId == tailId) {
            return previousResult;
        }

        int numEdgesBetweenVertices = IntSetUtil
                .intersection(graph.getEdgesIncidentTo(tailId), graph.getEdgesIncidentTo(headId)).size();

        int numberOfCommon = MetricUtils.getVerticesInCommon(graph, headId, tailId).size();

        // the previous result could be maintained except for 2 cases:
        double newResult = previousResult.getResult();

        // 1.case: remove an edge, and number of edges between head and tail is 1
        // -> new metric = old metric - number of common vertices
        if (numEdgesBetweenVertices == 1 && opt == Operation.REMOVE) {
            newResult = newResult - numberOfCommon;

            // 2.case: add an edge, and number of edges between head and tail is 0
            // -> new metric = old metric + number of common vertices
        } else if (numEdgesBetweenVertices == 0 && opt == Operation.ADD) {
            newResult = newResult + numberOfCommon;
        }

        newResult = newResult >= 0 ? newResult : 0;

        return new SingleValueMetricResult(previousResult.getMetricName(), newResult);
    }
}
