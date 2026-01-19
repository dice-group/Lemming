package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class S1ConnectingS2C extends AbstractFindSimplexes {

	/**
	 * Temporary map object for storing all edge Ids for connected 2-simplexes
	 * Note: They are used to calculate 1-simplexes connecting 2-simplexes.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsConnTriTemp;
	
	/**
	 * Map for storing vertex colors for the edge along with the probability for them and distributions in an array.
	 * Note: Count of edges is stored at the 0th index of the array,  
	 * the probability of an edge is stored at the 1st index 
	 * and the average count of edges per no. of vertices in the input graph is stored at the 2nd index. 
	 */
	private ObjectObjectOpenHashMap<EdgeColos, double[]> mColoEdgesCountDistAvg = new ObjectObjectOpenHashMap<EdgeColos, double[]>();
	
	
	
	public S1ConnectingS2C(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, FindTri computedTriObjects){
		// initialize variables
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		// get edges and vertices from FindTriangles class
		mGraphsEdgesIdsConnTriTemp = computedTriObjects.getmGraphsEdgesIdsConnectedTri();
		mGraphsVertIds = computedTriObjects.getmGraphsVertIdsConnectedTri();
		
		//initialize edgeIds
		mGraphsEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		//compute color mapper, and estimated edges and vertices.
		findSimplexes();
		computeColorMapper();
		estimateEdges();
		//estimateVertices(); // Vertices exist for connected 2-simplexes, which are connected. Thus, vertices should not be estimated for this case. 
	}
	
	@Override
	public void findSimplexes() {
		
		//initialize graph id
		int graphId = 1;
		
		for (ColouredGraph graph : inputGrphs) {
			if (graph!= null) {
				
		    	IntSet edgesWithinTrianglesResource = mGraphsEdgesIdsConnTriTemp.get(graphId); // temporary variable for tracking edge Ids within triangle
		    	IntSet verticesOnlyFormingTriangleResource = mGraphsVertIds.get(graphId); // temporary variable for tracking vert ids within triangle
				
		    	ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColosCountsTemp = new ObjectObjectOpenHashMap<EdgeColos, double[]>(); // temporary map to store edge colors
				
		    	
				IntSet edgesNotInTriangles = IntSetUtil.difference(graph.getEdges(), edgesWithinTrianglesResource);
				IntSet edgeIdConnectingTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
				for (int edgeIdNotTri : edgesNotInTriangles) {
					IntSet verticesEdgeNotTri = graph.getVerticesIncidentToEdge(edgeIdNotTri);
					if ((verticesOnlyFormingTriangleResource.containsAll(verticesEdgeNotTri)) && (verticesEdgeNotTri.size() == 2)) { 
						//update the condition vertices incident on edge should be 2
						edgeIdConnectingTriangles.add(edgeIdNotTri);
						
						// store the details of head color for 1-simplex in the map
						int headOfTheEdge = graph.getHeadOfTheEdge(edgeIdNotTri);
						BitSet headColo = graph.getVertexColour(headOfTheEdge);
						
						
						// store the details of head and tail color for 1-simplex in the map
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeIdNotTri);
						BitSet tailColo = graph.getVertexColour(tailOfTheEdge);
						
						//*****************Logic to store edge colors information **********************//
						EdgeColos edgeColoObj = new EdgeColos(headColo, tailColo); // initialize Edge Colors object
						
						if (mEdgeColosCountsTemp.containsKey(edgeColoObj)) {
							// Edge Color was found previously update the count in map
							double[] valuesEdgeColo = mEdgeColosCountsTemp.get(edgeColoObj);
							valuesEdgeColo[0]= valuesEdgeColo[0] + 1.0;
							valuesEdgeColo[1]= valuesEdgeColo[1] + 1.0; // probability will be updated later
							valuesEdgeColo[2]= valuesEdgeColo[2] + 1.0; // distribution of edge will be updated later
							
							mEdgeColosCountsTemp.put(edgeColoObj, valuesEdgeColo);
						}else {
							// Edge Color found for the first time
							double[] valuesEdgeColo = new double[3];
							valuesEdgeColo[0] = 1.0;
							valuesEdgeColo[1] = 1.0; // probability will be updated later
							valuesEdgeColo[2] = 1.0; // distribution of edge will be updated later
							
							mEdgeColosCountsTemp.put(edgeColoObj, valuesEdgeColo);
							
							
						}
					}
				}
				
				mGraphsEdgesIds.put(graphId, edgeIdConnectingTriangles);
				
				//update global map to store Edge Colors combination
				updateCountGlobalMap1Simplexes(graph, mColoEdgesCountDistAvg, mEdgeColosCountsTemp, edgeIdConnectingTriangles.size());
				
				graphId++;
			}
		}
	}

	
	
	
	// Getters
	public ObjectObjectOpenHashMap<EdgeColos, double[]> getmColoEdgesCountDistAvg() {
		return mColoEdgesCountDistAvg;
	}
	


}
