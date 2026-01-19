package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class finds all the self loops present in different input graphs.
 */
public class SelfLoopsInGraphs {

	/**
	 * Variable to track graph ids.
	 */
	private int graphId;
	
	/**
	* Map for storing edge ids forming self loops. The key is incremented as new graph is analysed. Thus, the key for the first input graph is 1, second input graph is 2, and so on.
	* Note: Edge Ids of self loops found in different input graphs is stored in this map.
	*/
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsForSelfLoop = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	public SelfLoopsInGraphs() {
		graphId = 1;
	}
	
	public void analyze(ColouredGraph[] origGrphs) {
		
		for (ColouredGraph graph : origGrphs) {
			
			if (graph!= null) {
				IntSet edgesFormingSelfLoop = new DefaultIntSet(Constants.DEFAULT_SIZE); // temporary variable storing edges for self loop
				
				for (int edgeId: graph.getEdges()) { //iterate over all edges
					
					int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
					int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
					
					if (headOfTheEdge == tailOfTheEdge) { // When the both the vertices are same, the edge is a self loop
						edgesFormingSelfLoop.add(edgeId);
					}
					
				}
				
				mGraphIdEdgeIdsForSelfLoop.put(graphId, edgesFormingSelfLoop); // Store all the edges found for input graph in the map
				graphId++;
			}
		}
		
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphIdEdgeIdsForSelfLoop() {
		return mGraphIdEdgeIdsForSelfLoop;
	}
}
