package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.VertexColourDistributionMetric;
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
		
		int numberOfGraph = 0;
		if(origGrphs != null && (numberOfGraph = origGrphs.length) >0){
			//vertex colour distribution
			VertexColourDistributionMetric vertexColourMetric = new VertexColourDistributionMetric();
			ObjectDoubleOpenHashMap<BitSet> mapVertColoRate = new ObjectDoubleOpenHashMap<BitSet>();
			
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
				}
			}
			
			Object [] keyVertColo = mapVertColoRate.keys;
			for(int i = 0 ; i< keyVertColo.length; i++){
				if(mapVertColoRate.allocated[i]){
					BitSet vertColo = (BitSet) keyVertColo[i];
					double avertage = mapVertColoRate.get(vertColo)/numberOfGraph;
					mapVertColoRate.put(vertColo, avertage);	
				}
			}
			return MapUtil.convert(mapVertColoRate);
		}else{
			LOGGER.warn("Find no input graphs!");
		}
		return null;
	}
	
}
