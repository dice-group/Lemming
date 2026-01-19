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
 * This class analyzes isolated self loops (i.e. 1-simplexes having same head and tail color) 
 */
public class IsolatedSelfLoops extends AbstractFindSimplexes{
	
	/**
	 * Map for storing count of colours for isolated self loops (1-simplexes).
	 */
	private ObjectDoubleOpenHashMap<BitSet> mColoCountSelfLoop;

	public IsolatedSelfLoops(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions) {
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		mGraphsEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		mGraphsVertIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		//initialize global map
		mColoCountSelfLoop = new ObjectDoubleOpenHashMap<BitSet>();
		
		findSimplexes();
		computeColorMapper();
		estimateEdges();
		estimateVertices();
	}
	
	/**
	 * The method finds isolated self loops for input graphs and stores average count of colors for the vertices.
	 */
	@Override
	public void findSimplexes() {
		int graphId = 1;
		for (ColouredGraph graph : inputGrphs) {
			
			if (graph!= null) {
				
				// temporary variables to track edge Ids, number of edges and vertices for isolated self loops (1-simplexes)
				IntSet edgesFormingSelfLoop = new DefaultIntSet(Constants.DEFAULT_SIZE);
				IntSet verticesFormingSelfLoop = new DefaultIntSet(Constants.DEFAULT_SIZE);
				ObjectIntOpenHashMap<BitSet> mColoCountTempSelfLoop = new ObjectIntOpenHashMap<BitSet>(); //temporary map to store head colors found in 1-simplex for a specific graph
				
				IntSet allEdges = graph.getEdges(); // Get all edges of graph
				
				//Iterate over edges to check if they are forming 1-simplexes
				for (int edgeId: allEdges) {
					
					if (!graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour())) {
					
						//store the end point vertices of the edge
						IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edgeId);
						
						//temporary set to store all the neighbors for the end point of the edge
						IntSet tempNeigbhorsForEndpoint = new DefaultIntSet(Constants.DEFAULT_SIZE);
						
						for(int endpointVertexId: verticesIncidentToEdge) {
							tempNeigbhorsForEndpoint = IntSetUtil.union(tempNeigbhorsForEndpoint, IntSetUtil.union(graph.getInNeighbors(endpointVertexId), graph.getOutNeighbors(endpointVertexId)));
						}
						
						if (tempNeigbhorsForEndpoint.size() == 1) { // Logic to check for 1-simplexes formed by self-loops
							// add details to temporary variables
							edgesFormingSelfLoop.add(edgeId);
							verticesFormingSelfLoop.addAll(tempNeigbhorsForEndpoint);
							
							//store details of color for self loop
							int vertexSelfLoop = graph.getHeadOfTheEdge(edgeId);
							BitSet coloSelfLoop = graph.getVertexColour(vertexSelfLoop);
							mColoCountTempSelfLoop.putOrAdd(coloSelfLoop, 1, 1);
						}
						
					}
				}
				
				//************************************** Store stats for Isolated self loops found in input graphs
				// update global map tracking distribution of isolated self loops
				updateSingleColoMapTrackingDistribution(mColoCountTempSelfLoop, mColoCountSelfLoop, edgesFormingSelfLoop.size());
				
				//store the statistics in maps
				mGraphsEdgesIds.put(graphId, edgesFormingSelfLoop);
				mGraphsVertIds.put(graphId, verticesFormingSelfLoop);
				
				graphId++;
				
			}
		}
	}
	
	
	//getter
	public ObjectDoubleOpenHashMap<BitSet> getmColoCountSelfLoop() {
		return mColoCountSelfLoop;
	}
}
