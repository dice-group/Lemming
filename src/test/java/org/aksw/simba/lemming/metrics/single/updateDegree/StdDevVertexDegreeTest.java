package org.aksw.simba.lemming.metrics.single.updateDegree;

import grph.Grph;
import junit.framework.Assert;

import org.aksw.simba.lemming.AddEdgeDecorator;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphDecorator;
import org.aksw.simba.lemming.RemoveEdgeDecorator;
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
        // Remove and Add edge between v1 and v2
        addRemoveSameEdge(metric, graph, 0, 1, 2);
    }

    void addRemoveSameEdge(StdDevVertexDegree metric, ColouredGraph graph, int tailId, int headId, int edgeId) {
        // check applyUpdatable method
        ColouredGraphDecorator iColouredGraph = new ColouredGraphDecorator(graph);
        UpdatableMetricResult prevResult = metric.applyUpdatable(iColouredGraph);
        Assert.assertEquals(fixTo6DecimalPlaces(metric.apply(graph)), fixTo6DecimalPlaces(prevResult.getResult()));

        // delete an edge
        ColouredGraphDecorator removeDecorator = new RemoveEdgeDecorator(graph, true);
        TripleBaseSingleID triple = new TripleBaseSingleID(tailId, null, headId, null, edgeId, null);
        removeDecorator.setTriple(triple);
        prevResult = metric.update(removeDecorator, triple, Operation.REMOVE, prevResult);
        graph = removeEdge(graph, triple.edgeId);
        Assert.assertEquals(fixTo6DecimalPlaces(metric.apply(graph)), fixTo6DecimalPlaces(prevResult.getResult()));

        // add same edge
        ColouredGraphDecorator addDecorator = new AddEdgeDecorator(graph, true);
        addDecorator.setTriple(triple);
        prevResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        graph = addEdge(graph, tailId, headId);
        Assert.assertEquals(fixTo6DecimalPlaces(metric.apply(graph)), fixTo6DecimalPlaces(prevResult.getResult()));

    }

    private double fixTo6DecimalPlaces(double number) {
        return Math.round(number * 100000) / 100000.0d;
    }

}
