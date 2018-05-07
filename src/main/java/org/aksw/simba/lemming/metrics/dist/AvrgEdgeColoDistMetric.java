package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * Compute average distribution of edges per color over all versions
 * @author nptsy
 *
 */
public class AvrgEdgeColoDistMetric {
	
private static final Logger LOGGER = LoggerFactory.getLogger(AvrgEdgeColoDistMetric.class);
	
	public static ObjectDistribution<BitSet> apply(ColouredGraph[] origGrphs){
		if(origGrphs != null && origGrphs.length >0){
			//vertex colour distribution
			EdgeColourDistributionMetric edgeColourMetric = new EdgeColourDistributionMetric();
			ObjectDoubleOpenHashMap<BitSet> mapEdgeColoRate = new ObjectDoubleOpenHashMap<BitSet>();
			ObjectIntOpenHashMap<BitSet> mapEdgeColoApprearTime = new ObjectIntOpenHashMap<BitSet>();
			
			for(ColouredGraph graph: origGrphs){
				// number of vertices
				int iNoOfEdges = graph.getEdges().size();
				
				//process vertex colour distribution
				ObjectDistribution<BitSet> edgeColoDist = edgeColourMetric.apply(graph);
				
				BitSet[] edgeSampleSpaces = edgeColoDist.getSampleSpace();
				double[] edgeSampleValues = edgeColoDist.getValues();
				
				int iSizeEdgeSpaces = edgeSampleSpaces.length;
				
				for(int i = 0 ; i < iSizeEdgeSpaces ; ++i ){
					BitSet edgeColo = edgeSampleSpaces[i];
					double rate = edgeSampleValues[i]/(iNoOfEdges);
					
					mapEdgeColoRate.putOrAdd(edgeColo, rate, rate);
					mapEdgeColoApprearTime.putOrAdd(edgeColo, 1, 1);
				}
			}
			
			Object [] keyEdgeColo = mapEdgeColoApprearTime.keys;
			int iNoOfEdgeColo = keyEdgeColo.length;
			for(int i = 0 ; i< iNoOfEdgeColo; i++){
				if(mapEdgeColoApprearTime.allocated[i]){
					BitSet edgeColo = (BitSet) keyEdgeColo[i];
					int iNoOfAppearTime = mapEdgeColoApprearTime.get(edgeColo);
					if(iNoOfAppearTime != 0){
						double avertage = mapEdgeColoRate.get(edgeColo)/iNoOfAppearTime;
						mapEdgeColoRate.put(edgeColo, avertage);	
					}
					else{
						LOGGER.warn("the vertColo does not exist!");
					}
				}
			}
			return MapUtil.convert(mapEdgeColoRate);
		}
		return null;
	}
	
}
