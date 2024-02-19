package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IsoS1C extends AbstractFindSimplexes{
	
	/**
	 * Map for storing vertex colors for the edge along with the probability for them and distributions in an array.
	 * Note: Count of edges is stored at the 0th index of the array,  
	 * the probability of an edge is stored at the 1st index 
	 * and the average count of edges per no. of vertices in the input graph is stored at the 2nd index. 
	 */
	private ObjectObjectOpenHashMap<EdgeColos, double[]> mColoEdgesCountDistAvg = new ObjectObjectOpenHashMap<EdgeColos, double[]>();

	//****************** Variables for storing average count of head color and head-tail color *****************************//
		
	public IsoS1C(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions) {
		inputGrphs = origGrphs;
		inputDesiredNoVert = desiredNoVertices;
		mNumOfInputGrphs = iNoOfVersions;

		mGraphsEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
		mGraphsVertIds = new ObjectObjectOpenHashMap<Integer, IntSet>();

		findSimplexes();
		computeColorMapper();
		estimateEdges();
		estimateVertices();
	}

	/**
	 * Finds Isolated 1-simplexes in input graphs and computes average count of head
	 * and tail colors for them.
	 */
	@Override
	public void findSimplexes() {

		int graphId = 1;
		for (ColouredGraph graph : inputGrphs) {

			if (graph != null) {

				// temporary variables to track edge Ids, number of edges and vertices for
				// 1-simplex
				IntSet edgesForming1Simplex = new DefaultIntSet(Constants.DEFAULT_SIZE);
				IntSet verticesForming1Simplex = new DefaultIntSet(Constants.DEFAULT_SIZE);

				// Get all edges of graph
				IntSet allEdges = graph.getEdges();
				
				ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColosCountsTemp = new ObjectObjectOpenHashMap<EdgeColos, double[]>();


				// Iterate over edges to check if they are forming 1-simplexes
				for (int edgeId : allEdges) {

					if (!graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour())) {
						// Do not consider RDF Type edges

						IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edgeId);

						IntSet tempNeigbhorsForEndpoint = new DefaultIntSet(Constants.DEFAULT_SIZE); 

						for (int endpointVertexId : verticesIncidentToEdge) {
							tempNeigbhorsForEndpoint = IntSetUtil.union(tempNeigbhorsForEndpoint, IntSetUtil.union(
									graph.getInNeighbors(endpointVertexId), graph.getOutNeighbors(endpointVertexId)));
						}

						if ((tempNeigbhorsForEndpoint.size() == 2) && (verticesIncidentToEdge.size() == 2)) {
							
							edgesForming1Simplex.add(edgeId);
							
							verticesForming1Simplex.addAll(tempNeigbhorsForEndpoint);

							// store the details of head color for 1-simplex in the map
							int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
							BitSet headColo = graph.getVertexColour(headOfTheEdge);
							

							// store the details of head and tail color for 1-simplex in the map
							int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
							BitSet tailColo = graph.getVertexColour(tailOfTheEdge);
							
							//*****************Logic to store edge colors information **********************//
							EdgeColos edgeColoObj = new EdgeColos(headColo, tailColo); // initialize Edge Colors object
							
							if (mEdgeColosCountsTemp.containsKey(edgeColoObj)) {
								// Edge Color was found previously update the count in map
								double[] valuesEdgeColo = mEdgeColosCountsTemp.get(edgeColoObj);
								valuesEdgeColo[0]= valuesEdgeColo[0] + 1.0;
								valuesEdgeColo[1]= valuesEdgeColo[1] + 1.0; // probability will updated later
								valuesEdgeColo[2]= valuesEdgeColo[2] + 1.0; // distribution of edge will be updated later
								
								mEdgeColosCountsTemp.put(edgeColoObj, valuesEdgeColo);
							}else {
								// Edge Color found for the first time
								double[] valuesEdgeColo = new double[3];
								valuesEdgeColo[0] = 1.0;
								valuesEdgeColo[1] = 1.0; // probability will updated later
								valuesEdgeColo[2] = 1.0; // distribution of edge will be updated later
								
								mEdgeColosCountsTemp.put(edgeColoObj, valuesEdgeColo);
								
							}
							
						}

					}
				}

				// store the statistics in maps
				mGraphsVertIds.put(graphId, verticesForming1Simplex);
				mGraphsEdgesIds.put(graphId, edgesForming1Simplex);
				
				//update global map to store Edge Colors combination
				updateCountGlobalMap1Simplexes(graph, mColoEdgesCountDistAvg, mEdgeColosCountsTemp, edgesForming1Simplex.size());

				graphId++;

			}
		}
	}

	public ObjectObjectOpenHashMap<EdgeColos, double[]> getmColoEdgesCountDistAvg() {
		return mColoEdgesCountDistAvg;
	}


	
}
