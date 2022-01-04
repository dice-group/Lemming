package org.aksw.simba.lemming.creation;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This test checks whether the requested head and tail IDs for edges make
 * sense. Related to issue #31 (https://github.com/dice-group/Lemming/issues/31).
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class EdgeHeadTailSelectionTest {

    private static final String GRAPH_FILE1 = "head_tail_edge_selection.n3";

    @Test
    public void testcase1() throws IOException {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE1);) {
            model.read(is, null, "N3");
        }

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);

        int numberOfVertices = graph.getVertices().size();
        IntSet edges;
        for (int v = 0; v < numberOfVertices; ++v) {
            edges = graph.getOutEdges(v);
            for (int edge : edges) {
                Assert.assertEquals(
                        "Found an outgoing edge of " + v
                                + " that has a tail with a different ID. That shouldn't happen!",
                        v, graph.getTailOfTheEdge(edge));
            }
            edges = graph.getInEdges(v);
            for (int edge : edges) {
                Assert.assertEquals(
                        "Found an incoming edge of " + v
                                + " that has a head with a different ID. That shouldn't happen!",
                        v, graph.getHeadOfTheEdge(edge));
            }
        }
    }
}
