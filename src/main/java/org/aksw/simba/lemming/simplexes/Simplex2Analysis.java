package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

public class Simplex2Analysis extends SimplexAnalysis{
	
	/**
	 * Map for storing count of head colors for 1-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mHeadColoCount;
	
	/**
	 * Map for storing count of head and tail colors for 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount;
	
	/**
	 * Variable to track graph ids.
	 */
	private int graphId;
	
	public Simplex2Analysis() {
		mHeadColoCount = new ObjectDoubleOpenHashMap<BitSet>();
		mHeadColoTailColoCount = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		graphId = 1;
	}
	
	//TODO: Find distribution of head colors and tail colors, similar to 1-simplexes.
	
	/**
	 * The function analyzes the input graphs, edge ids and to generates statistics for 2-simplexes.
	 * Here, we analyze the head color and head & tail colors distribution for edges connecting triangles with rest of the graph.
	 */
	public void analyze(ColouredGraph[] origGrphs, ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIds) {
		
		for (ColouredGraph graph : origGrphs) {
			
			if (graph!= null) {
				IntSet allEdges = graph.getEdges();
				
				IntSet edgesCommonEdges = mGraphsCommonEdgesIds.get(graphId);
				
				//temporary map to store head colors found in 1-simplex for a specific graph
				ObjectIntOpenHashMap<BitSet> mheadColoCountTemp = new ObjectIntOpenHashMap<BitSet>();
				
				//temporary map to store count of head-tail color found in 1-simplexes for a specific graph
				ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
				
				//temporary variable to track total number of head colors
				int sampleSizeHeadColor = 0;
				
				for (int edgeId: allEdges) {
					if(edgesCommonEdges.contains(edgeId) && !graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour())) {
						// Not considering RDF type edges
						
						// store the details of head color for 1-simplex in the map
						int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
						BitSet headColo = graph.getVertexColour(headOfTheEdge);
						//mHeadColoCount.putOrAdd(headColo, 1, 1); // commenting using temporary map to calculcate distribution
						mheadColoCountTemp.putOrAdd(headColo, 1, 1);
						sampleSizeHeadColor = sampleSizeHeadColor + 1;
						
						// store the details of head and tail color for 1-simplex in the map
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
						BitSet tailColo = graph.getVertexColour(tailOfTheEdge);
						//ObjectIntOpenHashMap<BitSet> mtailColoCount = mHeadColoTailColoCount.get(headColo); // commenting using temporary map to calculate distribution
						ObjectIntOpenHashMap<BitSet> mtailColoCount = mHeadTailColoCountTemp.get(headColo);
						if (mtailColoCount == null) {
							mtailColoCount = new ObjectIntOpenHashMap<BitSet>();
						}
						mtailColoCount.putOrAdd(tailColo, 1, 1);
						//mHeadColoTailColoCount.put(headColo, mtailColoCount); // commenting using temporary map to calculate distribution
						mHeadTailColoCountTemp.put(headColo, mtailColoCount);
					}
				}
				
				updateMapsTrackingDistributions(mheadColoCountTemp, mHeadTailColoCountTemp, mHeadColoCount, mHeadColoTailColoCount, sampleSizeHeadColor);
				
				graphId++;
			}
		}
	}

	public ObjectDoubleOpenHashMap<BitSet> getmHeadColoCount() {
		return mHeadColoCount;
	}

	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCount() {
		return mHeadColoTailColoCount;
	}
	

}
