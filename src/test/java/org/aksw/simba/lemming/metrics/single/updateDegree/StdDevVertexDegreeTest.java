package org.aksw.simba.lemming.metrics.single.updateDegree;

import grph.Grph;
import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);

        Assert.assertEquals(fixTo8DecimalPlaces(calculateStdDev(graph, dir)),
                fixTo8DecimalPlaces(prevResult.getResult()));

        // compute metric before deleting an edge
        TripleBaseSingleID triple = new TripleBaseSingleID(tailId, null, headId, null, edgeId, null);
        graph = removeEdge(graph, triple.edgeId);
        prevResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(fixTo8DecimalPlaces(calculateStdDev(graph, dir)),
                fixTo8DecimalPlaces(prevResult.getResult()));

        // compute metric before adding the same edge
        graph = addEdge(graph, tailId, headId);
        prevResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(fixTo8DecimalPlaces(calculateStdDev(graph, dir)),
                fixTo8DecimalPlaces(prevResult.getResult()));

    }

}
