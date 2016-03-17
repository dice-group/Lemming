/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.metrics.similarity;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

/**
 *
 * @author jsaveta
 */
public class HITSTest {

    private static final String GRAPH_FILE = "graph1.n3";

    @Test
    public void test() {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        
        //System.out.println("matrix: " +graph.getGraph().getAdjacencyMatrix());
        new HITS(graph);
        
        
//        graph.getGraph().display();
//        try {
//            Thread.sleep(100000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(HITSTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }    
}


        


        

        

