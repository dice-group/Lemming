package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class VertDistU {
	
	/**
	 * Color proposer for 0-simplexes.
	 */
	Set<BitSet> potentialColoProposer;

	public VertDistU(ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex, int iNoOfVersions, Random mRandom) {
		// initialization
		//this.iNoOfVersions = iNoOfVersions;
		//this.mRandom = mRandom;
		
		// Create Color Proposer
		potentialColoProposer = new HashSet<BitSet>();
		createHeadColoProposer(mColoCount0Simplex);
	}

	/**
	 * The method initializes the color proposer for 0-simplexes. It utilizes map storing count of colors for 0-simplexes in input graphs. 
	 * @param mHeadColoCount1Simplex - Map storing Head Color has keys and their count in input graphs as values.
	 */
	public void createHeadColoProposer(ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex) {
				
		Object[] vertColo0Simplex = mColoCount0Simplex.keys;
		
		//iterate list of head colors and add values to sample space and values
		for(int i=0; i < vertColo0Simplex.length; i++) {
			if (mColoCount0Simplex.allocated[i]) {
			BitSet coloS0 = (BitSet) vertColo0Simplex[i];
			potentialColoProposer.add(coloS0);
						
			}
		}
				
		
	}
	
	public Set<BitSet> getPotentialColoProposer() {
		return potentialColoProposer;
	}

}
