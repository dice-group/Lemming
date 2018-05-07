package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
/**
 * Compute average distribution of vertices per color over all versions
 * @author nptsy
 *
 */
public class AvrgVertColoDistMetric {
	private static final Logger LOGGER = LoggerFactory.getLogger(AvrgVertColoDistMetric.class);
	
	public static ObjectDistribution<BitSet> apply(ColouredGraph[] origGrphs){
		if(origGrphs != null && origGrphs.length >0){
			//vertex colour distribution
			VertexColourDistributionMetric vertexColourMetric = new VertexColourDistributionMetric();
			ObjectDoubleOpenHashMap<BitSet> mapVertColoRate = new ObjectDoubleOpenHashMap<BitSet>();
			ObjectIntOpenHashMap<BitSet> mapVertColoApprearTime = new ObjectIntOpenHashMap<BitSet>();
			
			for(ColouredGraph graph: origGrphs){
				// number of vertices
				int iNoVertices = graph.getVertices().size();
				
				//process vertex colour distribution
				ObjectDistribution<BitSet> vertColoDist = vertexColourMetric.apply(graph);
				
				BitSet[] vertSampleSpaces = vertColoDist.getSampleSpace();
				double[] vertSampleValues = vertColoDist.getValues();
				
				int iSizeVertSpaces = vertSampleSpaces.length;
				
				for(int i = 0 ; i < iSizeVertSpaces ; ++i ){
					BitSet vertColo = vertSampleSpaces[i];
					double rate = vertSampleValues[i]/(iNoVertices);
					
					mapVertColoRate.putOrAdd(vertColo, rate, rate);
					
					mapVertColoApprearTime.putOrAdd(vertColo, 1, 1);
				}
			}
			
			Object [] keyVertColo = mapVertColoApprearTime.keys;
			int iNoOfVertColo = keyVertColo.length;
			for(int i = 0 ; i< iNoOfVertColo; i++){
				if(mapVertColoApprearTime.allocated[i]){
					BitSet vertColo = (BitSet) keyVertColo[i];
					int iNoOfAppearTime = mapVertColoApprearTime.get(vertColo);
					if(iNoOfAppearTime != 0){
						double avertage = mapVertColoRate.get(vertColo)/iNoOfAppearTime;
						mapVertColoRate.put(vertColo, avertage);	
					}
					else{
						LOGGER.warn("the vertColo does not exist!");
					}
				}
			}
			return MapUtil.convert(mapVertColoRate);
		}
		return null;
	}
	
}
