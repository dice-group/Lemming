/**
 * 
 */
package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.AddEdgeDecorator;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphDecorator;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
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
        return new ColouredGraph(graph, null, null);
    }

    @Test
    public void testLinearGraph() {
        DiameterMetric metric = new DiameterMetric();
        ColouredGraph graph = buildLinearGraph();
        UpdatableMetricResult result = metric.applyUpdatable(new ColouredGraphDecorator(graph));
        Assert.assertEquals(numberOfNodes - 1, (int) result.getResult());

        // Add an Edge from middle node to the end node
        int headId = 5;
        int tailId = 0;
        TripleBaseSingleID triple = new TripleBaseSingleID(tailId, null, headId, null, numberOfNodes - 1, null);
        ColouredGraphDecorator graphDecorator = new AddEdgeDecorator(graph, true);
        graphDecorator.setTriple(triple);
        result = metric.update(graphDecorator, triple, Operation.ADD, result);
        graph.addEdge(tailId, headId);
        Assert.assertEquals(numberOfNodes - headId, (int) result.getResult());
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

}
