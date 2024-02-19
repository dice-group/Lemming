package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.simplexes.EdgeColos;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class EdgeDistI {
	
	
	/**
	 * HashMap of Head color and tail color, and their statistics
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> mEdgeColosv1v2 = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>();;
	
	/**
	 * Map of Head color and Tail colors set.
	 */
	private ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mHeadColoPossTailColos = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();;
	
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
	
	/**
	 * Object for storing distributions by analyzing isolated triangles found in all input graphs.
	 */
	private OfferedItemByRandomProb<EdgeColos> potentialIsolatedEdgeProposer;
	
	public EdgeDistI(ObjectObjectOpenHashMap<EdgeColos, double[]> mConnEdgesColoCountDistAvg, ObjectObjectOpenHashMap<EdgeColos, double[]> mIsoEdgeColosCounts, int iNoOfVersions, int mIDesiredNoOfVertices, Random mRandom){
		
		this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;
		
		// Initializing the map variables
		//mEdgeColosv1v2 = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>();
		
		//mHeadColoPossTailColos = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();

		
		// Update average vertices count for triangles and create different permutations for triangle colors
		Object[] keysEdgeColours = mConnEdgesColoCountDistAvg.keys;
		for(int i = 0; i < keysEdgeColours.length ; i++) {
			if(mConnEdgesColoCountDistAvg.allocated[i]) {
				EdgeColos edgeColorObj = (EdgeColos) keysEdgeColours[i];
				double[] edgeCountsDistArr = mConnEdgesColoCountDistAvg.get(edgeColorObj);
				edgeCountsDistArr[2] = (edgeCountsDistArr[2] * mIDesiredNoOfVertices) /iNoOfVersions;
			
				storeEdgeColosInMaps(edgeColorObj.getA(), edgeColorObj.getB(), edgeCountsDistArr);
				
			}
		}
		
		//Initialize distribution of triangle colors based on edge count
		potentialConnectedEdgeProposer = initializeDistributionAllEdgeColos(mConnEdgesColoCountDistAvg);
		
		potentialIsolatedEdgeProposer = initializeDistributionAllEdgeColos(mIsoEdgeColosCounts);
		
		updateMapHeadColoTailColos();
	}
	
	
	public void updateMapHeadColoTailColos() {
		Object[] keysHeadColo = mEdgeColosv1v2.keys;
		for (int i =0; i < keysHeadColo.length; i++) {
			if (mEdgeColosv1v2.allocated[i]) {
				BitSet headColo = (BitSet) keysHeadColo[i]; // get head colors
				
				
				Set<BitSet> TailColosTemp = new HashSet<BitSet>(); // temporary set to store tail colors
				
				ObjectObjectOpenHashMap<BitSet, double[]> tailColoMap = mEdgeColosv1v2.get(headColo);
				Object[] keysTailColo = tailColoMap.keys;
				for (int j =0; j < keysTailColo.length; j++) {
					if (tailColoMap.allocated[j]) {
						BitSet tailColo = (BitSet) keysTailColo[j];
						TailColosTemp.add(tailColo);
					}
				}
				
				mHeadColoPossTailColos.put(headColo, TailColosTemp);//update global map
				
			}
		}
	}
	
	public Set<BitSet> getPossTailColos(BitSet inputHeadColo) {
		Set<BitSet> setOfTailColos = mHeadColoPossTailColos.get(inputHeadColo);
		return setOfTailColos;
	}
	
	
	/**
	 * This generic function is used to store EdgeColors in a map, which is easier to use while generating the mimic graph.
	 */
	private void storeEdgeColosInMaps(BitSet firstVertexColor, BitSet secondVertexColor, double arrCountDist[]) {
		ObjectObjectOpenHashMap<BitSet, double[]> mEdgeColorsv2v3Temp = mEdgeColosv1v2.get(firstVertexColor);
		
		if (mEdgeColorsv2v3Temp == null) { // Logic when none of the colors are previously observed.
			
			ObjectObjectOpenHashMap<BitSet, double[]> mEdgeColorv3CountTemp = new ObjectObjectOpenHashMap<BitSet, double[]>();
			mEdgeColorv3CountTemp.put(secondVertexColor, arrCountDist);
			
			
			mEdgeColosv1v2.put(firstVertexColor, mEdgeColorv3CountTemp);
		}else {
				
			mEdgeColorsv2v3Temp.put(secondVertexColor, arrCountDist);
			//mEdgeColosv1v2.put(firstVertexColor, mEdgeColorsv2v3Temp);
		}
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
	 * This function returns a EdgeColors from the input set of EdgeColors based on probability distribution of EdgeColors found in Input graphs. 
	 * 
	 * @param setEdgeColorsMimicGraph - Set of TriangleColours that needs to be filtered.
	 * @return
	 */
	public EdgeColos proposeIsoTriToAddEdge(Set<EdgeColos> setEdgeColorsMimicGraph) {
		
		//Utilize initialized object distribution for all triangles

		// get the selected triangle colors
		EdgeColos potentialEdgeColours = potentialIsolatedEdgeProposer.getPotentialItem(setEdgeColorsMimicGraph);
		
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

	public OfferedItemByRandomProb<EdgeColos> getPotentialIsolatedEdgeColoProposer() {
		return potentialIsolatedEdgeProposer;
	}

	public OfferedItemByRandomProb<EdgeColos> getPotentialConnEdgeProposer() {
		return potentialConnectedEdgeProposer;
	}
	
	public ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> getmEdgeColorsv1v2() {
		return mEdgeColosv1v2;
	}

}
