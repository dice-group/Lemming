package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.generator.baseline.IGenerator;
import org.aksw.simba.lemming.util.MapUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

import grph.Grph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Baseline graph generator. It generates a baseline graph and then converts it
 * to a {@link ColouredGraph}.
 * 
 * @author Alexandra Silva
 *
 */
@Component("Bl")
@Scope(value = "prototype")
public class BaselineGenerator extends GraphGenerator {

	/** 
	 * Graph Initializer object 
	 */
	private BaselineInitializer graphInitializer;

	/**
	 * Baseline model
	 */
	private IGenerator baseline;

	/**
	 * The vertex to colour map
	 */
	private Map<Integer, BitSet> vertexColourMap;
	/**
	 * The edge to colour map
	 */
	private Map<Integer, BitSet> edgeColourMap;
	/**
	 * The colour to vertex map
	 */
	private Map<BitSet, IntSet> colourVertexIds;
	/**
	 * The colour to edge map
	 */
	private Map<BitSet, IntSet> colourEdgeIds;

	/**
	 * Constructor
	 * <p>
	 * Generates a barabasi graph and applies the average colour distribution from
	 * all the input graphs to it.
	 * 
	 * @param noNodes
	 * @param colouredGraphs
	 * @param seed
	 * @param valuesCarrier
	 * @param metrics
	 */
	public BaselineGenerator(BaselineInitializer graphInitializer, IGenerator baseline) {
		super(graphInitializer, null, null);
		this.graphInitializer = graphInitializer;
		this.baseline = baseline;
	}

	@Override
	public void initializeMimicGraph(ColouredGraph mimicGraph, int noOfThreads) {

		int noNodes = graphInitializer.getDesiredNoOfVertices();
		double avgDegree = (double) graphInitializer.getDesiredNoOfEdges() / noNodes;

		// generate the baseline graph
		Grph baselineGraph = baseline.generateGraph(noNodes, avgDegree,
				graphInitializer.getSeedGenerator().getNextSeed());

		// convert it to a ColouredGraph object
		mimicGraph.setGraph(baselineGraph);
		graphInitializer.copyColourPalette(graphInitializer.getOriginalGraphs(), mimicGraph);

		// assign colours
		applyEdgeDistribution(graphInitializer.getEdgeColourDist(), baselineGraph.getNumberOfEdges());
		applyVertexDistribution(graphInitializer.getVertexColourDist(), baselineGraph.getNumberOfVertices());
		mimicGraph.setEdgeColours(edgeColourMap);
		mimicGraph.setVertexColours(vertexColourMap);
		graphInitializer.setMapColourToEdgeIDs(colourEdgeIds);
		graphInitializer.setMapColourToVertexIDs(colourVertexIds);
	}

	public Map<BitSet, IntSet> getColourVertexIds() {
		return colourVertexIds;
	}

	public Map<BitSet, IntSet> getColourEdgeIds() {
		return colourEdgeIds;
	}

	public void applyVertexDistribution(ObjectDistribution<BitSet> vertexColourDistribution, int noVerts) {
		Map<Integer, BitSet> vertexColourMap = assignColours(vertexColourDistribution, noVerts);
		this.colourVertexIds = MapUtil.groupMapByValue(vertexColourMap);
		this.vertexColourMap = vertexColourMap;
	}

	public void applyEdgeDistribution(ObjectDistribution<BitSet> edgeColourDistribution, int noEdges) {
		Map<Integer, BitSet> edgeColourMap = assignColours(edgeColourDistribution, noEdges);
		this.colourEdgeIds = MapUtil.groupMapByValue(edgeColourMap);
		this.edgeColourMap = edgeColourMap;
	}

	/**
	 * Assigns colours based on a given distribution
	 * 
	 * @param colourDistribution the colour distribution
	 * @param max                number of nodes / edges in the graph
	 * @return
	 */
	public Map<Integer, BitSet> assignColours(ObjectDistribution<BitSet> colourDistribution, int max) {
		Random randomGen = new Random(graphInitializer.getSeedGenerator().getNextSeed());
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
}