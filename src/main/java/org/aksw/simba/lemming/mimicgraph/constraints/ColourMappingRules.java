package org.aksw.simba.lemming.mimicgraph.constraints;

import grph.algo.MultiThreadProcessing;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * The colour mapper for vertices' and edges' colours.
 * 
 * NOTE: tail --> head (tail has out going edges, and head has incoming edges)
 * 		   
 * @author nptsy
 */
public class ColourMappingRules implements IColourMappingRules{

	/*
	 * the keys are the head's colours and the values are the set of tail's colours
	 */
	private ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mMapHeadColoToTailColo;
	/*
	 * the keys are the tail's colours and the values are the set of head's colours
	 */
	private ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mMapTailColoToHeadColo;
	
	/*
	 * key1: edge's colours, key2: head's colours and the values are the set of tail's colours 
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, Set<BitSet>>> mMapEdgeColoToHeadAndTailColo;
	
	/*
	 * key1: edge's colours, key2: tail's colours and the values are the set of head's colours
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, Set<BitSet>>> mMapEdgeColoToTailAndHeadColo;

	private ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mMapDTEColoToVColo;
	
	private ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mMapVColoToDTEColo;
	
	boolean mIsMultiThreadProcessing = false;
	
	/**
	 * Constructor
	 */
	public ColourMappingRules(){
		mMapHeadColoToTailColo = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
		mMapTailColoToHeadColo = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
		mMapEdgeColoToHeadAndTailColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, Set<BitSet>>>();
		mMapEdgeColoToTailAndHeadColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, Set<BitSet>>>();
		mMapDTEColoToVColo = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
		mMapVColoToDTEColo = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
	}
	
	public void analyzeRules(ColouredGraph[] origGrphs) {
		analyzeRulesWithSingleThread(origGrphs);
	}
	
	private void analyzeRulesWithSingleThread(ColouredGraph[] origGrphs){
		for(ColouredGraph grph: origGrphs){
			IntSet setofVIDs = grph.getVertices();
			int[] arrOfVIDs = setofVIDs.toIntArray();
			
			for(int tailId : arrOfVIDs){
				BitSet tailColo = grph.getVertexColour(tailId);
				IntSet setOfOEIDs = grph.getOutEdges(tailId);
				if(setOfOEIDs != null && setOfOEIDs.size() > 0){
					int[] arrOfOEIDs = setOfOEIDs.toIntArray();
					
					for(int oeId: arrOfOEIDs){
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
		}
	}
	
	public Set<BitSet> getHeadColoursFromEdgeColour(BitSet edgeColour) {
		Set<BitSet> setColours = new HashSet<BitSet>();
		if(edgeColour != null){
			ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColour);
			if(mapHeadToTail != null){
				Object[] arrHeadColours = mapHeadToTail.keys;
				int iNoOfHeadColours = arrHeadColours.length;
				for(int i = 0 ; i < iNoOfHeadColours ; i++){
					if(mapHeadToTail.allocated[i]){
						BitSet headColo = (BitSet) arrHeadColours[i];
						setColours.add(headColo);
					}
				}
			}
		}
		return setColours;
	}

	@Override
	public Set<BitSet> getTailColoursFromEdgeColour(BitSet edgeColour) {
		Set<BitSet> setColours = new HashSet<BitSet>();
		if(edgeColour != null){
			ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapTailToHead = mMapEdgeColoToTailAndHeadColo.get(edgeColour);
			if(mapTailToHead != null){
				Object[] arrTailColours = mapTailToHead.keys;
				int iNoOfTailColours = arrTailColours.length;
				for(int i = 0 ; i < iNoOfTailColours ; i++){
					if(mapTailToHead.allocated[i]){
						BitSet tailColo = (BitSet) arrTailColours[i];
						setColours.add(tailColo);
					}
				}
			}
		}
		return setColours;
	}

	@Override
	public Set<BitSet> getHeadColours(BitSet tailColour) {
		if(tailColour != null){
			Set<BitSet> lstHeadColours = mMapTailColoToHeadColo.get(tailColour);
			if(lstHeadColours != null ){
				return lstHeadColours;
			}
		}
		return new HashSet<BitSet>();
	}

	@Override
	public Set<BitSet> getHeadColours(BitSet tailColour, BitSet edgeColour) {
		if(tailColour != null && edgeColour == null){
			return getHeadColours(tailColour);
		}else{
			Set<BitSet> setColours = new HashSet<BitSet>();
			
			if(tailColour != null && edgeColour != null){
				ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapTailToHead = mMapEdgeColoToTailAndHeadColo.get(edgeColour);
				if(mapTailToHead != null){
					Set<BitSet> setHeadColours = mapTailToHead.get(tailColour);
					if(setHeadColours != null){
						return setHeadColours;
					}
				}
			}else{
				if(tailColour == null && edgeColour == null){
					Object[] arrHeadColours = mMapHeadColoToTailColo.keys;
					if(arrHeadColours != null ){
						int iNoOfHeadColours = arrHeadColours.length;
						for(int i = 0 ; i < iNoOfHeadColours ; i++){
							if(mMapHeadColoToTailColo.allocated[i]){
								BitSet headColo = (BitSet) arrHeadColours[i];
								setColours.add(headColo);
							}
						}
					}
				}else{
					ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColour);
					if(mapHeadToTail != null){
						Object[] arrHeadColours = mapHeadToTail.keys;
						if(arrHeadColours != null ){
							int iNoOfHeadColours = arrHeadColours.length;
							for(int i = 0 ; i < iNoOfHeadColours ; i++){
								if(mapHeadToTail.allocated[i]){
									BitSet headColo = (BitSet) arrHeadColours[i];
									setColours.add(headColo);
								}
							}
						}
					}
				}
			}
			return setColours;
		}
	}

	@Override
	public Set<BitSet> getTailColours(BitSet headColour) {
		if(headColour != null){
			Set<BitSet> setTailColours = mMapHeadColoToTailColo.get(headColour);
			if(setTailColours != null ){
				return setTailColours;
			}
		}
		return new HashSet<BitSet>();
	}

	@Override
	public Set<BitSet> getTailColours(BitSet headColour, BitSet edgeColour) {
		if(headColour != null && edgeColour == null){
			return getTailColours(headColour);
		}else{
			Set<BitSet> setColours = new HashSet<BitSet>();
			if(headColour != null && edgeColour != null){
				ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColour);
				if(mapHeadToTail != null){
					Set<BitSet> setTailColours = mapHeadToTail.get(headColour);
					if(setTailColours != null){
						return setTailColours;
					}
				}
			}else{
				if(headColour == null && edgeColour == null){
					Object[] arrTailColours = mMapTailColoToHeadColo.keys;
					if(arrTailColours != null ){
						int iNoOftailColours = arrTailColours.length;
						for(int i = 0 ; i < iNoOftailColours ; i++){
							if(mMapTailColoToHeadColo.allocated[i]){
								BitSet tailColo = (BitSet)arrTailColours[i];
								setColours.add(tailColo);
							}
						}
					}	
				}else{
					ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapTailToHead = mMapEdgeColoToTailAndHeadColo.get(edgeColour);
					Object[] arrTailColours = mapTailToHead.keys;
					if(arrTailColours != null ){
						int iNoOftailColours = arrTailColours.length;
						for(int i = 0 ; i < iNoOftailColours ; i++){
							if(mapTailToHead.allocated[i]){
								BitSet tailColo = (BitSet)arrTailColours[i];
								setColours.add(tailColo);
							}
						}
					}	
				}
			}
			return setColours;
		}
	}

	public Set<BitSet> getPossibleOutEdgeColours(BitSet tailColour){
		Set<BitSet> setColours = new HashSet<BitSet>();
		if(tailColour != null){
			Object[] arrEdgeColours = mMapEdgeColoToTailAndHeadColo.keys;
			
			int iNoOfEdgeColours = arrEdgeColours.length;
			for(int i = 0 ; i < iNoOfEdgeColours ;i++){
				if(mMapEdgeColoToTailAndHeadColo.allocated[i]){
					BitSet edgeColo = (BitSet) arrEdgeColours[i];
					ObjectObjectOpenHashMap<BitSet, Set<BitSet>>  mapTailToHead = mMapEdgeColoToTailAndHeadColo.get(edgeColo);
					if(mapTailToHead!= null && mapTailToHead.containsKey(tailColour)){
						setColours.add(edgeColo);
					}
				}
			}
		}
		return setColours;
	}
	
	public Set<BitSet> getPossibleInEdgeColours(BitSet headColour){
		Set<BitSet> setColours = new HashSet<BitSet>();
		if(headColour != null){
			Object[] arrEdgeColours = mMapEdgeColoToHeadAndTailColo.keys;
			
			int iNoOfEdgeColours = arrEdgeColours.length;
			for(int i = 0 ; i < iNoOfEdgeColours ;i++){
				if(mMapEdgeColoToHeadAndTailColo.allocated[i]){
					BitSet edgeColo = (BitSet) arrEdgeColours[i];
					ObjectObjectOpenHashMap<BitSet, Set<BitSet>>  mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColo);
					if(mapHeadToTail != null && mapHeadToTail.containsKey(headColour)){
						setColours.add(edgeColo);
					}
				}
			}
		}
		return setColours;
	}
	
	@Override
	public Set<BitSet> getPossibleLinkingEdgeColours(BitSet tailColour, BitSet headColour
			) {
		
		Set<BitSet> setColours = new HashSet<BitSet>();

		if(headColour == null && tailColour == null){
			return setColours;
		}
		
		Object[] arrEdgeColours = mMapEdgeColoToHeadAndTailColo.keys;
		int iNoOfEdgeColours = arrEdgeColours.length;
		for(int i = 0 ; i < iNoOfEdgeColours; i++){
			if(headColour != null && tailColour != null){
				if(mMapEdgeColoToHeadAndTailColo.allocated[i]){
					BitSet edgeColo = (BitSet) arrEdgeColours[i];
					ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColo);
					if(mapHeadToTail != null){
						Set<BitSet> setTailColours = mapHeadToTail.get(headColour);
						if(setTailColours != null && setTailColours.contains(tailColour)){
							setColours.add(edgeColo);
						}
					}
				}
			}// end if of the case when both headColour and tailColour are not null
			else{
				if(headColour != null && tailColour == null){
					if(mMapEdgeColoToHeadAndTailColo.allocated[i]){
						BitSet edgeColo = (BitSet) arrEdgeColours[i];
						ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColo);
						if(mapHeadToTail != null && mapHeadToTail.containsKey(headColour)){
							setColours.add(edgeColo);
						}
					}
				} // end if of the case when headColour is not null and tailColour is null
				else{
					if(headColour == null && tailColour != null){
						if(mMapEdgeColoToHeadAndTailColo.allocated[i]){
							BitSet edgeColo = (BitSet) arrEdgeColours[i];
							ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColo);
							if(mapHeadToTail != null){
								Object[] arrHeadColours = mapHeadToTail.keys;
								int iNoOfHeadColours = arrHeadColours.length;
								for(int j = 0 ;j < iNoOfHeadColours ; j++){
									if(mapHeadToTail.allocated[j]){
										BitSet headColo = (BitSet) arrHeadColours[j];
										Set<BitSet> setTailColours = mapHeadToTail.get(headColo);
										if(setTailColours != null && setTailColours.contains(tailColour)){
											setColours.add(edgeColo);
										}
									}
								}
							}
						}
					}// end if of case when headColour is null and tailColour is not null
				}
			}
		}
		return setColours;
	}

	@Override
	public boolean isHeadColourOf(BitSet tailColour, BitSet checkedColour) {
		if(tailColour!=null && checkedColour != null){
			Set<BitSet> setTailColours = mMapHeadColoToTailColo.get(checkedColour);
			if(setTailColours!= null && setTailColours.contains(tailColour)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isTailColourOf(BitSet headColour, BitSet checkedColour) {
		if(headColour != null && checkedColour != null){
			Set<BitSet> setTailColours = mMapHeadColoToTailColo.get(headColour);
			if(setTailColours!= null && setTailColours.contains(checkedColour)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canConnect(BitSet tailColour, BitSet headColour, 
			BitSet edgeColour) {
		
		if(headColour != null && tailColour != null ){
			if(edgeColour!=null){
				ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColour) ;
				if(mapHeadToTail!= null){
					Set<BitSet> setTailColours = mapHeadToTail.get(headColour);
					if(setTailColours != null && setTailColours.contains(tailColour)){
						return true;
					}
				}	
			}else{
				Set<BitSet> setHeadColours = mMapTailColoToHeadColo.get(tailColour);
				if(setHeadColours != null && setHeadColours.contains(headColour)){
					return true;
				}
			}
			
		}
		return false;
	}
	
	
	
	
	@Deprecated
	private void analyzeRulesWithMultiThreads(ColouredGraph[] origGrphs){
		
		for(ColouredGraph grph: origGrphs){
			new MultiThreadProcessing(grph.getEdges()) {
				
				@Override
				protected void run(int threadID, int edgeId) {
					BitSet edgeColo = grph.getEdgeColour(edgeId);
					IntSet incidentVertices  = grph.getVerticesIncidentToEdge(edgeId);
					if(incidentVertices != null){
						int[] arrIncidentVertices = incidentVertices.toIntArray();
						int tailId = arrIncidentVertices[0];
						BitSet tailColo = grph.getVertexColour(tailId);
						
						int headId = arrIncidentVertices.length == 1 ? arrIncidentVertices[0] : arrIncidentVertices[1];
						BitSet headColo = grph.getVertexColour(headId);
						
						synchronized(mMapHeadColoToTailColo){
							//map head to tail
							Set<BitSet> setTailColo = mMapHeadColoToTailColo.get(headColo);
							if(setTailColo == null){
								setTailColo = new HashSet<BitSet>();
								mMapHeadColoToTailColo.put(headColo, setTailColo);
							}
							setTailColo.add(tailColo);	
						}
						
						
						synchronized(mMapTailColoToHeadColo){
							//map tail to head
							Set<BitSet> setHeadColo = mMapTailColoToHeadColo.get(tailColo);
							if(setHeadColo == null){
								setHeadColo = new HashSet<BitSet>();
								mMapTailColoToHeadColo.put(tailColo, setHeadColo);
							}
							setHeadColo.add(headColo);
						}
						
						synchronized(mMapEdgeColoToHeadAndTailColo){
							//map edge to head and tail
							ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapHeadToTail = mMapEdgeColoToHeadAndTailColo.get(edgeColo);
							if(mapHeadToTail == null){
								mapHeadToTail = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
								mMapEdgeColoToHeadAndTailColo.put(edgeColo, mapHeadToTail);
							}
							
							Set<BitSet> setTailColo = mapHeadToTail.get(headColo);
							if(setTailColo == null){
								setTailColo = new HashSet<BitSet>();
								mapHeadToTail.put(headColo, setTailColo);
							}
							setTailColo.add(tailColo);
						}
						
						synchronized(mMapEdgeColoToTailAndHeadColo){
							//map edge to tail and head
							ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapTailToHead = mMapEdgeColoToTailAndHeadColo.get(edgeColo);
							if(mapTailToHead == null){
								mapTailToHead = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
								mMapEdgeColoToTailAndHeadColo.put(edgeColo, mapTailToHead);
							}
							
							Set<BitSet> setHeadColo = mapTailToHead.get(tailColo);
							if(setHeadColo == null){
								setHeadColo = new HashSet<BitSet>();
								mapTailToHead.put(tailColo, setHeadColo);
							}
							setHeadColo.add(headColo);
						}
					}
				}
			};
		}
	}
	
	@Deprecated
	private void analyzeRulesWithSingleThreadF(ColouredGraph[] origGrphs){
		
		for(ColouredGraph grph: origGrphs){
			IntSet setEdgeIDs = grph.getEdges();
			int[] arrEdgeIDs = setEdgeIDs.toIntArray();
			
			for(int edgeId: arrEdgeIDs){
				BitSet edgeColo = grph.getEdgeColour(edgeId);
				IntSet incidentVertices  = grph.getVerticesIncidentToEdge(edgeId);
				if(incidentVertices != null){
					int[] arrIncidentVertices = incidentVertices.toIntArray();
					
					int headId = arrIncidentVertices.length == 1 ? arrIncidentVertices[0] : arrIncidentVertices[1];
					BitSet headColo = grph.getVertexColour(headId);
					
					int tailId = arrIncidentVertices[0];
					BitSet tailColo = grph.getVertexColour(tailId);
					
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
	}
	
}
