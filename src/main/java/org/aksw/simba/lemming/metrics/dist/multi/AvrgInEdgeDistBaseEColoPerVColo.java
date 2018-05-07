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
 * Compute the average vertex's color distribution for each in-edge's color.
 * i.e., for green in-edges, the red vertices have average 20.5% of green in-edges,
 * the blue vertices use about 28.5% of green in-edges and the yellow vertices use
 * the rest of the green in-edges.
 * 
 * @author nptsy
 *
 */
public class AvrgInEdgeDistBaseEColoPerVColo {
	/**
	 * key1: edge's colors, values the appearing time of edge's color associated
	 * with the vertex's color over all versions
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mMapAvrgInEdgeAppearTimes;
	
	/**
	 * key1: edge's colors, values: the distribution of edges over different vertex's colors
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgInEdgeDist;
	
	public static String getName (){
		return "AvrgInEdgeDistBaseEColoPerVColo";
	}
	
	public AvrgInEdgeDistBaseEColoPerVColo(ColouredGraph[] origGrphs){
		mMapAvrgInEdgeAppearTimes = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		mMapAvrgInEdgeDist = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		
		apply(origGrphs);
	}
	
	public Map<BitSet, ObjectDistribution<BitSet>> getMapAvrgInEdgeDist(){
		Map<BitSet, ObjectDistribution<BitSet>> res = new HashMap<BitSet, ObjectDistribution<BitSet>>();
		Object[] keyEdgeColors = mMapAvrgInEdgeDist.keys;
		int iNoOfEdgeColors = keyEdgeColors.length;
		for(int i = 0 ; i < iNoOfEdgeColors; i++){
			if(mMapAvrgInEdgeDist.allocated[i]){
				BitSet edgeColo = (BitSet) keyEdgeColors[i];
				ObjectDistribution<BitSet> dist = MapUtil.convert(mMapAvrgInEdgeDist.get(edgeColo)); 
				res.put(edgeColo, dist);
			}
		}
		return res;
	}
	
	public Map<BitSet, ObjectDistribution<BitSet>> getMapAvrgInEdgeDist(Set<BitSet> setFilteredEdgeColours, Set<BitSet> setFilteredVertColours){
		
		if(setFilteredEdgeColours !=null && setFilteredVertColours != null){
			// keys are the edge's colours and the values are the distribution of edges per vertex's colour
			Map<BitSet, ObjectDistribution<BitSet>> mapEdgeColoToEdgeDist = new HashMap<BitSet, ObjectDistribution<BitSet>>();
			
			Object[] keyEdgeColors = mMapAvrgInEdgeDist.keys;
			int iNoOfEdgeColors = keyEdgeColors.length;
			for(int i = 0 ; i < iNoOfEdgeColors; i++){
				if(mMapAvrgInEdgeDist.allocated[i]){
					BitSet edgeColo = (BitSet) keyEdgeColors[i];
					if(setFilteredEdgeColours.contains(edgeColo)){
						ObjectDoubleOpenHashMap<BitSet> edgeDistPerVertColo = mMapAvrgInEdgeDist.get(edgeColo); 
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
			return getMapAvrgInEdgeDist();
		}
	}
	
	private void apply(ColouredGraph[] origGrphs){
		
		InEdgeDistBaseEColoPerVColo inEdgeDistAnalyzer = new InEdgeDistBaseEColoPerVColo();
		EdgeColourDistributionMetric edgeColorDistMetric  = new EdgeColourDistributionMetric();
		for( ColouredGraph grph : origGrphs){
			
			ObjectDistribution<BitSet>  edgeColoDist = edgeColorDistMetric.apply(grph);
			ObjectDoubleOpenHashMap<BitSet> mapEdgeColoDist = MapUtil.convert(edgeColoDist);
			/**
			 * key1: edge's colors, values: the distribution of edges over vertex's colors
			 */
			Map<BitSet, ObjectDistribution<BitSet>> mapEdgeColoAndDist	= inEdgeDistAnalyzer.apply(grph);
			
			Set<BitSet> setEdgeColors = mapEdgeColoAndDist.keySet();
			
			for(BitSet edgeColo : setEdgeColors){
				
				// map of appearing time (key is vertex's color)
				ObjectIntOpenHashMap<BitSet> mapAppearTimes = mMapAvrgInEdgeAppearTimes.get(edgeColo);
				if(mapAppearTimes == null){
					mapAppearTimes = new ObjectIntOpenHashMap<BitSet>();
					mMapAvrgInEdgeAppearTimes.put(edgeColo, mapAppearTimes);
				}
				
				// map of average distribution of edges (key is vertex's color)
				ObjectDoubleOpenHashMap<BitSet> mapAvrgDist = mMapAvrgInEdgeDist.get(edgeColo);
				if(mapAvrgDist == null){
					mapAvrgDist = new ObjectDoubleOpenHashMap<BitSet>();
					mMapAvrgInEdgeDist.put(edgeColo, mapAvrgDist);
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
		Object[] keyEdgeColors =  mMapAvrgInEdgeAppearTimes.keys;
		int iNoOfEdgeColors = keyEdgeColors.length;
		for(int i = 0 ; i < iNoOfEdgeColors ; i++){
			if(mMapAvrgInEdgeAppearTimes.allocated[i]){
				BitSet edgeColo = (BitSet) keyEdgeColors[i];
				ObjectIntOpenHashMap<BitSet> mapAppearTime = mMapAvrgInEdgeAppearTimes.get(edgeColo);
				Object[] keyVertColors = mapAppearTime.keys;
				
				ObjectDoubleOpenHashMap<BitSet> mapAvrgDist = mMapAvrgInEdgeDist.get(edgeColo);
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
