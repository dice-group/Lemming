package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Random;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * The class creates distribution for 0-simplexes or vertices.
 */
public class VertDistI {
	/**
	 * Number of input graphs.
	 */
	private int iNoOfVersions;
	
	/**
	 * Object of Random class
	 */
	private Random mRandom;
	
	/**
	 * Color proposer for 0-simplexes.
	 */
	OfferedItemByRandomProb<BitSet> potentialColoProposer;

	public VertDistI(ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex, int iNoOfVersions, Random mRandom) {
		// initialization
		this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;
		
		// Create Color Proposer
		createHeadColoProposer(mColoCount0Simplex);
	}

	/**
	 * The method initializes the color proposer for 0-simplexes. It utilizes map storing count of colors for 0-simplexes in input graphs. 
	 * @param mHeadColoCount1Simplex - Map storing Head Color has keys and their count in input graphs as values.
	 */
	public void createHeadColoProposer(ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex) {
		
		int sampleSize = mColoCount0Simplex.assigned;
				
		//initialize array for sample space and values
		BitSet[] coloSampleSpace = new BitSet[sampleSize];
		double[] coloSampleValue = new double[sampleSize];
				
		int sampleSpaceIndex = 0;
		
		Object[] vertColo0Simplex = mColoCount0Simplex.keys;
		
		//iterate list of head colors and add values to sample space and values
		for(int i=0; i < vertColo0Simplex.length; i++) {
			if (mColoCount0Simplex.allocated[i]) {
			coloSampleSpace[sampleSpaceIndex] = (BitSet) vertColo0Simplex[i];
						
			double countColo = mColoCount0Simplex.get((BitSet) vertColo0Simplex[i]);
			double sampleDistribution = countColo * 1.0 / iNoOfVersions;
			coloSampleValue[sampleSpaceIndex] = sampleDistribution;
						
			sampleSpaceIndex++;
			}
		}
				
		ObjectDistribution<BitSet> potentialColo0Simplex = new ObjectDistribution<BitSet>(coloSampleSpace, coloSampleValue);
		
		if (!potentialColo0Simplex.isEmpty()) // create distribution when sample space is not empty
			potentialColoProposer = new OfferedItemByRandomProb<BitSet>(potentialColo0Simplex, mRandom);
		else
			potentialColoProposer = null;
	}
	
	public OfferedItemByRandomProb<BitSet> getPotentialColoProposer() {
		return potentialColoProposer;
	}

}
