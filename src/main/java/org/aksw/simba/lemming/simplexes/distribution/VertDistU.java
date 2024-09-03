package org.aksw.simba.lemming.simplexes.distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Creates the colour proposer for 0-simplexes and selects from the possible set
 * at random.
 */
public class VertDistU {

	/**
	 * Color proposer for 0-simplexes.
	 */
	IOfferedItem<BitSet> potentialColoProposer;

	/**
	 * Constructor.
	 * 
	 * @param mColoCount0Simplex
	 * @param iNoOfVersions
	 * @param mRandom
	 */
	public VertDistU(ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex, int iNoOfVersions, Random mRandom) {
		BitSet[] colours = createHeadColoProposer(mColoCount0Simplex);
		potentialColoProposer = new OfferedItemWrapper<BitSet>(colours, mRandom);
	}

	/**
	 * The method initializes the color proposer for 0-simplexes. It utilizes map
	 * storing count of colors for 0-simplexes in input graphs.
	 * 
	 * @param mHeadColoCount1Simplex - Map storing Head Color has keys and their
	 *                               count in input graphs as values.
	 */
	public BitSet[] createHeadColoProposer(ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex) {

		Object[] vertColo0Simplex = mColoCount0Simplex.keys;
		List<BitSet> colours = new ArrayList<>();

		// iterate list of head colors and add values to sample space and values
		for (int i = 0; i < vertColo0Simplex.length; i++) {
			if (mColoCount0Simplex.allocated[i]) {
				BitSet coloS0 = (BitSet) vertColo0Simplex[i];
				colours.add(coloS0);
			}
		}
		return colours.toArray(BitSet[]::new);
	}

	public IOfferedItem<BitSet> getPotentialColourProposer() {
		return potentialColoProposer;
	}
}
