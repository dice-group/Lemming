package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.AddEdgeDecorator;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.RemoveEdgeDecorator;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.junit.Assert;
import org.junit.Test;

public class NodeTriangleMetricTest extends UpdateMetricTest {

    @Test
    public void testGraph1() {
        NodeTriangleMetric metric = new NodeTriangleMetric();
        ColouredGraph graph = buildGraph1();
        double result = metric.apply(graph);
        Assert.assertEquals(1.0, result);

        UpdatableMetricResult prevResult = new SingleValueMetricResult(metric.getName(), result);
        // try to remove an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        RemoveEdgeDecorator removeDecorator = new RemoveEdgeDecorator(graph);
        removeDecorator.setTriple(triple);
        UpdatableMetricResult newResult = metric.update(removeDecorator, triple, Operation.REMOVE, prevResult);
        graph = removeEdge(graph, 0);
        Assert.assertEquals(0.0, newResult.getResult());
        prevResult = newResult;

        AddEdgeDecorator addDecorator = new AddEdgeDecorator(graph);
        // try to add an edge 0 = (0, 1)
        addDecorator.setTriple(triple);
        newResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        Assert.assertEquals(1.0, newResult.getResult());
        graph = addEdge(graph, 0, 1);
        prevResult = newResult;

        // delete an edge 2 = (1, 2)
        removeDecorator = new RemoveEdgeDecorator(graph);
        triple = new TripleBaseSingleID(1, null, 2, null, 2, null);
        removeDecorator.setTriple(triple);
        newResult = metric.update(removeDecorator, triple, Operation.REMOVE, prevResult);
        graph = removeEdge(graph, 2);
        Assert.assertEquals(1.0, newResult.getResult());
        prevResult = newResult;

        // add an edge 2 = (1, 2);
        addDecorator = new AddEdgeDecorator(graph);
        addDecorator.setTriple(triple);
        newResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        Assert.assertEquals(1.0, newResult.getResult());

    }

    @Test
    public void testGraph2() {
        NodeTriangleMetric metric = new NodeTriangleMetric();
        ColouredGraph graph = buildGraph2();

        double result = metric.apply(graph);
        Assert.assertEquals(0.0, result);

        UpdatableMetricResult prevResult = new SingleValueMetricResult(metric.getName(), result);

        // try to remove an edge 1 = (0, 1)
        RemoveEdgeDecorator removeDecorator = new RemoveEdgeDecorator(graph);
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 1, null);
        removeDecorator.setTriple(triple);
        UpdatableMetricResult newResult = metric.update(removeDecorator, triple, Operation.REMOVE, prevResult);
        graph = removeEdge(graph, 1);
        Assert.assertEquals(0.0, newResult.getResult());
        prevResult = newResult;

        // add an edge 4 = (1, 1);
        AddEdgeDecorator addDecorator = new AddEdgeDecorator(graph);
        addDecorator.setTriple(triple);
        triple = new TripleBaseSingleID(1, null, 1, null, 1, null);
        newResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        graph = addEdge(graph, 1, 1);
        Assert.assertEquals(0.0, newResult.getResult());
    }

}
