package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
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

        //delete an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());

        //delete an edge 2 = (1, 2)
        triple = new TripleBaseSingleID(1, null, 2, null, 2, null);
        newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(1.0, newResult.getResult());

        //add an edge 4 = (0, 1);
        graph = addEdge(graph, 0, 1);
        triple = new TripleBaseSingleID(0, null, 1, null, 4, null);
        newResult = metric.update(graph, triple, Operation.ADD, prevResult);
        graph = removeEdge(graph, 4);
        Assert.assertEquals(1.0, newResult.getResult());

        //add an edge 4 = (1, 1);
        graph = addEdge(graph, 1, 1);
        triple = new TripleBaseSingleID(1, null, 1, null, 4, null);
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

        //delete an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());

        //add an edge 4 = (1, 1);
        graph = addEdge(graph, 1, 1);
        triple = new TripleBaseSingleID(1, null, 1, null, 3, null);
        newResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());
    }
}
