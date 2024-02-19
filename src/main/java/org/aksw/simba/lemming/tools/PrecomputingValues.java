
package org.aksw.simba.lemming.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.refinement.CharacteristicExpressionSearcher;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.aksw.simba.lemming.algo.refinement.fitness.FitnessFunction;
import org.aksw.simba.lemming.algo.refinement.fitness.LengthAwareMinSquaredError;
import org.aksw.simba.lemming.algo.refinement.fitness.ReferenceGraphBasedFitnessDecorator;
import org.aksw.simba.lemming.algo.refinement.operator.RefinementOperator;
import org.aksw.simba.lemming.algo.refinement.redberry.RedberryBasedFactory;
import org.aksw.simba.lemming.configuration.Validator;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.beust.jcommander.JCommander;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import grph.Grph;
import grph.algo.topology.ClassicalGraphs;
import grph.algo.topology.StarTopologyGenerator;
import grph.in_memory.InMemoryGrph;

/**
 * 
 * Pre-computes the metrics and constant expressions of the input graphs. Also
 * saves the values to file to be used by the GraphGenerationTest class.
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.aksw.simba.lemming")
public class PrecomputingValues {

	/** Logging object */
	private static final Logger LOGGER = LoggerFactory.getLogger(PrecomputingValues.class);

	public static void main(String[] args) {

		// Start spring
		ConfigurableApplicationContext application = new SpringApplicationBuilder(PrecomputingValues.class)
				.web(WebApplicationType.NONE).run(args);
		LOGGER.info("Start precomputing metric and constant expressions!");

		// Parse input arguments
		PrecomputingArgs pArgs = new PrecomputingArgs();
		JCommander.newBuilder().addObject(pArgs).build().parse(args);

		// Validate dataset
		Validator val = (Validator) application.getBean(Validator.class);
		val.isDatasetAllowed(pArgs.dataset);

		// Read models from file and create corresponding coloured graphs
		IDatasetManager mDatasetManager = (IDatasetManager) application.getBean(pArgs.dataset);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles();

		// Compute metrics for each graph
		LOGGER.info("Compute metric values for graph ......");
		ConstantValueStorage valueCarrier = application.getBean(ConstantValueStorage.class,
				mDatasetManager.getDatasetPath());
		ObjectDoubleOpenHashMap<String> graphVectors[] = valueCarrier.computeMetrics(graphs, pArgs.recalculateMetrics);

		// Compute constant expressions with metrics from above
		LOGGER.info("Compute constant expressions ......");
		List<SingleValueMetric> metrics = valueCarrier.getMetrics();
		FitnessFunction fitnessFunc = new LengthAwareMinSquaredError();
		fitnessFunc = new ReferenceGraphBasedFitnessDecorator(fitnessFunc,
				createReferenceGraphVectors(graphs, metrics));

		RefinementOperator refinementOperator = (RefinementOperator) application.getBean("refOperator", metrics);
		CharacteristicExpressionSearcher searcher = new CharacteristicExpressionSearcher(metrics, refinementOperator,
				new RedberryBasedFactory(), fitnessFunc, pArgs.minFitness, pArgs.maxIterations);
		searcher.setDebug(true);

		// Get best 5 invariant expressions
		SortedSet<RefinementNode> bestNodes = searcher.findExpression(graphVectors, 5);
		for (RefinementNode n : bestNodes) {
			StringBuilder builder = new StringBuilder();
			builder.append(n.getFitness());
			builder.append(" --> ");
			builder.append(n.toString());
			builder.append('\n');

			for (int i = 0; i < graphs.length; ++i) {
				ObjectDoubleOpenHashMap<String> metricValues = valueCarrier.getMetricValues(graphs[i]);
				double constValue = n.getExpression().getValue(metricValues);
				builder.append(constValue);
				builder.append('\t');
				valueCarrier.addConstantValue(n.getExpression(), graphs[i], constValue);
				builder.append('\n');
			}
			LOGGER.info(builder.toString());
		}

		// save to file
		valueCarrier.storeData();
		LOGGER.info("Precomputation is DONE");
	}

	/**
	 * create reference graph to compute constant expressions
	 * 
	 * @param graphs  input dataset graphs
	 * @param metrics list of exploited metrics
	 * 
	 * @return map of metric values of reference graphs
	 */
	@SuppressWarnings("unchecked")
	private static ObjectDoubleOpenHashMap<String>[] createReferenceGraphVectors(ColouredGraph[] graphs,
			List<SingleValueMetric> metrics) {
		Grph temp;
		int numberOfNodes, partSize;
		List<ObjectDoubleOpenHashMap<String>> vectors = new ArrayList<ObjectDoubleOpenHashMap<String>>(
				5 * graphs.length);

		/*------------------
		 * FILTER metrics
		 * costly metrics: node triangles, edge triangles, clustering coefficient
		 * naive metrics: others
		 ------------------*/
		List<SingleValueMetric> costlyMetrics = new ArrayList<SingleValueMetric>();
		List<SingleValueMetric> naiveMetrics = new ArrayList<SingleValueMetric>();
		for (SingleValueMetric metric : metrics) {
			if (metric.getName().equalsIgnoreCase("#edgetriangles")
					|| metric.getName().equalsIgnoreCase("#nodetriangles")
					|| metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
				costlyMetrics.add(metric);
				LOGGER.info("Costly metric: " + metric.getName());
			} else {
				naiveMetrics.add(metric);
				LOGGER.info("Naive metric: " + metric.getName());
			}
		}

		for (int i = 0; i < graphs.length; ++i) {
			numberOfNodes = graphs[i].getGraph().getNumberOfVertices();
			partSize = (int) Math.sqrt(numberOfNodes);

			LOGGER.info("Generating reference graphs with " + numberOfNodes + " nodes.");
			/*------------------
			 *  Star
			 ------------------*/
			StarTopologyGenerator starGenerator = new StarTopologyGenerator();
			temp = new InMemoryGrph();
			temp.addNVertices(numberOfNodes);
			starGenerator.compute(temp);
			ColouredGraph startColouredGraph = new ColouredGraph(temp, null, null);
			ObjectDoubleOpenHashMap<String> starGraphMetrics = MetricUtils.calculateGraphMetrics(startColouredGraph,
					naiveMetrics);
			for (SingleValueMetric metric : costlyMetrics) {
				if (metric.getName().equalsIgnoreCase("#edgetriangles")
						|| metric.getName().equalsIgnoreCase("#nodetriangles")
						|| metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
					starGraphMetrics.putOrAdd(metric.getName(), 0, 0);
				} else {
					double val = metric.apply(startColouredGraph);
					starGraphMetrics.putOrAdd(metric.getName(), val, val);
				}
			}
			vectors.add(starGraphMetrics);
			temp = null;

			/*------------------
			 *  Grid
			 ------------------*/

			ColouredGraph gridColouredGraph = new ColouredGraph(ClassicalGraphs.grid(partSize, partSize), null, null);
			ObjectDoubleOpenHashMap<String> gridGraphMetrics = MetricUtils.calculateGraphMetrics(gridColouredGraph,
					naiveMetrics);

			for (SingleValueMetric metric : costlyMetrics) {
				if (metric.getName().equalsIgnoreCase("#edgetriangles")
						|| metric.getName().equalsIgnoreCase("#nodetriangles")
						|| metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
					gridGraphMetrics.putOrAdd(metric.getName(), 0, 0);
				} else {
					double val = metric.apply(gridColouredGraph);
					gridGraphMetrics.putOrAdd(metric.getName(), val, val);
				}
			}

			vectors.add(gridGraphMetrics);

			/*------------------
			 *  Ring
			 ------------------*/
			ColouredGraph ringColouredGraph = new ColouredGraph(ClassicalGraphs.cycle(numberOfNodes), null, null);
			ObjectDoubleOpenHashMap<String> ringGraphMetrics = MetricUtils.calculateGraphMetrics(ringColouredGraph,
					naiveMetrics);

			for (SingleValueMetric metric : costlyMetrics) {
				if (metric.getName().equalsIgnoreCase("#edgetriangles")
						|| metric.getName().equalsIgnoreCase("#nodetriangles")
						|| metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
					if (numberOfNodes == 3) {
						ringGraphMetrics.putOrAdd(metric.getName(), 1, 1);
					} else {
						ringGraphMetrics.putOrAdd(metric.getName(), 0, 0);
					}
				} else {
					double val = metric.apply(gridColouredGraph);
					ringGraphMetrics.putOrAdd(metric.getName(), val, val);
				}
			}

			vectors.add(ringGraphMetrics);

			/*------------------
			 *  Clique
			 ------------------*/
			ColouredGraph cliqueColouredGraph = new ColouredGraph(ClassicalGraphs.completeGraph(partSize), null, null);
			ObjectDoubleOpenHashMap<String> cliqueGraphMetrics = MetricUtils.calculateGraphMetrics(cliqueColouredGraph,
					naiveMetrics);

			for (SingleValueMetric metric : costlyMetrics) {
				if (metric.getName().equalsIgnoreCase("#edgetriangles")
						|| metric.getName().equalsIgnoreCase("#nodetriangles")
						|| metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {

					if (partSize < 3) {
						cliqueGraphMetrics.putOrAdd(metric.getName(), 0, 0);
					} else {
						int noOfValues = partSize * (partSize - 1) * (partSize - 2) / 6;
						cliqueGraphMetrics.putOrAdd(metric.getName(), noOfValues, noOfValues);
					}
				} else {
					double val = metric.apply(gridColouredGraph);
					cliqueGraphMetrics.putOrAdd(metric.getName(), val, val);
				}
			}

			vectors.add(cliqueGraphMetrics);

			/*------------------
			 *  Bipartite
			 ------------------*/
			// partSize = numberOfNodes / 2;
			partSize = numberOfNodes > 100000 ? numberOfNodes / 128 : numberOfNodes / 8;

			ColouredGraph bipartiteColouredGraph = new ColouredGraph(
					ClassicalGraphs.completeBipartiteGraph(partSize, partSize), null, null);
			ObjectDoubleOpenHashMap<String> bipartiteGraphMetrics = MetricUtils
					.calculateGraphMetrics(bipartiteColouredGraph, naiveMetrics);

			for (SingleValueMetric metric : costlyMetrics) {
				if (metric.getName().equalsIgnoreCase("#edgetriangles")
						|| metric.getName().equalsIgnoreCase("#nodetriangles")
						|| metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
					bipartiteGraphMetrics.putOrAdd(metric.getName(), 0, 0);
				} else {
					double val = metric.apply(gridColouredGraph);
					bipartiteGraphMetrics.putOrAdd(metric.getName(), val, val);
				}
			}
			vectors.add(bipartiteGraphMetrics);
		}
		return vectors.toArray(new ObjectDoubleOpenHashMap[vectors.size()]);
	}

}
