package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Compute, for each edge's color, the distribution of edges over vertex's
 * colors i.e., for green out-edges, the red vertices use about 15 green out-edges,
 * about 30 green out-edges are used for the blue vertices and the rest of them are
 * for the yellow vertices.
 * 
 * @author nptsy
 */
public class ColouredOEDistPerVColour extends AbstractMetric implements IMultiObjectDistributionMetric<BitSet>{
	
	
	public ColouredOEDistPerVColour(String name) {
		super(name);
	}
	
	public ColouredOEDistPerVColour() {
		super("ColouredOEDistPerVColour");
	}
	
	@Override
	public Map<BitSet, ObjectDistribution<BitSet>> apply(ColouredGraph graph) {
		IntSet setOfVIds = graph.getVertices();
		int[] arrOfVIds = setOfVIds.toIntArray();
		
		/**
		 * the keys are the edge's colors and the values are the distribution of edges over each vertex's color
		 */
		Map<BitSet, ObjectIntOpenHashMap<BitSet>> data = new HashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		  
		// iterate all vertices 
		for(int vId : arrOfVIds){
			BitSet vColo = graph.getVertexColour(vId);
			// set of out edges
			IntSet setOfOEIds = graph.getOutEdges(vId);
			int[] arrOfOEIds = setOfOEIds.toIntArray();
			
			
			// count the number of edges associated with the current vertex 
			for(int eId : arrOfOEIds){
				BitSet eColo = graph.getEdgeColour(eId); 
				// the number of edges base vertex's colours
				ObjectIntOpenHashMap<BitSet> counts = data.get(eColo);
				
				// data checks a key already existing
				if(counts == null){
					counts = new ObjectIntOpenHashMap<BitSet>();
					data.put(eColo, counts);
				}
				counts.putOrAdd(vColo, 1, 1);
			}			
		}
		
		// value ObjectIntOpenHashMap 
		Map<BitSet, ObjectDistribution<BitSet>> result = new HashMap<BitSet, ObjectDistribution<BitSet>>();
		
		for (BitSet key: data.keySet()){
			// the number of edges base vertex's colours
			ObjectIntOpenHashMap<BitSet> counts = data.get(key);
			
			BitSet sampleVColours[] = new BitSet[counts.assigned];
			double oeDistribution[] = new double [counts.assigned];
			
			int pos = 0;
			
			for (int i = 0; i < counts.allocated.length; ++i) {
				if (counts.allocated[i]) {
					// the vertex's colour
					sampleVColours[pos] = (BitSet)((Object[]) counts.keys)[i];
					// the number of edges
					oeDistribution[pos] = counts.values[i];
					++pos;
				}
			}
			result.put(key, new ObjectDistribution<BitSet>(sampleVColours, oeDistribution));
		}
		return result;
	}
}


