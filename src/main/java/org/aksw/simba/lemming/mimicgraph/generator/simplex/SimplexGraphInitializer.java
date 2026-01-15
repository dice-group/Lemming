package org.aksw.simba.lemming.mimicgraph.generator.simplex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.generator.binary.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.metricstorage.SimplexAnalysisResult;
import org.aksw.simba.lemming.mimicgraph.metricstorage.SimplexService;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.simplexes.analysis.FindTri;
import org.aksw.simba.lemming.simplexes.analysis.SimplexAnalysis;
import org.aksw.simba.lemming.util.Constants;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class is responsible for analyzing the simplexes from the input graphs
 * and initializing the mimic graph. It finds the simplexes in the input graphs
 * and learns distributions from these.
 * 
 */
@Component("simplex")
@Scope(value = "prototype")
public class SimplexGraphInitializer extends GraphInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimplexGraphInitializer.class);

	private int noOfIterations = 10000; // FIXME

	@Autowired
	private SimplexService service;

	/** Analyzes the different types of simplexes */
	private SimplexAnalysis simplexAnalysis;

	/** Map for tracking vertices denoting classes */
	private Map<BitSet, Integer> mMapClassColourToVertexIDSimplexes = new ConcurrentHashMap<BitSet, Integer>();

	/**
	 * Map for storing vertex colors of triangles as keys and array of triangle and
	 * edge counts as value.
	 */
	private ObjectObjectOpenHashMap<TriColours, double[]> mTriangleColoursTriangleEdgeCounts;

	/**
	 * Map for tracking triangle colors and vertex IDs forming triangles using those
	 * colors
	 */
	private ObjectObjectOpenHashMap<TriColours, List<IntSet>> mTriangleColorsVertexIds;

	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for
	 * 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs2Simplex = new ConcurrentHashMap<BitSet, IntSet>();

	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for
	 * 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToEdgeIDs2Simplex = new ConcurrentHashMap<BitSet, IntSet>();

	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for
	 * 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDsConnectedTo2Simplex = new ConcurrentHashMap<BitSet, IntSet>();

	/** Map for storing triangles along with their statistics */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> mTriColosCountsAvgProb;

	// **** Additional Simplex Analysis variables for isolated 2-simplexes ****

	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for isolated
	 * 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs2SimplexIsolated = new ConcurrentHashMap<BitSet, IntSet>();
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for isolated
	 * 2-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToEdgeIDs2SimplexIsolated = new ConcurrentHashMap<BitSet, IntSet>();
	/**
	 * Map for tracking triangle colors and vertex IDs forming isolated triangles
	 * using those colors
	 */
	private ObjectObjectOpenHashMap<TriColours, List<IntSet>> mIsolatedTriangleColorsVertexIds = new ObjectObjectOpenHashMap<TriColours, List<IntSet>>();

	// **** Additional Simplex Analysis variables for connected 1-simplexes ****

	/** Set for storing all edges */
	private Set<EdgeColos> setAllEdgeColours;
	/** Set for storing all triangles */
	private Set<TriColours> setAllTriangleColours;
	/** */
	private ObjectObjectOpenHashMap<EdgeColos, List<IntSet>> mEdgeColorsVertexIds;

	// **** Additional Simplex Analysis variables for isolated 1-simplexes ****

	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for
	 * 1-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs1Simplex = new ConcurrentHashMap<BitSet, IntSet>();
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for isolated
	 * self loop 1-simplexes (1-simplexes with same head and tail color)
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDsIsoSelfLoop = new ConcurrentHashMap<BitSet, IntSet>();
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value
	 */
	private Map<BitSet, IntSet> mMapColourToEdgeIDs1Simplex = new ConcurrentHashMap<BitSet, IntSet>();
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for connected
	 * 1-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs1SimplexConnected = new ConcurrentHashMap<BitSet, IntSet>();
	/**
	 * Map for storing vertex colours as Keys and Vertex IDs as Value for
	 * 0-simplexes
	 */
	private Map<BitSet, IntSet> mMapColourToVertexIDs0Simplex = new ConcurrentHashMap<BitSet, IntSet>();

	private int iNoOfVersions;

	/**
	 * Constructor
	 * 
	 * @param seedGenerator Seed Generator Object
	 */
	public SimplexGraphInitializer(SeedGenerator seedGenerator) {
		super(seedGenerator);
		mEdgeColorsVertexIds = new ObjectObjectOpenHashMap<EdgeColos, List<IntSet>>();
		mTriangleColorsVertexIds = new ObjectObjectOpenHashMap<TriColours, List<IntSet>>();
	}

	@Override
	public ColouredGraph initialize(ColouredGraph[] origGrphs, int noOfVertices, int noOfThreads) {
		// deep copy the graphs
		ColouredGraph[] clonedInput = Arrays.stream(origGrphs).map(ColouredGraph::clone).toArray(ColouredGraph[]::new);

		// copy the colour palette as in the normal modes
		ColouredGraph mimicGraph = super.init(clonedInput, noOfVertices);

		// set number of input graphs
		iNoOfVersions = clonedInput.length;

		// Compute triangles for input graphs
		LOGGER.info("Finding triangles in input graphs");
		FindTri computedTriangles = callMetricToGetTriangleInformation(origGrphs);

		// Get all triangles found in input graphs. Note:- metric is invoked by above
		// function call, thus set of colors for different triangle vertices are already
		// computed.

		mTriangleColoursTriangleEdgeCounts = computedTriangles.getmTriColoEdgesTriCountDistAvg();

		// Create HashSet of Triangle Colours. This set is used to randomly select an
		// object of TriangleColours while generating mimic graph.
		createSetForTriangleColours();

		// analyze simplexes identified from the triangles in the graph
		LOGGER.info("Find simplexes from triangles");
		simplexAnalysis = new SimplexAnalysis(clonedInput, noOfVertices, iNoOfVersions, computedTriangles);

		// compute distinct edge colors for random pick
		createSetForEdgeColours(simplexAnalysis.getConnS1Analysis().getmColoEdgesCountDistAvg());

//		// Compute triangles for input graphs
//		LOGGER.info("Finding triangles in input graphs");
////		SimplexAnalysisResult result = service.obtainSimplexAnalysis(clonedInput, noOfVertices, noOfThreads);
////		simplexAnalysis = result.getSimplex();
////		FindTri computedTriangles = result.getFindTri();
//		
//		// Get all triangles found in input graphs. Note:- metric is invoked by above
//		// function call, thus set of colors for different triangle vertices are already
//		// computed.
//		mTriangleColoursTriangleEdgeCounts = computedTriangles.getmTriColoEdgesTriCountDistAvg();
//
//		// Create HashSet of Triangle Colours. This set is used to randomly select an
//		// object of TriangleColours while generating mimic graph.
//		createSetForTriangleColours();
//					
//		// compute distinct edge colors for random pick
//		createSetForEdgeColours(simplexAnalysis.getConnS1Analysis().getmColoEdgesCountDistAvg());

		return mimicGraph;
	}

	private FindTri callMetricToGetTriangleInformation(ColouredGraph[] origGrphs) {
		FindTri findTriObj = new FindTri();
		for (ColouredGraph graph : origGrphs) {
			if (graph != null) {
				findTriObj.computeTriangles(graph);
			}
		}
		return findTriObj;
	}

	/**
	 * Computes the distinct edge colors for random pick
	 */
	private void createSetForEdgeColours(ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgesColorsCountDistAvg) {
		setAllEdgeColours = new HashSet<EdgeColos>();
		Object[] keysEdgeColors = mEdgesColorsCountDistAvg.keys;
		for (int i = 0; i < keysEdgeColors.length; i++) {
			if (mEdgesColorsCountDistAvg.allocated[i]) {
				EdgeColos edgeColorObj = (EdgeColos) keysEdgeColors[i];
				setAllEdgeColours.add(edgeColorObj);
			}
		}
	}

//	/**
//	 * Collects triangles found in the input graphs
//	 * 
//	 * @param origGrphs Input graphs
//	 * @return Triangle information of the input graphs
//	 */
//	private FindTri callMetricToGetTriangleInformation(ColouredGraph[] origGrphs) {
//		FindTri findTriObj = new FindTri();
//		for (ColouredGraph graph : origGrphs) {
//			if (graph != null) {
//				findTriObj.computeTriangles(graph);
//			}
//		}
//		return findTriObj;
//	}

	/**
	 * Computes the distinct triangles colors
	 */
	private void createSetForTriangleColours() {
		setAllTriangleColours = new HashSet<TriColours>();
		Object[] keysTriangleColours = mTriangleColoursTriangleEdgeCounts.keys;
		for (int i = 0; i < keysTriangleColours.length; i++) {
			if (mTriangleColoursTriangleEdgeCounts.allocated[i]) {
				TriColours triangleColorObj = (TriColours) keysTriangleColours[i];
				setAllTriangleColours.add(triangleColorObj);
			}
		}
	}

	// *********** Getters ***********

	public Map<BitSet, Integer> getmMapClassColourToVertexIDSimplexes() {
		return mMapClassColourToVertexIDSimplexes;
	}

	public ObjectObjectOpenHashMap<TriColours, double[]> getmTriangleColoursTriangleEdgeCounts() {
		return mTriangleColoursTriangleEdgeCounts;
	}

	public Set<TriColours> getSetAllTriangleColours() {
		return setAllTriangleColours;
	}

	public ObjectObjectOpenHashMap<TriColours, List<IntSet>> getmTriangleColorsVertexIds() {
		return mTriangleColorsVertexIds;
	}

	public Map<BitSet, IntSet> getmMapColourToVertexIDs2Simplex() {
		return mMapColourToVertexIDs2Simplex;
	}

	public Map<BitSet, IntSet> getmMapColourToEdgeIDs2Simplex() {
		return mMapColourToEdgeIDs2Simplex;
	}

	public Map<BitSet, IntSet> getmMapColourToVertexIDsConnectedTo2Simplex() {
		return mMapColourToVertexIDsConnectedTo2Simplex;
	}

	public ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> getmTriColosCountsAvgProb() {
		return mTriColosCountsAvgProb;
	}

	public Map<BitSet, IntSet> getmMapColourToVertexIDs2SimplexIsolated() {
		return mMapColourToVertexIDs2SimplexIsolated;
	}

	public Map<BitSet, IntSet> getmMapColourToEdgeIDs2SimplexIsolated() {
		return mMapColourToEdgeIDs2SimplexIsolated;
	}

	public ObjectObjectOpenHashMap<TriColours, List<IntSet>> getmIsolatedTriangleColorsVertexIds() {
		return mIsolatedTriangleColorsVertexIds;
	}

	public Set<EdgeColos> getSetAllEdgeColours() {
		return setAllEdgeColours;
	}

	public Set<BitSet> getAvailableEdgeColours() {
		return getmMapEdgeColoursToConnectedVertices().keySet();
	}

	public ObjectObjectOpenHashMap<EdgeColos, List<IntSet>> getmEdgeColorsVertexIds() {
		return mEdgeColorsVertexIds;
	}

	public Map<BitSet, IntSet> getmMapColourToVertexIDs1Simplex() {
		return mMapColourToVertexIDs1Simplex;
	}

	public Map<BitSet, IntSet> getmMapColourToVertexIDsIsoSelfLoop() {
		return mMapColourToVertexIDsIsoSelfLoop;
	}

	public Map<BitSet, IntSet> getmMapColourToEdgeIDs1Simplex() {
		return mMapColourToEdgeIDs1Simplex;
	}

	public Map<BitSet, IntSet> getmMapColourToVertexIDs1SimplexConnected() {
		return mMapColourToVertexIDs1SimplexConnected;
	}

	public Map<BitSet, IntSet> getmMapColourToVertexIDs0Simplex() {
		return mMapColourToVertexIDs0Simplex;
	}

	public SimplexAnalysis getSimplexAnalysis() {
		return simplexAnalysis;
	}

	public int getiNoOfVersions() {
		return iNoOfVersions;
	}

	public int getMaximumNoIterations() {
		return noOfIterations;
	}

	public void setmTriColosCountsAvgProb(
			ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> mTriColosCountsAvgProb) {
		this.mTriColosCountsAvgProb = mTriColosCountsAvgProb;
	}

	/**
	 * This method updates the global map for Edge color => tail ID => head IDs
	 * 
	 * @param possEdgeColo
	 * @param headID
	 * @param tailID
	 */
	public void updateMappingOfEdgeColoHeadTailColo(BitSet possEdgeColo, int headID, int tailID) {
		// Get the map storing edge colors and corresponding tail and head ids
		Map<Integer, IntSet> mTailHead = mapEdgeColoursToConnectedVertices.get(possEdgeColo);
		if (mTailHead == null) {
			mTailHead = new HashMap<Integer, IntSet>();
		}

		// initialize head ids for the map
		IntSet headIds = mTailHead.get(tailID);
		if (headIds == null) {
			headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
		}
		headIds.add(headID);

		// update the map for tail id and head ids
		mTailHead.put(tailID, headIds);

		mapEdgeColoursToConnectedVertices.put(possEdgeColo, mTailHead);
	}

//	@Override
//	public void connectVerticesWithRDFTypeEdges(ColouredGraph mimicGraph) {
//		generateRDFTypeEdges(mimicGraph, mMapColourToVertexIDs2Simplex); 
//		generateRDFTypeEdges(mimicGraph, mMapColourToVertexIDs1Simplex);
//		generateRDFTypeEdges(mimicGraph, mMapColourToVertexIDs0Simplex); 
//		generateRDFTypeEdges(mimicGraph, mMapColourToVertexIDs1SimplexConnected);
//		generateRDFTypeEdges(mimicGraph, mMapColourToVertexIDsConnectedTo2Simplex);
//
//	}
//
//	private void generateRDFTypeEdges(ColouredGraph mimicGraph, Map<BitSet, IntSet> mMapColourToVertexIDsForTypeEdges) {
//		// temporary variable to track colors
//		Set<BitSet> classColorsCreateVertices = new HashSet<BitSet>();
//
//		// create a set for vertex ids for which the type edges need to be added
//		IntSet vertexIdsTypeEdges = new DefaultIntSet(Constants.DEFAULT_SIZE);
//
//		// iterate over every vertex colour and get all class colors
//		for (BitSet vertexColor : mMapColourToVertexIDsForTypeEdges.keySet()) {
//			Set<BitSet> classColours = mimicGraph.getClassColour(vertexColor);
//			classColorsCreateVertices.addAll(classColours);
//			vertexIdsTypeEdges.addAll(mMapColourToVertexIDsForTypeEdges.get(vertexColor));
//		}
//
//		// Remove existing found class colors
//		classColorsCreateVertices.removeAll(mMapClassColourToVertexIDSimplexes.keySet());
//
//		// create vertex for each class color and store the in the map
//		for (BitSet classColor : classColorsCreateVertices) {
//			int classVertex = mimicGraph.addVertex();
//			mMapClassColourToVertexIDSimplexes.put(classColor, classVertex);
//			reversedMapClassVertices.put(classVertex, classColor); // reverse map of class color and vertex ids
//		}
//
//		// iterate over every vertex and connect it to vertices for class
//		for (int vertexIdMimicGraph : vertexIdsTypeEdges) {
//			// get colors for vertex id
//			BitSet vertexColor = mimicGraph.getVertexColour(vertexIdMimicGraph);
//
//			// get class colors for vertex color
//			Set<BitSet> classColoursForVertex = mimicGraph.getClassColour(vertexColor);
//
//			// Add Type edge for every class color
//			for (BitSet classColor : classColoursForVertex) {
//				int classVertexId = mMapClassColourToVertexIDSimplexes.get(classColor);
//				mimicGraph.addEdge(vertexIdMimicGraph, classVertexId, rdfTypePropertyColour);
//			}
//		}
//	}

}
