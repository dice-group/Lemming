package org.aksw.simba.lemming.metrics.dist.multi;

import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;


/**
 * Compute average in degree distribution of specific colored vertices
 * according to a particular edge's color i.e., for red vertices, the average of
 * in degree associated with white edges is about 15%, and 85% with black edges
 * 
 * @author nptsy
 */
public class AvrgInDegreeDistBaseVEColo {
	
	/*
	 * the keys are the vertex's colors and the values are the distribution of
	 * in degree associated with edge's colors
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mMapAvrgInDegreeAppearTime;
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgInDegreeValues;
	
	
	private ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mMapAvrgMaxInDegreeAppearTime;
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapMaxInDegreeValues;
	
	
	public AvrgInDegreeDistBaseVEColo(ColouredGraph[] origGrphs){
		mMapAvrgInDegreeAppearTime = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		mMapAvrgInDegreeValues = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		
		mMapAvrgMaxInDegreeAppearTime = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		mMapMaxInDegreeValues = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		apply(origGrphs);
	}
	
	public double getAvarageInDegreeOf(BitSet edgeColo, BitSet headColo){
		if(mMapAvrgInDegreeValues.get(headColo) != null){
			return mMapAvrgInDegreeValues.get(headColo).getOrDefault(edgeColo, 0);
		}
		return 0;
	}
	
	public double getAverageMaxInDegreeOf(BitSet edgeColo, BitSet headColo){
		if(mMapMaxInDegreeValues.get(headColo) != null){
			return mMapMaxInDegreeValues.get(headColo).getOrDefault(edgeColo, 0);
		}
		return 0;
	}
	
	private void apply(ColouredGraph[] origGrphs){
		
		for( ColouredGraph grph : origGrphs){
			
			InDegreeDistBaseVEColo inDegreeAnalyzer = new InDegreeDistBaseVEColo(grph);
			// key is vertex's colour, value is the set of edge's colours
			Map<BitSet, Map<BitSet, Object>> mapVColoToIEColo = inDegreeAnalyzer.getMapVertColoToInEdgeColo();
			// set vertex's colours
			Set<BitSet> vertexColours = mapVColoToIEColo.keySet();
			
			for(BitSet vertColo : vertexColours){
				Set<BitSet> setLinkedEdgeColours = mapVColoToIEColo.get(vertColo).keySet();
				
				for(BitSet edgeColo : setLinkedEdgeColours){
					double avrg = inDegreeAnalyzer.getAverageInDegreeOf(vertColo, edgeColo);
					double maxDegree = inDegreeAnalyzer.getMaxInDegreeOf(vertColo, edgeColo);
					
					// average degree
					if(avrg != 0 ){
						// add to map
						ObjectIntOpenHashMap<BitSet> mapAppearTimes = mMapAvrgInDegreeAppearTime.get(vertColo);
						if(mapAppearTimes == null){
							mapAppearTimes = new ObjectIntOpenHashMap<BitSet>();
							mMapAvrgInDegreeAppearTime.put(vertColo, mapAppearTimes);
						}
						mapAppearTimes.putOrAdd(edgeColo,1,1);
						
						ObjectDoubleOpenHashMap<BitSet> mapIEColoToAvrgInDegree= mMapAvrgInDegreeValues.get(vertColo);
						
						if(mapIEColoToAvrgInDegree == null){
							mapIEColoToAvrgInDegree = new ObjectDoubleOpenHashMap<BitSet>();
							mMapAvrgInDegreeValues.put(vertColo, mapIEColoToAvrgInDegree);
						}
						mapIEColoToAvrgInDegree.putOrAdd(edgeColo, avrg, avrg);						
					}
					
					// max degree
					if(maxDegree!=0){
						
						// add to map
						ObjectIntOpenHashMap<BitSet> mapMaxDegreeAppearTimes = mMapAvrgMaxInDegreeAppearTime.get(vertColo);
						if(mapMaxDegreeAppearTimes == null){
							mapMaxDegreeAppearTimes = new ObjectIntOpenHashMap<BitSet>();
							mMapAvrgMaxInDegreeAppearTime.put(vertColo, mapMaxDegreeAppearTimes);
						}
						mapMaxDegreeAppearTimes.putOrAdd(edgeColo,1,1);
						
						ObjectDoubleOpenHashMap<BitSet> mapIEColoToMaxDegree= mMapMaxInDegreeValues.get(vertColo);
						
						if(mapIEColoToMaxDegree == null){
							mapIEColoToMaxDegree = new ObjectDoubleOpenHashMap<BitSet>();
							mMapMaxInDegreeValues.put(vertColo, mapIEColoToMaxDegree);
						}
						mapIEColoToMaxDegree.putOrAdd(edgeColo, maxDegree, maxDegree);
					}
				}
			}
		}
		
		
		// compute average in degree
		Object[] keyVertexColours = mMapAvrgInDegreeAppearTime.keys;
		for(int i = 0 ; i < keyVertexColours.length ; ++i) {
			if(mMapAvrgInDegreeAppearTime.allocated[i]){
				BitSet vertColo = (BitSet) keyVertexColours[i];
				ObjectIntOpenHashMap<BitSet> mapIEColoToAppearTimes = mMapAvrgInDegreeAppearTime.get(vertColo);
				if(mapIEColoToAppearTimes != null){
					Object[] keyEdgeColours = mapIEColoToAppearTimes.keys;
					for(int j = 0; j< keyEdgeColours.length ; ++j){
						if(mapIEColoToAppearTimes.allocated[j]){
							BitSet edgeColo = (BitSet) keyEdgeColours[j];
							int noOfTimes = mapIEColoToAppearTimes.get(edgeColo);
							ObjectDoubleOpenHashMap<BitSet> mapAvrgDegreeValues = mMapAvrgInDegreeValues.get(vertColo);
							Double noOfInDegree = mapAvrgDegreeValues.get(edgeColo);
							mapAvrgDegreeValues.put(edgeColo, noOfInDegree / noOfTimes);
						}
					}
				}
			}
		}
		
		// compute average max degree
		keyVertexColours = mMapAvrgMaxInDegreeAppearTime.keys;
		for (int i = 0; i < keyVertexColours.length; ++i) {
			if (mMapAvrgMaxInDegreeAppearTime.allocated[i]) {
				BitSet vertColo = (BitSet) keyVertexColours[i];
				ObjectIntOpenHashMap<BitSet> mapIEColoToAppearTimes = mMapAvrgMaxInDegreeAppearTime.get(vertColo);
				if (mapIEColoToAppearTimes != null) {
					Object[] keyEdgeColours = mapIEColoToAppearTimes.keys;
					for (int j = 0; j < keyEdgeColours.length; ++j) {
						if(mapIEColoToAppearTimes.allocated[j]){
							BitSet edgeColo = (BitSet) keyEdgeColours[j];
							int noOfTimes =  mapIEColoToAppearTimes.get(edgeColo);
							ObjectDoubleOpenHashMap<BitSet> mapMaxDegreeValues = mMapMaxInDegreeValues.get(vertColo);
							Double maxInDegree = mapMaxDegreeValues.get(edgeColo);
							mapMaxDegreeValues.put(edgeColo, maxInDegree / noOfTimes);
						}
					}
				}
			}
		}
	}
}
