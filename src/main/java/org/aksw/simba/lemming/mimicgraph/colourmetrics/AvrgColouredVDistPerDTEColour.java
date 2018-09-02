package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.VertexColourDistributionMetric;
import org.aksw.simba.lemming.util.MapUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class AvrgColouredVDistPerDTEColour {

	
	/**
	 * key1: data typed edge's colors, values: the distribution of vertices in particular vertex's colour
	 */
	private Map<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgColouredVDist;
	
	public AvrgColouredVDistPerDTEColour(ColouredGraph [] origGrphs){
		
		mMapAvrgColouredVDist= new HashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		apply(origGrphs);
	}
	
	private void apply(ColouredGraph[] origGrphs){
		
		ColouredVDistPerDTEColour colouredVDistPerDTEColoMetric = new ColouredVDistPerDTEColour();
		VertexColourDistributionMetric vColoDistributionMetric = new VertexColourDistributionMetric();
		
		int numberOfGrpahs = origGrphs.length;
		if(numberOfGrpahs>0){
			for(ColouredGraph grph : origGrphs){
				//get vertices distributed to different colours
				ObjectDistribution<BitSet> vColoDistribution = vColoDistributionMetric.apply(grph);
				ObjectDoubleOpenHashMap<BitSet> mapVColoDistribution = MapUtil.convert(vColoDistribution);
				
				/**
				 * key is the data typed edge's colour and the value is the distribution of vertices
				 */
				Map<BitSet, ObjectDistribution<BitSet>> mapColouredVDistPerDTEColo = colouredVDistPerDTEColoMetric.apply(grph);
				Set<BitSet> setOfDTEColours = mapColouredVDistPerDTEColo.keySet();
					
				for(BitSet dteColo: setOfDTEColours){
					ObjectDistribution<BitSet> colouredVDistribution = mapColouredVDistPerDTEColo.get(dteColo);
					BitSet[] sampleVColours = colouredVDistribution.sampleSpace;
					double[] vDistribution = colouredVDistribution.values;
					
					// map of average distribution of coloured vertices (key is the data typed edge's color)
					ObjectDoubleOpenHashMap<BitSet> mapAvrgColouredVDist = mMapAvrgColouredVDist.get(dteColo);
					if(mapAvrgColouredVDist == null){
						mapAvrgColouredVDist = new ObjectDoubleOpenHashMap<BitSet>();
						mMapAvrgColouredVDist.put(dteColo, mapAvrgColouredVDist);
					}
					
					
					for(int i = 0 ; i < sampleVColours.length ; i++){
						BitSet vColo = sampleVColours[i];
						double noOfVertices = mapVColoDistribution.get(vColo);
						
						double rate = vDistribution[i] / noOfVertices;
						mapAvrgColouredVDist.putOrAdd(vColo, rate, rate);
					}
				}
			}
			
			Set<BitSet> setOfDTEColours = mMapAvrgColouredVDist.keySet();
			 
			
			for(BitSet dteColo : setOfDTEColours){
				
				ObjectDoubleOpenHashMap<BitSet> mapAvrgColouredVDist = mMapAvrgColouredVDist.get(dteColo);
				Object[] arrVColours = mapAvrgColouredVDist.keys;
				for(int j = 0 ; j< arrVColours.length ;j++){
					if(mapAvrgColouredVDist.allocated[j]){
						BitSet vColo = (BitSet) arrVColours[j];
						double sumRate = mapAvrgColouredVDist.get(vColo);
						mapAvrgColouredVDist.put(vColo, sumRate/numberOfGrpahs);
					}
				}
			}
		}
	}
	
	/**
	 * get the map of average distribution of vertices (in a specific colours)
	 * which associate with data typed edge's colours
	 * 
	 * @param setOfFilteredDTEColours
	 *            set of filtered data typed edge's colours
	 * @param setOfFilteredVColours
	 *            set of filtered vertex's colours
	 * 
	 * @return a map in which the key is the data typed edge's colour and the
	 *         value is the average distribution of vertices associating with
	 *         the edge
	 */
	public Map<BitSet, ObjectDoubleOpenHashMap<BitSet>> getMapAvrgColouredVDist(Set<BitSet> setOfFilteredDTEColours, Set<BitSet> setOfFilteredVColours){
		
		if(setOfFilteredDTEColours !=null && setOfFilteredVColours != null){
			// keys are the data typed edge's colours and the values are the distribution of vertices per vertex's colour
			Map<BitSet, ObjectDoubleOpenHashMap<BitSet>> mapVDistPerDTEColo = new HashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
			
			Set<BitSet> setOfDTEColours = mMapAvrgColouredVDist.keySet();
			
			for(BitSet dteColo : setOfDTEColours){
				if(setOfFilteredDTEColours.contains(dteColo)){
					ObjectDoubleOpenHashMap<BitSet> vDistPerVColo = mMapAvrgColouredVDist.get(dteColo); 
					Object[] sampleVColours = vDistPerVColo.keys;
					double[] vDistribution = vDistPerVColo.values;

					ObjectDoubleOpenHashMap<BitSet> newVDist = new ObjectDoubleOpenHashMap<BitSet>();
					for(int j = 0 ; j < sampleVColours.length ; j++){
						if(vDistPerVColo.allocated[j]){
							BitSet vertColo = (BitSet) sampleVColours[j];
							if(setOfFilteredVColours.contains(vertColo)){
								newVDist.putOrAdd(vertColo, vDistribution[j], vDistribution[j]);
							}
						}
					}
					mapVDistPerDTEColo.put(dteColo, newVDist);
				}
			}
			return mapVDistPerDTEColo;
		}else{
			return getMapAvrgColouredVDist();
		}
	}
	
	/**
	 * get the map of average distribution of vertices (in specific colours)
	 * which associate with data typed edge's colours
	 * 
	 * @return a map in which the key is the data typed edge's colour and the
	 *         value is the average distribution of vertices associating with
	 *         the edge
	 */
	public Map<BitSet, ObjectDoubleOpenHashMap<BitSet>> getMapAvrgColouredVDist(){
		return mMapAvrgColouredVDist;
	}
}
