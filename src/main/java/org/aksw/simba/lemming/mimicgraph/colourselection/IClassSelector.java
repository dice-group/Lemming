package org.aksw.simba.lemming.mimicgraph.colourselection;

import com.carrotsearch.hppc.BitSet;

public interface IClassSelector {

	BitSet getTailClass(BitSet edgeColour);
	
	BitSet getHeadClass(BitSet tailColour, BitSet edgeColour);
	
	/**
	 * 
	 * @param edgeColour
	 * @return
	 */
	default public ClassProposal getProposal(BitSet edgeColour) {
		BitSet tailColour = getTailClass(edgeColour);
		BitSet headColour = getHeadClass(tailColour, edgeColour);
		return new ClassProposal(tailColour, edgeColour, headColour);
	}

	
	

}
