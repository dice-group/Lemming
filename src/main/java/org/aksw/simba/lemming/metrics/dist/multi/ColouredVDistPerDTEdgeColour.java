package org.aksw.simba.lemming.metrics.dist.multi;

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
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * This class will calculate the distribution of vertices in a particular color over 
 * the data typed properties. For example,there are 30 vertices having the data typed property rdf:label
 * A data typed property is represented as one colour.
 * @author nptsy
 */
public class ColouredVDistPerDTEdgeColour extends AbstractMetric 	
											implements IMultiObjectDistributionMetric<BitSet>{

	public ColouredVDistPerDTEdgeColour(String name) {
		super(name);
	}
	
	public ColouredVDistPerDTEdgeColour() {
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
		ObjectObjectOpenHashMap<BitSet, IntSet> mapDTEColoursToVIds =
				graph.getMapDTEdgeColoursToVertexIDs();
		
		// array of data typed property's colours
		Object[] arrOfDTEColours = mapDTEColoursToVIds.keys;
		// number of colours
		int noOfDTEColours = arrOfDTEColours.length;
		for(int i = 0 ; i < noOfDTEColours ; i++){
			if(mapDTEColoursToVIds.allocated[i]){
				BitSet dteColo = (BitSet) arrOfDTEColours[i];
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
