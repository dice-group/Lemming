package org.aksw.simba.lemming.simplexes.analysis;

import java.util.HashSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class S1ConnToS2C extends AbstractFindSimplexes {
	
	/**
	 * Map for storing vertex colors for the edge along with the probability for them and distributions in an array.
	 * Note: Count of edges is stored at the 0th index of the array,  
	 * the probability of an edge is stored at the 1st index 
	 * and the average count of edges per no. of vertices in the input graph is stored at the 2nd index. 
	 */
	private ObjectObjectOpenHashMap<EdgeColos, double[]> mColoEdgesCountDistAvg = new ObjectObjectOpenHashMap<EdgeColos, double[]>();

	/**
	 * Temporary Map object storing vertex Ids found for 2-simplexes in every input graph.
	 * Note: They are used for finding 1-simplexes connected to 2-simplexes
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsTemp;
	
	/**
	 * Map for storing vertex colors for 1-simplexes connected to triangles
	 */
	private ObjectDoubleOpenHashMap<BitSet> mColoCountVertConnectedToTriangle = new ObjectDoubleOpenHashMap<BitSet>();
	

	public S1ConnToS2C(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, FindTri computedTriObjects) {
		// initialize variables
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		// get vertices of connected 2-simplexes from FindTriangles class
		mGraphsVertIdsTemp = computedTriObjects.getmGraphsVertIdsConnectedTri();
		
		// initialize count map 
		mColoCountVertConnectedToTriangle = new ObjectDoubleOpenHashMap<BitSet>();
		
		// initialize edge and vertex ids for simplexes of different input graphs
		mGraphsEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		mGraphsVertIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		//compute color mapper, and estimated edges and vertices.
		findSimplexes();
		computeColorMapper();
		estimateEdges();
		estimateVertices();

	}
	
	@Override
	public void findSimplexes() {
		
		//initialize graph id
		int graphId = 1;
		
		for (ColouredGraph graph : inputGrphs) {
			if (graph!= null) {
				IntSet vertGrph = graph.getVertices();
				
				IntSet verticesOnlyFormingTriangleResource = mGraphsVertIdsTemp.get(graphId); // temporary variable for tracking vert ids within triangle
				
		        //Logic to find set of class vertices
		        IntSet classVerticesSet = new DefaultIntSet(Constants.DEFAULT_SIZE);
//		        System.out.println("Graph: " + graphId);
		        for (int edgeId: graph.getEdges()) {
		        	if (graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour())) {
		        		int classVertexID = graph.getHeadOfTheEdge(edgeId);
		        		classVerticesSet.add(classVertexID);
		        	}
		        }
		        
		        
		    	IntSet commonEdgesTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE); // temporary variable to store common edge Ids connected to vertices in triangles and other vertices that are not in triangles
		    	IntSet verticesConnectingToTriangle = new DefaultIntSet(Constants.DEFAULT_SIZE); //temporary variables to store vertex IDs connecting to triangles. Note: The stored vertex ids are analyzed to determine self loops & creating new vertices for output graph
				
		        IntSet diffTotalVertices = IntSetUtil.difference(vertGrph, verticesOnlyFormingTriangleResource);
		        diffTotalVertices = IntSetUtil.difference(diffTotalVertices, classVerticesSet);
		        
		        // variable to track number of simplexes connected to Vertices of triangle
		        int numSimplexesVertConnectTriangle = 0;
		        // Map for storing Color of vertices connecting to triangle. Later, this map is used to compute distributions
		        ObjectIntOpenHashMap<BitSet> mVertColoConnectTriangleTemp = new ObjectIntOpenHashMap<BitSet>();
		        
				ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColosCountsTemp = new ObjectObjectOpenHashMap<EdgeColos, double[]>(); // temporary map to store edge colors
		        
		        // iterate over vertices not in triangles and check if they are connected to the vertices in triangles.
		        for(int vertexId: diffTotalVertices) {
		        	IntSet neighbors = IntSetUtil.union(graph.getInNeighbors(vertexId), graph.getOutNeighbors(vertexId));
		        	neighbors = IntSetUtil.difference(neighbors, classVerticesSet);// do not consider class vertices
		        	if (neighbors.contains(vertexId))
		        		neighbors.remove(vertexId); // in case of self loop, a vertex will be neighbor of itself. We do not want to consider it.
		        	
		        	IntSet intersectionTriangleVerts = IntSetUtil.intersection(neighbors, verticesOnlyFormingTriangleResource);
		        	for(int intersectVertId: intersectionTriangleVerts) {
		        		
		        		IntSet edgesVertexId = IntSetUtil.union(graph.getInEdges(vertexId), graph.getOutEdges(vertexId));
		        		IntSet edgesintersectVertId = IntSetUtil.union(graph.getInEdges(intersectVertId), graph.getOutEdges(intersectVertId));
		        		
		        		
		        		IntSet tempIntersection = IntSetUtil.intersection(edgesVertexId, edgesintersectVertId);
		        		
		        		
		            	HashSet<BitSet> setEdgeColors = new HashSet<BitSet>(); // Set to store colors of every edge within the nodes forming a triangle
		            	for(int edgeIdVal: tempIntersection ) {
		            		setEdgeColors.add(graph.getEdgeColour(edgeIdVal));
		            	}
		            	
		            	
		            	if (!setEdgeColors.contains(graph.getRDFTypePropertyColour())) {
			        		commonEdgesTriangles.addAll(tempIntersection); // store edge ids connected to triangle
			        		
			        		numSimplexesVertConnectTriangle++; // calculate number of simplexes found required to compute average count
			        		//mColoCountVertConnectedToTriangle.putOrAdd(graph.getVertexColour(vertexId), 1, 1); //commenting using temporary map to compute the distribution
			        		
			        		if (verticesOnlyFormingTriangleResource.containsAll(neighbors)) { // check 1-simplex is only connected to triangles
			        			// Note: vertices need to be created for such 1-simplexes. Thus, the count of the vertex color is stored and it is later used to create distribution
			        			mVertColoConnectTriangleTemp.putOrAdd(graph.getVertexColour(vertexId), 1, 1);
			        			verticesConnectingToTriangle.add(vertexId);
			        		}
			        		
			        		// update the count for found common edges
			        		for (int edgeId: tempIntersection) {
									
								// store the details of head color for 1-simplex in the map
								int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
								BitSet headColo = graph.getVertexColour(headOfTheEdge);
								
								
									
								// store the details of head and tail color for 1-simplex in the map
								int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
								BitSet tailColo = graph.getVertexColour(tailOfTheEdge);
								
								//*****************Logic to store edge colors information **********************//
								EdgeColos edgeColoObj = new EdgeColos(headColo, tailColo); // initialize Edge Colors object
								
								if (mEdgeColosCountsTemp.containsKey(edgeColoObj)) {
									// Edge Color was found previously update the count in map
									double[] valuesEdgeColo = mEdgeColosCountsTemp.get(edgeColoObj);
									valuesEdgeColo[0]= valuesEdgeColo[0] + 1.0;
									valuesEdgeColo[1]= valuesEdgeColo[1] + 1.0; // probability will updated later
									valuesEdgeColo[2]= valuesEdgeColo[2] + 1.0; // distribution of edge will be updated later
									
									mEdgeColosCountsTemp.put(edgeColoObj, valuesEdgeColo);
								}else {
									// Edge Color found for the first time
									double[] valuesEdgeColo = new double[3];
									valuesEdgeColo[0] = 1.0;
									valuesEdgeColo[1] = 1.0; // probability will updated later
									valuesEdgeColo[2] = 1.0; // distribution of edge will be updated later
									
									mEdgeColosCountsTemp.put(edgeColoObj, valuesEdgeColo);
									
								}
								
							}
			        		
			        		
		            	}
		            	
		            	
		        	}
		        }
		        
		        //Compute distribution of vertices connecting to triangles and update in global map
		        updateSingleColoMapTrackingDistribution(mVertColoConnectTriangleTemp, mColoCountVertConnectedToTriangle, numSimplexesVertConnectTriangle);
		        
		        
		        mGraphsEdgesIds.put(graphId, commonEdgesTriangles);
		        mGraphsVertIds.put(graphId, verticesConnectingToTriangle);
		        
		      //update global map to store Edge Colors combination
				updateCountGlobalMap1Simplexes(graph, mColoEdgesCountDistAvg, mEdgeColosCountsTemp, commonEdgesTriangles.size());
				
				graphId++;
			}
		}
	}

	public ObjectObjectOpenHashMap<EdgeColos, double[]> getmColoEdgesCountDistAvg() {
		return mColoEdgesCountDistAvg;
	}

	public ObjectDoubleOpenHashMap<BitSet> getmColoCountVertConnectedToTriangle() {
		return mColoCountVertConnectedToTriangle;
	}

}
