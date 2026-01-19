package org.aksw.simba.lemming.mimicgraph.constraints;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class extends the ColourMappingRules that evaluates all triples of input graphs. Instead this class will consider only specific triples for simplexes. 
 * The triples to be considered from the input graphs are specified using edge Ids for them.
 */
public class ColourMappingRulesSimplexes extends ColourMappingRules{
	
	/**
	 * Map that stores edges ids for different input graphs. Here, the key is integer value which is 1 for first input graph, 2 for second input graph and so on.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIds;
	
	public ColourMappingRulesSimplexes(ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIds){
		super();
		this.mGraphsEdgesIds = mGraphsEdgesIds;
	}
	
	protected void analyzeRulesWithSingleThread(ColouredGraph[] origGrphs){
		
		int graphId = 1; // Key for mGraphsEdgesIds map
		
		
		for(ColouredGraph grph: origGrphs){
			if(grph!=null) {
				
				//get the edge Ids to consider for triples
				IntSet edgesToConsider = mGraphsEdgesIds.get(graphId);
				
				IntSet setofVIDs = grph.getVertices();
				int[] arrOfVIDs = setofVIDs.toIntArray();
				
				for(int tailId : arrOfVIDs){
					BitSet tailColo = grph.getVertexColour(tailId);
					IntSet setOfOEIDs = grph.getOutEdges(tailId);
					if(setOfOEIDs != null && setOfOEIDs.size() > 0){
						int[] arrOfOEIDs = setOfOEIDs.toIntArray();
						
						for(int oeId: arrOfOEIDs){
							
							//condition to check edge
							if (edgesToConsider.contains(oeId)) {
							
								BitSet edgeColo = grph.getEdgeColour(oeId);
								
								int headId = grph.getHeadOfTheEdge(oeId);
								BitSet headColo = grph.getVertexColour(headId);
								
								/*
								 *  put to map
								 */
								
								//map head to tail
								Set<BitSet> setTailColo = mMapHeadColoToTailColo.get(headColo);
								if(setTailColo == null){
									setTailColo = new HashSet<BitSet>();
									mMapHeadColoToTailColo.put(headColo, setTailColo);
								}
								setTailColo.add(tailColo);
								
								
								//map tail to head
								Set<BitSet> setHeadColo = mMapTailColoToHeadColo.get(tailColo);
								if(setHeadColo == null){
									setHeadColo = new HashSet<BitSet>();
									mMapTailColoToHeadColo.put(tailColo, setHeadColo);
								}
								setHeadColo.add(headColo);
								
								//map edge to head and tail
								ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColo);
								if(mapHeadToTail == null){
									mapHeadToTail = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
									mMapEdgeColoToHeadAndTailColo.put(edgeColo, mapHeadToTail);
								}
								
								setTailColo = mapHeadToTail.get(headColo);
								if(setTailColo == null){
									setTailColo = new HashSet<BitSet>();
									mapHeadToTail.put(headColo, setTailColo);
								}
								setTailColo.add(tailColo);
								
								//map edge to tail and head
								ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapTailToHead = mMapEdgeColoToTailAndHeadColo.get(edgeColo);
								if(mapTailToHead == null){
									mapTailToHead = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
									mMapEdgeColoToTailAndHeadColo.put(edgeColo, mapTailToHead);
								}
								
								setHeadColo = mapTailToHead.get(tailColo);
								if(setHeadColo == null){
									setHeadColo = new HashSet<BitSet>();
									mapTailToHead.put(tailColo, setHeadColo);
								}
								setHeadColo.add(headColo);
						}
						}
					}
					
					//TODO: Data Typed Edge Colours are not for specific edges, Need to check
					// process for literals
					Set<BitSet> setOfDTEColours = grph.getDataTypedEdgeColours(tailColo);
					if(setOfDTEColours != null){
						Set<BitSet> setLinkedDTEColours = mMapVColoToDTEColo.get(tailColo);
						
						if(setLinkedDTEColours == null){
							setLinkedDTEColours = new HashSet<BitSet>();
							mMapVColoToDTEColo.put(tailColo, setLinkedDTEColours);
						}
						
						for(BitSet dteColo : setOfDTEColours){
							setLinkedDTEColours.add(dteColo);
	
							Set<BitSet> setLinkedVColours = mMapDTEColoToVColo.get(dteColo);
							if(setLinkedVColours == null){
								setLinkedVColours = new HashSet<BitSet>();
								mMapDTEColoToVColo.put(dteColo, setLinkedVColours);
							}
							setLinkedVColours.add(tailColo);
						}
					}
				}
			graphId++; // increment graph id
			}
		}
	}

}
