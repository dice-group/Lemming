package org.aksw.simba.lemming.metrics.dist.multi;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * Compute, for each in-edge's color, the distribution of in-edges over vertex's
 * colors i.e., for green in-edges, the red vertices use about 15 green in-edges,
 * about 30 green in-edges are used for the blue vertices, and the rest of them are 
 * used for the yellow vertices.
 * 
 * @author nptsy
 */

public class ColouredIEDistPerVColour extends AbstractMetric implements IMultiObjectDistributionMetric<BitSet>{

	public ColouredIEDistPerVColour(String name) {
		super(name);
	}

	public ColouredIEDistPerVColour() {
		super("ColouredIEDistPerVColour");
	}
	
	@Override
	public Map<BitSet, ObjectDistribution<BitSet>> apply(ColouredGraph graph) {
		
		// the set of all vertices
		IntSet setOfVIds = graph.getVertices();
		int[] arrOfVIds = setOfVIds.toIntArray();
		
		/**
		 * the keys are the edge's colors and the values are the distribution of edges over vertex's colors
		 */
		Map<BitSet, ObjectIntOpenHashMap<BitSet>> data = new HashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		  
		// iterate all vertices 
		for(int vId : arrOfVIds){
			BitSet vColo = graph.getVertexColour(vId);
			// the set of incoming edges
			IntSet setOfIEIds = graph.getInEdges(vId);
			int[] arrOfIEIds = setOfIEIds.toIntArray();
			
			// value ObjectIntOpenHashMap for storing distribution of edge colours
			for(int eId : arrOfIEIds){
				// edge colour
				BitSet eColo = graph.getEdgeColour(eId); 
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
		
		// key is edge's colour
		for (BitSet eColo: data.keySet()){
			// get distribution edges over vertex's colour 
			ObjectIntOpenHashMap<BitSet> counts = data.get(eColo);
			
			BitSet sampleVColours[] = new BitSet[counts.assigned];
			double IEDistribution[] = new double [counts.assigned];
			
			int pos = 0;
			
			for (int i = 0; i < counts.allocated.length; ++i) {
				if (counts.allocated[i]) {
					// vertex's colours
					sampleVColours[pos] = (BitSet)((Object[]) counts.keys)[i];
					// number of edges
					IEDistribution[pos] = counts.values[i];
					++pos;
				}
			}
			result.put(eColo, new ObjectDistribution<BitSet>(sampleVColours, IEDistribution));
		}
		return result;
	}
}
