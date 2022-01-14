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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodeTriangleMetric extends AbstractMetric implements SingleValueMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeTriangleMetric.class);

    public NodeTriangleMetric() {
        super("#nodetriangles");
    }

    @Override
    public double apply(ColouredGraph graph) {
        return applyUpdatable(new ColouredGraphDecorator(graph)).getResult();
    }

    /**
     * The method is used to initialize the node triangle metric with the given
     * graph.
     * 
     * @param graph the given graph
     * @return number of node triangles
     */
    @Override
    public UpdatableMetricResult applyUpdatable(ColouredGraphDecorator graph) {
        NodeTriangleMetricSelection selector = new NodeTriangleMetricSelection();
        SingleValueMetric nodeTriangleMetric = selector.getMinComplexityMetric(graph);

        double triangleMetric = nodeTriangleMetric.apply((ColouredGraph) graph.getGraph());
        return new SingleValueMetricResult(getName(), triangleMetric);
    }

    /**
     * @param graph the given graph is already modified!
     */
    @Override
    public UpdatableMetricResult update(@Nonnull ColouredGraphDecorator graph, @Nonnull TripleBaseSingleID triple,
            @Nonnull Operation opt, @Nullable UpdatableMetricResult previousResult) {

        if (previousResult == null) {
            return applyUpdatable(graph);
        }
        int headId = triple.headId;
        int tailId = triple.tailId;

        // if headId = tailId, result is not change.
        if (headId == tailId) {
            return previousResult;
        }

        int numEdgesBetweenVertices = graph.getNumberOfEdgesBetweenVertices();

        int numberOfCommon = MetricUtils.getVerticesInCommon(graph, headId, tailId).size();

        // the previous result could be maintained except for 2 cases:
        double newResult = previousResult.getResult();

        // 1.case: remove an edge, and number of edges between head and tail is 1
        // -> new metric = old metric - number of common vertices
        if (numEdgesBetweenVertices == 0 && opt == Operation.REMOVE) {
            newResult = newResult - numberOfCommon;

            // 2.case: add an edge, and number of edges between head and tail is 0
            // -> new metric = old metric + number of common vertices
        } else if (numEdgesBetweenVertices == 1 && opt == Operation.ADD) {
            newResult = newResult + numberOfCommon;
        }
        if (newResult < 0) {
            LOGGER.error("The new result of node triangle metric is negative : " + newResult);
            newResult = 0;
        }

        return new SingleValueMetricResult(previousResult.getMetricName(), newResult);
    }
}
