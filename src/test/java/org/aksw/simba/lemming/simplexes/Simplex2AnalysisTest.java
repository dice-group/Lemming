package org.aksw.simba.lemming.simplexes;

import java.util.Random;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class includes tests to evaluate the classes Simplex2Analysis and Simplex2Distribution classes.
 */
public class Simplex2AnalysisTest {
	private NodeIteratorMetric metric;
	
	
	/**
	 * This test case executes the logic of Simplex2Analysis for first dataset of SWDF. 
	 */
	//@Test
	public void simplex2HeadColoTailColoCounts() {
		
		//SWDF dataset
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 1;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
//		System.out.println("Before removing edges");
//		IntSet edges = graphs[0].getEdges();
//		for (int edgeId: edges) {
//			System.out.println(edgeId + " - " + graphs[0].getPropertyURI(graphs[0].getEdgeColour(edgeId)));
//		}
//		
//		removeTypeEdgesFromGraphs(graphs);
//		
//		System.out.println("After removing edges");
//		edges = graphs[0].getEdges();
//		for (int edgeId: edges) {
//			System.out.println(edgeId + " - " + graphs[0].getPropertyURI(graphs[0].getEdgeColour(edgeId)));
//		}
		
		// call metric
		callMetricToGetTriangleInformation(graphs);
		
		// get common edge ids for triangle
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIdsTriangle;
		mGraphsCommonEdgesIdsTriangle = metric.getmGraphsCommonEdgesIds();
		
		Simplex2Analysis simplex2Obj = new Simplex2Analysis();
		simplex2Obj.analyze(graphs, mGraphsCommonEdgesIdsTriangle);
		
		System.out.println(simplex2Obj.getmHeadColoCount());
		System.out.println(simplex2Obj.getmHeadColoTailColoCount());
		
		System.out.println();
		
	}
	
	/**
	 * This testcase executes the logic of Simplex2Analysis class for custom created graphs.
	 */
	//@Test
	public void simplex2HeadAndTailColoCountDummyGraph() {
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file4.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file5.n3", "N3");
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
//		System.out.println("Before removing edges");
//		IntSet edges = graphs[0].getEdges();
//		for (int edgeId: edges) {
//			System.out.println(edgeId + " - " + graphs[0].getPropertyURI(graphs[0].getEdgeColour(edgeId)));
//		}
		
		//removeTypeEdgesFromGraphs(graphs);
		
//		System.out.println("After removing edges");
//		edges = graphs[0].getEdges();
//		for (int edgeId: edges) {
//			System.out.println(edgeId + " - " + graphs[0].getPropertyURI(graphs[0].getEdgeColour(edgeId)));
//		}
		
		// call metric
		callMetricToGetTriangleInformation(graphs);
		
		// get common edge ids for triangle
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIdsTriangle;
		mGraphsCommonEdgesIdsTriangle = metric.getmGraphsCommonEdgesIds();
		//System.out.println(mGraphsCommonEdgesIdsTriangle);
		
		Simplex2Analysis simplex2Obj = new Simplex2Analysis();
		simplex2Obj.analyze(graphs, mGraphsCommonEdgesIdsTriangle);
		
		System.out.println(simplex2Obj.getmHeadColoCount());
		System.out.println(simplex2Obj.getmHeadColoTailColoCount());
		
		System.out.println();
	}
	
	/**
	 * This test case initially calls Simplex2Analysis class to compute counts of Head color and counts for mapping of Head color and Tail Color.
	 */
	//@Test
	public void simplex2AnalysisAndDistributionTest() {
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		mRandom = new Random();
		
		//ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file4.n3", "N3");
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file5.n3", "N3");
		
		ColouredGraph graphs[] = new ColouredGraph[1];
		graphs[0] = graph;
		
		System.out.println("Computing metric: ");
		// call metric
		callMetricToGetTriangleInformation(graphs);
		
		// get common edge ids for triangle
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIdsTriangle;
		mGraphsCommonEdgesIdsTriangle = metric.getmGraphsCommonEdgesIds();
		//System.out.println(mGraphsCommonEdgesIdsTriangle);
		
		Simplex2Analysis simplex2Obj = new Simplex2Analysis();
		simplex2Obj.analyze(graphs, mGraphsCommonEdgesIdsTriangle);
		System.out.println("Maps computed using Simplex2Analysis class: ");
		System.out.println(simplex2Obj.getmHeadColoCount());
		System.out.println(simplex2Obj.getmHeadColoTailColoCount());
		
		System.out.println("Creating instances for Simpelx2Distribution class");
		Simplex2Distribution simplex2distObj = new Simplex2Distribution(metric.getmColoCountVertConnectedToTriangle(), simplex2Obj.getmHeadColoCount(), simplex2Obj.getmHeadColoTailColoCount(), 1, mRandom);
		
		System.out.println("Possible Head colors: ");
		OfferedItemByRandomProb<BitSet> potentialHeadColoProposer = simplex2distObj.getPotentialHeadColoProposer();
		System.out.println(potentialHeadColoProposer.getPotentialItem());
		System.out.println(potentialHeadColoProposer.getPotentialItem());
		System.out.println(potentialHeadColoProposer.getPotentialItem());
		System.out.println(potentialHeadColoProposer.getPotentialItem());
		
		System.out.println("Get possible Tail colors: ");
		BitSet headColo = potentialHeadColoProposer.getPotentialItem();
		System.out.println(simplex2distObj.proposeVertColo(headColo));
		System.out.println(simplex2distObj.proposeVertColo(headColo));
		System.out.println(simplex2distObj.proposeVertColo(headColo));
		System.out.println(simplex2distObj.proposeVertColo(headColo));
		
		
		
	}
	
	/**
	 * Same as above test case for SWDF dataset.
	 */
	@Test
	public void simplex2AnalysisAndDistributionTestSWDF() {
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 1;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		System.out.println("Computing metric: ");
		// call metric
		callMetricToGetTriangleInformation(graphs);
		
		// get common edge ids for triangle
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIdsTriangle;
		mGraphsCommonEdgesIdsTriangle = metric.getmGraphsCommonEdgesIds();
		//System.out.println(mGraphsCommonEdgesIdsTriangle);
		
		Simplex2Analysis simplex2Obj = new Simplex2Analysis();
		simplex2Obj.analyze(graphs, mGraphsCommonEdgesIdsTriangle);
		System.out.println("Maps computed using Simplex2Analysis class: ");
		System.out.println(simplex2Obj.getmHeadColoCount());
		System.out.println(simplex2Obj.getmHeadColoTailColoCount());
		
		System.out.println("Creating instances for Simpelx2Distribution class");
		Simplex2Distribution simplex2distObj = new Simplex2Distribution(metric.getmColoCountVertConnectedToTriangle(), simplex2Obj.getmHeadColoCount(), simplex2Obj.getmHeadColoTailColoCount(), 1, mRandom);
		
		System.out.println("Possible Head colors: ");
		OfferedItemByRandomProb<BitSet> potentialHeadColoProposer = simplex2distObj.getPotentialHeadColoProposer();
		System.out.println(potentialHeadColoProposer.getPotentialItem());
		System.out.println(potentialHeadColoProposer.getPotentialItem());
		System.out.println(potentialHeadColoProposer.getPotentialItem());
		System.out.println(potentialHeadColoProposer.getPotentialItem());
		
		System.out.println("Get possible Tail colors: ");
		BitSet headColo = potentialHeadColoProposer.getPotentialItem();
		System.out.println(simplex2distObj.proposeVertColo(headColo));
		System.out.println(simplex2distObj.proposeVertColo(headColo));
		System.out.println(simplex2distObj.proposeVertColo(headColo));
		System.out.println(simplex2distObj.proposeVertColo(headColo));
		
		
		
	}

	private void callMetricToGetTriangleInformation(ColouredGraph[] origGrphs) {
		// Initialize metric, which will collect triangles found in Input graphs.
		metric = new NodeIteratorMetric();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
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

}
