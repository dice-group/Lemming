package org.aksw.simba.lemming.rules;

import grph.algo.MultiThreadProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.MapUtil;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;


public class TripleColourDistributionMetric  {

	/*
	 * 1st key: tail's colour, 2nd key: head's colour, 3rd key: edge's colour
	 * and the value is the set of distinguish tail's id
	 */
	private Map<BitSet, Map <BitSet, Map<BitSet, IntSet>>> mTripleMapTailEdgeHeads;

	/*
	 * 1st key: head's colour, 2nd key: tail's  colour, 3rd key: edge's colour
	 * and the value is the set of distinguish head's id
	 */
	private Map<BitSet, Map <BitSet, Map<BitSet, IntSet>>> mTripleMapHeadEdgeTails;
	
	/*
	 * 1st key: edge's colour, 2nd key: tail's  colour, 3rd key: head's colour
	 * and the value is the set of distinguish edge's id
	 */
	private Map<BitSet, Map <BitSet, Map<BitSet, IntSet>>> mTripleMapTailHeadEdges;

	private ObjectDoubleOpenHashMap<BitSet> mMapVertColoDist;
	private ObjectDoubleOpenHashMap<BitSet> mMapEdgeColoDist;
	
	
	private ColouredGraph testVarGrph;
	/**
	 * Constructor
	 */
	public TripleColourDistributionMetric(){
		mTripleMapHeadEdgeTails = new HashMap<BitSet, Map<BitSet, Map<BitSet, IntSet>>>();
		mTripleMapTailHeadEdges = new HashMap<BitSet, Map<BitSet, Map<BitSet, IntSet>>>();
		mTripleMapTailEdgeHeads = new HashMap<BitSet, Map<BitSet, Map<BitSet, IntSet>>>();
		mMapVertColoDist = new ObjectDoubleOpenHashMap<BitSet>();
		mMapEdgeColoDist = new ObjectDoubleOpenHashMap<BitSet>();
	}

	public double getTotalNoOfEdgesIn(BitSet edgeColo){
		if(mMapEdgeColoDist!=null){
			return mMapEdgeColoDist.get(edgeColo);
		}
		return 0.;
	}
	
	public double getTotalNoOfVerticesIn(BitSet vertColo){
		if(mMapVertColoDist!= null){
			return mMapVertColoDist.get(vertColo);
		}
		return 0.;
	}
	
	public double getNoOfIncidentTails(BitSet tailColo, BitSet edgeColo, BitSet headColo){
		if(mTripleMapHeadEdgeTails.containsKey(headColo)){
			Map<BitSet, Map<BitSet, IntSet>> mapEdgeToTails = mTripleMapHeadEdgeTails.get(headColo);
			if(mapEdgeToTails.containsKey(edgeColo)){
				Map<BitSet, IntSet> mapTails = mapEdgeToTails.get(edgeColo);
				
				if(mapTails.containsKey(tailColo)){
					IntSet setTailIDs = mapTails.get(tailColo);
					if(setTailIDs!= null){
						return setTailIDs.size();
					}
				}
			}
		}
		return 0.;
	}
	
	public double getNoOfIncidentHeads(BitSet tailColo, BitSet edgeColo, BitSet headColo){
		if(mTripleMapTailEdgeHeads.containsKey(tailColo)){
			Map<BitSet, Map<BitSet, IntSet>> mapEdgeToHeads = mTripleMapTailEdgeHeads.get(tailColo);
			
			if(mapEdgeToHeads.containsKey(edgeColo)){
				Map<BitSet, IntSet> mapHeads = mapEdgeToHeads.get(edgeColo);
				
				if(mapHeads.containsKey(headColo)){
					IntSet setHeadIDs = mapHeads.get(headColo);
					if(setHeadIDs != null){
						return setHeadIDs.size();
					}
				}
			}
		}
		return 0.;
	}
	
	public double getNoOfIncidentEdges(BitSet tailColo, BitSet edgeColo, BitSet headColo){
		if(mTripleMapTailHeadEdges.containsKey(tailColo)){
			Map<BitSet, Map<BitSet, IntSet>> mapHeadToEdges = mTripleMapTailHeadEdges.get(tailColo);
			
			if(mapHeadToEdges.containsKey(headColo)){
				Map<BitSet, IntSet> mapEdges = mapHeadToEdges.get(headColo);
				
				if(mapEdges.containsKey(edgeColo)){
					IntSet setEdgeIDs = mapEdges.get(edgeColo);
					if(setEdgeIDs != null){
						return setEdgeIDs.size();
					}
				}
			}
		}
		return 0.;
	}
	
	public void applyWithSingleThread(ColouredGraph grph){
		testVarGrph = grph;
		IntSet setOfVertices = grph.getVertices();
		int[] arrVertIDs = setOfVertices.toIntArray();
		int iNoOfVertices = setOfVertices.size();
		for(int i = 0 ; i< iNoOfVertices ; i++){
			
			int tId = arrVertIDs[i];
			BitSet tColo = grph.getVertexColour(tId);
			mMapVertColoDist.putOrAdd(tColo, 1, 1);
			
			IntSet outEdges = grph.getOutEdges(tId);
			
			int[] outEIDs = outEdges.toIntArray();
			
			for(int edgeId : outEIDs){
				BitSet edgeColo = grph.getEdgeColour(edgeId);
				
				mMapEdgeColoDist.putOrAdd(edgeColo, 1, 1);
				
				int hId = grph.getHeadOfTheEdge(edgeId);
				BitSet hColo = grph.getVertexColour(hId);
			
				putToMap(hColo, edgeColo, tColo, tId, mTripleMapHeadEdgeTails);
				putToMap(tColo, edgeColo, hColo, hId, mTripleMapTailEdgeHeads);
				putToMap(tColo, hColo, edgeColo, edgeId, mTripleMapTailHeadEdges);
					
			}
		}
	}
		
	private void putToMap( BitSet firstKey, BitSet secondKey, BitSet thirdKey, int itemID,
			Map<BitSet, Map<BitSet, Map<BitSet, IntSet>>> changedMap) {

		// first key
		Map<BitSet, Map<BitSet, IntSet>> mapSecondToItems = changedMap
				.get(firstKey);
		if (mapSecondToItems == null) {
			mapSecondToItems = new HashMap<BitSet, Map<BitSet, IntSet>>();
			changedMap.put(firstKey, mapSecondToItems);
		}

		// second key
		Map<BitSet, IntSet> mapItems = mapSecondToItems.get(secondKey);

		if (mapItems == null) {
			mapItems = new HashMap<BitSet, IntSet>();
			mapSecondToItems.put(secondKey, mapItems);
		}

		// third key
		IntSet setItemIDs = mapItems.get(thirdKey);
		if (setItemIDs == null) {
			setItemIDs = new DefaultIntSet();
			mapItems.put(thirdKey, setItemIDs);
		}

		// item id
		setItemIDs.add(itemID);
	}
	
	public void applyWithMultiThread(ColouredGraph grph){
		testVarGrph = grph;
		
		new MultiThreadProcessing(grph.getVertices()) {
			
			@Override
			protected void run(int threadID, int tId) {
				//tail's colour
				BitSet tColo = grph.getVertexColour(tId);
				
				
				synchronized(mMapVertColoDist){
					mMapVertColoDist.putOrAdd(tColo, 1, 1);
				}
				
				//out edges of the tail id
				IntSet outEdges = grph.getOutEdges(tId);
				int[] outEIDs = outEdges.toIntArray();
				
				
				for(int edgeId : outEIDs){
					// out edge's Colour
					BitSet edgeColo = grph.getEdgeColour(edgeId);
					
					synchronized(mMapEdgeColoDist){
						mMapEdgeColoDist.putOrAdd(edgeColo, 1, 1);
					}
					
					// get head id based on the in edgeId
					int hId = grph.getHeadOfTheEdge(edgeId);
					// head's colour
					BitSet hColo = grph.getVertexColour(hId);
				
					synchronized(mTripleMapHeadEdgeTails){
						// head color
						Map<BitSet, Map<BitSet, IntSet>> mapEdgeToTails = mTripleMapHeadEdgeTails.get(hColo);
						if(mapEdgeToTails == null){
							mapEdgeToTails = new HashMap<BitSet, Map<BitSet, IntSet>>();
							mTripleMapHeadEdgeTails.put(hColo, mapEdgeToTails);
						}
						
						// edge color
						Map<BitSet, IntSet> mapTails = mapEdgeToTails.get(edgeColo);
						
						if(mapTails == null){
							mapTails = new HashMap<BitSet, IntSet>();
							mapEdgeToTails.put(edgeColo, mapTails);
						}
						
						// tail color
						IntSet setTailIDs = mapTails.get(tColo);
						if(setTailIDs == null){
							setTailIDs = new DefaultIntSet();
							mapTails.put(tColo, setTailIDs);
						}
						setTailIDs.add(tId);
					}
					
					synchronized(mTripleMapTailEdgeHeads){
						// tail color
						Map<BitSet, Map<BitSet, IntSet>> mapEdgeToHeads = mTripleMapTailEdgeHeads.get(tColo);
						if(mapEdgeToHeads == null){
							mapEdgeToHeads = new HashMap<BitSet, Map<BitSet, IntSet>>();
							mTripleMapTailEdgeHeads.put(tColo, mapEdgeToHeads);
						}
						
						// edge color
						Map<BitSet, IntSet> mapHeads = mapEdgeToHeads.get(edgeColo);
						
						if(mapHeads == null){
							mapHeads = new HashMap<BitSet, IntSet>();
							mapEdgeToHeads.put(edgeColo, mapHeads);
						}
						
						// head color
						IntSet setHeadIDs = mapHeads.get(hColo);
						if(setHeadIDs == null){
							setHeadIDs = new DefaultIntSet();
							mapHeads.put(hColo, setHeadIDs);
						}
						
						setHeadIDs.add(hId);
					}
					
					synchronized(mTripleMapTailHeadEdges){
						// head color
						Map<BitSet, Map<BitSet, IntSet>> mapHeadToEdges = mTripleMapTailHeadEdges.get(tColo);
						if(mapHeadToEdges == null){
							mapHeadToEdges = new HashMap<BitSet, Map<BitSet, IntSet>>();
							mTripleMapTailHeadEdges.put(tColo, mapHeadToEdges);
						}
						
						// head color
						Map<BitSet, IntSet> mapEdges = mapHeadToEdges.get(hColo);
						
						if(mapEdges == null){
							mapEdges = new HashMap<BitSet, IntSet>();
							mapHeadToEdges.put(hColo, mapEdges);
						}
						
						// edge color
						IntSet setEdgeIDs = mapEdges.get(edgeColo);
						if(setEdgeIDs == null){
							setEdgeIDs = new DefaultIntSet();
							mapEdges.put(edgeColo, setEdgeIDs);
						}
						setEdgeIDs.add(edgeId);
					}
				}
			}
		};
	}
	
	
	/**
	 * for testing purpose 
	 * print out important information regarding data storing in variables
	 */
	public void printInfo(){
		
		Set<BitSet> setEdgeColo = MapUtil.keysToSet(mMapEdgeColoDist);
		Set<BitSet> setVertColo = MapUtil.keysToSet(mMapVertColoDist);
		
		
		
		for (BitSet tailColo : setVertColo) {

			Map<BitSet, List<IntSet>> lstSetOfTails = new HashMap<BitSet, List<IntSet>>();

			if (mTripleMapTailHeadEdges.containsKey(tailColo)) {
				Map<BitSet, Map<BitSet, IntSet>> mapHeadToEdges = mTripleMapTailHeadEdges.get(tailColo);
				for (BitSet headColo : setVertColo) {
					for (BitSet edgeColo : setEdgeColo) {
						if (mapHeadToEdges.containsKey(headColo)) {
							Map<BitSet, IntSet> mapEdges = mapHeadToEdges.get(headColo);
							if (mapEdges.containsKey(edgeColo)) {
								IntSet setTails = mTripleMapHeadEdgeTails.get(headColo).get(edgeColo).get(tailColo);
								List<IntSet> lst = lstSetOfTails.get(edgeColo);
								if(lst == null ){
									lst = new ArrayList<IntSet>();
									lstSetOfTails.put(edgeColo, lst);
								}
								lst.add(setTails);
							}
						}
					}
				}
			}

			
			System.out.println("Tail colour is " + tailColo + " has " + mMapVertColoDist.get(tailColo));
			IntSet setOfAllVertices = testVarGrph.getVertices(tailColo);

			Set<Integer> testSetInt = MapUtil.convert(setOfAllVertices);
			
			System.out.println("Vertices: " + setOfAllVertices);
			Set<BitSet> setEdgeColoTmp = lstSetOfTails.keySet();
			for(BitSet edgeColo : setEdgeColoTmp){
				List<IntSet> lstTails = lstSetOfTails.get(edgeColo);
				
				for (int i = 0; i < lstTails.size(); i++) {
					Set<Integer> setTmp = MapUtil.convert(lstTails.get(i));
					System.out.println("Tail ids: " + lstTails.get(i));
					testSetInt.retainAll(setTmp);
					System.out.println("After join: " + testSetInt);
				}
			}
			
			
		}
		//System.out.println("--------------------------------------" );
		//System.out.println("Total edges is " + totalEdges);
		
	}
}
