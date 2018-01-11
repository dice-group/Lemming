package org.aksw.simba.lemming.tools;


import java.util.Collections;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NumberOfTriangleMetricsPerformanceEvaluation {

   private static final Logger LOGGER = LoggerFactory.getLogger(NumberOfTriangleMetricsPerformanceEvaluation.class);

   private List<SingleValueMetric> singleValueMetrics;

   private List<ColouredGraph> graphs;


   public NumberOfTriangleMetricsPerformanceEvaluation(List<SingleValueMetric> singleValueMetrics) {
      this(singleValueMetrics, Collections.emptyList());
   }


   public NumberOfTriangleMetricsPerformanceEvaluation(List<SingleValueMetric> singleValueMetrics, List<ColouredGraph> graphs) {
      super();
      this.singleValueMetrics = singleValueMetrics;
      this.graphs = graphs;
   }


   public void evaluatePerformance() {
      for (ColouredGraph graph : graphs) {
         LOGGER.info("Evaluating graph {} with {} nodes and {} edges.", graph, graph.getGraph().getNumberOfVertices(),
               graph.getGraph().getNumberOfEdges());
         for (SingleValueMetric metric : singleValueMetrics) {
            long startTimeInMilliseconds = System.currentTimeMillis();
            double amountOfTriangles = metric.apply(graph);
            long requiredTimeInMilliseconds = System.currentTimeMillis() - startTimeInMilliseconds;
            LOGGER.info(String.format("%-30s%10d %10f", metric, (int) amountOfTriangles, requiredTimeInMilliseconds / 1000.0));
         }
      }
   }


   public void evaluatePerformance(ColouredGraph graph) {
      LOGGER.info("Evaluating graph {} with {} nodes and {} edges.", graph, graph.getGraph().getNumberOfVertices(),
            graph.getGraph().getNumberOfEdges());
      for (SingleValueMetric metric : singleValueMetrics) {
         long startTimeInMilliseconds = System.currentTimeMillis();
         double amountOfTriangles = metric.apply(graph);
         long requiredTimeInMilliseconds = System.currentTimeMillis() - startTimeInMilliseconds;
         LOGGER.info(String.format("%-30s%10d %10f", metric, (int) amountOfTriangles, requiredTimeInMilliseconds / 1000.0));
      }
   }

}
