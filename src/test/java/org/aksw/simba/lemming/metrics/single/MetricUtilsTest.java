package org.aksw.simba.lemming.metrics.single;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import junit.framework.Assert;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphDecorator;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.junit.Test;

public class MetricUtilsTest {

    @Test
    public void testGetVerticesCommon() {
        ColouredGraphDecorator decoratedGraph = new ColouredGraphDecorator(buildGraph());
        IntSet commonVertices1 = MetricUtils.getVerticesInCommon(decoratedGraph, 0, 2);
        IntSet expectedSet1 = createExpectedIntSet(1, 3);
        assertEqualIntSet(expectedSet1, commonVertices1);

        IntSet commonVertices2 = MetricUtils.getVerticesInCommon(decoratedGraph, 1, 3);
        IntSet expectedSet2 = createExpectedIntSet(0, 2);
        assertEqualIntSet(expectedSet2, commonVertices2);

        IntSet commonVertices3 = MetricUtils.getVerticesInCommon(decoratedGraph, 4, 5);
        IntSet expectedEmptySet = createExpectedIntSet();
        assertEqualIntSet(expectedEmptySet, commonVertices3);

        IntSet commonVertices4 = MetricUtils.getVerticesInCommon(decoratedGraph, 0, 1);
        assertEqualIntSet(expectedEmptySet, commonVertices4);
    }

    public ColouredGraph buildGraph() {
        Grph graph = new InMemoryGrph();
        int v0 = graph.addVertex(); // vertexID: 0
        int v1 = graph.addVertex(); // vertexID: 1
        int v2 = graph.addVertex(); // vertexID: 2
        int v3 = graph.addVertex(); // vertexID: 3
        int v4 = graph.addVertex(); // vertexID: 4
        int v5 = graph.addVertex(); // vertexID: 5
        graph.addDirectedSimpleEdge(v0, v1);
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v2, v3);
        graph.addDirectedSimpleEdge(v3, v0);
        graph.addDirectedSimpleEdge(v0, v4);
        graph.addDirectedSimpleEdge(v5, v2);
        return new ColouredGraph(graph, null, null);
    }

    private void assertEqualIntSet(IntSet expected, IntSet actual) {
        Assert.assertTrue(expected.size() == actual.size());
        for (int element : expected) {
            Assert.assertTrue(actual.contains(element));
        }
    }

    private IntSet createExpectedIntSet(int... elements) {
        IntSet set = new IntArraySet();
        for (int element : elements) {
            set.add(element);
        }
        return set;
    }
}
