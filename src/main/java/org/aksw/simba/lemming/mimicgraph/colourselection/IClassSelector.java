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
	

	default public ClassProposal getProposal(BitSet edgeColour, int fakeEdgeId, int n, Set<BitSet> restrictions) {
		return getProposal(edgeColour, n, restrictions);
	}
	
	default public ClassProposal getProposal(BitSet edgeColour, int n, Set<BitSet> restrictions) {
		IOfferedItem<BitSet> tailColourProposer = getTailClass(edgeColour);
		if(tailColourProposer == null)
			return null;
		BitSet tailColour;
		if(restrictions == null) {
			tailColour = tryValidColour(tailColourProposer, n);
		} else {
			tailColour = tryValidColour(tailColourProposer, restrictions, n);
		}
		
		if (tailColour == null) {
			return null;
		}
		
		IOfferedItem<BitSet> headColourProposer = getHeadClass(tailColour, edgeColour);
		if(headColourProposer == null)
			return null;
		BitSet headColour;
		if(restrictions == null) {
			headColour = tryValidColour(headColourProposer, n);
		} else {
			headColour = tryValidColour(headColourProposer, restrictions, n);
		}
		if (headColour == null) {
			return null;
		}
		
		return new ClassProposal(tailColour, edgeColour, headColour);
	}
	
	default public BitSet tryValidColour(IOfferedItem<BitSet> proposer, Set<BitSet> restrictions, int n) {
		BitSet colour = null;
		for (int i = 0; i < n; i++) {
			colour = proposer.getPotentialItem(restrictions);
			if(colour != null) {
				return colour;	
			} 					
		}
		return null;
	}
	
	default public BitSet tryValidColour(IOfferedItem<BitSet> proposer, int n) {
		BitSet colour = null;
		for (int i = 0; i < n; i++) {
			colour = proposer.getPotentialItem();
			if(colour != null) {
				return colour;	
			} 					
		}
		return null;
	}
	
	BitSet getEdgeColourProposal();
}
