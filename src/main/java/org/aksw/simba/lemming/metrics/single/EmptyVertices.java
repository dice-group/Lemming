package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import com.carrotsearch.hppc.BitSet;

import toools.set.IntSet;

public class EmptyVertices  extends AbstractMetric implements SingleValueMetric {
	
	public EmptyVertices(String name) {
		super(name);
	}
	
	public EmptyVertices(){
		super("#colourlessVertices");
	}

	@Override
	public double apply(ColouredGraph graph) {
		IntSet setOfVertices = graph.getVertices();
		int[] arrayIDs = setOfVertices.toIntArray();
		double noOfVertices = 0 ;
		for(int vId: arrayIDs){
			BitSet vColo = graph.getVertexColour(vId);
			if(vColo.isEmpty()){
				noOfVertices ++;
			}
		}
		return noOfVertices;
	}

}
