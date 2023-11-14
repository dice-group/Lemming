package org.aksw.simba.lemming.simplexes.analysis;

import grph.DefaultIntSet;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.HashSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.TriangleColours;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * This class is based on NodeIteratorMetric that counts number of triangles.
 */
public class FindTriangles {
	
	/**
	 * Map for storing vertex colors for the triangle along with the probability for them in terms of triangle count and edge count in an array.
	 * Note: Count of triangles is stored at the 0th index of the array, 
	 * count of edges in the triangle is stored at the 1st index, 
	 * the probability of triangle is stored at the 2nd index 
	 * and the average count of triangles per no. of vertices in the input graph is stored at the 3rd index. 
	 */
	private ObjectObjectOpenHashMap<TriangleColours, double[]> mTriColoEdgesTriCountDistAvg = new ObjectObjectOpenHashMap<TriangleColours, double[]>();
	// TODO: Remove additional elements stored in the array that are not required while creating the mimic graph.
	
	/**
	 * Map for storing vertex colors specifically for isolated triangles along with the probability for them in terms of triangle count and edge count in an array.
	 * Note: Count of triangles is stored at the 0th index of the array, 
	 * count of edges in the triangle is stored at the 1st index, 
	 * and the probability of triangle is stored at the 2nd index.  
	 */
	private ObjectObjectOpenHashMap<TriangleColours, double[]> mIsolatedTriColoEdgesTriCountDistAvg = new ObjectObjectOpenHashMap<TriangleColours, double[]>();
	
	int graphId = 1;
	
	//****************************************** Maps for Storing edges ************************************************//
	/**
	 * Map object storing edge Ids found in triangles for every input graph.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsTriangle = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
	/**
	 * Map object storing edge Ids found in isolated triangles for every input graph.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsIsolatedTri = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	//****************************************** Maps for Storing vertices ************************************************//
	/**
	 * Map object storing vertex Ids found in isolated triangles for every input graph.
	 * Note: This map is used later to evaluate vertices and find self loops for isolated triangles.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsIsolatedTri = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing vertex Ids connected 2-simplexes for every input graph.
	 * Note: Similar to above, this map is also used to evaluate vertices and find self loops.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsConnectTriangles = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	public FindTriangles() {
		
	}
    
    public void computeTriangles(ColouredGraph graph){
    	
    	//Temporary variables for storing connected 2-simplexes information
    	int countResourceTriangles = 0; // Variable to track count of triangles
    	IntSet verticesOnlyFormingTriangleResource = new DefaultIntSet(Constants.DEFAULT_SIZE);//temporary variables to track vertices forming only triangles
    	IntSet edgesWithinTrianglesResource = new DefaultIntSet(Constants.DEFAULT_SIZE);// temporary variable for tracking edge Ids within triangle
    	ObjectObjectOpenHashMap<TriangleColours, double[]> mTriangleColoursTriangleEdgeCountsTemp = new ObjectObjectOpenHashMap<TriangleColours, double[]>(); //temporary map to store count of edges and triangles, which is later used to compute probabilities
    	
    	//Temporary variables for storing isolated 2-simplexes information
    	IntSet verticesForIsolatedTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);//temporary variables to track vertices forming only triangles
    	IntSet edgesForIsolatedTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);// temporary variable for tracking edge Ids within triangle
    	int countIsolatedTriangles = 0;// Variable to track count of triangles
    	ObjectObjectOpenHashMap<TriangleColours, double[]> mIsolatedTriColosTriEdgeCountsTemp = new ObjectObjectOpenHashMap<TriangleColours, double[]>();//temporary map to store count of edges and triangles, which is later used to compute probabilities
    	
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
                        	
                        	TriangleColours tempObj = new TriangleColours(graph.getVertexColour(vertex), graph.getVertexColour(neighbor1), graph.getVertexColour(neighbor2));
                        	
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
        
     updateCountConn2Simplexes(graph, mTriangleColoursTriangleEdgeCountsTemp, countResourceTriangles); // update count for connected 2-simplexes in global map
        
     updateCountIso2Simplexes(graph, mIsolatedTriColosTriEdgeCountsTemp, countIsolatedTriangles); // update count for isolated 2-simplexes in global map
        
     mGraphsVertIdsIsolatedTri.put(graphId, verticesForIsolatedTriangles); // Isolated triangles vertices
        
     mGraphsVertIdsConnectTriangles.put(graphId, verticesOnlyFormingTriangleResource); // Connected triangles vertices
        
     mGraphsEdgesIdsTriangle.put(graphId, edgesWithinTrianglesResource); // Edge Ids connected 2-simplexes
        
     mGraphsEdgesIdsIsolatedTri.put(graphId, edgesForIsolatedTriangles); //store edge ids for isolated triangles
        
     graphId++;
        
    }
    
    private void updateCountConn2Simplexes(ColouredGraph graph, ObjectObjectOpenHashMap<TriangleColours, double[]> mTriangleColoursTriangleEdgeCountsTemp, int countResourceTriangles) {
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
       		 TriangleColours triangleColours = (TriangleColours) keysTriColo[i];
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
           			TriangleColours triangleColours = (TriangleColours) keysTriColo[i];
           			double[] arrCountDist = mTriangleColoursTriangleEdgeCountsTemp.get(triangleColours);
           		
           			//create a new array for storing avg count 
           			double[] arrCountDistTemp = new double[4]; // additional element in the array for storing average count of triangles per vertices
           			arrCountDistTemp[0] = arrCountDist[0];
           			arrCountDistTemp[1] = arrCountDist[1];
           			arrCountDistTemp[2] = (arrCountDist[2] * 1.0) / totalDistTri; // update the distribution
           			// Add at the 3rd index, avg count of triangles per total number of vertices in the input graph. Note: 0th and 1st indices store the number of edges and number of triangles respectively.
           			arrCountDistTemp[3] = arrCountDist[0] * 1.0 / totalVertices;
           		
           			System.out.println("Triangle colors: " + triangleColours.getA() + ", " + triangleColours.getB() + ", " + triangleColours.getC() + ".");
           			System.out.println("Number of triangles: " + arrCountDist[0]);
           			System.out.println("Total number of vertices: " + totalVertices);
           			System.out.println("Avg per no. of vertices: " + arrCountDistTemp[3]);
           		
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
    }
    
    private void updateCountIso2Simplexes(ColouredGraph graph, ObjectObjectOpenHashMap<TriangleColours, double[]> mIsolatedTriColosTriEdgeCountsTemp, int countIsolatedTriangles) {
    	// Logic to get total number of vertices in the input graph excluding class vertices
        double totalVertices = graph.getNumberOfVertices();
           
           //*************************** Logic to update map for isolated triangles ***************************************//
           //TODO: Code repetition same operation (except tracking average triangle distribution). Make variables global and use functions 
           
           double totalDistTri = 0.0;
           
           // Iterate over temporary map and add an element at the 3rd index of the array (No. of edges in triangle)/(Total number of triangles)
           Object[] keysTriColo = mIsolatedTriColosTriEdgeCountsTemp.keys;
           for (int i = 0; i < keysTriColo.length; i++ ) {
           	if (mIsolatedTriColosTriEdgeCountsTemp.allocated[i]) {
           		TriangleColours triangleColours = (TriangleColours) keysTriColo[i];
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
           
           System.out.println("Isolated triangles: ");
           // iterate over results and update the global map
           keysTriColo = mIsolatedTriColosTriEdgeCountsTemp.keys;
           for (int i = 0; i < keysTriColo.length; i++ ) {
           	if (mIsolatedTriColosTriEdgeCountsTemp.allocated[i]) {
           		TriangleColours triangleColours = (TriangleColours) keysTriColo[i];
           		double[] arrCountDist = mIsolatedTriColosTriEdgeCountsTemp.get(triangleColours);
           		
           		//create a new array for storing avg count 
           		double[] arrCountDistTemp = new double[3]; // additional element in the array for storing average count of triangles per vertices
           		arrCountDistTemp[0] = arrCountDist[0];
           		arrCountDistTemp[1] = arrCountDist[1];
           		arrCountDistTemp[2] = (arrCountDist[2] * 1.0) / totalDistTri; // update the distribution
           		
           		System.out.println("Triangle colors: " + triangleColours.getA() + ", " + triangleColours.getB() + ", " + triangleColours.getC() + ".");
           		System.out.println("Number of triangles: " + arrCountDist[0]);
           		System.out.println("Total number of vertices: " + totalVertices);
           		
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
    }

	private boolean verticesFormingIsolatedTriangles(int vertexID1, int vertexID2, int vertexID3, ColouredGraph graph, IntSet classVertices) {
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
    

	private Grph getUndirectedGraph(Grph graph) {
        Grph undirectedGraph = new InMemoryGrph();
        for (int e : graph.getEdges()) {
            int sourceNode = graph.getOneVertex(e);
            int targetNode = graph.getTheOtherVertex(e, sourceNode);
            undirectedGraph.addUndirectedSimpleEdge(sourceNode, targetNode);
        }
        return undirectedGraph;
    }

	//****************************public getters for triangle counts, edges and vertices*****************************************//
	
	public ObjectObjectOpenHashMap<TriangleColours, double[]> getmTriColoEdgesTriCountDistAvg() {
		return mTriColoEdgesTriCountDistAvg;
	}

	public ObjectObjectOpenHashMap<TriangleColours, double[]> getmIsolatedTriColoEdgesTriCountDistAvg() {
		return mIsolatedTriColoEdgesTriCountDistAvg;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsConnectedTri() {
		return mGraphsEdgesIdsTriangle;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsIsolatedTri() {
		return mGraphsEdgesIdsIsolatedTri;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertIdsIsolatedTri() {
		return mGraphsVertIdsIsolatedTri;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertIdsConnectedTri() {
		return mGraphsVertIdsConnectTriangles;
	}

}