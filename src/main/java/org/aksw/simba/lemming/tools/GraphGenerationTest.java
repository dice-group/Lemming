package org.aksw.simba.lemming.tools;

import grph.Grph;
import grph.algo.topology.ClassicalGraphs;
import grph.algo.topology.StarTopologyGenerator;
import grph.in_memory.InMemoryGrph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
import org.aksw.simba.lemming.grph.generator.GraphGenerationGroupingTriple;
import org.aksw.simba.lemming.grph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.grph.generator.GraphGenerationRandomly2;
import org.aksw.simba.lemming.grph.generator.GraphGenerationSimpleApproach;
import org.aksw.simba.lemming.grph.generator.GraphGenerationSimpleApproach2;
import org.aksw.simba.lemming.grph.generator.GraphGenerationWithoutEdgeColours;
import org.aksw.simba.lemming.grph.generator.GraphLexicalization;
import org.aksw.simba.lemming.grph.generator.GraphRefinement;
import org.aksw.simba.lemming.grph.generator.IGraphGeneration;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexInDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexOutDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.util.MetricTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class GraphGenerationTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationTest.class);
	private static final int MAX_ITERATIONS = 50;
	private static final double MIN_FITNESS = 100000.0;
	private static final boolean USE_SEMANTIC_DOG_FOOD = true;
	private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
	
	private static final int NUMBEROFDESIREDVERTICES = 15000;
	
	public static void main(String[] args) {
		
		boolean isStop = false;
		
// 		if(!isStop){
// 	   		return ;
//    	}

		// For this test, we do not need assertions
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);
        List<SingleValueMetric> metrics = new ArrayList<>();
        // metrics.add(new NumberOfTrianglesMetric());
        // metrics.add(new AvgClusteringCoefficientMetric());
        metrics.add(new AvgVertexDegreeMetric());
        metrics.add(new MaxVertexOutDegreeMetric());
        metrics.add(new MaxVertexInDegreeMetric());
        //metrics.add(new MinVertexOutDegreeMetric());
        //metrics.add(new MinVertexInDegreeMetric());
        //metrics.add(new MinVertexOutDegreeMetric());
        metrics.add(new DiameterMetric());
        metrics.add(new NumberOfEdgesMetric());
        metrics.add(new NumberOfVerticesMetric());
        
        ColouredGraph graphs[] = new ColouredGraph[20];
        
        //load RDF data to coloured graph
        if (USE_SEMANTIC_DOG_FOOD) {
            graphs = SemanticWebDogFoodReader.readGraphsFromFile(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
        }
        
        /*
         * ---------------------------------------
         * Generate a mimic graph
         * ---------------------------------------
         */
        IGraphGeneration grphGenerator;
       //grphGenerater = new GraphGenerationRandomly(NUMBEROFDESIREDVERTICES, graphs);
       //grphGenerater = new GraphGenerationRandomly2(NUMBEROFDESIREDVERTICES, graphs);
       grphGenerator = new GraphGenerationSimpleApproach(NUMBEROFDESIREDVERTICES, graphs);
       //grphGenerater = new GraphGenerationSimpleApproach2(NUMBEROFDESIREDVERTICES, graphs);
       //grphGenerater = new GraphGenerationGroupingTriple(NUMBEROFDESIREDVERTICES, graphs);
       //grphGenerater = new GraphGenerationWithoutEdgeColours(NUMBEROFDESIREDVERTICES, graphs);
        
        double currentTime = System.currentTimeMillis();
        // generate the new graph
        ColouredGraph tempGrph =  grphGenerator.generateGraph();
        // estimate the costed time for generation
        System.out.println("End of graph generation!");
        currentTime = System.currentTimeMillis() - currentTime;
        System.out.println("Time of graph generation: " + currentTime);
        
        //MetricTester.printMetricInformation(metrics, graphs);
        //MetricTester.printMetricInformation(metrics, tempGrph);
        
        FitnessFunction fitnessFunc = new LengthAwareMinSquaredError();
        fitnessFunc = new ReferenceGraphBasedFitnessDecorator(fitnessFunc,
                createReferenceGraphVectors(graphs, metrics));

        CharacteristicExpressionSearcher searcher = new CharacteristicExpressionSearcher(metrics,
                new LeaveNodeReplacingRefinementOperator(metrics), new RedberryBasedFactory(), fitnessFunc, MIN_FITNESS,
                MAX_ITERATIONS);
        searcher.setDebug(true);
        
        
        List<List<Double>> lstOrigConstantValues = new ArrayList<List<Double>>();
        
        
        SortedSet<RefinementNode> bestNodes = searcher.findExpression(graphs, 5);
        for (RefinementNode n : bestNodes) {
            System.out.print("Fitness value: " + n.getFitness());
            System.out.print(" --> ");
            System.out.println(n.toString());
            System.out.print("\t");
            List<Double> lstConsVal = new ArrayList<Double>();
            for (int i = 0; i < graphs.length; ++i) {
            	double val = n.getExpression().getValue(graphs[i]);
            	lstConsVal.add(val);
            	System.out.print(val + " ");
            }
            lstOrigConstantValues.add(lstConsVal);
            System.out.println();
        }
        
        /*
         * ---------------------------------------
         * refine the mimic graph
         * ---------------------------------------
         */
        GraphRefinement grphRefinement = new GraphRefinement(graphs, 1000, grphGenerator, bestNodes);
        System.out.println("Refine graph randomly");
        //grphRefinement.setRefineGraphRandomly(false);
        grphRefinement.setRefineGraphRandomly(true);
        ColouredGraph refinedGrph = grphRefinement.refineGraph();
        System.out.println("==============================");
        for (RefinementNode n : bestNodes) {
            double val = n.getExpression().getValue(refinedGrph);
            System.out.println(val + " ");
        }
        
        /*
         * ---------------------------------------
         * lexicalize the mimic graph
         * ---------------------------------------
         */
        GraphLexicalization graphLexicalization = new GraphLexicalization(graphs, grphGenerator);
        SemanticWebDogFoodReader.writeGraphsToFile(graphLexicalization.lexicalizeGraph());
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
	            //vectors.add(MetricUtils.calculateGraphMetrics(new ColouredGraph(temp, null, null), metrics));
	            vectors.add(MetricUtils.calculateGraphMetrics(new ColouredGraph(temp, null, null, null), metrics));
	            temp = null;

	            // Grid
	            partSize = (int) Math.sqrt(numberOfNodes);
	            //vectors.add(MetricUtils.calculateGraphMetrics(new ColouredGraph(ClassicalGraphs.grid(partSize, partSize), null, null), metrics));
	            vectors.add(MetricUtils.calculateGraphMetrics(new ColouredGraph(ClassicalGraphs.grid(partSize, partSize), null, null, null), metrics));

	            // Ring
//	            vectors.add(MetricUtils.calculateGraphMetrics(new ColouredGraph(ClassicalGraphs.cycle(numberOfNodes), null, null), metrics));
	            vectors.add(MetricUtils.calculateGraphMetrics(
	                    new ColouredGraph(ClassicalGraphs.cycle(numberOfNodes), null, null, null), metrics));

	            // Clique
	            // vectors.add(MetricUtils.calculateGraphMetrics(
	            // new ColouredGraph(ClassicalGraphs.completeGraph(numberOfNodes),
	            // null, null), metrics));
//	            vectors.add(MetricUtils.calculateGraphMetrics(new ColouredGraph(ClassicalGraphs.completeGraph(partSize), null, null), metrics));
	            vectors.add(MetricUtils.calculateGraphMetrics(new ColouredGraph(ClassicalGraphs.completeGraph(partSize), null, null, null), metrics));
	            // Bipartite
	            // partSize = numberOfNodes / 2;
	            partSize = numberOfNodes / 8;
	            //vectors.add(MetricUtils.calculateGraphMetrics( new ColouredGraph(ClassicalGraphs.completeBipartiteGraph(partSize, partSize), null, null), metrics));
	            vectors.add(MetricUtils.calculateGraphMetrics( new ColouredGraph(ClassicalGraphs.completeBipartiteGraph(partSize, partSize), null, null, null), metrics));
	            
	        }
	        return vectors.toArray(new ObjectDoubleOpenHashMap[vectors.size()]);
	    }
	
}

