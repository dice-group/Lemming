package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.EdgeColourDistributionMetric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.util.MapUtil;
import org.apache.jena.ext.com.google.common.primitives.Doubles;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;


/**
 * Compute, for each out-edge's color, the average distribution of out-edges over vertex's
 * colors. i.e., for green out-edges, the red vertices use average 16.5% of green out-edges,
 * the blue vertices use about 28.5% of the green out-edges and the yellow vertices use
 * the rest of the green out-edges.
 * 
 * @author nptsy
 */
public class AvrgColouredOEDistPerVColour {
	
	/**
	 * key1: edge's colors, values: the distribution of edges over different vertex's colors
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgOEDist;
	
	
	public static String getName(){
		return "AvrgColouredOEDistPerVColour";
	}
	
	public AvrgColouredOEDistPerVColour(ColouredGraph[] origGrphs){
		mMapAvrgOEDist = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		apply(origGrphs);
	}
	
	public Map<BitSet, ObjectDistribution<BitSet>> getMapAvrgOutEdgeDist(){
		Map<BitSet, ObjectDistribution<BitSet>> mapOEDistPerVColo = new HashMap<BitSet, ObjectDistribution<BitSet>>();
		Object[] keyOEColours = mMapAvrgOEDist.keys;
		int iNoOfOEColours = keyOEColours.length;
		for(int i = 0 ; i < iNoOfOEColours; i++){
			if(mMapAvrgOEDist.allocated[i]){
				BitSet oeColo = (BitSet) keyOEColours[i];
				ObjectDistribution<BitSet> dist = MapUtil.convert(mMapAvrgOEDist.get(oeColo)); 
				mapOEDistPerVColo.put(oeColo, dist);
			}
		}
		return mapOEDistPerVColo;
	}
	
	/**
	 * get the map of average distribution of out edges (in specific edge's colours) over different vertex's colours
	 * @param setOfFilteredEColours set of filtered edge's colours
	 * @param setOfFilteredVColours set of filtered vertex's colours
	 * @return a map
	 */
	public Map<BitSet, ObjectDistribution<BitSet>> getMapAvrgOutEdgeDist(Set<BitSet> setOfFilteredEColours, Set<BitSet> setOfFilteredVColours){
		
		if(setOfFilteredEColours !=null && setOfFilteredVColours != null){
			// keys are the edge's colours and the values are the distribution of edges per vertex's colour
			Map<BitSet, ObjectDistribution<BitSet>> mapOEDistPerVColo = new HashMap<BitSet, ObjectDistribution<BitSet>>();
			
			Object[] keyOEColours = mMapAvrgOEDist.keys;
			int iNoOfOEColours = keyOEColours.length;
			for(int i = 0 ; i < iNoOfOEColours; i++){
				if(mMapAvrgOEDist.allocated[i]){
					BitSet oeColo = (BitSet) keyOEColours[i];
					if(setOfFilteredEColours.contains(oeColo)){
						ObjectDoubleOpenHashMap<BitSet> oeDistPerVColo = mMapAvrgOEDist.get(oeColo); 
						Object[] arrOfVColours = oeDistPerVColo.keys;
						double[] oeDistribution = oeDistPerVColo.values;

						List<BitSet> newLstVertColours = new ArrayList<BitSet>();
						List<Double> newLstEdgeDist = new ArrayList<Double>();
						for(int j = 0 ; j < arrOfVColours.length ; j++){
							if(oeDistPerVColo.allocated[j]){
								BitSet vertColo = (BitSet) arrOfVColours[j];
								if(setOfFilteredVColours.contains(vertColo)){
									newLstVertColours.add(vertColo);
									newLstEdgeDist.add(oeDistribution[j]);
								}
							}
						}
						
						BitSet[] newArrVertColours = newLstVertColours.toArray(new BitSet[0]);
						double[] newArrEdgeDist = Doubles.toArray(newLstEdgeDist);
						
						ObjectDistribution<BitSet> newEdgeDist = new ObjectDistribution<>(newArrVertColours, newArrEdgeDist);
						mapOEDistPerVColo.put(oeColo, newEdgeDist);
					}
				}
			}
			return mapOEDistPerVColo;
		}else{
			return getMapAvrgOutEdgeDist();
		}
	}
	
	private void apply(ColouredGraph[] origGrphs){
		int numberOfGraphs = 0;
		if(origGrphs != null && (numberOfGraphs = origGrphs.length) > 0){
			ColouredOEDistPerVColour oeDistributionMetric = new ColouredOEDistPerVColour();
			EdgeColourDistributionMetric eColoDistributionMetric  = new EdgeColourDistributionMetric();
			for( ColouredGraph grph : origGrphs){
				
				ObjectDistribution<BitSet>  eColoDistribution = eColoDistributionMetric.apply(grph);
				ObjectDoubleOpenHashMap<BitSet> mapEColoDistribution = MapUtil.convert(eColoDistribution);
				/**
				 * key1: edge's colors, values: the distribution of edges over vertex's colors
				 */
				Map<BitSet, ObjectDistribution<BitSet>> mapOEDistPerVColo	= oeDistributionMetric.apply(grph);
				
				Set<BitSet> setOfOEColours = mapOEDistPerVColo.keySet();
				
				for(BitSet eColo : setOfOEColours){
					
					// map of average distribution of edges (key is vertex's color)
					ObjectDoubleOpenHashMap<BitSet> mapAvrgOEDistPerVColo = mMapAvrgOEDist.get(eColo);
					if(mapAvrgOEDistPerVColo == null){
						mapAvrgOEDistPerVColo = new ObjectDoubleOpenHashMap<BitSet>();
						mMapAvrgOEDist.put(eColo, mapAvrgOEDistPerVColo);
					}
					
					double noOfEdges = mapEColoDistribution.get(eColo);
					
					ObjectDistribution<BitSet> edgeDistPerVColo = mapOEDistPerVColo.get(eColo);
					BitSet[] sampleVColours = edgeDistPerVColo.sampleSpace;
					double[] oeDistribution = edgeDistPerVColo.values;
					
					int iNoOfVColours = sampleVColours.length;
					for(int i = 0 ; i < iNoOfVColours ; i++){
						BitSet vColo = sampleVColours[i];
						double rate = oeDistribution[i] /noOfEdges;
						
						mapAvrgOEDistPerVColo.putOrAdd(vColo, rate, rate);
					}
				}
			}
			
			//compute average distribution of edges over all different versions
			Object[] keyOEColours =  mMapAvrgOEDist.keys;
			for(int i = 0 ; i < keyOEColours.length ; i++){
				if(mMapAvrgOEDist.allocated[i]){
					BitSet eColo = (BitSet) keyOEColours[i];
					
					ObjectDoubleOpenHashMap<BitSet> mapAvrgDist = mMapAvrgOEDist.get(eColo);
					Object[] keyVColours = mapAvrgDist.keys;
					for(int j = 0 ; j< keyVColours.length ;j++){
						if(mapAvrgDist.allocated[j]){
							BitSet vColo = (BitSet) keyVColours[j];
							
							double sumRate = mapAvrgDist.get(vColo);
							mapAvrgDist.put(vColo, sumRate/numberOfGraphs);
						}
					}
				}
			}
		}
		//System.out.println("complete the computation");
	}
}
