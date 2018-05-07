package org.aksw.simba.lemming.metrics.dist.multi;

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
 * colors. i.e., for out green out-edges, the red vertices use average 16.5% of green out-edges,
 * the blue vertices use about 28.5% of the green out-edges and the yellow vertices use
 * the rest of the green out-edges.
 * 
 * @author nptsy
 */
public class AvrgOutEdgeDistBaseEColoPerVColo {
	
	/**
	 * key1: edge's colors, values the appearing time of edge's color associated
	 * with the vertex's color over all versions
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mMapAvrgOutEdgeAppearTimes;
	
	/**
	 * key1: edge's colors, values: the distribution of edges over different vertex's colors
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgOutEdgeDist;
	
	
	public static String getName(){
		return "AvrgOutEdgeDistBaseEColoPerVColo";
	}
	
	public AvrgOutEdgeDistBaseEColoPerVColo(ColouredGraph[] origGrphs){
		mMapAvrgOutEdgeAppearTimes = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		mMapAvrgOutEdgeDist = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		apply(origGrphs);
	}
	
	public Map<BitSet, ObjectDistribution<BitSet>> getMapAvrgOutEdgeDist(){
		Map<BitSet, ObjectDistribution<BitSet>> res = new HashMap<BitSet, ObjectDistribution<BitSet>>();
		Object[] keyEdgeColors = mMapAvrgOutEdgeDist.keys;
		int iNoOfEdgeColors = keyEdgeColors.length;
		for(int i = 0 ; i < iNoOfEdgeColors; i++){
			if(mMapAvrgOutEdgeDist.allocated[i]){
				BitSet edgeColo = (BitSet) keyEdgeColors[i];
				ObjectDistribution<BitSet> dist = MapUtil.convert(mMapAvrgOutEdgeDist.get(edgeColo)); 
				res.put(edgeColo, dist);
			}
		}
		return res;
	}
	
	public Map<BitSet, ObjectDistribution<BitSet>> getMapAvrgOutEdgeDist(Set<BitSet> setFilteredEdgeColours, Set<BitSet> setFilteredVertColours){
		
		if(setFilteredEdgeColours !=null && setFilteredVertColours != null){
			// keys are the edge's colours and the values are the distribution of edges per vertex's colour
			Map<BitSet, ObjectDistribution<BitSet>> mapEdgeColoToEdgeDist = new HashMap<BitSet, ObjectDistribution<BitSet>>();
			
			Object[] keyEdgeColors = mMapAvrgOutEdgeDist.keys;
			int iNoOfEdgeColors = keyEdgeColors.length;
			for(int i = 0 ; i < iNoOfEdgeColors; i++){
				if(mMapAvrgOutEdgeDist.allocated[i]){
					BitSet edgeColo = (BitSet) keyEdgeColors[i];
					if(setFilteredEdgeColours.contains(edgeColo)){
						ObjectDoubleOpenHashMap<BitSet> edgeDistPerVertColo = mMapAvrgOutEdgeDist.get(edgeColo); 
						Object[] arrVertColours = edgeDistPerVertColo.keys;
						double[] arrEdgeDist = edgeDistPerVertColo.values;

						List<BitSet> newLstVertColours = new ArrayList<BitSet>();
						List<Double> newLstEdgeDist = new ArrayList<Double>();
						for(int j = 0 ; j < arrVertColours.length ; j++){
							if(edgeDistPerVertColo.allocated[j]){
								BitSet vertColo = (BitSet) arrVertColours[j];
								if(setFilteredVertColours.contains(vertColo)){
									newLstVertColours.add(vertColo);
									newLstEdgeDist.add(arrEdgeDist[j]);
								}
							}
						}
						
						BitSet[] newArrVertColours = newLstVertColours.toArray(new BitSet[0]);
						double[] newArrEdgeDist = Doubles.toArray(newLstEdgeDist);
						
						ObjectDistribution<BitSet> newEdgeDist = new ObjectDistribution<>(newArrVertColours, newArrEdgeDist);
						mapEdgeColoToEdgeDist.put(edgeColo, newEdgeDist);
					}
				}
			}
			return mapEdgeColoToEdgeDist;
		}else{
			return getMapAvrgOutEdgeDist();
		}
	}
	
	private void apply(ColouredGraph[] origGrphs){
		
		OutEdgeDistBaseEColoPerVColo outEdgeDistAnalyzer = new OutEdgeDistBaseEColoPerVColo();
		EdgeColourDistributionMetric edgeColorDistMetric  = new EdgeColourDistributionMetric();
		for( ColouredGraph grph : origGrphs){
			
			ObjectDistribution<BitSet>  edgeColoDist = edgeColorDistMetric.apply(grph);
			ObjectDoubleOpenHashMap<BitSet> mapEdgeColoDist = MapUtil.convert(edgeColoDist);
			/**
			 * key1: edge's colors, values: the distribution of edges over vertex's colors
			 */
			Map<BitSet, ObjectDistribution<BitSet>> mapEdgeColoAndDist	= outEdgeDistAnalyzer.apply(grph);
			
			Set<BitSet> setEdgeColors = mapEdgeColoAndDist.keySet();
			
			for(BitSet edgeColo : setEdgeColors){
				
				// map of appearing time (key is vertex's color)
				ObjectIntOpenHashMap<BitSet> mapAppearTimes = mMapAvrgOutEdgeAppearTimes.get(edgeColo);
				if(mapAppearTimes == null){
					mapAppearTimes = new ObjectIntOpenHashMap<BitSet>();
					mMapAvrgOutEdgeAppearTimes.put(edgeColo, mapAppearTimes);
				}
				
				// map of average distribution of edges (key is vertex's color)
				ObjectDoubleOpenHashMap<BitSet> mapAvrgDist = mMapAvrgOutEdgeDist.get(edgeColo);
				if(mapAvrgDist == null){
					mapAvrgDist = new ObjectDoubleOpenHashMap<BitSet>();
					mMapAvrgOutEdgeDist.put(edgeColo, mapAvrgDist);
				}
				
				double iNoOfEdges = mapEdgeColoDist.get(edgeColo);
				
				ObjectDistribution<BitSet> edgeDistPerVColo = mapEdgeColoAndDist.get(edgeColo);
				BitSet[] vertColoSamples = edgeDistPerVColo.sampleSpace;
				double[] edgeDistValues = edgeDistPerVColo.values;
				
				int iNoOfVertColo = vertColoSamples.length;
				for(int i = 0 ; i < iNoOfVertColo ; i++){
					BitSet vertColo = vertColoSamples[i];
					double rate = edgeDistValues[i] /iNoOfEdges;
					
					mapAvrgDist.putOrAdd(vertColo, rate, rate);
					mapAppearTimes.putOrAdd(vertColo, 1, 1);
				}
			}
		}
		
		//compute average distribution of edges over all different versions
		Object[] keyEdgeColors =  mMapAvrgOutEdgeAppearTimes.keys;
		int iNoOfEdgeColors = keyEdgeColors.length;
		for(int i = 0 ; i < iNoOfEdgeColors ; i++){
			if(mMapAvrgOutEdgeAppearTimes.allocated[i]){
				BitSet edgeColo = (BitSet) keyEdgeColors[i];
				ObjectIntOpenHashMap<BitSet> mapAppearTime = mMapAvrgOutEdgeAppearTimes.get(edgeColo);
				Object[] keyVertColors = mapAppearTime.keys;
				
				ObjectDoubleOpenHashMap<BitSet> mapAvrgDist = mMapAvrgOutEdgeDist.get(edgeColo);
				int iNoOfVertColors = keyVertColors.length;
				for(int j = 0 ; j< iNoOfVertColors ;j++){
					if(mapAppearTime.allocated[j]){
						BitSet vertColo = (BitSet) keyVertColors[j];
						int iNoOfAppearTime = mapAppearTime.get(vertColo);
						
						double sumRate = mapAvrgDist.get(vertColo);
						if(iNoOfAppearTime != 0){
							mapAvrgDist.put(vertColo, sumRate/iNoOfAppearTime);
						}else{
							mapAvrgDist.put(vertColo, 0);
						}
					}
				}
			}
		}
		//System.out.println("complete the computation");
	}
}
