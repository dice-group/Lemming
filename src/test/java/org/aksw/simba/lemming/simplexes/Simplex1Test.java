package org.aksw.simba.lemming.simplexes;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

public class Simplex1Test {
private NodeIteratorMetric metric;
	
	
	/**
	 * This test case executes the logic of Simplex1Analysis for first dataset of SWDF. 
	 */
	//@Test
	public void analysisClassTestSWDF() {
		
		//SWDF dataset
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 3;
		
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
		
		Simplex1Analysis simplex1Obj = new Simplex1Analysis();
		simplex1Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsFor1Simplex = simplex1Obj.getmGraphIdEdgeIdsFor1Simplex();
		Map<Integer, Integer> mGraphIdNumberOf1SimplexEdges = simplex1Obj.getmGraphIdNumberOf1SimplexEdges();
		Map<Integer, Integer> getmGraphIdNumberOf1SimplexVertices = simplex1Obj.getmGraphIdNumberOf1SimplexVertices();
		ObjectDoubleOpenHashMap<BitSet> getmHeadColoCount1Simplex = simplex1Obj.getmHeadColoCount1Simplex();
		ObjectObjectOpenHashMap<BitSet,ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCount = simplex1Obj.getmHeadColoTailColoCount();
		ObjectObjectOpenHashMap<BitSet,ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCountConnected = simplex1Obj.getmHeadColoTailColoCountConnected();
		ObjectDoubleOpenHashMap<BitSet> getmVertColoCountConnected1Simplex = simplex1Obj.getmVertColoCountConnected1Simplex();
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
	 * This testcase executes the logic of Simplex1Analysis class for custom created graphs. (Negative test case)
	 */
	//@Test
	public void analysisClassTestCustomInput() {
		
		//Negative case. (Input graphs do not have isolated 1-simplexes)
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file4.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file5.n3", "N3");
		System.out.println("Information about graph: ");
		
		IntSet verticesGraph = graph.getVertices();
		for (int vertexId: verticesGraph) {
			System.out.println("Vertex ID: " + vertexId + ", Color: " + graph.getVertexColour(vertexId));
		}
		
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
		removeTypeEdgesFromGraphs(graphs);
		
		Simplex1Analysis simplex1Obj = new Simplex1Analysis();
		simplex1Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsFor1Simplex = simplex1Obj.getmGraphIdEdgeIdsFor1Simplex();
		Map<Integer, Integer> mGraphIdNumberOf1SimplexEdges = simplex1Obj.getmGraphIdNumberOf1SimplexEdges();
		Map<Integer, Integer> getmGraphIdNumberOf1SimplexVertices = simplex1Obj.getmGraphIdNumberOf1SimplexVertices();
		ObjectDoubleOpenHashMap<BitSet> getmHeadColoCount1Simplex = simplex1Obj.getmHeadColoCount1Simplex();
		ObjectObjectOpenHashMap<BitSet,ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCount = simplex1Obj.getmHeadColoTailColoCount();
		ObjectObjectOpenHashMap<BitSet,ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCountConnected = simplex1Obj.getmHeadColoTailColoCountConnected();
		ObjectDoubleOpenHashMap<BitSet> getmVertColoCountConnected1Simplex = simplex1Obj.getmVertColoCountConnected1Simplex();
		System.out.println();
		
		
	}
	
	/**
	 * This testcase executes the logic of Simplex1Analysis class for custom created graphs. (Positive test case)
	 */
	@Test
	public void analysisClassTestCustomInput2() {
		
		//Negative case. (Input graphs do not have isolated 1-simplexes)
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file6.n3", "N3");
		System.out.println("Information about graph: ");
		
		IntSet verticesGraph = graph.getVertices();
		for (int vertexId: verticesGraph) {
			System.out.println("Vertex ID: " + vertexId + ", Color: " + graph.getVertexColour(vertexId));
		}
		
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
		removeTypeEdgesFromGraphs(graphs);		
		
		Simplex1Analysis simplex1Obj = new Simplex1Analysis();
		simplex1Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsFor1Simplex = simplex1Obj.getmGraphIdEdgeIdsFor1Simplex();
		Map<Integer, Integer> mGraphIdNumberOf1SimplexEdges = simplex1Obj.getmGraphIdNumberOf1SimplexEdges();
		Map<Integer, Integer> getmGraphIdNumberOf1SimplexVertices = simplex1Obj.getmGraphIdNumberOf1SimplexVertices();
		ObjectDoubleOpenHashMap<BitSet> getmHeadColoCount1Simplex = simplex1Obj.getmHeadColoCount1Simplex();
		ObjectObjectOpenHashMap<BitSet,ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCount = simplex1Obj.getmHeadColoTailColoCount();
		ObjectObjectOpenHashMap<BitSet,ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCountConnected = simplex1Obj.getmHeadColoTailColoCountConnected();
		ObjectDoubleOpenHashMap<BitSet> getmVertColoCountConnected1Simplex = simplex1Obj.getmVertColoCountConnected1Simplex();
		System.out.println();
		
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		Simplex1Distribution simplex1Distribution = new Simplex1Distribution(simplex1Obj.getmHeadColoCount1Simplex(), simplex1Obj.getmHeadColoTailColoCount(), 1, mRandom);
		OfferedItemByRandomProb<BitSet> potentialIsoSelfLoopColoProposer = simplex1Distribution.getPotentialHeadColoProposer();
		System.out.println(potentialIsoSelfLoopColoProposer.getPotentialItem());
		System.out.println(potentialIsoSelfLoopColoProposer.getPotentialItem());
		
	}
	
	/**
	 * This test case checks the logic to find isolated self loop.
	 */
	//@Test
	public void isolatedSelfLoopTest1() {
		
		//Negative case. (Input graphs do not have isolated 1-simplexes)
		ColouredGraph graph = IOHelper.readGraphFromResource("isolatedselflooptest1.n3", "N3");
		System.out.println("Information about graph: ");
		
		IntSet verticesGraph = graph.getVertices();
		for (int vertexId: verticesGraph) {
			System.out.println("Vertex ID: " + vertexId + ", Color: " + graph.getVertexColour(vertexId));
		}
		
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
		removeTypeEdgesFromGraphs(graphs);		
		
		Simplex1Analysis simplex1Obj = new Simplex1Analysis();
		simplex1Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectDoubleOpenHashMap<BitSet> getmColoCountSelfLoop = simplex1Obj.getmColoCountSelfLoop();
		ObjectObjectOpenHashMap<Integer,IntSet> getmGraphIdEdgeIdsForSelfLoop = simplex1Obj.getmGraphIdEdgeIdsForSelfLoop();
		Map<Integer, Integer> getmGraphIdNumIsolatedSelfLoopEdges = simplex1Obj.getmGraphIdNumIsolatedSelfLoopEdges();
		Map<Integer, Integer> getmGraphIdNumIsolatedSelfLoopVertices = simplex1Obj.getmGraphIdNumIsolatedSelfLoopVertices();
		System.out.println();
		
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		Simplex1Distribution simplex1Distribution = new Simplex1Distribution(simplex1Obj.getmHeadColoCount1Simplex(), simplex1Obj.getmHeadColoTailColoCount(), 1, mRandom);
		simplex1Distribution.createIsoSelfLoopColoProposer(getmColoCountSelfLoop);
		OfferedItemByRandomProb<BitSet> potentialIsoSelfLoopColoProposer = simplex1Distribution.getPotentialIsoSelfLoopColoProposer();
		System.out.println(potentialIsoSelfLoopColoProposer.getPotentialItem());
		System.out.println(potentialIsoSelfLoopColoProposer.getPotentialItem());
		
	}
	
	/**
	 * This test case checks the logic to find isolated self loop.
	 */
	//@Test
	public void selfLoopInIso1SimplexTest1() {
		
		//Negative case. (Input graphs do not have isolated 1-simplexes)
		ColouredGraph graph = IOHelper.readGraphFromResource("selfloopin1simplex.n3", "N3");
		System.out.println("Information about graph: ");
		
		IntSet verticesGraph = graph.getVertices();
		for (int vertexId: verticesGraph) {
			System.out.println("Vertex ID: " + vertexId + ", Color: " + graph.getVertexColour(vertexId));
		}
		
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
		removeTypeEdgesFromGraphs(graphs);		
		
		Simplex1Analysis simplex1Obj = new Simplex1Analysis();
		simplex1Obj.analyze(graphs);
		
		// Checking below variables in debug mode
		ObjectDoubleOpenHashMap<BitSet> getmColoCountSelfLoop = simplex1Obj.getmColoCountSelfLoopIn1Simplex();
		ObjectObjectOpenHashMap<Integer,IntSet> getmGraphIdEdgeIdsForSelfLoop = simplex1Obj.getmGraphIdEdgeIdsForSelfLoopIn1Simplex();
		Map<Integer, Integer> getmGraphIdNumIsolatedSelfLoopEdges = simplex1Obj.getmGraphIdNumSelfLoopIn1SimplexEdges();
		System.out.println();
		
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		Simplex1Distribution simplex1Distribution = new Simplex1Distribution(simplex1Obj.getmHeadColoCount1Simplex(), simplex1Obj.getmHeadColoTailColoCount(), 1, mRandom);
		simplex1Distribution.createSelfLoopIn1SimplexColoProposer(getmColoCountSelfLoop);
		OfferedItemByRandomProb<BitSet> potentialIsoSelfLoopColoProposer = simplex1Distribution.getPotentialSelfLoopIn1SimplexColoProposer();
		System.out.println(potentialIsoSelfLoopColoProposer.getPotentialItem());
		System.out.println(potentialIsoSelfLoopColoProposer.getPotentialItem());
		
	}
	
}
