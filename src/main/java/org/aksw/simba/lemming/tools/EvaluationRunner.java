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
import org.aksw.simba.lemming.metrics.single.triangle.NodeIteratorCoreNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.triangle.NodeIteratorNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.triangle.forward.ForwardNumberOfTriangleMetric;
import org.aksw.simba.lemming.metrics.single.triangle.matrix.MatrixMultiplicationNumberOfTrianglesMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import grph.Grph;
import grph.algo.topology.ClassicalGraphs;
import grph.algo.topology.StarTopologyGenerator;
import grph.in_memory.InMemoryGrph;


public class EvaluationRunner {

   private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationRunner.class);

   private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
   private static final String SNAP_EVALUATION_DATASETS_PATH = "evaluation_datasets/snap";


   public static void main(String args[]) {

      List<SingleValueMetric> metrics = Arrays.asList(new ForwardNumberOfTriangleMetric(), new NodeIteratorNumberOfTrianglesMetric(),
            new NodeIteratorCoreNumberOfTrianglesMetric(), new MatrixMultiplicationNumberOfTrianglesMetric());

      List<ColouredGraph> graphs = new ArrayList<>();
      // graphs.addAll(getSnapEvaluationGraphs());
      graphs.addAll(getSemanticDogFoodGraphs());

      NumberOfTriangleMetricsPerformanceEvaluation evaluation = new NumberOfTriangleMetricsPerformanceEvaluation(metrics);
      System.out.println(graphs.size());
      for (ColouredGraph graph : graphs) {
         LOGGER.info("===STARTING NEXT MAIN GRAPH===");
         evaluation.evaluatePerformance(graph);
         evaluation.evaluatePerformance(generateReferenceStarGraph(graph));
         evaluation.evaluatePerformance(generateReferenceGridGraph(graph));
         evaluation.evaluatePerformance(generateReferenceRingGraph(graph));
         evaluation.evaluatePerformance(generateReferenceCliqueGraph(graph));
         evaluation.evaluatePerformance(generateReferenceCompleteBipartiteGraph(graph));
      }

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


   private static ColouredGraph generateReferenceStarGraph(ColouredGraph graph) {
      StarTopologyGenerator starGenerator = new StarTopologyGenerator();
      Grph starGraph = new InMemoryGrph();
      starGraph.addNVertices(graph.getGraph().getNumberOfVertices());
      starGenerator.compute(starGraph);
      return new ColouredGraph(starGraph, graph.toString() + "-star");
   }


   private static ColouredGraph generateReferenceGridGraph(ColouredGraph graph) {
      int partSize = (int) Math.sqrt(graph.getGraph().getNumberOfVertices());
      return new ColouredGraph(ClassicalGraphs.grid(partSize, partSize), graph.toString() + "-grid");
   }


   private static ColouredGraph generateReferenceRingGraph(ColouredGraph graph) {
      int numberOfNodes = graph.getGraph().getNumberOfVertices();
      return new ColouredGraph(ClassicalGraphs.cycle(numberOfNodes), graph.toString() + "-ring");
   }


   private static ColouredGraph generateReferenceCliqueGraph(ColouredGraph graph) {
      int partSize = (int) Math.sqrt(graph.getGraph().getNumberOfVertices());
      return new ColouredGraph(ClassicalGraphs.completeGraph(partSize), graph.toString() + "-clique");
   }


   private static ColouredGraph generateReferenceCompleteBipartiteGraph(ColouredGraph graph) {
      int partSize = (int) (graph.getGraph().getNumberOfVertices() / 8.0);
      return new ColouredGraph(ClassicalGraphs.completeBipartiteGraph(partSize, partSize), graph.toString() + "-bipartite graph");
   }


}
