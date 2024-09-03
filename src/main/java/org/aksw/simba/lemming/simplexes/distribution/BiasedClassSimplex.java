package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
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
	/** Store distributions of 1-simplexes connected to triangles */
	private ConnS1DistI s1connToTriDist;
	/** Store distributions of 1-simplexes connecting triangles */
	private ConnS1DistI s1connTriDist;
	/** Store distributions of connected 1-simplexes */
	private EdgeDistI s1ConnDist;
	/** Store distributions of connected 1-simplexes */
	private EdgeDistIS s1ConnheadTailDist;
	/** Store distributions of isolated and connected 2-simplexes */
	private ITriDist triangleDistribution;


	public BiasedClassSimplex(SimplexGraphInitializer initializer) {
		SimplexAnalysis simplexAnalysis = initializer.getSimplexAnalysis();
		int iNoOfVersions = initializer.getiNoOfVersions();
		Random mRandom = new Random(initializer.getSeedGenerator().getNextSeed());
		int noOfVertices = initializer.getDesiredNoOfVertices();
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
		s1ConnDist = new EdgeDistI(simplexAnalysis.getConnS1Analysis().getmColoEdgesCountDistAvg(),
				simplexAnalysis.getIsoS1Analysis().getmColoEdgesCountDistAvg(), iNoOfVersions,
				initializer.getDesiredNoOfVertices(), mRandom);
		s1ConnheadTailDist = new EdgeDistIS(simplexAnalysis.getConnS1Analysis().getmHeadColoCountConnected1Simplex(),
				simplexAnalysis.getConnS1Analysis().getmHeadColoTailColoCountConnected(), iNoOfVersions, mRandom);
		s1connToTriDist = new ConnS1DistI(simplexAnalysis.getS1ConnToTri().getmColoEdgesCountDistAvg(), iNoOfVersions,
				mRandom);
		s1connTriDist = new ConnS1DistI(simplexAnalysis.getS1ConnectingTri().getmColoEdgesCountDistAvg(), iNoOfVersions,
				mRandom);
		triangleDistribution = new TriDistWithEdgeI(simplexAnalysis.getConnTriAnalysis().getmTriColoEdgesTriCountDistAvg(),
				simplexAnalysis.getIsoTriAnalysis().getmIsolatedTriColoEdgesTriCountDistAvg(), iNoOfVersions,
				noOfVertices, mRandom);
	}
	
	public void setTriangleDistribution(ITriDist triDist) {
		triangleDistribution = triDist;
	}
	
	public ITriDist getTriangleDistribution() {
		return triangleDistribution;
	}
	
	@Override
	public IOfferedItem<TriColours> getTriangleProposal() {
		return triangleDistribution.getPotentialTriangleProposer();
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
	public IOfferedItem<TriColours> getIsolatedTriangleProposer() {
		return triangleDistribution.getPotentialIsolatedTriangleProposer();
	}

	@Override
	public TriColours proposeIsoTriangleToAddEdge(Set<TriColours> setIsoTriInMimicGraph) {
		return triangleDistribution.proposeIsoTriToAddEdge(setIsoTriInMimicGraph);
	}

	@Override
	public IOfferedItem<BitSet> getHeadProposer() {
		return selfLoops1IsoS1.getPotentialColoProposer();
	}

	@Override
	public IOfferedItem<BitSet> getColourPointProposer() {
		return s0Dist.getPotentialColoProposer();
	}

	@Override
	public IOfferedItem<EdgeColos> getIsolatedHeadProposer() {
		return s1connTriDist.getPotentialConnEdgeProposer();
	}

	@Override
	public IOfferedItem<BitSet> getColourProposerVertConnTriangle() {
		return s1connToTriVertDist.getPotentialColoProposer();
	}

	@Override
	public IOfferedItem<EdgeColos> getHeadConnEdgeProposer() {
		return s1connToTriDist.getPotentialConnEdgeProposer();
	}

	@Override
	public IOfferedItem<BitSet> getIsoS1Proposer() {
		return selfLoopsInS1Dist.getPotentialColoProposer();
	}

	@Override
	public IOfferedItem<BitSet> getIsoS2Proposer() {
		return selfLoopIsoTritDist.getPotentialColoProposer();
	}

	@Override
	public IOfferedItem<BitSet> getConnS2Proposer() {
		return selfLoopConnTriDist.getPotentialColoProposer();
	}

	@Override
	public IOfferedItem<BitSet> getConnS1TriProposer() {
		return selfLoops1ConnToTriDist.getPotentialColoProposer();
	}

	@Override
	public IOfferedItem<BitSet> getConnS1Proposer() {
		return selfLoopsInConnS1Dist.getPotentialColoProposer();
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

	public ConnS1DistI getS1connToTriDist() {
		return s1connToTriDist;
	}

	public ConnS1DistI getS1connTriDist() {
		return s1connTriDist;
	}

	public EdgeDistI getS1ConnDist() {
		return s1ConnDist;
	}

	public EdgeDistIS getS1ConnheadTailDist() {
		return s1ConnheadTailDist;
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
	public EdgeColorsSorted getEdgeProposalFromTriangleDist(Set<EdgeColorsSorted> edgeColosSet) {
		return triangleDistribution.getPotentialEdgeProposer().getPotentialItem(edgeColosSet);
	}
}
