package org.aksw.simba.lemming.simplexes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

import it.unimi.dsi.fastutil.ints.IntSet;

public class EstimatedEdgesVerticesTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(EstimatedEdgesVerticesTest.class);
	
	/**
	 * NodeIterator metric object used in this class to collect information about triangles present in different input graphs. 
	 */
	NodeIteratorMetric metric;
	
	/**
	 * Estimated number of edges for all triangles.
	 */
	int estimatedEdgesTriangle;
	
	/**
	 * Estimated number of edges connecting vertices present in triangles and vertices not in triangles.
	 */
	int estimatedEdgesCommon;
	
	/**
	 * Estimated number of edges not linked to vertices in triangle.
	 */
	int estimatedEdgesNotTriangle;
	
	
	/**
	 * Estimated number of type edges not in triangle.
	 */
	int estimatedTypeEdgesNotTriangle;
	
	/**
	 * Estimated number of Vertices forming triangles
	 */
	int estimatedVerticesTriangle;
	
	/**
	 * Estimated number of vertices present in triangles and connected to 1-simplex.
	 */
	int estimatedVerticesCommon;
	
	/**
	 * Variable to store number of input graphs.
	 */
	private int iNoOfVersions;
	
	/**
	 * Estimated number of edges forming 1-simplexes
	 */
	private int estimatedEdges1Simplexes;
	
	/**
	 * Estimated number of Vertices forming 1-simplexes
	 */
	private int estimatedVertices1Simplexes;
	
	/**
	 * Estimated number of Vertices forming 0-simplexes
	 */
	private int estimatedVertices0Simplexes;
	
	/**
	 * Map for storing graphs along with their total count of edges. This count includes rdf type edges, which is required for estimating edges in the output graph.  
	 */
	private Map<Integer, Integer> mGraphEdges;
	
	/**
	 * Map for storing number of 1-simplex edges for different input graphs. Note: It considers 1-simplexes that are not connected to any other 1-simplexes.
	 */
	private Map<Integer, Integer> mGraphIdNumberOf1SimplexEdges;
	
	/**
	 * Map for storing number of vertices forming 1-simplexes for different input graphs. Note: Similar to above, this map also considers 1-simplexes that are not connected to any other 1-simplexes.
	 */
	private Map<Integer, Integer> mGraphIdNumberOf1SimplexVertices;
	
	/**
	 * Map for storing number of vertices forming 0-simplexes for different input graphs.
	 */
	private Map<Integer, Integer> mGraphIdNumberOf0SimplexVertices;
	
	@Test
	public void estimateEdgesTest() {
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 2;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		graphs= mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		// read input graph
		ColouredGraph graphsTest[] = new ColouredGraph[20];
		graphsTest[0] = graphs[0];
		graphsTest[1] = graphs[1];
		
		// Find total edges
		mGraphEdges = new HashMap<>();
		computeTotalEdgesInGraphs(graphsTest);
		
		callMetricToGetTriangleInformation(graphsTest);
		
		// remove type edges
		removeTypeEdgesFromGraphs(graphsTest);
		
		// 1-Simplexes calculations
		Simplex1Analysis simplex1Obj = new Simplex1Analysis();
		simplex1Obj.analyze(graphsTest);
		
		mGraphIdNumberOf1SimplexEdges = simplex1Obj.getmGraphIdNumberOf1SimplexEdges();
		mGraphIdNumberOf1SimplexVertices = simplex1Obj.getmGraphIdNumberOf1SimplexVertices();
		
		// 0-simplexes calculations
		Simplex0Analysis simplex0Obj = new Simplex0Analysis();
		simplex0Obj.analyze(graphsTest);
		mGraphIdNumberOf0SimplexVertices = simplex0Obj.getmGraphIdNumberOf0SimplexVertices();
		
		iNoOfVersions = numOfGraphsToAnalyze;
		estimateNoEdgesSimplexes(graphsTest, 45420);
		
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
	
	private void computeTotalEdgesInGraphs(ColouredGraph[] origGrphs) {
		
		// temporary variable to track graph number
		int graphId = 1;
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				int edgesCount = graph.getEdges().size();
				mGraphEdges.put(graphId, edgesCount);
				graphId++;
			}
		}
	}
	
	protected int estimateNoEdgesSimplexes(ColouredGraph[] origGrphs, int noVertices) {
		LOGGER.info("Estimate the number of edges in the new graph.");
		
		
		
		int estimatedEdges = 0;
		
		// initialize estimated edges for different cases
		estimatedEdgesTriangle = 0;
		estimatedEdgesCommon = 0;
		estimatedEdgesNotTriangle = 0;
		//estimatedTypeEdgesNotTriangle = 0;
		estimatedEdges1Simplexes = 0; // 1-simplex
		
		// initialize estimated vertices for different cases
		estimatedVerticesTriangle = 0;
		estimatedVerticesCommon = 0;
		estimatedVertices1Simplexes = 0;
		estimatedVertices0Simplexes = 0;
		
		// Logic to check number of input graphs
		iNoOfVersions = 0;
		for (ColouredGraph tempGrph: origGrphs) {
			if(tempGrph!=null) {
				iNoOfVersions = iNoOfVersions + 1;
			}
		}
		
		if (origGrphs != null && origGrphs.length > 0) {
		
			double noEdges = 0;
			
			// temporary variable to track graph number
			int graphId = 1;
			
			// variables to track edge density for different cases
			double noEdgesTriangles = 0;
			double noEdgesCommon = 0;
			double noEdgesNotTriangle = 0;
			//double noTypeEdgesNotTriangle = 0;
			double noEdges1Simplexes = 0;
			
			// variables to track vertices information
			double noVerticesTriangles = 0;
			double noVerticesCommon = 0;
			double noVertices1Simplexes = 0;
			double noVertices0Simplexes = 0;
			
			
			for (ColouredGraph graph : origGrphs) {
				
				if (graph!= null) {
				
					// compute triangles information for input graph
					//metric.computeTriangles(graph); // commenting this call, metric is invoked by separate method
					LOGGER.info("Edge stats computed for graph " + graphId);
					
					//*******************Computation for edges**************************//
					
					// computation for total number of edges
					int iNoEdges = mGraphEdges.get(graphId); // get total number of edges including the rdf type edges//graph.getEdges().size();
					int iNoVertices = graph.getVertices().size();
					noEdges += iNoEdges / (iNoVertices * 1.0);
					
					// get hashmap consisting of computed statistics
					Map<Integer, List<Integer>> mstatsVertEdges = metric.getMstatsVertEdges();
					List<Integer> statsList = mstatsVertEdges.get(graphId);
					
					int iNoEdgesTriangles = statsList.get(1); // get number of edges for triangle
					noEdgesTriangles += iNoEdgesTriangles / (iNoVertices * 1.0);
					LOGGER.info("Number of edges in triangle: " + iNoEdgesTriangles);
					LOGGER.info("Edge density [Number of edges in triangle]: " + noEdgesTriangles);
					
					int iNoEdgesCommon = statsList.get(3); // get number of edges connecting vertices present in triangle and vertices not in triangle
					noEdgesCommon += iNoEdgesCommon / (iNoVertices * 1.0);
					LOGGER.info("Number of common edges: " + iNoEdgesCommon);
					LOGGER.info("Edge density [Number of common edges]: " + noEdgesCommon);
					
					int iNoEdgesNotTriangle = statsList.get(5); // get number of edges not linked to vertices of triangles
					noEdgesNotTriangle += iNoEdgesNotTriangle / (iNoVertices * 1.0);
					LOGGER.info("Number of edges not linked to triangle: " + iNoEdgesNotTriangle);
					LOGGER.info("Edge density [Number of edges not linked to triangle]: " + noEdgesNotTriangle);

					int iNoEdges1Simplexes = mGraphIdNumberOf1SimplexEdges.get(graphId); 
					noEdges1Simplexes +=iNoEdges1Simplexes / (iNoVertices * 1.0);
					LOGGER.info("Number of edges for 1-simplexes: " + iNoEdges1Simplexes);
					LOGGER.info("Edge density [1-simplexes]: " + noEdges1Simplexes);
					
					/*
					 * Not computing type information. Thus, commenting below statements.
					int iNoTypeEdgesNotTriangle = statsList.get(6); // get number of type edges not part of triangle
					noTypeEdgesNotTriangle += iNoTypeEdgesNotTriangle / (iNoVertices * 1.0);
					LOGGER.debug("Number of type edges not in triangle: " + iNoTypeEdgesNotTriangle);
					LOGGER.debug("Edge density [Number of type edges not in triangle]: " + noTypeEdgesNotTriangle);
					*/
					
					//********************Computation for vertices**************************//
					
					int iNoVerticesTriangles = statsList.get(0); // get number of vertices for triangle
					noVerticesTriangles += iNoVerticesTriangles / (iNoVertices * 1.0);
					LOGGER.info("Percentage of vertices in triangle: " + noVerticesTriangles);
					
					int iNoVerticesCommon = statsList.get(2);
					noVerticesCommon += iNoVerticesCommon / (iNoVertices * 1.0);
					LOGGER.info("Percentage of common vertices: " + noVerticesCommon);
					
					int iNoVertices1Simplexes = mGraphIdNumberOf1SimplexVertices.get(graphId);
					noVertices1Simplexes += iNoVertices1Simplexes / (iNoVertices * 1.0);
					LOGGER.info("Percentage of vertices for 1-simplexes: " + noVertices1Simplexes);
					
					int iNoVertices0Simplexes = mGraphIdNumberOf0SimplexVertices.get(graphId);
					noVertices0Simplexes += iNoVertices0Simplexes / (iNoVertices * 1.0);
					LOGGER.info("Percentage of vertices for 0-simplexes: " + noVertices0Simplexes);
					
					
					graphId++;
				}
			}
			
			//*******************Estimating edges**************************//
			
			// computation for total number of edges
			noEdges *= noVertices;
			noEdges /= iNoOfVersions;
			estimatedEdges = (int) Math.round(noEdges);
			LOGGER.info("Total number of estimated edges: " + estimatedEdges);
			
			//computation of edges for different cases
			noEdgesTriangles *= noVertices;
			noEdgesTriangles /= iNoOfVersions;
			estimatedEdgesTriangle = (int) Math.round(noEdgesTriangles);
			LOGGER.info("Estimated number of edges in triangle: " + estimatedEdgesTriangle);
			
			noEdgesCommon *= noVertices;
			noEdgesCommon /= iNoOfVersions;
			estimatedEdgesCommon = (int) Math.round(noEdgesCommon);
			LOGGER.info("Estimated number of common edges: " + estimatedEdgesCommon);
			
			noEdgesNotTriangle *= noVertices;
			noEdgesNotTriangle /= iNoOfVersions;
			estimatedEdgesNotTriangle = (int) Math.round(noEdgesNotTriangle);
			LOGGER.info("Estimated number of edges not linked to triangles: " + estimatedEdgesNotTriangle);
			
			noEdges1Simplexes *= noVertices;
			noEdges1Simplexes /= iNoOfVersions;
			estimatedEdges1Simplexes = (int) Math.round(noEdges1Simplexes);
			LOGGER.info("Estimated number of edges [1-simplexes]: " + estimatedEdges1Simplexes);
			
			/*
			 * Not computing type information. Thus, commenting below statements.
			noTypeEdgesNotTriangle *= noVertices;
			noTypeEdgesNotTriangle /= iNoOfVersions;
			estimatedTypeEdgesNotTriangle = (int) Math.round(noTypeEdgesNotTriangle);
			LOGGER.debug("Estimated number of type edges not in triangle: " + estimatedTypeEdgesNotTriangle);
			*/
			
			LOGGER.warn("Estimated the number of edges in the new graph is " + estimatedEdges);
			
			
			//*******************Estimating vertices**************************//
			
			// computation of vertices for different cases
			noVerticesTriangles *= noVertices;
			noVerticesTriangles /= iNoOfVersions;
			estimatedVerticesTriangle = (int) Math.round(noVerticesTriangles);
			LOGGER.info("Estimated number of vertices forming triangle: " + estimatedVerticesTriangle);
			
			noVerticesCommon *= noVertices;
			noVerticesCommon /= iNoOfVersions;
			estimatedVerticesCommon = (int) Math.round(noVerticesCommon);
			LOGGER.info("Estimated number of common vertices: " + estimatedVerticesCommon);
			
			noVertices1Simplexes *= noVertices;
			noVertices1Simplexes /= iNoOfVersions;
			estimatedVertices1Simplexes = (int) Math.round(noVertices1Simplexes);
			LOGGER.info("Estimated number of vertices [1-simplexes]: " + estimatedVertices1Simplexes);
			
			noVertices0Simplexes *= noVertices;
			noVertices0Simplexes /= iNoOfVersions;
			estimatedVertices0Simplexes = (int) Math.round(noVertices0Simplexes);
			LOGGER.info("Estimated number of vertices [0-simplexes]: " + estimatedVertices0Simplexes);
			
			
		} else {
			LOGGER.warn("The array of original graphs is empty!");
		}
		return estimatedEdges;
	}
	
//	int estimateNoEdges(ColouredGraph[] origGrphs, int noVertices) {
//		LOGGER.info("Estimate the number of edges in the new graph.");
//		
//		metric = new NodeIteratorMetric();
//		
//		int estimatedEdges = 0;
//		
//		// initialize estimated edges for different cases
//		estimatedEdgesTriangle = 0;
//		estimatedEdgesCommon = 0;
//		estimatedEdgesNotTriangle = 0;
//		//estimatedTypeEdgesNotTriangle = 0;
//		
//		// initialize estimated vertices for different cases
//		estimatedVerticesTriangle = 0;
//		estimatedVerticesCommon = 0;
//		
//		int iNoOfVersions = 0;
//		for (ColouredGraph tempGrph: origGrphs) {
//			if(tempGrph!=null) {
//				iNoOfVersions = iNoOfVersions + 1;
//			}
//		}
//		
//		
//		if (origGrphs != null && origGrphs.length > 0) {
//			
//			double noEdges = 0;
//			
//			// temporary variable to track graph number
//			int graphId = 1;
//			
//			// variables to track edge density for different cases
//			double noEdgesTriangles = 0;
//			double noEdgesCommon = 0;
//			double noEdgesNotTriangle = 0;
//			//double noTypeEdgesNotTriangle = 0;
//			
//			// variables to track vertices information
//			double noVerticesTriangles = 0;
//			double noVerticesCommon = 0;
//			
//			
//			for (ColouredGraph graph : origGrphs) {
//				
//				if (graph!= null) {
//				
//					// compute triangles information for input graph
//					metric.computeTriangles(graph);
//					LOGGER.info("Triangle stats computed for graph " + graphId);
//					
//					//*******************Computation for edges**************************//
//					
//					// computation for total number of edges
//					int iNoEdges = graph.getEdges().size();
//					int iNoVertices = graph.getVertices().size();
//					noEdges += iNoEdges / (iNoVertices * 1.0);
//					
//					// get hashmap consisting of computed statistics
//					Map<Integer, List<Integer>> mstatsVertEdges = metric.getMstatsVertEdges();
//					List<Integer> statsList = mstatsVertEdges.get(graphId);
//					
//					int iNoEdgesTriangles = statsList.get(1); // get number of edges for triangle
//					noEdgesTriangles += iNoEdgesTriangles / (iNoVertices * 1.0);
//					LOGGER.info("Number of edges in triangle: " + iNoEdgesTriangles);
//					LOGGER.info("Edge density [Number of edges in triangle]: " + noEdgesTriangles);
//					
//					int iNoEdgesCommon = statsList.get(3); // get number of edges connecting vertices present in triangle and vertices not in triangle
//					noEdgesCommon += iNoEdgesCommon / (iNoVertices * 1.0);
//					LOGGER.info("Number of common edges: " + iNoEdgesCommon);
//					LOGGER.info("Edge density [Number of common edges]: " + noEdgesCommon);
//					
//					int iNoEdgesNotTriangle = statsList.get(5); // get number of edges not linked to vertices of triangles
//					noEdgesNotTriangle += iNoEdgesNotTriangle / (iNoVertices * 1.0);
//					LOGGER.info("Number of edges not linked to triangle: " + iNoEdgesNotTriangle);
//					LOGGER.info("Edge density [Number of edges not linked to triangle]: " + noEdgesNotTriangle);
//					
//					/*
//					 * Not computing type information. Thus, commenting below statements.
//					int iNoTypeEdgesNotTriangle = statsList.get(6); // get number of type edges not part of triangle
//					noTypeEdgesNotTriangle += iNoTypeEdgesNotTriangle / (iNoVertices * 1.0);
//					LOGGER.debug("Number of type edges not in triangle: " + iNoTypeEdgesNotTriangle);
//					LOGGER.debug("Edge density [Number of type edges not in triangle]: " + noTypeEdgesNotTriangle);
//					*/
//					
//					//********************Computation for vertices**************************//
//					
//					int iNoVerticesTriangles = statsList.get(0); // get number of vertices for triangle
//					noVerticesTriangles += ((iNoVerticesTriangles / (iNoVertices * 1.0))*1);
//					LOGGER.info("Percentage of vertices in triangle: " + noVerticesTriangles);
//					
//					int iNoVerticesCommon = statsList.get(2);
//					noVerticesCommon += ((iNoVerticesCommon / (iNoVertices * 1.0))*1);
//					LOGGER.info("Percentage of common vertices: " + noVerticesCommon);
//					
//					
//					graphId++;
//				}
//			}
//			
//			//*******************Estimating edges**************************//
//			
//			// computation for total number of edges
//			noEdges *= noVertices;
//			noEdges /= iNoOfVersions;
//			estimatedEdges = (int) Math.round(noEdges);
//			LOGGER.info("Total number of estimated edges: " + estimatedEdges);
//			
//			//computation of edges for different cases
//			noEdgesTriangles *= noVertices;
//			noEdgesTriangles /= iNoOfVersions;
//			estimatedEdgesTriangle = (int) Math.round(noEdgesTriangles);
//			LOGGER.info("Estimated number of edges in triangle: " + estimatedEdgesTriangle);
//			
//			noEdgesCommon *= noVertices;
//			noEdgesCommon /= iNoOfVersions;
//			estimatedEdgesCommon = (int) Math.round(noEdgesCommon);
//			LOGGER.info("Estimated number of common edges: " + estimatedEdgesCommon);
//			
//			noEdgesNotTriangle *= noVertices;
//			noEdgesNotTriangle /= iNoOfVersions;
//			estimatedEdgesNotTriangle = (int) Math.round(noEdgesNotTriangle);
//			LOGGER.info("Estimated number of edges not linked to triangles: " + estimatedEdgesNotTriangle);
//			
//			/*
//			 * Not computing type information. Thus, commenting below statements.
//			noTypeEdgesNotTriangle *= noVertices;
//			noTypeEdgesNotTriangle /= iNoOfVersions;
//			estimatedTypeEdgesNotTriangle = (int) Math.round(noTypeEdgesNotTriangle);
//			LOGGER.debug("Estimated number of type edges not in triangle: " + estimatedTypeEdgesNotTriangle);
//			*/
//			
//			LOGGER.warn("Estimated the number of edges in the new graph is " + estimatedEdges);
//			
//			
//			//*******************Estimating vertices**************************//
//			
//			// computation of vertices for different cases
//			System.out.println(noVerticesTriangles);
//			noVerticesTriangles *= noVertices;
//			noVerticesTriangles /= iNoOfVersions;
//			System.out.println(noVerticesTriangles);
//			estimatedVerticesTriangle = (int) Math.round(noVerticesTriangles);
//			LOGGER.info("Estimated number of vertices forming triangle: " + estimatedVerticesTriangle);
//			
//			System.out.println(noVerticesCommon);
//			noVerticesCommon *= noVertices;
//			noVerticesCommon /= iNoOfVersions;
//			System.out.println(noVerticesCommon);
//			estimatedVerticesCommon = (int) Math.round(noVerticesCommon);
//			LOGGER.info("Estimated number of common vertices: " + estimatedVerticesCommon);
//			
//			
//			
//		} else {
//			LOGGER.warn("The array of original graphs is empty!");
//		}
//		return estimatedEdges;
//	}

}
