package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.constraints.ColourMappingRulesSimplexes;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class defining default implemenation for analysis of simplexes.
 * Note: The default implementation computes the estimated number of edges, vertices and colourmapper.
 * 		 While extending this class, the implementation of findSimplexes method needs to defined that 
 * 		 finds edges and verts for the simplexes in input graph and updates the map.
 */
public abstract class AbstractFindSimplexes implements ISimplexesAnalysis{
	
	/**
	 * Variable storing estimated number of edges
	 */
	protected int estEdges = 0;
	
	/**
	 * Variable storing estimated number of vertices
	 */
	protected int estVertices = 0;
	
	/**
	 * The Colour mapper used for simplexes.
	 */
	protected IColourMappingRules mColourMapperSimplexes;
	
	/**
	 * Map object storing edge Ids found for simplexes in every input graph.
	 * Note: They are used for estimating edges in output graph.
	 */
	protected ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIds;
	
	/**
	 * Map object storing vertex Ids found for simplexes in every input graph.
	 * Note: They are used for estimating vertices in output graph.
	 */
	protected ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIds;
	
	/**
	 * Input RDF graphs
	 */
	protected ColouredGraph[] inputGrphs;
	
	/**
	 * Number of vertices in the output graph.
	 */
	protected int inputDesiredNoVert;
	
	/**
	 * Number of input graphs
	 */
	protected int mNumOfInputGrphs;
	
	protected ObjectObjectOpenHashMap<EdgeColorsSorted, ObjectDoubleOpenHashMap<BitSet>> mVertColosPropDist = new ObjectObjectOpenHashMap<EdgeColorsSorted, ObjectDoubleOpenHashMap<BitSet>>();
	
	public AbstractFindSimplexes() {
		
	}
	
	public AbstractFindSimplexes(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions) {
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
	 * This method defines the logic for finding simplexes. 
	 * This implementation will vary based on the dimension of the simplexes considered. Thus, the default implementation does not do anything.
	 */
	public void findSimplexes() {
		
	}
	
	
	/**
	 * The method defines the logic to determine color mapper. It needs to be computed for creating properties for simplexes.
	 * Default implementation: Defines color mapper based on edges found for simplexes.
	 */
	public void computeColorMapper() {
		mColourMapperSimplexes = new ColourMappingRulesSimplexes(mGraphsEdgesIds);
		mColourMapperSimplexes.analyzeRules(inputGrphs);
	}
	
	/**
	 * The calculation of estimated edges for simplexes is defined in this method.
	 */
	public void estimateEdges() {
		//initialize graph id
		int graphId = 1;
		
		// temporary variable to store edge density
		double noEdgesTemp = 0;
		
		for (ColouredGraph graph : inputGrphs) {
			
			if (graph!= null) {
				
				int iNoEdges = mGraphsEdgesIds.get(graphId).size(); // get total number of edges
				int iNoVertices = graph.getVertices().size();
				noEdgesTemp += iNoEdges / (iNoVertices * 1.0);
				
				graphId++;
				
			}
		}
		
		// compute estimated edges for input number of vertices
		noEdgesTemp *= inputDesiredNoVert;
		noEdgesTemp /= mNumOfInputGrphs;
		estEdges = (int) Math.round(noEdgesTemp);
	}
	
	/**
	 * The calculation of estimated vertices for simplexes is defined in this method.
	 */
	public void estimateVertices() {
		//initialize graph id
		int graphId = 1;
		
		// temporary variable to store edge density
		double noVertsTemp = 0;
		
		for (ColouredGraph graph : inputGrphs) {
			
			if (graph!= null) {
				
				int iNoVerts = mGraphsVertIds.get(graphId).size(); // get total number of edges
				int iNoVertices = graph.getVertices().size();
				noVertsTemp += iNoVerts / (iNoVertices * 1.0);
				
				graphId++;
			}
		}
		
		// compute estimated edges for input number of vertices
		noVertsTemp *= inputDesiredNoVert;
		noVertsTemp /= mNumOfInputGrphs;
		estVertices = (int) Math.round(noVertsTemp);
	}
	
	// helper functions used to compute average counts
	/**
	 * This method updates the global map that tracks the distribution of single color, using the temporary map. The input temp map stores the count of colors for different vertices. 
	 * For these maps, probability distribution with respect to specific input graphs needs to be calculated. This function is repeatedly call to sum up the distributions for different input graphs and store it in the global map.
	 * @param mheadColoCountTemp
	 * @param mHeadTailColoCountTemp
	 * @param mGlobalHeadColoCount
	 * @param mGlobalHeadTailColoCount
	 */
	public void updateSingleColoMapTrackingDistribution(ObjectIntOpenHashMap<BitSet> mheadColoCountTemp, ObjectDoubleOpenHashMap<BitSet> mGlobalHeadColoCount, int sampleSizeHeadColo) {
		//find distribution of head color count for input graph and update the global map
		Object[] keysheadColo = mheadColoCountTemp.keys;
		for (int i=0; i < keysheadColo.length; i++) {
			if (mheadColoCountTemp.allocated[i]) {
				BitSet headColo = (BitSet) keysheadColo[i];
				double distributionInGraph = mheadColoCountTemp.get(headColo) * 1.0 / (sampleSizeHeadColo);
				mGlobalHeadColoCount.putOrAdd(headColo, distributionInGraph, distributionInGraph);
			}
		}
	}
	
	/**
	 * This method updates the global maps that track the distributions, using the temporary maps. The first map stores the count of head colors for different vertices and the second map stores the head color, tail color and their count. 
	 * For these maps, probability distribution with respect to specific input graphs needs to be calculated. This function is repeatedly call to sum up the distributions for different input graphs and store it in global maps.
	 * @param mheadColoCountTemp
	 * @param mHeadTailColoCountTemp
	 * @param mGlobalHeadColoCount
	 * @param mGlobalHeadTailColoCount
	 */
	protected void updateMapsTrackingDistributions(ObjectIntOpenHashMap<BitSet> mheadColoCountTemp, 
			ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mHeadTailColoCountTemp, 
			ObjectDoubleOpenHashMap<BitSet> mGlobalHeadColoCount, 
			ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mGlobalHeadTailColoCount, int sampleSizeHeadColo) {
		//find distribution of head color count for input graph and update the global map
		Object[] keysheadColo = mheadColoCountTemp.keys;
		for (int i=0; i < keysheadColo.length; i++) {
			if (mheadColoCountTemp.allocated[i]) {
				BitSet headColo = (BitSet) keysheadColo[i];
				double distributionInGraph = mheadColoCountTemp.get(headColo) * 1.0 / (sampleSizeHeadColo);
				mGlobalHeadColoCount.putOrAdd(headColo, distributionInGraph, distributionInGraph);
				
				//find distribution of head-tail color count for input graph and update the global map
				ObjectIntOpenHashMap<BitSet> mTailColoCount = mHeadTailColoCountTemp.get(headColo);
				
				//variable to track total number of tail colors for the head color
				int totalnumOfTailColo = 0;
				
				// iterate over all tail colors and get total number of tail colors for a specific head color
				Object[] keysTailColo = mTailColoCount.keys;
				
				for (int tailIndex = 0; tailIndex < keysTailColo.length; tailIndex++) {
					if (mTailColoCount.allocated[tailIndex]) {
						BitSet tailColo = (BitSet) keysTailColo[tailIndex];
						totalnumOfTailColo = totalnumOfTailColo + mTailColoCount.get(tailColo);
					}
				}
				
				for (int tailIndex = 0; tailIndex < keysTailColo.length; tailIndex++) {
					if (mTailColoCount.allocated[tailIndex]) {
						BitSet tailColo = (BitSet) keysTailColo[tailIndex];
						double distributionTailInGraph = mTailColoCount.get(tailColo) * 1.0 / totalnumOfTailColo;
						
						ObjectDoubleOpenHashMap<BitSet> globalTailColoCount = mGlobalHeadTailColoCount.get(headColo);
						if (globalTailColoCount == null) {
							globalTailColoCount = new ObjectDoubleOpenHashMap<BitSet>();
						}
						globalTailColoCount.putOrAdd(tailColo, distributionTailInGraph, distributionTailInGraph);
						mGlobalHeadTailColoCount.put(headColo, globalTailColoCount);
					}
				}
				
			}
		}
	}
	
	protected void updateCountGlobalMap1Simplexes(ColouredGraph graph, ObjectObjectOpenHashMap<EdgeColos, double[]> mGlobalColoEdgesCountDistAvg, ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgeColosEdgeCountsTemp, int countEdges) {
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
				if (mGlobalColoEdgesCountDistAvg.containsKey(edgeColos)) {

					// get old distribution values
					double[] previousCountdist = mGlobalColoEdgesCountDistAvg.get(edgeColos);

					// update the distribution values
					arrCountDistTemp[0] = arrCountDistTemp[0] + previousCountdist[0];
					arrCountDistTemp[1] = arrCountDistTemp[1] + previousCountdist[1];
					arrCountDistTemp[2] = arrCountDistTemp[2] + previousCountdist[2];

				}
				mGlobalColoEdgesCountDistAvg.put(edgeColos, arrCountDistTemp); // update the map with the new array
			}
		}
    }
	
	public void computePropertyProbabilities() {
		int graphId = 1;
		
		for (ColouredGraph graph : inputGrphs) {
			
			if (graph!= null) {
				ObjectObjectOpenHashMap<EdgeColorsSorted, ObjectIntOpenHashMap<BitSet>> mEdgeColosPropCountTemp = new ObjectObjectOpenHashMap<EdgeColorsSorted, ObjectIntOpenHashMap<BitSet>>();
				
				IntSet edgesSet = mGraphsEdgesIds.get(graphId); // get total number of edges
				for (int edgeId: edgesSet) {// iterate over edges
					
					BitSet edgeColour = graph.getEdgeColour(edgeId); // get edge color
					IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edgeId);
					
					BitSet[] vertexColors = new BitSet[2];
					int tempIndex = 0;
					// get vertex colors
					for (int vertexId: verticesIncidentToEdge) {
						BitSet vertexColour = graph.getVertexColour(vertexId);
						vertexColors[tempIndex] = vertexColour;
						tempIndex++;
					}
					
					if (verticesIncidentToEdge.size() == 1)
						vertexColors[1] = vertexColors[0];
					
					EdgeColorsSorted edgeColorsSorted = new EdgeColorsSorted(vertexColors[0], vertexColors[1]);
					
					ObjectIntOpenHashMap<BitSet> mPropColoCount = mEdgeColosPropCountTemp.get(edgeColorsSorted);
					if (mPropColoCount == null) {
						mPropColoCount = new ObjectIntOpenHashMap<BitSet>();
					}
					mPropColoCount.putOrAdd(edgeColour, 1, 1);
					mEdgeColosPropCountTemp.put(edgeColorsSorted, mPropColoCount);
					
				}
				
				
				// update global map
				updatePropertyMap(mEdgeColosPropCountTemp);
				
				graphId++;
				
			}
		}
	}
	
	private void updatePropertyMap(ObjectObjectOpenHashMap<EdgeColorsSorted, ObjectIntOpenHashMap<BitSet>> mEdgeColosPropCountTemp) {
		
		Object[] keysEdgeColo = mEdgeColosPropCountTemp.keys;
		
		for (int i = 0; i < keysEdgeColo.length; i++) {
			if (mEdgeColosPropCountTemp.allocated[i]) {
				EdgeColorsSorted edgeColorsSorted = (EdgeColorsSorted) keysEdgeColo[i];
				
				//find distribution for input graph and update the global map
				ObjectIntOpenHashMap<BitSet> mColoCount = mEdgeColosPropCountTemp.get(edgeColorsSorted);
				
				//variable to track total number of tail colors for the head color
				int totalnumOfpropColo = 0;
				
				// iterate over all tail colors and get total number of tail colors for a specific head color
				Object[] keysPropColo = mColoCount.keys;
				
				for (int propIndex = 0; propIndex < keysPropColo.length; propIndex++) {
					if (mColoCount.allocated[propIndex]) {
						BitSet propColo = (BitSet) keysPropColo[propIndex];
						totalnumOfpropColo = totalnumOfpropColo + mColoCount.get(propColo);
					}
				}
				
				for (int propIndex = 0; propIndex < keysPropColo.length; propIndex++) {
					if (mColoCount.allocated[propIndex]) {
						BitSet propColo = (BitSet) keysPropColo[propIndex];
						double distributionPropInGraph = mColoCount.get(propColo) * 1.0 / totalnumOfpropColo;
						
						ObjectDoubleOpenHashMap<BitSet> globalPropColoCount = mVertColosPropDist.get(edgeColorsSorted);
						if (globalPropColoCount == null) {
							globalPropColoCount = new ObjectDoubleOpenHashMap<BitSet>();
						}
						globalPropColoCount.putOrAdd(propColo, distributionPropInGraph, distributionPropInGraph);
						mVertColosPropDist.put(edgeColorsSorted, globalPropColoCount);
					}
				}
				
			}
		}
		
	}
	
	// Defining getters for computed variables

	public int getEstEdges() {
		return estEdges;
	}

	public int getEstVertices() {
		return estVertices;
	}

	public ObjectObjectOpenHashMap<EdgeColorsSorted, ObjectDoubleOpenHashMap<BitSet>> getmVertColosPropDist() {
		return mVertColosPropDist;
	}

	public IColourMappingRules getmColourMapperSimplexes() {
		return mColourMapperSimplexes;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIds() {
		return mGraphsEdgesIds;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertIds() {
		return mGraphsVertIds;
	}
	
	
}
