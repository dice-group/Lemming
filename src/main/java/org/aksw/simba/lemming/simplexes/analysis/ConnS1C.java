package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ConnS1C extends AbstractFindSimplexes{
	
	/**
	 * Map for storing vertex colors for the edge along with the probability for them and distributions in an array.
	 * Note: Count of edges is stored at the 0th index of the array,  
	 * the probability of an edge is stored at the 1st index 
	 * and the average count of edges per no. of vertices in the input graph is stored at the 2nd index. 
	 */
	private ObjectObjectOpenHashMap<EdgeColos, double[]> mColoEdgesCountDistAvg = new ObjectObjectOpenHashMap<EdgeColos, double[]>();
	
	/**
	 * Map for storing count of vertex colours for connected 1-simplexes.
	 */
	private ObjectDoubleOpenHashMap<BitSet> mHeadColoCountConnected1Simplex;
	
	/**
	 * Map for storing count of head and tail colours for connected 1-simplexes.
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mHeadColoTailColoCountConnected;
	
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsAllSimplexesTemp;
	
	public ConnS1C(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions, ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsAllSimplexes) {
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
				IntSet result = IntSetUtil.difference(allEdgeIds, edgeIdsAllsimplexes); // all edges subtracted from edges of all other simplexes
				
				mGraphsEdgesIds.put(keyGraphId, result); //store the edges in global map
				
				//temporary map to store head colors found in 1-simplex for a specific graph
				ObjectIntOpenHashMap<BitSet> mheadColoCountTemp = new ObjectIntOpenHashMap<BitSet>();
				
				//temporary variables to store vertex IDs connecting to triangles. Note: The stored vertex ids are analyzed to determine self loops
		    	IntSet verticesConn1Simplexes = new DefaultIntSet(Constants.DEFAULT_SIZE); // variable for self loop
				
				//temporary map to store count of head-tail color found in 1-simplexes for a specific graph
				ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
				
				//temporary variable to track total number of head colors
				int sampleSizeHeadColor = 0;
				
				ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColosCountsTemp = new ObjectObjectOpenHashMap<EdgeColos, double[]>();
				
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
				
				//update global maps tracking distribution
				updateMapsTrackingDistributions(mheadColoCountTemp, mHeadTailColoCountTemp, mHeadColoCountConnected1Simplex, mHeadColoTailColoCountConnected, sampleSizeHeadColor);
				
				//update vertex ids map
				mGraphsVertIds.put(keyGraphId, verticesConn1Simplexes);
				
				//update global map to store Edge Colors combination
				updateCountConn1Simplexes(graph, mEdgeColosCountsTemp, result.size());
				
				keyGraphId++;
			}
		}
	}
	
	private void updateCountConn1Simplexes(ColouredGraph graph, ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColosEdgeCountsTemp, int countEdges) {
		double totalVertices = graph.getNumberOfVertices();
           
        //******************************* Logic to update map for connected triangles *********************************//
           
        // Iterate over temporary map and add an element at the 3rd index of the array (No. of edges in triangle)/(Total number of triangles)
        Object[] keysTriColo = mEdgeColosEdgeCountsTemp.keys;
           
		// iterate over results and update the global map
		keysTriColo = mEdgeColosEdgeCountsTemp.keys;
		for (int i = 0; i < keysTriColo.length; i++) {
			if (mEdgeColosEdgeCountsTemp.allocated[i]) {
				EdgeColos edgeColos = (EdgeColos) keysTriColo[i];
				double[] arrCountDist = mEdgeColosEdgeCountsTemp.get(edgeColos);

				// create a new array for storing avg count
				double[] arrCountDistTemp = new double[4]; // additional element in the array for storing average
															// count of triangles per vertices
				arrCountDistTemp[0] = arrCountDist[0];
				arrCountDistTemp[1] = (arrCountDist[1] * 1.0) / countEdges;
				arrCountDistTemp[2] = (arrCountDist[2] * 1.0) / totalVertices; 

				// update the distribution in global maps
				if (mColoEdgesCountDistAvg.containsKey(edgeColos)) {

					// get old distribution values
					double[] previousCountdist = mColoEdgesCountDistAvg.get(edgeColos);

					// update the distribution values
					arrCountDistTemp[0] = arrCountDistTemp[0] + previousCountdist[0];
					arrCountDistTemp[1] = arrCountDistTemp[1] + previousCountdist[1];
					arrCountDistTemp[2] = arrCountDistTemp[2] + previousCountdist[2];

				}
				mColoEdgesCountDistAvg.put(edgeColos, arrCountDistTemp); // update the map with the new array
			}
		}
    }
	
	//getters
	public ObjectDoubleOpenHashMap<BitSet> getmHeadColoCountConnected1Simplex() {
		return mHeadColoCountConnected1Simplex;
	}

	public ObjectObjectOpenHashMap<EdgeColos, double[]> getmColoEdgesCountDistAvg() {
		return mColoEdgesCountDistAvg;
	}

	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getmHeadColoTailColoCountConnected() {
		return mHeadColoTailColoCountConnected;
	}

}
