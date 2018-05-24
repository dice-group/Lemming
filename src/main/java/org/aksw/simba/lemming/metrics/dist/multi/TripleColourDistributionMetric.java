package org.aksw.simba.lemming.metrics.dist.multi;

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
	private TestMaxNoOfGrpOfAVertex testMaxNoOfGrps;
	/**
	 * Constructor
	 */
	public TripleColourDistributionMetric(){
		mTripleMapHeadEdgeTails = new HashMap<BitSet, Map<BitSet, Map<BitSet, IntSet>>>();
		mTripleMapTailHeadEdges = new HashMap<BitSet, Map<BitSet, Map<BitSet, IntSet>>>();
		mTripleMapTailEdgeHeads = new HashMap<BitSet, Map<BitSet, Map<BitSet, IntSet>>>();
		mMapVertColoDist = new ObjectDoubleOpenHashMap<BitSet>();
		mMapEdgeColoDist = new ObjectDoubleOpenHashMap<BitSet>();
		
		testMaxNoOfGrps = new TestMaxNoOfGrpOfAVertex();
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
		
		testMaxNoOfGrps.apply(grph);
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
		
		testMaxNoOfGrps.apply(grph);
	}
	
	
	/**
	 * for testing purpose 
	 * print out important information regarding data storing in variables
	 */
	public void printInfo(){
		
		Set<BitSet> setEdgeColo = MapUtil.keysToSet(mMapEdgeColoDist);
		Set<BitSet> setVertColo = MapUtil.keysToSet(mMapVertColoDist);
		
		Map<BitSet, List<IntSet>> mapColourToTails = new HashMap<BitSet, List<IntSet>>();
		Map<BitSet, List<IntSet>> mapColourToHeads = new HashMap<BitSet, List<IntSet>>();
		
		
		for (BitSet headColo : setVertColo) {
			for (BitSet tailColo : setVertColo) {
				if (mTripleMapTailEdgeHeads.containsKey(tailColo)) {
					Map<BitSet, Map<BitSet, IntSet>> mapEdgeToHeads = mTripleMapTailEdgeHeads.get(tailColo);
					for (BitSet edgeColo : setEdgeColo) {
						if (mapEdgeToHeads.containsKey(edgeColo)) {
							Map<BitSet, IntSet> mapHeads = mapEdgeToHeads.get(edgeColo);
							if (mapHeads.containsKey(headColo)) {
								// the vetices as tails
								IntSet setHeads = mTripleMapTailEdgeHeads.get(tailColo).get(edgeColo).get(headColo);
								List<IntSet> lstHeads = mapColourToHeads.get(headColo);
								if(lstHeads == null ){
									lstHeads = new ArrayList<IntSet>();
									mapColourToHeads.put(headColo, lstHeads);
								}
								lstHeads.add(setHeads);
							}
						}
					}
				}
			}
		}
		
		
		for (BitSet tailColo : setVertColo) {
			for (BitSet headColo : setVertColo) {
				if(mTripleMapHeadEdgeTails.containsKey(headColo)){
					Map<BitSet, Map<BitSet, IntSet>> mapEdgeToTails = mTripleMapHeadEdgeTails.get(headColo);
					for (BitSet edgeColo : setEdgeColo) {
						if (mapEdgeToTails.containsKey(edgeColo)) {
							Map<BitSet, IntSet> mapTails = mapEdgeToTails.get(edgeColo);
							if (mapTails.containsKey(tailColo)) {
								// the vetices as tails
								IntSet setTails = mTripleMapHeadEdgeTails.get(headColo).get(edgeColo).get(tailColo);
								List<IntSet> lstTails = mapColourToTails.get(tailColo);
								if(lstTails == null ){
									lstTails = new ArrayList<IntSet>();
									mapColourToTails.put(tailColo, lstTails);
								}
								lstTails.add(setTails);
							}
						}
					}
				}
			}
		}
			
		Set<BitSet> setTailColo = 	mapColourToTails.keySet();
		Set<BitSet> setHeadColo = mapColourToHeads.keySet();
		System.out.println("No of tail colours: " + setTailColo.size() + " No of head colours: " + setHeadColo.size());
		Set<BitSet> setJoinedColo = mapColourToHeads.keySet();
		setJoinedColo.retainAll(setTailColo);
		System.out.println("There are " + setJoinedColo.size() +" colours having both tails and heads");	

		for(BitSet joinedColo: setJoinedColo){
			List<IntSet> lstAllHeads = mapColourToHeads.get(joinedColo);
			List<IntSet> lstAllTails = mapColourToTails.get(joinedColo);
			
			System.out.println("");
			System.out.println("Vertex colour: " + joinedColo + " has " + mMapVertColoDist.get(joinedColo) + " vertices");
			System.out.println("Max number of group: " + testMaxNoOfGrps.getMapTailsToNoOfGrp().get(joinedColo));
			System.out.println("Vertices (as tails) are clustered in : " + lstAllTails.size() + " groups");
			System.out.println("Vertices (as heads) are clustered in : " + lstAllHeads.size() + " groups");
		}
		
		for(BitSet tailColo: setTailColo){
			if(!setJoinedColo.contains(tailColo)){
				List<IntSet> lstAllTails = mapColourToTails.get(tailColo);
				System.out.println("");
				System.out.println("Vertex colour: " + tailColo + " has " + mMapVertColoDist.get(tailColo) + " vertices");
				System.out.println("Max number of group: " + testMaxNoOfGrps.getMapTailsToNoOfGrp().get(tailColo));
				System.out.println("Vertices (as tails) are clustered in : " + lstAllTails.size() + " groups");
				System.out.println("Vertices (as heads) are in NO groups");
			}
		}
		
		for(BitSet headColo: setHeadColo){
			if(!setJoinedColo.contains(headColo)){
				List<IntSet> lstAllHeads = mapColourToHeads.get(headColo);
				System.out.println("");
				System.out.println("Vertex colour: " + headColo + " has " + mMapVertColoDist.get(headColo) + " vertices");
				System.out.println("Max number of group: " + testMaxNoOfGrps.getMapTailsToNoOfGrp().get(headColo));
				System.out.println("Vertices (as tails) are in NO groups");
				System.out.println("Vertices (as heads) are clustered in : " + lstAllHeads.size() + " groups");
			}
		}
		
//			System.out.println("Vertices: " + setOfAllVertices);
//			int iNoOfGrp =lstAllTails.size(); 
//			if(iNoOfGrp > 1){
//				for (int i = 0 ; i < iNoOfGrp -1 ; i++ ){
//					Set<Integer> setI = MapUtil.convert(lstAllTails.get(i));
//					for(int j = i+1 ; j < iNoOfGrp ; j++){
//						Set<Integer> setJ = MapUtil.convert(lstAllTails.get(j));
//						
//						System.out.println("- No of items in setI: " + setI.size());
//						System.out.println("- No of items in setJ: " + setJ.size());
//						setJ.retainAll(setI);
//						System.out.println("      => No of joined items: " + setJ.size());
//					}
//				}
//			}
			
//			Set<BitSet> setEdgeColoTmp = mapEdgeToTails.keySet();
//			for(BitSet edgeColo : setEdgeColoTmp){
//				List<IntSet> lstTails = mapEdgeToTails.get(edgeColo);
//				
//				for (int i = 0; i < lstTails.size()- 1; i++) {
//					Set<Integer> setI = MapUtil.convert(lstTails.get(i));
//					for(int j = i+1 ; j<  lstTails.size() ; j++){
//						Set<Integer> setJ = MapUtil.convert(lstTails.get(j));
//						System.out.println("- No of items in seti: " + setI.size());
//						System.out.println("- No of items in setj: " + setJ.size());
//						setJ.retainAll(setI);
//						System.out.println("\tNo of joined items: " + setJ.size());
//					}
//				}
//			}
		//System.out.println("--------------------------------------" );
		//System.out.println("Total edges is " + totalEdges);
		
	}
}
