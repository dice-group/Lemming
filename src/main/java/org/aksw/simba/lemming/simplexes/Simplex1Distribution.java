package org.aksw.simba.lemming.simplexes;

import java.util.List;
import java.util.Random;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.PoissonDistribution;
import org.aksw.simba.lemming.util.MapUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * This class includes a two proposers. One for head colors of vertices for 1-simplexes and other for tail colours given a head color. 
 */
/**
 * 
 */
public class Simplex1Distribution {
	
	
	/**
	 * Map for storing count of head and tail colours for 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount;
	
	/**
	 * Number of input graphs.
	 */
	protected int iNoOfVersions;
	
	/**
	 * Object of Random class
	 */
	protected Random mRandom;
	
	/**
	 * Head Color proposer for 1-simplex.
	 */
	OfferedItemByRandomProb<BitSet> potentialVertColoProposer;
	
	/**
	 * Map for storing probability distributions for different head colors
	 */
	ObjectObjectOpenHashMap<BitSet, OfferedItemByRandomProb<BitSet>> mVertColoProbDist;
	
	/**
	 * Distribution related to isolated self loops
	 */
	OfferedItemByRandomProb<BitSet> potentialIsoSelfLoopColoProposer;
	
	/**
	 * Distribution related to self loops in isolated 1-simplexes
	 */
	OfferedItemByRandomProb<BitSet> potentialSelfLoopIn1SimplexColoProposer;

	public Simplex1Distribution(ObjectDoubleOpenHashMap<BitSet> mVertColoCount1Simplex, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount, int iNoOfVersions, Random mRandom) {
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
		
		//TODO: Check if first call to get keys of map can be avoided? Iterating map twice
		System.out.println();
		
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
	
	/**
	 * The method creates a color proposer for isolated self loops. It utilizes map storing count of colors for the isolated self loops found in Input graphs. 
	 * @param mVertColoCount1Simplex - Map storing Color has keys and their count in input graphs as values.
	 */
	public void createIsoSelfLoopColoProposer(ObjectDoubleOpenHashMap<BitSet> mVertColoCount1Simplex) {
		
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
			potentialIsoSelfLoopColoProposer = new OfferedItemByRandomProb<BitSet>(potentialHeadColo, mRandom);
		else
			potentialIsoSelfLoopColoProposer = null;
		
	}
	
	/**
	 * The method creates a color proposer for self loops in isolated 1-simplexes. It utilizes map storing count of colors for the isolated self loops found in Input graphs. 
	 * @param mVertColoCount1Simplex - Map storing Color has keys and their count in input graphs as values.
	 */
	public void createSelfLoopIn1SimplexColoProposer(ObjectDoubleOpenHashMap<BitSet> mVertColoCount1Simplex) {
		
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
			potentialSelfLoopIn1SimplexColoProposer = new OfferedItemByRandomProb<BitSet>(potentialHeadColo, mRandom);
		else
			potentialSelfLoopIn1SimplexColoProposer = null;
		
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

	// *******************************getter for isolated self loops ********************************************//
	public OfferedItemByRandomProb<BitSet> getPotentialIsoSelfLoopColoProposer() {
		return potentialIsoSelfLoopColoProposer;
	}

	// *******************************getter for self loops in isolated 1-simplexes ********************************************//
	public OfferedItemByRandomProb<BitSet> getPotentialSelfLoopIn1SimplexColoProposer() {
		return potentialSelfLoopIn1SimplexColoProposer;
	}
}
