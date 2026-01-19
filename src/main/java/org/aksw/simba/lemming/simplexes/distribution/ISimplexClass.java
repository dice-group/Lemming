package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public interface ISimplexClass {

	EdgeColos proposeTriangleToAddEdgeColours(Set<EdgeColos> setEdgeColorsMimicGraph);

	IOfferedItem<BitSet> getHeadProposer();

	IOfferedItem<BitSet> getColourPointProposer();

	IOfferedItem<EdgeColos> getIsolatedHeadProposer();

	IOfferedItem<BitSet> getColourProposerVertConnTriangle();

	IOfferedItem<EdgeColos> getHeadConnEdgeProposer();

	IOfferedItem<BitSet> getIsoS1Proposer();

	IOfferedItem<BitSet> getIsoS2Proposer();

	IOfferedItem<BitSet> getConnS2Proposer();

	IOfferedItem<BitSet> getConnS1TriProposer();

	IOfferedItem<BitSet> getConnS1Proposer();

	IOfferedItem<EdgeColos> getIsolatedEdgeProposer();

	EdgeColos proposeConnEdge();

	ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> getEdgeColoursV1V2Dist();

	IOfferedItem<BitSet> proposeColour(BitSet proposedHeadColo);

	IOfferedItem<BitSet> getConnHeadProposer();
	
	BitSet proposeVertex3Colour(BitSet selectedVertex1Colo, BitSet selectedVertex2Colo);

	TriColours proposeTriangleToAddEdge(Set<TriColours> setTriangleColorsMimicGraph);

	IOfferedItem<TriColours> getIsolatedTriangleProposer();

	TriColours proposeIsoTriangleToAddEdge(Set<TriColours> setIsoTriInMimicGraph);
	
	IOfferedItem<TriColours> getTriangleProposal();

	ITriDist getTriangleDistribution();
	
	EdgeColorsSorted getEdgeProposalFromTriangleDist(Set<EdgeColorsSorted> edgeColosSet);

}
