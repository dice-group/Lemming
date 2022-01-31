package org.aksw.simba.lemming.metrics.single.updateDegree;

import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntSet;

public class TripleIssue {
    private static final String GRAPH_FILE1 = "expressions_max_test.n3";
    
    @SuppressWarnings("deprecation")
    @Test
    public void testcase1() {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE1);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        
        IntSet outEdges = graph.getOutEdges(2);
        System.out.println("Outgoing Edges: " + outEdges);
        for(int edge: outEdges) {
            System.out.println("Head: " + graph.getHeadOfTheEdge(edge));
            System.out.println("Tail: " + graph.getTailOfTheEdge(edge));
            System.out.println("--");
        }
        
        IntSet inEdges = graph.getInEdges(2);
        System.out.println("Incoming Edges: " + inEdges);
        for(int edge:inEdges) {
            System.out.println("Head: " + graph.getHeadOfTheEdge(edge));
            System.out.println("Tail: " + graph.getTailOfTheEdge(edge));
            System.out.println("--");
        }
    }
    
}
