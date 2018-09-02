package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

public interface IGraphGeneration {
	public ColouredGraph generateGraph();
	public IColourMappingRules getColourMapper();
	public Map<BitSet,IntSet> getMappingColoursAndVertices();
	public Map<BitSet,IntSet> getMappingColoursAndEdges();
	
	//public BitSet getProposedEdgeColour(BitSet headColour, BitSet tailColour);
	//public BitSet getProposedHeadColour(BitSet edgeColour, BitSet tailColour);
	//public BitSet getProposedTailColour(BitSet headColour, BitSet edgeColour);
	public TripleBaseSingleID getProposedTriple(boolean isRamdom);
	
	public ColouredGraph getMimicGraph();
	public String getLiteralType(BitSet dteColo);
	public void setMimicGraph(ColouredGraph refinedGraph);
}
