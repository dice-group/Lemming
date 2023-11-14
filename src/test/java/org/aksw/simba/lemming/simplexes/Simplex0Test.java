package org.aksw.simba.lemming.simplexes;

import java.util.Map;
import java.util.Random;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

public class Simplex0Test {
	
	/**
	 * This test case executes the logic of Simplex0Analysis for first dataset of SWDF. 
	 */
	@Test
	public void analysisClassTestSWDF() {
		
		//SWDF dataset
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 2;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
//		System.out.println("Before removing edges");
//		IntSet edges = graphs[0].getEdges();
//		for (int edgeId: edges) {
//			System.out.println(edgeId + " - " + graphs[0].getPropertyURI(graphs[0].getEdgeColour(edgeId)));
//		}
//		
		removeTypeEdgesFromGraphs(graphs);
//		
//		System.out.println("After removing edges");
//		edges = graphs[0].getEdges();
//		for (int edgeId: edges) {
//			System.out.println(edgeId + " - " + graphs[0].getPropertyURI(graphs[0].getEdgeColour(edgeId)));
//		}
		
		Simplex0Analysis simplex0Obj = new Simplex0Analysis();
		simplex0Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex = simplex0Obj.getmColoCount0Simplex();
		Map<Integer, Integer> mGraphIdNumberOf0SimplexVertices = simplex0Obj.getmGraphIdNumberOf0SimplexVertices();
		System.out.println();
		
	}
	
	/**
	 * This testcase executes the logic of Simplex0Analysis class for custom created graphs. (Negative test case)
	 */
	//@Test
	public void analysisClassTestCustomInput() {
		
		//Negative case. (Input graphs do not have isolated 1-simplexes)
		//ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file4.n3", "N3");
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file6.n3", "N3");
		System.out.println("Information about graph: ");
		
		IntSet verticesGraph = graph.getVertices();
		for (int vertexId: verticesGraph) {
			System.out.println("Vertex ID: " + vertexId + ", Color: " + graph.getVertexColour(vertexId));
		}
		
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
		removeTypeEdgesFromGraphs(graphs);
		
		Simplex0Analysis simplex0Obj = new Simplex0Analysis();
		simplex0Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex = simplex0Obj.getmColoCount0Simplex();
		Map<Integer, Integer> mGraphIdNumberOf0SimplexVertices = simplex0Obj.getmGraphIdNumberOf0SimplexVertices();
		System.out.println();
		
		
	}
	
	/**
	 * This testcase executes the logic of Simplex0Analysis class for custom created graphs. (Positive test case)
	 */
	//@Test
	public void analysisClassTestCustomInput2() {
		
		ColouredGraph graph = IOHelper.readGraphFromResource("simplex0.n3", "N3");
		System.out.println("Information about graph: ");
		
		IntSet verticesGraph = graph.getVertices();
		for (int vertexId: verticesGraph) {
			System.out.println("Vertex ID: " + vertexId + ", Color: " + graph.getVertexColour(vertexId));
		}
		
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
		removeTypeEdgesFromGraphs(graphs);
		
		Simplex0Analysis simplex0Obj = new Simplex0Analysis();
		simplex0Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex = simplex0Obj.getmColoCount0Simplex();
		Map<Integer, Integer> mGraphIdNumberOf0SimplexVertices = simplex0Obj.getmGraphIdNumberOf0SimplexVertices();
		System.out.println();
		
		
	}
	
	private void removeTypeEdgesFromGraphs(ColouredGraph[] origGrphs) {
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				
				// get rdf type edge
				BitSet rdfTypePropertyColour = graph.getRDFTypePropertyColour();
				
				// get all edges
				IntSet allEdges = graph.getEdges();
		
				//Iterate over all edges
				for(int edgeId:allEdges) {
					// remove if rdf type edge
					if (graph.getEdgeColour(edgeId).equals(rdfTypePropertyColour)) {
						graph.removeEdge(edgeId);
					}
				}
			}
		}
	}
	
	/**
	 * This test case initially calls Simplex0Analysis class to compute counts of Vertex colors, which is passed as a parameter to Simplex0Distribution class for generating color proposer for vertices.
	 */
	//@Test
	public void simplex0AnalysisAndDistributionTest() {
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		ColouredGraph graph = IOHelper.readGraphFromResource("simplex0.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file5.n3", "N3");
		
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
		removeTypeEdgesFromGraphs(graphs);		
		
		Simplex0Analysis simplex0Obj = new Simplex0Analysis();
		simplex0Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex = simplex0Obj.getmColoCount0Simplex();
		Map<Integer, Integer> mGraphIdNumberOf0SimplexVertices = simplex0Obj.getmGraphIdNumberOf0SimplexVertices();
		System.out.println("Maps computed for simplex0 distributions: ");
		System.out.println("Graph Id -> Number of 0-simplexes: " + mGraphIdNumberOf0SimplexVertices);
		System.out.println("Vertex color -> Number of 0-simplexes: " + mColoCount0Simplex);
		
		System.out.println("Creating instances for Simpelx0Distribution class");
		Simplex0Distribution simplex0distObj = new Simplex0Distribution(simplex0Obj.getmColoCount0Simplex(), 1, mRandom);
		
		System.out.println("Possible Vertex colors: ");
		OfferedItemByRandomProb<BitSet> vertColoProposer = simplex0distObj.getPotentialColoProposer();
		System.out.println(vertColoProposer.getPotentialItem());
		System.out.println(vertColoProposer.getPotentialItem());
		System.out.println(vertColoProposer.getPotentialItem());
		System.out.println(vertColoProposer.getPotentialItem());
		
	}
	
	/**
	 * Same as above test case for SWDF dataset.
	 */
	//@Test
	public void simplex0AnalysisAndDistributionTestSWDF() {
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 2;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		removeTypeEdgesFromGraphs(graphs);		
		
		Simplex0Analysis simplex0Obj = new Simplex0Analysis();
		simplex0Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex = simplex0Obj.getmColoCount0Simplex();
		Map<Integer, Integer> mGraphIdNumberOf0SimplexVertices = simplex0Obj.getmGraphIdNumberOf0SimplexVertices();
		System.out.println("Maps computed for simplex0 distributions: ");
		System.out.println("Graph Id -> Number of 0-simplexes: " + mGraphIdNumberOf0SimplexVertices);
		System.out.println("Vertex color -> Number of 0-simplexes: " + mColoCount0Simplex);
		
		System.out.println("Creating instances for Simpelx0Distribution class");
		Simplex0Distribution simplex0distObj = new Simplex0Distribution(simplex0Obj.getmColoCount0Simplex(), 2, mRandom);
		
		System.out.println("Possible Vertex colors: ");
		OfferedItemByRandomProb<BitSet> vertColoProposer = simplex0distObj.getPotentialColoProposer();
		System.out.println(vertColoProposer.getPotentialItem());
		System.out.println(vertColoProposer.getPotentialItem());
		System.out.println(vertColoProposer.getPotentialItem());
		System.out.println(vertColoProposer.getPotentialItem());
		
	}

}
