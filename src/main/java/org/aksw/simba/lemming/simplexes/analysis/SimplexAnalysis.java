package org.aksw.simba.lemming.simplexes.analysis;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

public class SimplexAnalysis {

	/** */
	private ConnS2 connTriAnalysis;
	/** */
	private ConnS1C connS1Analysis;
	/** */
	private S1ConnToS2C s1ConnToTri;
	/** */
	private S1ConnectingS2C s1ConnectingTri;
	/** */
	private FindSelfLoops selfLoopIsoTri;
	/** */
	private FindSelfLoops selfLoopConnTri;
	/** */
	private FindSelfLoops selfLoopsInConnS1;
	/** */
	private FindSelfLoops selfLoops1ConnToTri;
	/** */
	private FindSelfLoops selfLoopsInIsoS1;
	/** */
	private IsoS2 isoTriAnalysis;
	/** */
	private IsoS1C isoS1Analysis;
	/** */
	private IsolatedSelfLoops isoS1SelfLoopAnalysis;
	/** */
	private S0C s0Analysis;

	/**
	 * 
	 * @param origGrphs
	 * @param noOfVertices
	 * @param iNoOfVersions
	 * @param computedTriangles
	 */
	public SimplexAnalysis(ColouredGraph[] origGrphs, int noOfVertices, int iNoOfVersions, FindTri computedTriangles) {
		connTriAnalysis = new ConnS2(origGrphs, noOfVertices, iNoOfVersions, computedTriangles);
		connTriAnalysis.computePropertyProbabilities();

		isoTriAnalysis = new IsoS2(origGrphs, noOfVertices, iNoOfVersions, computedTriangles);
		isoTriAnalysis.computePropertyProbabilities();

		s1ConnToTri = new S1ConnToS2C(origGrphs, noOfVertices, iNoOfVersions, computedTriangles);
		s1ConnToTri.computePropertyProbabilities();

		s1ConnectingTri = new S1ConnectingS2C(origGrphs, noOfVertices, iNoOfVersions, computedTriangles);
		s1ConnectingTri.computePropertyProbabilities();

		selfLoopIsoTri = new FindSelfLoops(origGrphs, noOfVertices, iNoOfVersions, isoTriAnalysis.getmGraphsVertIds());
		selfLoopIsoTri.computePropertyProbabilities();

		selfLoopConnTri = new FindSelfLoops(origGrphs, noOfVertices, iNoOfVersions,
				connTriAnalysis.getmGraphsVertIds());
		selfLoopConnTri.computePropertyProbabilities();

		selfLoops1ConnToTri = new FindSelfLoops(origGrphs, noOfVertices, iNoOfVersions,
				s1ConnToTri.getmGraphsVertIds());
		selfLoops1ConnToTri.computePropertyProbabilities();

		isoS1Analysis = new IsoS1C(origGrphs, noOfVertices, iNoOfVersions);
		isoS1Analysis.computePropertyProbabilities();

		isoS1SelfLoopAnalysis = new IsolatedSelfLoops(origGrphs, noOfVertices, iNoOfVersions);
		isoS1SelfLoopAnalysis.computePropertyProbabilities();

		selfLoopsInIsoS1 = new FindSelfLoops(origGrphs, noOfVertices, iNoOfVersions, isoS1Analysis.getmGraphsVertIds());
		selfLoopsInIsoS1.computePropertyProbabilities();

		s0Analysis = new S0C(origGrphs, noOfVertices, iNoOfVersions);
		ObjectObjectOpenHashMap<Integer, IntSet> edgeIdsUnionMap = addEdgeIdsForDifferentSimplexes(
				connTriAnalysis.getmGraphsEdgesIds(), isoTriAnalysis.getmGraphsEdgesIds(),
				s1ConnToTri.getmGraphsEdgesIds(), s1ConnectingTri.getmGraphsEdgesIds(),
				selfLoopIsoTri.getmGraphsEdgesIds(), selfLoopConnTri.getmGraphsEdgesIds(),
				selfLoops1ConnToTri.getmGraphsEdgesIds(), isoS1Analysis.getmGraphsEdgesIds(),
				isoS1SelfLoopAnalysis.getmGraphsEdgesIds(), selfLoopsInIsoS1.getmGraphsEdgesIds(), iNoOfVersions);
		
		connS1Analysis = new ConnS1C(origGrphs, noOfVertices, iNoOfVersions, edgeIdsUnionMap);
		connS1Analysis.computePropertyProbabilities();

		selfLoopsInConnS1 = new FindSelfLoops(origGrphs, noOfVertices, iNoOfVersions,
				connS1Analysis.getmGraphsVertIds());
		selfLoopsInConnS1.computePropertyProbabilities();

	}

	/**
	 * The function computes the union of edge ids found in different input graphs
	 * for various simplexes. The edge ids are provided as input maps.
	 * 
	 * @param m1            Map of graph to edge IDS
	 * @param m2            Map of graph to edge IDS
	 * @param m3            Map of graph to edge IDS
	 * @param m4            Map of graph to edge IDS
	 * @param m5            Map of graph to edge IDS
	 * @param m6            Map of graph to edge IDS
	 * @param m7            Map of graph to edge IDS
	 * @param m8            Map of graph to edge IDS
	 * @param m9            Map of graph to edge IDS
	 * @param m10           Map of graph to edge IDS
	 * @param iNoOfVersions Number of valid input graphs
	 * @return
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> addEdgeIdsForDifferentSimplexes(
			ObjectObjectOpenHashMap<Integer, IntSet> m1, ObjectObjectOpenHashMap<Integer, IntSet> m2,
			ObjectObjectOpenHashMap<Integer, IntSet> m3, ObjectObjectOpenHashMap<Integer, IntSet> m4,
			ObjectObjectOpenHashMap<Integer, IntSet> m5, ObjectObjectOpenHashMap<Integer, IntSet> m6,
			ObjectObjectOpenHashMap<Integer, IntSet> m7, ObjectObjectOpenHashMap<Integer, IntSet> m8,
			ObjectObjectOpenHashMap<Integer, IntSet> m9, ObjectObjectOpenHashMap<Integer, IntSet> m10,
			int iNoOfVersions) {
		ObjectObjectOpenHashMap<Integer, IntSet> outputMap = new ObjectObjectOpenHashMap<Integer, IntSet>();
		for (int graphId = 1; graphId <= iNoOfVersions; graphId++) {
			IntSet result1 = IntSetUtil.union(IntSetUtil.union(
					IntSetUtil.union(IntSetUtil.union(m1.get(graphId), m2.get(graphId)), m3.get(graphId)),
					m4.get(graphId)), m5.get(graphId));
			IntSet result2 = IntSetUtil.union(IntSetUtil.union(
					IntSetUtil.union(IntSetUtil.union(m6.get(graphId), m7.get(graphId)), m8.get(graphId)),
					m9.get(graphId)), m10.get(graphId));
			IntSet finalResult = IntSetUtil.union(result1, result2);
			outputMap.put(graphId, finalResult);
		}
		return outputMap;
	}

	public ConnS2 getConnTriAnalysis() {
		return connTriAnalysis;
	}

	public ConnS1C getConnS1Analysis() {
		return connS1Analysis;
	}

	public S1ConnToS2C getS1ConnToTri() {
		return s1ConnToTri;
	}

	public S1ConnectingS2C getS1ConnectingTri() {
		return s1ConnectingTri;
	}

	public FindSelfLoops getSelfLoopIsoTri() {
		return selfLoopIsoTri;
	}

	public FindSelfLoops getSelfLoopConnTri() {
		return selfLoopConnTri;
	}

	public FindSelfLoops getSelfLoopsInConnS1() {
		return selfLoopsInConnS1;
	}

	public FindSelfLoops getSelfLoops1ConnToTri() {
		return selfLoops1ConnToTri;
	}

	public FindSelfLoops getSelfLoopsInIsoS1() {
		return selfLoopsInIsoS1;
	}

	public IsoS2 getIsoTriAnalysis() {
		return isoTriAnalysis;
	}

	public IsoS1C getIsoS1Analysis() {
		return isoS1Analysis;
	}

	public IsolatedSelfLoops getIsoS1SelfLoopAnalysis() {
		return isoS1SelfLoopAnalysis;
	}

	public S0C getS0Analysis() {
		return s0Analysis;
	}

}
