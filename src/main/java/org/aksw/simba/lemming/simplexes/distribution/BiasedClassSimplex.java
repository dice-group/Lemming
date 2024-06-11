package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.creation.SimplexGraphInitializer;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.simplexes.analysis.SimplexAnalysis;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

@Component("BCSI")
@Scope(value = "prototype")
public class BiasedClassSimplex implements ISimplexClass {

	/** Distribution of vertices connected to triangles */
	private VertDistI s1connToTriVertDist;
	/** 0-simplexes */
	private VertDistI s0Dist;
	/** Distribution of self loops for isolated triangles */
	private VertDistI selfLoopIsoTritDist;
	/** Distribution of self loops for connected triangles */
	private VertDistI selfLoopConnTriDist;
	/** Distribution of self loops for vertices connected to triangles */
	private VertDistI selfLoops1ConnToTriDist;
	/** Distribution of isolated self loops */
	private VertDistI selfLoops1IsoS1;
	/** Distribution of self loops for vertices in isolated 1-simplexes */
	private VertDistI selfLoopsInS1Dist;
	/** Distribution of self loops for vertices in connected 1-simplexes */
	private VertDistI selfLoopsInConnS1Dist;
	/** Store distributions of isolated and connected 2-simplexes */
	private TriDistWithEdgeI triangleDistribution;
	/** Store distributions of 1-simplexes connected to triangles */
	private ConnS1DistI s1connToTriDist;
	/** Store distributions of 1-simplexes connecting triangles */
	private ConnS1DistI s1connTriDist;
	/** Store distributions of connected 1-simplexes */
	private EdgeDistIS s1ConnheadTailDist;

	public BiasedClassSimplex(SimplexGraphInitializer initializer) {
		SimplexAnalysis simplexAnalysis = initializer.getSimplexAnalysis();
		int iNoOfVersions = initializer.getiNoOfVersions();
		int noOfVertices = initializer.getDesiredNoOfVertices();
		Random mRandom = new Random(initializer.getSeedGenerator().getNextSeed());
		selfLoopIsoTritDist = new VertDistI(simplexAnalysis.getSelfLoopIsoTri().getmColoCountSelfLoop(), iNoOfVersions,
				mRandom);
		selfLoopConnTriDist = new VertDistI(simplexAnalysis.getSelfLoopConnTri().getmColoCountSelfLoop(), iNoOfVersions,
				mRandom);
		selfLoops1ConnToTriDist = new VertDistI(simplexAnalysis.getSelfLoops1ConnToTri().getmColoCountSelfLoop(),
				iNoOfVersions, mRandom);
		selfLoops1IsoS1 = new VertDistI(simplexAnalysis.getIsoS1SelfLoopAnalysis().getmColoCountSelfLoop(),
				iNoOfVersions, mRandom);
		selfLoopsInS1Dist = new VertDistI(simplexAnalysis.getSelfLoopsInIsoS1().getmColoCountSelfLoop(), iNoOfVersions,
				mRandom);
		s0Dist = new VertDistI(simplexAnalysis.getS0Analysis().getmColoCount0Simplex(), iNoOfVersions, mRandom);
		selfLoopsInConnS1Dist = new VertDistI(simplexAnalysis.getSelfLoopsInConnS1().getmColoCountSelfLoop(),
				iNoOfVersions, mRandom);
		s1connToTriVertDist = new VertDistI(simplexAnalysis.getS1ConnToTri().getmColoCountVertConnectedToTriangle(),
				iNoOfVersions, mRandom);

		// ******* Compute Triangle distributions *******
		triangleDistribution = new TriDistWithEdgeI(
				simplexAnalysis.getConnTriAnalysis().getmTriColoEdgesTriCountDistAvg(),
				simplexAnalysis.getIsoTriAnalysis().getmIsolatedTriColoEdgesTriCountDistAvg(), iNoOfVersions,
				noOfVertices, mRandom);
		s1connToTriDist = new ConnS1DistI(simplexAnalysis.getS1ConnToTri().getmColoEdgesCountDistAvg(), iNoOfVersions,
				mRandom);
		s1connTriDist = new ConnS1DistI(simplexAnalysis.getS1ConnectingTri().getmColoEdgesCountDistAvg(), iNoOfVersions,
				mRandom);
		s1ConnheadTailDist = new EdgeDistIS(simplexAnalysis.getConnS1Analysis().getmHeadColoCountConnected1Simplex(),
				simplexAnalysis.getConnS1Analysis().getmHeadColoTailColoCountConnected(), iNoOfVersions, mRandom);

	}

	public VertDistI getS1connToTriVertDist() {
		return s1connToTriVertDist;
	}

	public VertDistI getS0Dist() {
		return s0Dist;
	}

	public VertDistI getSelfLoopIsoTritDist() {
		return selfLoopIsoTritDist;
	}

	public VertDistI getSelfLoopConnTriDist() {
		return selfLoopConnTriDist;
	}

	public VertDistI getSelfLoops1ConnToTriDist() {
		return selfLoops1ConnToTriDist;
	}

	public VertDistI getSelfLoops1IsoS1() {
		return selfLoops1IsoS1;
	}

	public VertDistI getSelfLoopsInS1Dist() {
		return selfLoopsInS1Dist;
	}

	public VertDistI getSelfLoopsInConnS1Dist() {
		return selfLoopsInConnS1Dist;
	}

	public TriDistWithEdgeI getTriangleDistribution() {
		return triangleDistribution;
	}

	public ConnS1DistI getS1connToTriDist() {
		return s1connToTriDist;
	}

	public ConnS1DistI getS1connTriDist() {
		return s1connTriDist;
	}

	@Override
	public TriColours getTriangleProposal() {
		return triangleDistribution.getPotentialTriangleProposer().getPotentialItem();
	}

	@Override
	public EdgeColorsSorted getEdgeProposalFromTriangleDist(Set<EdgeColorsSorted> edgeColosSet) {
		return triangleDistribution.getPotentialEdgeProposer().getPotentialItem(edgeColosSet);
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
	public OfferedItemByRandomProb<BitSet> getHeadProposer() {
		return selfLoops1IsoS1.getPotentialColoProposer();
	}

	@Override
	public OfferedItemByRandomProb<BitSet> getColourPointProposer() {
		return s0Dist.getPotentialColoProposer();
	}

	@Override
	public OfferedItemByRandomProb<EdgeColos> getIsolatedHeadProposer() {
		return s1connTriDist.getPotentialConnEdgeProposer();
	}

	@Override
	public OfferedItemByRandomProb<TriColours> getIsolatedTriangleProposer() {
		return triangleDistribution.getPotentialIsolatedTriangleProposer();
	}

	@Override
	public TriColours proposeIsoTriangleToAddEdge(Set<TriColours> setIsoTriInMimicGraph) {
		return triangleDistribution.proposeIsoTriToAddEdge(setIsoTriInMimicGraph);
	}

	@Override
	public OfferedItemByRandomProb<BitSet> getConnHeadProposer() {
		return s1ConnheadTailDist.getPotentialHeadColoProposer();
	}

	@Override
	public OfferedItemByRandomProb<BitSet> proposeColour(BitSet proposedHeadColo) {
		return s1ConnheadTailDist.proposeVertColo(proposedHeadColo);
	}

	@Override
	public OfferedItemByRandomProb<BitSet> getColourProposerVertConnTriangle() {
		return s1connToTriVertDist.getPotentialColoProposer();
	}

	@Override
	public OfferedItemByRandomProb<EdgeColos> getHeadConnEdgeProposer() {
		return s1connToTriDist.getPotentialConnEdgeProposer();
	}

	@Override
	public OfferedItemByRandomProb<BitSet> getIsoS1Proposer() {
		return selfLoopsInS1Dist.getPotentialColoProposer();
	}

	@Override
	public OfferedItemByRandomProb<BitSet> getIsoS2Proposer() {
		return selfLoopIsoTritDist.getPotentialColoProposer();
	}

	@Override
	public OfferedItemByRandomProb<BitSet> getConnS2Proposer() {
		return selfLoopConnTriDist.getPotentialColoProposer();
	}

	@Override
	public OfferedItemByRandomProb<BitSet> getConnS1TriProposer() {
		return selfLoops1ConnToTriDist.getPotentialColoProposer();
	}

	@Override
	public OfferedItemByRandomProb<BitSet> getConnS1Proposer() {
		return selfLoopsInConnS1Dist.getPotentialColoProposer();
	}

}
