package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * for each vertex's colours, compute in-degree distribution associated with each edge's colour
 * 
 * @author nptsy
 */
public class InDegreeDistBaseVEColo {
	//key1: vertex's colors, key2: edge's colors and values: in-degree distribution
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap>> mapInDegreeBaseVEColo;
	
	//key1: vertex's colors, key2 edge's colors and values: set of vertex ids
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IntSet>> mapVertIDsBaseVEColo;
	
	public InDegreeDistBaseVEColo(ColouredGraph grph){
		mapInDegreeBaseVEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap>>();
		mapVertIDsBaseVEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IntSet>>();
		apply(grph);
	}
	
	public Map<BitSet, Map<BitSet, Object>> getMapVertColoToInEdgeColo(){
		Object[] keyVertexColours = mapInDegreeBaseVEColo.keys;
		Map<BitSet, Map<BitSet, Object>> res = new HashMap<BitSet, Map<BitSet, Object>>();
		for(int i = 0 ; i< keyVertexColours.length; ++i){
			if(mapInDegreeBaseVEColo.allocated[i]){
				BitSet vertColo = (BitSet)keyVertexColours[i];
				
				Map<BitSet, Object> linkedInEdges = res.get(vertColo);
				
				if(linkedInEdges == null){
					linkedInEdges = new HashMap<BitSet, Object>();
					res.put(vertColo, linkedInEdges);
				}
				
				ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapIEColoToInDegree = mapInDegreeBaseVEColo.get(vertColo);
				Object[] keyEdgeColours = mapIEColoToInDegree.keys;
				for(int j = 0 ; j < keyEdgeColours.length ; ++j){
					if(mapIEColoToInDegree.allocated[j]){
						BitSet edgeColo = (BitSet) keyEdgeColours[j];
						linkedInEdges.put(edgeColo, null);
					}
				}
			}
		}
		
		return res;
	}
	
	public int getMaxInDegreeOf(BitSet vertColo, BitSet edgeColo){
		int maxInDegree = 0;
		ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapIEColoToInDegree = mapInDegreeBaseVEColo.get(vertColo);
		if(mapIEColoToInDegree != null){
			IntIntOpenHashMap mapInDegree = mapIEColoToInDegree.get(edgeColo);
			if(mapInDegree!= null){ 
				int [] arrDataPoints = mapInDegree.keys;
				for(int i = 0 ; i < arrDataPoints.length; ++i){
					if(mapInDegree.allocated[i]){
						if(maxInDegree < arrDataPoints[i]){
							maxInDegree = arrDataPoints[i];
						}
					}
				}
			}
		}
		return maxInDegree;
	}
	
	
	public double getAverageInDegreeOf(BitSet vertColo, BitSet edgeColo){
		ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapIEColoToInDegree = mapInDegreeBaseVEColo.get(vertColo);
		ObjectObjectOpenHashMap<BitSet, IntSet> mapEdgeColoToVertexIDs = mapVertIDsBaseVEColo.get(vertColo);
		if(mapIEColoToInDegree != null && mapEdgeColoToVertexIDs != null){
			IntIntOpenHashMap mapInDegree = mapIEColoToInDegree.get(edgeColo);
			IntSet headIDs = mapEdgeColoToVertexIDs.get(edgeColo);
			if(mapInDegree!= null && headIDs != null){ 
				int [] arrDegreeSamples = mapInDegree.keys;
				double totalInDegree = 0;
				
				for(int i = 0 ; i < arrDegreeSamples.length; ++i){
					if(mapInDegree.allocated[i]){
						int noOfVertices = mapInDegree.get(arrDegreeSamples[i]);
						totalInDegree += (noOfVertices * arrDegreeSamples[i]);
					}
				}
				if(headIDs.size() != 0){
					return (totalInDegree/headIDs.size() );
				}
			}
		}
		return 0;
	}
	
	
	private void apply(ColouredGraph grph){
		int[] arrVertexIDs = grph.getVertices().toIntArray();
		
		for(int i = 0 ; i < arrVertexIDs.length ; i++){
			int vertId = arrVertexIDs[i];
			BitSet vertColo = grph.getVertexColour(vertId);
			int [] arrInEdgeIDs = grph.getInEdges(vertId).toIntArray();
			
			ObjectIntOpenHashMap<BitSet> mapInEdgeDist = new ObjectIntOpenHashMap<BitSet>();
			
			for(int j = 0 ; j < arrInEdgeIDs.length ; ++j){
				BitSet edgeColo = grph.getEdgeColour(arrInEdgeIDs[j]); 
				mapInEdgeDist.putOrAdd(edgeColo, 1, 1);
			}
			
			int iNoOfEdgeColours = mapInEdgeDist.allocated.length;
			for(int j = 0 ; j< iNoOfEdgeColours ; ++j){
				if(mapInEdgeDist.allocated[j]){
					
					BitSet edgeColo = (BitSet)((Object[])mapInEdgeDist.keys)[j];
					int noOfInEdges = mapInEdgeDist.get(edgeColo);
					
					// put to the global map
					ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapIEColoToInDegree = mapInDegreeBaseVEColo.get(vertColo);
					if(mapIEColoToInDegree == null){
						mapIEColoToInDegree = new ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap>();
						mapInDegreeBaseVEColo.put(vertColo, mapIEColoToInDegree);
					}
					
					IntIntOpenHashMap mapInDegree = mapIEColoToInDegree.get(edgeColo);
					if(mapInDegree == null){
						mapInDegree = new IntIntOpenHashMap();
						mapIEColoToInDegree.put(edgeColo, mapInDegree);
					}
					mapInDegree.putOrAdd(noOfInEdges, 1, 1);
					
					ObjectObjectOpenHashMap<BitSet, IntSet> mapIEColoToVertexIDs = mapVertIDsBaseVEColo.get(vertColo);
					if(mapIEColoToVertexIDs == null){
						mapIEColoToVertexIDs = new ObjectObjectOpenHashMap<BitSet, IntSet>();
						mapVertIDsBaseVEColo.put(vertColo, mapIEColoToVertexIDs);
					}
					
					IntSet setHeadIDs = mapIEColoToVertexIDs.get(edgeColo);
					if(setHeadIDs == null){
						setHeadIDs = new DefaultIntSet();
						mapIEColoToVertexIDs.put(edgeColo, setHeadIDs);
					}
					setHeadIDs.add(vertId);
				}
			}
		}
	}
}
