package org.aksw.simba.lemming.tools;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodReader;
import org.aksw.simba.lemming.creation.SimpleGraphFormatReader;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.triangle.AyzNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.triangle.DuolionNumberOfTrianglesMetric;
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

   private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

   private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
   private static final String SNAP_EVALUATION_DATASETS_PATH = "evaluation_datasets/snap";

   private static final int TIMEOUT_IN_SECONDS = 30;


   public static void main(String args[]) throws InterruptedException, ExecutionException, IOException {
      List<List<Long>> results = runEvaluationWithTimeLimit();
      StringJoiner contentJoiner = new StringJoiner("\n");
      for (List<Long> resultsForGraph : results) {
         StringJoiner lineJoiner = new StringJoiner(" ");
         resultsForGraph.stream().mapToDouble(result -> result / 1000.0).forEach(result -> lineJoiner.add(String.valueOf(result)));
         contentJoiner.add(lineJoiner.toString());
      }
      writeToFileCreatingMissingDirectories(new File("results/result.txt"), contentJoiner.toString());
      executorService.shutdown();
   }


   private static List<List<Long>> runEvaluationWithTimeLimit() throws InterruptedException, ExecutionException {
      List<SingleValueMetric> metrics = Arrays.asList(new ForwardNumberOfTriangleMetric(), new NodeIteratorNumberOfTrianglesMetric(),
            new NodeIteratorCoreNumberOfTrianglesMetric(), new MatrixMultiplicationNumberOfTrianglesMetric(),
            new AyzNumberOfTrianglesMetric(5), new DuolionNumberOfTrianglesMetric(new ForwardNumberOfTriangleMetric(), 0.5, 20735378465L));

      List<ColouredGraph> graphs = new ArrayList<>();
      graphs.addAll(getSnapEvaluationGraphs());
      graphs.addAll(getSemanticDogFoodGraphs());

      List<List<Long>> graphResults = new ArrayList<>();
      for (ColouredGraph graph : graphs) {
         LOGGER.info("Evaluating graph {} with {} nodes and {} edges.", graph, graph.getGraph().getNumberOfVertices(),
               graph.getGraph().getNumberOfEdges());
         List<Long> metricResults = new ArrayList<>();
         for (SingleValueMetric metric : metrics) {
            metricResults.add(runEvaluationOnGraphWithTimeLimit(metric, graph, TIMEOUT_IN_SECONDS));
            metricResults.add(runEvaluationOnGraphWithTimeLimit(metric, generateReferenceStarGraph(graph), TIMEOUT_IN_SECONDS));
            metricResults.add(runEvaluationOnGraphWithTimeLimit(metric, generateReferenceGridGraph(graph), TIMEOUT_IN_SECONDS));
            metricResults.add(runEvaluationOnGraphWithTimeLimit(metric, generateReferenceRingGraph(graph), TIMEOUT_IN_SECONDS));
            metricResults.add(runEvaluationOnGraphWithTimeLimit(metric, generateReferenceCliqueGraph(graph), TIMEOUT_IN_SECONDS));
            metricResults
                  .add(runEvaluationOnGraphWithTimeLimit(metric, generateReferenceCompleteBipartiteGraph(graph), TIMEOUT_IN_SECONDS));
         }
         graphResults.add(metricResults);
      }
      return graphResults;
   }


   private static Long runEvaluationOnGraphWithTimeLimit(SingleValueMetric metric, ColouredGraph graph, int timeoutInSeconds)
         throws InterruptedException,
            ExecutionException {
      NumberOfTriangleMetricsPerformanceEvaluation evaluationTask = new NumberOfTriangleMetricsPerformanceEvaluation(metric, graph);
      Future<Long> evaluationResult = executorService.submit(evaluationTask);
      try {
         return evaluationResult.get(timeoutInSeconds, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
         evaluationResult.cancel(true);
         return -1L;
      }
   }


   private static void runEvaluationWithoutTimeLimit() {
      List<SingleValueMetric> metrics = Arrays.asList(new ForwardNumberOfTriangleMetric(), new NodeIteratorNumberOfTrianglesMetric(),
            new NodeIteratorCoreNumberOfTrianglesMetric(), new MatrixMultiplicationNumberOfTrianglesMetric(),
            new AyzNumberOfTrianglesMetric(5), new DuolionNumberOfTrianglesMetric(new ForwardNumberOfTriangleMetric(), 0.5, 20735378465L));

      List<ColouredGraph> graphs = new ArrayList<>();
      graphs.addAll(getSnapEvaluationGraphs());
      graphs.addAll(getSemanticDogFoodGraphs());

      System.out.println(graphs.size());
      for (ColouredGraph graph : graphs) {
         LOGGER.info("Evaluating graph {} with {} nodes and {} edges.", graph, graph.getGraph().getNumberOfVertices(),
               graph.getGraph().getNumberOfEdges());
         for (SingleValueMetric metric : metrics) {
            NumberOfTriangleMetricsPerformanceEvaluation evaluation = new NumberOfTriangleMetricsPerformanceEvaluation(metric, graph);
            evaluation.evaluatePerformance(graph);
            evaluation.evaluatePerformance(generateReferenceStarGraph(graph));
            evaluation.evaluatePerformance(generateReferenceGridGraph(graph));
            evaluation.evaluatePerformance(generateReferenceRingGraph(graph));
            evaluation.evaluatePerformance(generateReferenceCliqueGraph(graph));
            evaluation.evaluatePerformance(generateReferenceCompleteBipartiteGraph(graph));
         }
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


   /**
    * Writes the given {@code content} to the given file.
    * 
    * @param file The {@link File} to write the given {@code content} to.
    * @param content The content to write in the given {@code file}.
    * @throws IOException If an I/O error occurs.
    */
   public static void writeToFile(File file, String content) throws IOException {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
         writer.write(content);
      }
   }


   /**
    * Writes the given {@code content} to the given file.
    * 
    * @param file The {@link File} to write the given {@code content} to.
    * @param content The content to write in the given {@code file}.
    * @throws IOException If an I/O error occurs.
    */
   public static void writeToFileCreatingMissingDirectories(File file, String content) throws IOException {
      File directory = file.getParentFile();
      if (directory != null) {
         directory.mkdirs();
      }
      writeToFile(file, content);
   }


}
