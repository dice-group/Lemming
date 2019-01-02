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
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * Compute, for each in-edge's colour, the average distribution of in-edges over vertex's
 * colors. i.e., for green in-edges, the red vertices use average 16.5% of green in-edges,
 * the blue vertices use about 15.5% of the green in-edges and the yellow vertices use
 * the rest of the green in-edges.
 * 
 * @author nptsy
 */
public class AvrgColouredIEDistPerVColour {
	/**
	 * key1: edge's colors, values: the distribution of edges over different vertex's colors
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mMapAvrgInEdgeDist;
	
	public static String getName (){
		return "AvrgColouredIEDistPerVColour";
	}
	
	public AvrgColouredIEDistPerVColour(ColouredGraph[] origGrphs){
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
		
		int numberOfGraphs = 0;
		if (origGrphs!= null && (numberOfGraphs = origGrphs.length) > 0 ){
			ColouredIEDistPerVColour inEdgeDistAnalyzer = new ColouredIEDistPerVColour();
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
					}
				}
			}
			
			//compute average distribution of edges over all different versions
			Object[] keyEdgeColors =  mMapAvrgInEdgeDist.keys;
			int iNoOfEdgeColors = keyEdgeColors.length;
			for(int i = 0 ; i < iNoOfEdgeColors ; i++){
				if(mMapAvrgInEdgeDist.allocated[i]){
					BitSet edgeColo = (BitSet) keyEdgeColors[i];
					
					ObjectDoubleOpenHashMap<BitSet> mapAvrgDist = mMapAvrgInEdgeDist.get(edgeColo);
					Object[] arrVertexColours = mapAvrgDist.keys;
					for(int j = 0 ; j< arrVertexColours.length ;j++){
						if(mapAvrgDist.allocated[j]){
							BitSet vertColo = (BitSet) arrVertexColours[j];
							
							double sumRate = mapAvrgDist.get(vertColo);
							mapAvrgDist.put(vertColo, sumRate/numberOfGraphs);
						}
					}
				}
			}
		}
		
		//System.out.println("complete the computation");
	}
}
