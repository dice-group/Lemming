package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.EdgeColourDistributionMetric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Compute average distribution of edges per color over all versions
 * @author nptsy
 *
 */
public class AvrgEdgeColoDistMetric {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AvrgEdgeColoDistMetric.class);
	
	public static ObjectDistribution<BitSet> apply(ColouredGraph[] origGrphs){
		int numberOfGraphs = 0;
		if(origGrphs != null && (numberOfGraphs = origGrphs.length) >0){
			//vertex colour distribution
			EdgeColourDistributionMetric edgeColourMetric = new EdgeColourDistributionMetric();
			ObjectDoubleOpenHashMap<BitSet> mapEdgeColoRate = new ObjectDoubleOpenHashMap<BitSet>();
			
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
				}
			}
			
			Object [] keyEdgeColo = mapEdgeColoRate.keys;
			for(int i = 0 ; i< keyEdgeColo.length; i++){
				if(mapEdgeColoRate.allocated[i]){
					BitSet edgeColo = (BitSet) keyEdgeColo[i];
						double avertage = mapEdgeColoRate.get(edgeColo)/numberOfGraphs;
						mapEdgeColoRate.put(edgeColo, avertage);	
				}
			}
			return MapUtil.convert(mapEdgeColoRate);
		}
		else{
			LOGGER.warn("Find no input graphs!");
		}
		return null;
	}
	
}
