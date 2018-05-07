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
 * Compute, for each edge's color, the distribution of edges over vertex's
 * colors i.e., for green out-edges, the red vertices use about 15 green out-edges,
 * about 30 green out-edges are used for the blue vertices and the rest of them are
 * for the yellow vertices.
 * 
 * @author nptsy
 */
public class OutEdgeDistBaseEColoPerVColo extends AbstractMetric implements IMultiObjectDistributionMetric<BitSet>{
	
	
	public OutEdgeDistBaseEColoPerVColo(String name) {
		super(name);
	}
	
	public OutEdgeDistBaseEColoPerVColo() {
		super("OutDegreeColourDistOfEdgeColourMetric");
	}
	
	@Override
	public Map<BitSet, ObjectDistribution<BitSet>> apply(ColouredGraph graph) {
		IntSet setVertIDs = graph.getVertices();
		int[] arrOfVertIDs = setVertIDs.toIntArray();
		
		/**
		 * the keys are the edge's colors and the values are the distribution of edges for each vertex's color
		 */
		Map<BitSet, ObjectIntOpenHashMap<BitSet>> data = new HashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		  
		// iterate all vertices 
		for(int vertexID : arrOfVertIDs){
			BitSet vertColo = graph.getVertexColour(vertexID);
			// set of out edges
			IntSet setOfOutEdgeIDs = graph.getOutEdges(vertexID);
			int[] arrOfOutEdgeIDs = setOfOutEdgeIDs.toIntArray();
			for(int edgeId : arrOfOutEdgeIDs){
				BitSet edgeColor = graph.getEdgeColour(edgeId); 
				// value ObjectIntOpenHashMap 
				ObjectIntOpenHashMap<BitSet> counts = data.get(edgeColor);;
				
				// data checks a key already existing
				if(counts == null){
					counts = new ObjectIntOpenHashMap<BitSet>();
					data.put(edgeColor, counts);
				}
				counts.putOrAdd(vertColo, 1, 1);
			}			
		}
		
		// value ObjectIntOpenHashMap 
		Map<BitSet, ObjectDistribution<BitSet>> result = new HashMap<BitSet, ObjectDistribution<BitSet>>();
		
		for (BitSet key: data.keySet()){
			// value ObjectIntOpenHashMap 
			ObjectIntOpenHashMap<BitSet> counts = data.get(key);
			
			BitSet sampleSpace[] = new BitSet[counts.assigned];
			double distribution[] = new double [counts.assigned];
			
			int pos = 0;
			
			for (int i = 0; i < counts.allocated.length; ++i) {
				if (counts.allocated[i]) {
					sampleSpace[pos] = (BitSet)((Object[]) counts.keys)[i];
					distribution[pos] = counts.values[i];
					++pos;
				}
			}
			result.put(key, new ObjectDistribution<BitSet>(sampleSpace, distribution));
		}
		return result;
	}
}


