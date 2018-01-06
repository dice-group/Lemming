package org.aksw.simba.lemming.metrics.single.triangle;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;


public abstract class AbstractNumberOfTrianglesMetricTest {

   private static final int EXPECTED_TRIANGLES = 1;
   private static final String GRAPH_FILE = "graph1.n3";

   private SingleValueMetric metric;


   public AbstractNumberOfTrianglesMetricTest(SingleValueMetric metric) {
      this.metric = metric;
   }


   @Test
   public void testOnSimpleGraph() {
      Model model = ModelFactory.createDefaultModel();
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
      model.read(is, null, "N3");
      IOUtils.closeQuietly(is);

      GraphCreator creator = new GraphCreator();
      ColouredGraph graph = creator.processModel(model);
      Assert.assertNotNull(graph);

      double countedTriangles = metric.apply(graph);
      Assert.assertEquals(EXPECTED_TRIANGLES, countedTriangles, 0.000001);
   }


   @Test
   public void testOnEmailEuCoreNetwork() throws IOException {
      ColouredGraph graph = readEmailEuCoreNetwork();
      double countedTriangles = metric.apply(graph);
      Assert.assertEquals(105461, countedTriangles, 0.000001);
   }


   private ColouredGraph readEmailEuCoreNetwork() throws IOException {
      InputStream is = this.getClass().getClassLoader().getResourceAsStream("email-Eu-core.txt");
      List<String> lines = IOUtils.readLines(is);
      IOUtils.closeQuietly(is);
      Set<Integer> createdVertices = new HashSet<>();
      ColouredGraph graph = new ColouredGraph();
      for (String line : lines) {
         String[] splitLine = line.split(" ");
         if (splitLine.length == 2) {
            int from = Integer.parseInt(splitLine[0]);
            if (!createdVertices.contains(from)) {
               createdVertices.add(from);
               from = graph.addVertex();
            }
            int to = Integer.parseInt(splitLine[1]);
            if (!createdVertices.contains(to)) {
               createdVertices.add(to);
               to = graph.addVertex();
            }
            graph.addEdge(from, to);
         }
      }
      return graph;
   }

}
