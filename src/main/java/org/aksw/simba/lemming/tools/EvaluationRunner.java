package org.aksw.simba.lemming.tools;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodReader;
import org.aksw.simba.lemming.creation.SimpleGraphFormatReader;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.triangle.EdgeIteratorNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.triangle.NodeIteratorCoreNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.triangle.forward.ForwardNumberOfTriangleMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EvaluationRunner {

   private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationRunner.class);

   private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
   private static final String SNAP_EVALUATION_DATASETS_PATH = "evaluation_datasets/snap";


   public static void main(String args[]) {

      List<SingleValueMetric> metrics = Arrays.asList(new ForwardNumberOfTriangleMetric(), new NodeIteratorCoreNumberOfTrianglesMetric(),
            new EdgeIteratorNumberOfTrianglesMetric());

      List<ColouredGraph> graphs = new ArrayList<>();
      graphs.addAll(getSnapEvaluationGraphs());
      graphs.addAll(getSemanticDogFoodGraphs());

      NumberOfTriangleMetricsPerformanceEvaluation evaluation = new NumberOfTriangleMetricsPerformanceEvaluation(metrics, graphs);
      evaluation.evaluatePerformance();
   }


   public static List<ColouredGraph> getSemanticDogFoodGraphs() {
      return Arrays.asList(SemanticWebDogFoodReader.readGraphsFromFile(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH));
   }


   public static List<ColouredGraph> getSnapEvaluationGraphs() {
      List<ColouredGraph> graphs = new ArrayList<>();
      File snapEvaluationDatasetFolderDirectory = new File(SNAP_EVALUATION_DATASETS_PATH);
      for (File evaluationDatasetFile : snapEvaluationDatasetFolderDirectory.listFiles()) {
         if (evaluationDatasetFile.getName().endsWith(".txt")) {
            LOGGER.debug("Reading file {}", evaluationDatasetFile);
            try {
               ColouredGraph graph = SimpleGraphFormatReader.readSimpleGraphFormatFile(evaluationDatasetFile.getAbsolutePath());
               graphs.add(graph);
            } catch (IOException e) {
               LOGGER.error("Could not read evaluation graph {}.", evaluationDatasetFile, e);
            }
         }
      }
      return graphs;
   }


}
