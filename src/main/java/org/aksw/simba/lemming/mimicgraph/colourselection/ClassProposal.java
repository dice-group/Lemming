package org.aksw.simba.lemming.mimicgraph.colourselection;

import com.carrotsearch.hppc.BitSet;

public class ClassProposal {
	BitSet tailColour;
	BitSet edgeColour;
	BitSet headColour;

	public ClassProposal(BitSet tailColour, BitSet edgeColour, BitSet headColour) {
		this.tailColour = tailColour;
		this.edgeColour = edgeColour;
		this.headColour = headColour;
	}

	public BitSet getTailColour() {
		return tailColour;
	}

	public BitSet getEdgeColour() {
		return edgeColour;
	}

	public BitSet getHeadColour() {
		return headColour;
	}
}
