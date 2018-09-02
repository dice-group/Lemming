package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.util.MapUtil;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * This class will calculate the distribution of vertices in a particular color over 
 * the data typed properties. For example,there are 30 green vertices (among 57 green vertices)
 * having the data typed property rdf:label.
 * A data typed property is represented as one colour.
 * @author nptsy
 */
public class ColouredVDistPerDTEColour extends AbstractMetric 	
											implements IMultiObjectDistributionMetric<BitSet>{

	public ColouredVDistPerDTEColour(String name) {
		super(name);
	}
	
	public ColouredVDistPerDTEColour() {
		super("ColouredVDistPerDTEdgeColour");
	}

	/**
	 * calculate the vertex distribution over the data typed property of the given graph
	 * the key is the data typed property's colours and the value is the distribution of vertices
	 * in each vertex's colour
	 * @param the given graph
	 */
	@Override
	public Map<BitSet, ObjectDistribution<BitSet>> apply(ColouredGraph graph) {
		Map<BitSet, ObjectDoubleOpenHashMap<BitSet>> mapDTEColoToVColoDist = 
				new HashMap<BitSet,ObjectDoubleOpenHashMap<BitSet>>(); 
		
		/*
		 *  get the map of data typed properties to the set of vertices
		 *  the key is the data typed property's colour and the value is the set of vertex's IDs
		 */
		Map<BitSet, IntSet> mapDTEColoursToVIds =
				graph.getMapDTEdgeColoursToVertexIDs();
		
		// array of data typed property's colours
		Set<BitSet> setOfDTEColours = mapDTEColoursToVIds.keySet();
		// number of colours
		for(BitSet dteColo: setOfDTEColours){
			// distribution of vertices in particular vertex's colours
			ObjectDoubleOpenHashMap<BitSet> counts = mapDTEColoToVColoDist.get(dteColo);
			if(counts == null ){
				counts = new ObjectDoubleOpenHashMap<BitSet>();
				mapDTEColoToVColoDist.put(dteColo, counts);
			}
			
			IntSet setOfVIds = mapDTEColoursToVIds.get(dteColo);
			int [] arrOfVIds = setOfVIds.toIntArray();
			
			// count the number vertices 
			for(int vId : arrOfVIds){
				BitSet vColo = graph.getVertexColour(vId);
				counts.putOrAdd(vColo, 1, 1);
			}
		}
		
		// if the map (of data typed property's colours to the vertex distribution) has data
		if(mapDTEColoToVColoDist.size()> 0 ){
			Map<BitSet, ObjectDistribution<BitSet>> mapRes = new HashMap<BitSet, ObjectDistribution<BitSet>>();
			Set<BitSet> setDTEdgeColours = mapDTEColoToVColoDist.keySet();
			for(BitSet dtEdgeColo : setDTEdgeColours){
				ObjectDoubleOpenHashMap<BitSet> vertColoDist = mapDTEColoToVColoDist.get(dtEdgeColo);
				mapRes.put(dtEdgeColo, MapUtil.convert(vertColoDist));
			}
			return mapRes;
		}
		return null;
	}

}
