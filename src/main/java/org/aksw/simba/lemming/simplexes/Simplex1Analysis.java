package org.aksw.simba.lemming.simplexes;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class Simplex1Analysis extends SimplexAnalysis {
	
	//TODO: Create interface, such a class may also be required for 0 simplex.
	
	/**
	 * Map for storing number of edges forming 1-simplexes. The key is incremented as new graph is analysed. Thus, the key for the first input graph is 1, second input graph is 2, and so on.
	 */
	private Map<Integer, Integer> mGraphIdNumberOf1SimplexEdges = new HashMap<Integer, Integer>();
	
	/**
	 * Map for storing number of vertices forming 1-simplexes. The key is similar to above map.
	 */
	private Map<Integer, Integer> mGraphIdNumberOf1SimplexVertices = new HashMap<Integer, Integer>();
	
	/**
	 * Map for storing edge ids forming 1-simplexes. The key is same as that for above hashmap.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsFor1Simplex = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map for storing count of head colours for 1-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mHeadColoCount1Simplex = new ObjectDoubleOpenHashMap<BitSet>();
	
	/**
	 * Map for storing count of head and tail colours for 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCount = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();

	/**
	 * Variable to track graph ids.
	 */
	private int graphId;
	
	/**
	 * Map for storing count of vertex colours for connected 1-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mVertColoCountConnected1Simplex = new ObjectDoubleOpenHashMap<BitSet>();
	
	/**
	 * Map for storing count of head and tail colours for connected 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCountConnected = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
	
	//*********************** Variable decalration for self loop Isolated 1-simplexes ***********************************//
	/**
	 * Map for storing number of edges forming isolated self loops. The key is incremented as new graph is analysed. Thus, the key for the first input graph is 1, second input graph is 2, and so on.
	 */
	private Map<Integer, Integer> mGraphIdNumIsolatedSelfLoopEdges = new HashMap<Integer, Integer>();
	
	/**
	 * Map for storing number of vertices forming 1-simplexes. The key is similar to above map.
	 */
	private Map<Integer, Integer> mGraphIdNumIsolatedSelfLoopVertices = new HashMap<Integer, Integer>();
	
	/**
	 * Map for storing edge ids forming isolated self loops. The key is same as that for above hashmap.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsForSelfLoop = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map for storing count of colours for isolated self loops (1-simplexes).
	 */
	private ObjectDoubleOpenHashMap<BitSet> mColoCountSelfLoop = new ObjectDoubleOpenHashMap<BitSet>();
	//*******************************************************************************************************************//
	
	//*********************** Variable decalration for self loop In 1-simplexes ***********************************//
	/**
	* Map for storing number of edges forming self loops in isolated 1-simplexes. The key is incremented as new graph is analysed. Thus, the key for the first input graph is 1, second input graph is 2, and so on.
	*/
	private Map<Integer, Integer> mGraphIdNumSelfLoopIn1SimplexEdges = new HashMap<Integer, Integer>();
		
	/**
	* Map for storing edge ids forming self loops in isolated 1-simplexes. The key is same as that for above hashmap.
	*/
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsForSelfLoopIn1Simplex = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
	/**
	* Map for storing count of colours for self loops found in isolated 1-simplexes.
	*/
	private ObjectDoubleOpenHashMap<BitSet> mColoCountSelfLoopIn1Simplex = new ObjectDoubleOpenHashMap<BitSet>();
	
	//*******************************************************************************************************************//
	
	//************************ Variable declaration for self loop in connected 1-simplexes *********************//
	/**
	 * Map object storing vertex Ids for connected 1-simplexes for every input graph.
	 * Note: This map is used to evaluate vertices and find self loops.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdConn1Simplexes = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	public Simplex1Analysis() {
		graphId = 1;
	}
	
	/**
	 * The function analyzes the input graphs and generates statistics for 1-simplex.
	 */
	public void analyze(ColouredGraph[] origGrphs) {
		
		for (ColouredGraph graph : origGrphs) {
			
			if (graph!= null) {
				
				// temporary variables to track edge Ids, number of edges and vertices for 1-simplex
				IntSet edgesForming1Simplex = new DefaultIntSet(Constants.DEFAULT_SIZE);
				IntSet verticesForming1Simplex = new DefaultIntSet(Constants.DEFAULT_SIZE);
				
				//int numberOfVertices1Simplex = 0;
				// Instead of tracking number of vertices tracking the vertices forming 1-simplexes because a pair of vertices could have multiple edges among themselves.
				
				//****************************** temporary variables for self loop ****************************************//
				
				// temporary variables to track edge Ids, number of edges and vertices for isolated self loops (1-simplexes)
				IntSet edgesFormingSelfLoop = new DefaultIntSet(Constants.DEFAULT_SIZE);
				IntSet verticesFormingSelfLoop = new DefaultIntSet(Constants.DEFAULT_SIZE);
				
				//temporary map to store head colors found in 1-simplex for a specific graph
				ObjectIntOpenHashMap<BitSet> mColoCountTempSelfLoop = new ObjectIntOpenHashMap<BitSet>();
				//******************************Temporary variables declaration end ***************************************//
				
				
				//****************************** temporary variables for self loop found for isolated 1-simplexes ****************************************//
				
				// temporary variables to track edge Ids, number of edges and vertices for self loops found in isolated 1-simplexes
				IntSet selfLoopEdgesIn1Simplex = new DefaultIntSet(Constants.DEFAULT_SIZE);
				
				//temporary map to store head colors found in 1-simplex for a specific graph
				ObjectIntOpenHashMap<BitSet> mColoCountTempSelfLoopIn1Simplex = new ObjectIntOpenHashMap<BitSet>();
				IntSet verticesFormingSelfLoopIn1Simplex = new DefaultIntSet(Constants.DEFAULT_SIZE);// track vertices that have been evaluated. If there are multiple isolated edges between vertices then same vertices will be evaluated multiple times.
				//******************************Temporary variables declaration end ***************************************//
				
				// Get all edges of graph
				IntSet allEdges = graph.getEdges();
				
				//temporary map to store head colors found in 1-simplex for a specific graph
				ObjectIntOpenHashMap<BitSet> mheadColoCountTemp = new ObjectIntOpenHashMap<BitSet>();
				
				//temporary map to store count of head-tail color found in 1-simplexes for a specific graph
				ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
				
				//temporary variable to track total number of head colors
				//int sampleSizeHeadColor = 0; // not required using computed edge set 
				
				//Iterate over edges to check if they are forming 1-simplexes
				for (int edgeId: allEdges) {
					
					if (!graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour())) {
						// Do not consider RDF Type edges
					
						//store the end point vertices of the edge
						IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edgeId);
						
						//temporary set to store all the neighbors for the end point of the edge
						IntSet tempNeigbhorsForEndpoint = new DefaultIntSet(Constants.DEFAULT_SIZE);
						
						for(int endpointVertexId: verticesIncidentToEdge) {
							tempNeigbhorsForEndpoint = IntSetUtil.union(tempNeigbhorsForEndpoint, IntSetUtil.union(graph.getInNeighbors(endpointVertexId), graph.getOutNeighbors(endpointVertexId)));
						}
						
						//tempNeigbhorsForEndpoint = IntSetUtil.difference(tempNeigbhorsForEndpoint, classNodes);
						
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
							
							// store the details of head and tail color for 1-simplex in the map
							int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
							BitSet tailColo = graph.getVertexColour(tailOfTheEdge);
							ObjectIntOpenHashMap<BitSet> mtailColoCount = mHeadTailColoCountTemp.get(headColo);
							if (mtailColoCount == null) {
								mtailColoCount = new ObjectIntOpenHashMap<BitSet>();
							}
							mtailColoCount.putOrAdd(tailColo, 1, 1);
							mHeadTailColoCountTemp.put(headColo, mtailColoCount);
							
							//************************* Self loops found in isolated 1-simplexes ************************************//
							
							//check edges for the head
							IntSet edgesIncidentToHead = graph.getEdgesIncidentTo(headOfTheEdge);
							for (int edgeForHead: edgesIncidentToHead) {
								if ((graph.getVerticesIncidentToEdge(edgeForHead).size() == 1) && (!verticesFormingSelfLoopIn1Simplex.contains(headOfTheEdge))) {
									// self loop when both end points are same (i.e. number of vertices incident to the edge is 1)
									selfLoopEdgesIn1Simplex.add(edgeForHead);
									mColoCountTempSelfLoopIn1Simplex.putOrAdd(headColo, 1, 1);
								}
							}
							verticesFormingSelfLoopIn1Simplex.add(headOfTheEdge);
							
							//check edges for tail
							IntSet edgesIncidentToTail = graph.getEdgesIncidentTo(tailOfTheEdge);
							for (int edgeForTail: edgesIncidentToTail) {
								if ((graph.getVerticesIncidentToEdge(edgeForTail).size() == 1) && (!verticesFormingSelfLoopIn1Simplex.contains(tailOfTheEdge))) {
									// self loop when both end points are same (i.e. number of vertices incident to the edge is 1)
									selfLoopEdgesIn1Simplex.add(edgeForTail);
									mColoCountTempSelfLoopIn1Simplex.putOrAdd(tailColo, 1, 1);
								}
							}
							verticesFormingSelfLoopIn1Simplex.add(tailOfTheEdge);
							
							//******************************************************************************************************//
							
						}
						
						//*********************** Temporary stats collected for Isolated self loops found in a input graph**************************************//
						// Logic to check for 1-simplexes formed by self-loops
						if (tempNeigbhorsForEndpoint.size() == 1) {
							// add details to temporary variables
							edgesFormingSelfLoop.add(edgeId);
							verticesFormingSelfLoop.addAll(tempNeigbhorsForEndpoint);
							
							//store details of color for self loop
							int vertexSelfLoop = graph.getHeadOfTheEdge(edgeId);
							BitSet coloSelfLoop = graph.getVertexColour(vertexSelfLoop);
							mColoCountTempSelfLoop.putOrAdd(coloSelfLoop, 1, 1);
						}
						//**************************************************************************************************************************************//
						
					}
				}
				
				//update global maps tracking distribution
				updateMapsTrackingDistributions(mheadColoCountTemp, mHeadTailColoCountTemp, mHeadColoCount1Simplex, mHeadColoTailColoCount, (edgesForming1Simplex.size() / 2)); // Sample size total number of vertices found for 1-simplexes divided by 2
				
				//store the statistics in maps
				mGraphIdNumberOf1SimplexEdges.put(graphId, edgesForming1Simplex.size());
				mGraphIdNumberOf1SimplexVertices.put(graphId, verticesForming1Simplex.size());
				mGraphIdEdgeIdsFor1Simplex.put(graphId, edgesForming1Simplex);
				
				//************************************** Store stats for Isolated self loops found in input graphs *********************************************//
				// update global map tracking distribution of isolated self loops
				updateSingleColoMapTrackingDistribution(mColoCountTempSelfLoop, mColoCountSelfLoop, edgesFormingSelfLoop.size());
				
				//store the statistics in maps
				mGraphIdNumIsolatedSelfLoopEdges.put(graphId, edgesFormingSelfLoop.size());
				mGraphIdNumIsolatedSelfLoopVertices.put(graphId, verticesFormingSelfLoop.size());
				mGraphIdEdgeIdsForSelfLoop.put(graphId, edgesFormingSelfLoop);
				
				//*********************************************************************************************************************************************//
				
				//*****************************************Store stats for self loops found in isolated 1-simplexes ***********************************************//
				//update global map tracking distribution of such self loops
				updateSingleColoMapTrackingDistribution(mColoCountTempSelfLoopIn1Simplex, mColoCountSelfLoopIn1Simplex, selfLoopEdgesIn1Simplex.size());
				
				//store statistics in maps
				mGraphIdNumSelfLoopIn1SimplexEdges.put(graphId, selfLoopEdgesIn1Simplex.size());
				mGraphIdEdgeIdsForSelfLoopIn1Simplex.put(graphId, selfLoopEdgesIn1Simplex);
				//*************************************************************************************************************************************************//
				
				
				graphId++;
				
			}
		}
	}
	
	/**
	 * This method finds the edge Ids for connected 1-simplexes using the below formula:
	 * Edge Ids for connected 1-Simplexes = (Edge Ids of complete graph) - (Edge Ids of triangles + Edge Ids connecting triangles with rest of the graph(referred to as common Edges above) + Edge Ids of isolated 1-Simplexes)
	 * And it also updates the global distributions for head colors, head-tail-colors.
	 * @param origGrphs - Input graphs
	 * @param mGraphsEdgesIdsTriangle - Map storing edge Ids found in triangles for every input graph.
	 * @param mGraphsCommonEdgesIds - Map object storing common edge Ids connected to vertices in triangles and other vertices that are not in triangles
	 * @param mGraphIdEdgeIdsFor1Simplex 
	 * @return
	 */
	public ObjectObjectOpenHashMap<Integer, IntSet> findEdgeIdsForConnected1Simplexes(ColouredGraph[] origGrphs, ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsTriangle, ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIds, 
			ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsFor1Simplex, ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsForSelfLoops) {
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsConnected1Simplex = new ObjectObjectOpenHashMap<Integer, IntSet>();
		
		int keyGraphId = 1;
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				IntSet allEdgeIds = graph.getEdges();
				
				IntSet edgeIdsTriangles = mGraphsEdgesIdsTriangle.get(keyGraphId);
				System.out.println(edgeIdsTriangles.size());
				IntSet edgeIdsConnectingTriangles = mGraphsCommonEdgesIds.get(keyGraphId);
				System.out.println(edgeIdsConnectingTriangles.size());
				IntSet edgeIds1Simplexes = mGraphIdEdgeIdsFor1Simplex.get(keyGraphId);
				System.out.println(edgeIds1Simplexes.size());
				
				IntSet result1 = IntSetUtil.difference(allEdgeIds, edgeIdsTriangles);
				IntSet result2 = IntSetUtil.difference(result1, edgeIdsConnectingTriangles);
				IntSet edgeIdsConnected1Simplexes = IntSetUtil.difference(result2, edgeIds1Simplexes);
				mGraphIdEdgeIdsConnected1Simplex.put(keyGraphId, edgeIdsConnected1Simplexes);
				
				//temporary map to store head colors found in 1-simplex for a specific graph
				ObjectIntOpenHashMap<BitSet> mheadColoCountTemp = new ObjectIntOpenHashMap<BitSet>();
				
				//temporary variables to store vertex IDs connecting to triangles. Note: The stored vertex ids are analyzed to determine self loops
		    	IntSet verticesConn1Simplexes = new DefaultIntSet(Constants.DEFAULT_SIZE); // variable for self loop
				
				//temporary map to store count of head-tail color found in 1-simplexes for a specific graph
				ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
				
				//temporary variable to track total number of head colors
				int sampleSizeHeadColor = 0;
				
				// Iterate and store the statistics of vertex colors for these Edges
				for (int edgeIdConnected1Simplex: edgeIdsConnected1Simplexes) {
					
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
						//mVertColoCountConnected1Simplex.putOrAdd(headColo, 1, 1); // commenting this using temporary map
						//mVertColoCountConnected1Simplex.putOrAdd(tailColo, 1, 1); // commenting this since we would creating 1-simplexes by proposing first a head color and then tail color for it
						
						// Add head and tail color's count to map
						//ObjectIntOpenHashMap<BitSet> mtailColoCountConnected = mHeadColoTailColoCountConnected.get(headColo); // commenting this using temporary map
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
				updateMapsTrackingDistributions(mheadColoCountTemp, mHeadTailColoCountTemp, mVertColoCountConnected1Simplex, mHeadColoTailColoCountConnected, sampleSizeHeadColor);
				
				//update vertex ids map
				mGraphsVertIdConn1Simplexes.put(keyGraphId, verticesConn1Simplexes);
				
				keyGraphId++;
			}
		}
		
		return mGraphIdEdgeIdsConnected1Simplex;
	}
	

	public ObjectDoubleOpenHashMap<BitSet> getmVertColoCountConnected1Simplex() {
		return mVertColoCountConnected1Simplex;
	}

	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCountConnected() {
		return mHeadColoTailColoCountConnected;
	}

	public Map<Integer, Integer> getmGraphIdNumberOf1SimplexEdges() {
		return mGraphIdNumberOf1SimplexEdges;
	}


	public Map<Integer, Integer> getmGraphIdNumberOf1SimplexVertices() {
		return mGraphIdNumberOf1SimplexVertices;
	}


	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphIdEdgeIdsFor1Simplex() {
		return mGraphIdEdgeIdsFor1Simplex;
	}
	
	public ObjectDoubleOpenHashMap<BitSet> getmHeadColoCount1Simplex() {
		return mHeadColoCount1Simplex;
	}


	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCount() {
		return mHeadColoTailColoCount;
	}

	
	//************************** Getters for Isolated Self Loops *******************************************//
	public Map<Integer, Integer> getmGraphIdNumIsolatedSelfLoopEdges() {
		return mGraphIdNumIsolatedSelfLoopEdges;
	}

	public Map<Integer, Integer> getmGraphIdNumIsolatedSelfLoopVertices() {
		return mGraphIdNumIsolatedSelfLoopVertices;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphIdEdgeIdsForSelfLoop() {
		return mGraphIdEdgeIdsForSelfLoop;
	}

	public ObjectDoubleOpenHashMap<BitSet> getmColoCountSelfLoop() {
		return mColoCountSelfLoop;
	}
	//*******************************************************************************************************//

	//*************************** Getters for Self loops in Isolated 1-simplexes ***********************************//
	public Map<Integer, Integer> getmGraphIdNumSelfLoopIn1SimplexEdges() {
		return mGraphIdNumSelfLoopIn1SimplexEdges;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphIdEdgeIdsForSelfLoopIn1Simplex() {
		return mGraphIdEdgeIdsForSelfLoopIn1Simplex;
	}

	public ObjectDoubleOpenHashMap<BitSet> getmColoCountSelfLoopIn1Simplex() {
		return mColoCountSelfLoopIn1Simplex;
	}
	//**********************************************************************************************************//

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertIdConn1Simplexes() {
		return mGraphsVertIdConn1Simplexes;
	}
}
