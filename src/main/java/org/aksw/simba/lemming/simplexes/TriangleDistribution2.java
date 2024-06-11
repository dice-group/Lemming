package org.aksw.simba.lemming.simplexes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.PoissonDistribution;
import org.aksw.simba.lemming.util.MapUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * The class stores the colors of vertices for triangles in all possible combination. These combinations are utilized while generating the simulated graph.
 */
public class TriangleDistribution2 {
	
	/**
	 * HashMap to store different permutation of colors for triangle vertices. Note: Always sort the two input colors when evaluating against these maps.
	 * Example: If three vertices have colors such that 1st vertex color is represented by long value 1, 2nd vertex - long value 2, and 3rd vertex - long value 3. 
	 * The use case is when we have any two of these colors in sorted order, we should be able to determine the third color and its count.
	 * Possible case: given vertex colors 1 and 2, using first map we can get the third vertex color 3 and the count of such triangles in input graphs.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> mTriangleColorsv1v2v3;
	// Value of the hashmap stores an array. For this array, different values are stored at different indices as described below:
	// 0th index: Edge counts
	// 1st index: Triangle counts
	// 2nd index: Triangle probability
	// 3rd index: Average triangle count for output graph
	// 4th index: Probability of the third vertex given two vertices
	
	/**
	 * Variable to store count of distinct triangle colours, which is used to initialize collections. These collections help to compute probability distribution.
	 */
	private int numberOfDistinctTriangleColours;
	
	/**
	 * Variable to store number of input graphs.
	 */
	int iNoOfVersions;
	
	/**
	 * Object of Random class for generating random values.
	 */
	private Random mRandom;
	
	/**
	 * Object for storing distributions by analyzing triangles found in all input graphs.
	 */
	OfferedItemByRandomProb<TriColours> potentialTriangleProposer;
	
	/**
	 * Object for storing distributions by analyzing isolated triangles found in all input graphs.
	 */
	OfferedItemByRandomProb<TriColours> potentialIsolatedTriangleProposer;
	
	/**
	 * Map for storing probability distributions computed for two vertex colors.
	 * Note: The vertex color are sorted in ascending order. The vertex color having lower long value is the key for the first hashmap and the one with the highest long value is the key for the second hashmap.
	 */
	ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, OfferedItemByRandomProb<BitSet>>> mV1ColoV2ColoProbDist = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet,OfferedItemByRandomProb<BitSet>>>();
	
	public TriangleDistribution2(ObjectObjectOpenHashMap<TriColours, double[]> mTriColoEdgesTriCountDistAvg, ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColosCountsDist, int iNoOfVersions, int mIDesiredNoOfVertices, Random mRandom){
		
		this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;
		
		// Initializing the map variables
		mTriangleColorsv1v2v3 = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>>();
		//this.mTriangleColorsv1v2v3 = mTriangleColorsProbability;
		
		// Update average vertices count for triangles and create different permutations for triangle colors
		Object[] keysTriangleColours = mTriColoEdgesTriCountDistAvg.keys;
		for(int i = 0; i < keysTriangleColours.length ; i++) {
			if(mTriColoEdgesTriCountDistAvg.allocated[i]) {
				TriColours triangleColorObj = (TriColours) keysTriangleColours[i];
				double[] triangleCountsDistArr = mTriColoEdgesTriCountDistAvg.get(triangleColorObj);
				triangleCountsDistArr[3] = (triangleCountsDistArr[3] * mIDesiredNoOfVertices) /iNoOfVersions;
			
				// Update or insert into first map to store the combination of 1st vertex, 2nd vertex, and 3rd vertex, and their count
				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getA(), triangleColorObj.getB(), triangleColorObj.getC(), triangleCountsDistArr);
				
				// Update or insert into second map to store the combination of 2nd vertex, 3rd vertex, and 1st vertex, and their count
				// Note: BitSet objects are already sorted in the TriangleColors object, and here the first two BitSet parameters of the function should be sorted in ascending order
				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getB(), triangleColorObj.getC(), triangleColorObj.getA(), triangleCountsDistArr);
				
				// Update or insert into second map to store the combination of 2nd vertex, 3rd vertex, and 1st vertex, and their count
				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getA(), triangleColorObj.getC(), triangleColorObj.getB(), triangleCountsDistArr);
				
			}
		}
		
		numberOfDistinctTriangleColours = mTriColoEdgesTriCountDistAvg.assigned;
		
		//*************************** Below computation is performed for Triangle colours and edge counts **********************//
		// Below distribution is used to propose a triangle and more edges are added to this triangle.
		
		//Initialize distribution of triangle colors based on edge count
		initializeDistributionAllTriangles(mTriColoEdgesTriCountDistAvg, iNoOfVersions, mRandom);
		
		initializeDistributionIsolatedTri(mIsolatedTriColosCountsDist, iNoOfVersions, mRandom);
		
		// Add the probability of the third vertex given two vertices
		updateVertexProbabilities();
	}
	
	public void updateVertexProbabilities() {
		Object[] keysVertexColo1Tri = mTriangleColorsv1v2v3.keys; // get all 1st vertex colors
        for (int i=0; i< keysVertexColo1Tri.length;i++) {
        	if (mTriangleColorsv1v2v3.allocated[i]) {
        		BitSet vertexColo1Tri = (BitSet) keysVertexColo1Tri[i];// store 1st vertex color key
        		
        		ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> mVColo2VColo3CountTriTemp = mTriangleColorsv1v2v3.get(vertexColo1Tri);// get nested hashmap
        		Object[] keysVertexColo2Tri = mVColo2VColo3CountTriTemp.keys; // get all 2nd vertex colors (from the first nested hashmap)
        		for (int j=0; j < keysVertexColo2Tri.length; j++) {
        			if (mVColo2VColo3CountTriTemp.allocated[j]) {
        				BitSet vertexColo2Tri = (BitSet) keysVertexColo2Tri[j];// store 2nd vertex color key
        				
        				ObjectObjectOpenHashMap<BitSet, double[]> mVColo3CountTriTemp = mVColo2VColo3CountTriTemp.get(vertexColo2Tri);// get 2nd nested hashmap
        				Object[] keysVertexColo3Tri = mVColo3CountTriTemp.keys;// get all 3rd vertex colors (from the second nested hashmap)
        				
        				double tempTotalCountOfTriangles = 0;
        				//first iteration for the last hashmap to get the total distribution
        				for (int k=0; k < keysVertexColo3Tri.length; k++) {
        					if (mVColo3CountTriTemp.allocated[k]) {
        						BitSet vertexColo3Tri = (BitSet) keysVertexColo3Tri[k];
        						tempTotalCountOfTriangles = tempTotalCountOfTriangles + mVColo3CountTriTemp.get(vertexColo3Tri)[2];
        					}
        				}
        				
        				// Calculate distribution for the vertex colors and update the global hashmap
        				for (int k=0; k < keysVertexColo3Tri.length; k++) {
        					if (mVColo3CountTriTemp.allocated[k]) {
        						BitSet vertexColo3Tri = (BitSet) keysVertexColo3Tri[k];
        						double distributionValue = mVColo3CountTriTemp.get(vertexColo3Tri)[2] * (1.0) / tempTotalCountOfTriangles;
        						
        						ObjectObjectOpenHashMap<BitSet, double[]> mv3DoubleArr = mTriangleColorsv1v2v3.get(vertexColo1Tri).get(vertexColo2Tri);
        						
        						double[] arrCountsDistPrev = mv3DoubleArr.get(vertexColo3Tri);
        						
        						// create a new double array with additional element for probability of third vertex
        						double[] arrCountsDistNew = new double[5];
        						//assign previous values
        						arrCountsDistNew[0] = arrCountsDistPrev[0];
        						arrCountsDistNew[1] = arrCountsDistPrev[1];
        						arrCountsDistNew[2] = arrCountsDistPrev[2];
        						arrCountsDistNew[3] = arrCountsDistPrev[3];
        						// add new value
        						arrCountsDistNew[4] = distributionValue;
        						
        						mv3DoubleArr.put(vertexColo3Tri, arrCountsDistNew);        					
        					}
        				}
        				
        			}
        			
        		}
        	}
        }
	}
	
	/**
	 * This generic function is used to store the different permutation of TriangleColors. It evaluates the inputmap, if the colors are not already present then it creates new nested maps and stores them. Otherwise, the existing maps are updated.
	 * @param triColorMapToUpdate - Input map to update.
	 * @param firstVertexColor - First vertex color (first key .i.e. key for the outer map)
	 * @param secondVertexColor - Second vertex color (second key .i.e. key for the first nested map)
	 * @param thirdVertexColor - Third vertex color (third key .i.e. key for the second nested map)
	 * @param countToUpdate - Count for the vertex color of the triangle.
	 */
	private void storeTriangleColorsInMaps(ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> triColorMapToUpdate, BitSet firstVertexColor, BitSet secondVertexColor, 
			BitSet thirdVertexColor, double arrCountDist[]) {
		ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> mTriColorsv2v3Temp = triColorMapToUpdate.get(firstVertexColor);
		
		if (mTriColorsv2v3Temp == null) { // Logic when none of the colors are previously observed.
			
			// Temporary map for 3rd vertex (key) and count (value)
			ObjectObjectOpenHashMap<BitSet, double[]> mTriColorv3CountTemp = new ObjectObjectOpenHashMap<BitSet, double[]>();
			mTriColorv3CountTemp.put(thirdVertexColor, arrCountDist);
			
			
			//Temporary map for 2nd vertex (key) and map of 3rd vertex with count (value)
			ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> mTriColorv2v3Temp = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>();
			mTriColorv2v3Temp.put(secondVertexColor, mTriColorv3CountTemp);
			
			triColorMapToUpdate.put(firstVertexColor, mTriColorv2v3Temp);
		}else {
			//Logic when first vertex color was already previously observed
			
			// Checking if second vertex color also exists
			ObjectObjectOpenHashMap<BitSet, double[]> mTriColorv3Temp = mTriColorsv2v3Temp.get(secondVertexColor);
			
			if(mTriColorv3Temp == null) {
				// when second vertex color was not previously observed
				
				// Temporary map for 3rd vertex (key) and count (value)
				ObjectObjectOpenHashMap<BitSet, double[]> mTriColorv3CountTemp = new ObjectObjectOpenHashMap<BitSet, double[]>();
				mTriColorv3CountTemp.put(thirdVertexColor, arrCountDist);
				
				// add second vertex color and map of 3rd vertex with count (value)
				mTriColorsv2v3Temp.put(secondVertexColor, mTriColorv3CountTemp);
				
			}else {
				// Logic when first vertex and second vertex color already exists in the map.
				// Note: All three vertex colors could not present in the map. This is already evaluated in the Triangle metric computation
				
				// Add third vertex color 
				mTriColorv3Temp.put(thirdVertexColor, arrCountDist);
			}
		}
	}
	

	public ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> getmTriangleColorsv1v2v3() {
		return mTriangleColorsv1v2v3;
	}
	
	/**
	 * The function proposes a Color for the third vertex to create a triangle, given the colors of first and second vertices as input.
	 * @param vertex1Color - BitSet color for vertex 1.
	 * @param vertex2Color - BitSet color for vertex 2.
	 * @return - Bitset color for vertex 3.
	 */
	public BitSet proposeVertexColorForVertex3(BitSet vertex1Color, BitSet vertex2Color) {
		
		
		// arrange the input colors in ascending order by getting using their long values
		long[] v1Bits = vertex1Color.bits;
		double vertex1LongValue = 0.0;
		for (long tempValue: v1Bits) {
			vertex1LongValue = vertex1LongValue + tempValue;
		}
		
		long[] v2Bits = vertex2Color.bits;
		double vertex2LongValue = 0.0;
		for (long tempValue: v2Bits) {
			vertex2LongValue = vertex2LongValue + tempValue;
		}
		//long vertex1LongValue = vertex1Color.bits[0];
    	//long vertex2LongValue = vertex2Color.bits[0];
    	
    	BitSet sortedVertex1Colo, sortedVertex2Colo;
    	
    	if (vertex1LongValue < vertex2LongValue) {
    		sortedVertex1Colo = vertex1Color;
    		sortedVertex2Colo = vertex2Color;
    	}else {
    		sortedVertex1Colo = vertex2Color;
    		sortedVertex2Colo = vertex1Color;
    	}
    	
    	// check if previously for the input vertex colors the third vertex color was proposed
    	ObjectObjectOpenHashMap<BitSet,OfferedItemByRandomProb<BitSet>> mV2ColoProbDistOpenHashMap = mV1ColoV2ColoProbDist.get(sortedVertex1Colo);
    	if (mV2ColoProbDistOpenHashMap != null) {
    		OfferedItemByRandomProb<BitSet> probDist = mV2ColoProbDistOpenHashMap.get(sortedVertex2Colo);
    		if (probDist != null) {
    			return probDist.getPotentialItem();
    		}
    	} 
    	
    	BitSet thirdVertexColor = null;
    	// get the Map for the input vertex Colors
    	ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> mapPossV2V3ColoCount = mTriangleColorsv1v2v3.get(sortedVertex1Colo);
    	
    	
		if (mapPossV2V3ColoCount == null)
			return thirdVertexColor; // No map found that stores the selected vertex color for a triangle. No Triangle was found in the input graph for the selected colors.
    	
		// map to store possible third vertex color along with its count
		ObjectObjectOpenHashMap<BitSet, double[]> mapPossV3Count = mapPossV2V3ColoCount.get(sortedVertex2Colo);
		
		if (mapPossV3Count == null)
			return thirdVertexColor; //No third vertex color possible for the input colors, returning null
		
		//List<BitSet> listPossibleColoV3 = MapUtil.keysToList(mapPossV3Count);
		int sampleSize = mapPossV3Count.assigned;
		
		BitSet[] allPossibleColoV3 = new BitSet[sampleSize];
		
		// create array of double values for ObjectDistribution class
		double[] possEdgesInTriangle = new double[sampleSize];
		int sampleSpaceIndex = 0;
		
		Object[] keysVertColo = mapPossV3Count.keys;
		
		for(int i = 0; i < keysVertColo.length; i++) {
			if (mapPossV3Count.allocated[i]) {
				allPossibleColoV3[sampleSpaceIndex] = (BitSet) keysVertColo[i];
				double numOfEdgesV3 = mapPossV3Count.get((BitSet) keysVertColo[i])[4]; // get the triangle probability stored at first index
				
				 double sampleDistribution = numOfEdgesV3 * 1.0 /iNoOfVersions; // get average values
				 possEdgesInTriangle[sampleSpaceIndex] = sampleDistribution;
				 
				 sampleSpaceIndex++;
			}
		}
		
		// create distribution
		ObjectDistribution<BitSet> potentialColo = new ObjectDistribution<BitSet>(allPossibleColoV3, possEdgesInTriangle);
		OfferedItemByRandomProb<BitSet> potentialColoProposer = new OfferedItemByRandomProb<BitSet>(potentialColo, mRandom);
		
		// get potential third color
		thirdVertexColor = potentialColoProposer.getPotentialItem();
		
		// save the created probability distribution
		if (mV2ColoProbDistOpenHashMap == null) {
			// There is no distribution for vertex color 1 initializing the map
			mV2ColoProbDistOpenHashMap = new ObjectObjectOpenHashMap<BitSet, OfferedItemByRandomProb<BitSet>>();
		}
		// Add computed distribution to the map of vertex color 2 and probability distribution
		mV2ColoProbDistOpenHashMap.put(sortedVertex2Colo, potentialColoProposer);
		
		//Add to map vertex color 1, vertex color 2 and probability distribution 
		mV1ColoV2ColoProbDist.put(sortedVertex1Colo, mV2ColoProbDistOpenHashMap);
		
		return thirdVertexColor;
	}
	
	/**
	 * This function returns a TriangleColours from the input set of TriangleColours based on probability distribution of TriangleColours found in Input graphs. 
	 * 
	 * @param setTriangleColorsMimicGraph - Set of TriangleColours that needs to be filtered.
	 * @return
	 */
	public TriColours proposeTriangleToAddEdge(Set<TriColours> setTriangleColorsMimicGraph) {
		
		//Utilize initialized object distribution for all triangles

		// get the selected triangle colors
		TriColours potentialTriangleColours = potentialTriangleProposer.getPotentialItem(setTriangleColorsMimicGraph);
		
		for(TriColours triangleColourObject: setTriangleColorsMimicGraph) {
			if ( triangleColourObject.equals(potentialTriangleColours)) {
				return triangleColourObject;
			}
		}
		
		return null;
		
		
	}
	
	/**
	 * This method initializes the probability distribution of triangles. It checks all the triangles found in the input graphs and the number of edges in them to create the probability distribution.
	 * 
	 * @param inputSetTriangleColors - Set of TriangleColours object found after analyzing input graphs
	 * @param iNoOfVersions - Number of input graphs
	 * @param mRandom - Random object
	 */
	private void initializeDistributionAllTriangles(ObjectObjectOpenHashMap<TriColours, double[]> mTriangleColoursTriangleCountsEdgeCountsResourceNodes, int iNoOfVersions, Random mRandom) {
		
		// create sample space & values for triangle colors
		TriColours[] triangleColorsSampleSpace = new TriColours[numberOfDistinctTriangleColours];
		double[] possEdgesInTriangle = new double[numberOfDistinctTriangleColours];
		
		Object[] keysTriangleColours = mTriangleColoursTriangleCountsEdgeCountsResourceNodes.keys;
		
		
		int i = 0; //temp variable to track index of array
		for(int mapKeyIndex = 0; mapKeyIndex < keysTriangleColours.length ; mapKeyIndex++) {
			if(mTriangleColoursTriangleCountsEdgeCountsResourceNodes.allocated[mapKeyIndex]) {
				TriColours triangleColorObj = (TriColours) keysTriangleColours[mapKeyIndex];
				double[] triangleEdgeCountArr = mTriangleColoursTriangleCountsEdgeCountsResourceNodes.get(triangleColorObj);
				double countOfEdges = triangleEdgeCountArr[2];//probability of triangle is stored at 2nd index
				
				triangleColorsSampleSpace[i] = triangleColorObj;
				
				double tempMeanValue = countOfEdges * 1.0 / iNoOfVersions;
				possEdgesInTriangle[i] = tempMeanValue;
				
				i++;
			}
		}
		
		ObjectDistribution<TriColours> potentialTriangle = new ObjectDistribution<TriColours>(triangleColorsSampleSpace, possEdgesInTriangle);
		if (!potentialTriangle.isEmpty()) // When samplespace is empty, initialzing the proposer with null
			potentialTriangleProposer = new OfferedItemByRandomProb<TriColours>(potentialTriangle, mRandom);
		else
			potentialTriangleProposer = null;
		
	}
	
	/**
	 * This method initializes the probability distribution of isolated triangles. It checks all the isolated triangles found in the input graphs and the number of edges in them to create the probability distribution.
	 * 
	 * @param inputSetTriangleColors - Set of TriangleColours object found after analyzing input graphs
	 * @param iNoOfVersions - Number of input graphs
	 * @param mRandom - Random object
	 */
	private void initializeDistributionIsolatedTri(ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColosCountsDist, int iNoOfVersions, Random mRandom) {
		//TODO: Duplicate of previous method. Need to create a single method for this.
		
		int sampleSize = mIsolatedTriColosCountsDist.assigned;
		
		// create sample space & values for triangle colors
		TriColours[] triangleColorsSampleSpace = new TriColours[sampleSize];
		double[] possEdgesInTriangle = new double[sampleSize];
		
		Object[] keysTriangleColours = mIsolatedTriColosCountsDist.keys;
		
		
		int i = 0; //temp variable to track index of array
		for(int mapKeyIndex = 0; mapKeyIndex < keysTriangleColours.length ; mapKeyIndex++) {
			if(mIsolatedTriColosCountsDist.allocated[mapKeyIndex]) {
				TriColours triangleColorObj = (TriColours) keysTriangleColours[mapKeyIndex];
				double[] triangleEdgeCountArr = mIsolatedTriColosCountsDist.get(triangleColorObj);
				double countOfEdges = triangleEdgeCountArr[2];//probability of triangle is stored at 2nd index
				
				triangleColorsSampleSpace[i] = triangleColorObj;
				
				double tempMeanValue = countOfEdges * 1.0 / iNoOfVersions;
				possEdgesInTriangle[i] = tempMeanValue;
				
				i++;
			}
		}
		
		ObjectDistribution<TriColours> potentialTriangle = new ObjectDistribution<TriColours>(triangleColorsSampleSpace, possEdgesInTriangle);
		if (!potentialTriangle.isEmpty()) // When samplespace is empty, initialzing the proposer with null
			potentialIsolatedTriangleProposer = new OfferedItemByRandomProb<TriColours>(potentialTriangle, mRandom);
		else
			potentialIsolatedTriangleProposer = null;
		
	}

	public OfferedItemByRandomProb<TriColours> getPotentialIsolatedTriangleProposer() {
		return potentialIsolatedTriangleProposer;
	}
}
