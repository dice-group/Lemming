package org.aksw.simba.lemming.simplexes;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public abstract class SimplexAnalysis {
	
	/**
	 * This method updates the global maps that track the distributions, using the temporary maps. The first map stores the count of head colors for different vertices and the second map stores the head color, tail color and their count. 
	 * For these maps, probability distribution with respect to specific input graphs needs to be calculated. This function is repeatedly call to sum up the distributions for different input graphs and store it in global maps.
	 * @param mheadColoCountTemp
	 * @param mHeadTailColoCountTemp
	 * @param mGlobalHeadColoCount
	 * @param mGlobalHeadTailColoCount
	 */
	protected void updateMapsTrackingDistributions(ObjectIntOpenHashMap<BitSet> mheadColoCountTemp, 
			ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp, 
			ObjectDoubleOpenHashMap<BitSet> mGlobalHeadColoCount, 
			ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mGlobalHeadTailColoCount, int sampleSizeHeadColo) {
		//find distribution of head color count for input graph and update the global map
		Object[] keysheadColo = mheadColoCountTemp.keys;
		for (int i=0; i < keysheadColo.length; i++) {
			if (mheadColoCountTemp.allocated[i]) {
				BitSet headColo = (BitSet) keysheadColo[i];
				double distributionInGraph = mheadColoCountTemp.get(headColo) * 1.0 / (sampleSizeHeadColo);
				mGlobalHeadColoCount.putOrAdd(headColo, distributionInGraph, distributionInGraph);
				
				//find distribution of head-tail color count for input graph and update the global map
				ObjectIntOpenHashMap<BitSet> mTailColoCount = mHeadTailColoCountTemp.get(headColo);
				
				//variable to track total number of tail colors for the head color
				int totalnumOfTailColo = 0;
				
				// iterate over all tail colors and get total number of tail colors for a specific head color
				Object[] keysTailColo = mTailColoCount.keys;
				
				for (int tailIndex = 0; tailIndex < keysTailColo.length; tailIndex++) {
					if (mTailColoCount.allocated[tailIndex]) {
						BitSet tailColo = (BitSet) keysTailColo[tailIndex];
						totalnumOfTailColo = totalnumOfTailColo + mTailColoCount.get(tailColo);
					}
				}
				
				for (int tailIndex = 0; tailIndex < keysTailColo.length; tailIndex++) {
					if (mTailColoCount.allocated[tailIndex]) {
						BitSet tailColo = (BitSet) keysTailColo[tailIndex];
						double distributionTailInGraph = mTailColoCount.get(tailColo) * 1.0 / totalnumOfTailColo;
						
						ObjectDoubleOpenHashMap<BitSet> globalTailColoCount = mGlobalHeadTailColoCount.get(headColo);
						if (globalTailColoCount == null) {
							globalTailColoCount = new ObjectDoubleOpenHashMap<BitSet>();
						}
						globalTailColoCount.putOrAdd(tailColo, distributionTailInGraph, distributionTailInGraph);
						mGlobalHeadTailColoCount.put(headColo, globalTailColoCount);
					}
				}
				
			}
		}
	}
	
	/**
	 * This method updates the global map that tracks the distribution of single color, using the temporary map. The input temp map stores the count of colors for different vertices. 
	 * For these maps, probability distribution with respect to specific input graphs needs to be calculated. This function is repeatedly call to sum up the distributions for different input graphs and store it in the global map.
	 * @param mheadColoCountTemp
	 * @param mHeadTailColoCountTemp
	 * @param mGlobalHeadColoCount
	 * @param mGlobalHeadTailColoCount
	 */
	protected void updateSingleColoMapTrackingDistribution(ObjectIntOpenHashMap<BitSet> mheadColoCountTemp,	ObjectDoubleOpenHashMap<BitSet> mGlobalHeadColoCount, int sampleSizeHeadColo) {
		//find distribution of head color count for input graph and update the global map
		Object[] keysheadColo = mheadColoCountTemp.keys;
		for (int i=0; i < keysheadColo.length; i++) {
			if (mheadColoCountTemp.allocated[i]) {
				BitSet headColo = (BitSet) keysheadColo[i];
				double distributionInGraph = mheadColoCountTemp.get(headColo) * 1.0 / (sampleSizeHeadColo);
				mGlobalHeadColoCount.putOrAdd(headColo, distributionInGraph, distributionInGraph);
			}
		}
	}

}
