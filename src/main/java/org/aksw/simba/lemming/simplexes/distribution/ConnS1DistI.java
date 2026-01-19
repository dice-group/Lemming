package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.simplexes.EdgeColos;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class ConnS1DistI {
	
	/**
	 * Variable to store number of input graphs.
	 */
	private int iNoOfVersions;
	
	/**
	 * Object of Random class for generating random values.
	 */
	private Random mRandom;
	
	/**
	 * Object for storing distributions by analyzing triangles found in all input graphs.
	 */
	private OfferedItemByRandomProb<EdgeColos> potentialConnectedEdgeProposer;
	
	
	public ConnS1DistI(ObjectObjectOpenHashMap<EdgeColos, double[]> mConnEdgesColoCountDistAvg, int iNoOfVersions, Random mRandom){
		
		this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;
		
		//Initialize distribution of triangle colors based on edge count
		potentialConnectedEdgeProposer = initializeDistributionAllEdgeColos(mConnEdgesColoCountDistAvg);
		
	}
	
	/**
	 * This function returns a EdgeColors from the input set of EdgeColors based on their probability distribution found in Input graphs. 
	 * 
	 */
	public EdgeColos proposeTriangleToAddEdge(Set<EdgeColos> setEdgeColorsMimicGraph) {

		EdgeColos potentialEdgeColours = potentialConnectedEdgeProposer.getPotentialItem(setEdgeColorsMimicGraph);
		
		for(EdgeColos edgeColourObject: setEdgeColorsMimicGraph) {
			if ( edgeColourObject.equals(potentialEdgeColours)) {
				return edgeColourObject;
			}
		}
		
		return null;
	}
	
	/**
	 * This method initializes the probability distribution of edgecolors.
	 * 
	 */
	private OfferedItemByRandomProb<EdgeColos> initializeDistributionAllEdgeColos(ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColoursCountsDist) {
		
		OfferedItemByRandomProb<EdgeColos> edgeProposerOutput;
		
		int numberOfDistinctEdgeColours = mEdgeColoursCountsDist.assigned;
		
		// create sample space & values for triangle colors
		EdgeColos[] edgeColorsSampleSpace = new EdgeColos[numberOfDistinctEdgeColours];
		double[] computedArrFromInput = new double[numberOfDistinctEdgeColours];
		
		Object[] keysTriangleColours = mEdgeColoursCountsDist.keys;
		
		
		int i = 0; //temp variable to track index of array
		for(int mapKeyIndex = 0; mapKeyIndex < keysTriangleColours.length ; mapKeyIndex++) {
			if(mEdgeColoursCountsDist.allocated[mapKeyIndex]) {
				EdgeColos edgeColorObj = (EdgeColos) keysTriangleColours[mapKeyIndex];
				double[] edgeColoCountArr = mEdgeColoursCountsDist.get(edgeColorObj);
				double countOfEdges = edgeColoCountArr[1];//probability of edge is stored at 1st index
				
				edgeColorsSampleSpace[i] = edgeColorObj;
				
				double tempMeanValue = countOfEdges * 1.0 / iNoOfVersions;
				computedArrFromInput[i] = tempMeanValue;
				
				i++;
			}
		}
		
		ObjectDistribution<EdgeColos> potentialEdge = new ObjectDistribution<EdgeColos>(edgeColorsSampleSpace, computedArrFromInput);
		if (!potentialEdge.isEmpty()) // When samplespace is empty, initialzing the proposer with null
			edgeProposerOutput = new OfferedItemByRandomProb<EdgeColos>(potentialEdge, mRandom);
		else
			edgeProposerOutput = null;
		
		return edgeProposerOutput;
		
	}

	public OfferedItemByRandomProb<EdgeColos> getPotentialConnEdgeProposer() {
		return potentialConnectedEdgeProposer;
	}
}
