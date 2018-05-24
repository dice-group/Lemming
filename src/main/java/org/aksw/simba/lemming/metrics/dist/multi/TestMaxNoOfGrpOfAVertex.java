package org.aksw.simba.lemming.metrics.dist.multi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import grph.algo.MultiThreadProcessing;

import org.aksw.simba.lemming.ColouredGraph;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

public class TestMaxNoOfGrpOfAVertex {
	
	private ObjectIntOpenHashMap<BitSet> mMapTailColourToNoOfGrp;
	
	public TestMaxNoOfGrpOfAVertex(){
		mMapTailColourToNoOfGrp = new ObjectIntOpenHashMap<BitSet>();
	}
	
	public ObjectIntOpenHashMap getMapTailsToNoOfGrp(){
		return mMapTailColourToNoOfGrp;
	}
	
	public void apply(ColouredGraph grph){
		
		new MultiThreadProcessing(grph.getVertices()) {
			@Override
			protected void run(int threadID, int tailId) {
				BitSet tailColo = grph.getVertexColour(tailId);
				IntSet outEdges = grph.getOutEdges(tailId);
				if(outEdges != null && outEdges.size() > 0){
					int[] arrOutEdges = outEdges.toIntArray();
					
					Set<BitSet> mapEdgeColours = new HashSet<BitSet>();
					Set<BitSet> mapHeadColours = new HashSet<BitSet>();
					
					for(int edgeId: arrOutEdges){
						BitSet edgeColo = grph.getEdgeColour(edgeId);
						mapEdgeColours.add(edgeColo);
						int headId = grph.getHeadOfTheEdge(edgeId);
						BitSet headColo = grph.getVertexColour(headId);
						mapHeadColours.add(headColo);						
					}
					
					int noOfEdgeColours = mapEdgeColours.size();
					int noOfHeadColours = mapHeadColours.size();
					int iNoOfGrpu = noOfEdgeColours * noOfHeadColours;
					synchronized(mMapTailColourToNoOfGrp){
						int noOfGrp = mMapTailColourToNoOfGrp.get(tailColo);
						if(noOfGrp < iNoOfGrpu){
							mMapTailColourToNoOfGrp.put(tailColo, iNoOfGrpu);
						}
					}
				}
			}
		};
	}
}
