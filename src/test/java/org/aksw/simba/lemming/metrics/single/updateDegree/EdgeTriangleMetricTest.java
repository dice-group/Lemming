package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.AddEdgeDecorator;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.RemoveEdgeDecorator;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.junit.Assert;
import org.junit.Test;

public class EdgeTriangleMetricTest extends UpdateMetricTest {

    @Test
    public void testGraph1() {
        EdgeTriangleMetric metric = new EdgeTriangleMetric();
        ColouredGraph graph = buildGraph1();

        double result = metric.apply(graph);
        Assert.assertEquals(2.0, result);

        UpdatableMetricResult prevResult = new SingleValueMetricResult(metric.getName(), result);
        RemoveEdgeDecorator remDecorator = new RemoveEdgeDecorator(graph);
        // delete an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        remDecorator.setTriple(triple);
        UpdatableMetricResult newResult = metric.update(remDecorator, triple, Operation.REMOVE, prevResult);
        graph = removeEdge(graph, 0);
        Assert.assertEquals(0.0, newResult.getResult());
        prevResult = newResult;

        // add an edge 0 = (0, 1)
        AddEdgeDecorator addDecorator = new AddEdgeDecorator(graph);
        addDecorator.setTriple(triple);
        newResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        Assert.assertEquals(2.0, newResult.getResult());
        graph = addEdge(graph, 0, 1);
        prevResult = newResult;

        // add an edge 4 = (0, 1);
        triple = new TripleBaseSingleID(0, null, 1, null, 4, null);
        addDecorator = new AddEdgeDecorator(graph);
        addDecorator.setTriple(triple);
        newResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        graph = addEdge(graph, 0, 1);
        Assert.assertEquals(4.0, newResult.getResult());
        prevResult = newResult;

        // add an edge 5 = (1, 1);
        addDecorator = new AddEdgeDecorator(graph);
        addDecorator.setTriple(triple);
        newResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        graph = addEdge(graph, 1, 1);
        Assert.assertEquals(6.0, newResult.getResult());
    }

    @Test
    public void testGraph2() {
        EdgeTriangleMetric metric = new EdgeTriangleMetric();
        ColouredGraph graph = buildGraph2();

        double result = metric.apply(graph);
        Assert.assertEquals(0.0, result);

        UpdatableMetricResult prevResult = new SingleValueMetricResult(metric.getName(), result);

        // delete an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        RemoveEdgeDecorator remDecorator = new RemoveEdgeDecorator(graph);
        remDecorator.setTriple(triple);
        UpdatableMetricResult newResult = metric.update(remDecorator, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());
        graph = removeEdge(graph, 0);

        // add an edge 0 = (1, 1);
        triple = new TripleBaseSingleID(1, null, 1, null, 0, null);
        AddEdgeDecorator addDecorator = new AddEdgeDecorator(graph);
        addDecorator.setTriple(triple);
        newResult = metric.update(addDecorator, triple, Operation.ADD, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());
    }
}
