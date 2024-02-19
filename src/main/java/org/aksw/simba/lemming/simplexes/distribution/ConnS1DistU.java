package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.simplexes.EdgeColos;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class ConnS1DistU {
	
	/**
	 * Variable to store number of input graphs.
	 */
	//private int iNoOfVersions;
	
	/**
	 * Object of Random class for generating random values.
	 */
	private Random mRandom;
	
	/**
	 * Object for storing distributions by analyzing triangles found in all input graphs.
	 */
	private Set<EdgeColos> potentialConnectedEdgeProposer;
	
	
	public ConnS1DistU(ObjectObjectOpenHashMap<EdgeColos, double[]> mConnEdgesColoCountDistAvg, int iNoOfVersions, Random mRandom){
		
		//this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;
		
		//Initialize distribution of triangle colors based on edge count
		potentialConnectedEdgeProposer = initializeDistributionAllEdgeColos(mConnEdgesColoCountDistAvg);
		
	}
	
	/**
	 * This function returns a EdgeColors from the input set of EdgeColors based on their probability distribution found in Input graphs. 
	 * 
	 */
	public EdgeColos proposeTriangleToAddEdge(Set<EdgeColos> setEdgeColorsMimicGraph) {

		EdgeColos potentialEdgeColours = setEdgeColorsMimicGraph.toArray(new EdgeColos[setEdgeColorsMimicGraph.size()])[mRandom.nextInt(setEdgeColorsMimicGraph.size())];
		
		return potentialEdgeColours;
	}
	
	/**
	 * This method initializes the probability distribution of edgecolors.
	 * 
	 */
	private Set<EdgeColos> initializeDistributionAllEdgeColos(ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColoursCountsDist) {
		
		Set<EdgeColos> edgeProposerOutput = new HashSet<EdgeColos>();
		
		Object[] keysTriangleColours = mEdgeColoursCountsDist.keys;
		
		
		for(int mapKeyIndex = 0; mapKeyIndex < keysTriangleColours.length ; mapKeyIndex++) {
			if(mEdgeColoursCountsDist.allocated[mapKeyIndex]) {
				EdgeColos edgeColorObj = (EdgeColos) keysTriangleColours[mapKeyIndex];
				edgeProposerOutput.add(edgeColorObj);
				
				
			}
		}
		
		return edgeProposerOutput;
		
	}

	public Set<EdgeColos> getPotentialConnEdgeProposer() {
		return potentialConnectedEdgeProposer;
	}

}
