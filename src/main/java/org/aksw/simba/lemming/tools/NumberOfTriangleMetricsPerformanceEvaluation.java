package org.aksw.simba.lemming.tools;


import java.util.concurrent.Callable;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NumberOfTriangleMetricsPerformanceEvaluation implements Callable<Long> {

   private static final Logger LOGGER = LoggerFactory.getLogger(NumberOfTriangleMetricsPerformanceEvaluation.class);

   private SingleValueMetric metric;

   private ColouredGraph graph;


   public NumberOfTriangleMetricsPerformanceEvaluation(SingleValueMetric metric, ColouredGraph graph) {
      super();
      this.metric = metric;
      this.graph = graph;
   }


   public long evaluatePerformance() {
      long startTimeInMilliseconds = System.currentTimeMillis();
      double amountOfTriangles = metric.apply(graph);
      long requiredTimeInMilliseconds = System.currentTimeMillis() - startTimeInMilliseconds;
      LOGGER.info(String.format("%-30s%10d %10f", metric, (int) amountOfTriangles, requiredTimeInMilliseconds / 1000.0));
      return requiredTimeInMilliseconds;
   }


   public long evaluatePerformance(ColouredGraph graph) {
      this.graph = graph;
      return evaluatePerformance();
   }


   public void setGraph(ColouredGraph graph) {
      this.graph = graph;
   }


   @Override
   public Long call() throws Exception {
      return evaluatePerformance();
   }

}
