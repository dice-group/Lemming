package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * Creates the colour proposers for 1-simplexes and selects from the possible set
 * at random.
 */
public class ConnS1DistU {

	/**
	 * Object of Random class for generating random values.
	 */
	private Random mRandom;

	/**
	 * Object for storing distributions by analyzing triangles found in all input
	 * graphs.
	 */
	private IOfferedItem<EdgeColos> potentialConnectedEdgeProposer;

	/**
	 * Constructor.
	 * 
	 * @param mConnEdgesColoCountDistAvg
	 * @param iNoOfVersions
	 * @param mRandom
	 */
	public ConnS1DistU(ObjectObjectOpenHashMap<EdgeColos, double[]> mConnEdgesColoCountDistAvg, int iNoOfVersions,
			Random mRandom) {

		// this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;

		// Initialize distribution of triangle colors based on edge count
		Set<EdgeColos> edgeDistr = initializeDistributionAllEdgeColos(mConnEdgesColoCountDistAvg);
		potentialConnectedEdgeProposer = new OfferedItemWrapper<EdgeColos>(edgeDistr.toArray(EdgeColos[]::new),
				mRandom);
	}

	/**
	 * This function returns a EdgeColors from the input set of EdgeColors at random
	 * 
	 */
	public EdgeColos proposeTriangleToAddEdge(Set<EdgeColos> setEdgeColorsMimicGraph) {

		EdgeColos potentialEdgeColours = setEdgeColorsMimicGraph.toArray(
				new EdgeColos[setEdgeColorsMimicGraph.size()])[mRandom.nextInt(setEdgeColorsMimicGraph.size())];

		return potentialEdgeColours;
	}

	/**
	 * This method initializes the probability distribution of edgecolors.
	 * 
	 */
	private Set<EdgeColos> initializeDistributionAllEdgeColos(
			ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColoursCountsDist) {

		Set<EdgeColos> edgeProposerOutput = new HashSet<EdgeColos>();

		Object[] keysTriangleColours = mEdgeColoursCountsDist.keys;

		for (int mapKeyIndex = 0; mapKeyIndex < keysTriangleColours.length; mapKeyIndex++) {
			if (mEdgeColoursCountsDist.allocated[mapKeyIndex]) {
				EdgeColos edgeColorObj = (EdgeColos) keysTriangleColours[mapKeyIndex];
				edgeProposerOutput.add(edgeColorObj);
			}
		}

		return edgeProposerOutput;

	}

	public IOfferedItem<EdgeColos> getPotentialConnEdgeProposer() {
		return potentialConnectedEdgeProposer;
	}

}
