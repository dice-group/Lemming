package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.junit.Assert;
import org.junit.Test;

public class NodeTriangleMetricTest extends UpdateMetricTest{

    @Test
    public void testGraph1(){
        NodeTriangleMetric metric = new NodeTriangleMetric();
        ColouredGraph graph = buildGraph1();
        double result = metric.apply(graph);
        Assert.assertEquals(1.0, result);

        UpdatableMetricResult prevResult = new SingleValueMetricResult(metric.getName(), result);

        //try to remove an edge 0 = (0, 1)
        graph = removeEdge(graph, 0);
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());
        prevResult = newResult;

        //try to add an edge 0 = (0, 1)
        graph = addEdge(graph, 0, 1);
        newResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(1.0, newResult.getResult());
        prevResult = newResult;

        //try to remove an edge 2 = (1, 2)
        graph = removeEdge(graph, 2);
        triple = new TripleBaseSingleID(1, null, 2, null, 2, null);
        newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(1.0, newResult.getResult());
        prevResult = newResult;

        //add an edge 2 = (1, 2);
        graph = addEdge(graph, 1, 2);
        newResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(1.0, newResult.getResult());
    }

    @Test
    public void testGraph2(){
        NodeTriangleMetric metric = new NodeTriangleMetric();
        ColouredGraph graph = buildGraph2();

        double result = metric.apply(graph);
        Assert.assertEquals(0.0, result);

        UpdatableMetricResult prevResult = new SingleValueMetricResult(metric.getName(), result);

        //try to remove an edge 1 = (0, 1)
        graph = removeEdge(graph, 1);
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 1, null);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());
        prevResult = newResult;

        //try to add an edge 1 = (1, 1);
        graph = addEdge(graph, 1, 1);
        triple = new TripleBaseSingleID(1, null, 1, null, 1, null);
        newResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());
    }

}
