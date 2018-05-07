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
 * colors i.e., for green in-edges, the red vertices use about 15 green in-edges,
 * about 30 green in-edges are used for the blue vertices and the rest of them are 
 * for the yellow vertices.
 * 
 * @author nptsy
 */

public class InEdgeDistBaseEColoPerVColo extends AbstractMetric implements IMultiObjectDistributionMetric<BitSet>{

	public InEdgeDistBaseEColoPerVColo(String name) {
		super(name);
	}

	public InEdgeDistBaseEColoPerVColo() {
		super("InDegreeColourDistOfEdgeColourMetric");
	}
	
	@Override
	public Map<BitSet, ObjectDistribution<BitSet>> apply(ColouredGraph graph) {
		
		// the set of all vertices
		IntSet intSetVertices = graph.getVertices();
		int[] setOfVertices = intSetVertices.toIntArray();
		
		/**
		 * the keys are the edge's colors and the values are the distribution of edges over vertex's colors
		 */
		Map<BitSet, ObjectIntOpenHashMap<BitSet>> data = new HashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		  
		// iterate all vertices 
		for(int vertexID : setOfVertices){
			BitSet vertexColour = graph.getVertexColour(vertexID);
			// the set of incoming edges
			IntSet intsetOfInEdges = graph.getInEdges(vertexID);
			int[] setOfInEdges = intsetOfInEdges.toIntArray();
			
			// value ObjectIntOpenHashMap for storing distribution of edge colours
			for(int edgeID : setOfInEdges){
				// edge colour
				BitSet edgeColor = graph.getEdgeColour(edgeID); 
				ObjectIntOpenHashMap<BitSet> counts = data.get(edgeColor);
				// data checks a key already existing
				if(counts == null){
					counts = new ObjectIntOpenHashMap<BitSet>();
					data.put(edgeColor, counts);
				}
				
				counts.putOrAdd(vertexColour, 1, 1);
			}			
		}
		
		// value ObjectIntOpenHashMap 
		Map<BitSet, ObjectDistribution<BitSet>> result = new HashMap<BitSet, ObjectDistribution<BitSet>>();
		
		// key is vertex colour
		for (BitSet key: data.keySet()){
			// get distribution of each vertex colour 
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
