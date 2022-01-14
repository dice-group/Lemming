package org.aksw.simba.lemming.metrics.single.updateDegree;

import grph.Grph;
import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
        addRemoveSameEdge(metric, graph, 0, 1, 0, Grph.DIRECTION.in);
        metric = new StdDevVertexDegree(Grph.DIRECTION.out);
        // Remove and Add edge between v2 and v3
        addRemoveSameEdge(metric, graph, 1, 2, 2, Grph.DIRECTION.out);
    }

    @Test
    public void testGraph2() {
        StdDevVertexDegree metric = new StdDevVertexDegree(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph2();
        // Remove and Add edge between v1 and v2
        addRemoveSameEdge(metric, graph, 0, 1, 1, Grph.DIRECTION.in);
        metric = new StdDevVertexDegree(Grph.DIRECTION.out);
        // Remove and Add edge between v1 and v1
        addRemoveSameEdge(metric, graph, 0, 0, 0, Grph.DIRECTION.out);
    }

    private double calculateAvg(IntArrayList degrees) {
        double sum = 0;
        for (int i = 0; i < degrees.size(); ++i) {
            sum += degrees.getInt(i);
        }
        return sum / degrees.size();
    }

    private double calculateStdDev(ColouredGraph graph, DIRECTION dir) {
        IntArrayList degrees = dir == DIRECTION.in ? graph.getGraph().getAllInEdgeDegrees()
                : graph.getGraph().getAllOutEdgeDegrees();
        double avg = calculateAvg(degrees);
        double temp, sum = 0;
        for (int i = 0; i < degrees.size(); ++i) {
            temp = avg - degrees.getInt(i);
            temp *= temp;
            sum += temp;
        }
        return Math.sqrt(sum / degrees.size());
    }

    private double fixTo8DecimalPlaces(double number) {
        return Math.round(number * 10000000) / 10000000.0d;
    }

    void addRemoveSameEdge(StdDevVertexDegree metric, ColouredGraph graph, int tailId, int headId, int edgeId,
            DIRECTION dir) {
        // check applyUpdatable method
        ColouredGraphDecorator iColouredGraph = new ColouredGraphDecorator(graph);
        UpdatableMetricResult prevResult = metric.applyUpdatable(iColouredGraph);
        Assert.assertEquals(fixTo8DecimalPlaces(calculateStdDev(graph, dir)), fixTo8DecimalPlaces(prevResult.getResult()));

        // delete an edge
        ColouredGraphDecorator removeDecorator = new RemoveEdgeDecorator(graph, true);
        TripleBaseSingleID triple = new TripleBaseSingleID(tailId, null, headId, null, edgeId, null);
        removeDecorator.setTriple(triple);
        prevResult = metric.update(removeDecorator, triple, Operation.REMOVE, prevResult);
        graph = removeEdge(graph, triple.edgeId);
        Assert.assertEquals(fixTo8DecimalPlaces(calculateStdDev(graph, dir)), fixTo8DecimalPlaces(prevResult.getResult()));

        // compute metric before adding the same edge
        ColouredGraphDecorator addDecorator = new AddEdgeDecorator(graph, true);
        addDecorator.setTriple(triple);
        prevResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        graph = addEdge(graph, tailId, headId);
        Assert.assertEquals(fixTo8DecimalPlaces(calculateStdDev(graph, dir)), fixTo8DecimalPlaces(prevResult.getResult()));

    }

}
