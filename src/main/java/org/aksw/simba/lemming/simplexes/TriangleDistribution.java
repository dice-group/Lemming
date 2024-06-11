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
public class TriangleDistribution {
	
	/**
	 * HashMap to store different permutation of colors for triangle vertices. Note: Always sort the two input colors when evaluating against these maps.
	 * Example: If three vertices have colors such that 1st vertex color is represented by long value 1, 2nd vertex - long value 2, and 3rd vertex - long value 3. 
	 * The use case is when we have any two of these colors in sorted order, we should be able to determine the third color and its count.
	 * Possible case: given vertex colors 1 and 2, using first map we can get the third vertex color 3 and the count of such triangles in input graphs.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>> mTriangleColorsv1v2v3;
	
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
	 * Map for storing probability distributions computed for two vertex colors.
	 * Note: The vertex color are sorted in ascending order. The vertex color having lower long value is the key for the first hashmap and the one with the highest long value is the key for the second hashmap.
	 */
	ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, OfferedItemByRandomProb<BitSet>>> mV1ColoV2ColoProbDist = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet,OfferedItemByRandomProb<BitSet>>>();
	
	public TriangleDistribution(ObjectObjectOpenHashMap<TriColours, double[]> mTriangleColoursTriangleCountsEdgeCountsResourceNodes, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>> mTriangleColorsProbability, int iNoOfVersions, Random mRandom){
		
		this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;
		
		// Initializing the map variables
		mTriangleColorsv1v2v3 = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>>();
		this.mTriangleColorsv1v2v3 = mTriangleColorsProbability;
		
//  	Commenting below computation of triangle permutations, since this is required to be done in metric itself.		
//		int tempCounterTriangleColours = 0;
//		
//		//********************** Below computation is done for Triangle colours and count of triangles ********************************//
//		// The below created maps are used to propose a new vertex for forming a triangle given two vertices
//		
//		Object[] keysVertexColo1Tri = mTriangleColorsProbability.keys; // get all 1st vertex colors
//        for (int i=0; i< keysVertexColo1Tri.length;i++) {
//        	if (mTriangleColorsProbability.allocated[i]) {
//        		BitSet vertexColo1Tri = (BitSet) keysVertexColo1Tri[i];// store 1st vertex color key
//        		
//        		ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mVColo2VColo3CountTriTemp = mTriangleColorsProbability.get(vertexColo1Tri);// get nested hashmap
//        		Object[] keysVertexColo2Tri = mVColo2VColo3CountTriTemp.keys; // get all 2nd vertex colors (from the first nested hashmap)
//        		for (int j=0; j < keysVertexColo2Tri.length; j++) {
//        			if (mVColo2VColo3CountTriTemp.allocated[j]) {
//        				BitSet vertexColo2Tri = (BitSet) keysVertexColo2Tri[j];// store 2nd vertex color key
//        				
//        				ObjectDoubleOpenHashMap<BitSet> mVColo3CountTriTemp = mVColo2VColo3CountTriTemp.get(vertexColo2Tri);// get 2nd nested hashmap
//        				Object[] keysVertexColo3Tri = mVColo3CountTriTemp.keys;// get all 3rd vertex colors (from the second nested hashmap)
//        				
//        				//first iteration for the last hashmap to get the distribution
//        				for (int k=0; k < keysVertexColo3Tri.length; k++) {
//        					if (mVColo3CountTriTemp.allocated[k]) {
//        						BitSet vertexColo3Tri = (BitSet) keysVertexColo3Tri[k];
//        						 double distValue = mVColo3CountTriTemp.get(vertexColo3Tri);
//        						 storeTriangleColorsInMaps(mTriangleColorsv1v2v3, vertexColo1Tri, vertexColo2Tri, vertexColo3Tri, distValue);
//        						 storeTriangleColorsInMaps(mTriangleColorsv1v2v3, vertexColo2Tri, vertexColo3Tri, vertexColo1Tri, distValue);
//        						 storeTriangleColorsInMaps(mTriangleColorsv1v2v3, vertexColo1Tri, vertexColo3Tri, vertexColo2Tri, distValue);
//        						 tempCounterTriangleColours++;
//        					}
//        				}
//        				
//        				
//        			}
//        			
//        		}
//        	}
//        }
		
//      Object[] keysTriangleColours = mTriangleColoursTriangleCountsEdgeCountsResourceNodes.keys;
//		for(int i = 0; i < keysTriangleColours.length ; i++) {
//			if(mTriangleColoursTriangleCountsEdgeCountsResourceNodes.allocated[i]) {
//				TriangleColours triangleColorObj = (TriangleColours) keysTriangleColours[i];
//				double[] triangleCountsEdgeCountArr = mTriangleColoursTriangleCountsEdgeCountsResourceNodes.get(triangleColorObj);
//				double countOfTriangles = triangleCountsEdgeCountArr[0];
//			
//				// Update or insert into first map to store the combination of 1st vertex, 2nd vertex, and 3rd vertex, and their count
//				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getA(), triangleColorObj.getB(), triangleColorObj.getC(), countOfTriangles);
//				
//				// Update or insert into second map to store the combination of 2nd vertex, 3rd vertex, and 1st vertex, and their count
//				// Note: BitSet objects are already sorted in the TriangleColors object, and here the first two BitSet parameters of the function should be sorted in ascending order
//				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getB(), triangleColorObj.getC(), triangleColorObj.getA(), countOfTriangles);
//				
//				// Update or insert into second map to store the combination of 2nd vertex, 3rd vertex, and 1st vertex, and their count
//				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getA(), triangleColorObj.getC(), triangleColorObj.getB(), countOfTriangles);
//				
//				tempCounterTriangleColours++;
//			}
//			
//		}
		
		numberOfDistinctTriangleColours = mTriangleColoursTriangleCountsEdgeCountsResourceNodes.assigned;
		
		//*************************** Below computation is performed for Triangle colours and edge counts **********************//
		// Below distribution is used to propose a triangle and more edges are added to this triangle.
		
		//Initialize distribution of triangle colors based on edge count
		initializeDistributionAllTriangles(mTriangleColoursTriangleCountsEdgeCountsResourceNodes, iNoOfVersions, mRandom);
		
	}
	
//	private void initializeMapsTriangleColors(HashSet<TriangleColours> inputSetTriangleColors){
//		
//		for(TriangleColours triangleColorObj: inputSetTriangleColors) {
//			
//			// first permutation - v1, v2, v3
//			ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, Integer>> mTriColorsv2v3Temp = mTriangleColorsv1v2v3.get(triangleColorObj.getA());
//			
//			if (mTriColorsv2v3Temp == null) { // Logic when none of the colors are previously observed.
//				
//				// Temporary map for 3rd vertex (key) and count (value)
//				ObjectObjectOpenHashMap<BitSet, Integer> mTriColorv3CountTemp = new ObjectObjectOpenHashMap<BitSet, Integer>();
//				mTriColorv3CountTemp.put(triangleColorObj.getC(), triangleColorObj.getCount());
//				
//				
//				//Temporary map for 2nd vertex (key) and map of 3rd vertex with count (value)
//				ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, Integer>> mTriColorv2v3Temp = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, Integer>>();
//				mTriColorv2v3Temp.put(triangleColorObj.getB(), mTriColorv3CountTemp);
//				
//				mTriangleColorsv1v2v3.put(triangleColorObj.getA(), mTriColorv2v3Temp);
//			}else {
//				//Logic when first vertex color was already previously observed
//				
//				// Checking if second vertex color also exists
//				ObjectObjectOpenHashMap<BitSet,Integer> mTriColorv3Temp = mTriColorsv2v3Temp.get(triangleColorObj.getB());
//				
//				if(mTriColorv3Temp == null) {
//					// when second vertex color was not previously observed
//					
//					// Temporary map for 3rd vertex (key) and count (value)
//					ObjectObjectOpenHashMap<BitSet, Integer> mTriColorv3CountTemp = new ObjectObjectOpenHashMap<BitSet, Integer>();
//					mTriColorv3CountTemp.put(triangleColorObj.getC(), triangleColorObj.getCount());
//					
//					// add second vertex color and map of 3rd vertex with count (value)
//					mTriColorsv2v3Temp.put(triangleColorObj.getB(), mTriColorv3CountTemp);
//					
//				}else {
//					// Logic when first vertex and second vertex color already exists in the map.
//					// Note: All three vertex colors could not present in the map. This is already evaluated in the Triangle metric computation
//					
//					// Add third vertex color 
//					mTriColorv3Temp.put(triangleColorObj.getC(), triangleColorObj.getCount());
//				}
//			}
//		}
//	}
	
	/**
	 * This generic function is used to store the different permutation of TriangleColors. It evaluates the inputmap, if the colors are not already present then it creates new nested maps and stores them. Otherwise, the existing maps are updated.
	 * @param triColorMapToUpdate - Input map to update.
	 * @param firstVertexColor - First vertex color (first key .i.e. key for the outer map)
	 * @param secondVertexColor - Second vertex color (second key .i.e. key for the first nested map)
	 * @param thirdVertexColor - Third vertex color (third key .i.e. key for the second nested map)
	 * @param countToUpdate - Count for the vertex color of the triangle.
	 */
	private void storeTriangleColorsInMaps(ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>> triColorMapToUpdate, BitSet firstVertexColor, BitSet secondVertexColor, BitSet thirdVertexColor, double countToUpdate) {
		ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mTriColorsv2v3Temp = triColorMapToUpdate.get(firstVertexColor);
		
		if (mTriColorsv2v3Temp == null) { // Logic when none of the colors are previously observed.
			
			// Temporary map for 3rd vertex (key) and count (value)
			ObjectDoubleOpenHashMap<BitSet> mTriColorv3CountTemp = new ObjectDoubleOpenHashMap<BitSet>();
			mTriColorv3CountTemp.put(thirdVertexColor, countToUpdate);
			
			
			//Temporary map for 2nd vertex (key) and map of 3rd vertex with count (value)
			ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mTriColorv2v3Temp = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
			mTriColorv2v3Temp.put(secondVertexColor, mTriColorv3CountTemp);
			
			triColorMapToUpdate.put(firstVertexColor, mTriColorv2v3Temp);
		}else {
			//Logic when first vertex color was already previously observed
			
			// Checking if second vertex color also exists
			ObjectDoubleOpenHashMap<BitSet> mTriColorv3Temp = mTriColorsv2v3Temp.get(secondVertexColor);
			
			if(mTriColorv3Temp == null) {
				// when second vertex color was not previously observed
				
				// Temporary map for 3rd vertex (key) and count (value)
				ObjectDoubleOpenHashMap<BitSet> mTriColorv3CountTemp = new ObjectDoubleOpenHashMap<BitSet>();
				mTriColorv3CountTemp.put(thirdVertexColor, countToUpdate);
				
				// add second vertex color and map of 3rd vertex with count (value)
				mTriColorsv2v3Temp.put(secondVertexColor, mTriColorv3CountTemp);
				
			}else {
				// Logic when first vertex and second vertex color already exists in the map.
				// Note: All three vertex colors could not present in the map. This is already evaluated in the Triangle metric computation
				
				// Add third vertex color 
				mTriColorv3Temp.put(thirdVertexColor, countToUpdate);
			}
		}
	}

	public ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>> getmTriangleColorsv1v2v3() {
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
		long vertex1LongValue = vertex1Color.bits[0];
    	long vertex2LongValue = vertex2Color.bits[0];
    	
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
    	ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mapPossV2V3ColoCount = mTriangleColorsv1v2v3.get(sortedVertex1Colo);
    	
    	
		if (mapPossV2V3ColoCount == null)
			return thirdVertexColor; // No map found that stores the selected vertex color for a triangle. No Triangle was found in the input graph for the selected colors.
    	
		// map to store possible third vertex color along with its count
		ObjectDoubleOpenHashMap<BitSet> mapPossV3Count = mapPossV2V3ColoCount.get(sortedVertex2Colo);
		
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
				double numOfEdgesV3 = mapPossV3Count.get((BitSet) keysVertColo[i]);
				
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
				double countOfEdges = triangleEdgeCountArr[1];
				
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
}
