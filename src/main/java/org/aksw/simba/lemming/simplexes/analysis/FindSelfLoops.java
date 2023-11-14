package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;


/**
 * This class finds self loops for input vertices of simplexes found in graphs.
 */
public class FindSelfLoops extends AbstractFindSimplexes {
	
	/**
	* Map for storing count of colours for self loops.
	* Note: This map is used to create probability distributions.
	*/
	private ObjectDoubleOpenHashMap<BitSet> mColoCountSelfLoop;

	public FindSelfLoops(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsInput) {
		// initialize variables
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		// initialize vertices
		mGraphsVertIds = mGraphsVertIdsInput;
		
		//initialize edge ids for simplexes found in different input graphs
		mGraphsEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		
		//initialize colocount self loop map
		mColoCountSelfLoop = new ObjectDoubleOpenHashMap<BitSet>();
		
		//compute color mapper, and estimated edges and vertices.
		findSimplexes();
		computeColorMapper();
		estimateEdges();
		estimateVertices();
		
	}
	
	@Override
	public void findSimplexes() {
		int graphId = 1;
		
		for (ColouredGraph graph : inputGrphs) {
			
			if (graph!= null) {
				//****************************** temporary variables for self loop ****************************************//
				IntSet edgesFormingSelfLoop = new DefaultIntSet(Constants.DEFAULT_SIZE); // edges for self loop
				ObjectIntOpenHashMap<BitSet> mColoCountTempSelfLoop = new ObjectIntOpenHashMap<BitSet>(); // map for vertex color of self loop
				IntSet verticesFormingSelfLoop = mGraphsVertIds.get(graphId); // get vertices to check for current graph
				
				for (int vertexID: verticesFormingSelfLoop) {//iterate over vertices
					
					BitSet vertexColour = graph.getVertexColour(vertexID); // get vertex color
					
					IntSet edgesIncidentToVertex = graph.getEdgesIncidentTo(vertexID); // find all edges incident to vertex
					
					for (int edgeForVertex: edgesIncidentToVertex) { // iterate over incident edges to find self loops
						
						if ((graph.getVerticesIncidentToEdge(edgeForVertex).size() == 1)) { // when a single vertex is incident on a edge then it is a self loop
							// self loop when both end points are same (i.e. number of vertices incident to the edge is 1)
							edgesFormingSelfLoop.add(edgeForVertex);
							mColoCountTempSelfLoop.putOrAdd(vertexColour, 1, 1);
						}
					}
				}
				
				//update stats in global map after evaluating all vertices
				updateSingleColoMapTrackingDistribution(mColoCountTempSelfLoop, mColoCountSelfLoop, edgesFormingSelfLoop.size());
				
				//store statistics in maps
				mGraphsEdgesIds.put(graphId, edgesFormingSelfLoop);
				
				graphId++;
			}
		}
	}

	
	/**
	 * Getter for average count of vertex colors for self loops
	 * @return
	 */
	public ObjectDoubleOpenHashMap<BitSet> getmColoCountSelfLoop() {
		return mColoCountSelfLoop;
	}
	
}
