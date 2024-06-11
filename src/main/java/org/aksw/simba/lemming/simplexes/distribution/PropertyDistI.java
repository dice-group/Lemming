package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Random;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class PropertyDistI implements IPropertyDist {
	
	private ObjectObjectOpenHashMap<EdgeColorsSorted, ObjectDoubleOpenHashMap<BitSet>> mPropDist;
	
	private ObjectObjectOpenHashMap<EdgeColorsSorted, OfferedItemByRandomProb<BitSet>> medgeColoPropDist = new ObjectObjectOpenHashMap<EdgeColorsSorted,OfferedItemByRandomProb<BitSet>>();
	
	private int iNoOfVersions;
	
	private Random mRandom;
	
	public PropertyDistI(ObjectObjectOpenHashMap<EdgeColorsSorted, ObjectDoubleOpenHashMap<BitSet>> mPropDist, int iNoOfVersions, Random mRandom) {
		this.mPropDist = mPropDist;
		this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;
	}
	
	/**
	 * The function proposes a Color for the third vertex to create a triangle, given the colors of first and second vertices as input.
	 * @param vertex1Color - BitSet color for vertex 1.
	 * @param vertex2Color - BitSet color for vertex 2.
	 * @return - Bitset color for vertex 3.
	 */
	public BitSet proposePropColor(EdgeColorsSorted edgeColorInput) {
    	
    	// check if previously for the input vertex colors the third vertex color was proposed
    	OfferedItemByRandomProb<BitSet> mPropColoDist = medgeColoPropDist.get(edgeColorInput);
    	if (mPropColoDist != null) {
    		return mPropColoDist.getPotentialItem();
    	} 
    	
    	BitSet thirdVertexColor = null;
    	// get the Map for the input vertex Colors
    	ObjectDoubleOpenHashMap<BitSet> mapPropColoCount = mPropDist.get(edgeColorInput);
    	
		if (mapPropColoCount == null)
			return thirdVertexColor; // No map found 
    	
		
		//List<BitSet> listPossibleColoV3 = MapUtil.keysToList(mapPossV3Count);
		int sampleSize = mapPropColoCount.assigned;
		
		BitSet[] allPossibleColoV3 = new BitSet[sampleSize];
		
		// create array of double values for ObjectDistribution class
		double[] possEdgesInTriangle = new double[sampleSize];
		int sampleSpaceIndex = 0;
		
		Object[] keysVertColo = mapPropColoCount.keys;
		
		for(int i = 0; i < keysVertColo.length; i++) {
			if (mapPropColoCount.allocated[i]) {
				allPossibleColoV3[sampleSpaceIndex] = (BitSet) keysVertColo[i];
				double numOfEdgesV3 = mapPropColoCount.get((BitSet) keysVertColo[i]); // get the triangle probability stored at first index
				
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
		
		//Add to map vertex color 1, vertex color 2 and probability distribution 
		medgeColoPropDist.put(edgeColorInput, potentialColoProposer);
		
		return thirdVertexColor;
	}

}
