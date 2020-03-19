package org.aksw.simba.lemming.creation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
		Map<Integer, BitSet> vertexColourMap = assignColours(vertexColourDistribution, graph.getGraph().getNumberOfNodes());
		graph.setVertexColourMap(vertexColourMap);
	}

	public void applyEdgeDistribution(ObjectDistribution<BitSet> edgeColourDistribution) {
		Map<Integer, BitSet> edgeColourMap = assignColours(edgeColourDistribution, graph.getGraph().getNumberOfEdges());
		graph.setVertexColourMap(edgeColourMap);
	}

	/**
	 * 
	 * @param colourDistribution
	 * @param max number of nodes / edges in the graph
	 * @return
	 */
	private Map<Integer, BitSet> assignColours(ObjectDistribution<BitSet> colourDistribution, int max) {
		Random randomGen = new Random(graph.getSeed());
		Map<Integer, BitSet> colourIDMap = new HashMap<Integer, BitSet>();
		double sum = DoubleStream.of(colourDistribution.getValues()).sum();

		Map<BitSet, Double> map = IntStream.range(0, colourDistribution.getSampleSpace().length).boxed().collect(
				Collectors.toMap(i -> colourDistribution.getSampleSpace()[i], i -> colourDistribution.getValues()[i]));

		List<BitSet> sortedColours = sortByValueThenKey(map);

		// foreach node/edge
		for (int j = 0; j < max; j++) {
			// for each colour
			for (int i = 0; i < sum; i++) {
				double random = sum * randomGen.nextDouble();
				int id = -1;
				while (random > 0) {
					id++;
					double count = map.get(sortedColours.get(id));
					random = -count;
				}
				// assign colour to current node if not already assigned (shouldn't be)
				colourIDMap.putIfAbsent(j, sortedColours.get(id));
			}
		}

		return colourIDMap;
	}

	/**
	 * Sort by value, if value is the same, sort by key
	 * 
	 * @param map
	 * @return
	 */
	private List<BitSet> sortByValueThenKey(Map<BitSet, Double> map) {
		Comparator<Entry<BitSet, Double>> valueThenKeyComparator = new Comparator<Entry<BitSet, Double>>() {
			@Override
			public int compare(Entry<BitSet, Double> arg0, Entry<BitSet, Double> arg1) {
				int comparison = Double.compare(arg0.getValue(), arg1.getValue());
				if (comparison != 0) {
					return comparison;
				} else {
					if (arg0.getKey().equals(arg1.getKey()))
						return 0;
					BitSet xor = (BitSet) arg0.getKey().clone();
					xor.xor(arg1.getKey());
					int firstDifferent = (int) (xor.length() - 1);
					if (firstDifferent == -1)
						return 0;

					return arg1.getKey().get(firstDifferent) ? 1 : -1;
				}
			}
		};
		return map.entrySet().stream().sorted(valueThenKeyComparator).map(Map.Entry::getKey)
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
