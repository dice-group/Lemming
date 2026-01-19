package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.TriColours;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * This class provides analysis information for isolated 2-simplexes.
 * Note: For computation efficiency, isolated triangles and connected triangles are computed using class FindTriangles. This class is required as input.
 */
public class IsoS2 extends AbstractFindSimplexes{
	
	/**
	 * Map for storing vertex colors specifically for isolated triangles along with the probability for them in terms of triangle count and edge count in an array.
	 * Note: Count of triangles is stored at the 0th index of the array, 
	 * count of edges in the triangle is stored at the 1st index, 
	 * and the probability of triangle is stored at the 2nd index.  
	 */
	private ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColoEdgesTriCountDistAvg;

	public IsoS2(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, FindTri computedTriObjects) {
		//System.out.println("Debug!");
		// initialize variables
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		// get edges and vertices from FindTriangles class
		mGraphsEdgesIds = computedTriObjects.getmGraphsEdgesIdsIsolatedTri();
		mGraphsVertIds = computedTriObjects.getmGraphsVertIdsIsolatedTri();
		
		//compute color mapper, and estimated edges and vertices.
		computeColorMapper();
		estimateEdges();
		estimateVertices();
		
		//initialize computed count for isolated triangles
		mIsolatedTriColoEdgesTriCountDistAvg = computedTriObjects.getmIsolatedTriColoEdgesTriCountDistAvg();
		
	}

	//getters
	public ObjectObjectOpenHashMap<TriColours, double[]> getmIsolatedTriColoEdgesTriCountDistAvg() {
		return mIsolatedTriColoEdgesTriCountDistAvg;
	}

}
