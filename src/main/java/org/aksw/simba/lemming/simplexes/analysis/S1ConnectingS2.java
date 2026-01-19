package org.aksw.simba.lemming.simplexes.analysis;

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
 * This class provides analysis information for 1-simplexes connecting 2-simplexes.
 * Note: It requires connected triangles computed using class FindTriangles. Thus, this class is required as input.
 */
public class S1ConnectingS2 extends AbstractFindSimplexes {
	
	/**
	 * Temporary map object for storing all edge Ids for connected 2-simplexes
	 * Note: They are used to calculate 1-simplexes connecting 2-simplexes.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsConnTriTemp;
	
	/**
	 * Map for storing count of head colors for 1-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mHeadColoCount;
	
	/**
	 * Map for storing count of head and tail colors for 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount;
	
	public S1ConnectingS2(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, FindTri computedTriObjects){
		// initialize variables
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		// get edges and vertices from FindTriangles class
		mGraphsEdgesIdsConnTriTemp = computedTriObjects.getmGraphsEdgesIdsConnectedTri();
		mGraphsVertIds = computedTriObjects.getmGraphsVertIdsConnectedTri();
		
		//initialize edgeIds
		mGraphsEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		// Initialize head and hed-tail color maps
		mHeadColoCount = new ObjectDoubleOpenHashMap<BitSet>();
		mHeadColoTailColoCount = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
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
				
				ObjectIntOpenHashMap<BitSet> mheadColoCountTemp = new ObjectIntOpenHashMap<BitSet>(); //temporary map to store head colors found in 1-simplex for a specific graph
				
				ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>(); //temporary map to store count of head-tail color found in 1-simplexes for a specific graph
				
				int sampleSizeHeadColor = 0; //temporary variable to track total number of head colors
		    	
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
						//mHeadColoCount.putOrAdd(headColo, 1, 1); // commenting using temporary map to calculcate distribution
						mheadColoCountTemp.putOrAdd(headColo, 1, 1);
						sampleSizeHeadColor = sampleSizeHeadColor + 1;
						
						// store the details of head and tail color for 1-simplex in the map
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeIdNotTri);
						BitSet tailColo = graph.getVertexColour(tailOfTheEdge);
						//ObjectIntOpenHashMap<BitSet> mtailColoCount = mHeadColoTailColoCount.get(headColo); // commenting using temporary map to calculate distribution
						ObjectIntOpenHashMap<BitSet> mtailColoCount = mHeadTailColoCountTemp.get(headColo);
						if (mtailColoCount == null) {
							mtailColoCount = new ObjectIntOpenHashMap<BitSet>();
						}
						mtailColoCount.putOrAdd(tailColo, 1, 1);
						//mHeadColoTailColoCount.put(headColo, mtailColoCount); // commenting using temporary map to calculate distribution
						mHeadTailColoCountTemp.put(headColo, mtailColoCount);
					}
				}
				
				mGraphsEdgesIds.put(graphId, edgeIdConnectingTriangles);
				
				// update global map
				updateMapsTrackingDistributions(mheadColoCountTemp, mHeadTailColoCountTemp, mHeadColoCount, mHeadColoTailColoCount, sampleSizeHeadColor);
				
				graphId++;
			}
		}
	}
	
	
	// Getters
	public ObjectDoubleOpenHashMap<BitSet> getmHeadColoCount() {
		return mHeadColoCount;
	}

	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCount() {
		return mHeadColoTailColoCount;
	}
	
}
