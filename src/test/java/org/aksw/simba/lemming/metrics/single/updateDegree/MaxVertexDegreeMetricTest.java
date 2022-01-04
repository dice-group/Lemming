package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.AddEdgeDecorator;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphDecorator;
import org.aksw.simba.lemming.RemoveEdgeDecorator;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.single.updateDegree.UpdateMetricTest;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.junit.Test;

import grph.Grph.DIRECTION;
import junit.framework.Assert;

public class MaxVertexDegreeMetricTest extends UpdateMetricTest {

    @Test
    public void testcase1() {
        // Test case for in-degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        ColouredGraph buildGraph1 = buildGraph1();

        UpdatableMetricResult maxMetricResultObj = metric.applyUpdatable(new ColouredGraphDecorator(buildGraph1));
        Assert.assertEquals(3.0, maxMetricResultObj.getResult());

        // UpdatableMetricResult prevResult = new
        // SingleValueMetricResult(metric.getName(), result);

        // delete an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        ColouredGraphDecorator rDec = new RemoveEdgeDecorator(buildGraph1, false);
        rDec.setTriple(triple);
        UpdatableMetricResult newResult = metric.update(rDec, triple, Operation.REMOVE, maxMetricResultObj);
        buildGraph1 = removeEdge(buildGraph1, triple.edgeId);
        Assert.assertEquals(3.0, newResult.getResult());

        // delete an edge 2 = (1, 2)
        triple = new TripleBaseSingleID(1, null, 2, null, 2, null);
        rDec.setGraph(buildGraph1);
        rDec.setTriple(triple);
        newResult = metric.update(rDec, triple, Operation.REMOVE, newResult);
        buildGraph1 = removeEdge(buildGraph1, triple.edgeId);
        Assert.assertEquals(2.0, newResult.getResult());

        // add an edge 4 = (1, 2);
        ColouredGraphDecorator aDec = new AddEdgeDecorator(buildGraph1, true);
        triple = new TripleBaseSingleID(1, null, 2, null, 4, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph1 = addEdge(buildGraph1, 1, 2);
        Assert.assertEquals(3.0, newResult.getResult());

        // add an edge 4 = (2, 2);
        aDec.setGraph(buildGraph1);
        triple = new TripleBaseSingleID(2, null, 2, null, 4, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph1 = addEdge(buildGraph1, 2, 2);
        Assert.assertEquals(4.0, newResult.getResult());

    }

    @Test
    public void testcase2() {
        // test case for out-degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        ColouredGraph buildGraph1 = buildGraph1();

        UpdatableMetricResult maxMetricResultObj = metric.applyUpdatable(new ColouredGraphDecorator(buildGraph1));
        Assert.assertEquals(2.0, maxMetricResultObj.getResult());

        // UpdatableMetricResult prevResult = new
        // SingleValueMetricResult(metric.getName(), result);

        // delete an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        ColouredGraphDecorator rDec = new RemoveEdgeDecorator(buildGraph1, false);
        rDec.setTriple(triple);
        UpdatableMetricResult newResult = metric.update(rDec, triple, Operation.REMOVE, maxMetricResultObj);
        buildGraph1 = removeEdge(buildGraph1, triple.edgeId);
        Assert.assertEquals(2.0, newResult.getResult());

        // delete an edge 2 = (1, 2)
        triple = new TripleBaseSingleID(1, null, 2, null, 2, null);
        rDec.setGraph(buildGraph1);
        rDec.setTriple(triple);
        newResult = metric.update(rDec, triple, Operation.REMOVE, newResult);
        buildGraph1 = removeEdge(buildGraph1, triple.edgeId);
        Assert.assertEquals(1.0, newResult.getResult());

        // add an edge 4 = (1, 2);
        triple = new TripleBaseSingleID(1, null, 2, null, 4, null);
        ColouredGraphDecorator aDec = new AddEdgeDecorator(buildGraph1, true);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph1 = addEdge(buildGraph1, 1, 2);
        Assert.assertEquals(2.0, newResult.getResult());

        // add an edge 4 = (2, 2);
        aDec.setGraph(buildGraph1);
        triple = new TripleBaseSingleID(2, null, 2, null, 4, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph1 = addEdge(buildGraph1, 2, 2);
        Assert.assertEquals(2.0, newResult.getResult());

        // add an edge 4 = (2, 2);
        aDec.setGraph(buildGraph1);
        triple = new TripleBaseSingleID(1, null, 1, null, 4, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph1 = addEdge(buildGraph1, 1, 1);
        Assert.assertEquals(3, 0, newResult.getResult());
    }

    @Test
    public void testcase3() {
        // test case for in-degree with graph 2
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        ColouredGraph buildGraph2 = buildGraph2();

        UpdatableMetricResult maxMetricResultObj = metric.applyUpdatable(new ColouredGraphDecorator(buildGraph2));
        Assert.assertEquals(2.0, maxMetricResultObj.getResult());

        // UpdatableMetricResult prevResult = new
        // SingleValueMetricResult(metric.getName(), result);

        // delete an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 1, null);
        ColouredGraphDecorator rDec = new RemoveEdgeDecorator(buildGraph2, false);
        rDec.setTriple(triple);
        UpdatableMetricResult newResult = metric.update(rDec, triple, Operation.REMOVE, maxMetricResultObj);
        buildGraph2 = removeEdge(buildGraph2, triple.edgeId);
        Assert.assertEquals(1.0, newResult.getResult());

        // add an edge 0 = (0, 1);
        ColouredGraphDecorator aDec = new AddEdgeDecorator(buildGraph2, true);
        triple = new TripleBaseSingleID(0, null, 1, null, 1, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph2 = addEdge(buildGraph2, 0, 1);
        Assert.assertEquals(2.0, newResult.getResult());

        // add an edge 4 = (1, 1);
        aDec.setGraph(buildGraph2);
        triple = new TripleBaseSingleID(1, null, 1, null, 4, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph2 = addEdge(buildGraph2, 1, 1);
        Assert.assertEquals(3.0, newResult.getResult());

    }

    @Test
    public void testcase4() {
        // test case for in-degree with graph 2
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        ColouredGraph buildGraph2 = buildGraph2();

        UpdatableMetricResult maxMetricResultObj = metric.applyUpdatable(new ColouredGraphDecorator(buildGraph2));
        Assert.assertEquals(3.0, maxMetricResultObj.getResult());

        // delete an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 1, null);
        ColouredGraphDecorator rDec = new RemoveEdgeDecorator(buildGraph2, false);
        rDec.setTriple(triple);
        UpdatableMetricResult newResult = metric.update(rDec, triple, Operation.REMOVE, maxMetricResultObj);
        buildGraph2 = removeEdge(buildGraph2, triple.edgeId);
        Assert.assertEquals(2.0, newResult.getResult());

        // add an edge 0 = (0, 1);
        ColouredGraphDecorator aDec = new AddEdgeDecorator(buildGraph2, true);
        triple = new TripleBaseSingleID(0, null, 1, null, 1, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph2 = addEdge(buildGraph2, 0, 1);
        Assert.assertEquals(3.0, newResult.getResult());

        // add an edge 4 = (1, 1);
        aDec.setGraph(buildGraph2);
        triple = new TripleBaseSingleID(1, null, 1, null, 4, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph2 = addEdge(buildGraph2, 1, 1);
        Assert.assertEquals(3.0, newResult.getResult());

        // add an edge 5 = (0, 1);
        aDec.setGraph(buildGraph2);
        triple = new TripleBaseSingleID(0, null, 1, null, 5, null);
        aDec.setTriple(triple);
        newResult = metric.update(aDec, triple, Operation.ADD, newResult);
        buildGraph2 = addEdge(buildGraph2, 0, 1);
        Assert.assertEquals(4.0, newResult.getResult());
    }

}
