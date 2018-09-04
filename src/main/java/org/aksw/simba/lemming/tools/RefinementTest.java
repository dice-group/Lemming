package org.aksw.simba.lemming.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.refinement.CharacteristicExpressionSearcher;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.aksw.simba.lemming.algo.refinement.fitness.FitnessFunction;
import org.aksw.simba.lemming.algo.refinement.fitness.LengthAwareMinSquaredError;
import org.aksw.simba.lemming.algo.refinement.fitness.ReferenceGraphBasedFitnessDecorator;
import org.aksw.simba.lemming.algo.refinement.operator.LeaveNodeReplacingRefinementOperator;
import org.aksw.simba.lemming.algo.refinement.redberry.RedberryBasedFactory;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodReader;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.single.AvgClusteringCoefficientMetric;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import grph.Grph;
import grph.algo.topology.ClassicalGraphs;
import grph.algo.topology.GridTopologyGenerator;
import grph.algo.topology.StarTopologyGenerator;
import grph.in_memory.InMemoryGrph;

@Ignore
public class RefinementTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefinementTest.class);

    private static final double MIN_FITNESS = 100000.0;
    private static final int MAX_ITERATIONS = 50;
    private static final boolean USE_SEMANTIC_DOG_FOOD = true;
    private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";

    public static void main(String[] args) {
        // MultiThreadProcessing.defaultNumberOfThreads = 1;

        // For this test, we do not need assertions
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);

        List<SingleValueMetric> metrics = new ArrayList<>();
        // metrics.add(new AvgClusteringCoefficientMetric());
        // metrics.add(new AvgVertexDegreeMetric());
         metrics.add(new NumberOfTrianglesMetric());
//        metrics.add(new MaxVertexOutDegreeMetric());
//        metrics.add(new DiameterMetric());
//        metrics.add(new NumberOfEdgesMetric());
//        metrics.add(new NumberOfVerticesMetric());

        ColouredGraph graphs[] = new ColouredGraph[20];
        // StarTopologyGenerator generator = new StarTopologyGenerator();
        GridTopologyGenerator generator = new GridTopologyGenerator();
        // RingTopologyGenerator generator = new RingTopologyGenerator();
        // RandomNewmanWattsStrogatzTopologyGenerator generator = new
        // RandomNewmanWattsStrogatzTopologyGenerator();
        Grph temp;
        for (int i = 0; i < graphs.length; ++i) {
            temp = new InMemoryGrph();
            generator.setWidth(i + 2);
            generator.setHeight(i + 1);
            // temp.addNVertices((i + 2) * (i + 1));
            // generator.setNumberOfEdges((i + 2) * (i + 1));
            // generator.setK(3);
            generator.compute(temp);
            graphs[i] = new ColouredGraph(temp, null, null);
        }

        if (USE_SEMANTIC_DOG_FOOD) {
            graphs = SemanticWebDogFoodReader.readGraphsFromFile(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
        }

        // FitnessFunction fitnessFunc = new MinSquaredError();
        FitnessFunction fitnessFunc = new LengthAwareMinSquaredError();
        fitnessFunc = new ReferenceGraphBasedFitnessDecorator(fitnessFunc,
                createReferenceGraphVectors(graphs, metrics));

        CharacteristicExpressionSearcher searcher = new CharacteristicExpressionSearcher(metrics,
                new LeaveNodeReplacingRefinementOperator(metrics), new RedberryBasedFactory(), fitnessFunc, MIN_FITNESS,
                MAX_ITERATIONS);
        searcher.setDebug(true);

        SortedSet<RefinementNode> bestNodes = searcher.findExpression(graphs, 5);
        for (RefinementNode n : bestNodes) {
            System.out.print(n.getFitness());
            System.out.print(" --> ");
            System.out.println(n.toString());

            System.out.print(n.getExpression().getValue(graphs[0]));
            for (int i = 1; i < graphs.length; ++i) {
                System.out.print('\t');
                System.out.print(n.getExpression().getValue(graphs[i]));
            }
            System.out.println();
        }
    }

    @SuppressWarnings("unchecked")
    private static ObjectDoubleOpenHashMap<String>[] createReferenceGraphVectors(ColouredGraph[] graphs,
            List<SingleValueMetric> metrics) {
        Grph temp;
        int numberOfNodes, partSize;
        List<ObjectDoubleOpenHashMap<String>> vectors = new ArrayList<ObjectDoubleOpenHashMap<String>>(
                5 * graphs.length);
        for (int i = 0; i < graphs.length; ++i) {
            numberOfNodes = graphs[i].getGraph().getNumberOfVertices();
            LOGGER.info("Generating reference graphs with " + numberOfNodes + " nodes.");
            // Star
            StarTopologyGenerator starGenerator = new StarTopologyGenerator();
            temp = new InMemoryGrph();
            temp.addNVertices(numberOfNodes);
            starGenerator.compute(temp);
            vectors.add(MetricUtils.calculateGraphMetrics(new ColouredGraph(temp, null, null), metrics));
            temp = null;

            // Grid
            partSize = (int) Math.sqrt(numberOfNodes);
            MetricUtils.calculateGraphMetrics(new ColouredGraph(ClassicalGraphs.grid(partSize, partSize), null, null),
                    metrics);

            // Ring
            vectors.add(MetricUtils.calculateGraphMetrics(
                    new ColouredGraph(ClassicalGraphs.cycle(numberOfNodes), null, null), metrics));

            // Clique
            // vectors.add(MetricUtils.calculateGraphMetrics(
            // new ColouredGraph(ClassicalGraphs.completeGraph(numberOfNodes),
            // null, null), metrics));
            vectors.add(MetricUtils.calculateGraphMetrics(
                    new ColouredGraph(ClassicalGraphs.completeGraph(partSize), null, null), metrics));

            // Bipartite
            // partSize = numberOfNodes / 2;
            partSize = numberOfNodes / 8;
            vectors.add(MetricUtils.calculateGraphMetrics(
                    new ColouredGraph(ClassicalGraphs.completeBipartiteGraph(partSize, partSize), null, null),
                    metrics));
        }
        return vectors.toArray(new ObjectDoubleOpenHashMap[vectors.size()]);
    }
}
