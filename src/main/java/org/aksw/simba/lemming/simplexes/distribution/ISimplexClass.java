package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;

import com.carrotsearch.hppc.BitSet;

public interface ISimplexClass {

	TriColours getTriangleProposal();

	EdgeColorsSorted getEdgeProposalFromTriangleDist(Set<EdgeColorsSorted> edgeColosSet);

	BitSet proposeVertex3Colour(BitSet selectedVertex1Colo, BitSet selectedVertex2Colo);

	TriColours proposeTriangleToAddEdge(Set<TriColours> setTriangleColorsMimicGraph);

	IOfferedItem<BitSet> getHeadProposer();

	IOfferedItem<BitSet> getColourPointProposer();

	IOfferedItem<EdgeColos> getIsolatedHeadProposer();

	IOfferedItem<TriColours> getIsolatedTriangleProposer();

	TriColours proposeIsoTriangleToAddEdge(Set<TriColours> setIsoTriInMimicGraph);

	IOfferedItem<BitSet> getConnHeadProposer();

	IOfferedItem<BitSet> proposeColour(BitSet proposedHeadColo);

	IOfferedItem<BitSet> getColourProposerVertConnTriangle();

	IOfferedItem<EdgeColos> getHeadConnEdgeProposer();

	IOfferedItem<BitSet> getIsoS1Proposer();

	IOfferedItem<BitSet> getIsoS2Proposer();

	IOfferedItem<BitSet> getConnS2Proposer();

	IOfferedItem<BitSet> getConnS1TriProposer();

	IOfferedItem<BitSet> getConnS1Proposer();

	ITriDist getTriangleDistribution();

}
