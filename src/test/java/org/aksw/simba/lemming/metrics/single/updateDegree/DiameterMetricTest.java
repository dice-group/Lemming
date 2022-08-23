/**
 * 
 */
package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.AddEdgeDecorator;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphDecorator;
import org.aksw.simba.lemming.RemoveEdgeDecorator;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
import org.aksw.simba.lemming.metrics.single.DiameterMetricResult;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.junit.Test;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import junit.framework.Assert;

/**
 * @author Pranav
 *
 */
public class DiameterMetricTest {

    private int numberOfNodes = 10;

    /**
     * Build a graph where each node has 2 neighbors except the first and last node
     * 
     * @param nodes
     * @return
     */
    private ColouredGraph buildLinearGraph() {
        Grph graph = new InMemoryGrph();
        int[] nodeList = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            nodeList[i] = graph.addVertex();
            if (i > 0) {
                graph.addDirectedSimpleEdge(nodeList[i - 1], nodeList[i]);
            }
        }
        return new ColouredGraph(graph, null, null);
    }

    /**
     * Build a bidirectional full connected graph
     * 
     * @param nodes
     * @return
     */
    private ColouredGraph buildFullConnectedGraph() {
        Grph graph = new InMemoryGrph();
        int[] nodeList = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            nodeList[i] = graph.addVertex();
        }
        for (int i = 0; i < numberOfNodes; i++) {
            for (int j = 0; j < numberOfNodes; j++) {
                graph.addDirectedSimpleEdge(nodeList[i], nodeList[j]);
            }
        }
        return new ColouredGraph(graph, null, null);
    }

    /**
     * Build a graph with all nodes connected to a single node
     * 
     * @param nodes
     * @return
     */
    private ColouredGraph buildTree() {
        Grph graph = new InMemoryGrph();
        int[] nodeList = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            nodeList[i] = graph.addVertex();
            if (i > 0) {
                graph.addDirectedSimpleEdge(nodeList[0], nodeList[i]);
            }
        }
        return new ColouredGraph(graph, null, null);
    }

    private ColouredGraph buildDisconnectedGraph() {
        Grph graph = new InMemoryGrph();
        int[] nodeList = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            nodeList[i] = graph.addVertex();
        }
        // Add Random Edges
        graph.addDirectedSimpleEdge(numberOfNodes / numberOfNodes, 2 * numberOfNodes % (numberOfNodes - 1));
        graph.addDirectedSimpleEdge(numberOfNodes / numberOfNodes, 3 * numberOfNodes % (numberOfNodes - 1));
        return new ColouredGraph(graph, null, null);
    }

    @Test
    public void testLinearGraph() {
        DiameterMetric metric = new DiameterMetric();
        ColouredGraph graph = buildLinearGraph();
        UpdatableMetricResult result = metric.applyUpdatable(new ColouredGraphDecorator(graph));
        Assert.assertEquals(numberOfNodes - 1, (int) result.getResult());
    }

    @Test
    public void testFullConnectedGraph() {
        DiameterMetric metric = new DiameterMetric();
        ColouredGraph graph = buildFullConnectedGraph();
        Assert.assertEquals(1, (int) metric.apply(graph));
    }

    @Test
    public void testTreeGraph() {
        DiameterMetric metric = new DiameterMetric();
        ColouredGraph graph = buildTree();
        Assert.assertEquals(1, (int) metric.apply(graph));
    }

    @Test
    public void testDisconnectedGraph() {
        DiameterMetric metric = new DiameterMetric();
        ColouredGraph graph = buildDisconnectedGraph();
        Assert.assertEquals(0, (int) metric.apply(graph));
    }

    @Test
    public void simulateGraphOptimizationPhase() {
        // Build graph
        Grph graph = new InMemoryGrph();
        int numberOfNodes = 5;
        int[] nodeList = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            nodeList[i] = graph.addVertex();
        }
        graph.addDirectedSimpleEdge(0, 1);
        graph.addDirectedSimpleEdge(0, 2);
        graph.addDirectedSimpleEdge(1, 2);
        graph.addDirectedSimpleEdge(2, 3);
        graph.addDirectedSimpleEdge(2, 4);
        graph.addDirectedSimpleEdge(3, 4);
        // Nodes: 0,1,2,3,4
        // Diameters: 0-2-3, 0-2-4, 1-2-3, 1-2-4

        DiameterMetric metric = new DiameterMetric();
        ColouredGraph colouredGraph = new ColouredGraph(graph, null, null);
        ColouredGraphDecorator graphDec = new ColouredGraphDecorator(colouredGraph);
        AddEdgeDecorator addDec = new AddEdgeDecorator(colouredGraph, true);
        RemoveEdgeDecorator remDec = new RemoveEdgeDecorator(colouredGraph, false);

        DiameterMetricResult resultObj = (DiameterMetricResult) metric.applyUpdatable(graphDec);
        Assert.assertEquals(2.0, resultObj.getResult());

        // Addition of an edge that does not affect the diameter
        TripleBaseSingleID triple = new TripleBaseSingleID(2, null, 3, null, numberOfNodes + 1, null);
        addDec.setTriple(triple);
        resultObj = (DiameterMetricResult) metric.update(addDec, triple, Operation.ADD, resultObj);
        Assert.assertEquals(2.0, resultObj.getResult());

        // Addition of an edge that shortens the diameter
        triple = new TripleBaseSingleID(0, null, 4, null, numberOfNodes + 2, null);
        addDec.setTriple(triple);
        resultObj = (DiameterMetricResult) metric.update(addDec, triple, Operation.ADD, resultObj);
        // Result is still 2 because a other paths exist
        Assert.assertEquals(2.0, resultObj.getResult());

        // Reset the result object back to the initial state
        resultObj = (DiameterMetricResult) metric.applyUpdatable(graphDec);

        // Removal of an edge that does not affect the diameter
        triple = new TripleBaseSingleID(1, null, 2, null, 2, null);
        remDec.setTriple(triple);
        resultObj = (DiameterMetricResult) metric.update(remDec, triple, Operation.REMOVE, resultObj);
        Assert.assertEquals(2.0, resultObj.getResult());

        // Removal of an edge that changes the diameter
        triple = new TripleBaseSingleID(0, null, 2, null, 1, null);
        remDec.setTriple(triple);
        resultObj = (DiameterMetricResult) metric.update(remDec, triple, Operation.REMOVE, resultObj);
        Assert.assertEquals(3.0, resultObj.getResult());
    }

}
