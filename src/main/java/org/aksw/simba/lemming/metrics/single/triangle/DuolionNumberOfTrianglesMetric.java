package org.aksw.simba.lemming.metrics.single.triangle;


import java.util.Random;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.cursors.IntCursor;


public class DuolionNumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

   private SingleValueMetric triangleCountingAlgorithm;

   private double edgeSurvivalProbability;

   private Random random;

   private ColouredGraph graphCopy;


   public DuolionNumberOfTrianglesMetric(SingleValueMetric triangleCountingMetric, double edgeSurvivalProbability, long seed) {
      super("duolion " + triangleCountingMetric.getName());
      this.triangleCountingAlgorithm = triangleCountingMetric;
      this.edgeSurvivalProbability = edgeSurvivalProbability;
      random = new Random(seed);
   }


   @Override
   public double apply(ColouredGraph graph) {
      graphCopy = graph.copy();

      for (IntCursor edgeCursor : graph.getGraph().getEdges()) {
         if (random.nextDouble() > edgeSurvivalProbability) {
            graphCopy.getGraph().removeEdge(edgeCursor.value);
         }
      }

      return triangleCountingAlgorithm.apply(graphCopy) * (1 / Math.pow(edgeSurvivalProbability, 3));
   }


}
