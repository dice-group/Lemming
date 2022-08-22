package org.aksw.simba.lemming.metrics.single.updateDegree;

import grph.Grph;
import junit.framework.Assert;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.junit.Test;

public class StdDevVertexDegreeTest extends UpdateMetricTest {

    @Test
    public void testGraph1() {
        StdDevVertexDegree metric = new StdDevVertexDegree(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph1();
        // Remove and Add edge between v1 and v2
        addRemoveSameEdge(metric, graph, 0, 1, 0);
        metric = new StdDevVertexDegree(Grph.DIRECTION.out);
        // Remove and Add edge between v2 and v3
        addRemoveSameEdge(metric, graph, 1, 2, 2);
    }

    @Test
    public void testGraph2() {
        StdDevVertexDegree metric = new StdDevVertexDegree(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph2();
        // Remove and Add edge between v1 and v2
        addRemoveSameEdge(metric, graph, 0, 1, 1);
        metric = new StdDevVertexDegree(Grph.DIRECTION.out);
        // Remove and Add edge between v1 and v1
        addRemoveSameEdge(metric, graph, 0, 0, 0);
    }

    void addRemoveSameEdge(StdDevVertexDegree metric, ColouredGraph graph, int tailId, int headId, int edgeId) {
        // check applyUpdatable method
        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);
        Assert.assertEquals(Math.round(metric.apply(graph)), Math.round(prevResult.getResult()));
        // delete an edge
        TripleBaseSingleID triple = new TripleBaseSingleID(tailId, null, headId, null, edgeId, null);
        graph = removeEdge(graph, triple.edgeId);
        prevResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(Math.round(metric.apply(graph)), Math.round(prevResult.getResult()));

        // add same edge
        graph = addEdge(graph, tailId, headId);
        prevResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(Math.round(metric.apply(graph)), Math.round(prevResult.getResult()));

    }

}
