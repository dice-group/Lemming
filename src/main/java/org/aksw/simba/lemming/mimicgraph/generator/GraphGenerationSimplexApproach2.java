package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric2;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.constraints.ColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.ColourMappingRulesSimplexes;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.simplexes.Simplex0Analysis;
import org.aksw.simba.lemming.simplexes.SelfLoopInSimplexAnalysis;
import org.aksw.simba.lemming.simplexes.SelfLoopsInGraphs;
import org.aksw.simba.lemming.simplexes.Simplex0Distribution;
import org.aksw.simba.lemming.simplexes.Simplex1Analysis;
import org.aksw.simba.lemming.simplexes.Simplex1Distribution;
import org.aksw.simba.lemming.simplexes.Simplex2Analysis;
import org.aksw.simba.lemming.simplexes.Simplex2Distribution;
import org.aksw.simba.lemming.simplexes.TriColos;
import org.aksw.simba.lemming.simplexes.TriangleDistribution;
import org.aksw.simba.lemming.simplexes.TriangleDistribution2;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.aksw.simba.lemming.util.MapUtil;
import org.apache.commons.collections.SetUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Sets.SetView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphGenerationSimplexApproach2 extends AbstractGraphGeneration implements IGraphGeneration {
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationSimplexApproach2.class);
	private static int maximumIteration;
	
	/**
	 * NodeIterator metric object used in this class to collect information about triangles present in different input graphs. 
	 */
	private NodeIteratorMetric2 metric;
	
	/**
	 * Estimated number of edges for connected triangles.
	 */
	private int estimatedEdgesTriangle;
	
	/**
	 * Estimated number of edges connecting vertices present in triangles and vertices not in triangles.
	 */
	private int estimatedEdgesCommon;
	
	
	/**
	 * Estimated number of type edges not in triangle.
	 */
	private int estimatedTypeEdgesNotTriangle;
	
	/**
	 * Estimated number of Vertices forming triangles
	 */
	private int estimatedVerticesTriangle;
	
	/**
	 * Estimated number of vertices present in triangles and connected to 1-simplex.
	 */
	private int estimatedVerticesCommon;
	
	/**
	 * Estimated number of edges connecting two simplexes
	 */
	private int estimatedEdges1SimplexesConnect2Simplexes;
	
	/**
	 * Map for storing graphs along with their total count of edges. This count includes rdf type edges, which is required for estimating edges in the output graph.  
	 */
	private Map<Integer, Integer> mGraphEdges;
	
	/**
	 * Variable to store number of input graphs.
	 */
	private int iNoOfVersions;
	
	/**
	 * Map for storing vertex colors of triangles as keys and array of triangle and edge counts as value.
	 */
	private ObjectObjectOpenHashMap<TriColos, double[]> mTriangleColoursTriangleEdgeCounts;
	
	private Set<TriColos> setAllTriangleColours;
	
	/**
	 * This class object stores information related triangles.
	 */
	private TriangleDistribution2 triangledistributionObject;
	
	/**
	 * Map for tracking triangle colors and vertex IDs forming triangles using those colors
	 */
	private ObjectObjectOpenHashMap<TriColos, List<IntSet>> mTriangleColorsVertexIds; 
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs2Simplex = new ConcurrentHashMap<BitSet, IntSet>();
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToEdgeIDs2Simplex = new ConcurrentHashMap<BitSet, IntSet>();
	
	/**
	 * Map for tracking vertices denoting classes
	 */
	private Map<BitSet, Integer> mMapClassColourToVertexIDSimplexes = new ConcurrentHashMap<BitSet, Integer>();
	
	/**
	 * ColourMappingRules define the possible combination of colours between vertices and edges. This variable is specifically for Triangles
	 */
	private IColourMappingRules mColourMapperTriangles;
	
	/**
	 * This Colour Mapper variable is specifically for common edges connecting triangles in input graphs with vertices that are not part of triangles.
	 */
	private IColourMappingRules mColourMapperCommonEdges;
	
	/**
	 * This Colour Mapper variable is specifically for edges that are not part of triangles.
	 */
	//private IColourMappingRules mColourMapperNotTriangles;
	
	/**
	 * This class object is used to create 1-simplexes that connect with triangles (2-simplexes).
	 */
	private Simplex2Distribution simplex2distObj;
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDsConnectedTo2Simplex = new ConcurrentHashMap<BitSet, IntSet>();
	
	//**************** Objects for isolated 2-simplexes ************************************//
	/**
	 * ColourMappingRules define the possible combination of colours between vertices and edges. This variable is specifically for Triangles
	 */
	private IColourMappingRules mColourMapperIsolatedTriangles;
	
	/**
	 * Estimated number of edges for isolated triangles.
	 */
	private int estimatedEdgesIsolatedTriangle;
	
	/**
	 * Estimated number of Vertices forming triangles
	 */
	private int estimatedVerticesIsolatedTriangle;
	
	//TODO: Below variables for isolated use case are not absolutely necessary. For now, added to create a generic function could be removed later.
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for isolated 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs2SimplexIsolated = new ConcurrentHashMap<BitSet, IntSet>();
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for isolated 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToEdgeIDs2SimplexIsolated = new ConcurrentHashMap<BitSet, IntSet>();
	
	/**
	 * Map for tracking triangle colors and vertex IDs forming isolated triangles using those colors
	 */
	private ObjectObjectOpenHashMap<TriColos, List<IntSet>> mIsolatedTriangleColorsVertexIds = new ObjectObjectOpenHashMap<TriColos, List<IntSet>>();
	
	
	//**************** Objects for 1-simplexes connecting triangles ************************//
	private Simplex1Distribution simplex1ConnTriDist;
	
	/**
	 * This Colour Mapper variable is specifically for 1-simplexes that link triangles.
	 */
	private IColourMappingRules mColourMapper1SimplexesConnTri;
	
	//**************** Objects for 1-simplex ******************************//
	/**
	 * Map for storing number of 1-simplex edges for different input graphs. Note: It considers 1-simplexes that are not connected to any other 1-simplexes.
	 */
	private Map<Integer, Integer> mGraphIdNumberOf1SimplexEdges;
	
	/**
	 * Map for storing number of vertices forming 1-simplexes for different input graphs. Note: Similar to above, this map also considers 1-simplexes that are not connected to any other 1-simplexes.
	 */
	private Map<Integer, Integer> mGraphIdNumberOf1SimplexVertices;
	
	/**
	 * This Colour Mapper variable is specifically for isolated 1-simplexes.
	 */
	private IColourMappingRules mColourMapper1Simplexes;
	
	/**
	 * Estimated number of edges forming 1-simplexes
	 */
	private int estimatedEdges1Simplexes;
	
	/**
	 * Estimated number of Vertices forming 1-simplexes
	 */
	private int estimatedVertices1Simplexes;
	
	/**
	 * This class object is used to create 1-simplexes in mimic graph.
	 */
	private Simplex1Distribution simplex1distObj;
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for 1-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs1Simplex = new ConcurrentHashMap<BitSet, IntSet>();
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for isolated self loop 1-simplexes (1-simplexes with same head and tail color)
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDsIsoSelfLoop = new ConcurrentHashMap<BitSet, IntSet>();
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value
	 */
	private Map<BitSet, IntSet> mMapColourToEdgeIDs1Simplex = new ConcurrentHashMap<BitSet, IntSet>();
	
	/**
	 * This Colour Mapper variable is specifically for connected 1-simplexes
	 */
	private IColourMappingRules mColourMapperConnected1Simplexes;
	
	/**
	 * This class object is used to create 1-simplexes in mimic graph.
	 */
	private Simplex1Distribution connectedSimplex1distObj;
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for connected 1-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs1SimplexConnected = new ConcurrentHashMap<BitSet, IntSet>();
	
	/**
	 * Map for storing number of vertices for 1-simplexes only connected to triangles. Note: Similar to above, this map also considers 1-simplexes that are not connected to any other 1-simplexes.
	 */
	private Map<Integer, Integer> mGraphId1SimplexVertsConnToTri;
	
	/**
	 * Estimated number of Vertices forming 1-simplexes that are connected to triangles.
	 */
	private int estVerts1SimplexesConntoTri;
	
	//**************** Objects for Isolated Self Loops ******************************//
	/**
	* Map for storing number of Isolated self loop edges (1-simplexes with same head and tail) for different input graphs.
	*/
	private Map<Integer, Integer> mGraphIdNumIsoSelfLoopEdges;
		
	/**
	* Map for storing number of vertices forming Isolated self loop (1-simplexes with same head and tail) for different input graphs.
	*/
	private Map<Integer, Integer> mGraphIdNumIsoSelfLoopVertices;
		
	/**
	* This Colour Mapper variable is specifically for isolated self loop (1-simplexes with same head and tail).
	*/
	private IColourMappingRules mColourMapperIsoSelfLoop;
		
	/**
	* Estimated number of edges forming isolated self loop (1-simplexes with same head and tail).
	*/
	private int estimatedEdgesIsoSelfLoop;
		
	/**
	* Estimated number of Vertices forming 1-simplexes
	*/
	private int estimatedVerticesIsoSelfLoop;
	
	//**************** Objects for Self Loops in Isolated 1-simplexes ******************************//
	/**
	* Map for storing number of self loop edges in isolated 1-simplexes found for different input graphs.
	*/
	private Map<Integer, Integer> mGraphIdNumSelfLoopIn1SimplexEdges;
			
	/**
	* This Colour Mapper variable is specifically for self loop in isolated 1-simplexes
	*/
	private IColourMappingRules mColourMapperSelfLoopIn1Simplex;
			
	/**
	* Estimated number of edges forming self loop in isolated 1-simplexes
	*/
	private int estimatedEdgesSelfLoopIn1Simplex;
	
	//**************** Variables storing estimation of Self Loops ******************//
	private int estEdgesSelfLoopIsoTri;
	
	private int estEdgesSelfLoopConnTri;
	
	private int estEdgesSelfLoop1SimplexConnToTri;
	
	private int estEdgesSelfLoopConn1Simplexes;
	
	//*************** Color mapper for self loops ***********************//
	/**
	 * This Colour Mapper variable is specifically for self loops for isolated triangles.
	 */
	private IColourMappingRules mColourMapperSelfLoopIsoTri;
	
	/**
	 * This Colour Mapper variable is specifically for self loops for connected triangles.
	 */
	private IColourMappingRules mColourMapperSelfLoopConnTri;
	
	/**
	 * This Colour Mapper variable is specifically for self loops for 1-simplexes linked to connected triangles.
	 */
	private IColourMappingRules mColourMapperSelfLoop1SimplexConnToTri;
	
	/**
	 * This Colour Mapper variable is specifically for self loops for connected 1-simplexes.
	 */
	private IColourMappingRules mColourMapperSelfLoopConn1Simplexes;
	
	//**************** Probability distribution for Self loops *************************//
	private OfferedItemByRandomProb<BitSet> distColoProposerSelfLoopIsoTri;
	
	private OfferedItemByRandomProb<BitSet> distColoProposerSelfLoopConnTri;
	
	private OfferedItemByRandomProb<BitSet> distColoProposerSelfLoop1SimplexConnToTri;
	
	private OfferedItemByRandomProb<BitSet> distColoProposerSelfLoopConn1Simplexes;
	
	//******************* Objects for 0-simplexes ************************//
	/**
	 * Map for storing number of vertices forming 0-simplexes for different input graphs.
	 */
	private Map<Integer, Integer> mGraphIdNumberOf0SimplexVertices;
	
	/**
	 * Estimated number of Vertices forming 0-simplexes
	 */
	private int estimatedVertices0Simplexes;
	
	/**
	 * This class is used to create 0-simplexes in mimic graph
	 */
	private Simplex0Distribution simplex0distObj;
	
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for 0-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs0Simplex = new ConcurrentHashMap<BitSet, IntSet>();
	
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> mTriColosCountsAvgProb;
	
	
	public GraphGenerationSimplexApproach2(int iNumberOfVertices,
			ColouredGraph[] inputGrphs, int iNumberOfThreads, long seed, int maximumIterationInput) {
		super();
		
		this.seed = seed;
		mRandom = new Random(this.seed);
		
		maximumIteration = maximumIterationInput;
		
		// Logic to check number of input graphs
		iNoOfVersions = 0;
		for (ColouredGraph tempGrph: inputGrphs) {
			if(tempGrph!=null) {
				iNoOfVersions = iNoOfVersions + 1;
			}
		}
		
		ColouredGraph[] origGrphs = new ColouredGraph[iNoOfVersions];
		int i = 0;
		for (ColouredGraph inputGraph: inputGrphs) {
			if (inputGrphs[i] != null) {
				origGrphs[i] = inputGraph.clone();
				i++;
			}		
		}
		
		//number of vertices
		mIDesiredNoOfVertices = iNumberOfVertices;
		this.seed = seed+1;
		// mimic grpah
		mMimicGraph = new ColouredGraph();
		
		//copy all colour palette to the mimic graph
		copyColourPalette(origGrphs, mMimicGraph);
		
		// below maps are required during graph optimization phase
		mReversedMapClassVertices = new HashMap<Integer, BitSet>(); // map for storing class vertices and color
		mMapEdgeColoursToConnectedVertices = new HashMap<BitSet, Map<Integer, IntSet>>(); // map for storing edge colors, tail ids and head ids
		
		// colour of rdf:type edge
		mRdfTypePropertyColour = mMimicGraph.getRDFTypePropertyColour();
		
		// color mapper analyzing all the graphs (required refine process)
		mColourMapper = new ColourMappingRules();
		mColourMapper.analyzeRules(origGrphs);
		
		mGraphEdges = new HashMap<>();
		computeTotalEdgesInGraphs(origGrphs);
		
		// removing rdf type edges. Note: For above method call, total number of edges are computed including rdf type edges. Thus, removing them after the above method invocation.
	
		// TODO: Move the metric call into Simplex2Analysis class and get the maps from that class
		callMetricToGetTriangleInformation(origGrphs);
		
		// Get all triangles found in input graphs. Note:- metric is invoked by above function call, thus set of colors for different triangle vertices are already computed.
		mTriangleColoursTriangleEdgeCounts = metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes();
		
		
		// Create HashSet of Triangle Colours. This set is used to randomly select an object of TriangleColours while generating mimic graph.
		createSetForTriangleColours();
		
		// Map object storing edge Ids found in triangles for every input graph.
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsTriangle;
		//Map object storing common edge Ids connected to vertices in triangles and other vertices that are not in triangles for every input graph.
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIds;
		
		//******************* Get stats for isolated triangles ********************//
		// Map object storing edge Ids found in isolated triangles for every input graph.
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsIsolatedTriangle;
		mGraphsEdgesIdsIsolatedTriangle = metric.getmGraphsEdgesIdsIsolatedTri();
		mColourMapperIsolatedTriangles = new ColourMappingRulesSimplexes(mGraphsEdgesIdsIsolatedTriangle); // initialize color mapper for isolated triangles
		mColourMapperIsolatedTriangles.analyzeRules(origGrphs);
		
		
		// Get edge maps from the metric object
		mGraphsEdgesIdsTriangle = metric.getmGraphsEdgesIdsTriangle();
		mGraphsCommonEdgesIds = metric.getmGraphsCommonEdgesIds();
	
		//TODO: Need to check for alternative instead of iterating over map and getting size of integer set.
		mGraphId1SimplexVertsConnToTri = new HashMap<Integer, Integer>(); // map storing number of vertices
		ObjectObjectOpenHashMap<Integer,IntSet> getmGraphsVertId1SimplexesConnToTri = metric.getmGraphsVertId1SimplexesConnToTri(); // get map storing vertex ids from metric object
		for (int graphId = 1; graphId <= iNoOfVersions; graphId++) {
			mGraphId1SimplexVertsConnToTri.put(graphId, getmGraphsVertId1SimplexesConnToTri.get(graphId).size()); //store number of 1-simplexes just connecting to triangles in the map
		}
		
		
		triangledistributionObject = new TriangleDistribution2(mTriangleColoursTriangleEdgeCounts, metric.getmIsolatedTriColoEdgesTriCountDistAvg(), iNoOfVersions, mIDesiredNoOfVertices, mRandom); // Note: iNoOfVersions : number of input graphs is computed when function estimateNoEdges is called.
		
		mTriColosCountsAvgProb = triangledistributionObject.getmTriangleColorsv1v2v3(); // get hash map for triangle vertex colors storing count of triangle distribution
		
		// color mapper analyzes colors of head, tail and edge and stores their mapping. The object of this class is planned to be used during graph generation.
		//mColourMapper = new ColourMappingRules();
		//mColourMapper.analyzeRules(origGrphs);
		//colour mapper for triangles
		mColourMapperTriangles = new ColourMappingRulesSimplexes(mGraphsEdgesIdsTriangle);
		mColourMapperTriangles.analyzeRules(origGrphs);
		//colour mapper for common edges
		mColourMapperCommonEdges = new ColourMappingRulesSimplexes(mGraphsCommonEdgesIds);
		mColourMapperCommonEdges.analyzeRules(origGrphs);
		
		// Initializing hash map used for storing triangle colors and list of vertices for them
		mTriangleColorsVertexIds = new ObjectObjectOpenHashMap<TriColos, List<IntSet>>();
		
		// initialize simplex2Analysis class and compute statistics for head color and tail color
		Simplex2Analysis simplex2Obj = new Simplex2Analysis();
		simplex2Obj.analyze(origGrphs, mGraphsCommonEdgesIds);
		
		simplex2distObj = new Simplex2Distribution(metric.getmColoCountVertConnectedToTriangle(), simplex2Obj.getmHeadColoCount(), simplex2Obj.getmHeadColoTailColoCount(), iNoOfVersions, mRandom);
		
		//ColouredGraph[] origGrphsCloned = origGrphs.clone();
		
		//***************** Calculations for 1-Simplexes connecting Triangles *************************//
		//Analysis, Distribution & mapper objects for 1-simplexes connecting triangles
		ObjectObjectOpenHashMap<Integer,IntSet> mGraphsEdgesIdsConnectTriangles = metric.getmGraphsEdgesIdsConnectTriangles(); // get map storing edge ids for 1-simplexes that link triangles
		
		// analyze edge ids found for this case and create distributions found for head and head-tail colors found across different input graphs
		Simplex2Analysis simplex1ConnTriObj = new Simplex2Analysis();
		simplex1ConnTriObj.analyze(origGrphs, mGraphsEdgesIdsConnectTriangles);
		
		// create a distributions for found head and head-tail colors, and average over it
		simplex1ConnTriDist = new Simplex1Distribution(simplex1ConnTriObj.getmHeadColoCount(), simplex1ConnTriObj.getmHeadColoTailColoCount(), iNoOfVersions, mRandom);
		
		// color mapper for collecting triples
		mColourMapper1SimplexesConnTri = new ColourMappingRulesSimplexes(mGraphsEdgesIdsConnectTriangles);
		mColourMapper1SimplexesConnTri.analyzeRules(origGrphs);
		
		//**************** Calculations for Self Loops ******************************//
		
		// Isolated triangles
		SelfLoopInSimplexAnalysis selfIsoTriAnalysis = new SelfLoopInSimplexAnalysis();
		selfIsoTriAnalysis.analyze(origGrphs, metric.getmGraphsVertIdsIsolatedTri()); // analyze graph to find self loops
		selfIsoTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions); // compute estimated number of edges for output graph
		estEdgesSelfLoopIsoTri = selfIsoTriAnalysis.getEstimatedNoEdges();
		
		mColourMapperSelfLoopIsoTri = new ColourMappingRulesSimplexes(selfIsoTriAnalysis.getmGraphIdEdgeIdsForSelfLoop()); // Define mapping of colors for triples
		mColourMapperSelfLoopIsoTri.analyzeRules(origGrphs);
		
		distColoProposerSelfLoopIsoTri = selfIsoTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom); // create probability distribution for the vertex color
		
		// Connected triangles
		SelfLoopInSimplexAnalysis selfConnTriAnalysis = new SelfLoopInSimplexAnalysis();
		selfConnTriAnalysis.analyze(origGrphs, metric.getmGraphsVertIdsConnectTriangles());
		selfConnTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions);
		estEdgesSelfLoopConnTri = selfConnTriAnalysis.getEstimatedNoEdges();
		
		mColourMapperSelfLoopConnTri = new ColourMappingRulesSimplexes(selfConnTriAnalysis.getmGraphIdEdgeIdsForSelfLoop());
		mColourMapperSelfLoopConnTri.analyzeRules(origGrphs);
		
		distColoProposerSelfLoopConnTri = selfConnTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom);
		
		// 1-simplexes only connected triangles
		SelfLoopInSimplexAnalysis self1SimplexesConnToTriAnalysis = new SelfLoopInSimplexAnalysis();
		self1SimplexesConnToTriAnalysis.analyze(origGrphs, metric.getmGraphsVertId1SimplexesConnToTri());
		self1SimplexesConnToTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions);
		estEdgesSelfLoop1SimplexConnToTri = self1SimplexesConnToTriAnalysis.getEstimatedNoEdges();
		
		mColourMapperSelfLoop1SimplexConnToTri = new ColourMappingRulesSimplexes(self1SimplexesConnToTriAnalysis.getmGraphIdEdgeIdsForSelfLoop());
		mColourMapperSelfLoop1SimplexConnToTri.analyzeRules(origGrphs);
		
		distColoProposerSelfLoop1SimplexConnToTri = self1SimplexesConnToTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom);
		
		
		//************* Calculations for isolated 1-Simplexes and self loops ********************************//
		
		removeTypeEdgesFromGraphs(origGrphs); // commenting removal of edges since node iterator creates a new graph to compute triangles and the computed edge Ids don't match with the original graph
		// Initially, tried not to remove the RDF type edges and instead use RDF type edges to identify class nodes. 
		//Seems to be some issue with this approach. Thus, removing type edges and then computing 1- and 0-Simplexes
		
		// Initialization of analysis object
		Simplex1Analysis simplex1Obj = new Simplex1Analysis();
		simplex1Obj.analyze(origGrphs);
		
		// Get maps storing different statistics related to 1-simplexes
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsFor1Simplex = simplex1Obj.getmGraphIdEdgeIdsFor1Simplex();
		mGraphIdNumberOf1SimplexEdges = simplex1Obj.getmGraphIdNumberOf1SimplexEdges();
		mGraphIdNumberOf1SimplexVertices = simplex1Obj.getmGraphIdNumberOf1SimplexVertices();
		
		// Define colour mapper for 1-simplexes
		mColourMapper1Simplexes = new ColourMappingRulesSimplexes(mGraphIdEdgeIdsFor1Simplex);
		mColourMapper1Simplexes.analyzeRules(origGrphs);
		
		// initialize simplex distribution object
		simplex1distObj = new Simplex1Distribution(simplex1Obj.getmHeadColoCount1Simplex(), simplex1Obj.getmHeadColoTailColoCount(), iNoOfVersions, mRandom);
		
		// ******* Computations related to isolated self loops (1-simplexes with same head and tail)
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsIsoSelfLoops = simplex1Obj.getmGraphIdEdgeIdsForSelfLoop();
		mGraphIdNumIsoSelfLoopEdges = simplex1Obj.getmGraphIdNumIsolatedSelfLoopEdges();
		mGraphIdNumIsoSelfLoopVertices = simplex1Obj.getmGraphIdNumIsolatedSelfLoopVertices();
		
		mColourMapperIsoSelfLoop = new ColourMappingRulesSimplexes(mGraphIdEdgeIdsIsoSelfLoops);
		mColourMapperIsoSelfLoop.analyzeRules(origGrphs);
		
		simplex1distObj.createIsoSelfLoopColoProposer(simplex1Obj.getmColoCountSelfLoop());
		//*************************************************************************************************************//
		
		// ******* Computations related to isolated self loops (1-simplexes with same head and tail)
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsSelfLoopsIn1Simplex = simplex1Obj.getmGraphIdEdgeIdsForSelfLoopIn1Simplex();
		mGraphIdNumSelfLoopIn1SimplexEdges = simplex1Obj.getmGraphIdNumSelfLoopIn1SimplexEdges();
		
		mColourMapperSelfLoopIn1Simplex = new ColourMappingRulesSimplexes(mGraphIdEdgeIdsSelfLoopsIn1Simplex);
		mColourMapperSelfLoopIn1Simplex.analyzeRules(origGrphs);
				
		simplex1distObj.createSelfLoopIn1SimplexColoProposer(simplex1Obj.getmColoCountSelfLoopIn1Simplex());
		//*************************************************************************************************************//
		
		
		//************ Calculations for 0-Simplexes *********************************//
		Simplex0Analysis simplex0Obj = new Simplex0Analysis();
		simplex0Obj.analyze(origGrphs);
		
		mGraphIdNumberOf0SimplexVertices = simplex0Obj.getmGraphIdNumberOf0SimplexVertices();
		
		simplex0distObj = new Simplex0Distribution(simplex0Obj.getmColoCount0Simplex(), iNoOfVersions, mRandom);
		
		//super(iNumberOfVertices, origGrphs, iNumberOfThreads, seed);
		//LOGGER.info("Graph Initialization completed");
		
		//************* Calculations for connected 1-Simplexes ****************************//
		//Collecting triples for them using edge ids of different input graphs
		
		// initialize selfLoopsInGraphs class and find all edges for self loops to determine edge Ids of connected 1-simplexes
		SelfLoopsInGraphs selfloopsObj = new SelfLoopsInGraphs();
		selfloopsObj.analyze(origGrphs);
		
		
		// map to store edge ids for different input graphs
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphIdEdgeIdsConnected1Simplex = simplex1Obj.findEdgeIdsForConnected1Simplexes(origGrphs, mGraphsEdgesIdsTriangle, mGraphsCommonEdgesIds, mGraphIdEdgeIdsFor1Simplex, selfloopsObj.getmGraphIdEdgeIdsForSelfLoop());
		mColourMapperConnected1Simplexes = new ColourMappingRulesSimplexes(mGraphIdEdgeIdsConnected1Simplex);
		mColourMapperConnected1Simplexes.analyzeRules(origGrphs);
		
		connectedSimplex1distObj = new Simplex1Distribution(simplex1Obj.getmVertColoCountConnected1Simplex(), simplex1Obj.getmHeadColoTailColoCountConnected(), iNoOfVersions, mRandom);
		
		// Self loop analysis for connected 1-Simplexes
		SelfLoopInSimplexAnalysis self1SimplexesConn1Simplexes = new SelfLoopInSimplexAnalysis();
		self1SimplexesConn1Simplexes.analyze(origGrphs, simplex1Obj.getmGraphsVertIdConn1Simplexes());
		self1SimplexesConn1Simplexes.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions);
		estEdgesSelfLoopConn1Simplexes = self1SimplexesConn1Simplexes.getEstimatedNoEdges();
		
		mColourMapperSelfLoopConn1Simplexes = new ColourMappingRulesSimplexes(self1SimplexesConn1Simplexes.getmGraphIdEdgeIdsForSelfLoop());
		mColourMapperSelfLoopConn1Simplexes.analyzeRules(origGrphs);
		
		distColoProposerSelfLoopConn1Simplexes = self1SimplexesConn1Simplexes.createVertColoProposer(iNoOfVersions, mRandom);
		
		// compute estimated edges
		mIDesiredNoOfEdges = estimateNoEdgesSimplexes(origGrphs, mIDesiredNoOfVertices);
		
		
	}
	
	private void callMetricToGetTriangleInformation(ColouredGraph[] origGrphs) {
		// Initialize metric, which will collect triangles found in Input graphs.
		metric = new NodeIteratorMetric2();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
	}
	
	private void createSetForTriangleColours() {
		//TODO: Move this method to Triangle Distribution class and Triangle should be proposed from that class
		// Initialize set variable
		setAllTriangleColours = new HashSet<TriColos>();
		
		Object[] keysTriangleColours = mTriangleColoursTriangleEdgeCounts.keys;
		for(int i = 0; i < keysTriangleColours.length ; i++) {
			if(mTriangleColoursTriangleEdgeCounts.allocated[i]) {
				TriColos triangleColorObj = (TriColos) keysTriangleColours[i];
				setAllTriangleColours.add(triangleColorObj);
			}
		}
	}
	

	protected int estimateNoEdgesSimplexes(ColouredGraph[] origGrphs, int noVertices) {
		LOGGER.info("Estimate the number of edges in the new graph.");
		
		
		
		int estimatedEdges = 0;
		
		// initialize estimated edges for different cases
		estimatedEdgesTriangle = 0;
		estimatedEdgesCommon = 0;
		
		estimatedEdges1Simplexes = 0; // 1-simplex
		estimatedEdges1SimplexesConnect2Simplexes = 0;
		estimatedEdgesIsolatedTriangle = 0;
		
		estimatedEdgesIsoSelfLoop = 0; // isolated self loop (1-simplex with same head and tail)
		
		estimatedEdgesSelfLoopIn1Simplex = 0; //self loop in isolated 1-simplexes
		
		// initialize estimated vertices for different cases
		estimatedVerticesTriangle = 0;
		estimatedVerticesCommon = 0;
		estimatedVertices1Simplexes = 0;
		estimatedVertices0Simplexes = 0;
		estimatedVerticesIsolatedTriangle = 0;
		
		estimatedVerticesIsoSelfLoop = 0; // isolated self loop
		
		estVerts1SimplexesConntoTri = 0; // number of vertices for 1-simplexes only connected to triangles
		
		if (origGrphs != null && origGrphs.length > 0) {
		
			double noEdges = 0;
			
			// temporary variable to track graph number
			int graphId = 1;
			
			// variables to track edge density for different cases
			double noEdgesTriangles = 0;
			double noEdgesCommon = 0;
			
			//double noTypeEdgesNotTriangle = 0;
			double noEdges1Simplexes = 0;
			double noEdges1SimplexConn2Simplex = 0;
			double noEdgesIsoTriangles = 0;
			
			double noEdgesIsoSelfLoop = 0; // isolated self loop
			
			double noEdgesSelfLoopIso1Simplex = 0; // self loop in isolated 1-simplex
			
			// variables to track vertices information
			double noVerticesTriangles = 0;
			double noVerticesCommon = 0;
			double noVertices1Simplexes = 0;
			double noVertices0Simplexes = 0;
			double noVerticesIsolatedTriangles = 0;
			
			double noVerticesIsoSelfLoop = 0; // isolated self loop
			
			double noVerts1SimplexesConnToTri = 0;
			
			
			for (ColouredGraph graph : origGrphs) {
				
				if (graph!= null) {
				
					// compute triangles information for input graph
					//metric.computeTriangles(graph); // commenting this call, metric is invoked by separate method
					//LOGGER.info("Edge stats computed for graph " + graphId);
					
					//*******************Computation for edges**************************//
					
					// computation for total number of edges
					int iNoEdges = mGraphEdges.get(graphId); // get total number of edges including the rdf type edges//graph.getEdges().size();
					int iNoVertices = graph.getVertices().size();
					noEdges += iNoEdges / (iNoVertices * 1.0);
					
					// get hashmap consisting of computed statistics
					Map<Integer, List<Integer>> mstatsVertEdges = metric.getMstatsVertEdges();
					List<Integer> statsList = mstatsVertEdges.get(graphId);
					
					int iNoEdgesTriangles = statsList.get(1); // get number of edges for triangle
					noEdgesTriangles += iNoEdgesTriangles / (iNoVertices * 1.0);
					//LOGGER.info("Number of edges in triangle: " + iNoEdgesTriangles);
					//LOGGER.info("Edge density [Number of edges in triangle]: " + noEdgesTriangles);
					
					int iNoEdgesCommon = statsList.get(3); // get number of edges connecting vertices present in triangle and vertices not in triangle
					noEdgesCommon += iNoEdgesCommon / (iNoVertices * 1.0);
					//LOGGER.info("Number of common edges: " + iNoEdgesCommon);
					//LOGGER.info("Edge density [Number of common edges]: " + noEdgesCommon);

					int iNoEdges1Simplexes = mGraphIdNumberOf1SimplexEdges.get(graphId); 
					noEdges1Simplexes +=iNoEdges1Simplexes / (iNoVertices * 1.0);
					//LOGGER.info("Number of edges for 1-simplexes: " + iNoEdges1Simplexes);
					//LOGGER.info("Edge density [1-simplexes]: " + noEdges1Simplexes);
					
					int iNoEdges1SimplexConn2Simplex = statsList.get(4);
					noEdges1SimplexConn2Simplex += iNoEdges1SimplexConn2Simplex/ (iNoVertices * 1.0);
					//LOGGER.info("Number of edges connecting triangles: " + iNoEdges1SimplexConn2Simplex);
					//LOGGER.info("Edge density [Number of edges connecting triangles]: " + noEdges1SimplexConn2Simplex);
					
					int iNoEdgesIsolatedTriangles = statsList.get(6); // get number of edges for isolated triangles
					noEdgesIsoTriangles += iNoEdgesIsolatedTriangles / (iNoVertices * 1.0);
					//LOGGER.info("Number of edges in isolated triangles: " + iNoEdgesIsolatedTriangles);
					//LOGGER.info("Edge density [Number of edges in Isolated triangle]: " + noEdgesIsoTriangles);
					
					/*
					 * Not computing type information. Thus, commenting below statements.
					int iNoTypeEdgesNotTriangle = statsList.get(6); // get number of type edges not part of triangle
					noTypeEdgesNotTriangle += iNoTypeEdgesNotTriangle / (iNoVertices * 1.0);
					LOGGER.debug("Number of type edges not in triangle: " + iNoTypeEdgesNotTriangle);
					LOGGER.debug("Edge density [Number of type edges not in triangle]: " + noTypeEdgesNotTriangle);
					*/
					
					//isolated selfloop edges
					int iNoEdgesIsoSelfLoop = mGraphIdNumIsoSelfLoopEdges.get(graphId); 
					noEdgesIsoSelfLoop +=iNoEdgesIsoSelfLoop / (iNoVertices * 1.0);
					
					//self loop edges in isolated 1-simplexes
					int iNoEdgesSelfLoopInIso1Simplex = mGraphIdNumSelfLoopIn1SimplexEdges.get(graphId);
					noEdgesSelfLoopIso1Simplex += iNoEdgesSelfLoopInIso1Simplex / (iNoVertices * 1.0);
					
					//********************Computation for vertices**************************//
					
					int iNoVerticesTriangles = statsList.get(0); // get number of vertices for triangle
					noVerticesTriangles += iNoVerticesTriangles / (iNoVertices * 1.0);
					//LOGGER.info("Percentage of vertices in triangle: " + noVerticesTriangles);
					
					int iNoVerticesCommon = statsList.get(2);
					noVerticesCommon += iNoVerticesCommon / (iNoVertices * 1.0);
					//LOGGER.info("Percentage of common vertices: " + noVerticesCommon);
					
					int iNoVertices1Simplexes = mGraphIdNumberOf1SimplexVertices.get(graphId);
					noVertices1Simplexes += iNoVertices1Simplexes / (iNoVertices * 1.0);
					//LOGGER.info("Percentage of vertices for 1-simplexes: " + noVertices1Simplexes);
					
					int iNoVertices0Simplexes = mGraphIdNumberOf0SimplexVertices.get(graphId);
					noVertices0Simplexes += iNoVertices0Simplexes / (iNoVertices * 1.0);
					//LOGGER.info("Percentage of vertices for 0-simplexes: " + noVertices0Simplexes);
					
					int iNoVerticesIsolatedTriangles = statsList.get(5); // get number of vertices for isolated triangles
					noVerticesIsolatedTriangles += iNoVerticesIsolatedTriangles / (iNoVertices * 1.0);
					//LOGGER.info("Percentage of vertices for isolated triangles: " + noVerticesIsolatedTriangles);
					
					//isolated self loop vertices
					int iNoVerticesIsoSelfLoop = mGraphIdNumIsoSelfLoopVertices.get(graphId);
					noVerticesIsoSelfLoop += iNoVerticesIsoSelfLoop / (iNoVertices * 1.0);
					
					//number of vertices connected to triangles
					int iNoVerts1SimplexesConnToTri = mGraphId1SimplexVertsConnToTri.get(graphId);
					noVerts1SimplexesConnToTri += iNoVerts1SimplexesConnToTri / (iNoVertices * 1.0); 
					
					graphId++;
				}
			}
			
			//*******************Estimating edges**************************//
			
			// computation for total number of edges
			noEdges *= noVertices;
			noEdges /= iNoOfVersions;
			estimatedEdges = (int) Math.round(noEdges);
			LOGGER.info("Total number of estimated edges: " + estimatedEdges);
			
			//computation of edges for different cases
			noEdgesTriangles *= noVertices;
			noEdgesTriangles /= iNoOfVersions;
			estimatedEdgesTriangle = (int) Math.round(noEdgesTriangles);
			LOGGER.info("Estimated number of edges in triangle: " + estimatedEdgesTriangle);
			
			noEdgesIsoTriangles *= noVertices;
			noEdgesIsoTriangles /= iNoOfVersions;
			estimatedEdgesIsolatedTriangle = (int) Math.round(noEdgesIsoTriangles);
			LOGGER.info("Estimated number of edges in isolated triangle: " + estimatedEdgesIsolatedTriangle);
			
			noEdgesCommon *= noVertices;
			noEdgesCommon /= iNoOfVersions;
			estimatedEdgesCommon = (int) Math.round(noEdgesCommon);
			LOGGER.info("Estimated number of common edges: " + estimatedEdgesCommon);
			
			noEdges1Simplexes *= noVertices;
			noEdges1Simplexes /= iNoOfVersions;
			estimatedEdges1Simplexes = (int) Math.round(noEdges1Simplexes);
			LOGGER.info("Estimated number of edges [1-simplexes]: " + estimatedEdges1Simplexes);
			
			noEdges1SimplexConn2Simplex *= noVertices;
			noEdges1SimplexConn2Simplex /= iNoOfVersions;
			estimatedEdges1SimplexesConnect2Simplexes = (int) Math.round(noEdges1SimplexConn2Simplex);
			LOGGER.info("Estimated number of edges connecting triangles: " + estimatedEdges1SimplexesConnect2Simplexes);
			
			/*
			 * Not computing type information. Thus, commenting below statements.
			noTypeEdgesNotTriangle *= noVertices;
			noTypeEdgesNotTriangle /= iNoOfVersions;
			estimatedTypeEdgesNotTriangle = (int) Math.round(noTypeEdgesNotTriangle);
			LOGGER.debug("Estimated number of type edges not in triangle: " + estimatedTypeEdgesNotTriangle);
			*/
			
			//isolated self loop edges
			noEdgesIsoSelfLoop *= noVertices;
			noEdgesIsoSelfLoop /= iNoOfVersions;
			estimatedEdgesIsoSelfLoop = (int) Math.round(noEdgesIsoSelfLoop);
			LOGGER.info("Estimated number of edges for isolated self loop: " + estimatedEdgesIsoSelfLoop);
			
			//self loop in isolated 1-simplexes
			noEdgesSelfLoopIso1Simplex *= noVertices;
			noEdgesSelfLoopIso1Simplex /= iNoOfVersions;
			estimatedEdgesSelfLoopIn1Simplex = (int) Math.round(noEdgesSelfLoopIso1Simplex);
			LOGGER.info("Estimated number of edges for self loop in isolated 1-simplexes: " + estimatedEdgesSelfLoopIn1Simplex);
			
			LOGGER.warn("Estimated the number of edges in the new graph is " + estimatedEdges);
			
			
			//*******************Estimating vertices**************************//
			
			// computation of vertices for different cases
			noVerticesTriangles *= noVertices;
			noVerticesTriangles /= iNoOfVersions;
			estimatedVerticesTriangle = (int) Math.round(noVerticesTriangles);
			LOGGER.info("Estimated number of vertices forming triangle: " + estimatedVerticesTriangle);
			
			noVerticesIsolatedTriangles *= noVertices;
			noVerticesIsolatedTriangles /= iNoOfVersions;
			estimatedVerticesIsolatedTriangle = (int) Math.round(noVerticesIsolatedTriangles);
			LOGGER.info("Estimated number of vertices forming isolated triangle: " + estimatedVerticesIsolatedTriangle);
			
			noVerticesCommon *= noVertices;
			noVerticesCommon /= iNoOfVersions;
			estimatedVerticesCommon = (int) Math.round(noVerticesCommon);
			LOGGER.info("Estimated number of common vertices: " + estimatedVerticesCommon);
			
			noVertices1Simplexes *= noVertices;
			noVertices1Simplexes /= iNoOfVersions;
			estimatedVertices1Simplexes = (int) Math.round(noVertices1Simplexes);
			LOGGER.info("Estimated number of vertices [1-simplexes]: " + estimatedVertices1Simplexes);
			
			noVertices0Simplexes *= noVertices;
			noVertices0Simplexes /= iNoOfVersions;
			estimatedVertices0Simplexes = (int) Math.round(noVertices0Simplexes);
			LOGGER.info("Estimated number of vertices [0-simplexes]: " + estimatedVertices0Simplexes);
			
			//isolated self loop vertices
			noVerticesIsoSelfLoop *= noVertices;
			noVerticesIsoSelfLoop /= iNoOfVersions;
			estimatedVerticesIsoSelfLoop = (int) Math.round(noVerticesIsoSelfLoop);
			LOGGER.info("Estimated number of vertices for isolated self loop: " + estimatedVerticesIsoSelfLoop);
			
			// number of vertices for 1-simplexes only connected to triangles
			noVerts1SimplexesConnToTri *= noVertices;
			noVerts1SimplexesConnToTri /= iNoOfVersions;
			estVerts1SimplexesConntoTri = (int) Math.round(noVerts1SimplexesConnToTri);
			LOGGER.info("Estimated number of vertices for 1-simplexes only connected to triangles: " + estVerts1SimplexesConntoTri);
			
			
		} else {
			LOGGER.warn("The array of original graphs is empty!");
		}
		return estimatedEdges;
	}
	
	@Override
	public ColouredGraph generateGraph(){
		
		//*************************************** 2-simplex creation (that could be connected to each other) ************************************************//
		LOGGER.info("Case 1: Model higher dimensional simplexes with 2-simplexes");
		LOGGER.info("Estimated Edges: " + estimatedEdgesTriangle);
		LOGGER.info("Estimated Vertices: " + estimatedVerticesTriangle);
		// get random triangle
		TriColos initialRandomTriangle = getRandomTriangle();
		
		// Variable to track number of edges added to mimic graph in previous iteration.
		// Note: This variable is used to stop the iteration if no new edges could be added to the mimic graph after trying for predefined number of iterations
		int oldEdgesCountGraph = mMimicGraph.getEdges().size();
		int numOfIterationAddingEdgesToGraph = 0;
		
		if (initialRandomTriangle!=null) { // TODO: Add condition for estimatedEdgesTriangle should be >= 3 and estimatedVerticesTriangle >= 3
			//TODO: Consider estimated no. of edges and vertices
			
			// Update the count of triangles in the map
			double[] arrTriProbCount = mTriColosCountsAvgProb.get(initialRandomTriangle.getA()).get(initialRandomTriangle.getB()).get(initialRandomTriangle.getC());
			arrTriProbCount[3] = arrTriProbCount[3] - 1; // Triangle count is stored at first index, updating its count.
			// Note: As it is the first triangle that is added not validating if we are allowed to add a triangle or not.
			
			// Variables to track number of edges and vertices added in triangle
			int actualEdgesInTriangles = 0;
			int actualVerticesInTriangles = 0;
			
			// Variable to track edges that cannot form triangle
			IntSet edgesNotFormingTriangle = new DefaultIntSet(Constants.DEFAULT_SIZE);
			
			// Variable to track set of triangle added to the mimic graph (i.e. set of Colors of the vertices forming the triangle)
			Set<TriColos> setTriangleColorsMimicGraph = new HashSet<TriColos>();
			
			// add the selected random triangle to mimic graph			
			addTriangleToMimicGraph( initialRandomTriangle, mColourMapperTriangles, mMapColourToVertexIDs2Simplex, mMapColourToEdgeIDs2Simplex, mTriangleColorsVertexIds);
			
			//increment no of vertices in triangle
			actualVerticesInTriangles = actualVerticesInTriangles + 3;
			
			//increment no. of edges in triangle
			actualEdgesInTriangles = actualEdgesInTriangles + 3;
			
			// Add the triangle colors to set variable
			setTriangleColorsMimicGraph.add(initialRandomTriangle);
			
			// 
			while(actualEdgesInTriangles< estimatedEdgesTriangle) {
				
				if(actualVerticesInTriangles < estimatedVerticesTriangle) {
					//If we can add more triangles when we are allowed to include additional vertices otherwise edges need to be added to existing triangles
				
					// get all edges of the mimic graph
					IntSet edgesMimicGraph = mMimicGraph.getEdges(); //TODO: Instead of getting all edges from the mimic graph. All the added edges can be stored in an array and selected randomly?
					edgesMimicGraph = IntSetUtil.difference(edgesMimicGraph, edgesNotFormingTriangle);
					
					if (edgesMimicGraph.size() != 0) {
						// Continue to randomly select an edge and grow the graph by adding triangle only if candidate edges are found. These candidate edges will be evaluated to check if triangles can be created for them.
			
						//TODO: The randomly selected edge should not be a self loop.
						// select an edge at random from the mimic graph
						int randomEdgeID = edgesMimicGraph.toArray(new Integer[edgesMimicGraph.size()])[mRandom.nextInt(edgesMimicGraph.size())];
						IntSet verticesIncidentToEdge = mMimicGraph.getVerticesIncidentToEdge(randomEdgeID);
				
						// Find the vertices for the randomly selected edge
						int tempIndex = 0;
						int selectedVertex1 = -1, selectedVertex2 = -1;
						for (int vertexIDTemp: verticesIncidentToEdge) {
							if (tempIndex == 0) {
								selectedVertex1 = vertexIDTemp;
								tempIndex++;
							} else {
								selectedVertex2 = vertexIDTemp;
							}
						}
				
						// Get colors for the selected vertices
						//TODO: Instead of getting the color of vertex ID from the graph. A Map can be used to store with the vertex ID as the key and color as the value? Note: We already have map for storing colors (key) and set of vertex IDs (value).
						if ((selectedVertex1 == -1) || (selectedVertex2 == -1)) {
							throw new RuntimeException("Vertices not selected correctly for adding triangles. Vertex ID 1: " + selectedVertex1 + ", Vertex ID 2: " + selectedVertex2 + ". Edge ID: " + randomEdgeID);
						}
						
						BitSet selectedVertex1Colo = mMimicGraph.getVertexColour(selectedVertex1);
						BitSet selectedVertex2Colo = mMimicGraph.getVertexColour(selectedVertex2);
				
						//Get the color for the third vertex
						BitSet proposedVertex3Colo = triangledistributionObject.proposeVertexColorForVertex3(selectedVertex1Colo, selectedVertex2Colo);
				
						// Add new Triangle for the selected vertices
						// TODO: Third vertex color might not be exist for the selected vertex colors. Create a logic to handle such a scenario. Or if the triangle is already present[Done]
						
						// boolean variable to track if new edge are added
						boolean newEdgesNotAddedToTriangle = true;
						
						if (proposedVertex3Colo != null) {
							// If third vertex color is proposed, create a triangle with it
							
							// create a temporary triangle colors object
							TriColos newPossibleTriangle = new TriColos( selectedVertex1Colo, selectedVertex2Colo, proposedVertex3Colo);
							//System.out.println(newPossibleTriangle.getA());
							//System.out.println(newPossibleTriangle.getB());
							//System.out.println(newPossibleTriangle.getC());
							
							// get triangle count
							double[] arrNewPossTriProbCount = mTriColosCountsAvgProb.get(newPossibleTriangle.getA()).get(newPossibleTriangle.getB()).get(newPossibleTriangle.getC());// get count of triangle for the proposed new triangle
							
							// temporary variable to track count of loops
							int numOfLoopsTri = 0;
							
							// try to propose a color for third vertex multiple times if it is not possible to create a triangle
							while ( (arrNewPossTriProbCount[3] < 1)  && (numOfLoopsTri < 100)) { // trying to create a new triangle 100 times
								proposedVertex3Colo = triangledistributionObject.proposeVertexColorForVertex3(selectedVertex1Colo, selectedVertex2Colo);
								newPossibleTriangle = new TriColos( selectedVertex1Colo, selectedVertex2Colo, proposedVertex3Colo);
								arrNewPossTriProbCount = mTriColosCountsAvgProb.get(newPossibleTriangle.getA()).get(newPossibleTriangle.getB()).get(newPossibleTriangle.getC());// get count of triangle for the proposed new triangle
								numOfLoopsTri++;
							}
							
							//if (( !setTrianglesForEdge.contains( newPossibleTriangle ) )) {
							if (arrNewPossTriProbCount[3] >= 1) {
								//Update the count of triangle since a triangle will be created
								arrNewPossTriProbCount[3] = arrNewPossTriProbCount[3] - 1;
					
								// create vertex for the proposed color
								int proposedVertId = addVertexToMimicGraph(proposedVertex3Colo, mMapColourToVertexIDs2Simplex);
						
								// add edges among selected vertices and proposed color
								//Note: Ideally properties should exist among them. since they were also forming a triangle in input graphs
								addEdgeTriangle(selectedVertex1Colo, proposedVertex3Colo, selectedVertex1, proposedVertId, mMapColourToEdgeIDs2Simplex, mColourMapperTriangles, newPossibleTriangle);
								addEdgeTriangle(selectedVertex2Colo, proposedVertex3Colo, selectedVertex2, proposedVertId, mMapColourToEdgeIDs2Simplex, mColourMapperTriangles, newPossibleTriangle);
						
								// increment number of vertices and edges added in the mimic graph for triangles
								actualVerticesInTriangles = actualVerticesInTriangles + 1;
								actualEdgesInTriangles = actualEdgesInTriangles + 2;
								
								//Add the created triangle Colors to set
								setTriangleColorsMimicGraph.add(newPossibleTriangle);
								
								
								// Track the triangle colors along with vertex ids
								updateMapTriangleColorsVertices(selectedVertex1, selectedVertex2, proposedVertId, newPossibleTriangle, mTriangleColorsVertexIds);
								
								//update the boolean variable
								newEdgesNotAddedToTriangle = false;
							} 
						} 
						
						if (newEdgesNotAddedToTriangle){
							// Logic if no third vertex color could be proposed
							// Don't consider the randomly selected edge, since it is not able to form a triangle
							edgesNotFormingTriangle.add(randomEdgeID);
						}
						
					} // end if condition - check if triangles can be added to the edges
					else {
						LOGGER.info("Growing 2-simplexes not possible.... Proposing new 2-simplex");
						// If no candidate edges exist then new random triangle should be added to the mimic graph
						TriColos randomTriangle = getRandomTriangle();
						
						// get triangle count
						double[] arrNewTriProbCount = mTriColosCountsAvgProb.get(randomTriangle.getA()).get(randomTriangle.getB()).get(randomTriangle.getC());// get count of triangle for the proposed new triangle
						
						// variable to track number of times a random triangle was selected
						int numOfIterationRandomTri = 1;
						
						// check if it is possible to add new triangle
						while ((arrNewTriProbCount[3] < 1) && (numOfIterationRandomTri < 500)) { // discontinue after trying 500 times
							randomTriangle = getRandomTriangle();
							arrNewTriProbCount = mTriColosCountsAvgProb.get(randomTriangle.getA()).get(randomTriangle.getB()).get(randomTriangle.getC());// get count of triangle for the proposed new triangle
							numOfIterationRandomTri++;
						}
						
						if (arrNewTriProbCount[1] > 1) {
							// Update the triangle count
							arrNewTriProbCount[1] = arrNewTriProbCount[1] - 1;
							
							// Add the triangle to mimic graph
							addTriangleToMimicGraph(randomTriangle, mColourMapperTriangles, mMapColourToVertexIDs2Simplex, mMapColourToEdgeIDs2Simplex, mTriangleColorsVertexIds);
							
							//increment no of vertices in triangle
							actualVerticesInTriangles = actualVerticesInTriangles + 3;
							
							//increment no. of edges in triangle
							actualEdgesInTriangles = actualEdgesInTriangles + 3;
							
							// Add the triangle colors to set variable
							setTriangleColorsMimicGraph.add(new TriColos(randomTriangle.getA(), randomTriangle.getB(), randomTriangle.getC()));
						} else {
							break; // terminate while condition if it is not possible to add random triangle
						}
						
					}
				} else {
					break; // Cannot add more vertices 
				}
			} // end while condition checking if actual number of edges is less than estimated number of edges
			LOGGER.info("Growing 2-simplexes phase completed");
			LOGGER.info("Added Edges: " + actualEdgesInTriangles);
			LOGGER.info("Added Vertices: " + actualVerticesInTriangles);
			
			LOGGER.info("Adding additional Edges to created 2-simplexes");
			
			
			while (actualEdgesInTriangles< estimatedEdgesTriangle) { // Case: Triangles cannot be added to the mimic graph but edges can be added to existing triangles
					//System.out.println("Number of edges in the mimic graph: " + mMimicGraph.getEdges().size());
					//System.out.println("Number of vertices in the mimic graph: " + mMimicGraph.getVertices().size());
					
					if (oldEdgesCountGraph == actualEdgesInTriangles) {
						numOfIterationAddingEdgesToGraph++;
						//System.out.println("Iteration: " + numOfIterationAddingEdgesToGraph);
					} else {
						oldEdgesCountGraph = actualEdgesInTriangles;
						numOfIterationAddingEdgesToGraph = 0;
					}
					
					
					if (setTriangleColorsMimicGraph.size() != 0) {
					
					// Logic for adding edges to existing triangles
					TriColos proposeTriangleToAddEdge = triangledistributionObject.proposeTriangleToAddEdge(setTriangleColorsMimicGraph);
					
					// Best case: A triangle is returned
					List<IntSet> selectedTrianglesList = mTriangleColorsVertexIds.get(proposeTriangleToAddEdge);
					
					// randomly selecting one of these triangles
					IntSet selectedVertices = selectedTrianglesList.toArray(new IntSet[selectedTrianglesList.size()])[mRandom.nextInt(selectedTrianglesList.size())];
					
					// temporary variable to track if edge was added to existing triangle
					boolean edgeNotAddedToExistingTriangle = true;
					
					// Considering different vertex pairs to add an edge
					
					//Convert vertices to Array
					Integer[] vertexIDExistingTriangle = selectedVertices.toArray(new Integer[selectedVertices.size()]);
					
					// creating different pairs of combinations for 3 vertices of a triangle
					List<List<Integer>> differentPairsOfVertices = new ArrayList<List<Integer>>();
					differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[1]));
					differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[1], vertexIDExistingTriangle[2]));
					differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[2]));
					
					for(List<Integer> pairOfVertices: differentPairsOfVertices) {
					
						// Get vertex colours for a pair
						BitSet existingVertexColo1 = mMimicGraph.getVertexColour(pairOfVertices.get(0));
						BitSet existingVertexColo2 = mMimicGraph.getVertexColour(pairOfVertices.get(1));
						
						boolean edgeAdded = addEdgeInAnyDirectionWithDuplicateCheck(existingVertexColo1, existingVertexColo2, pairOfVertices.get(0), pairOfVertices.get(1), mMapColourToEdgeIDs2Simplex, mColourMapperTriangles);
						
						if (edgeAdded) {
							//increment no. of edges in triangle
							actualEdgesInTriangles = actualEdgesInTriangles + 1;
							
							//update boolean tracker variable
							edgeNotAddedToExistingTriangle = false;
							
							// break iterating over pair of vertices, since an edge is found
							break;
						}
						
					}//end iterating over vertex pairs
					
					// commenting below condition as the mimic graph could have multiple triangles with same vertex colors and below condition could lead to ignoring such triangles
					//if (edgeNotAddedToExistingTriangle) {
						// edge could not be added to the selected triangle
						// removing the triangle from consideration
						//proposeTriangleToAddEdge
						//setTriangleColorsMimicGraph.remove(proposeTriangleToAddEdge); // commenting to check if the approach adds enough edges for triangles
					//}
					
					if (numOfIterationAddingEdgesToGraph == 5000) {
						// terminating while condition
						LOGGER.warn("Maximum iteration reached!! Not able to add edges to existing triangles and add new triangles to the mimic graph.");
						break;
					}
					
				} // end if condition - check if candidate triangles exists for adding edges to them
				else { // TODO: this if-else condition can be removed? after confirmation with supervisor
					
						// No edges can be added to existing triangles
						// Give warning and break while loop
					LOGGER.warn("Not able to add edges to existing triangles and add new triangles to the mimic graph. Estimated number is greater than actual number of edges! The process of adding triangle ends. ");
					break; // terminate out of while condition
					
				}
				

				} // end else condition adding edges to existing triangles
			
			
		}// end if condition - initial random triangle is not null
		LOGGER.info("Case 1 completed!");
		LOGGER.info("Added Edges: " + mMimicGraph.getEdges().size());
		LOGGER.info("Added Vertices: " + mMimicGraph.getVertices().size());
		
		//************************ Logic for isolated 1-simplexes ***********************************//
		//TODO: pending testing (Note: first two input graphs for SWDF have no isolated 1-simplexes)
		
		//temporary variables to track addition of vertices and edges for 1-simplexes
		int actualVerticesSimplexes = 0;
		int actualEdgesSimplexes = 0;
		
		//initialize variable tracking iteration count for this case
		oldEdgesCountGraph = 0;
		numOfIterationAddingEdgesToGraph = 0;
		
		// get head proposer defined for 1-simplex distribution
		OfferedItemByRandomProb<BitSet> potentialHeadColoProposer = simplex1distObj.getPotentialHeadColoProposer();
		
		LOGGER.info("Case 2a: Isolated 1-simplexes (with different source and target node)");
		LOGGER.info("Estimated Edges: " + estimatedEdges1Simplexes);
		LOGGER.info("Estimated Vertices: " + estimatedVertices1Simplexes);
		
		while((estimatedEdges1Simplexes > actualEdgesSimplexes) && (potentialHeadColoProposer != null) && (numOfIterationAddingEdgesToGraph < 5000)) { // additional condition that color proposer should not be null
			//TODO: Move Color proposer condition in if-statement?
			// check until it is possible to add more edges for 1-simplexes
			
			//variable to track if new vertex is not added
			boolean newVertexNotAdded = true;
			
			// get potential head for it and create vertex
			BitSet potentialheadColo = potentialHeadColoProposer.getPotentialItem();
			
			// Get potential tail color and add edge for it
			BitSet potentialTailColo = simplex1distObj.proposeVertColo(potentialheadColo).getPotentialItem();
			
			// temporary assignment for head vertex id
			int vertexIDHead = -1;
			// temporary assignment for tail vertex id
			int vertexIDTail = -1;
			
			
			
			//check if vertex can be added or existing vertex should be selected
			if ((estimatedVertices1Simplexes - actualVerticesSimplexes) >= 2 ) {
				// add a vertex if enough vertices are not created for 1-simplexes
				vertexIDHead = addVertexToMimicGraph(potentialheadColo, mMapColourToVertexIDs1Simplex);
				vertexIDTail = addVertexToMimicGraph(potentialTailColo, mMapColourToVertexIDs1Simplex);
				actualVerticesSimplexes = actualVerticesSimplexes + 2;
				
				newVertexNotAdded = false;//set the variable to false, since new variable is added

			}else {
				// New vertex cannot be created, select an existing vertex of the potential head color at random
				IntSet vertexIDshead = mMapColourToVertexIDs1Simplex.get(potentialheadColo);
				if (vertexIDshead == null) {
					numOfIterationAddingEdgesToGraph++;
					continue; // it is possible there is no vertex exist for the proposed head color, continue to check with new head color in that case
				}
				
				
				while((vertexIDshead.size() > 0) && (vertexIDTail == -1)) { // randomly select a head vertex and check if it has a tail with proposed color
					
					vertexIDHead = vertexIDshead.toArray(new Integer[vertexIDshead.size()])[mRandom.nextInt(vertexIDshead.size())];
					
					// get neighbors of selected vertex head
					IntSet neighbors = IntSetUtil.union(mMimicGraph.getInNeighbors(vertexIDHead), mMimicGraph.getOutNeighbors(vertexIDHead));
					
					IntSet vertexIDstail = new DefaultIntSet(Constants.DEFAULT_SIZE);//temporary set for storing potential tail IDs
					
					for (int potentialTail: neighbors) {
						BitSet vertexColour = mMimicGraph.getVertexColour(potentialTail);
						if (vertexColour.equals(potentialTailColo))
							vertexIDstail.add(potentialTail);
					}
					
					if (vertexIDstail.size() > 0)
						vertexIDTail = vertexIDstail.toArray(new Integer[vertexIDstail.size()])[mRandom.nextInt(vertexIDstail.size())];
					else
						vertexIDshead.remove(vertexIDHead); // randomly select head does not have the tail node with proposed colors
				}
				
				if (vertexIDTail == -1) {
					numOfIterationAddingEdgesToGraph++;
					continue;// propose a new head and tail colors when proposed colors are not found.
				}
				
			}
			
			boolean edgeAdded = addEdgeWithTriangleCheck(potentialheadColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapperConnected1Simplexes, newVertexNotAdded); // TODO: last parameter should always be false since we are selecting isolated 1-simplexes
			//boolean addedEdge = addEdgeInAnyDirectionWithDuplicateCheck(potentialheadColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapper1Simplexes);
			//addEdgeToMimicGraph(potentialheadColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapper1Simplexes); // commenting: using new method with duplicate check
			if (edgeAdded) {
				actualEdgesSimplexes++; // increment count if edge was added
				numOfIterationAddingEdgesToGraph = 0;
			} else {
				numOfIterationAddingEdgesToGraph++;
			}
			
			
		}
		LOGGER.info("Case 2a completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);
		
		//******************************* Logic for adding self loops in isolated 1-simplexes ***************************************//
		actualEdgesSimplexes = 0;
		
		//initialize variable tracking iteration count for this case
		oldEdgesCountGraph = 0;
		numOfIterationAddingEdgesToGraph = 0;
		
		// get head proposer defined for 1-simplex distribution
		potentialHeadColoProposer = simplex1distObj.getPotentialSelfLoopIn1SimplexColoProposer();
		LOGGER.info("Case 2b: Isolated self loop");
		LOGGER.info("Estimated Edges: " + estimatedEdgesSelfLoopIn1Simplex);
		
		while((estimatedEdgesSelfLoopIn1Simplex > actualEdgesSimplexes) && (potentialHeadColoProposer != null)) { // additional condition that color proposer should not be null
			//TODO: Move Color proposer condition in if-statement?
			// check until it is possible to add more edges for 1-simplexes
			
			// Maximum iteration check
			if (oldEdgesCountGraph == actualEdgesSimplexes) {
				numOfIterationAddingEdgesToGraph++;
				//System.out.println("Iteration: " + numOfIterationAddingEdgesToGraph);
			} else {
				oldEdgesCountGraph = actualEdgesSimplexes;
				numOfIterationAddingEdgesToGraph = 0;
			}
			
			// terminate while loop if maximum iteration reached
			if (numOfIterationAddingEdgesToGraph == 5000)
				break;
			
			// get potential head for it and create vertex
			BitSet potentialColoSelfLoop = potentialHeadColoProposer.getPotentialItem();
			
			// temporary assignment for head vertex id
			int vertexIDHead;
			
			// New vertex cannot be created, select an existing vertex of the potential head color at random
			IntSet vertexIDshead = mMapColourToVertexIDs1Simplex.get(potentialColoSelfLoop);
			if (vertexIDshead == null)
				continue; // it is possible there is no vertex exist for the proposed head color, continue to check with new head color in that case
			vertexIDHead = vertexIDshead.toArray(new Integer[vertexIDshead.size()])[mRandom.nextInt(vertexIDshead.size())];
			
			int vertexIDTail = vertexIDHead;
			
			boolean edgeAdded = addEdgeWithTriangleCheck(potentialColoSelfLoop, potentialColoSelfLoop, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapperSelfLoopIn1Simplex, false);
			//boolean addedEdge = addEdgeInAnyDirectionWithDuplicateCheck(potentialheadColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapper1Simplexes);
			//addEdgeToMimicGraph(potentialheadColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapper1Simplexes); // commenting: using new method with duplicate check
			if (edgeAdded)
				actualEdgesSimplexes++; // increment count if edge was added
			
		}
		
		LOGGER.info("Case 2b completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);
		
		
		//****************************** Logic for isolated self loop (i.e. 1-simplexes with same source and target node) *******************************************//
		actualVerticesSimplexes = 0;
		actualEdgesSimplexes = 0;
		
		//initialize variable tracking iteration count for this case
		oldEdgesCountGraph = 0;
		numOfIterationAddingEdgesToGraph = 0;
		
		// get head proposer defined for 1-simplex distribution
		potentialHeadColoProposer = simplex1distObj.getPotentialIsoSelfLoopColoProposer();
		
		LOGGER.info("Case 2c: Isolated self loop");
		LOGGER.info("Estimated Edges: " + estimatedEdgesIsoSelfLoop);
		LOGGER.info("Estimated Vertices: " + estimatedVerticesIsoSelfLoop);
		
		while((estimatedEdgesIsoSelfLoop > actualEdgesSimplexes) && (potentialHeadColoProposer != null)) { // additional condition that color proposer should not be null
			//TODO: Move Color proposer condition in if-statement?
			// check until it is possible to add more edges for 1-simplexes
			
			// Maximum iteration check
			if (oldEdgesCountGraph == actualEdgesSimplexes) {
				numOfIterationAddingEdgesToGraph++;
							//System.out.println("Iteration: " + numOfIterationAddingEdgesToGraph);
			} else {
				oldEdgesCountGraph = actualEdgesSimplexes;
				numOfIterationAddingEdgesToGraph = 0;
			}
						
			// terminate while loop if maximum iteration reached
			if (numOfIterationAddingEdgesToGraph == 5000)
				break;
			
			//variable to track if new vertex is not added
			boolean newVertexNotAdded = true;
			
			// get potential head for it and create vertex
			BitSet potentialColoSelfLoop = potentialHeadColoProposer.getPotentialItem();
			
			// temporary assignment for head vertex id
			int vertexIDHead;
			
			//check if vertex can be added or existing vertex should be selected
			if (actualVerticesSimplexes < estimatedVerticesIsoSelfLoop) {
				// add a vertex if enough vertices are not created for 1-simplexes
				vertexIDHead = addVertexToMimicGraph(potentialColoSelfLoop, mMapColourToVertexIDsIsoSelfLoop);
				actualVerticesSimplexes++;
				
				newVertexNotAdded = false;//set the variable to false, since new variable is added
				
			}else {
				// New vertex cannot be created, select an existing vertex of the potential head color at random
				IntSet vertexIDshead = mMapColourToVertexIDsIsoSelfLoop.get(potentialColoSelfLoop);
				if (vertexIDshead == null)
					continue; // it is possible there is no vertex exist for the proposed head color, continue to check with new head color in that case
				vertexIDHead = vertexIDshead.toArray(new Integer[vertexIDshead.size()])[mRandom.nextInt(vertexIDshead.size())];
			}
			
			// temporary assignment for tail vertex id
			int vertexIDTail = vertexIDHead;
			
			boolean edgeAdded = addEdgeWithTriangleCheck(potentialColoSelfLoop, potentialColoSelfLoop, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapperIsoSelfLoop, newVertexNotAdded);
			//boolean addedEdge = addEdgeInAnyDirectionWithDuplicateCheck(potentialheadColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapper1Simplexes);
			//addEdgeToMimicGraph(potentialheadColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapper1Simplexes); // commenting: using new method with duplicate check
			if (edgeAdded)
				actualEdgesSimplexes++; // increment count if edge was added
			
		}
		
		LOGGER.info("Case 2c completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);
		
		//************************ Logic for 0-simplexes ***********************************//
		LOGGER.info("Case 3: Isolated 0-simplexes");
		LOGGER.info("Estimated Vertices: " + estimatedVertices0Simplexes);
		//define proposer for 0-simplexes 
		OfferedItemByRandomProb<BitSet> potentialColoProposer0Simplex = simplex0distObj.getPotentialColoProposer();
		
		//initialize tracking variable
		actualVerticesSimplexes = 0;
		
		while((actualVerticesSimplexes < estimatedVertices0Simplexes) && (potentialColoProposer0Simplex != null)) {
			// get possible color
			BitSet potentialColo0Simplex = potentialColoProposer0Simplex.getPotentialItem();
			
			// Add 0-simplex to mimic graph
			addVertexToMimicGraph(potentialColo0Simplex, mMapColourToVertexIDs0Simplex);
			actualVerticesSimplexes++;
		}
		LOGGER.info("Case 3 completed!");
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);
		
		//********************** Add RDF Type Edges *************************************//
		LOGGER.info("Starting to add RDF Type edges for above cases");
		generateRDFTypeEdges(mMapColourToVertexIDs2Simplex); // Add for 2-simplexes
		generateRDFTypeEdges(mMapColourToVertexIDs1Simplex); // Add for 1-simplexes
		generateRDFTypeEdges(mMapColourToVertexIDs0Simplex); // Add for 0-simplexes
		LOGGER.info("Added RDF Type edges for above cases");
		LOGGER.info("Total Edges in mimic graph: " + mMimicGraph.getEdges().size());
		LOGGER.info("Total Vertices in mimic graph: " + mMimicGraph.getVertices().size());
		
		//*********************** Logic for connecting triangles using 1-simplexes *********************//
		// Initially, find isolated triangles. We can proceed with this case only if isolated triangles are added to the mimic graph by previous process
		LOGGER.info("Case 4: Connecting two-simplexes with 1-simplex");
		LOGGER.info("Estimated Edges: " + estimatedEdges1SimplexesConnect2Simplexes);
		
		//********************** Connect Isolated triangles *********************************//
		LOGGER.info("Finding Isolated triangles to connect (Case 4a)");
		// temporary map for storing isolated triangle colors and their vertices
		ObjectObjectOpenHashMap<TriColos, List<IntSet>> mTriangleColorsVertexIdsIsolated = new ObjectObjectOpenHashMap<TriColos, List<IntSet>>();
		
		// Variable to store colors found in isolated triangles
		Set<BitSet> allColoIsolatedTri = new HashSet<BitSet>();
		
		// iterate over all store triangle colors
		Object[] triColoskeys = mTriangleColorsVertexIds.keys;
		for (int i=0; i < triColoskeys.length; i++) {
			if (mTriangleColorsVertexIds.allocated[i]) {
				TriColos triColosToCheck = (TriColos) triColoskeys[i];
				
				//get vertices related to this triangle color
				List<IntSet> listVerticesTri = mTriangleColorsVertexIds.get(triColosToCheck);
				
				//check vertices neighbors and find isolated triangles
				for(IntSet setVerticesTri:listVerticesTri) {
					// collect all neighbors
					IntSet setNeighbors = new DefaultIntSet(Constants.DEFAULT_SIZE);
					for (int vertexIdTri: setVerticesTri) {
						setNeighbors = IntSetUtil.union(setNeighbors, mMimicGraph.getInNeighbors(vertexIdTri));
						setNeighbors = IntSetUtil.union(setNeighbors, mMimicGraph.getOutNeighbors(vertexIdTri));
					}
					
					// Remove vertices forming triangles
					setNeighbors = IntSetUtil.difference(setNeighbors, setVerticesTri);
					
					if (setNeighbors.size() == 0) {
						// isolated triangle found
						
						//Convert vertices to Array
						Integer[] vertexIDTriArr = setVerticesTri.toArray(new Integer[setVerticesTri.size()]);
					
						//update map
						updateMapTriangleColorsVertices(vertexIDTriArr[0], vertexIDTriArr[1], vertexIDTriArr[2], triColosToCheck, mTriangleColorsVertexIdsIsolated);
						
						allColoIsolatedTri.add(triColosToCheck.getA());
						allColoIsolatedTri.add(triColosToCheck.getB());
						allColoIsolatedTri.add(triColosToCheck.getC());
						
					}
					
				}
				
			}
			
		}
		
		if (mTriangleColorsVertexIdsIsolated.size() > 0) {
			// try to connect isolated triangles using 1-simplexes
			LOGGER.info("Isolated triangles found connecting them with 1-simplex.");
			
			//initialize variable to track number of edges added in the mimic graph 
			actualEdgesSimplexes = 0;
			
			//initialize variable tracking iteration count for this case //TODO: Create a function for evaluating maximum number of iteration
			oldEdgesCountGraph = 0;
			numOfIterationAddingEdgesToGraph = 0;
			
			while(actualEdgesSimplexes < estimatedEdges1SimplexesConnect2Simplexes) {
				
				// Maximum iteration check
				if (oldEdgesCountGraph == actualEdgesSimplexes) {
					numOfIterationAddingEdgesToGraph++;
					System.out.println("Iteration: " + numOfIterationAddingEdgesToGraph);
				} else {
					oldEdgesCountGraph = actualEdgesSimplexes;
					numOfIterationAddingEdgesToGraph = 0;
				}
							
				// terminate while loop if maximum iteration reached
				if (numOfIterationAddingEdgesToGraph == 5000)
					break;
			
				// get head color from the colors available in the triangle
				BitSet potentialHeadColoIsolatedTri = null; // initialize head color
				
				OfferedItemByRandomProb<BitSet> headColoProposerIsolatedTri = simplex1ConnTriDist.getPotentialHeadColoProposer();
				if (headColoProposerIsolatedTri != null)//check if head color proposer is not null
					potentialHeadColoIsolatedTri = headColoProposerIsolatedTri.getPotentialItem(allColoIsolatedTri);
				
				if (potentialHeadColoIsolatedTri != null) { //check for tail color only if head color is not null
				
					// get tail colors based on head color and available colors in the triangle
					BitSet potentialTailColoIsolatedTri = null; // initialize tail color
					
					OfferedItemByRandomProb<BitSet> tailColoProposer = simplex1ConnTriDist.proposeVertColo(potentialHeadColoIsolatedTri);
					if (tailColoProposer != null) // propose a color if proposer is not null
						potentialTailColoIsolatedTri = tailColoProposer.getPotentialItem(allColoIsolatedTri);
					
					if (potentialTailColoIsolatedTri != null) { // try to connect triangles using 1-simplexes if tail color is also not null
						
						//temporary variable to store the Triangle for proposed head color
						TriColos potentialTriangleHead = null;
						
						//temporary variable to store the Triangle for proposed tail color
						TriColos potentialTriangleTail = null;
						
						//find triangle with a head color 
						Object[] triColoIsolatedkeys = mTriangleColorsVertexIdsIsolated.keys;
						for (int i=0; i< triColoIsolatedkeys.length; i++) {
							if (mTriangleColorsVertexIdsIsolated.allocated[i]) {
								TriColos triColoIsolated = (TriColos) triColoIsolatedkeys[i];
								
								//check triangle for head color
								if (triColoIsolated.getA().equals(potentialHeadColoIsolatedTri) || triColoIsolated.getB().equals(potentialHeadColoIsolatedTri) || triColoIsolated.getC().equals(potentialHeadColoIsolatedTri)) {
									potentialTriangleHead = triColoIsolated;
								}
								
								//check triangle for tail color
								if (triColoIsolated.getA().equals(potentialTailColoIsolatedTri) || triColoIsolated.getB().equals(potentialTailColoIsolatedTri) || triColoIsolated.getC().equals(potentialTailColoIsolatedTri)) {
									potentialTriangleTail = triColoIsolated;
								}
								
								//stop looking for triangles of head and tail colors once found
								if ((potentialTriangleHead!=null) && (potentialTriangleTail!=null))
									break;
							}
						}
						
						if ((potentialTriangleHead == null) || (potentialTriangleTail == null))
							continue;// retry to propose a different head or tail and find triangle for it
						
						// variables storing head and tail IDs for Triangles to connect
						int possHeadIDIsolatedTri = 0;
						int possTailIDIsolatedTri = 0;
						
						// Variables storing vertices for isolated triangles found for head and tail colors
						IntSet verticesTriIsolatedhead;
						IntSet verticesTriIsolatedtail;
						
						// get unique set of vertices for the found triangle colors
						if (potentialTriangleHead.equals(potentialTriangleTail)) {
							// Check if both the triangle colors are same. Note: It is possible that we have same triangle colors for head and tail
							List<IntSet> listTriangleToConn = mTriangleColorsVertexIdsIsolated.get(potentialTriangleHead);
							
							if (listTriangleToConn.size() == 1)
								continue;//Just have single triangle keep looking for new triangles to connect
							
							// store the possible vertices for head and tail
							verticesTriIsolatedhead = listTriangleToConn.get(0);
							verticesTriIsolatedtail = listTriangleToConn.get(1);
							
						}else {
							// store the possible vertices for head and tail
							verticesTriIsolatedhead = mTriangleColorsVertexIdsIsolated.get(potentialTriangleHead).get(0); //TODO: Any triangle could be selected randomly
							verticesTriIsolatedtail = mTriangleColorsVertexIdsIsolated.get(potentialTriangleTail).get(0); //TODO: Any triangle could be selected randomly
							
						}
						
						// compute concrete vertex id for head color and tail color
						for (int vertexIDheadIsoTri: verticesTriIsolatedhead) {
							if (mMimicGraph.getVertexColour(vertexIDheadIsoTri).equals(potentialHeadColoIsolatedTri)) {
								possHeadIDIsolatedTri = vertexIDheadIsoTri;
								break;
							}
						}
						
						for (int vertexIDTailIsoTri: verticesTriIsolatedtail) {
							if (mMimicGraph.getVertexColour(vertexIDTailIsoTri).equals(potentialTailColoIsolatedTri)) {
								possTailIDIsolatedTri = vertexIDTailIsoTri;
								break;
							}
						}
						
						// Compute possible edge color
						Set<BitSet> possibleLinkingEdgeColours = mColourMapper1SimplexesConnTri.getPossibleLinkingEdgeColours(potentialTailColoIsolatedTri, potentialHeadColoIsolatedTri);
						BitSet possEdgeColo = possibleLinkingEdgeColours.toArray(new BitSet[possibleLinkingEdgeColours.size()])[mRandom.nextInt(possibleLinkingEdgeColours.size())];
						
						// add edge to the graph
						mMimicGraph.addEdge(possTailIDIsolatedTri, possHeadIDIsolatedTri, possEdgeColo);
						actualEdgesSimplexes++;
						
						// update the mapping of edge color and tail-head IDs
						updateMappingOfEdgeColoHeadTailColo(possEdgeColo, possHeadIDIsolatedTri, possTailIDIsolatedTri);
						
						//update the map storing isolated triangles
						// get vertices for isolated triangles having head color and remove them. Since, the triangle is connected with an edge to another edge.
						List<IntSet> listHeadVerticesIsoTri = mTriangleColorsVertexIdsIsolated.get(potentialTriangleHead);
						listHeadVerticesIsoTri.remove(verticesTriIsolatedhead);
						
						// get vertices for isolated triangles having tail color and remove them.
						List<IntSet> listTailVerticesIsoTri = mTriangleColorsVertexIdsIsolated.get(potentialTriangleTail);
						listTailVerticesIsoTri.remove(verticesTriIsolatedtail);
						LOGGER.info("Two isolated triangles connected with 1-simplex");
					}
					
				}
			}
			
		}
		
		//******************************** Link set of connected triangles with 1-simplex ***************************************************//
		// Note: This is a second scenario possible for connecting 2-simplexes using 1-simplexes. Here, we use a sampling approach to select created triangles and try to link them without creating a new triangle
		LOGGER.info("Trying to connect set of connected triangles using 1-simplex (Case 4b)");
		
		//initialize variable tracking iteration count for this case
		oldEdgesCountGraph = actualEdgesSimplexes;
		numOfIterationAddingEdgesToGraph = 0;
		
		while(actualEdgesSimplexes < estimatedEdges1SimplexesConnect2Simplexes) { // check if we can add more edge
			
			// Maximum iteration check
			if (oldEdgesCountGraph == actualEdgesSimplexes) {
				numOfIterationAddingEdgesToGraph++;
				//System.out.println("Iteration: " + numOfIterationAddingEdgesToGraph);
			} else {
				oldEdgesCountGraph = actualEdgesSimplexes;
				numOfIterationAddingEdgesToGraph = 0;
			}
						
			// terminate while loop if maximum iteration reached
			if (numOfIterationAddingEdgesToGraph == 5000)
				break;
			
			// Propose a possible colors for head
			OfferedItemByRandomProb<BitSet> headColoProposercase4b = simplex1ConnTriDist.getPotentialHeadColoProposer();
			BitSet potentialHeadColocase4b = null; // initialize head color
			
			if (headColoProposercase4b != null) {
				potentialHeadColocase4b = headColoProposercase4b.getPotentialItem();
				
				// get tail colors based on head color
				OfferedItemByRandomProb<BitSet> tailColoProposercase4b = simplex1ConnTriDist.proposeVertColo(potentialHeadColocase4b);
				BitSet potentialTailColocase4b = null;
				if (tailColoProposercase4b != null) {
					potentialTailColocase4b = tailColoProposercase4b.getPotentialItem();
				}
				
				// search for triangles when potential head and tail colors are not null
				if ( (potentialHeadColocase4b!= null) && (potentialTailColocase4b!=null)) {
					
					// Temporary triangles for head and tail color
					TriColos potentialTriangleHeadCase4b = null;
					TriColos potentialTriangleTailCase4b = null;
					
					
					
					// Iterate over triangles to find head and tail colors
					Object[] connTrianglesColos = mTriangleColorsVertexIds.keys;
					for (int i=0; i< connTrianglesColos.length; i++) {
						if (mTriangleColorsVertexIds.allocated[i]) {
							TriColos tempTriColo = (TriColos) connTrianglesColos[i];
							
							// variable to track same triangles are not selected for head and tail colors
							boolean triangleFoundForHead = false;
							
							//check triangle for head color (if not already found)
							if ((tempTriColo.getA().equals(potentialHeadColocase4b) || tempTriColo.getB().equals(potentialHeadColocase4b) || tempTriColo.getC().equals(potentialHeadColocase4b)) && (potentialTriangleHeadCase4b == null)) {
								potentialTriangleHeadCase4b = tempTriColo;
								triangleFoundForHead = true;
							}
							
							//check triangle for tail color
							if ( (tempTriColo.getA().equals(potentialTailColocase4b) || tempTriColo.getB().equals(potentialTailColocase4b) || tempTriColo.getC().equals(potentialTailColocase4b)) && !triangleFoundForHead && (potentialTriangleTailCase4b == null)) {
								potentialTriangleTailCase4b = tempTriColo;
							}
							
							//stop looking for triangles of head and tail colors once found
							if ((potentialTriangleHeadCase4b!=null) && (potentialTriangleTailCase4b!=null))
								break;
							
						}// end if condition: Triangle color allocated to Open hash map
						
						
					} // end for loop
					
					
					//If triangles are not found retry by proposing a head and a tail color
					if ((potentialTriangleHeadCase4b == null) || (potentialTriangleTailCase4b == null))
						continue;
					
					
					//***************** Potential head and tail colors found for triangles to connect ***********//
					// Select triangles at random to connect
					// There could be multiple triangles for the found triangle colors, selecting one of them at random
					
					// We try to add edges in multiple iterations for the found head and tail colors. 
					// It is possible that such iterations do not allow to add edges between two triangles since it forms a new triangle.
					
					int numIterationsCase4b = 0; // temporary variable to track number of iterations
					
					while ( (numIterationsCase4b < 51)) {
						
						numIterationsCase4b++;//increment number of edges
						
						//get vertices for head color
						List<IntSet> possVerticesListhead = mTriangleColorsVertexIds.get(potentialTriangleHeadCase4b);
						IntSet verticesheadcase4b = possVerticesListhead.get(mRandom.nextInt(possVerticesListhead.size()));
						
						//get vertices for tail color
						List<IntSet> possVerticesListtail = mTriangleColorsVertexIds.get(potentialTriangleTailCase4b);
						IntSet verticestailcase4b = possVerticesListtail.get(mRandom.nextInt(possVerticesListtail.size()));
						
						// variables storing head and tail IDs for Triangles to connect
						int possHeadIDcase4b = 0;
						int possTailIDcase4b = 0;
						
						// compute concrete vertex id for head color and tail color
						for (int vertexIDheadtemp: verticesheadcase4b) {
							if (mMimicGraph.getVertexColour(vertexIDheadtemp).equals(potentialHeadColocase4b)) {
								possHeadIDcase4b = vertexIDheadtemp;
								break;
							}
						}
						
						for (int vertexIDTailTemp: verticestailcase4b) {
							if (mMimicGraph.getVertexColour(vertexIDTailTemp).equals(potentialTailColocase4b)) {
								possTailIDcase4b = vertexIDTailTemp;
								break;
							}
						}
						
						//Add edge between selected vertices if they do not have a vertex in common using below function call
						boolean edgeAdded = addEdgeWithTriangleCheck(potentialHeadColocase4b, potentialTailColocase4b, possHeadIDcase4b, possTailIDcase4b, mMapColourToEdgeIDs1Simplex, mColourMapper1SimplexesConnTri, true);
						if (edgeAdded) {
							actualEdgesSimplexes++;
							LOGGER.info("Successfully connected triangles using 1-simplex (Case 4b)");
							
							break;//terminate while loop trying to add edge for the found head and tail color
						}
						
						
					}
					
					
				}// end if condition: null check for head and tail color
			}
		}
		
		
		LOGGER.info("Case 4 completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		
		//******************* Logic for creating isolated 2-simplexes ***********************//
		LOGGER.info("Case 5: Isolated 2-simplexes");
		LOGGER.info("Estimated Edges: " + estimatedEdgesIsolatedTriangle);
		LOGGER.info("Estimated Vertices: " + estimatedVerticesIsolatedTriangle);
		OfferedItemByRandomProb<TriColos> potentialIsolatedTriangleProposer = triangledistributionObject.getPotentialIsolatedTriangleProposer(); // get isolated triangle proposer
		
		// initialize tracker variable
		actualVerticesSimplexes = 0;
		actualEdgesSimplexes = 0;
		
		// Variable to track set of triangle added to the mimic graph (i.e. set of Colors of the vertices forming the triangle)
		Set<TriColos> setIsoTriInMimicGraph = new HashSet<TriColos>();
		
		while ( ((estimatedVerticesIsolatedTriangle - actualVerticesSimplexes) >= 3) && ((estimatedEdgesIsolatedTriangle - actualEdgesSimplexes) >= 3 ) && (potentialIsolatedTriangleProposer != null) ) {
			TriColos possIsoTri = potentialIsolatedTriangleProposer.getPotentialItem();
			// add the selected random triangle to mimic graph			
			addTriangleToMimicGraph( possIsoTri, mColourMapperIsolatedTriangles, mMapColourToVertexIDs2SimplexIsolated, mMapColourToEdgeIDs2SimplexIsolated, mIsolatedTriangleColorsVertexIds);
			
			setIsoTriInMimicGraph.add(possIsoTri);
						
			//increment no of vertices in triangle
			actualVerticesSimplexes = actualVerticesSimplexes + 3;
						
			//increment no. of edges in triangle
			actualEdgesSimplexes = actualEdgesSimplexes + 3;
		}
		//TODO: Add edges to isolated triangles when it is not possible to further add vertices. Check logs for earlier executions.
		
		int iterationCount = 0;
		
		while ((estimatedEdgesIsolatedTriangle > actualEdgesSimplexes) && (iterationCount < 5000)) {
			
			if (setIsoTriInMimicGraph.size() > 0) {
			
				// Logic for adding edges to existing triangles
				TriColos proposeTriangleToAddEdge = triangledistributionObject.proposeTriangleToAddEdge(setIsoTriInMimicGraph);
				
				// Best case: A triangle is returned
				List<IntSet> selectedTrianglesList = mIsolatedTriangleColorsVertexIds.get(proposeTriangleToAddEdge);
				
				// randomly selecting one of these triangles
				IntSet selectedVertices = selectedTrianglesList.toArray(new IntSet[selectedTrianglesList.size()])[mRandom.nextInt(selectedTrianglesList.size())];
				
				// temporary variable to track if edge was added to existing triangle
				boolean edgeNotAddedToExistingTriangle = true;
				
				// Considering different vertex pairs to add an edge
				
				//Convert vertices to Array
				Integer[] vertexIDExistingTriangle = selectedVertices.toArray(new Integer[selectedVertices.size()]);
				
				// creating different pairs of combinations for 3 vertices of a triangle
				List<List<Integer>> differentPairsOfVertices = new ArrayList<List<Integer>>();
				differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[1]));
				differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[1], vertexIDExistingTriangle[2]));
				differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[2]));
				
				for(List<Integer> pairOfVertices: differentPairsOfVertices) {
				
					// Get vertex colours for a pair
					BitSet existingVertexColo1 = mMimicGraph.getVertexColour(pairOfVertices.get(0));
					BitSet existingVertexColo2 = mMimicGraph.getVertexColour(pairOfVertices.get(1));
					
					boolean edgeAdded = addEdgeInAnyDirectionWithDuplicateCheck(existingVertexColo1, existingVertexColo2, pairOfVertices.get(0), pairOfVertices.get(1), mMapColourToEdgeIDs2SimplexIsolated, mColourMapperIsolatedTriangles);
					
					if (edgeAdded) {
						//increment no. of edges in triangle
						actualEdgesSimplexes = actualEdgesSimplexes + 1;
						
						//update boolean tracker variable
						edgeNotAddedToExistingTriangle = false;
						iterationCount = 0;
						
						// break iterating over pair of vertices, since an edge is found
						break;
					}
					
				}
				
				if (edgeNotAddedToExistingTriangle) {
					iterationCount++;
				}
				
			} else {
				break; // no isolated triangles are present in the mimic graph
			}
		}
		
		LOGGER.info("Case 5 completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);
		
		
		//******************* Logic for connected 1-simplexes ***********************//
		// TODO: pending
		// Determine number of edges and vertices for this case [done]
		
		int actualEdgesMimicGraph = mMimicGraph.getEdges().size();
		int edgesConnected1Simplexes = mIDesiredNoOfEdges - actualEdgesMimicGraph - estimatedEdgesCommon - estEdgesSelfLoopIsoTri - estEdgesSelfLoopConnTri - estEdgesSelfLoop1SimplexConnToTri - estEdgesSelfLoopConn1Simplexes;
				
		int actualVerticesMimicGraph = mMimicGraph.getVertices().size();
		int verticesConnected1Simplexes = mIDesiredNoOfVertices - actualVerticesMimicGraph - estVerts1SimplexesConntoTri; // subtracting estimated number of vertices for 1-simplexes connected only to triangles. Such vertices are created in next step.
		
		// initialize tracker variable
		actualVerticesSimplexes = 0;
		actualEdgesSimplexes = 0;
		
		//initialize variable tracking iteration count for this case
		oldEdgesCountGraph = 0;
		numOfIterationAddingEdgesToGraph = 0;
		
		// Get vertex color proposer
		OfferedItemByRandomProb<BitSet> potentialVertColoProposer = connectedSimplex1distObj.getPotentialHeadColoProposer();
		
		// set to track class colors. New vertices need to be created for them later
		Set<BitSet> vertexClassColoSet = new HashSet<BitSet>();
		LOGGER.info("Case 6: Connected 1-simplexes");
		LOGGER.info("Estimated Edges: " + edgesConnected1Simplexes);
		LOGGER.info("Estimated Vertices: " + verticesConnected1Simplexes);
		
		while((edgesConnected1Simplexes > actualEdgesSimplexes) && (potentialVertColoProposer != null)) {
			// check until it is possible to add more edges for 1-simplexes
			
			//variable to track if new vertex is not added
			boolean newVertexNotAdded = true;
			
			// Maximum iteration check
			if (oldEdgesCountGraph == actualEdgesSimplexes) {
				numOfIterationAddingEdgesToGraph++;
				//System.out.println("Iteration: " + numOfIterationAddingEdgesToGraph);
			} else {
				oldEdgesCountGraph = actualEdgesSimplexes;
				numOfIterationAddingEdgesToGraph = 0;
			}
						
			// terminate while loop if maximum iteration reached
			if (numOfIterationAddingEdgesToGraph == 5000)
				break;
			
			// get potential head for it and create vertex
			BitSet potentialvertColo = potentialVertColoProposer.getPotentialItem();
			
			// get class colors for head color and check if class vertex exist for them
			Set<BitSet> classColourSet = mMimicGraph.getClassColour(potentialvertColo);
			int numberOfClassVertices = 0;
			for (BitSet classColour: classColourSet) {
				Integer vertexIdClass = mMapClassColourToVertexIDSimplexes.get(classColour);
				if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
					numberOfClassVertices++;//class vertex need to be created for this vertex
					vertexClassColoSet.add(classColour);
				}
			}
			
			// temporary assignment for head vertex id
			int vertexIDHead;
			
			//check if vertex can be added or existing vertex should be selected
			if ((actualVerticesSimplexes+numberOfClassVertices) < verticesConnected1Simplexes) {
				// add a vertex if enough vertices are not created for 1-simplexes
				vertexIDHead = addVertexToMimicGraph(potentialvertColo, mMapColourToVertexIDs1SimplexConnected);
				actualVerticesSimplexes++;
				//updating estimated vertices for this case. Note: class nodes are created in next step
				verticesConnected1Simplexes = verticesConnected1Simplexes - numberOfClassVertices;
				//updating estimated edges for this case. Since type edges need to be added for this vertices depending upon number of classes assigned to the vertex
				edgesConnected1Simplexes = edgesConnected1Simplexes - classColourSet.size();
				
				newVertexNotAdded = false;//set the variable to false, since new variable is added
				
			}else {
				// New vertex cannot be created, select an existing vertex of the potential head color at random
				IntSet vertexIDshead = mMapColourToVertexIDs1SimplexConnected.get(potentialvertColo); // TODO: returned set could be null. Need to consider that [done]
				if (vertexIDshead == null)
					continue; // Propose another color if no vertex found. TODO: Evaluate If this condition runs into infinite loop
				vertexIDHead = vertexIDshead.toArray(new Integer[vertexIDshead.size()])[mRandom.nextInt(vertexIDshead.size())];
			}
			
			// Get potential tail color and add edge for it
			BitSet potentialTailColo = connectedSimplex1distObj.proposeVertColo(potentialvertColo).getPotentialItem();
			
			// get class colors for head color and check if class vertex exist for them
			classColourSet = mMimicGraph.getClassColour(potentialvertColo);
			numberOfClassVertices = 0;
			for (BitSet classColour: classColourSet) {
				Integer vertexIdClass = mMapClassColourToVertexIDSimplexes.get(classColour);
				if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
					numberOfClassVertices++;//class vertex need to be created for this vertex
					vertexClassColoSet.add(classColour);
				}
			}
			
			// temporary assignment for tail vertex id
			int vertexIDTail;
			
			//Check similar to above
			if ((actualVerticesSimplexes+numberOfClassVertices) < verticesConnected1Simplexes) {
				vertexIDTail = addVertexToMimicGraph(potentialTailColo, mMapColourToVertexIDs1SimplexConnected);
				actualVerticesSimplexes++;
				
				//updating estimated vertices for this case. Note: class nodes are created in next step
				verticesConnected1Simplexes = verticesConnected1Simplexes - numberOfClassVertices;
				//updating estimated edges for this case. Since type edges need to be added for this vertices
				edgesConnected1Simplexes = edgesConnected1Simplexes - classColourSet.size();
				
				newVertexNotAdded = false;//set the variable to false, since new variable is added
			} else {
				IntSet vertexIDstail = mMapColourToVertexIDs1SimplexConnected.get(potentialTailColo);
				
				if (vertexIDstail == null)
					continue; // Propose another color if no vertex found. TODO: Evaluate If this condition runs into infinite loop
				
				vertexIDTail = vertexIDstail.toArray(new Integer[vertexIDstail.size()])[mRandom.nextInt(vertexIDstail.size())];
			}
			
			boolean edgeAdded = addEdgeWithTriangleCheck(potentialvertColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapperConnected1Simplexes, newVertexNotAdded);
			//boolean edgeAdded = addEdgeInAnyDirectionWithDuplicateCheck(potentialvertColo, potentialTailColo, vertexIDHead, vertexIDTail, mMapColourToEdgeIDs1Simplex, mColourMapperConnected1Simplexes);
			if (edgeAdded)
				actualEdgesSimplexes++;
		}
		LOGGER.info("Case 6 completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);
		
		// Add RDF Type edges for connected 1-simplexes
		generateRDFTypeEdges(mMapColourToVertexIDs1SimplexConnected);
		LOGGER.info("Added RDF Type edges for case 6");
		LOGGER.info("Total Edges in graph: " + mMimicGraph.getEdges().size());
		LOGGER.info("Total Vertices in graph: " + mMimicGraph.getVertices().size());
		
		//******************* Logic for 2-simplexes connected to rest of the graph ********************// 
		//update temporary variables
		actualVerticesSimplexes = 0;
		
		// get actual vertices count
		actualVerticesMimicGraph = mMimicGraph.getVertices().size();
		
		// compute remaining vertices and edges
		//int remainingVertices = mIDesiredNoOfVertices - actualVerticesMimicGraph;
		
		//update the estimated counts for "2-simplexes connected" if remaining counts is greater than estimated counts
		int estimatedVerticesCommon = estVerts1SimplexesConntoTri; // initially assuming no vertices can be created for creating "2-simplexes connected" case
		//if (remainingVertices > 0) {
		//	estimatedVerticesCommon = remainingVertices;
		//}
		
		LOGGER.info("Case 7: Connect 2-simplexes to other vertices");
		LOGGER.info("Estimated Vertices: " + estimatedVerticesCommon);
		
		// get proposer of Vertex color (to create vertices connected to triangles) 
		OfferedItemByRandomProb<BitSet> potentialColoProposerForVertConnectedToTriangle = simplex2distObj.getPotentialColoProposerForVertConnectedToTriangle();
		
		// set to track class colors. New vertices need to be created for them later
		vertexClassColoSet = new HashSet<BitSet>();
		
		// Add vertices if not enough vertices
		while((actualVerticesSimplexes < estimatedVerticesCommon) && (potentialColoProposerForVertConnectedToTriangle!= null)) {
			BitSet potentialvertexColo = potentialColoProposerForVertConnectedToTriangle.getPotentialItem();
			
			// check class node exists for the proposed color
			Set<BitSet> classColourSet = mMimicGraph.getClassColour(potentialvertexColo);
			int numberOfClassVertices = 0;
			for (BitSet classColour: classColourSet) {
				Integer vertexIdClass = mMapClassColourToVertexIDSimplexes.get(classColour);
				if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
					numberOfClassVertices++;
					vertexClassColoSet.add(classColour);
				}
			}
			
			// create a new vertex only if we can add class nodes
			if ((actualVerticesSimplexes+numberOfClassVertices) < estimatedVerticesCommon) {
				addVertexToMimicGraph(potentialvertexColo, mMapColourToVertexIDsConnectedTo2Simplex);
				actualVerticesSimplexes++;
				//updating estimated vertices for this case. Note: class nodes are created in next step
				estimatedVerticesCommon = estimatedVerticesCommon - numberOfClassVertices;
			}
		}
		
		LOGGER.info("Vertex addition completed");
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);
		
		//Add RDF Type edges for vertices
		generateRDFTypeEdges(mMapColourToVertexIDsConnectedTo2Simplex);
		LOGGER.info("Added RDF Type edges for above created vertices");
		
		// get actual edges count and update temporary variable
		actualEdgesMimicGraph = mMimicGraph.getEdges().size();
		actualEdgesSimplexes = 0;
		
		//initialize variable tracking iteration count for this case
		oldEdgesCountGraph = 0;
		numOfIterationAddingEdgesToGraph = 0;
		
		// compute remaining edges
		int remainingEdges = mIDesiredNoOfEdges - actualEdgesMimicGraph;
		
		//update the estimated counts for "2-simplexes connected" if remaining counts is greater than estimated counts
		if (remainingEdges > estimatedEdgesCommon) {
			estimatedEdgesCommon = remainingEdges;
		}
		
		LOGGER.info("Estimated Edges: " + estimatedEdgesCommon);
		
		// Get head color proposer for creating 1-simplexes connected to triangles
		OfferedItemByRandomProb<BitSet> potentialHeadColoCommon2Simplex = simplex2distObj.getPotentialHeadColoProposer();
		
		while((actualEdgesSimplexes < estimatedEdgesCommon) && (potentialHeadColoCommon2Simplex != null)) {
			
			// Maximum iteration check
			if (oldEdgesCountGraph == actualEdgesSimplexes) {
				numOfIterationAddingEdgesToGraph++;
				//System.out.println("Iteration: " + numOfIterationAddingEdgesToGraph);
			} else {
				oldEdgesCountGraph = actualEdgesSimplexes;
				numOfIterationAddingEdgesToGraph = 0;
			}
									
			// terminate while loop if maximum iteration reached
			if (numOfIterationAddingEdgesToGraph == 5000)
				break;
			
			//TODO: Simplify below logic			
			
			// Get possible head color from distribution
			BitSet potentialheadColo = potentialHeadColoCommon2Simplex.getPotentialItem();
			
			//Get possible tail Color for the head color
			BitSet potentialTailColo = simplex2distObj.proposeVertColo(potentialheadColo).getPotentialItem();
			
			// Check if head Color is available in 2-Simplexes
			IntSet vertsWithHeadColo = mMapColourToVertexIDs2Simplex.get(potentialheadColo);
			
			// variable to track if head color is in 2-simplex
			boolean headColoIn2Simplex = true;
			
			if (vertsWithHeadColo == null) {
				// check head color is available in connected 1-simplexes
				vertsWithHeadColo = mMapColourToVertexIDs1SimplexConnected.get(potentialheadColo);
				
				//check head color is available in new created vertex for connecting with triangle
				if (vertsWithHeadColo == null) {
					vertsWithHeadColo = mMapColourToVertexIDsConnectedTo2Simplex.get(potentialheadColo);
				}
				
				headColoIn2Simplex = false;
			}
			
			if (vertsWithHeadColo == null) {
				// There does not exist any vertex with potential color, continue and try to use other head color
				continue;
			}
			
			
			// Get vertices with tail color from 2-simplex or connected 1-simplex depending where the vertex of head color exist
			IntSet vertsWithTailColo = null;
			if (headColoIn2Simplex) {
				vertsWithTailColo = mMapColourToVertexIDs1SimplexConnected.get(potentialTailColo);
				
				//check tail color is available in new created vertex for connecting with triangle
				if (vertsWithTailColo == null) {
					vertsWithTailColo = mMapColourToVertexIDsConnectedTo2Simplex.get(potentialTailColo);
					
				}
			}
			else
				vertsWithTailColo = mMapColourToVertexIDs2Simplex.get(potentialTailColo);
			
			if (vertsWithTailColo == null) {
				// Vertex for tail color is not found, Look for head color and tail color in different simplexes
				
				if (headColoIn2Simplex) {
					// Earlier head color was in 2-simplex, now looking in connected 1-simplex
					vertsWithHeadColo = mMapColourToVertexIDs1SimplexConnected.get(potentialheadColo);
					
					//check head color is available in new created vertex for connecting with triangle
					if (vertsWithHeadColo == null) {
						vertsWithHeadColo = mMapColourToVertexIDsConnectedTo2Simplex.get(potentialheadColo);
					}
					
				} else {
					vertsWithHeadColo = mMapColourToVertexIDs2Simplex.get(potentialheadColo);
				}
				
				// Continue if vertices are not found for head color even after swap
				if (vertsWithHeadColo == null)
					continue;
				
				// Get vertices for tail color
				if (headColoIn2Simplex) {
					vertsWithTailColo = mMapColourToVertexIDs1SimplexConnected.get(potentialTailColo);
					//check tail color is available in new created vertex for connecting with triangle
					if (vertsWithTailColo == null) {
						vertsWithTailColo = mMapColourToVertexIDsConnectedTo2Simplex.get(potentialTailColo);
						
					}
				}
				else
					vertsWithTailColo = mMapColourToVertexIDs2Simplex.get(potentialTailColo);
				
				// Continue if vertices are not found for tail color even after swap
				if (vertsWithTailColo == null)
					continue;
				
			}
			
			// Select any random vertex with head color
			int vertexIDWithHeadColo = vertsWithHeadColo.toArray(new Integer[vertsWithHeadColo.size()])[mRandom.nextInt(vertsWithHeadColo.size())];
			
			// Select any random vertex with head color
			int vertexIDWithTailColo = vertsWithTailColo.toArray(new Integer[vertsWithTailColo.size()])[mRandom.nextInt(vertsWithTailColo.size())];
			
			//  Get possible edge color using the mapper
			Set<BitSet> possibleLinkingEdgeColours = mColourMapperCommonEdges.getPossibleLinkingEdgeColours(potentialTailColo, potentialheadColo);
			
			// check existing edge colors between vertices and remove them from the possible edge colors set
			possibleLinkingEdgeColours = removeDuplicateEdgeColors(vertexIDWithHeadColo, vertexIDWithTailColo, possibleLinkingEdgeColours);
			
			boolean havingVertices = commonVertices(vertexIDWithHeadColo, vertexIDWithTailColo);
			
			if ((possibleLinkingEdgeColours.size() > 0) && !havingVertices) {
				//select a random edge
				BitSet possEdgeColo = possibleLinkingEdgeColours.toArray(new BitSet[possibleLinkingEdgeColours.size()])[mRandom.nextInt(possibleLinkingEdgeColours.size())];
			
				// add the edge to graph
				mMimicGraph.addEdge(vertexIDWithTailColo, vertexIDWithHeadColo, possEdgeColo);
				actualEdgesSimplexes++;
				
				//TODO: tracking the added edges
				
				//update the map to track edge colors, tail id and head ids
				updateMappingOfEdgeColoHeadTailColo(possEdgeColo, vertexIDWithHeadColo, vertexIDWithTailColo);
				
			}
			
		}
		LOGGER.info("Case 7 completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		
		//************************* Logic to add self loops for simplexes created for different cases *********************************//
		LOGGER.info("Adding Self loops...........");
		
		//Isolated 2-simplexes		
		LOGGER.info("Isolated 2-simplexes");
		addSelfLoops(estEdgesSelfLoopIsoTri, distColoProposerSelfLoopIsoTri, mMapColourToVertexIDs2SimplexIsolated, mColourMapperSelfLoopIsoTri, mMapColourToEdgeIDs2SimplexIsolated);
		
		// Connected 2-simplexes		
		LOGGER.info("Connected 2-simplexes");
		addSelfLoops(estEdgesSelfLoopConnTri, distColoProposerSelfLoopConnTri, mMapColourToVertexIDs2Simplex, mColourMapperSelfLoopConnTri, mMapColourToEdgeIDs2Simplex);
		
		//1-simplexes only connected to triangles
		LOGGER.info("1-simplexes connected only to triangles");
		addSelfLoops(estEdgesSelfLoop1SimplexConnToTri, distColoProposerSelfLoop1SimplexConnToTri, mMapColourToVertexIDsConnectedTo2Simplex, mColourMapperSelfLoop1SimplexConnToTri, mMapColourToEdgeIDs1Simplex);
		
		//Connected 1-simplexes
		LOGGER.info("Connected 1-simplexes");
		addSelfLoops(estEdgesSelfLoopConn1Simplexes, distColoProposerSelfLoopConn1Simplexes, mMapColourToVertexIDs1SimplexConnected, mColourMapperSelfLoopConn1Simplexes, mMapColourToEdgeIDs1Simplex);
		
		System.out.println("Number of edges in the mimic graph: " + mMimicGraph.getEdges().size());
		System.out.println("Number of vertices in the mimic graph: " + mMimicGraph.getVertices().size());
		
		// Update mMapColourToVertexIDs used for adding edges when improving the graph in next phase
		updateVertexColoMap(mMapColourToVertexIDs1Simplex); //isolated 1-simplexes
		updateVertexColoMap(mMapColourToVertexIDs2Simplex); // connected 2-simplexes
		updateVertexColoMap(mMapColourToVertexIDsIsoSelfLoop); // isolated self loop
		updateVertexColoMap(mMapColourToVertexIDs2SimplexIsolated); // isolated 2-simplexes
		updateVertexColoMap(mMapColourToVertexIDs1SimplexConnected); // connected 1-simplexes
		updateVertexColoMap(mMapColourToVertexIDsConnectedTo2Simplex); // 1-simplexes connected to 2-simplexes
		
		return mMimicGraph;
	}
	
	private void addSelfLoops(int estEdgesInput, OfferedItemByRandomProb<BitSet> distColoProposerSelfLoopInput, Map<BitSet, IntSet> mMapColourToVertexIDsInput, IColourMappingRules mColourMapperSelfLoopInput, Map<BitSet, IntSet> mMapColourToEdgeIDsInput) {
		LOGGER.info("Estimated edges: " + estEdgesInput);
		int actualEdgesSimplexes = 0;
		int iterationCountSelf = 0;
		while ((estEdgesInput > actualEdgesSimplexes) && (iterationCountSelf < 5000)) {
			BitSet proposedVertexColor = distColoProposerSelfLoopInput.getPotentialItem();
			IntSet possVertices = mMapColourToVertexIDsInput.get(proposedVertexColor);
			if (possVertices.size() > 0) {
				Integer vertexID = possVertices.toArray(new Integer[possVertices.size()])[mRandom.nextInt(possVertices.size())];
				boolean edgeAdded = addEdgeInAnyDirectionWithDuplicateCheck(proposedVertexColor, proposedVertexColor, vertexID, vertexID, mMapColourToEdgeIDsInput, mColourMapperSelfLoopInput);
				if (edgeAdded) {
					actualEdgesSimplexes++;
					iterationCountSelf = 0;
				} else {
					iterationCountSelf++;
				}
			}	
		}
		LOGGER.info("Added edges: " + actualEdgesSimplexes);
	}
	
	private void updateVertexColoMap(Map<BitSet, IntSet> mMapColourToVertexIDsinput) {
		Set<BitSet> keySetIso1Simplexes = mMapColourToVertexIDsinput.keySet();
		for (BitSet vertexColo: keySetIso1Simplexes) {
			IntSet tempSet = mMapColourToVertexIDs.get(vertexColo);
			if (tempSet == null)
				tempSet = new DefaultIntSet(Constants.DEFAULT_SIZE);
			tempSet.addAll(mMapColourToVertexIDsinput.get(vertexColo));
		}
	}
	
	
	private void generateRDFTypeEdges(Map<BitSet, IntSet> mMapColourToVertexIDsForTypeEdges) {
		// Check number of remaining edges to add
		int actualEdgesMimicGraph = mMimicGraph.getEdges().size();
		int remainingEdges = mIDesiredNoOfEdges - actualEdgesMimicGraph;
				
		int actualVerticesMimicGraph = mMimicGraph.getVertices().size();
		int remainingVertices = mIDesiredNoOfVertices - actualVerticesMimicGraph;
				
		if ((remainingEdges < 0) || (remainingVertices < 0)) {
			//throw new RuntimeException("Cannot Add RDF Type edges!, Remaining Vertices (to add): " + remainingVertices + ", Remaining Edges (to add): " + remainingEdges);
			LOGGER.warn("Cannot Add RDF Type edges!, Remaining Vertices (to add): " + remainingVertices + ", Remaining Edges (to add): " + remainingEdges);
		}
		
		// Variable to track number of rdf type edges & vertices for these edges added to the mimic graph
		int trackerEdgesCount = 0;
					
		// temporary variable to track colors
		Set<BitSet> classColorsCreateVertices = new HashSet<BitSet>();
		
		//create a set for vertex ids for which the type edges need to be added
		IntSet vertexIdsTypeEdges = new DefaultIntSet(Constants.DEFAULT_SIZE);
		
					
		// iterate over every vertex colour and get all class colors
		for(BitSet vertexColor : mMapColourToVertexIDsForTypeEdges.keySet()) {
			Set<BitSet> classColours = mMimicGraph.getClassColour(vertexColor);
			classColorsCreateVertices.addAll(classColours);
			vertexIdsTypeEdges.addAll(mMapColourToVertexIDsForTypeEdges.get(vertexColor));
		}
					
		// Remove existing found  class colors
		classColorsCreateVertices.removeAll(mMapClassColourToVertexIDSimplexes.keySet());
					
		if (classColorsCreateVertices.size() > remainingVertices)
			LOGGER.warn("More number of vertices are required for class nodes. Number of Vertices required: " + classColorsCreateVertices.size() + ", Remaining Vertices: " + remainingVertices);
			//throw new RuntimeException("More number of vertices are required for class nodes. Number of Vertices required: " + classColorsCreateVertices.size() + ", Remaining Vertices: " + remainingVertices);
		
		// commenting below since mMapColourToVertexIDs should store different vertex colors and not only class vertices
		// Add class vertices to mMapColourToVertexIDs
//		BitSet emptyBitset = new BitSet(); // empty bitset for storing nodes created for classes
//		IntSet vertexIDsClass = mMapColourToVertexIDs.get(emptyBitset);// get vertex ids for this empty bitset 
//		if (vertexIDsClass == null) {
//			vertexIDsClass = new DefaultIntSet(Constants.DEFAULT_SIZE);
//			mMapColourToVertexIDs.put(emptyBitset, vertexIDsClass);
//		}
					
		// create vertex for each class color and store the  in the map
		for(BitSet classColor: classColorsCreateVertices) {
			int classVertex = mMimicGraph.addVertex();
			mMapClassColourToVertexIDSimplexes.put(classColor, classVertex);
			mReversedMapClassVertices.put(classVertex, classColor); // reverse map of class color and vertex ids, required when optimizing the graph
			//vertexIDsClass.add(classVertex);//add vertices created for classes in mMapColourToVertexID // commenting this since mMapColourToVertexIDs should store different vertex colors and not only class vertices
		}
					
		// iterate over every vertex and connect it to vertices for class
		for (int vertexIdMimicGraph:vertexIdsTypeEdges) {
			// get colors for vertex id
			BitSet vertexColor = mMimicGraph.getVertexColour(vertexIdMimicGraph);
			
			// get class colors for vertex color
			Set<BitSet> classColoursForVertex = mMimicGraph.getClassColour(vertexColor);
			
			//Add Type edge for every class color
			for (BitSet classColor: classColoursForVertex) {
				if (trackerEdgesCount > remainingEdges) {
					LOGGER.info("While adding RDF Types, maximum number of edges are added");
					//return;
				}
				int classVertexId = mMapClassColourToVertexIDSimplexes.get(classColor);
				mMimicGraph.addEdge(vertexIdMimicGraph, classVertexId, mRdfTypePropertyColour);
				trackerEdgesCount++;
			}
		}
	}
	
	/**
	 * The method adds a triangle to mimic graph for the input TriangleColours Object. Edge colors are selected for the given vertex colors of the triangle to create edge and form the complete triangle
	 * @param inputTriangleColours - TriangleColours Object
	 */
	private void addTriangleToMimicGraph(TriColos inputTriangleColours, IColourMappingRules mColourMapperToUse, Map<BitSet, IntSet> mMapColourToVertexIDsToUpdate, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate
			, ObjectObjectOpenHashMap<TriColos, List<IntSet>> mTriangleColorsVertexIdsToUpdate) {
		// storing colors of vertices for initial triangle
		BitSet vertex1Color = inputTriangleColours.getA();
		BitSet vertex2Color = inputTriangleColours.getB();
		BitSet vertex3Color = inputTriangleColours.getC();
					
		// create vertex for the triangle colors in the mimic graph
		int vert1Id = addVertexToMimicGraph(vertex1Color, mMapColourToVertexIDsToUpdate);
		int vert2Id = addVertexToMimicGraph(vertex2Color, mMapColourToVertexIDsToUpdate);
		int vert3Id = addVertexToMimicGraph(vertex3Color, mMapColourToVertexIDsToUpdate);
		
		// Add edges between found vertices
		addEdgeTriangle(vertex1Color, vertex2Color, vert1Id, vert2Id, mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours);
		addEdgeTriangle(vertex1Color, vertex3Color, vert1Id, vert3Id, mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours);
		addEdgeTriangle(vertex2Color, vertex3Color, vert2Id, vert3Id, mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours);
		
		// update the map for trackign the colours of the triangle
		updateMapTriangleColorsVertices(vert1Id, vert2Id, vert3Id, inputTriangleColours, mTriangleColorsVertexIdsToUpdate);
		
	}
	
	/**
	 * This method is used to update the map that tracks the triangle colours and vertices for those colours.
	 * @param vert1Id - Integer vertex id for the first vertex of the triangle
	 * @param vert2Id - Integer vertex id  for the second vertex of the triangle
	 * @param vert3Id - Integer vertex id for the third vertex of the triangle
	 * @param inputTriangleColours - TriangleColours object with the colours for the vertices of the triangle
	 */
	private void updateMapTriangleColorsVertices(int vert1Id, int vert2Id, int vert3Id, TriColos inputTriangleColours, ObjectObjectOpenHashMap<TriColos, List<IntSet>> mTriColVertexIDsToUpdate) {
		
		//list of vertex ids, initially checks if the vertex ids exists for the input triangle colours
		List<IntSet> tempVerticesList = mTriColVertexIDsToUpdate.get(inputTriangleColours);
		if (tempVerticesList == null) {
			tempVerticesList = new ArrayList<IntSet>();
		}
		
		// create set for vertex ids
		IntSet verticesOfNewTriangle = new DefaultIntSet(Constants.DEFAULT_SIZE);
		verticesOfNewTriangle.add(vert1Id);
		verticesOfNewTriangle.add(vert2Id);
		verticesOfNewTriangle.add(vert3Id);
		
		tempVerticesList.add(verticesOfNewTriangle);
		mTriColVertexIDsToUpdate.put(inputTriangleColours, tempVerticesList);
	}
	
	/**
	 * The method adds a vertex of the input color to the mimic graph and returns the vertex id. It also updates a map, which stores the mapping of vertex colors and vertex IDs.
	 * @param vertexColor - BitSet for the vertex color
	 * @return - vertex id
	 */
	private int addVertexToMimicGraph(BitSet vertexColor, Map<BitSet, IntSet> mMapColourToVertexIDsToUpdate) {
		int vertId = mMimicGraph.addVertex(vertexColor);
		IntSet setVertIDs = mMapColourToVertexIDsToUpdate.get(vertexColor); //mMapColourToVertexIDs.get(vertexColor);
		if(setVertIDs == null){
			setVertIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
			mMapColourToVertexIDsToUpdate.put(vertexColor, setVertIDs); //mMapColourToVertexIDs.put(vertexColor, setVertIDs);
			mMapColourToVertexIDs.put(vertexColor, setVertIDs); // updating this map, required when refining the graph
		}
		setVertIDs.add(vertId);
		
		return vertId;
	}
	
	/**
	 * 
	 * This method add an edge for the input vertex 1 and vertex 2. It utilizes colormapper object to determine edge color and the head and tail vertex.
	 * For the edge color found, an edge is added between vertex 1 and vertex 2, and the edge id is stored with its color information in the map.
	 * @param inputVertex1Colo - Color for input vertex 1.
	 * @param inputVertex2Colo - Color for input vertex 2.
	 * @param inputVertex1ID - ID for input vertex 1.
	 * @param inputVertex2ID - ID for input vertex 2.
	 */
//	private void addEdgeToMimicGraph(BitSet inputVertex1Colo, BitSet inputVertex2Colo, int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse) {
//		boolean isEdgeFromFirstToSecondVertex = true;
//		// Get edge between vertex1 and vertex 2, assuming vertex 1 is tail and vertex 2 is head
//		Set<BitSet> possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex1Colo, inputVertex2Colo);
//		if (possEdgeColov1tailv2head.size() == 0) {
//			// When vertex 1 is not tail and vertex 2 is not head
//			// get edge assuming vertex 1 is head and vertex 2 is tail
//			possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex2Colo, inputVertex1Colo);
//			isEdgeFromFirstToSecondVertex = false;
//		}
//		
//		
//		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices
//			
//		// randomly select edge colo
//		BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];
//		
//		// add the edge to mimic graph
//		int edgeIdTemp;
//		if (isEdgeFromFirstToSecondVertex)
//			edgeIdTemp = mMimicGraph.addEdge(inputVertex1ID, inputVertex2ID, randomEdgeColov1v2);
//		else
//			edgeIdTemp = mMimicGraph.addEdge(inputVertex2ID, inputVertex1ID, randomEdgeColov1v2);
//		
//		// Update or Add to the mapping of edge color and edge Id
//		// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared to previously developed generators. 
//		IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);//mMapColourToEdgeIDs.get(randomEdgeColov1v2);
//		if (setOfEdgeIds == null) {
//			setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
//			mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); //mMapColourToEdgeIDs.put(randomEdgeColov1v2, setOfEdgeIds);
//		}
//		setOfEdgeIds.add(edgeIdTemp);
//		
//		}else {
//			LOGGER.warn("No edge could be found for vertices with Colors: " + inputVertex1Colo + ", " + inputVertex2Colo);
//		}
//	}
	
	/**
	 * 
	 * This method add an edge for the input vertex 1 and vertex 2. It utilizes colormapper object to determine edge color and the head and tail vertex.
	 * For the edge color found, an edge is added between vertex 1 and vertex 2, and the edge id is stored with its color information in the map.
	 * In contrast with earlier method, this method specifically evaluates the existing edge colors between input vertices and does not add an edge with duplicate color
	 * @param inputVertex1Colo - Color for input vertex 1.
	 * @param inputVertex2Colo - Color for input vertex 2.
	 * @param inputVertex1ID - ID for input vertex 1.
	 * @param inputVertex2ID - ID for input vertex 2.
	 */
	private boolean addEdgeTriangle(BitSet inputVertex1Colo, BitSet inputVertex2Colo, int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse, TriColos inputTriangleColours) {
		boolean isEdgeFromFirstToSecondVertex = true;
		// Get edge between vertex1 and vertex 2, assuming vertex 1 is tail and vertex 2 is head
		Set<BitSet> possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex1Colo, inputVertex2Colo);
		if (possEdgeColov1tailv2head.size() == 0) {
			// When vertex 1 is not tail and vertex 2 is not head
			// get edge assuming vertex 1 is head and vertex 2 is tail
			possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex2Colo, inputVertex1Colo);
			isEdgeFromFirstToSecondVertex = false;
		}
		
		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices
			
		// randomly select edge colo
		BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];
		
		// Get the map storing edge colors and corresponding tail and head ids
		Map<Integer, IntSet> mTailHead = mMapEdgeColoursToConnectedVertices.get(randomEdgeColov1v2);
		if (mTailHead == null) {
			mTailHead = new HashMap<Integer, IntSet>();
		}
		
		// initialize head ids for the map
		IntSet headIds = null;
		
		// add the edge to mimic graph
		int edgeIdTemp;
		if (isEdgeFromFirstToSecondVertex) {
			edgeIdTemp = mMimicGraph.addEdge(inputVertex1ID, inputVertex2ID, randomEdgeColov1v2);
			
			// get existing head ids if present
			headIds = mTailHead.get(inputVertex1ID);
			if (headIds == null) {
				headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
			}
			headIds.add(inputVertex2ID);
			
			//update the map for tail id and head ids
			mTailHead.put(inputVertex1ID, headIds);
			
		}
		else {
			edgeIdTemp = mMimicGraph.addEdge(inputVertex2ID, inputVertex1ID, randomEdgeColov1v2);
			
			
			headIds = mTailHead.get(inputVertex2ID);
			if (headIds == null) {
				headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
			}
			headIds.add(inputVertex1ID);
			
			mTailHead.put(inputVertex2ID, headIds);
			
		}
		
		mMapEdgeColoursToConnectedVertices.put(randomEdgeColov1v2, mTailHead);
		
		
		
		// Update or Add to the mapping of edge color and edge Id
		// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared to previously developed generators. 
		IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);//mMapColourToEdgeIDs.get(randomEdgeColov1v2);
		if (setOfEdgeIds == null) {
			setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
			mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); //mMapColourToEdgeIDs.put(randomEdgeColov1v2, setOfEdgeIds);
		}
		setOfEdgeIds.add(edgeIdTemp);
		
		return true;
		
		}else {
			return false;
		}
	}
	
	/**
	 * 
	 * This method add an edge for the input vertex 1 and vertex 2. It utilizes colormapper object to determine edge color and the head and tail vertex.
	 * For the edge color found, an edge is added between vertex 1 and vertex 2, and the edge id is stored with its color information in the map.
	 * In contrast with earlier method, this method specifically evaluates the existing edge colors between input vertices and does not add an edge with duplicate color
	 * @param inputVertex1Colo - Color for input vertex 1.
	 * @param inputVertex2Colo - Color for input vertex 2.
	 * @param inputVertex1ID - ID for input vertex 1.
	 * @param inputVertex2ID - ID for input vertex 2.
	 */
	private boolean addEdgeInAnyDirectionWithDuplicateCheck(BitSet inputVertex1Colo, BitSet inputVertex2Colo, int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse) {
		boolean isEdgeFromSecondToFirstVertex = true;
		// Get edge between vertex1 and vertex 2, assuming vertex 1 is tail and vertex 2 is head
		Set<BitSet> possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex2Colo, inputVertex1Colo);
		if (possEdgeColov1tailv2head.size() == 0) {
			// When vertex 1 is not tail and vertex 2 is not head
			// get edge assuming vertex 1 is head and vertex 2 is tail
			possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex1Colo, inputVertex2Colo);
			isEdgeFromSecondToFirstVertex = false;
		}
		
		// Check for duplicate edge color if it is essential
		// Note: This check is not required when a triangle is created for the first time or a edge is created between vertices for the first time
		possEdgeColov1tailv2head = removeDuplicateEdgeColors(inputVertex2ID, inputVertex1ID, possEdgeColov1tailv2head);	
		
		// when no edge can be added and second assumption is not evaluated (vertex 1 - head and vertex 2 - tail) get edge colors for the second case
		if ((possEdgeColov1tailv2head.size() == 0) && isEdgeFromSecondToFirstVertex) {
			possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex1Colo, inputVertex2Colo);
			isEdgeFromSecondToFirstVertex = false;
				
			possEdgeColov1tailv2head = removeDuplicateEdgeColors(inputVertex2ID, inputVertex1ID, possEdgeColov1tailv2head);
		} 
		
		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices
			
		// randomly select edge colo
		BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];
		
		// Get the map storing edge colors and corresponding tail and head ids
		Map<Integer, IntSet> mTailHead = mMapEdgeColoursToConnectedVertices.get(randomEdgeColov1v2);
		if (mTailHead == null) {
			mTailHead = new HashMap<Integer, IntSet>();
		}
		
		// initialize head ids for the map
		IntSet headIds = null;
		
		// add the edge to mimic graph
		int edgeIdTemp;
		if (isEdgeFromSecondToFirstVertex) {
			edgeIdTemp = mMimicGraph.addEdge(inputVertex2ID, inputVertex1ID, randomEdgeColov1v2);
			
			// get existing head ids if present
			headIds = mTailHead.get(inputVertex2ID);
			if (headIds == null) {
				headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
			}
			headIds.add(inputVertex1ID);
						
			//update the map for tail id and head ids
			mTailHead.put(inputVertex2ID, headIds);
		}
		else {
			edgeIdTemp = mMimicGraph.addEdge(inputVertex1ID, inputVertex2ID, randomEdgeColov1v2);
			
			headIds = mTailHead.get(inputVertex1ID);
			if (headIds == null) {
				headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
			}
			headIds.add(inputVertex2ID);
			
			mTailHead.put(inputVertex1ID, headIds);
		}
		
		mMapEdgeColoursToConnectedVertices.put(randomEdgeColov1v2, mTailHead);
		
		// Update or Add to the mapping of edge color and edge Id
		// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared to previously developed generators. 
		IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);//mMapColourToEdgeIDs.get(randomEdgeColov1v2);
		if (setOfEdgeIds == null) {
			setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
			mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); //mMapColourToEdgeIDs.put(randomEdgeColov1v2, setOfEdgeIds);
		}
		setOfEdgeIds.add(edgeIdTemp);
		
		return true;
		
		}else {
			return false;
		}
	}
	
	/**
	 * The method returns true if the input vertices have one or more vertices in common.
	 * @param headID
	 * @param tailID
	 * @return
	 */
	public boolean commonVertices(int headID, int tailID) {
		// get vertices incident on input ids
		IntSet verticesIncidentHead = IntSetUtil.union(mMimicGraph.getInNeighbors(headID), mMimicGraph.getOutNeighbors(headID));
		IntSet verticesIncidentTail = IntSetUtil.union(mMimicGraph.getInNeighbors(tailID), mMimicGraph.getOutNeighbors(tailID));
		
		//find vertices incident to both
		IntSet commonVertices = IntSetUtil.intersection(verticesIncidentHead, verticesIncidentTail);
		//do not consider class vertices
		Set<Integer> classVertices = mReversedMapClassVertices.keySet();
		Set<Integer> commonVerticesSet = new HashSet<Integer>();
		for (int vertexId:commonVertices)
			commonVerticesSet.add(vertexId);
		commonVerticesSet = Sets.difference(commonVerticesSet, classVertices);
		if (commonVerticesSet.size() > 0)
			return true;
		return false;
	}
	
	
	/**
	 * This method adds a unique edge between given head and tail ids. Additionally, it will check that edge should be added only such that no triangles are formed between input head and tail ids if the last parameter of the method is true.
	 * @param headColo - input color for the head vertex.
	 * @param tailColo - input color for the tail vertex.
	 * @param headID - id for the head vertex.
	 * @param tailID - id for the tail vertex.
	 * @param mMapColourToEdgeIDsToUpdate - map to update the edge color and ids if edge is added.
	 * @param mColourMapperToUse - Color mapper to use for edge colors.
	 * @param triangleCheck - boolean variable to indicate if it should check that triangle is not formed. When set to true the edge is added between input vertices only if it does not form a triangle.
	 * @return - will return true if edge is added to the mimic graph else false.
	 */
	private boolean addEdgeWithTriangleCheck(BitSet headColo, BitSet tailColo, int headID, int tailID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse, boolean triangleCheck) {
		// Get edge between head and tail, assuming vertex 1 is tail and vertex 2 is head
		Set<BitSet> possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(tailColo, headColo);
		
		// Check for duplicate edge color if it is essential
		// Note: This check is not required when a triangle is created for the first time or a edge is created between vertices for the first time
		possEdgeColov1tailv2head = removeDuplicateEdgeColors(tailID, headID, possEdgeColov1tailv2head);	
		
		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices
			
			// check the head id and tail id does not have a vertex in common. Adding an edge could form a triangle
			if (triangleCheck) {
				if (commonVertices(headID, tailID))
					return false;//do not add an edge and return false. if input vertices have a vertex in common.
			}
			
			// randomly select edge colo
			BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];
			
			// add the edge to mimic graph
			int edgeIdTemp;
			edgeIdTemp = mMimicGraph.addEdge(tailID, headID, randomEdgeColov1v2);
			
			// update the map of edge colors and tail, head IDs
			updateMappingOfEdgeColoHeadTailColo(randomEdgeColov1v2, headID, tailID);
			
			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared to previously developed generators. 
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);//mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); //mMapColourToEdgeIDs.put(randomEdgeColov1v2, setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);
			
			return true;
		
		}else {
			return false;
		}
	}
	
	/**
	 * This method updates the global map for Edge color => tail ID => head IDs
	 * @param possEdgeColo
	 * @param headID
	 * @param tailID
	 */
	public void updateMappingOfEdgeColoHeadTailColo(BitSet possEdgeColo, int headID, int tailID) {
		// Get the map storing edge colors and corresponding tail and head ids
		Map<Integer, IntSet> mTailHead = mMapEdgeColoursToConnectedVertices.get(possEdgeColo);
		if (mTailHead == null) {
			mTailHead = new HashMap<Integer, IntSet>();
		}
								
		// initialize head ids for the map
		IntSet headIds =  mTailHead.get(tailID);
		if (headIds == null) {
			headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
		}
		headIds.add(headID);
									
		//update the map for tail id and head ids
		mTailHead.put(tailID, headIds);
					
		mMapEdgeColoursToConnectedVertices.put(possEdgeColo, mTailHead);
	}
	
	
	/**
	 * For the input set of edge colors between the given vertices, the function removes edge colors that already exist among input vertices. It returns the updated set.
	 * @param inputVertex1ID - input vertex id 1
	 * @param inputVertex2ID - input vertex id 2
	 * @param possEdgeColov1tailv2head - possible set of edge colors between input vertices
	 * @return - updated input set with not already existing edge colors
	 */
	private Set<BitSet> removeDuplicateEdgeColors(int inputVertex1ID, int inputVertex2ID, Set<BitSet> possEdgeColov1tailv2head) {
		// check existing edge colors between vertices and remove them from the possible edge colors set
		IntSet edgesIncidentVert1 = mMimicGraph.getEdgesIncidentTo(inputVertex1ID);
		IntSet edgesIncidentVert2 = mMimicGraph.getEdgesIncidentTo(inputVertex2ID);
		IntSet edgesIncidentExistingVertices = IntSetUtil.intersection(edgesIncidentVert1, edgesIncidentVert2);
					
		Set<BitSet> existingEdgeColours = new HashSet<BitSet>();
		for(int existingEdgeId : edgesIncidentExistingVertices) {
			existingEdgeColours.add(mMimicGraph.getEdgeColour(existingEdgeId));
		}
					
		// Difference between possible colors and existing colors
		// TODO: Create a BitSetUtil? Similar to IntSetUtil.
		SetView<BitSet> differenceSet = Sets.difference(possEdgeColov1tailv2head, existingEdgeColours);
		possEdgeColov1tailv2head = new HashSet<BitSet>();
		possEdgeColov1tailv2head.addAll(differenceSet);
		
		return possEdgeColov1tailv2head;
	}
	
	/**
	 * The method selects a random triangle by using Random object. It will return null, if no triangles are found in the input graphs.
	 * 
	 * @return - Triangle Colours
	 * 			 Set of colors for each vertex of a randomly selected triangle along with their occurrence count in input graphs.
	 */
	private TriColos getRandomTriangle() {
		TriColos randomTriangleColor = null;
		
		if (setAllTriangleColours.size() != 0) {
			randomTriangleColor = setAllTriangleColours.toArray(new TriColos[setAllTriangleColours.size()])[mRandom.nextInt(setAllTriangleColours.size())];
		}
		
		
		return randomTriangleColor;
	}
	
	private void removeTypeEdgesFromGraphs(ColouredGraph[] origGrphs) {
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				
				// get rdf type edge
				BitSet rdfTypePropertyColour = graph.getRDFTypePropertyColour();
				
				// get all edges
				IntSet allEdges = graph.getEdges();
		
				//Iterate over all edges
				for(int edgeId:allEdges) {
					// remove if rdf type edge
					if (graph.getEdgeColour(edgeId).equals(rdfTypePropertyColour)) {
						graph.removeEdge(edgeId);
					}
				}
			}
		}
	}
	
	private void computeTotalEdgesInGraphs(ColouredGraph[] origGrphs) {
		
		// temporary variable to track graph number
		int graphId = 1;
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				int edgesCount = graph.getEdges().size();
				mGraphEdges.put(graphId, edgesCount);
				graphId++;
			}
		}
	}
		
}
