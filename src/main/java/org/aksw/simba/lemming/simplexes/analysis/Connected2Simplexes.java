package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.TriangleColours;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * This class provides analysis information for isolated 2-simplexes.
 * Note: For computation efficiency, isolated triangles and connected triangles are computed using class FindTriangles. This class is required as input.
 */
public class Connected2Simplexes extends AbstractFindSimplexes{
	
	/**
	 * Map for storing vertex colors for the triangle along with the probability for them in terms of triangle count and edge count in an array.
	 * Note: Count of triangles is stored at the 0th index of the array, 
	 * count of edges in the triangle is stored at the 1st index, 
	 * the probability of triangle is stored at the 2nd index 
	 * and the average count of triangles per no. of vertices in the input graph is stored at the 3rd index. 
	 */
	private ObjectObjectOpenHashMap<TriangleColours, double[]> mTriColoEdgesTriCountDistAvg;

	public Connected2Simplexes(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, FindTriangles computedTriObjects) {
		// initialize variables
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		// get edges and vertices from FindTriangles class
		mGraphsEdgesIds = computedTriObjects.getmGraphsEdgesIdsConnectedTri();
		mGraphsVertIds = computedTriObjects.getmGraphsVertIdsConnectedTri();
		
		//compute color mapper, and estimated edges and vertices.
		computeColorMapper();
		estimateEdges();
		estimateVertices();
		
		//initialize computed count for connected triangles
		mTriColoEdgesTriCountDistAvg = computedTriObjects.getmTriColoEdgesTriCountDistAvg();
		
	}
	
	//getter

	public ObjectObjectOpenHashMap<TriangleColours, double[]> getmTriColoEdgesTriCountDistAvg() {
		return mTriColoEdgesTriCountDistAvg;
	}
	
	
}
