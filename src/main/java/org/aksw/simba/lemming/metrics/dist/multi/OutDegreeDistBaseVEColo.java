package org.aksw.simba.lemming.metrics.dist.multi;

import grph.algo.MultiThreadProcessing;

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
 * for each vertex's colour, we compute the average out degree associated with a specific edge's colour 
 * @author nptsy
 */
public class OutDegreeDistBaseVEColo {
	
	//key1: vertex's colors, key2: edge's colors and values: in-degree distribution
	ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap>> mapOutDegreeBaseVEColo;
	//key1: vertex's colors, key2 edge's colors and values: set of vertex ids
	ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IntSet>> mapVertexIDsBaseEVColo;
	
	public OutDegreeDistBaseVEColo(ColouredGraph grph){
		mapOutDegreeBaseVEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap>>();
		mapVertexIDsBaseEVColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IntSet>>();

		apply(grph);
	}
	
	public Map<BitSet, Map<BitSet,Object>> getMapVertColoToOutEdgeColo(){
		Object[] keyVertexColours = mapOutDegreeBaseVEColo.keys;
		Map< BitSet, Map<BitSet,Object>> res = new HashMap<BitSet, Map<BitSet,Object>>();
		for(int i = 0 ; i< keyVertexColours.length; ++i){
			if(mapOutDegreeBaseVEColo.allocated[i]){
				BitSet vertColo = (BitSet)keyVertexColours[i];
				
				Map<BitSet,Object>linkedOutEdges = res.get(vertColo);
				
				if(linkedOutEdges == null){
					linkedOutEdges = new HashMap<BitSet,Object>();
					res.put(vertColo, linkedOutEdges);
				}
				
				ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapEdgeAndOutDegree = mapOutDegreeBaseVEColo.get(vertColo);
				Object[] keyEdgeColours = mapEdgeAndOutDegree.keys;
				for(int j = 0 ; j < keyEdgeColours.length ; ++j){
					if(mapEdgeAndOutDegree.allocated[j]){
						BitSet edgeColo = (BitSet) keyEdgeColours[j];
						linkedOutEdges.put(edgeColo, null);
					}
				}
			}
		}
		
		return res;
	}
	
	public int getMaxOutDegreeOf(BitSet vertColo, BitSet edgeColo){
		int maxOutDegree = 0;
		ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapOEColoToOutDegree = mapOutDegreeBaseVEColo.get(vertColo);
		if(mapOEColoToOutDegree != null){
			IntIntOpenHashMap mapOutDegree = mapOEColoToOutDegree.get(edgeColo);
			if(mapOutDegree!= null){ 
				int [] arrDataPoints = mapOutDegree.keys;
				for(int i = 0 ; i < arrDataPoints.length; ++i){
					if(mapOutDegree.allocated[i]){
						if(maxOutDegree < arrDataPoints[i]){
							maxOutDegree = arrDataPoints[i];
						}
					}
				}
			}
		}
		return maxOutDegree;
	}
	
	public double getAverageOutDegreeOf(BitSet vertColo, BitSet edgeColo){
		ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapOEColoToOutDegree = mapOutDegreeBaseVEColo.get(vertColo);
		ObjectObjectOpenHashMap<BitSet, IntSet> mapOEColoToVertexIDs = mapVertexIDsBaseEVColo.get(vertColo);
		if(mapOEColoToOutDegree != null && mapOEColoToVertexIDs != null){
			IntIntOpenHashMap mapOutDegree = mapOEColoToOutDegree.get(edgeColo);
			IntSet tailIDs = mapOEColoToVertexIDs.get(edgeColo);
			if(mapOutDegree!= null && tailIDs != null){ 
				int [] arrDegreeSamples = mapOutDegree.keys;
				double totalOutDegree = 0;
				
				for(int i = 0 ; i < arrDegreeSamples.length; ++i){
					if(mapOutDegree.allocated[i]){
						int noOfVertices = mapOutDegree.get(arrDegreeSamples[i]);
						totalOutDegree += (noOfVertices * arrDegreeSamples[i]);
					}
				}
				
				if(tailIDs.size() !=0){
					return (totalOutDegree/tailIDs.size());
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
			int [] arrOutEdgeIDs = grph.getOutEdges(vertId).toIntArray();
			
			ObjectIntOpenHashMap<BitSet> mapOutEdgeDist = new ObjectIntOpenHashMap<BitSet>();
			
			for(int j = 0 ; j < arrOutEdgeIDs.length ; ++j){
				BitSet edgeColo = grph.getEdgeColour(arrOutEdgeIDs[j]); 
				mapOutEdgeDist.putOrAdd(edgeColo, 1, 1);
			}
			
			int iNoOfEdgeColours = mapOutEdgeDist.allocated.length;
			for(int j = 0 ; j< iNoOfEdgeColours ; ++j){
				if(mapOutEdgeDist.allocated[j]){
					
					BitSet edgeColo = (BitSet)((Object[])mapOutEdgeDist.keys)[j];
					int noOfOutEdges = mapOutEdgeDist.get(edgeColo);
					
					// put out degree to the global mapOutDegreeBaseEVColo
					ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapOEColoToOutDegree = mapOutDegreeBaseVEColo.get(vertColo);
					if(mapOEColoToOutDegree == null){
						mapOEColoToOutDegree = new ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap>();
						mapOutDegreeBaseVEColo.put(vertColo, mapOEColoToOutDegree);
					}

					IntIntOpenHashMap mapOutDegree = mapOEColoToOutDegree.get(edgeColo);
					if(mapOutDegree == null){
						mapOutDegree = new IntIntOpenHashMap();
						mapOEColoToOutDegree.put(edgeColo, mapOutDegree);
					}
					mapOutDegree.putOrAdd(noOfOutEdges, 1, 1);
					
					
					//put the vertex id to the global mapVertexIDsBaseEVColo
					ObjectObjectOpenHashMap<BitSet, IntSet> mapOEColoToVertIDs = mapVertexIDsBaseEVColo.get(vertColo) ;
					if(mapOEColoToVertIDs == null){
						mapOEColoToVertIDs = new ObjectObjectOpenHashMap<BitSet, IntSet>();
						mapVertexIDsBaseEVColo.put(vertColo, mapOEColoToVertIDs);
					}
					
					IntSet setTailIDs = mapOEColoToVertIDs.get(edgeColo);
					if(setTailIDs == null){
						setTailIDs = new DefaultIntSet();
						mapOEColoToVertIDs.put(edgeColo, setTailIDs);
					}
					setTailIDs.add(vertId);
				}
			}
		}
	}
	
	
	private double getAverageOutDegree(BitSet vertColo){
		ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapOutDegreeAndEdge = mapOutDegreeBaseVEColo.get(vertColo);
		if(mapOutDegreeAndEdge != null){
			Object[] keyEdgeColo = mapOutDegreeAndEdge.keys;
			if(keyEdgeColo!= null && keyEdgeColo.length > 0){
				double avrgOutDegree = 0;
				int totalVertices = 0;
				
				// for each of edge colour
				for(int i = 0 ; i < mapOutDegreeAndEdge.allocated.length ; ++i){
					if(mapOutDegreeAndEdge.allocated[i]){
						BitSet edgeColo = (BitSet)keyEdgeColo[i];
						IntIntOpenHashMap mapOutDegree = mapOutDegreeAndEdge.get(edgeColo);
						if(mapOutDegree!= null){ 
							int [] arrDataPoints = mapOutDegree.keys;
							
							// for each of degree
							for(int j = 0 ; i < arrDataPoints.length; ++j){
								if(mapOutDegree.allocated[j]){
									int noOfVertices = mapOutDegree.get(arrDataPoints[j]);
									totalVertices += noOfVertices;
									avrgOutDegree += (noOfVertices * arrDataPoints[j]);
								}
							}
						}
					}
				}
				
				if(totalVertices != 0){
					return (avrgOutDegree/totalVertices);
				}
			}
		}
		return 0;
	}
	
	private IntIntOpenHashMap getOutDegreeDistributionOf(BitSet vertColo, BitSet edgeColo){
		ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapOutEdgeAndDegree = mapOutDegreeBaseVEColo.get(vertColo);
		if(mapOutEdgeAndDegree != null){
			return mapOutEdgeAndDegree.get(edgeColo);
		}
		return null;
	}
	
	private void applyMultiThread(ColouredGraph grph){
		new MultiThreadProcessing(grph.getVertices()) {
			
			@Override
			protected void run(int threadID, int vertId) {
				BitSet vertColo = grph.getVertexColour(vertId);
				
				IntSet outEdgeIDs = grph.getOutEdges(vertId);
				int [] arrOutEdgeIDs = outEdgeIDs.toIntArray();
				
				ObjectIntOpenHashMap<BitSet> mapEdgeColours = new ObjectIntOpenHashMap<BitSet>();
				
				for(int i = 0 ; i < arrOutEdgeIDs.length ; ++i){
					BitSet edgeColo = grph.getEdgeColour(arrOutEdgeIDs[i]); 
					mapEdgeColours.putOrAdd(edgeColo, 1, 1);
				}
				
				synchronized(mapOutDegreeBaseVEColo){
					int iNoOfEdgeColours = mapEdgeColours.allocated.length;
					for(int i = 0 ; i< iNoOfEdgeColours ; ++i){
						if(mapEdgeColours.allocated[i]){
							BitSet edgeColo = (BitSet)((Object[])mapEdgeColours.keys)[i];
							int noOfOutEdges = mapEdgeColours.get(edgeColo);
							
							
							// put to the global map
							ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapOutEdgeAndDegree = mapOutDegreeBaseVEColo.get(vertColo);
							if(mapOutEdgeAndDegree == null){
								mapOutEdgeAndDegree = new ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap>();
								mapOutDegreeBaseVEColo.put(vertColo, mapOutEdgeAndDegree);
							}
							
							IntIntOpenHashMap mapOutDegree = mapOutEdgeAndDegree.get(edgeColo);
							if(mapOutDegree == null){
								mapOutDegree = new IntIntOpenHashMap();
								mapOutEdgeAndDegree.put(edgeColo, mapOutDegree);
							}
							
							mapOutDegree.putOrAdd(noOfOutEdges, 1, 1);
						}
					}
				}
			}
		};
	}
	
	public void printColourInfo(){
		Object[] keyVertColours = mapOutDegreeBaseVEColo.keys;
		int iNoOfKeyVertColours = keyVertColours.length;
		
		int noOfVertices = 0;
		int noOfEdges = 0;
		
		for(int i= 0 ; i< iNoOfKeyVertColours ; i++){
			if(mapOutDegreeBaseVEColo.allocated[i]){
				BitSet vertColo = (BitSet)keyVertColours[i];
				
				ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> mapEdgeAndDegree = mapOutDegreeBaseVEColo.get(vertColo);
				
				Object [] keyEdgeColours = mapEdgeAndDegree.keys;
				int iNoOfKeyEdgeColours = keyEdgeColours.length;
				for(int j = 0 ; j < iNoOfKeyEdgeColours ; j++){
					if(mapEdgeAndDegree.allocated[j]){
						BitSet edgeColo = (BitSet)keyEdgeColours[j];
						
						IntSet vertIDs = mapVertexIDsBaseEVColo.get(vertColo).get(edgeColo);
						noOfVertices += vertIDs.size();
						
						
						IntIntOpenHashMap distribution = mapEdgeAndDegree.get(edgeColo);
						
						int[] degree = distribution.keys;
						int[] values = distribution.values;
						
						int iNoOfItems = degree.length;
						for(int k = 0 ; k < iNoOfItems; k++){
							noOfEdges += (values[k] * degree[k]);
						}
						
					}
				}
			}
		}
		System.out.println("Number of vertices: "+ noOfVertices + " - number of edges: " + noOfEdges);
	}
}
