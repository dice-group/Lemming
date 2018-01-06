package org.aksw.simba.lemming.tools;


import java.util.Arrays;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodReader;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.triangle.EdgeIteratorNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.triangle.ForwardNumberOfTriangleMetric;
import org.aksw.simba.lemming.metrics.single.triangle.NodeIteratorCoreNumberOfTrianglesMetric;


public class EvaluationRunner {

   private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";


   public static void main(String args[]) {

      List<SingleValueMetric> metrics = Arrays.asList(new ForwardNumberOfTriangleMetric(), new NodeIteratorCoreNumberOfTrianglesMetric(),
            new EdgeIteratorNumberOfTrianglesMetric());

      List<ColouredGraph> graphs = getSemanticDogFoodGraphs();

      NumberOfTriangleMetricsPerformanceEvaluation evaluation = new NumberOfTriangleMetricsPerformanceEvaluation(metrics, graphs);
      evaluation.evaluatePerformance();
   }


   public static List<ColouredGraph> getSemanticDogFoodGraphs() {
      return Arrays.asList(SemanticWebDogFoodReader.readGraphsFromFile(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH));
   }


}
