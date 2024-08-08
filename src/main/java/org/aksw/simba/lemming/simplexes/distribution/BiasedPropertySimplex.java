package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.generator.SimplexGraphInitializer;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.analysis.SimplexAnalysis;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * 
 */
@Component("BPSI")
@Scope(value = "prototype")
public class BiasedPropertySimplex implements ISimplexProperty {

	private PropertyDistI mPropDistConnTri;
	private PropertyDistI mPropDistIsoTri;
	private PropertyDistI mPropDistS1ConnToTri;
	private PropertyDistI mPropDistS1ConnectingTri;
	private PropertyDistI mPropDistselfLoops1ConnToTri;
	private PropertyDistI mPropDistselfLoopConnTri;
	private PropertyDistI mPropDistselfLoopIsoTri;
	private PropertyDistI mPropDistconnS1Analysis;
	private PropertyDistI mPropDistselfLoopsInIsoS1;
	private PropertyDistI mPropDistisoS1SelfLoop;
	private PropertyDistI mPropDistisoS1;
	private PropertyDistI mPropDistSelfLoopConnS1;
	/** Store distributions of connected 1-simplexes */
	private EdgeDistI s1ConnDist;

	public BiasedPropertySimplex(SimplexGraphInitializer initializer) {
		SimplexAnalysis simplexAnalysis = initializer.getSimplexAnalysis();
		int iNoOfVersions = initializer.getiNoOfVersions();
		Random mRandom = new Random(initializer.getSeedGenerator().getNextSeed());

		mPropDistConnTri = new PropertyDistI(simplexAnalysis.getConnTriAnalysis().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistIsoTri = new PropertyDistI(simplexAnalysis.getIsoTriAnalysis().getmVertColosPropDist(), iNoOfVersions,
				mRandom);
		mPropDistS1ConnToTri = new PropertyDistI(simplexAnalysis.getS1ConnToTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistS1ConnectingTri = new PropertyDistI(simplexAnalysis.getS1ConnectingTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistS1ConnToTri = new PropertyDistI(simplexAnalysis.getS1ConnToTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistselfLoopIsoTri = new PropertyDistI(simplexAnalysis.getSelfLoopIsoTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistselfLoopConnTri = new PropertyDistI(simplexAnalysis.getSelfLoopConnTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistselfLoops1ConnToTri = new PropertyDistI(
				simplexAnalysis.getSelfLoops1ConnToTri().getmVertColosPropDist(), iNoOfVersions, mRandom);
		mPropDistisoS1 = new PropertyDistI(simplexAnalysis.getIsoS1Analysis().getmVertColosPropDist(), iNoOfVersions,
				mRandom);
		mPropDistisoS1SelfLoop = new PropertyDistI(simplexAnalysis.getIsoS1SelfLoopAnalysis().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistselfLoopsInIsoS1 = new PropertyDistI(simplexAnalysis.getSelfLoopsInIsoS1().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistconnS1Analysis = new PropertyDistI(simplexAnalysis.getConnS1Analysis().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistSelfLoopConnS1 = new PropertyDistI(simplexAnalysis.getSelfLoopsInConnS1().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		s1ConnDist = new EdgeDistI(simplexAnalysis.getConnS1Analysis().getmColoEdgesCountDistAvg(),
				simplexAnalysis.getIsoS1Analysis().getmColoEdgesCountDistAvg(), iNoOfVersions,
				initializer.getDesiredNoOfVertices(), mRandom);

	}

	public PropertyDistI getmPropDistConnTri() {
		return mPropDistConnTri;
	}

	public PropertyDistI getmPropDistIsoTri() {
		return mPropDistIsoTri;
	}

	public PropertyDistI getmPropDistS1ConnToTri() {
		return mPropDistS1ConnToTri;
	}

	public PropertyDistI getmPropDistS1ConnectingTri() {
		return mPropDistS1ConnectingTri;
	}

	public PropertyDistI getmPropDistselfLoops1ConnToTri() {
		return mPropDistselfLoops1ConnToTri;
	}

	public PropertyDistI getmPropDistselfLoopConnTri() {
		return mPropDistselfLoopConnTri;
	}

	public PropertyDistI getmPropDistselfLoopIsoTri() {
		return mPropDistselfLoopIsoTri;
	}

	public PropertyDistI getmPropDistconnS1Analysis() {
		return mPropDistconnS1Analysis;
	}

	public PropertyDistI getmPropDistselfLoopsInIsoS1() {
		return mPropDistselfLoopsInIsoS1;
	}

	public PropertyDistI getmPropDistisoS1SelfLoop() {
		return mPropDistisoS1SelfLoop;
	}

	public PropertyDistI getmPropDistisoS1() {
		return mPropDistisoS1;
	}

	public PropertyDistI getmPropDistSelfLoopConnS1() {
		return mPropDistSelfLoopConnS1;
	}

	public EdgeDistI getS1ConnDist() {
		return s1ConnDist;
	}

	@Override
	public BitSet proposeColour(EdgeColorsSorted edgeColors) {
		return mPropDistConnTri.proposePropColor(edgeColors);
	}

	@Override
	public OfferedItemByRandomProb<EdgeColos> getIsolatedEdgeProposer() {
		return s1ConnDist.getPotentialIsolatedEdgeColoProposer();
	}

	@Override
	public BitSet proposeColourForTri(EdgeColorsSorted edgeColors) {
		return mPropDistS1ConnectingTri.proposePropColor(edgeColors);
	}

	@Override
	public EdgeColos proposeConnEdge() {
		return s1ConnDist.getPotentialConnEdgeProposer().getPotentialItem();
	}

	@Override
	public ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> getEdgeColoursV1V2Dist() {
		return s1ConnDist.getmEdgeColorsv1v2();
	}

	@Override
	public BitSet proposeConnPropColour(EdgeColorsSorted edgeColorsSorted) {
		return mPropDistconnS1Analysis.proposePropColor(edgeColorsSorted);
	}

	@Override
	public EdgeColos proposeTriangleToAddEdge(Set<EdgeColos> setEdgeColorsMimicGraph) {
		return s1ConnDist.proposeTriangleToAddEdge(setEdgeColorsMimicGraph);
	}

	@Override
	public BitSet proposeS1ConnProp(EdgeColorsSorted edgeColorsSorted) {
		return mPropDistS1ConnToTri.proposePropColor(edgeColorsSorted);
	}
	


}
