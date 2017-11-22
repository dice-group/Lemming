package org.aksw.simba.lemming.metrics.similarity;

import java.io.InputStream;
import java.util.Arrays;
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
//        System.out.println("graph.getInNeighborhoodsArray()" +Arrays.deepToString(graph.getInNeighborhoodsArray()));
//        System.out.println("graph.getOutNeighborhoodsArray()" +Arrays.deepToString(graph.getOutNeighborhoodsArray()));
        
        // Commented out the following line since HITS is missing.
        // new HITS(graph);
        
        
//        graph.getGraph().display();
//        try {
//            Thread.sleep(100000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(HITSTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }    
}


        


        

        

