package org.aksw.simba.lemming.tools;


import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NumberOfTriangleMetricsPerformanceEvaluation {

   private static final Logger LOGGER = LoggerFactory.getLogger(NumberOfTriangleMetricsPerformanceEvaluation.class);

   private List<SingleValueMetric> singleValueMetrics;

   private List<ColouredGraph> graphs;


   public NumberOfTriangleMetricsPerformanceEvaluation(List<SingleValueMetric> singleValueMetrics, List<ColouredGraph> graphs) {
      super();
      this.singleValueMetrics = singleValueMetrics;
      this.graphs = graphs;
   }


   public void evaluatePerformance() {
      for (ColouredGraph graph : graphs) {
         LOGGER.info("Starting computation of graph with size {}.", graph.getVertices().size());
         for (SingleValueMetric metric : singleValueMetrics) {
            long startTimeInMilliseconds = System.currentTimeMillis();
            double amountOfTriangles = metric.apply(graph);
            long requiredTimeInMilliseconds = System.currentTimeMillis() - startTimeInMilliseconds;
            LOGGER.info(String.format("%-30s%10d %10f", metric, (int) amountOfTriangles, requiredTimeInMilliseconds / 1000.0));
         }
      }
   }


}
