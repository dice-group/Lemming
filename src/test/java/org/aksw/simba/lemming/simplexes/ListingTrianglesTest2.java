package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric2;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;


public class ListingTrianglesTest2 {
	
	//@Test
	public void getTriangles() {
		
		//ColouredGraph graph = IOHelper.readGraphFromResource("graph1.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("graph_loop.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("graph_loop_2.n3", "N3");
		ColouredGraph graph = IOHelper.readGraphFromResource("email-Eu-core.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file7.n3", "N3");
		
		System.out.println(graph.getRDFTypePropertyColour());
//		
		NodeIteratorMetric2 metric;
		metric = new NodeIteratorMetric2();
		metric.computeTriangles(graph);
//		
		System.out.println("Edges and triangle counts information (Resource Nodes):");
		ObjectObjectOpenHashMap<TriangleColours,double[]> getmTriangleColoursTriangleCountsEdgeCountsResourceNodes = metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes();
		System.out.println(getmTriangleColoursTriangleCountsEdgeCountsResourceNodes);
		
		
		// Logic to evaluate long values
//		Object[] keysTriColo = mTrianglesColoursEdgeCountsClassNodes.keys;
//		for (int i = 0; i < keysTriColo.length; i++) {
//			if (mTrianglesColoursEdgeCountsClassNodes.allocated[i]) {
//				int countOfEdges = mTrianglesColoursEdgeCountsClassNodes.get((TriangleColours) keysTriColo[i]);
//				System.out.println(countOfEdges);
//				long[] bitsA = ((TriangleColours) keysTriColo[i]).getA().bits;
//				long[] bitsB = ((TriangleColours) keysTriColo[i]).getB().bits;
//				long[] bitsC = ((TriangleColours) keysTriColo[i]).getC().bits;
//				
//				System.out.println(bitsA.toString());
//				System.out.println(bitsB.toString());
//				System.out.println(bitsC.toString());
//			}
//		}
		
		System.out.println("List edge ids for different cases: ");
		System.out.println(metric.getmGraphsCommonEdgesIds());
		
		System.out.println(metric.getmGraphsEdgesIdsTriangle());
//	
//		ObjectArrayList<BitSet> vertexColours = graph.getVertexColours();
//		System.out.println(graph.getVertexPalette().getMapOfURIAndColour());
//		for(int i=0; i< vertexColours.size(); i++) {
//			System.out.println(vertexColours.get(i).cardinality());
//		}
//		
//		System.out.println(isFirstSmallerthanSecond(vertexColours.get(0), vertexColours.get(1)));
//		System.out.println(isFirstSmallerthanSecond(vertexColours.get(1), vertexColours.get(1)));
		
	}
	
	public boolean isFirstSmallerthanSecond(BitSet temp1, BitSet temp2) {
    	BitSet clone = (BitSet)temp1.clone();
    	clone.xor(temp2); // Performing XOR operation to get first different bit
    	int firstDifferentBit = (int) (clone.length() - 1);
    	
    	// if both the BitSet are equal the XOR operation returns empty BitSet
    	if (clone.length() == 0)
    		return false;
    	
    	// temp1 is true at firstDifferentBit index if it is greater than temp2
    	return !temp1.get(firstDifferentBit);
    	
    }
	
	//@Test
	public void testComputeStats() {
		// Compute stats for different graphs and validate the number of vertices and edges for different cases are correct.
		
		//create input graph
		ColouredGraph graph = IOHelper.readGraphFromResource("graph1.n3", "N3");
		
		// Initialize metric
		NodeIteratorMetric2 metric;
		metric = new NodeIteratorMetric2();
		metric.computeTriangles(graph);
		
		// Check number of triangles formed for class and resource nodes
		System.out.println(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes());
		
		// Get the statistics about triangles
		System.out.println(metric.getMstatsVertEdges());
		
		// create second input graph
		graph = IOHelper.readGraphFromResource("graph_loop.n3", "N3");
		
		//initialize metric again. Since, evaluating the metrics separately
		metric = new NodeIteratorMetric2();
		metric.computeTriangles(graph);
		
		// Check number of triangles formed for class and resource nodes
		System.out.println(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes());
				
		// Get the statistics about triangles
		System.out.println(metric.getMstatsVertEdges());
		
		// create second input graph
		graph = IOHelper.readGraphFromResource("graph_loop_2.n3", "N3");
				
				//initialize metric again. Since, evaluating the metrics separately
		metric = new NodeIteratorMetric2();
		metric.computeTriangles(graph);
				
		// Check number of triangles formed for class and resource nodes
		System.out.println(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes());
						
				// Get the statistics about triangles
		System.out.println(metric.getMstatsVertEdges());
		
	}
	
	//@Test
	public void testMultigraph() {
		// Compute stats for different graphs and validate the number of vertices and edges for different cases are correct.
		
		//create input graph
		ColouredGraph graph = IOHelper.readGraphFromResource("graph1.n3", "N3");
//		
//		// Initialize metric
		NodeIteratorMetric2 metric;
		metric = new NodeIteratorMetric2();
		metric.computeTriangles(graph);
//		
//		// create second input graph
		graph = IOHelper.readGraphFromResource("graph_loop.n3", "N3");
//		
//		// call using same metric object
		metric.computeTriangles(graph);
//		
//		// create third input graph
		graph = IOHelper.readGraphFromResource("graph_loop_2.n3", "N3");
//				
		metric.computeTriangles(graph);
		
		// create fourth input graph
		graph = IOHelper.readGraphFromResource("triangle_file7.n3", "N3");
//						
		metric.computeTriangles(graph);
		
		// create 5th same input graph to check if count is incremented correctly
		graph = IOHelper.readGraphFromResource("triangle_file7.n3", "N3");
//								
		metric.computeTriangles(graph);
//				
//		// Check number of triangles formed for class and resource nodes
		System.out.println(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes());
//						
//				// Get the statistics about triangles
		System.out.println(metric.getMstatsVertEdges());
//		
	}
	
	//@Test
	public void testStatsLargeGraph() {
		// TODO: Need to update this test case
		// Compute stats for different graphs and validate the number of vertices and edges for different cases are correct.
		
		//create input graph
//		ColouredGraph graph = IOHelper.readGraphFromResource("email-Eu-core.n3", "N3");
//		
//		System.out.println(graph.getNumberOfVertices());
//		System.out.println(graph.getNumberOfEdges());
//		
//		// Initialize metric
//		NodeIteratorMetric metric;
//		metric = new NodeIteratorMetric();
//		metric.computeTriangles(graph);
//				
//		// Check number of triangles formed for class and resource nodes
//		System.out.println(metric.getSetOfTriangleshavingClassNodes());
//		System.out.println(metric.getSetOfTriangleshavingResourceNodes());
//						
//				// Get the statistics about triangles
//		System.out.println(metric.getMstatsVertEdges());
		
	}
	
	//@Test
	public void testGraphSWDF() {
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(2);
		ColouredGraph graphs[] = new ColouredGraph[20];
		graphs= mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
//		
		//System.out.println(graphs[0].getNumberOfVertices());
		//removeTypeEdgesFromGraphs(graphs);
		System.out.println(graphs[0].getNumberOfVertices());
//		
		System.out.println(graphs[0].getNumberOfEdges());
//		
//		// Initialize metric
		NodeIteratorMetric2 metric;
		metric = new NodeIteratorMetric2();
		//for(ColouredGraph graphColor: graphs) {
			metric.computeTriangles(graphs[0]);
			metric.computeTriangles(graphs[1]);;
		//}
//						
//				// Check number of triangles formed for class and resource nodes
		System.out.println(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes());
//		
//						// Get the statistics about triangles
		System.out.println(metric.getMstatsVertEdges());
//		
	}
	
	@Test
	public void test1SimplexConnecting2Simplexes() {
		//create input graph
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file9.n3", "N3");
//		
		//System.out.println(graphs[0].getNumberOfVertices());
		//removeTypeEdgesFromGraphs(graphs);
		System.out.println(graph.getNumberOfVertices());
//		
		System.out.println(graph.getNumberOfEdges());
//		
//		// Initialize metric
		NodeIteratorMetric2 metric;
		metric = new NodeIteratorMetric2();
		//for(ColouredGraph graphColor: graphs) {
			metric.computeTriangles(graph);
		//}
//						
//				// Check number of triangles formed for class and resource nodes
		System.out.println(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes());
//		
//						// Get the statistics about triangles
		System.out.println(metric.getMstatsVertEdges());
//		
	}
	
		//@Test
		public void test1SimplexTwoVerticesSameColorTriangle() {
			//create input graph
			ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file10.n3", "N3");
//			
			//System.out.println(graphs[0].getNumberOfVertices());
			//removeTypeEdgesFromGraphs(graphs);
			System.out.println(graph.getNumberOfVertices());
//			
			System.out.println(graph.getNumberOfEdges());
//			
//			// Initialize metric
			NodeIteratorMetric2 metric;
			metric = new NodeIteratorMetric2();
			//for(ColouredGraph graphColor: graphs) {
				metric.computeTriangles(graph);
			//}
//							
//					// Check number of triangles formed for class and resource nodes
			System.out.println(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes());
//			
//							// Get the statistics about triangles
			System.out.println(metric.getMstatsVertEdges());
//			
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
	
	//@Test
	public void getVerticesConnectedToTrianglesTest() {
		
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file8.n3", "N3");

		NodeIteratorMetric2 metric;
		metric = new NodeIteratorMetric2();
		metric.computeTriangles(graph);
		
		System.out.println("Map for vertices connected to triangle: ");
		System.out.println(metric.getmColoCountVertConnectedToTriangle());
	}
}
