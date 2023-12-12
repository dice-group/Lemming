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
 * This class analyzes 0-simplexes present in the input graphs.
 */
public class S0C extends AbstractFindSimplexes{
	
	/**
	 * Map for storing count of colours for 0-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mColoCount0Simplex;
	
	public S0C(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions) {
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		mGraphsVertIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		//initialize global map
		mColoCount0Simplex = new ObjectDoubleOpenHashMap<BitSet>();
		
		findSimplexes();
		
		estimateVertices();
	}
	
	@Override
	public void findSimplexes() {
		int graphId = 1;
		for (ColouredGraph graph : inputGrphs) {
			
			if (graph!= null) {
				
				IntSet vertices0Simplexes = new DefaultIntSet(Constants.DEFAULT_SIZE);
					
				int numberOfVertices0Simplex = 0; // temporary variables to track number of vertices forming 0-simplexes
				
				System.out.println(graph.getRDFTypePropertyColour());
				
				ObjectIntOpenHashMap<BitSet> mVertexColorCountTemp = new ObjectIntOpenHashMap<BitSet>();
				
					
				// Get all vertices of graph
				IntSet allVertices = graph.getVertices();
				
				for(int vertexId: allVertices) {
					if (!graph.getVertexColour(vertexId).isEmpty()) { // Check for not empty colors
						IntSet allNeighborsVertices = IntSetUtil.union(graph.getInNeighbors(vertexId), graph.getOutNeighbors(vertexId));
						//allNeighborsVertices = IntSetUtil.difference(allNeighborsVertices, classNodes);
						if (allNeighborsVertices.size() == 0) {
							numberOfVertices0Simplex++;
							vertices0Simplexes.add(vertexId);
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
				
				mGraphsVertIds.put(graphId, vertices0Simplexes);
				graphId++;
					
			}
		}
	}
	
	
	//getter
	public ObjectDoubleOpenHashMap<BitSet> getmColoCount0Simplex() {
		return mColoCount0Simplex;
	}

	
}
