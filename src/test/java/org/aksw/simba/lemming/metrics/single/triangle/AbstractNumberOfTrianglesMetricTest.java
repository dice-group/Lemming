package org.aksw.simba.lemming.metrics.single.triangle;


import java.io.InputStream;

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
   public void test() {
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
}
