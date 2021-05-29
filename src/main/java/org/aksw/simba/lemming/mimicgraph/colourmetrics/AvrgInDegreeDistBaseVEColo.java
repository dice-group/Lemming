package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
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
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgInDegreeValues;
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapMaxInDegreeValues;
	
	
	public AvrgInDegreeDistBaseVEColo(ColouredGraph[] origGrphs){
		mMapAvrgInDegreeValues = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
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
		
		int numberOfGraphs = 0;
		if(origGrphs!= null && (numberOfGraphs = origGrphs.length) > 0 ){
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
							ObjectDoubleOpenHashMap<BitSet> mapIEColoToAvrgInDegree= mMapAvrgInDegreeValues.get(vertColo);
							
							if(mapIEColoToAvrgInDegree == null){
								mapIEColoToAvrgInDegree = new ObjectDoubleOpenHashMap<BitSet>();
								mMapAvrgInDegreeValues.put(vertColo, mapIEColoToAvrgInDegree);
							}
							mapIEColoToAvrgInDegree.putOrAdd(edgeColo, avrg, avrg);						
						}
						
						// max degree
						if(maxDegree!=0){
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
			Object[] keyVertexColours = mMapAvrgInDegreeValues.keys;
			for(int i = 0 ; i < keyVertexColours.length ; ++i) {
				if(mMapAvrgInDegreeValues.allocated[i]){
					BitSet vertColo = (BitSet) keyVertexColours[i];
					ObjectDoubleOpenHashMap<BitSet> edgeColourDist =  mMapAvrgInDegreeValues.get(vertColo);
					Object[] keyEdgeColours = edgeColourDist.keys;
					for(int j = 0; j< keyEdgeColours.length ; ++j){
						if(edgeColourDist.allocated[j]){
							BitSet edgeColo = (BitSet) keyEdgeColours[j];
							Double noOfInDegree = edgeColourDist.get(edgeColo);
							edgeColourDist.put(edgeColo, noOfInDegree / numberOfGraphs);
						}
					}
				}
			}
			
			// compute average max degree
			keyVertexColours = mMapMaxInDegreeValues.keys;
			for (int i = 0; i < keyVertexColours.length; ++i) {
				if (mMapMaxInDegreeValues.allocated[i]) {
					BitSet vertColo = (BitSet) keyVertexColours[i];
					ObjectDoubleOpenHashMap<BitSet> mapMaxDegreeValues = mMapMaxInDegreeValues.get(vertColo);
					Object[] keyEdgeColours = mapMaxDegreeValues.keys;
					for (int j = 0; j < keyEdgeColours.length; ++j) {
						if(mapMaxDegreeValues.allocated[j]){
							BitSet edgeColo = (BitSet) keyEdgeColours[j];
							Double maxInDegree = mapMaxDegreeValues.get(edgeColo);
							mapMaxDegreeValues.put(edgeColo, maxInDegree / numberOfGraphs);
						}
					}
				}
			}
		}
	}
}
