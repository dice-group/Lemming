package org.aksw.simba.lemming.metrics.single.triangle;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;


public class NumberOfTrianglesMetricTest {

   private static final int EXPECTED_TRIANGLES = 1;
   private static final String GRAPH_FILE = "graph1.n3";


   @Test
   public void test() {
      List<SingleValueMetric> metrics = new ArrayList<>();
      metrics.add(new EdgeIteratorNumberOfTrianglesMetric());
      metrics.add(new NodeIteratorCoreNumberOfTrianglesMetric());
      metrics.add(new ForwardNumberOfTriangleMetric());

      Model model = ModelFactory.createDefaultModel();
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
      model.read(is, null, "N3");
      IOUtils.closeQuietly(is);

      GraphCreator creator = new GraphCreator();
      ColouredGraph graph = creator.processModel(model);
      Assert.assertNotNull(graph);

      for (SingleValueMetric metric : metrics) {
         double countedTriangles = metric.apply(graph);
         Assert.assertEquals(EXPECTED_TRIANGLES, countedTriangles, 0.000001);
      }

   }
}
