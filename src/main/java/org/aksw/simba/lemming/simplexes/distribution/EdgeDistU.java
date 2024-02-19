package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.simplexes.EdgeColos;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class EdgeDistU {

	/**
	 * HashMap of Head color and tail color, and their statistics
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> mEdgeColosv1v2;
	
	/**
	 * Map of Head color and Tail colors set.
	 */
	private ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mHeadColoPossTailColos;
	
	/**
	 * Object of Random class for generating random values.
	 */
	private Random mRandom;
	
	/**
	 * Object for storing distributions by analyzing triangles found in all input graphs.
	 */
	private Set<EdgeColos> potentialConnectedEdgeProposer;
	
	/**
	 * Object for storing distributions by analyzing isolated triangles found in all input graphs.
	 */
	private Set<EdgeColos> potentialIsolatedEdgeProposer;
	
	public EdgeDistU(ObjectObjectOpenHashMap<EdgeColos, double[]> mConnEdgesColoCountDistAvg, ObjectObjectOpenHashMap<EdgeColos, double[]> mIsoEdgeColosCounts, int iNoOfVersions, int mIDesiredNoOfVertices, Random mRandom){
		
		this.mRandom = mRandom;
		
		// Initializing the map variables
		mEdgeColosv1v2 = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>();
		
		mHeadColoPossTailColos = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();

		
		// Update average vertices count for triangles and create different permutations for triangle colors
		Object[] keysEdgeColours = mConnEdgesColoCountDistAvg.keys;
		for(int i = 0; i < keysEdgeColours.length ; i++) {
			if(mConnEdgesColoCountDistAvg.allocated[i]) {
				EdgeColos edgeColorObj = (EdgeColos) keysEdgeColours[i];
				double[] edgeCountsDistArr = mConnEdgesColoCountDistAvg.get(edgeColorObj);
				edgeCountsDistArr[2] = (edgeCountsDistArr[2] * mIDesiredNoOfVertices) /iNoOfVersions;
			
				storeEdgeColosInMaps(mEdgeColosv1v2, edgeColorObj.getA(), edgeColorObj.getB(), edgeCountsDistArr);
				
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
	private void storeEdgeColosInMaps(ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> edgeColorMapToUpdate, BitSet firstVertexColor, BitSet secondVertexColor, double arrCountDist[]) {
		ObjectObjectOpenHashMap<BitSet, double[]> mEdgeColorsv2v3Temp = edgeColorMapToUpdate.get(firstVertexColor);
		
		if (mEdgeColorsv2v3Temp == null) { // Logic when none of the colors are previously observed.
			
			ObjectObjectOpenHashMap<BitSet, double[]> mEdgeColorv3CountTemp = new ObjectObjectOpenHashMap<BitSet, double[]>();
			mEdgeColorv3CountTemp.put(secondVertexColor, arrCountDist);
			
			
			edgeColorMapToUpdate.put(firstVertexColor, mEdgeColorv3CountTemp);
		}else {
				
			mEdgeColorsv2v3Temp.put(secondVertexColor, arrCountDist);
			edgeColorMapToUpdate.put(firstVertexColor, mEdgeColorsv2v3Temp);
		}
	}
	
	/**
	 * This function returns a EdgeColors from the input set of EdgeColors based on their probability distribution found in Input graphs. 
	 * 
	 */
	public EdgeColos proposeTriangleToAddEdge(Set<EdgeColos> setEdgeColorsMimicGraph) {
		
		EdgeColos edgeColours = setEdgeColorsMimicGraph.toArray(new EdgeColos[setEdgeColorsMimicGraph.size()])[mRandom.nextInt(setEdgeColorsMimicGraph.size())];
		return edgeColours;
		
	}
	
	/**
	 * This function returns a EdgeColors from the input set of EdgeColors based on probability distribution of EdgeColors found in Input graphs. 
	 * 
	 * @param setEdgeColorsMimicGraph - Set of TriangleColours that needs to be filtered.
	 * @return
	 */
	public EdgeColos proposeIsoTriToAddEdge(Set<EdgeColos> setEdgeColorsMimicGraph) {
		
		EdgeColos edgeColours = setEdgeColorsMimicGraph.toArray(new EdgeColos[setEdgeColorsMimicGraph.size()])[mRandom.nextInt(setEdgeColorsMimicGraph.size())];
		return edgeColours;
		
		
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

	public Set<EdgeColos> getPotentialIsolatedEdgeColoProposer() {
		return potentialIsolatedEdgeProposer;
	}

	public Set<EdgeColos> getPotentialConnEdgeProposer() {
		return potentialConnectedEdgeProposer;
	}
	
	public ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> getmEdgeColorsv1v2() {
		return mEdgeColosv1v2;
	}

}
