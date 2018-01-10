package org.aksw.simba.lemming.metrics.single.triangle;


import java.util.Random;

import org.aksw.simba.lemming.metrics.single.triangle.forward.ForwardNumberOfTriangleMetric;


public class DuolionNumberOfTrianglesMetricTest extends AbstractNumberOfTrianglesMetricTest {

   private static final double EDGE_SURVIVAL_PROBABILITY = 0.9;


   public DuolionNumberOfTrianglesMetricTest() {
      super(new DuolionNumberOfTrianglesMetric(new ForwardNumberOfTriangleMetric(), EDGE_SURVIVAL_PROBABILITY, new Random().nextLong()));
   }

}
