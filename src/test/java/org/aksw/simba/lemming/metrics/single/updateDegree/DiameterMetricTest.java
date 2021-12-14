/**
 * 
 */
package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
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

    @Test
    public void testLinearGraph() {
        DiameterMetric metric = new DiameterMetric();
        ColouredGraph graph = buildLinearGraph();
        Assert.assertEquals((int) metric.apply(graph), numberOfNodes - 1);
    }

    @Test
    public void testFullConnectedGraph() {
        DiameterMetric metric = new DiameterMetric();
        ColouredGraph graph = buildFullConnectedGraph();
        Assert.assertEquals((int) metric.apply(graph), 1);
    }

    @Test
    public void testTreeGraph() {
        DiameterMetric metric = new DiameterMetric();
        ColouredGraph graph = buildTree();
        Assert.assertEquals((int) metric.apply(graph), 1);
    }

}
