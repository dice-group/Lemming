package org.aksw.simba.lemming.creation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.simplexes.analysis.FindTri;
import org.aksw.simba.lemming.simplexes.analysis.SimplexAnalysis;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

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

		return mimicGraph;
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

	/**
	 * Collects triangles found in the input graphs
	 * 
	 * @param origGrphs Input graphs
	 * @return Triangle information of the input graphs
	 */
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
}
