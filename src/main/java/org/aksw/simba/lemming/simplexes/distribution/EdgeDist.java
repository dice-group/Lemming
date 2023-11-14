package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Random;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * This class creates distributions for 1-simplexes. 
 * A distribution to propose a head color and another distribution to propose a tail color given a head color.
 */
public class EdgeDist {

	/**
	 * Map for storing count of head and tail colours for 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount;
	
	/**
	 * Number of input graphs.
	 */
	private int iNoOfVersions;
	
	/**
	 * Object of Random class
	 */
	private Random mRandom;
	
	/**
	 * Head Color proposer for 1-simplex.
	 */
	private OfferedItemByRandomProb<BitSet> potentialVertColoProposer;
	
	/**
	 * Map for storing probability distributions for different head colors
	 */
	private ObjectObjectOpenHashMap<BitSet, OfferedItemByRandomProb<BitSet>> mVertColoProbDist;

	public EdgeDist(ObjectDoubleOpenHashMap<BitSet> mVertColoCount1Simplex, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount, int iNoOfVersions, Random mRandom) {
		// initialization
		this.mHeadColoTailColoCount = mHeadColoTailColoCount;
		this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;
		mVertColoProbDist = new ObjectObjectOpenHashMap<BitSet, OfferedItemByRandomProb<BitSet>>();
		
		// create head color proposer
		createVertColoProposer(mVertColoCount1Simplex);
	}
	
	/**
	 * The method initializes the head color proposer for 1-simplexes. It utilizes map storing count of head colors for input graphs. 
	 * @param mVertColoCount1Simplex - Map storing Head Color has keys and their count in input graphs as values.
	 * @param iNoOfVersions - Number of input graphs.
	 * @param mRandom - Random generator object.
	 */
	public void createVertColoProposer(ObjectDoubleOpenHashMap<BitSet> mVertColoCount1Simplex) {
		
		int sampleSize = mVertColoCount1Simplex.assigned;
		
		// initialize array for sample space and values
		BitSet[] vertColoSampleSpace = new BitSet[sampleSize];
		double[] vertColoSampleValue = new double[sampleSize];
		
		int sampleSpaceIndex = 0;
		//iterate list of head colors and add values to sample space and values
		Object[] keysVertColo = mVertColoCount1Simplex.keys;
		for(int i=0; i < keysVertColo.length; i++) {
			if (mVertColoCount1Simplex.allocated[i]) {
				vertColoSampleSpace[sampleSpaceIndex] = (BitSet) keysVertColo[i];
					
				double countOfVertColo = mVertColoCount1Simplex.get((BitSet) keysVertColo[i]);
				double sampleDistribution = countOfVertColo * 1.0 / iNoOfVersions;
				vertColoSampleValue[sampleSpaceIndex] = sampleDistribution;
					
				sampleSpaceIndex++;
			}
		}
		
		ObjectDistribution<BitSet> potentialHeadColo = new ObjectDistribution<BitSet>(vertColoSampleSpace, vertColoSampleValue);
		
		if (!potentialHeadColo.isEmpty()) // Distribution can be created only when sample space is not null
			potentialVertColoProposer = new OfferedItemByRandomProb<BitSet>(potentialHeadColo, mRandom);
		else
			potentialVertColoProposer = null;
		
	}
	
	public OfferedItemByRandomProb<BitSet> proposeVertColo(BitSet headColo) {
		
		OfferedItemByRandomProb<BitSet> potentialTailColoProposer = mVertColoProbDist.get(headColo);
		
		if(potentialTailColoProposer == null) {
			//create a new distribution if distribution does not already exist for head color
		
			// get map of tail color and count for the input head color
			ObjectDoubleOpenHashMap<BitSet> mtailColoCount = mHeadColoTailColoCount.get(headColo);
			
			int sampleSize = mtailColoCount.assigned;
			
			//TODO: Code repetition. create a method in one of the utils class? Similar code is used in TriangleDistribution class as well.
			
			// initialize array for sample space and values
			BitSet[] tailColoSampleSpace = new BitSet[sampleSize];
			double[] tailColoSampleValue = new double[sampleSize];
			
			int sampleSpaceIndex = 0;
			//iterate list of head colors and add values to sample space and values
			Object[] keysVertColo = mtailColoCount.keys;
			for(int i = 0; i < keysVertColo.length; i++) {
				if (mtailColoCount.allocated[i]) {
					tailColoSampleSpace[sampleSpaceIndex] = (BitSet) keysVertColo[i];
						
					double countOfHeadColo = mtailColoCount.get((BitSet) keysVertColo[i]);
					double sampleDistribution = countOfHeadColo * 1.0 / iNoOfVersions;
					tailColoSampleValue[sampleSpaceIndex] = sampleDistribution;
						
					sampleSpaceIndex++;
				}
			}
			
			// create distributions
			ObjectDistribution<BitSet> potentialTailColoDist = new ObjectDistribution<BitSet>(tailColoSampleSpace, tailColoSampleValue);
			
			if (potentialTailColoDist.isEmpty()) // returning null if sample space is empty, distribution cannot be created
				return null;
			
			
			potentialTailColoProposer = new OfferedItemByRandomProb<BitSet>(potentialTailColoDist, mRandom);
			
			//store the probability distribution in the map
			mVertColoProbDist.put(headColo, potentialTailColoProposer);
			
		} 
		
		//return potentialTailColoProposer.getPotentialItem(); //changing return type so that the proposer could be filtered
		return potentialTailColoProposer;
	}

	public OfferedItemByRandomProb<BitSet> getPotentialHeadColoProposer() {
		return potentialVertColoProposer;
	}
}
