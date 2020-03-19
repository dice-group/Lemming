package org.aksw.simba.lemming.creation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.aksw.simba.lemming.BaselineGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;

import com.carrotsearch.hppc.BitSet;

public class BaselineCreator {
	private BaselineGraph graph;
	
	public BaselineCreator(BaselineGraph graph) {
		this.graph = graph;
	}
	
	public void applyVertexDistribution(ObjectDistribution<BitSet> vertexColourDistribution) {
		Map<Integer, BitSet> vertexColourMap = assignColours(vertexColourDistribution);
		graph.setVertexColourMap(vertexColourMap);
	}
	
	public void applyEdgeDistribution(ObjectDistribution<BitSet> edgeColourDistribution) {
		Map<Integer, BitSet> edgeColourMap = assignColours(edgeColourDistribution);
		graph.setVertexColourMap(edgeColourMap);
	}

	private Map<Integer, BitSet> assignColours(ObjectDistribution<BitSet> colourDistribution) {
		Random randomGen = new Random(graph.getSeed());
		Map<Integer, BitSet> colourIDMap = new HashMap<Integer, BitSet>();
		double sum = DoubleStream.of(colourDistribution.getValues()).sum();
		
		Map<BitSet, Double> map = IntStream.range(0, colourDistribution.getSampleSpace().length).boxed()
			    .collect(Collectors.toMap(i -> colourDistribution.getSampleSpace()[i], i -> colourDistribution.getValues()[i]));
		
		//sort colours by frequency
		List<BitSet> sortedColours = map.entrySet().stream()
			       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			       .map(Map.Entry::getKey)
			       .collect(Collectors.toCollection(ArrayList::new));
		
		//foreach node/edge
		for (int i = 0; i < sum; i++) {
			int random = randomGen.nextInt((int) Math.round(sum));
			int id = -1;
			while(random>0) {
				id++;
				int count = (int) Math.round(map.get(sortedColours.get(id)));
				random=-count;
			}
			// assign colour to current node if not already assigned (shouldn't be)
			colourIDMap.putIfAbsent(id, sortedColours.get(id));
		}
		return colourIDMap;
	}
}
