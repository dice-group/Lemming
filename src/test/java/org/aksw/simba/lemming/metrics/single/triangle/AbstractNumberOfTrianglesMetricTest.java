package org.aksw.simba.lemming.metrics.single.triangle;


import java.io.IOException;
import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.creation.SimpleGraphFormatReader;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;


public abstract class AbstractNumberOfTrianglesMetricTest {

   private static final double DOUBLE_COMPARISON_DELTA = 0.000001;
   private static final String PATH_TO_SIMPLE_EXAMPLES = "metric/triangle/";

   private SingleValueMetric metric;


   public AbstractNumberOfTrianglesMetricTest(SingleValueMetric metric) {
      this.metric = metric;
   }


   @Test
   public void testOnSimpleGraph() {
      Model model = ModelFactory.createDefaultModel();
      InputStream is = this.getClass().getClassLoader().getResourceAsStream("graph1.n3");
      model.read(is, null, "N3");
      IOUtils.closeQuietly(is);

      GraphCreator creator = new GraphCreator();
      ColouredGraph graph = creator.processModel(model);
      Assert.assertNotNull(graph);

      double countedTriangles = metric.apply(graph);
      Assert.assertEquals(1, countedTriangles, DOUBLE_COMPARISON_DELTA);
   }


   @Test
   public void testOnEmailEuCoreNetwork() throws IOException {
      ColouredGraph graph = SimpleGraphFormatReader.readSimpleGraphFormatFileFromResources(PATH_TO_SIMPLE_EXAMPLES + "email-Eu-core.txt");
      double countedTriangles = metric.apply(graph);
      Assert.assertEquals(105461, countedTriangles, DOUBLE_COMPARISON_DELTA);
   }


   @Test
   public void testOnSimpleSelfloopTriangle() throws IOException {
      ColouredGraph graph = SimpleGraphFormatReader
            .readSimpleGraphFormatFileFromResources(PATH_TO_SIMPLE_EXAMPLES + "simple_selfloop_triangle.txt");
      double countedTriangles = metric.apply(graph);
      Assert.assertEquals(1, countedTriangles, DOUBLE_COMPARISON_DELTA);
   }


   @Test
   public void testOnSimpleHexagonNetwork() throws IOException {
      ColouredGraph graph = SimpleGraphFormatReader
            .readSimpleGraphFormatFileFromResources(PATH_TO_SIMPLE_EXAMPLES + "simple_hexagon_graph.txt");
      double countedTriangles = metric.apply(graph);
      Assert.assertEquals(6, countedTriangles, DOUBLE_COMPARISON_DELTA);
   }


   @Test
   public void testOnSimpleHalfHexagonNetwork() throws IOException {
      ColouredGraph graph = SimpleGraphFormatReader
            .readSimpleGraphFormatFileFromResources(PATH_TO_SIMPLE_EXAMPLES + "simple_half_hexagon_graph.txt");
      double countedTriangles = metric.apply(graph);
      Assert.assertEquals(3, countedTriangles, 0.000001);
   }


   @Test
   public void testOnSchankWagnerExample() throws IOException {
      ColouredGraph graph = SimpleGraphFormatReader
            .readSimpleGraphFormatFileFromResources(PATH_TO_SIMPLE_EXAMPLES + "schank_wagner_example.txt");
      double countedTriangles = metric.apply(graph);
      Assert.assertEquals(3, countedTriangles, 0.000001);
   }

}
