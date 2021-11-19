package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.junit.Assert;
import org.junit.Test;

public class EdgeTriangleMetricTest extends UpdateMetricTest{

    @Test
    public void testGraph1(){
        EdgeTriangleMetric metric = new EdgeTriangleMetric();
        ColouredGraph graph = buildGraph1();

        double result = metric.apply(graph);
        Assert.assertEquals(2.0, result);

        UpdatableMetricResult prevResult = new SingleValueMetricResult(metric.getName(), result);

        //try to remove an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());
        graph = removeEdge(graph, 0);
        prevResult = newResult;

        //try to add an edge 0 = (0, 1)
        newResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(2.0, newResult.getResult());
        graph = addEdge(graph, 0, 1);
        prevResult = newResult;

        //try to add an edge 4 = (0, 1);
        triple = new TripleBaseSingleID(0, null, 1, null, 4, null);
        newResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(4.0, newResult.getResult());
        graph = addEdge(graph, 0, 1);
        prevResult = newResult;

        //try to remove an edge 4 = (0, 1);
        newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(2.0, newResult.getResult());
    }

    @Test
    public void testGraph2(){
        EdgeTriangleMetric metric = new EdgeTriangleMetric();
        ColouredGraph graph = buildGraph2();

        double result = metric.apply(graph);
        Assert.assertEquals(0.0, result);

        UpdatableMetricResult prevResult = new SingleValueMetricResult(metric.getName(), result);

        //try an edge 0 = (0, 1)
        TripleBaseSingleID triple = new TripleBaseSingleID(0, null, 1, null, 0, null);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());

        //add an edge 0 = (1, 1);
        triple = new TripleBaseSingleID(1, null, 1, null, 0, null);
        newResult = metric.update(graph, triple, Operation.ADD, prevResult);
        Assert.assertEquals(0.0, newResult.getResult());
    }
}
