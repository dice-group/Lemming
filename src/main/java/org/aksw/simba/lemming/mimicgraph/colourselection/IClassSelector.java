package org.aksw.simba.lemming.mimicgraph.colourselection;

import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;

import com.carrotsearch.hppc.BitSet;

public interface IClassSelector {

	IOfferedItem<BitSet> getTailClass(BitSet edgeColour);
	
	IOfferedItem<BitSet> getHeadClass(BitSet tailColour, BitSet edgeColour);
	
//	BitSet getTailClass();
//	BitSet getHeadClassFromTailColour(BitSet tailColour);
//	BitSet getEdgeColourFromTailHeadColour(BitSet tailColour, BitSet headColour);
	

	default public ClassProposal getProposal(BitSet edgeColour, int fakeEdgeId, Set<BitSet> restrictions) {
		return getProposal(edgeColour, restrictions);
	}
	
	default public ClassProposal getProposal(BitSet edgeColour, Set<BitSet> restrictions) {
		IOfferedItem<BitSet> tailColourProposer = getTailClass(edgeColour);
		if(tailColourProposer == null)
			return null;
		BitSet tailColour;
		if(restrictions == null || restrictions.isEmpty()) {
			tailColour = tailColourProposer.getPotentialItem();
		} else {
			tailColour = tailColourProposer.getPotentialItem(restrictions);
		}
		
		if (tailColour == null) {
			return null;
		}
		
		IOfferedItem<BitSet> headColourProposer = getHeadClass(tailColour, edgeColour);
		if(headColourProposer == null)
			return null;
		BitSet headColour;
		if(restrictions == null || restrictions.isEmpty()) {
			headColour = headColourProposer.getPotentialItem();
		} else {
			headColour = headColourProposer.getPotentialItem(restrictions);
		}
		if (headColour == null) {
			return null;
		}
		
		return new ClassProposal(tailColour, edgeColour, headColour);
	}
	
	BitSet getEdgeColourProposal();
}
