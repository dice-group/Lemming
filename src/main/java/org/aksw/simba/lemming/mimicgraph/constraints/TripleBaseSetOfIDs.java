package org.aksw.simba.lemming.mimicgraph.constraints;

import org.aksw.simba.lemming.util.Constants;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

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
	
	public TripleBaseSetOfIDs(BitSet tailColo, double rateOfTails, BitSet edgeColo, double rateOfEdges, BitSet headColo, double rateOfHeads){
		
		tailColour = tailColo;
		noOfTails = rateOfTails;
		tailIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);

		edgeColour = edgeColo;
		noOfEdges = rateOfEdges;
		edgeIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
		
		headColour = headColo;
		noOfHeads = rateOfHeads;
		headIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
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
