package org.aksw.simba.lemming.metrics.dist.multi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.VertexColourDistributionMetric;
import org.aksw.simba.lemming.util.MapUtil;
import org.apache.jena.ext.com.google.common.primitives.Doubles;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class AvrgColouredVDistPerDTEColour {

	/**
	 * key1: data typed edge's colors, values the number of appearing time of a vertex's color associated
	 * with the edge's color over all versions
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mMapDTEAppearTimes;
	
	/**
	 * key1: data typed edge's colors, values: the distribution of vertices in particular vertex's colour
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgColouredVDist;
	
	public AvrgColouredVDistPerDTEColour(ColouredGraph [] origGrphs){
		
		mMapDTEAppearTimes = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		mMapAvrgColouredVDist= new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		apply(origGrphs);
	}
	
	private void apply(ColouredGraph[] origGrphs){
		
		ColouredVDistPerDTEdgeColour colouredVDistPerDTEColoMetric = new ColouredVDistPerDTEdgeColour();
		VertexColourDistributionMetric vColoDistributionMetric = new VertexColourDistributionMetric();
		
		for(ColouredGraph grph : origGrphs){
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
				
				// map of appearing time (key is the data typed edge's color)
				ObjectIntOpenHashMap<BitSet> mapAppearingTimes = mMapDTEAppearTimes.get(dteColo);
				if(mapAppearingTimes == null ){
					mapAppearingTimes = new ObjectIntOpenHashMap<BitSet>();
					mMapDTEAppearTimes.put(dteColo, mapAppearingTimes);
				}
				
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
					mapAppearingTimes.putOrAdd(vColo, 1, 1);
				}
			}
		}
		
		Object[] keyDTEColours = mMapDTEAppearTimes.keys;
		 
		
		for(int i = 0 ; i < keyDTEColours.length; i++){
			if(mMapDTEAppearTimes.allocated[i]){
				BitSet dteColo = (BitSet)keyDTEColours[i];
				ObjectIntOpenHashMap<BitSet> mapAppearTimes = mMapDTEAppearTimes.get(dteColo);
				
				Object[] keyVColours = mapAppearTimes.keys;
				ObjectDoubleOpenHashMap<BitSet> mapAvrgColouredVDist = mMapAvrgColouredVDist.get(dteColo);
				
				for(int j = 0 ; j< keyVColours.length ;j++){
					BitSet vColo = (BitSet) keyVColours[j];
					int noOfAppearTimes = mapAppearTimes.get(vColo);
					double sumRate = mapAvrgColouredVDist.get(vColo);
					
					if(noOfAppearTimes != 0){
						mapAvrgColouredVDist.put(vColo, sumRate/noOfAppearTimes);
					}else{
						mapAvrgColouredVDist.put(vColo, 0);
					}
				}
			}
		}
	}
	
	/**
	 * get the map of average distribution of vertices (in a specific colours) which associate with data typed edge's colours
	 * @param setOfFilteredDTEColours set of filtered data typed edge's colours
	 * @param setOfFilteredVColours set of filtered vertex's colours
	 * 
	 * @return a map in which the key is the data typed edge's colour and the
	 *         value is the average distribution of vertices associating with
	 *         the edge
	 */
	public Map<BitSet, ObjectDistribution<BitSet>> getMapAvrgColouredVDist(Set<BitSet> setOfFilteredDTEColours, Set<BitSet> setOfFilteredVColours){
		
		if(setOfFilteredDTEColours !=null && setOfFilteredVColours != null){
			// keys are the data typed edge's colours and the values are the distribution of vertices per vertex's colour
			Map<BitSet, ObjectDistribution<BitSet>> mapVDistPerDTEColo = new HashMap<BitSet, ObjectDistribution<BitSet>>();
			
			Object[] keyDTEColours = mMapAvrgColouredVDist.keys;
			
			for(int i = 0 ; i < keyDTEColours.length; i++){
				if(mMapAvrgColouredVDist.allocated[i]){
					BitSet dteColo = (BitSet) keyDTEColours[i];
					if(setOfFilteredDTEColours.contains(dteColo)){
						ObjectDoubleOpenHashMap<BitSet> vDistPerVColo = mMapAvrgColouredVDist.get(dteColo); 
						Object[] sampleVColours = vDistPerVColo.keys;
						double[] vDistribution = vDistPerVColo.values;

						List<BitSet> newLstVColours = new ArrayList<BitSet>();
						List<Double> newLstVDist = new ArrayList<Double>();
						for(int j = 0 ; j < sampleVColours.length ; j++){
							if(vDistPerVColo.allocated[j]){
								BitSet vertColo = (BitSet) sampleVColours[j];
								if(setOfFilteredVColours.contains(vertColo)){
									newLstVColours.add(vertColo);
									newLstVDist.add(vDistribution[j]);
								}
							}
						}
						
						BitSet[] newArrVColours = newLstVColours.toArray(new BitSet[0]);
						double[] newArrVDist = Doubles.toArray(newLstVDist);
						
						ObjectDistribution<BitSet> newVDist = new ObjectDistribution<>(newArrVColours, newArrVDist);
						mapVDistPerDTEColo.put(dteColo, newVDist);
					}
				}
			}
			return mapVDistPerDTEColo;
		}else{
			return getMapAvrgColouredVDist();
		}
	}
	
	/**
	 * get the map of average distribution of vertices (in a specific colours)
	 * which associate with data typed edge's colours
	 * 
	 * @return a map in which the key is the data typed edge's colour and the
	 *         value is the average distribution of vertices associating with
	 *         the edge
	 */
	public Map<BitSet, ObjectDistribution<BitSet>> getMapAvrgColouredVDist(){
		Map<BitSet, ObjectDistribution<BitSet>> mapVDistPerVColo = new HashMap<BitSet, ObjectDistribution<BitSet>>();
		Object[] keyDTEColours = mMapAvrgColouredVDist.keys;
		
		for(int i = 0 ; i < keyDTEColours.length; i++){
			if(mMapAvrgColouredVDist.allocated[i]){
				BitSet dteColo = (BitSet) keyDTEColours[i];
				ObjectDistribution<BitSet> dist = MapUtil.convert(mMapAvrgColouredVDist.get(dteColo)); 
				mapVDistPerVColo.put(dteColo, dist);
			}
		}
		return mapVDistPerVColo;
	}
}
