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
 * This class defined analysis for isolated 1-simplexes found in input graphs.
 */
public class Isolated1Simplexes extends AbstractFindSimplexes{
	
	//****************** Variables for storing average count of head color and head-tail color *****************************//
	/**
	 * Map for storing count of head colours for 1-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mHeadColoCount1Simplex;
	
	/**
	 * Map for storing count of head and tail colours for 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount;
	
	public Isolated1Simplexes(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions) {
		System.out.println("Debug!");
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;
		
		mGraphsEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		mGraphsVertIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		//initialize global map
		mHeadColoCount1Simplex = new ObjectDoubleOpenHashMap<BitSet>();
		mHeadColoTailColoCount = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		findSimplexes();
		computeColorMapper();
		estimateEdges();
		estimateVertices();
	}

	
	/**
	 *Finds Isolated 1-simplexes in input graphs and computes average count of head and tail colors for them.
	 */
	@Override
	public void findSimplexes() {
		
		int graphId = 1;
		for (ColouredGraph graph : inputGrphs) {
			
			if (graph!= null) {
				
				// temporary variables to track edge Ids, number of edges and vertices for 1-simplex
				IntSet edgesForming1Simplex = new DefaultIntSet(Constants.DEFAULT_SIZE);
				IntSet verticesForming1Simplex = new DefaultIntSet(Constants.DEFAULT_SIZE);
				
				// Get all edges of graph
				IntSet allEdges = graph.getEdges();
				
				//temporary map to store head colors found in 1-simplex for a specific graph
				ObjectIntOpenHashMap<BitSet> mheadColoCountTemp = new ObjectIntOpenHashMap<BitSet>();
				
				//temporary map to store count of head-tail color found in 1-simplexes for a specific graph
				ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
				
				//Iterate over edges to check if they are forming 1-simplexes
				for (int edgeId: allEdges) {
					
					if (!graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour())) {
						// Do not consider RDF Type edges
					
						IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edgeId);//store the end point vertices of the edge
						
						IntSet tempNeigbhorsForEndpoint = new DefaultIntSet(Constants.DEFAULT_SIZE); //temporary set to store all the neighbors for the end point of the edge
						
						for(int endpointVertexId: verticesIncidentToEdge) {
							tempNeigbhorsForEndpoint = IntSetUtil.union(tempNeigbhorsForEndpoint, IntSetUtil.union(graph.getInNeighbors(endpointVertexId), graph.getOutNeighbors(endpointVertexId)));
						}
						
						if ((tempNeigbhorsForEndpoint.size() == 2) && (verticesIncidentToEdge.size() == 2)) { 
							//2nd condition special case: 1-simple with end-point(s) having self loops. In this case, when self loop edge's neighbors are checked below it could be considered as 1-simplex
							edgesForming1Simplex.add(edgeId);
							//numberOfVertices1Simplex = numberOfVertices1Simplex + 2; // commenting this seems to be incorrect logic same pair of vertices can have multiple 1-simplexes among themselves
							verticesForming1Simplex.addAll(tempNeigbhorsForEndpoint);
							
							// store the details of head color for 1-simplex in the map
							int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
							BitSet headColo = graph.getVertexColour(headOfTheEdge);
							mheadColoCountTemp.putOrAdd(headColo, 1, 1);
							//sampleSizeHeadColor = sampleSizeHeadColor + 1; // not required using computed edge set 
							
							if (headColo.bits[0]==1)
								System.out.println("Found head colo causing issue");
							
							// store the details of head and tail color for 1-simplex in the map
							int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
							BitSet tailColo = graph.getVertexColour(tailOfTheEdge);
							ObjectIntOpenHashMap<BitSet> mtailColoCount = mHeadTailColoCountTemp.get(headColo);
							if (mtailColoCount == null) {
								mtailColoCount = new ObjectIntOpenHashMap<BitSet>();
							}
							mtailColoCount.putOrAdd(tailColo, 1, 1);
							mHeadTailColoCountTemp.put(headColo, mtailColoCount);
							
						}
						
					}
				}
				
				
				
				//update global maps tracking distribution
				updateMapsTrackingDistributions(mheadColoCountTemp, mHeadTailColoCountTemp, mHeadColoCount1Simplex, mHeadColoTailColoCount, edgesForming1Simplex.size()); // Sample size total number of vertices found for 1-simplexes divided by 2
				
				//store the statistics in maps
				mGraphsVertIds.put(graphId, verticesForming1Simplex);
				mGraphsEdgesIds.put(graphId, edgesForming1Simplex);
				
				
				graphId++;
				
			}
		}
	}


	public ObjectDoubleOpenHashMap<BitSet> getmHeadColoCount1Simplex() {
		return mHeadColoCount1Simplex;
	}


	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCount() {
		return mHeadColoTailColoCount;
	}
}
