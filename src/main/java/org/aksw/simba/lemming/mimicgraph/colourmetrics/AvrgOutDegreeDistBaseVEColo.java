package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * Compute average out degree distribution of specific colored vertices
 * according to a particular edge's color i.e., for red vertices, the average of
 * out degree associated with green edges is about 48%, and 52% with black edges
 * 
 * @author nptsy
 */
public class AvrgOutDegreeDistBaseVEColo {
	
	/*
	 * the keys are the vertex's colors and the values are the distribution of
	 * in degree associated with edge's colors
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mMapAvrgOutDegreeAppearTime;
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgOutDegreeValues;
	
	private ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mMapAvrgMaxOutDegreeAppearTime;
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapMaxOutDegreeValues;
		
	public AvrgOutDegreeDistBaseVEColo(ColouredGraph[] origGrphs){
		mMapAvrgOutDegreeAppearTime = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		mMapAvrgOutDegreeValues = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		
		mMapAvrgMaxOutDegreeAppearTime = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		mMapMaxOutDegreeValues = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		apply(origGrphs);
	}
	
	public double getAvarageOutDegreeOf(BitSet tailColo, BitSet edgeColo){
		if(mMapAvrgOutDegreeValues.get(tailColo) != null){
			return mMapAvrgOutDegreeValues.get(tailColo).getOrDefault(edgeColo, 0);
		}
		return 0;
	}
	
	public double getAverageMaxOutDegreeOf(BitSet tailColo, BitSet edgeColo){
		if(mMapMaxOutDegreeValues.get(tailColo) != null){
			return mMapMaxOutDegreeValues.get(tailColo).getOrDefault(edgeColo, 0);
		}
		return 0;
	}
	
	private void apply(ColouredGraph[] origGrphs){
		
		for( ColouredGraph grph : origGrphs){
			
			OutDegreeDistBaseVEColo outDegreeAnalyzer = new OutDegreeDistBaseVEColo(grph);
					
			Map<BitSet, Map<BitSet,Object>> mapVColoToOEColo = outDegreeAnalyzer.getMapVertColoToOutEdgeColo();
			
			Set<BitSet> vertexColours = mapVColoToOEColo.keySet();
			
			for(BitSet vertColo : vertexColours){
				Set<BitSet> setLinkedEdgeColours = mapVColoToOEColo.get(vertColo).keySet();
				
				for(BitSet edgeColo : setLinkedEdgeColours){
					double avrg = outDegreeAnalyzer.getAverageOutDegreeOf(vertColo, edgeColo);
					double maxDegree = outDegreeAnalyzer.getMaxOutDegreeOf(vertColo, edgeColo);

					// average degree
					if(avrg != 0 ){
						// add to map
						ObjectIntOpenHashMap<BitSet> mapAppearTimes = mMapAvrgOutDegreeAppearTime.get(vertColo);
						if(mapAppearTimes == null){
							mapAppearTimes = new ObjectIntOpenHashMap<BitSet>();
							mMapAvrgOutDegreeAppearTime.put(vertColo, mapAppearTimes);
						}
						mapAppearTimes.putOrAdd(edgeColo,1,1);
						
						ObjectDoubleOpenHashMap<BitSet> mapAvrgDegree= mMapAvrgOutDegreeValues.get(vertColo);
						
						if(mapAvrgDegree == null){
							mapAvrgDegree = new ObjectDoubleOpenHashMap<BitSet>();
							mMapAvrgOutDegreeValues.put(vertColo, mapAvrgDegree);
						}
						mapAvrgDegree.putOrAdd(edgeColo, avrg, avrg);						
					}
					
					// max degree
					if(maxDegree!=0){
						
						// add to map
						ObjectIntOpenHashMap<BitSet> mapMaxAppearTimes = mMapAvrgMaxOutDegreeAppearTime.get(vertColo);
						if(mapMaxAppearTimes == null){
							mapMaxAppearTimes = new ObjectIntOpenHashMap<BitSet>();
							mMapAvrgMaxOutDegreeAppearTime.put(vertColo, mapMaxAppearTimes);
						}
						mapMaxAppearTimes.putOrAdd(edgeColo,1,1);
						
						ObjectDoubleOpenHashMap<BitSet> mapOEColoToMaxDegree= mMapMaxOutDegreeValues.get(vertColo);
						
						if(mapOEColoToMaxDegree == null){
							mapOEColoToMaxDegree = new ObjectDoubleOpenHashMap<BitSet>();
							mMapMaxOutDegreeValues.put(vertColo, mapOEColoToMaxDegree);
						}
						mapOEColoToMaxDegree.putOrAdd(edgeColo, maxDegree, maxDegree);
					}
				}
			}
		}
		
		
		// compute average out degree
		Object[] keyVertexColours = mMapAvrgOutDegreeAppearTime.keys;
		for(int i = 0 ; i < keyVertexColours.length ; ++i) {
			if(mMapAvrgOutDegreeAppearTime.allocated[i]){
				BitSet vertColo = (BitSet) keyVertexColours[i];
				ObjectIntOpenHashMap<BitSet> mapoEColoToAppearTimes = mMapAvrgOutDegreeAppearTime.get(vertColo);
				if(mapoEColoToAppearTimes != null){
					Object[] keyEdgeColours = mapoEColoToAppearTimes.keys;
					for(int j = 0; j< keyEdgeColours.length ; ++j){
						if(mapoEColoToAppearTimes.allocated[j]){
							BitSet edgeColo = (BitSet) keyEdgeColours[j];
							int noOfTimes = mapoEColoToAppearTimes.get(edgeColo);
							ObjectDoubleOpenHashMap<BitSet> mapAvrgDegreeValues = mMapAvrgOutDegreeValues.get(vertColo);
							Double noOfOutDegree = mapAvrgDegreeValues.get(edgeColo);
							mapAvrgDegreeValues.put(edgeColo, noOfOutDegree / noOfTimes);
						}
					}
				}
			}
		}
		
		
		// compute average max degree
		keyVertexColours = mMapAvrgMaxOutDegreeAppearTime.keys;
		for (int i = 0; i < keyVertexColours.length; ++i) {
			if (mMapAvrgMaxOutDegreeAppearTime.allocated[i]) {
				BitSet vertColo = (BitSet) keyVertexColours[i];
				ObjectIntOpenHashMap<BitSet> mapMaxDegreeTimes = mMapAvrgMaxOutDegreeAppearTime.get(vertColo);
				if (mapMaxDegreeTimes != null) {
					Object[] keyEdgeColours = mapMaxDegreeTimes.keys;
					for (int j = 0; j < keyEdgeColours.length; ++j) {
						if(mapMaxDegreeTimes.allocated[j]){
							BitSet edgeColo = (BitSet) keyEdgeColours[j];
							int noOfTimes =  mapMaxDegreeTimes.get(edgeColo);
							ObjectDoubleOpenHashMap<BitSet> mapMaxDegreeValues = mMapMaxOutDegreeValues.get(vertColo);
							Double maxOutDegree = mapMaxDegreeValues.get(edgeColo);
							mapMaxDegreeValues.put(edgeColo, maxOutDegree / noOfTimes);
						}
					}
				}
			}
		}
	}
}	
