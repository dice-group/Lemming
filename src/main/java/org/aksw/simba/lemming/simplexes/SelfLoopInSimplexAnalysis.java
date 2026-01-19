package org.aksw.simba.lemming.simplexes;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.util.Constants;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class SelfLoopInSimplexAnalysis extends SimplexAnalysis{
	
	/**
	 * Variable to track graph ids.
	 */
	private int graphId;
	
	//*********************** Variable decalration for self loop In 1-simplexes ***********************************//
	/**
	* Map for storing number of edges forming self loops. The key is incremented as new graph is analysed. Thus, the key for the first input graph is 1, second input graph is 2, and so on.
	* Note: This map is used to compute estimations.
	*/
	private Map<Integer, Integer> mGraphIdNumSelfLoop = new HashMap<Integer, Integer>();
			
	/**
	* Map for storing edge ids forming self loops. The key is same as that for above hashmap.
	* Note: This map is used for color mapper.
	*/
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsForSelfLoop = new ObjectObjectOpenHashMap<Integer, IntSet>();
			
	/**
	* Map for storing count of colours for self loops.
	* Note: This map is used to create probability distributions.
	*/
	private ObjectDoubleOpenHashMap<BitSet> mColoCountSelfLoop = new ObjectDoubleOpenHashMap<BitSet>();
	
	/**
	 * Estimated number of edges for self loop
	 */
	private int estimatedNoEdges;
	
	
	public SelfLoopInSimplexAnalysis() {
	}
	
	/**
	 * The function analyzes the input graphs and generates statistics for self loops.
	 */
	public void analyze(ColouredGraph[] origGrphs, ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIds) {
		graphId = 1;
		
		for (ColouredGraph graph : origGrphs) {
			
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
				mGraphIdNumSelfLoop.put(graphId, edgesFormingSelfLoop.size());
				mGraphIdEdgeIdsForSelfLoop.put(graphId, edgesFormingSelfLoop);
				
				
				graphId++;
			}
		}
	}


	/**
	 * Method for computing estimated number of edges based on number of self loops found.
	 * @param origGrphs - input graphs
	 * @param desiredNoVertices - expected number of vertices in output graph 
	 * @param iNoOfVersions - number of input graphs
	 */
	public void computeEstimatedEdges(ColouredGraph[] origGrphs, int desiredNoVertices, int iNoOfVersions) {
		
		//initialize graph id
		graphId = 1;
		
		// temporary variable to store edge density
		double noEdgesTemp = 0;
		
		for (ColouredGraph graph : origGrphs) {
			
			if (graph!= null) {
				
				int iNoEdges = mGraphIdNumSelfLoop.get(graphId); // get total number of edges
				int iNoVertices = graph.getVertices().size();
				noEdgesTemp += iNoEdges / (iNoVertices * 1.0);
				graphId++;
				
			}
		}
		
		// compute estimated edges for input number of vertices
		noEdgesTemp *= desiredNoVertices;
		noEdgesTemp /= iNoOfVersions;
		estimatedNoEdges = (int) Math.round(noEdgesTemp);
		
	}
	
	/**
	 * Create vertex color proposer for self loop.
	 * @param iNoOfVersions - Number of input graphs
	 * @param mRandom - Random class object
	 * @return - The distribution of vertex color for self loop.
	 */
	public OfferedItemByRandomProb<BitSet> createVertColoProposer(int iNoOfVersions, Random mRandom) {
		/**
		 * Distribution of color for the vertex with the self loop.  
		 */
		OfferedItemByRandomProb<BitSet> potentialSelfLoopColoProposer;
		
		int sampleSize = mColoCountSelfLoop.assigned;
		
		// initialize array for sample space and values
		BitSet[] vertColoSampleSpace = new BitSet[sampleSize];
		double[] vertColoSampleValue = new double[sampleSize];
				
		int sampleSpaceIndex = 0;
		//iterate list of head colors and add values to sample space and values
		Object[] keysVertColo = mColoCountSelfLoop.keys;
		for(int i=0; i < keysVertColo.length; i++) {
			if (mColoCountSelfLoop.allocated[i]) {
				vertColoSampleSpace[sampleSpaceIndex] = (BitSet) keysVertColo[i];
							
				double countOfVertColo = mColoCountSelfLoop.get((BitSet) keysVertColo[i]);
				double sampleDistribution = countOfVertColo * 1.0 / iNoOfVersions;
				vertColoSampleValue[sampleSpaceIndex] = sampleDistribution;
							
				sampleSpaceIndex++;
			}
		}
				
		ObjectDistribution<BitSet> potentialHeadColo = new ObjectDistribution<BitSet>(vertColoSampleSpace, vertColoSampleValue);
				
		if (!potentialHeadColo.isEmpty()) // Distribution can be created only when sample space is not null
			potentialSelfLoopColoProposer = new OfferedItemByRandomProb<BitSet>(potentialHeadColo, mRandom);
		else
			potentialSelfLoopColoProposer = null;
		
		return potentialSelfLoopColoProposer;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphIdEdgeIdsForSelfLoop() {
		return mGraphIdEdgeIdsForSelfLoop;
	}

	public int getEstimatedNoEdges() {
		return estimatedNoEdges;
	}
	
}
