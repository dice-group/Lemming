package org.aksw.simba.lemming.simplexes.analysis;

import java.util.HashSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class stores analysis results for 1-simplexes that are connected to 2-simplexes.
 * Note: There are two cases here. 
 * Case 1) 1-simplexes linked to other 1-simplexes is connected to 2-simplexes.
 * Case 2) 1-simplex is just connected to 2-simplexes.
 */
public class S1ConnToS2 extends AbstractFindSimplexes {
	
	/**
	 * Temporary Map object storing vertex Ids found for 2-simplexes in every input graph.
	 * Note: They are used for finding 1-simplexes connected to 2-simplexes
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsTemp;
	
	/**
	 * Map for storing vertex colors for 1-simplexes connected to triangles
	 */
	private ObjectDoubleOpenHashMap<BitSet> mColoCountVertConnectedToTriangle = new ObjectDoubleOpenHashMap<BitSet>();
	
	/**
	 * Map for storing count of head and tail colors for 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount;
	
	/**
	 * Map for storing count of head colors for 1-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mHeadColoCount;
	

	public S1ConnToS2(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, FindTri computedTriObjects) {
		// initialize variables
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		// get vertices of connected 2-simplexes from FindTriangles class
		mGraphsVertIdsTemp = computedTriObjects.getmGraphsVertIdsConnectedTri();
		
		// initialize count map 
		mColoCountVertConnectedToTriangle = new ObjectDoubleOpenHashMap<BitSet>();
		
		//initialize head and head-tail map
		mHeadColoCount = new ObjectDoubleOpenHashMap<BitSet>();
		mHeadColoTailColoCount = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
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
		        System.out.println("Graph: " + graphId);
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
		        
				//temporary variable to track total number of head colors
				int sampleSizeHeadColor = 0;
		        ObjectIntOpenHashMap<BitSet> mheadColoCountTemp = new ObjectIntOpenHashMap<BitSet>(); //temporary map to store head colors found in 1-simplex for a specific graph
		        ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>(); //temporary map to store count of head-tail color found in 1-simplexes for a specific graph
		        
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
								//mHeadColoCount.putOrAdd(headColo, 1, 1); // commenting using temporary map to calculcate distribution
								mheadColoCountTemp.putOrAdd(headColo, 1, 1);
								sampleSizeHeadColor = sampleSizeHeadColor + 1;
									
								// store the details of head and tail color for 1-simplex in the map
								int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
								BitSet tailColo = graph.getVertexColour(tailOfTheEdge);
								//ObjectIntOpenHashMap<BitSet> mtailColoCount = mHeadColoTailColoCount.get(headColo); // commenting using temporary map to calculate distribution
								ObjectIntOpenHashMap<BitSet> mtailColoCount = mHeadTailColoCountTemp.get(headColo);
								if (mtailColoCount == null) {
									mtailColoCount = new ObjectIntOpenHashMap<BitSet>();
								}
								mtailColoCount.putOrAdd(tailColo, 1, 1);
								mHeadTailColoCountTemp.put(headColo, mtailColoCount);
							}
			        		
			        		
		            	}
		            	
		            	
		        	}
		        }
		        
		        //Compute distribution of vertices connecting to triangles and update in global map
		        updateSingleColoMapTrackingDistribution(mVertColoConnectTriangleTemp, mColoCountVertConnectedToTriangle, numSimplexesVertConnectTriangle);
		        
		        // compute distribution of head and head tail colors and update in global map
		        updateMapsTrackingDistributions(mheadColoCountTemp, mHeadTailColoCountTemp, mHeadColoCount, mHeadColoTailColoCount, sampleSizeHeadColor);
		        
		        mGraphsEdgesIds.put(graphId, commonEdgesTriangles);
		        mGraphsVertIds.put(graphId, verticesConnectingToTriangle);
				
				graphId++;
			}
		}
	}

	// getters
	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCount() {
		return mHeadColoTailColoCount;
	}

	public ObjectDoubleOpenHashMap<BitSet> getmHeadColoCount() {
		return mHeadColoCount;
	}

	public ObjectDoubleOpenHashMap<BitSet> getmColoCountVertConnectedToTriangle() {
		return mColoCountVertConnectedToTriangle;
	}
}
