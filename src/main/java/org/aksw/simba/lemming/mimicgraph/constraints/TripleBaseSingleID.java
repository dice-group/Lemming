package org.aksw.simba.lemming.mimicgraph.constraints;

import com.carrotsearch.hppc.BitSet;

public class TripleBaseSingleID {
	public int tailId = -1;
	public BitSet headColour = null;
	
	public int headId = -1;
	public BitSet tailColour = null;
	
	public int edgeId = -1;
	public BitSet edgeColour = null;
	
	public TripleBaseSingleID() {
	}

	public TripleBaseSingleID(int tailId, BitSet headColour, int headId, BitSet tailColour, int edgeId,
			BitSet edgeColour) {
		super();
		this.tailId = tailId;
		this.headColour = headColour;
		this.headId = headId;
		this.tailColour = tailColour;
		this.edgeId = edgeId;
		this.edgeColour = edgeColour;
	}

	public boolean equals(Object obj){
		if(obj instanceof TripleBaseSingleID){
			TripleBaseSingleID comObj = (TripleBaseSingleID) obj;
			if(comObj.tailId != tailId)
				return false;
			if(comObj.headId != headId)
				return false;
			if(comObj.edgeId != edgeId)
				return false;
			if(!comObj.tailColour.equals(tailColour))
				return false;
			if(!comObj.headColour.equals(headColour))
				return false;
			if(!comObj.edgeColour.equals(edgeColour))
				return false;
		}
		return true;
	}
	
}
