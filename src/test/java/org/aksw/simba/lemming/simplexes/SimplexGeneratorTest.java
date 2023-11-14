package org.aksw.simba.lemming.simplexes;


import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.LinkedGeoDataset;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationSimplexApproach2;
import org.aksw.simba.lemming.simplexes.generator.GraphGenerationSimplexApproach1;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;

public class SimplexGeneratorTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimplexGeneratorTest.class);
	
	/**
	 * This test executes the generator for two SWDF datasets.
	 */
	//@Test
	public void testGeneratorOn2SWDFGraphs() {
		
		//Read two input graphs for SWDF dataset
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 20;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		/*
		 * Print stats for two graphs of SWDF dataset
		 * int graphId = 1; for (ColouredGraph graph: graphs) {
		 * System.out.println(graphId); System.out.println("Edges count: " +
		 * graph.getEdges().size()); System.out.println("Vertices count: " +
		 * graph.getVertices().size()); graphId++; }
		 */
		
		// Intialize other parameters required for the generator
		
		// threads
		int iNumberOfThreads = -1;
		
		// seed for Random object
		long seed = Long.parseLong("1694360290692");
		
		// Number of vertices
		//int iNumberOfVertices = 2773;
		int iNumberOfVertices = 45420;
		
		GraphGenerationSimplexApproach2 generatorSimplex = new GraphGenerationSimplexApproach2(iNumberOfVertices, graphs, iNumberOfThreads, seed);
		ColouredGraph mimicGraph = generatorSimplex.generateGraph();
		System.out.println("Mimic graph statistics:");
		System.out.println(mimicGraph.getVertices().size());
		System.out.println(mimicGraph.getEdges().size());
		ObjectArrayList<BitSet> edgeColours = mimicGraph.getEdgeColours();
		Object[] arredgeColours = edgeColours.toArray();
		int numOfnullEdges = 0;
		for (Object objEdge :arredgeColours) {
			if (objEdge == null) {
				numOfnullEdges++;
			}
		}
		
		System.out.println("Number of null edges: " + numOfnullEdges);
		//System.out.println(edgeColours);
		
	}
	
	/**
	 * This test executes the generator for two SWDF datasets.
	 */
	//@Test
	public void secondGeneratorTestOn2SWDFGraphs() {
		
		//Read two input graphs for SWDF dataset
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 20;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		/*
		 * Print stats for two graphs of SWDF dataset
		 * int graphId = 1; for (ColouredGraph graph: graphs) {
		 * System.out.println(graphId); System.out.println("Edges count: " +
		 * graph.getEdges().size()); System.out.println("Vertices count: " +
		 * graph.getVertices().size()); graphId++; }
		 */
		
		// Intialize other parameters required for the generator
		
		// threads
		int iNumberOfThreads = -1;
		
		// seed for Random object
		long seed = Long.parseLong("1694360290692");
		
		// Number of vertices
		int iNumberOfVertices = 2773;
		//int iNumberOfVertices = 45420;
		
		GraphGenerationSimplexApproach2 generatorSimplex = new GraphGenerationSimplexApproach2(iNumberOfVertices, graphs, iNumberOfThreads, seed);
		ColouredGraph mimicGraph = generatorSimplex.generateGraph();
		System.out.println("Mimic graph statistics:");
		System.out.println(mimicGraph.getVertices().size());
		System.out.println(mimicGraph.getEdges().size());
		ObjectArrayList<BitSet> edgeColours = mimicGraph.getEdgeColours();
		Object[] arredgeColours = edgeColours.toArray();
		int numOfnullEdges = 0;
		for (Object objEdge :arredgeColours) {
			if (objEdge == null) {
				numOfnullEdges++;
			}
		}
		
		System.out.println("Number of null edges: " + numOfnullEdges);
		//System.out.println(edgeColours);
		
	}
	
	/**
	 * This test executes the generator for two SWDF datasets.
	 */
	//@Test
	public void secondGeneratorTestOnLGD() {
		
		//Read two input graphs for SWDF dataset
		
		String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";
		

		int numOfGraphsToAnalyze = 20;
		
		IDatasetManager mDatasetManager = new LinkedGeoDataset();
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(LINKED_GEO_DATASET_FOLDER_PATH);
		
		/*
		 * Print stats for two graphs of SWDF dataset
		 * int graphId = 1; for (ColouredGraph graph: graphs) {
		 * System.out.println(graphId); System.out.println("Edges count: " +
		 * graph.getEdges().size()); System.out.println("Vertices count: " +
		 * graph.getVertices().size()); graphId++; }
		 */
		
		// Intialize other parameters required for the generator
		
		// threads
		int iNumberOfThreads = -1;
		
		// seed for Random object
		long seed = Long.parseLong("1694360290692");
		
		// Number of vertices
		int iNumberOfVertices = 4542000;
		//int iNumberOfVertices = 45420;
		
		GraphGenerationSimplexApproach2 generatorSimplex = new GraphGenerationSimplexApproach2(iNumberOfVertices, graphs, iNumberOfThreads, seed);
		ColouredGraph mimicGraph = generatorSimplex.generateGraph();
		System.out.println("Mimic graph statistics:");
		System.out.println(mimicGraph.getVertices().size());
		System.out.println(mimicGraph.getEdges().size());
		ObjectArrayList<BitSet> edgeColours = mimicGraph.getEdgeColours();
		Object[] arredgeColours = edgeColours.toArray();
		int numOfnullEdges = 0;
		for (Object objEdge :arredgeColours) {
			if (objEdge == null) {
				numOfnullEdges++;
			}
		}
		
		System.out.println("Number of null edges: " + numOfnullEdges);
		//System.out.println(edgeColours);
		
	}
	
	//@Test
	public void newDesignSimplexGenerator() {
		// GraphGenerationSimplexApproach1
		//Read two input graphs for SWDF dataset
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 20;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		/*
		 * Print stats for two graphs of SWDF dataset
		 * int graphId = 1; for (ColouredGraph graph: graphs) {
		 * System.out.println(graphId); System.out.println("Edges count: " +
		 * graph.getEdges().size()); System.out.println("Vertices count: " +
		 * graph.getVertices().size()); graphId++; }
		 */
		
		// Intialize other parameters required for the generator
		
		// threads
		int iNumberOfThreads = -1;
		
		// seed for Random object
		long seed = Long.parseLong("1694360290692");
		
		// Number of vertices
		int iNumberOfVertices = 2773;
		//int iNumberOfVertices = 45420;
		
		GraphGenerationSimplexApproach1 generatorSimplex = new GraphGenerationSimplexApproach1(iNumberOfVertices, graphs, iNumberOfThreads, seed);
		ColouredGraph mimicGraph = generatorSimplex.generateGraph();
		System.out.println("Mimic graph statistics:");
		System.out.println(mimicGraph.getVertices().size());
		System.out.println(mimicGraph.getEdges().size());
		ObjectArrayList<BitSet> edgeColours = mimicGraph.getEdgeColours();
		Object[] arredgeColours = edgeColours.toArray();
		int numOfnullEdges = 0;
		for (Object objEdge :arredgeColours) {
			if (objEdge == null) {
				numOfnullEdges++;
			}
		}
		
		System.out.println("Number of null edges: " + numOfnullEdges);
		//System.out.println(edgeColours);
	}
	
	//@Test
	public void newDesignLGD() {
		
		//Read two input graphs for SWDF dataset
		
		String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";
		

		int numOfGraphsToAnalyze = 20;
		
		IDatasetManager mDatasetManager = new LinkedGeoDataset();
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(LINKED_GEO_DATASET_FOLDER_PATH);
		
		/*
		 * Print stats for two graphs of SWDF dataset
		 * int graphId = 1; for (ColouredGraph graph: graphs) {
		 * System.out.println(graphId); System.out.println("Edges count: " +
		 * graph.getEdges().size()); System.out.println("Vertices count: " +
		 * graph.getVertices().size()); graphId++; }
		 */
		
		// Intialize other parameters required for the generator
		
		// threads
		int iNumberOfThreads = -1;
		
		// seed for Random object
		long seed = Long.parseLong("1694360290692");
		
		// Number of vertices
		int iNumberOfVertices = 591649;
		//int iNumberOfVertices = 45420;
		
		GraphGenerationSimplexApproach1 generatorSimplex = new GraphGenerationSimplexApproach1(iNumberOfVertices, graphs, iNumberOfThreads, seed);
		ColouredGraph mimicGraph = generatorSimplex.generateGraph();
		System.out.println("Mimic graph statistics:");
		System.out.println(mimicGraph.getVertices().size());
		System.out.println(mimicGraph.getEdges().size());
		ObjectArrayList<BitSet> edgeColours = mimicGraph.getEdgeColours();
		Object[] arredgeColours = edgeColours.toArray();
		int numOfnullEdges = 0;
		for (Object objEdge :arredgeColours) {
			if (objEdge == null) {
				numOfnullEdges++;
			}
		}
		
		System.out.println("Number of null edges: " + numOfnullEdges);
		//System.out.println(edgeColours);
		
	}
	
	@Test
	public void NodeIteratorMetricTestLGD() {
		NodeIteratorMetric metricNode = new NodeIteratorMetric();
		String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";
		
		IDatasetManager mDatasetManager = new LinkedGeoDataset();
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(LINKED_GEO_DATASET_FOLDER_PATH);
		double apply = metricNode.apply(graphs[0]);
		System.out.println(apply);
	}
}
