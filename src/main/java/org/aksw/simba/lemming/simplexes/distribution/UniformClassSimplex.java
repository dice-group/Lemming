package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;
import org.aksw.simba.lemming.mimicgraph.generator.SimplexGraphInitializer;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.simplexes.analysis.SimplexAnalysis;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * 
 */
@Component("UCSI")
@Scope(value = "prototype")
public class UniformClassSimplex implements ISimplexClass {
	/** Distribution of vertices connected to triangles */
	private VertDistU s1connToTriVertDist;
	/** 0-simplexes */
	private VertDistU s0Dist;
	/** Distribution of self loops for isolated triangles */
	private VertDistU selfLoopIsoTritDist;
	/** Distribution of self loops for connected triangles */
	private VertDistU selfLoopConnTriDist;
	/** Distribution of self loops for vertices connected to triangles */
	private VertDistU selfLoops1ConnToTriDist;
	/** Distribution of isolated self loops */
	private VertDistU selfLoops1IsoS1;
	/** Distribution of self loops for vertices in isolated 1-simplexes */
	private VertDistU selfLoopsInS1Dist;
	/** Distribution of self loops for vertices in connected 1-simplexes */
	private VertDistU selfLoopsInConnS1Dist;
	/** Store distributions of 1-simplexes connected to triangles */
	private ConnS1DistU s1connToTriDist;
	/** Store distributions of 1-simplexes connecting triangles */
	private ConnS1DistU s1connTriDist;
	/** Store distributions of connected 1-simplexes */
	private EdgeDistU s1ConnDist;
	/** Store distributions of connected 1-simplexes */
	private EdgeDistUS s1ConnheadTailDist;
	/** Store distributions of isolated and connected 2-simplexes */
	private TriDistU triangleDistribution;
	
	private Random mRandom;
	
	private Set<EdgeColorsSorted> sortedEdgeColours;
	

	public UniformClassSimplex(SimplexGraphInitializer initializer)  {
		SimplexAnalysis simplexAnalysis = initializer.getSimplexAnalysis();
		int iNoOfVersions = initializer.getiNoOfVersions();
		int noOfVertices = initializer.getDesiredNoOfVertices();
		mRandom = new Random(initializer.getSeedGenerator().getNextSeed());
		selfLoopIsoTritDist = new VertDistU(simplexAnalysis.getSelfLoopIsoTri().getmColoCountSelfLoop(), iNoOfVersions,
				mRandom);
		selfLoopConnTriDist = new VertDistU(simplexAnalysis.getSelfLoopConnTri().getmColoCountSelfLoop(), iNoOfVersions,
				mRandom);
		selfLoops1ConnToTriDist = new VertDistU(simplexAnalysis.getSelfLoops1ConnToTri().getmColoCountSelfLoop(),
				iNoOfVersions, mRandom);
		selfLoops1IsoS1 = new VertDistU(simplexAnalysis.getIsoS1SelfLoopAnalysis().getmColoCountSelfLoop(),
				iNoOfVersions, mRandom);
		selfLoopsInS1Dist = new VertDistU(simplexAnalysis.getSelfLoopsInIsoS1().getmColoCountSelfLoop(), iNoOfVersions,
				mRandom);
		s0Dist = new VertDistU(simplexAnalysis.getS0Analysis().getmColoCount0Simplex(), iNoOfVersions, mRandom);
		selfLoopsInConnS1Dist = new VertDistU(simplexAnalysis.getSelfLoopsInConnS1().getmColoCountSelfLoop(),
				iNoOfVersions, mRandom);
		s1connToTriVertDist = new VertDistU(simplexAnalysis.getS1ConnToTri().getmColoCountVertConnectedToTriangle(),
				iNoOfVersions, mRandom);

		s1ConnDist = new EdgeDistU(simplexAnalysis.getConnS1Analysis().getmColoEdgesCountDistAvg(),
				simplexAnalysis.getIsoS1Analysis().getmColoEdgesCountDistAvg(), iNoOfVersions,
				initializer.getDesiredNoOfVertices(), mRandom);
		s1ConnheadTailDist = new EdgeDistUS(simplexAnalysis.getConnS1Analysis().getmHeadColoCountConnected1Simplex(),
				simplexAnalysis.getConnS1Analysis().getmHeadColoTailColoCountConnected(), iNoOfVersions, mRandom);

		s1connToTriDist = new ConnS1DistU(simplexAnalysis.getS1ConnToTri().getmColoEdgesCountDistAvg(), iNoOfVersions,
				mRandom);
		s1connTriDist = new ConnS1DistU(simplexAnalysis.getS1ConnectingTri().getmColoEdgesCountDistAvg(), iNoOfVersions,
				mRandom);
		triangleDistribution = new TriDistU(simplexAnalysis.getConnTriAnalysis().getmTriColoEdgesTriCountDistAvg(),
				simplexAnalysis.getIsoTriAnalysis().getmIsolatedTriColoEdgesTriCountDistAvg(), iNoOfVersions,
				noOfVertices, mRandom);

		sortedEdgeColours = new HashSet<>();
		for(EdgeColos e: initializer.getSetAllEdgeColours()) {
			sortedEdgeColours.add(new EdgeColorsSorted(e.getA(), e.getB()));
		}
	}

	@Override
	public BitSet proposeVertex3Colour(BitSet selectedVertex1Colo, BitSet selectedVertex2Colo) {
		return triangleDistribution.proposeVertexColorForVertex3(selectedVertex1Colo, selectedVertex2Colo);
	}

	@Override
	public TriColours proposeTriangleToAddEdge(Set<TriColours> setTriangleColorsMimicGraph) {
		return triangleDistribution.proposeTriangleToAddEdge(setTriangleColorsMimicGraph);
	}

	@Override
	public IOfferedItem<BitSet> getHeadProposer() {
		return selfLoops1IsoS1.getPotentialColourProposer();
	}

	@Override
	public IOfferedItem<BitSet> getColourPointProposer() {
		return s0Dist.getPotentialColourProposer();
	}

	@Override
	public IOfferedItem<EdgeColos> getIsolatedHeadProposer() {
		return s1connTriDist.getPotentialConnEdgeProposer();
	}

	@Override
	public IOfferedItem<TriColours> getIsolatedTriangleProposer() {
		return triangleDistribution.getPotentialIsolatedTriangleProposer();
	}

	@Override
	public TriColours proposeIsoTriangleToAddEdge(Set<TriColours> setIsoTriInMimicGraph) {
		return triangleDistribution.proposeIsoTriToAddEdge(setIsoTriInMimicGraph);
	}

	@Override
	public IOfferedItem<BitSet> getColourProposerVertConnTriangle() {
		return s1connToTriVertDist.getPotentialColourProposer();
	}

	@Override
	public IOfferedItem<EdgeColos> getHeadConnEdgeProposer() {
		return s1connToTriDist.getPotentialConnEdgeProposer();
	}

	@Override
	public IOfferedItem<BitSet> getIsoS1Proposer() {
		return selfLoopsInS1Dist.getPotentialColourProposer();
	}

	@Override
	public IOfferedItem<BitSet> getIsoS2Proposer() {
		return selfLoopIsoTritDist.getPotentialColourProposer();
	}

	@Override
	public IOfferedItem<BitSet> getConnS2Proposer() {
		return selfLoopConnTriDist.getPotentialColourProposer();
	}

	@Override
	public IOfferedItem<BitSet> getConnS1TriProposer() {
		return selfLoops1ConnToTriDist.getPotentialColourProposer();
	}

	@Override
	public IOfferedItem<BitSet> getConnS1Proposer() {
		return selfLoopsInConnS1Dist.getPotentialColourProposer();
	}

	@Override
	public IOfferedItem<EdgeColos> getIsolatedEdgeProposer() {
		return s1ConnDist.getPotentialIsolatedEdgeColoProposer();
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
	public EdgeColos proposeTriangleToAddEdgeColours(Set<EdgeColos> setEdgeColorsMimicGraph) {
		return s1ConnDist.proposeTriangleToAddEdge(setEdgeColorsMimicGraph);
	}

	public VertDistU getS1connToTriVertDist() {
		return s1connToTriVertDist;
	}

	public VertDistU getS0Dist() {
		return s0Dist;
	}

	public VertDistU getSelfLoopIsoTritDist() {
		return selfLoopIsoTritDist;
	}

	public VertDistU getSelfLoopConnTriDist() {
		return selfLoopConnTriDist;
	}

	public VertDistU getSelfLoops1ConnToTriDist() {
		return selfLoops1ConnToTriDist;
	}

	public VertDistU getSelfLoops1IsoS1() {
		return selfLoops1IsoS1;
	}

	public VertDistU getSelfLoopsInS1Dist() {
		return selfLoopsInS1Dist;
	}

	public VertDistU getSelfLoopsInConnS1Dist() {
		return selfLoopsInConnS1Dist;
	}

	public ConnS1DistU getS1connToTriDist() {
		return s1connToTriDist;
	}

	public ConnS1DistU getS1connTriDist() {
		return s1connTriDist;
	}

	public EdgeDistU getS1ConnDist() {
		return s1ConnDist;
	}

	public EdgeDistUS getS1ConnheadTailDist() {
		return s1ConnheadTailDist;
	}

	public TriDistU getTriangleDistribution() {
		return triangleDistribution;
	}

	@Override
	public IOfferedItem<BitSet> proposeColour(BitSet proposedHeadColo) {
		return s1ConnheadTailDist.proposeVertColo(proposedHeadColo);
	}

	@Override
	public IOfferedItem<BitSet> getConnHeadProposer() {
		return s1ConnheadTailDist.getPotentialHeadColoProposer();
	}

	@Override
	public IOfferedItem<TriColours> getTriangleProposal() {
		return triangleDistribution.getPotentialTriangleProposer();
	}

	@Override
	public EdgeColorsSorted getEdgeProposalFromTriangleDist(Set<EdgeColorsSorted> edgeColosSet) {
		return new OfferedItemWrapper<EdgeColorsSorted>(sortedEdgeColours.toArray(EdgeColorsSorted[]::new),
				mRandom).getPotentialItem(edgeColosSet);
	}

}
