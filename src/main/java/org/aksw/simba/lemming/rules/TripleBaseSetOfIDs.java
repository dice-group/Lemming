package org.aksw.simba.lemming.rules;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

public class TripleBaseSetOfIDs {
	
	public BitSet headColour;
	public double noOfHeads;
	public IntSet headIDs;
	
	
	public BitSet tailColour;
	public double noOfTails;
	public IntSet tailIDs;
	
	
	public BitSet edgeColour;
	public double noOfEdges;
	public IntSet edgeIDs;
	
	public TripleBaseSetOfIDs(BitSet headColo, double rateOfHeads, BitSet tailColo, double rateOfTails,
						BitSet edgeColo, double rateOfEdges){
		
		headColour = headColo;
		noOfHeads = rateOfHeads;
		headIDs = new DefaultIntSet();
		
		tailColour = tailColo;
		noOfTails = rateOfTails;
		tailIDs = new DefaultIntSet();
		
		edgeColour = edgeColo;
		noOfEdges = rateOfEdges;
		edgeIDs = new DefaultIntSet();
	}
	
	public boolean equals(Object obj){

		if(obj instanceof TripleBaseSetOfIDs){
			TripleBaseSetOfIDs tripple = (TripleBaseSetOfIDs) obj;
			if(headColour.equals(tripple.headColour) && tailColour.equals(tripple.tailColour) &&
					edgeColour.equals(tripple.edgeColour)){
				return true;
			}
		}
		return false;
	}
	
}
