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
 * This class creates different distributions for 2-simplexes.
 */
public class Simplex2Distribution extends Simplex1Distribution{
	// Extending class Simplex1Distribution because some of the methods from that class can be used directly for creating distributions of common edges connecting triangles to rest of the graph.
	
	
	/**
	 * Color proposer for connected vertices to 2-simplexes.
	 */
	OfferedItemByRandomProb<BitSet> potentialColoProposerForVertConnectedToTriangle;
	
	public Simplex2Distribution(ObjectDoubleOpenHashMap<BitSet> mVertColoConnectedToTriangles, ObjectDoubleOpenHashMap<BitSet> mHeadColoCount1Simplex, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount, int iNoOfVersions, Random randomObj) {
		super(mHeadColoCount1Simplex, mHeadColoTailColoCount, iNoOfVersions, randomObj);
		createColoProposerForVertConnectedToTriangle(mVertColoConnectedToTriangles);
	}
	
	/**
	 * The method initializes the head color proposer for 1-simplexes. It utilizes map storing count of head colors for input graphs. 
	 * @param mVertColoCountConnectedToTriangles - Map storing Head Color has keys and their count in input graphs as values.
	 * @param iNoOfVersions - Number of input graphs.
	 * @param mRandom - Random generator object.
	 */
	public void createColoProposerForVertConnectedToTriangle(ObjectDoubleOpenHashMap<BitSet> mVertColoCountConnectedToTriangles) {
		
		System.out.println();
		
		int sampleSize = mVertColoCountConnectedToTriangles.assigned;
		
		// initialize array for sample space and values
		BitSet[] vertColoSampleSpace = new BitSet[sampleSize];
		double[] vertColoSampleValue = new double[sampleSize];
		
		int sampleSpaceIndex = 0;
		//iterate list of head colors and add values to sample space and values
		Object[] vertColoKeys = mVertColoCountConnectedToTriangles.keys;
		for(int i=0; i < vertColoKeys.length; i++) {
			if (mVertColoCountConnectedToTriangles.allocated[i]) {
				vertColoSampleSpace[sampleSpaceIndex] = (BitSet) vertColoKeys[i];
					
				double countOfVertColo = mVertColoCountConnectedToTriangles.get((BitSet) vertColoKeys[i]);
				double sampleDistribution = countOfVertColo * 1.0 / iNoOfVersions;
				vertColoSampleValue[sampleSpaceIndex] = sampleDistribution;
					
				sampleSpaceIndex++;
			}
		}
		
		ObjectDistribution<BitSet> potentialHeadColo = new ObjectDistribution<BitSet>(vertColoSampleSpace, vertColoSampleValue);
		
		if (!potentialHeadColo.isEmpty()) // Distribution can be created only when sample space is not null
			potentialColoProposerForVertConnectedToTriangle = new OfferedItemByRandomProb<BitSet>(potentialHeadColo, mRandom);
		else
			potentialColoProposerForVertConnectedToTriangle = null;
		
	}

	public OfferedItemByRandomProb<BitSet> getPotentialColoProposerForVertConnectedToTriangle() {
		return potentialColoProposerForVertConnectedToTriangle;
	}

}
