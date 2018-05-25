package org.aksw.simba.lemming.rules;

import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;

public interface IColourMappingRules {
	
	public void analyzeRules(ColouredGraph[] origGrphs);
	
	public Set<BitSet> getHeadColoursFromEdgeColour(BitSet edgeColour);
	public Set<BitSet> getTailColoursFromEdgeColour(BitSet edgeColour);
	public Set<BitSet> getHeadColours(BitSet tailColour);
	public Set<BitSet> getHeadColours(BitSet tailColour, BitSet edgeColour);
	public Set<BitSet> getTailColours(BitSet headColour);
	public Set<BitSet> getTailColours(BitSet headColour, BitSet edgeColour);
	public Set<BitSet> getPossibleLinkingEdgeColours(BitSet tailColour, BitSet headColour);
	public Set<BitSet> getPossibleOutEdgeColours(BitSet tailColour);
	public Set<BitSet> getPossibleInEdgeColours(BitSet headColour);
	boolean isHeadColourOf(BitSet tailColour, BitSet checkedColour);
	boolean isTailColourOf(BitSet headColour, BitSet checkedColour);
	public boolean canConnect(BitSet headColour, BitSet tailColour, BitSet edgeColour);
	public Set<BitSet> getDataTypedEdgeColoursByVertexColour(BitSet vertexColour);
}
