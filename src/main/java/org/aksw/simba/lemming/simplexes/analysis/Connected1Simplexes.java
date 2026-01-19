package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class analyzes the connected 1-simplexes from the input graphs. It requires as an input set of edges for all other simplexes.
 * Note: Such 1-simplexes are determined by subtracting set of all edges in the graph by set of edges found for different type of simplexes.
 */
public class Connected1Simplexes extends AbstractFindSimplexes{
	/**
	 * Map for storing count of vertex colours for connected 1-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mHeadColoCountConnected1Simplex;
	
	/**
	 * Map for storing count of head and tail colours for connected 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCountConnected;
	
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsAllSimplexesTemp;
	
	public Connected1Simplexes(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsAllSimplexes) {
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		mGraphsEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		mGraphsVertIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		//initialize edge ids for all other simplexes
		mGraphsEdgesIdsAllSimplexesTemp = mGraphsEdgesIdsAllSimplexes;
		
		//initialize global map
		mHeadColoCountConnected1Simplex = new ObjectDoubleOpenHashMap<BitSet>();
		mHeadColoTailColoCountConnected = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		findSimplexes();
		computeColorMapper();
		estimateEdges();
		estimateVertices();
	}
	
	@Override
	public void findSimplexes() {
		int keyGraphId = 1;
		for (ColouredGraph graph : inputGrphs) {
			if (graph!= null) {
				IntSet allEdgeIds = graph.getEdges();
				
				IntSet edgeIdsAllsimplexes = mGraphsEdgesIdsAllSimplexesTemp.get(keyGraphId);
				IntSet result = IntSetUtil.difference(allEdgeIds, edgeIdsAllsimplexes); // all edges from edges of all other simplexes
				
				mGraphsEdgesIds.put(keyGraphId, result); //store the edges in global map
				
				//temporary map to store head colors found in 1-simplex for a specific graph
				ObjectIntOpenHashMap<BitSet> mheadColoCountTemp = new ObjectIntOpenHashMap<BitSet>();
				
				//temporary variables to store vertex IDs connecting to triangles. Note: The stored vertex ids are analyzed to determine self loops
		    	IntSet verticesConn1Simplexes = new DefaultIntSet(Constants.DEFAULT_SIZE); // variable for self loop
				
				//temporary map to store count of head-tail color found in 1-simplexes for a specific graph
				ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
				
				//temporary variable to track total number of head colors
				int sampleSizeHeadColor = 0;
				
				// Iterate and store the statistics of vertex colors for these Edges
				for (int edgeIdConnected1Simplex: result) {
					
					if (!graph.getEdgeColour(edgeIdConnected1Simplex).equals(graph.getRDFTypePropertyColour())) {
					
						//Get tail and head of the edge
						int tailEdge = graph.getTailOfTheEdge(edgeIdConnected1Simplex);
						int headEdge = graph.getHeadOfTheEdge(edgeIdConnected1Simplex);
						
						// Colors of the for head and tail
						BitSet headColo = graph.getVertexColour(headEdge);
						BitSet tailColo = graph.getVertexColour(tailEdge);
						
						// Add it to map storing vertex colors and count
						mheadColoCountTemp.putOrAdd(headColo, 1, 1);
						sampleSizeHeadColor = sampleSizeHeadColor + 1;
						
						// Add head and tail color's count to map
						ObjectIntOpenHashMap<BitSet> mtailColoCountConnected = mHeadTailColoCountTemp.get(headColo);
						if (mtailColoCountConnected == null) {
							mtailColoCountConnected = new ObjectIntOpenHashMap<BitSet>();
						}
						mtailColoCountConnected.putOrAdd(tailColo, 1, 1);
						//mHeadColoTailColoCountConnected.put(headColo, mtailColoCountConnected); // Here too using temporary map to calculate distributions
						mHeadTailColoCountTemp.put(headColo, mtailColoCountConnected);
						
						//store vertex ids (later used to evaluate self loops)
						verticesConn1Simplexes.add(headEdge);
						verticesConn1Simplexes.add(tailEdge);
					}
					
				}
				
				//update global maps tracking distribution
				updateMapsTrackingDistributions(mheadColoCountTemp, mHeadTailColoCountTemp, mHeadColoCountConnected1Simplex, mHeadColoTailColoCountConnected, sampleSizeHeadColor);
				
				//update vertex ids map
				mGraphsVertIds.put(keyGraphId, verticesConn1Simplexes);
				
				keyGraphId++;
			}
		}
	}
	
	//getters
	public ObjectDoubleOpenHashMap<BitSet> getmHeadColoCountConnected1Simplex() {
		return mHeadColoCountConnected1Simplex;
	}

	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCountConnected() {
		return mHeadColoTailColoCountConnected;
	}
	
	
}
