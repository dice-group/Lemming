package org.aksw.simba.lemming.creation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GraphCreatorTest {

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        testConfigs.add(new Object[] { "graph1.n3", 5, 5 });
        testConfigs.add(new Object[] { "graph_loop.n3", 3, 5 });
        testConfigs.add(new Object[] { "email-Eu-core.n3", 1005, 25571 });

        return testConfigs;
    }

    private String graphFile;
    private int expectedVertices;
    private int expectedEdges;

    public GraphCreatorTest(String graphFile, int expectedVertices, int expectedEdges) {
        super();
        this.graphFile = graphFile;
        this.expectedVertices = expectedVertices;
        this.expectedEdges = expectedEdges;
    }

    @Test
    public void test() {
        ColouredGraph graph = IOHelper.readGraphFromResource(this.getClass().getClassLoader(), graphFile, "N3");
        Assert.assertNotNull(graph);
        Assert.assertEquals(expectedVertices, graph.getVertices().size());
        Assert.assertEquals(expectedVertices, graph.getVertexColours().size());
        Assert.assertEquals(expectedEdges, graph.getEdgeColours().size());
    }

}
