package org.aksw.simba.lemming.metrics.single.edgetriangles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class is based on NodeIteratorMetric that counts number of triangles.
 */
public class NodeIteratorMetric2 {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeIteratorMetric2.class);
	/**
	 * Map for storing vertex colors for the triangle along with the probability for them in terms of triangle count and edge count in an array.
	 * Note: Count of triangles is stored at the 0th index of the array, 
	 * count of edges in the triangle is stored at the 1st index, 
	 * the probability of triangle is stored at the 2nd index 
	 * and the average count of triangles per no. of vertices in the input graph is stored at the 3rd index. 
	 */
	ObjectObjectOpenHashMap<TriColours, double[]> mTriColoEdgesTriCountDistAvg = new ObjectObjectOpenHashMap<TriColours, double[]>();
	// TODO: Remove additional elements stored in the array that are not required while creating the mimic graph.
	
	/**
	 * Map for storing vertex colors specifically for isolated triangles along with the probability for them in terms of triangle count and edge count in an array.
	 * Note: Count of triangles is stored at the 0th index of the array, 
	 * count of edges in the triangle is stored at the 1st index, 
	 * and the probability of triangle is stored at the 2nd index.  
	 */
	ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColoEdgesTriCountDistAvg = new ObjectObjectOpenHashMap<TriColours, double[]>();
	
	//HashSet<TriangleColours> setOfTriangleshavingClassNodes = new HashSet<TriangleColours>();
	
	/**
	 * Map for storing vertex colors for connected 1-simplexes connected to triangle
	 */
	ObjectDoubleOpenHashMap<BitSet> mColoCountVertConnectedToTriangle = new ObjectDoubleOpenHashMap<BitSet>();
	
	// Incremental key
	// Value:
	//0th index: Number of vertices included in triangles
	//1st index: Number of edges for triangles
	//2nd index: Number of vertices included in triangles and also form other 1-simplex
	//3rd index: Number of edges for the above case
	//4th index: Number of vertices not part of triangles
	//5th index: Number of edges for the above case
	Map<Integer, List<Integer>> mstatsVertEdges = new HashMap<Integer, List<Integer>>();
	
	int graphId = 1;
	
	/**
	 * Map object storing edge Ids found in triangles for every input graph.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsTriangle = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing common edge Ids connected to vertices in triangles and other vertices that are not in triangles for every input graph.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing edge Ids connecting 2-simplexes.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsConnectTriangles = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing edge Ids found in isolated triangles for every input graph.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsIsolatedTri = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	//****************************************** Maps for Storing vertices ************************************************//
	/**
	 * Map object storing vertex Ids found in isolated triangles for every input graph.
	 * Note: This map is used later to evaluate vertices and find self loops for isolated triangles.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsIsolatedTri = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing vertex Ids connected 2-simplexes for every input graph.
	 * Note: Similar to above, this map is also used to evaluate vertices and find self loops.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsConnectTriangles = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing vertex Ids for 1-simplexes connected to 2-simplexes for every input graph.
	 * Important: It only stores vertex Ids that are only connected to triangles and not any other 1-simplexes(the self loops for connected 1-simplexes is modelled separately).
	 * Note: Similar to above, this map is also used to evaluate vertices and find self loops.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertId1SimplexesConnToTri = new ObjectObjectOpenHashMap<Integer, IntSet>();
    
    public void computeTriangles(ColouredGraph graph){
    	
    	// Variable to track count of triangles
    	int countResourceTriangles = 0;
    	
    	//temporary map to store count of edges and triangles, which is later used to compute probabilities
    	ObjectObjectOpenHashMap<TriColours, double[]> mTriangleColoursTriangleEdgeCountsTemp = new ObjectObjectOpenHashMap<TriColours, double[]>();
    	
    	//************************** Variables Declaration for Isolated Triangles ********************************************//
    	//temporary map to store count of edges and triangles, which is later used to compute probabilities
    	ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColosTriEdgeCountsTemp = new ObjectObjectOpenHashMap<TriColours, double[]>();
    	
    	//temporary variables to track vertices forming only triangles
    	IntSet verticesForIsolatedTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// temporary variable for tracking edge Ids within triangle
    	IntSet edgesForIsolatedTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// Variable to track count of triangles
    	int countIsolatedTriangles = 0;
    	
    	//*************************** End Variable Declaration for Isolated Triangles ****************************************//
    	
    	//temporary variables to track vertices forming only triangles
    	IntSet verticesOnlyFormingTriangleResource = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// temporary variable for tracking edge Ids within triangle
    	IntSet edgesWithinTrianglesResource = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// temporary variable to store common edge Ids connected to vertices in triangles and other vertices that are not in triangles
    	IntSet commonEdgesTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	//temporary variable to track vertices that have additional edges within triangle
    	IntSet commonVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// variable to track total number of additional edges within triangles in a graph
    	int totalAdditionalEdgesInTriangle = 0;
    	
    	//temporary variables to store vertex IDs connecting to triangles. Note: The stored vertex ids are analyzed to determine self loops
    	IntSet verticesConnectingToTriangle = new DefaultIntSet(Constants.DEFAULT_SIZE); // variable for self loop
    	
    	//commenting this statement. getUndirectedGraph creates a new vertex only for edges found in the graph. As RDF type edges are removed some vertices will not be considered
    	IntSet[] edges = new IntSet[graph.getGraph().getNumberOfVertices()];

        Grph grph = getUndirectedGraph(graph.getGraph()); // creates graph based on edges
        IntSet vertices = grph.getVertices();

        for (int vertexId:vertices) {
        	edges[vertexId] = grph.getOutEdges(vertexId);
        	edges[vertexId].addAll(grph.getInEdges(vertexId));
        }
        
        //Logic to find set of class vertices
        IntSet classVerticesSet = new DefaultIntSet(Constants.DEFAULT_SIZE);
        System.out.println("Graph: " + graphId);
        for (int edgeId: graph.getEdges()) {
        	if (graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour())) {
        		int classVertexID = graph.getHeadOfTheEdge(edgeId);
        		classVerticesSet.add(classVertexID);
        	}
        }

        IntSet vertGrph = grph.getVertices();
        IntSet visitedVertices = new DefaultIntSet(vertGrph.size());
        for (int vertex : vertGrph) {
            IntSet neighbors = IntSetUtil.difference(
                    IntSetUtil.union(grph.getInNeighbors(vertex), grph.getOutNeighbors(vertex)), visitedVertices);
            for (int neighbor1 : neighbors) {
                IntSet neighbors1 = IntSetUtil.difference(
                        IntSetUtil.union(grph.getInNeighbors(neighbor1), grph.getOutNeighbors(neighbor1)),
                        visitedVertices);
                for (int neighbor2 : neighbors) {
                    if (vertex != neighbor1 && vertex != neighbor2 && neighbor1 < neighbor2
                            && neighbors1.contains(neighbor2)) {
                    	
                    	IntSet intersection1 = IntSetUtil.intersection(edges[vertex], edges[neighbor1]);
                    	IntSet intersection2 = IntSetUtil.intersection(edges[neighbor1], edges[neighbor2]);
                    	IntSet intersection3 = IntSetUtil.intersection(edges[neighbor2], edges[vertex]);
                    	
                    	
                    	int intersectionResult1 = intersection1.size();
                    	int intersectionResult2 = intersection2.size();
                    	int intersectionResult3 = intersection3.size();
                    	
                    	int intersectionResult = (intersectionResult1 * intersectionResult2 * intersectionResult3);
                        
                        if (intersectionResult > 0) {
                        	
                        	TriColours tempObj = new TriColours(graph.getVertexColour(vertex), graph.getVertexColour(neighbor1), graph.getVertexColour(neighbor2));
                        	
                        	// Set to store colors of every edge within the nodes forming a triangle
                        	HashSet<BitSet> setEdgeColors = new HashSet<BitSet>();
                        	
                        	// get colours of all edges and add to the Set
                        	for(int edgeIdVal: IntSetUtil.union( IntSetUtil.intersection(edges[vertex], edges[neighbor1]),IntSetUtil.union(IntSetUtil.intersection(edges[neighbor1], edges[neighbor2]), IntSetUtil.intersection(edges[neighbor2], edges[vertex]))) ) {
                        		setEdgeColors.add(graph.getEdgeColour(edgeIdVal));
                        	}
                        	
                        	// if RDF type edge is not present among all the edges then add the node colours to setOfTriangleshavingResourceNodes set
                        	if ( !setEdgeColors.contains(graph.getRDFTypePropertyColour()) ) {
                        		// finding number of triangles
                    			int numOfTri = Math.min(Math.min(intersectionResult1, intersectionResult2), intersectionResult3);
                    			
                    			if (!verticesFormingIsolatedTriangles(vertex, neighbor1, neighbor2, graph, classVerticesSet)) {
                    				// Update results for connected triangles
                        		
	                        		// Add edges within triangle for resource nodes only
	                            	edgesWithinTrianglesResource.addAll(intersection1);
	                            	edgesWithinTrianglesResource.addAll(intersection2);
	                            	edgesWithinTrianglesResource.addAll(intersection3);
	                            	
	                            	// Store the vertex forming triangle into temporary set for resource nodes only
	                            	verticesOnlyFormingTriangleResource.add(vertex);
	                            	verticesOnlyFormingTriangleResource.add(neighbor1);
	                            	verticesOnlyFormingTriangleResource.add(neighbor2);
	                            	
	                            	countResourceTriangles=countResourceTriangles+ numOfTri; // incrementing with number of triangles instead of number of edges earlier
	                        		
	                        		if (mTriangleColoursTriangleEdgeCountsTemp.containsKey(tempObj)) {
	                        			
	                        			// triangle was already found previously, update the count of triangle and edges
	                        			double[] triangleCountEdgeCountArr = mTriangleColoursTriangleEdgeCountsTemp.get(tempObj);
	                        			
	                        			//previous count of edges
	                        			double previousEdgeCount = triangleCountEdgeCountArr[1];
	                        			double previousTriangleCount = triangleCountEdgeCountArr[0];
	                        			
	                        			//create new updated array
	                        			double[] newTriangleEdgeCountsArr = new double[2];
	                        			
	                        			newTriangleEdgeCountsArr[0] = previousTriangleCount + numOfTri; // storing number of triangles instead of number of edges earlier
	                        			newTriangleEdgeCountsArr[1] = previousEdgeCount + intersectionResult1 + intersectionResult2 + intersectionResult3;
	
	                        			// update the hashmap
	                        			mTriangleColoursTriangleEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
	                        			
	                            	}
	                            	else {
	                            		
	                            		//create an array for triangle and edge counts of triangle
	                        			double[] newTriangleEdgeCountsArr = new double[2];
	                        			//newTriangleEdgeCountsArr[0] = (int) ((intersectionResult1 + intersectionResult2 + intersectionResult3)/3);
	                        			
	                        			//newTriangleEdgeCountsArr[0] = intersectionResult;
	                        			newTriangleEdgeCountsArr[0] = numOfTri; // storing number of triangles instead of number of edges earlier
	                        			newTriangleEdgeCountsArr[1] = intersectionResult1 + intersectionResult2 + intersectionResult3;
	                            		
	                            		// triangle is found for the first time
	                        			mTriangleColoursTriangleEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
	                            	}
	                        		
                    			} else {
                    				//update results for isolated triangles since vertices form isolated triangles
                    				
                    				//TODO: Code repetition same operation with different set of variable. One possible way to optimize is make variables global and define functions for these operations.
                    				
                    				// Add edges within triangle for resource nodes only
                    				edgesForIsolatedTriangles.addAll(intersection1);
                    				edgesForIsolatedTriangles.addAll(intersection2);
                    				edgesForIsolatedTriangles.addAll(intersection3);
	                            	
	                            	// Store the vertex forming triangle into temporary set for resource nodes only
                    				verticesForIsolatedTriangles.add(vertex);
                    				verticesForIsolatedTriangles.add(neighbor1);
                    				verticesForIsolatedTriangles.add(neighbor2);
                    				
                    				countIsolatedTriangles = countIsolatedTriangles + numOfTri; // incrementing with number of triangles instead of number of edges earlier
	                        		
	                        		if (mIsolatedTriColosTriEdgeCountsTemp.containsKey(tempObj)) {
	                        			
	                        			// triangle was already found previously, update the count of triangle and edges
	                        			double[] triangleCountEdgeCountArr = mIsolatedTriColosTriEdgeCountsTemp.get(tempObj);
	                        			
	                        			//previous count of edges
	                        			double previousEdgeCount = triangleCountEdgeCountArr[1];
	                        			double previousTriangleCount = triangleCountEdgeCountArr[0];
	                        			
	                        			//create new updated array
	                        			double[] newTriangleEdgeCountsArr = new double[2];
	                        			
	                        			newTriangleEdgeCountsArr[0] = previousTriangleCount + numOfTri; // storing number of triangles instead of number of edges earlier
	                        			newTriangleEdgeCountsArr[1] = previousEdgeCount + intersectionResult1 + intersectionResult2 + intersectionResult3;
	
	                        			// update the hashmap
	                        			mIsolatedTriColosTriEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
	                        			
	                            	}
	                            	else {
	                            		
	                            		//create an array for triangle and edge counts of triangle
	                        			double[] newTriangleEdgeCountsArr = new double[2];
	                        			//newTriangleEdgeCountsArr[0] = (int) ((intersectionResult1 + intersectionResult2 + intersectionResult3)/3);
	                        			
	                        			//newTriangleEdgeCountsArr[0] = intersectionResult;
	                        			newTriangleEdgeCountsArr[0] = numOfTri; // storing number of triangles instead of number of edges earlier
	                        			newTriangleEdgeCountsArr[1] = intersectionResult1 + intersectionResult2 + intersectionResult3;
	                            		
	                            		// triangle is found for the first time
	                        			mIsolatedTriColosTriEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
	                            	}
                    			}
                        		
                        	}
                        }
                    }
                }
            }
            visitedVertices.add(vertex);
        }
        
     // Logic to get total number of vertices in the input graph excluding class vertices
     double totalVertices = graph.getNumberOfVertices();
     //remove number of class vertices from the total vertices count
     //totalVertices = totalVertices - classVerticesSet.size();// class vertices should not be subtracted from the total vertices. since they are also considered in input number of vertices required in output graph.
        
     //******************************* Logic to update map for connected triangles *********************************//
     //temporary variable to store total triangle distribution
     double totalDistTri = 0.0;
        
     // Iterate over temporary map and add an element at the 3rd index of the array (No. of edges in triangle)/(Total number of triangles)
     Object[] keysTriColo = mTriangleColoursTriangleEdgeCountsTemp.keys;
     for (int i = 0; i < keysTriColo.length; i++ ) {
    	 if (mTriangleColoursTriangleEdgeCountsTemp.allocated[i]) {
    		 TriColours triangleColours = (TriColours) keysTriColo[i];
    		 double[] arrCountsDist = mTriangleColoursTriangleEdgeCountsTemp.get(triangleColours);
        		
    		 //create a new array for storing avg count 
    		 double[] arrCountDistTemp = new double[3];
    		 arrCountDistTemp[0] = arrCountsDist[0];
    		 arrCountDistTemp[1] = arrCountsDist[1];
    		 arrCountDistTemp[2] = (arrCountsDist[1] * 1.0) / countResourceTriangles;
    		 totalDistTri = totalDistTri + arrCountDistTemp[2]; // incrementing total distribution
    		 
    		 mTriangleColoursTriangleEdgeCountsTemp.put(triangleColours, arrCountDistTemp); //update the map with the new array
        		}
        	}
        
        	// iterate over results and update the global map
        	keysTriColo = mTriangleColoursTriangleEdgeCountsTemp.keys;
        	for (int i = 0; i < keysTriColo.length; i++ ) {
        		if (mTriangleColoursTriangleEdgeCountsTemp.allocated[i]) {
        			TriColours triangleColours = (TriColours) keysTriColo[i];
        			double[] arrCountDist = mTriangleColoursTriangleEdgeCountsTemp.get(triangleColours);
        		
        			//create a new array for storing avg count 
        			double[] arrCountDistTemp = new double[4]; // additional element in the array for storing average count of triangles per vertices
        			arrCountDistTemp[0] = arrCountDist[0];
        			arrCountDistTemp[1] = arrCountDist[1];
        			arrCountDistTemp[2] = (arrCountDist[2] * 1.0) / totalDistTri; // update the distribution
        			// Add at the 3rd index, avg count of triangles per total number of vertices in the input graph. Note: 0th and 1st indices store the number of edges and number of triangles respectively.
        			arrCountDistTemp[3] = arrCountDist[0] * 1.0 / totalVertices;
        		
        			LOGGER.debug("Triangle colors: " + triangleColours.getA() + ", " + triangleColours.getB() + ", " + triangleColours.getC() + ".");
        			LOGGER.debug("Number of triangles: " + arrCountDist[0]);
        			LOGGER.debug("Total number of vertices: " + totalVertices);
        			LOGGER.debug("Avg per no. of vertices: " + arrCountDistTemp[3]);
        		
        			// update the distribution in global maps
        			if (mTriColoEdgesTriCountDistAvg.containsKey(triangleColours)) {
        			
        				// get old distribution values
        				double[] previousCountdist = mTriColoEdgesTriCountDistAvg.get(triangleColours);
        			
        				// update the distribution values
        				arrCountDistTemp[0] = arrCountDistTemp[0] + previousCountdist[0];
        				arrCountDistTemp[1] = arrCountDistTemp[1] + previousCountdist[1];
        				arrCountDistTemp[2] = arrCountDistTemp[2] + previousCountdist[2];
        				arrCountDistTemp[3] = arrCountDistTemp[3] + previousCountdist[3];
        			
        			}
        			mTriColoEdgesTriCountDistAvg.put(triangleColours, arrCountDistTemp); //update the map with the new array
        		}
        	}
        
        //*************************** Logic to update map for isolated triangles ***************************************//
        //TODO: Code repetition same operation (except tracking average triangle distribution). Make variables global and use functions 
        
        totalDistTri = 0.0;
        
        // Iterate over temporary map and add an element at the 3rd index of the array (No. of edges in triangle)/(Total number of triangles)
        keysTriColo = mIsolatedTriColosTriEdgeCountsTemp.keys;
        for (int i = 0; i < keysTriColo.length; i++ ) {
        	if (mIsolatedTriColosTriEdgeCountsTemp.allocated[i]) {
        		TriColours triangleColours = (TriColours) keysTriColo[i];
        		double[] arrCountsDist = mIsolatedTriColosTriEdgeCountsTemp.get(triangleColours);
        		
        		//create a new array for storing avg count 
        		double[] arrCountDistTemp = new double[3];
        		arrCountDistTemp[0] = arrCountsDist[0];
        		arrCountDistTemp[1] = arrCountsDist[1];
        		arrCountDistTemp[2] = (arrCountsDist[1] * 1.0) / countIsolatedTriangles;
        		totalDistTri = totalDistTri + arrCountDistTemp[2]; // incrementing total distribution
        		
        		mIsolatedTriColosTriEdgeCountsTemp.put(triangleColours, arrCountDistTemp); //update the map with the new array
        	}
        }
        
        LOGGER.debug("Isolated triangles: ");
        // iterate over results and update the global map
        keysTriColo = mIsolatedTriColosTriEdgeCountsTemp.keys;
        for (int i = 0; i < keysTriColo.length; i++ ) {
        	if (mIsolatedTriColosTriEdgeCountsTemp.allocated[i]) {
        		TriColours triangleColours = (TriColours) keysTriColo[i];
        		double[] arrCountDist = mIsolatedTriColosTriEdgeCountsTemp.get(triangleColours);
        		
        		//create a new array for storing avg count 
        		double[] arrCountDistTemp = new double[3]; // additional element in the array for storing average count of triangles per vertices
        		arrCountDistTemp[0] = arrCountDist[0];
        		arrCountDistTemp[1] = arrCountDist[1];
        		arrCountDistTemp[2] = (arrCountDist[2] * 1.0) / totalDistTri; // update the distribution
        		
        		LOGGER.debug("Triangle colors: " + triangleColours.getA() + ", " + triangleColours.getB() + ", " + triangleColours.getC() + ".");
        		LOGGER.debug("Number of triangles: " + arrCountDist[0]);
        		LOGGER.debug("Total number of vertices: " + totalVertices);
        		
        		// update the distribution in global maps
        		if (mIsolatedTriColoEdgesTriCountDistAvg.containsKey(triangleColours)) {
        			
        			// get old distribution values
        			double[] previousCountdist = mIsolatedTriColoEdgesTriCountDistAvg.get(triangleColours);
        			
        			// update the distribution values
        			arrCountDistTemp[0] = arrCountDistTemp[0] + previousCountdist[0];
        			arrCountDistTemp[1] = arrCountDistTemp[1] + previousCountdist[1];
        			arrCountDistTemp[2] = arrCountDistTemp[2] + previousCountdist[2];
        			
        		}
        		mIsolatedTriColoEdgesTriCountDistAvg.put(triangleColours, arrCountDistTemp); //update the map with the new array
        	}
        }
        
        //*************************** Logic to compute 1-simplex connecting two simplex ********************************//
        // Evaluate edges not in triangles and check if two end points are vertices of a triangle
        IntSet edgesNotInTriangles = IntSetUtil.difference(graph.getEdges(), edgesWithinTrianglesResource);
        IntSet edgeIdConnectingTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
        for (int edgeIdNotTri : edgesNotInTriangles) {
        	IntSet verticesEdgeNotTri = graph.getVerticesIncidentToEdge(edgeIdNotTri);
        	if ((verticesOnlyFormingTriangleResource.containsAll(verticesEdgeNotTri)) && (verticesEdgeNotTri.size() == 2)) { 
        		//TODO: update condition if loops are present for any vertex then it is considered 1-simplex connecting two triangles. 
        		//update the condition vertices incident on edge should be 2
        		edgeIdConnectingTriangles.add(edgeIdNotTri);
        	}
        }
        
        //*************************** Logic to compute 1-simplexes connected to 2-simplexes ***********************//
        // Logic for computing the number of vertices and edges for different cases
        IntSet diffTotalVertices = IntSetUtil.difference(vertGrph, verticesOnlyFormingTriangleResource);
        diffTotalVertices = IntSetUtil.difference(diffTotalVertices, classVerticesSet);
        
        // variable to track number of simplexes connected to Vertices of triangle
        int numSimplexesVertConnectTriangle = 0;
        // Map for storing Color of vertices connecting to triangle. Later, this map is used to compute distributions
        ObjectIntOpenHashMap<BitSet> mVertColoConnectTriangleTemp = new ObjectIntOpenHashMap<BitSet>();
        
        for(int vertexId: diffTotalVertices) {
        	IntSet neighbors = IntSetUtil.union(grph.getInNeighbors(vertexId), grph.getOutNeighbors(vertexId));
        	neighbors = IntSetUtil.difference(neighbors, classVerticesSet);// do not consider class vertices
        	if (neighbors.contains(vertexId))
        		neighbors.remove(vertexId); // in case of self loop, a vertex will be neighbor of itself. We do not want to consider it.
        	
        	IntSet intersectionTriangleVerts = IntSetUtil.intersection(neighbors, verticesOnlyFormingTriangleResource);
        	for(int intersectVertId: intersectionTriangleVerts) {
        		
        		IntSet tempIntersection = IntSetUtil.intersection(edges[vertexId], edges[intersectVertId]);
        		
        		// *************Logic to not consider RDF Type edges*************** //
            	HashSet<BitSet> setEdgeColors = new HashSet<BitSet>(); // Set to store colors of every edge within the nodes forming a triangle
            	
            	// get colours of all edges and add to the Set
            	for(int edgeIdVal: tempIntersection ) {
            		setEdgeColors.add(graph.getEdgeColour(edgeIdVal));
            	}
            	
            	
            	if (!setEdgeColors.contains(graph.getRDFTypePropertyColour())) {
	        		commonVertices.add(intersectVertId);
	        		totalAdditionalEdgesInTriangle = totalAdditionalEdgesInTriangle + tempIntersection.size();
	        		commonEdgesTriangles.addAll(tempIntersection);
	        		
	        		numSimplexesVertConnectTriangle++;
	        		//mColoCountVertConnectedToTriangle.putOrAdd(graph.getVertexColour(vertexId), 1, 1); //commenting using temporary map to compute the distribution
	        		
	        		
	        		if (verticesOnlyFormingTriangleResource.containsAll(neighbors)) { // check 1-simplex is only connected to triangles
	        			// Note: vertices need to be created for such 1-simplexes. Thus, the count of the vertex color is stored and it is later used to create distribution
	        			mVertColoConnectTriangleTemp.putOrAdd(graph.getVertexColour(vertexId), 1, 1);
	        			verticesConnectingToTriangle.add(vertexId);
	        		}
            	}
        	}
        }
        
        //Compute distribution of vertices connecting to triangles
        Object[] vertColoConntTriArr = mVertColoConnectTriangleTemp.keys;
        for (int vertConnIndex = 0; vertConnIndex < vertColoConntTriArr.length; vertConnIndex++) {
        	if (mVertColoConnectTriangleTemp.allocated[vertConnIndex]) {
        		BitSet vertColoConntTri = (BitSet) vertColoConntTriArr[vertConnIndex];
        		double distVertColoConnt = mVertColoConnectTriangleTemp.get(vertColoConntTri) * 1.0 / numSimplexesVertConnectTriangle;
        		mColoCountVertConnectedToTriangle.putOrAdd(vertColoConntTri, distVertColoConnt, distVertColoConnt);
        	}
        }
        
        
        //*************************Storing statistics used later for estimations**********************************************//
        
        // Number of vertices for different cases in a variable
        int numCommonVert = commonVertices.size();
        int numVertFormingOnlyTriangle = verticesOnlyFormingTriangleResource.size();
        
        // Temporary List to store output for different graphs
        List<Integer> tempList = new ArrayList<Integer>();
        tempList.add(numVertFormingOnlyTriangle); // 0th index - Number of vertices Forming only triangles
        tempList.add(edgesWithinTrianglesResource.size()); // 1st index - Number of edges for vertices only forming triangles
        tempList.add(numCommonVert); // 2nd index - Common vertices part of triangles and also other 1-simplex
        tempList.add(totalAdditionalEdgesInTriangle); // 3rd index - Common edges that could be part of triangles and/or edges connecting vertices forming triangle and 1-simplex.
        tempList.add(edgeIdConnectingTriangles.size()); // 5th index - number of edges that are connecting triangles with 1-simplex
        tempList.add(verticesForIsolatedTriangles.size()); // 6th index - number of vertices for isolated triangles
        tempList.add(edgesForIsolatedTriangles.size()); // 7th index - number of vertices for isolated triangles
        
        
        LOGGER.debug("Graph Id: " + graphId);
        LOGGER.debug("Count of edges for Resource triangles: " + edgesWithinTrianglesResource.size());
        
        mstatsVertEdges.put(graphId, tempList);
        
        // store edges ids for different cases in the map
        mGraphsEdgesIdsTriangle.put(graphId, edgesWithinTrianglesResource);
        
        mGraphsCommonEdgesIds.put(graphId, commonEdgesTriangles);
        
        //store edge ids for isolated triangles
        mGraphsEdgesIdsIsolatedTri.put(graphId, edgesForIsolatedTriangles);
        
        // store edge Ids for 1-simplexes connecting triangles
        mGraphsEdgesIdsConnectTriangles.put(graphId, edgeIdConnectingTriangles);
        
        // ************************* Find self -loops for different cases *************************************//
        // Storing vertices forming triangles and they are evaluated later to find distribution of self loops
        
        // Isolated triangles
        mGraphsVertIdsIsolatedTri.put(graphId, verticesForIsolatedTriangles);
        
        // Connected triangles
        mGraphsVertIdsConnectTriangles.put(graphId, verticesOnlyFormingTriangleResource);
        
        // 1-simplexes connected to triangles
        mGraphsVertId1SimplexesConnToTri.put(graphId, verticesConnectingToTriangle);
        
        graphId++;
        
    }
    
    public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertId1SimplexesConnToTri() {
		return mGraphsVertId1SimplexesConnToTri;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertIdsIsolatedTri() {
		return mGraphsVertIdsIsolatedTri;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertIdsConnectTriangles() {
		return mGraphsVertIdsConnectTriangles;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsIsolatedTri() {
		return mGraphsEdgesIdsIsolatedTri;
	}

	public ObjectObjectOpenHashMap<TriColours, double[]> getmIsolatedTriColoEdgesTriCountDistAvg() {
		return mIsolatedTriColoEdgesTriCountDistAvg;
	}

	public boolean verticesFormingIsolatedTriangles(int vertexID1, int vertexID2, int vertexID3, ColouredGraph graph, IntSet classVertices) {
    	// get vertices incident on input ids
    	IntSet vertices1Incident = IntSetUtil.union(graph.getInNeighbors(vertexID1), graph.getOutNeighbors(vertexID1));
    	IntSet vertices2Incident = IntSetUtil.union(graph.getInNeighbors(vertexID2), graph.getOutNeighbors(vertexID2));
    	IntSet vertices3Incident = IntSetUtil.union(graph.getInNeighbors(vertexID3), graph.getOutNeighbors(vertexID3));
    			
    	//find union of vertices incident to all ids
    	IntSet unionResult = IntSetUtil.union(IntSetUtil.union(vertices1Incident, vertices2Incident), vertices3Incident);
    	
    	//do not consider class vertices
    	IntSet differenceResult = IntSetUtil.difference(unionResult, classVertices);
    	
    	IntSet inputVertexIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	inputVertexIDs.add(vertexID1);
    	inputVertexIDs.add(vertexID2);
    	inputVertexIDs.add(vertexID3);
    	
    	differenceResult = IntSetUtil.difference(differenceResult, inputVertexIDs);
    	
    	if (differenceResult.size() == 0)
    		return true;
    	
    	return false;
    	
    }
    
    public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsConnectTriangles() {
		return mGraphsEdgesIdsConnectTriangles;
	}

	public ObjectObjectOpenHashMap<TriColours, double[]> getmTriangleColoursTriangleCountsEdgeCountsResourceNodes() {
		return mTriColoEdgesTriCountDistAvg;
	}

	public ObjectDoubleOpenHashMap<BitSet> getmColoCountVertConnectedToTriangle() {
		return mColoCountVertConnectedToTriangle;
	}

	/**
     * This function computes the count of total number of edges of rdf type for the input set of vertices. The computation is performed for the graph, the given set of edges and the edges that should not be considered. 
     * @return
     */
    private int computeCountOfRDFTypeEdges(IntSet verticesIdSet, ColouredGraph graph, IntSet[] edges, IntSet edgesNotToConsider) {
    	// ************ Logic for calculation for edges of rdf type for common vertices ************
    	IntSet edgesForInputVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
        
        // Iterate over all vertices and store their edgeIds
        for(int vertexIdCommon : verticesIdSet) {
        	edgesForInputVertices.addAll(edges[vertexIdCommon]);
        }
        
        edgesForInputVertices = IntSetUtil.difference(edgesForInputVertices, edgesNotToConsider);
        
        // Variables to store color of rdf type edge and count of such an edge 
        Object rdfTypePropertyColour = graph.getRDFTypePropertyColour();
        int numOfrdfTypeEdgesCommon = 0;
        
        //For the found edges, evaluate their color and compute the count of total number of edges of rdf type
        for (int edgeIdTemp: edgesForInputVertices) {
        	if (graph.getEdgeColour(edgeIdTemp).equals(rdfTypePropertyColour))
        		numOfrdfTypeEdgesCommon = numOfrdfTypeEdgesCommon + 1;
        }
        
        return numOfrdfTypeEdgesCommon;
    }

    public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsTriangle() {
		return mGraphsEdgesIdsTriangle;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsCommonEdgesIds() {
		return mGraphsCommonEdgesIds;
	}

	public Map<Integer, List<Integer>> getMstatsVertEdges() {
		return mstatsVertEdges;
	}

	private Grph getUndirectedGraph(Grph graph) {
        Grph undirectedGraph = new InMemoryGrph();
        for (int e : graph.getEdges()) {
            int sourceNode = graph.getOneVertex(e);
            int targetNode = graph.getTheOtherVertex(e, sourceNode);
            undirectedGraph.addUndirectedSimpleEdge(sourceNode, targetNode);
        }
        return undirectedGraph;
    }

}