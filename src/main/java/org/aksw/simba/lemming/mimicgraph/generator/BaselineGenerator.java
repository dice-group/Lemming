package org.aksw.simba.lemming.mimicgraph.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ExtGrphBasedGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.EdgeModifier;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgEdgeColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgVertColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.ErrorScoreCalculator;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.MapUtil;
import org.dice_research.ldcbench.generate.GraphGenerator;
import org.dice_research.ldcbench.generate.ParallelBarabasiRDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Baseline graph generator. It generates a random, scale-free Barabasi graph
 * and then converts it to a {@link ColouredGraph}.
 * 
 * @author Alexandra Silva
 *
 */
public class BaselineGenerator extends BasicGraphGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaselineGenerator.class);
	
	/**
	 * The baseline's graph as a {@link ColouredGraph} 
	 */
	private ColouredGraph mimicGraph;
	/**
	 * Edge modifier - computes the graph's metrics
	 */
	private EdgeModifier edgeModifier;
	/**
	 * The error score calculator - computes the error score
	 */
	private ErrorScoreCalculator mErrScoreCalculator;
	/**
	 * Seed for result reproduction
	 */
	private long seed;

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
	public BaselineGenerator(int noNodes, ColouredGraph[] colouredGraphs, long seed, ConstantValueStorage valuesCarrier,
			List<SingleValueMetric> metrics) {
		// estimate an average degree
		double avgDegree = estimateNoEdges(colouredGraphs, noNodes) / noNodes;
		LOGGER.info("Estimated average degree: " + avgDegree);
		this.seed = seed;

		// colour distribution
		ObjectDistribution<BitSet> vertexDistribution = AvrgVertColoDistMetric.apply(colouredGraphs);
		ObjectDistribution<BitSet> edgeDistribution = AvrgEdgeColoDistMetric.apply(colouredGraphs);

		// Generate a random, scale-free Barabasi graph
		ExtGrphBasedGraph baselineGraph = new ExtGrphBasedGraph();
		GraphGenerator generator = new ParallelBarabasiRDF(Constants.BASELINE_STRING);
		generator.generateGraph(noNodes, avgDegree, seed, baselineGraph);

		// convert it to a ColouredGraph object
		mimicGraph = new ColouredGraph(baselineGraph.getGrph(), null, null, null);
		copyColourPalette(colouredGraphs, mimicGraph);

		// assign colours
		applyEdgeDistribution(edgeDistribution, baselineGraph.getNumberOfNodes());
		applyVertexDistribution(vertexDistribution, baselineGraph.getNumberOfNodes());
		mimicGraph.setEdgeColours(edgeColourMap);
		mimicGraph.setVertexColours(vertexColourMap);

		// compute metrics
		edgeModifier = new EdgeModifier(mimicGraph, metrics);
		mErrScoreCalculator = new ErrorScoreCalculator(colouredGraphs, valuesCarrier);
	}

	public Map<BitSet, IntSet> getColourVertexIds() {
		return colourVertexIds;
	}
	
	public Map<BitSet, IntSet> getColourEdgeIds() {
		return colourEdgeIds;
	}

	public ColouredGraph getMimicGraph() {
		return mimicGraph;
	}

	public void applyVertexDistribution(ObjectDistribution<BitSet> vertexColourDistribution, int noVerts) {
		Map<Integer, BitSet> vertexColourMap = assignColours(vertexColourDistribution, noVerts);
		this.colourVertexIds = MapUtil.groupMapByValue(vertexColourMap);
		this.vertexColourMap = vertexColourMap;
	}

	public void applyEdgeDistribution(ObjectDistribution<BitSet> edgeColourDistribution, int noVerts) {
		Map<Integer, BitSet> edgeColourMap = assignColours(edgeColourDistribution, noVerts);
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
		Random randomGen = new Random(seed);
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

	/**
	 * Prints the results to file
	 * 
	 * @param args
	 * @param startingTime
	 * @param savedFile
	 * @param seed
	 */
	public void printResult(Map<String, String> args, double startingTime, String savedFile, long seed) {
		try (BufferedWriter fWriter = new BufferedWriter(new FileWriter("LemmingEx.result", true));) {
			LOGGER.info("Output results to file!");

			// number of input graphs
			fWriter.write("#----------------------------------------------------------------------#\n");
			fWriter.write("# Graph Generation: " + LocalDateTime.now().toString() + ".\n");
			fWriter.write("# Total number of input graphs: " + mErrScoreCalculator.getNumberOfGraphs() + ".\n");
			fWriter.write("# Generate a mimic graph of " + edgeModifier.getGraph().getVertices().size()
					+ " vertices and " + edgeModifier.getGraph().getEdges().size() + " edges.\n");
			fWriter.write("# Saved file: " + savedFile + ".\n");
			fWriter.write("# Seed: " + seed + "\n");
			if (args != null && args.size() > 0) {
				// dataset
				if (args.containsKey("-ds")) {
					fWriter.write("# Input dataset: " + args.get("-ds") + ".\n");
				} else {
					fWriter.write("# Default input dataset: Sematic Web Dog Food.\n");
				}
				fWriter.write("# Generation approach: Baseline.\n");
			}

			fWriter.write("#----------------------------------------------------------------------#\n");

			Map<String, String> mapGraphName = new HashMap<String, String>();

			// metric values of all graphs
			fWriter.write("\n");
			fWriter.write("- Metric Values\n");
			Map<String, Map<String, Double>> mapInputGraphMetricValues = mErrScoreCalculator
					.getMapMetricValuesOfInputGraphs();
			ObjectDoubleOpenHashMap<String> mOrigMetricValuesOfMimicGrpah = edgeModifier.getOriginalMetricValues();
			ObjectDoubleOpenHashMap<String> mOptimizedMetricValues = edgeModifier.getOptimizedMetricValues();
			Object[] arrMetricNames = mOrigMetricValuesOfMimicGrpah.keys;
			for (int i = 0; i < arrMetricNames.length; i++) {
				if (mOrigMetricValuesOfMimicGrpah.allocated[i]) {
					String metricName = (String) arrMetricNames[i];
					fWriter.write("-- Metric: " + metricName + ":\n");

					int idxGraph = 1;
					Set<String> setKeyGraphs = mapInputGraphMetricValues.keySet();
					for (String keyGraph : setKeyGraphs) {
						// generate name for each graph
						String graphName = "Graph " + idxGraph;
						mapGraphName.put(keyGraph, graphName);

						Map<String, Double> mapInputGraphVal = mapInputGraphMetricValues.get(keyGraph);
						double inputGraphValue = mapInputGraphVal.containsKey(metricName)
								? mapInputGraphVal.get(metricName)
								: Double.NaN;
						fWriter.write("\t " + graphName + ": " + inputGraphValue + "\n");
						idxGraph++;
					}

					double originalVal = mOrigMetricValuesOfMimicGrpah.get(metricName);
					fWriter.write("\t The first mimic graph: " + originalVal + "\n");
					double optimizedVal = mOptimizedMetricValues.get(metricName);
					fWriter.write("\t The opimized mimic graph: " + optimizedVal + "\n");
				}
			}
			fWriter.write("\n");
			fWriter.write("- Constant expressions\n");
			// constant expressions and their values for each graph
			Map<Expression, Map<String, Double>> mapConstantValues = mErrScoreCalculator.getMapConstantExpressions();
			Set<Expression> setExprs = mapConstantValues.keySet();
			for (Expression expr : setExprs) {
				fWriter.write("-- Expression: " + expr.toString() + ":\n");

				Map<String, Double> mapGraphAndConstantValues = mapConstantValues.get(expr);
				Set<String> setKeyGraphs = mapGraphAndConstantValues.keySet();
				for (String keyGraph : setKeyGraphs) {
					double constVal = mapGraphAndConstantValues.get(keyGraph);
					fWriter.write("\t " + mapGraphName.get(keyGraph) + ": " + constVal + "\n");
				}

				double origConstantVal = expr.getValue(mOrigMetricValuesOfMimicGrpah);
				fWriter.write("\t The first mimic graph: " + origConstantVal + "\n");
				double optimizedConstantVal = expr.getValue(mOptimizedMetricValues);
				fWriter.write("\t The opimized mimic graph: " + optimizedConstantVal + "\n");
			}

			fWriter.write("\n");
			fWriter.write("- Sum error score\n");
			fWriter.write("-- Average sum error score: " + mErrScoreCalculator.getAverageErrorScore() + ":\n");
			fWriter.write("-- Min sum error score: " + mErrScoreCalculator.getMinErrorScore() + ":\n");
			fWriter.write("-- Max sum error score: " + mErrScoreCalculator.getMaxErrorScore() + ":\n");
			// constant expressions and their values for each graphs
			Map<String, Double> mapSumErrorScores = mErrScoreCalculator.getMapSumErrorScore();
			Set<String> setKeyGraphs = mapSumErrorScores.keySet();
			for (String keyGraph : setKeyGraphs) {
				double errorScore = mapSumErrorScores.get(keyGraph);
				fWriter.write("\t " + mapGraphName.get(keyGraph) + ": " + errorScore + "\n");

			}
			fWriter.write("\t Mimic graph: "
					+ mErrScoreCalculator.computeErrorScore(edgeModifier.getOriginalMetricValues()) + "\n");
			fWriter.write("\n\n\n");

		} catch (Exception ex) {
			LOGGER.warn("Cannot output results to file! Please check: " + ex.getMessage());
		}
	}

}