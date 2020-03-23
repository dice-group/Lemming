package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.aksw.simba.lemming.BaselineGraph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.InMemoryPalette;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgEdgeColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgVertColoDistMetric;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

import toools.set.IntSet;

public class BaselineGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaselineGenerator.class);
	private ColouredGraph mimicGraph;
	private BaselineGraph baselineGraph;

	public BaselineGenerator(int noNodes, ColouredGraph[] colouredGraphs, long seed) {
		// estimate an average degree
		double avgDegree = estimateNoEdges(colouredGraphs, noNodes) / noNodes;
		LOGGER.info("Estimated average degree: " + avgDegree);

		// colour distribution
		ObjectDistribution<BitSet> vertexDistribution = AvrgVertColoDistMetric.apply(colouredGraphs);
		ObjectDistribution<BitSet> edgeDistribution = AvrgEdgeColoDistMetric.apply(colouredGraphs);

		// Generate a random, scale-free Barabasi graph
		baselineGraph = new BaselineGraph(noNodes, avgDegree, seed);

		// assign colours
		applyEdgeDistribution(edgeDistribution);
		applyVertexDistribution(vertexDistribution);

		// convert it to a ColouredGraph object
		mimicGraph = new ColouredGraph(baselineGraph.getGraph().getGraph(), null, null, null);
		copyColourPalette(colouredGraphs, mimicGraph);
		mimicGraph.setEdgeColours(baselineGraph.getEdgeColourMap());
		mimicGraph.setVertexColours(baselineGraph.getVertexColourMap());

	}

	public Map<BitSet, IntSet> getColourVertexIds() {
		return baselineGraph.getColourVertexIds();
	}

	public ColouredGraph getMimicGraph() {
		return mimicGraph;
	}

	public void applyVertexDistribution(ObjectDistribution<BitSet> vertexColourDistribution) {
		Map<Integer, BitSet> vertexColourMap = assignColours(vertexColourDistribution,
				baselineGraph.getGraph().getNumberOfNodes());
		baselineGraph.setColourVertexIds(MapUtil.groupMapByValue(vertexColourMap));
		baselineGraph.setVertexColourMap(vertexColourMap);
	}

	public void applyEdgeDistribution(ObjectDistribution<BitSet> edgeColourDistribution) {
		Map<Integer, BitSet> edgeColourMap = assignColours(edgeColourDistribution,
				baselineGraph.getGraph().getNumberOfEdges());
		baselineGraph.setColourEdgeIds(MapUtil.groupMapByValue(edgeColourMap));
		baselineGraph.setEdgeColourMap(edgeColourMap);
	}

	/**
	 * Assigns colours based on a given distribution
	 * 
	 * @param colourDistribution
	 * @param max                number of nodes / edges in the graph
	 * @return
	 */
	public Map<Integer, BitSet> assignColours(ObjectDistribution<BitSet> colourDistribution, int max) {
		Random randomGen = new Random(baselineGraph.getSeed());
		Map<Integer, BitSet> nodeIdToColourMap = new HashMap<Integer, BitSet>();
		double sum = DoubleStream.of(colourDistribution.getValues()).sum();

		Map<BitSet, Double> map = IntStream.range(0, colourDistribution.getSampleSpace().length).boxed().collect(
				Collectors.toMap(i -> colourDistribution.getSampleSpace()[i], i -> colourDistribution.getValues()[i]));

		List<BitSet> sortedColours = MapUtil.sortByValueThenKey(map);

		// foreach node/edge
		for (int j = 0; j < max; j++) {
			double random = sum * randomGen.nextDouble();
			int id = -1;
			while (random > 0) {
				id++;
				double count = map.get(sortedColours.get(id));
				random -= count;
			}
			// assign colour to current node/edge if not already assigned (shouldn't be)
			nodeIdToColourMap.putIfAbsent(j, sortedColours.get(id));
		}

		return nodeIdToColourMap;
	}

	/**********************************************************************
	 * org.aksw.simba.lemming.mimicgraph.generator.AbstractGraphGeneration
	 * ********************************************************************
	 */

	/**
	 * draft estimation of number edges
	 * 
	 * @param origGrphs
	 */
	private double estimateNoEdges(ColouredGraph[] origGrphs, int noVertices) {
		LOGGER.info("Estimate the number of edges in the new graph.");
		double estimatedEdges = 0;
		if (origGrphs != null && origGrphs.length > 0) {
			int iNoOfVersions = origGrphs.length;
			double noEdges = 0;
			for (ColouredGraph graph : origGrphs) {
				int iNoEdges = graph.getEdges().size();
				int iNoVertices = graph.getVertices().size();
				noEdges += iNoEdges / (iNoVertices * 1.0);
			}
			noEdges *= noVertices;
			noEdges /= iNoOfVersions;
			estimatedEdges = (int) Math.round(noEdges);
			LOGGER.warn("Estimated the number of edges in the new graph is " + estimatedEdges);
		} else {
			LOGGER.warn("The array of original graphs is empty!");
		}
		return estimatedEdges;
	}

	private void copyColourPalette(ColouredGraph[] origGraphs, ColouredGraph mimicGraph) {
		if (Constants.IS_EVALUATION_MODE) {
			ColourPalette newVertexPalette = new InMemoryPalette();
			ColourPalette newEdgePalette = new InMemoryPalette();
			ColourPalette newDTEdgePalette = new InMemoryPalette();

			// copy colour palette of all the original graphs to the new one
			for (ColouredGraph grph : origGraphs) {
				// merge vertex colours
				ColourPalette vPalette = grph.getVertexPalette();
				Map<String, BitSet> mapVertexURIsToColours = vPalette.getMapOfURIAndColour();
				fillColourToPalette(newVertexPalette, mapVertexURIsToColours);

				// merge edge colours
				ColourPalette ePalette = grph.getEdgePalette();
				Map<String, BitSet> mapEdgeURIsToColours = ePalette.getMapOfURIAndColour();
				fillColourToPalette(newEdgePalette, mapEdgeURIsToColours);

				// merge data typed edge colours
				ColourPalette dtePalette = grph.getDataTypedEdgePalette();
				Map<String, BitSet> mapDTEdgeURIsToColours = dtePalette.getMapOfURIAndColour();
				fillColourToPalette(newDTEdgePalette, mapDTEdgeURIsToColours);
			}

			mimicGraph.setVertexPalette(newVertexPalette);
			mimicGraph.setEdgePalette(newEdgePalette);
			mimicGraph.setDataTypeEdgePalette(newDTEdgePalette);
		}
	}

	private void fillColourToPalette(ColourPalette palette, Map<String, BitSet> mapOfURIsAndColours) {
		Object[] arrObjURIs = mapOfURIsAndColours.keySet().toArray();
		for (int i = 0; i < arrObjURIs.length; i++) {
			String uri = (String) arrObjURIs[i];
			BitSet colour = mapOfURIsAndColours.get(uri);
			palette.updateColour(colour, uri);
		}
	}
}