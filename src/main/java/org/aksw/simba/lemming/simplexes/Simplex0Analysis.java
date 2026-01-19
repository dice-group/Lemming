package org.aksw.simba.lemming.simplexes;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class Simplex0Analysis {
	
	/**
	* Map for storing number of vertices forming 0-simplexes for different input graphs. The key is incremented as new graph is analysed. Thus, the key for the first input graph is 1, second input graph is 2, and so on.
	*/
	private Map<Integer, Integer> mGraphIdNumberOf0SimplexVertices = new HashMap<Integer, Integer>();
	
	/**
	 * Map for storing count of colours for 0-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex;
		
		
	/**
	* Variable to track graph ids.
	*/
	private int graphId;
		
		
	public Simplex0Analysis() {
		graphId = 1;
		mColoCount0Simplex = new ObjectDoubleOpenHashMap<BitSet>();
	}
		
		
	/**
	* The function analyzes the input graphs and generates statistics for 1-simplex.
	*/
	public void analyze(ColouredGraph[] origGrphs) {
			
		for (ColouredGraph graph : origGrphs) {
				
			if (graph!= null) {
					
				// temporary variables to track number of vertices forming 0-simplexes
				int numberOfVertices0Simplex = 0;
				
				// Iterating over all edges to find class nodes, such nodes should not be considered as neighbors since they are connected with RDF type edges
//				IntSet classNodes = new DefaultIntSet(Constants.DEFAULT_SIZE);
//				for (int edgeId: graph.getEdges()) {
//					if (graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour()))
//						classNodes.add(graph.getHeadOfTheEdge(edgeId));
//				}
//				System.out.println(graph.getRDFTypePropertyColour());
				
				ObjectIntOpenHashMap<BitSet> mVertexColorCountTemp = new ObjectIntOpenHashMap<BitSet>();
				
					
				// Get all vertices of graph
				IntSet allVertices = graph.getVertices();
				
				for(int vertexId: allVertices) {
					if (!graph.getVertexColour(vertexId).isEmpty()) { // Check for not empty colors
						IntSet allNeighborsVertices = IntSetUtil.union(graph.getInNeighbors(vertexId), graph.getOutNeighbors(vertexId));
						//allNeighborsVertices = IntSetUtil.difference(allNeighborsVertices, classNodes);
						if (allNeighborsVertices.size() == 0) {
							numberOfVertices0Simplex++;
							BitSet vertexColour = graph.getVertexColour(vertexId);
							mVertexColorCountTemp.putOrAdd(vertexColour, 1, 1);
							
						}
					}
				}
				
				// find distribution of vertex color count for input graph and update the global double hash map
				Object[] keysVertColo = mVertexColorCountTemp.keys;
				for (int i=0; i< keysVertColo.length; i++) {
					if (mVertexColorCountTemp.allocated[i]) {
						BitSet vertColo = (BitSet) keysVertColo[i];
						double distributionInGraph = mVertexColorCountTemp.get(vertColo) * 1.0 /numberOfVertices0Simplex;
						mColoCount0Simplex.putOrAdd(vertColo, distributionInGraph, distributionInGraph);
					}
				}
				
				mGraphIdNumberOf0SimplexVertices.put(graphId, numberOfVertices0Simplex);
				graphId++;
					
			}
		}
			
	}


	public ObjectDoubleOpenHashMap<BitSet> getmColoCount0Simplex() {
		return mColoCount0Simplex;
	}


	public Map<Integer, Integer> getmGraphIdNumberOf0SimplexVertices() {
		return mGraphIdNumberOf0SimplexVertices;
	}


}
