package org.aksw.simba.lemming.tools;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.configuration.Validator;
import org.aksw.simba.lemming.creation.GraphInitializer;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.SimplexGraphInitializer;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.generator.GraphOptimization;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexClass;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexProperty;
import org.aksw.simba.lemming.simplexes.generator.IGraphGenerator;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.dice_research.ldcbench.generate.SequentialSeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.beust.jcommander.JCommander;

/**
 * 
 * Generates synthetic graphs from the input graphs. Requires a store produced
 * by PrecomputingValues.java.
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.aksw.simba.lemming")
public class GraphGenerationTest {

	/** Logging object */
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationTest.class);

	public static void main(String[] args) {

		// Start spring
		ConfigurableApplicationContext application = new SpringApplicationBuilder(GraphGenerationTest.class)
				.web(WebApplicationType.NONE).run(args);

		// Parse arguments
		GraphGenerationArgs pArgs = new GraphGenerationArgs();
		JCommander.newBuilder().addObject(pArgs).build().parse(args);

		// Validate dataset
		Validator val = (Validator) application.getBean(Validator.class);
		val.isDatasetAllowed(pArgs.dataset);
		pArgs.noThreads = val.validateThreads(pArgs.noThreads); // TODO
		SeedGenerator seedGenerator = new SequentialSeedGenerator(pArgs.seed, 0, 5000);

		// Load RDF graphs into ColouredGraph models
		LOGGER.info("Loading the input graphs...");
		IDatasetManager mDatasetManager = (IDatasetManager) application.getBean(pArgs.dataset);
		ColouredGraph[] graphs = mDatasetManager.readGraphsFromFiles();

		// Load and verify metric values and constant expressions
		ConstantValueStorage valuesCarrier = application.getBean(ConstantValueStorage.class,
				mDatasetManager.getDatasetPath());
		valuesCarrier.getMetricsOfExpressions();
		valuesCarrier.isComputableMetrics();

		// Generation of a draft graph or loads it from file
		long startTime = System.currentTimeMillis();
		LOGGER.info("Generating the mimic graph...");
		IGraphGenerator graphGenerator;
		GraphInitializer initializer = (GraphInitializer) application.getBean(pArgs.mode.toLowerCase(), seedGenerator);
		ColouredGraph mimicGraph = initializer.initialize(graphs, pArgs.noVertices, pArgs.noThreads);
		if (pArgs.mode.toLowerCase().equals("binary")) { // FIXME
			IClassSelector classSelector = (IClassSelector) application.getBean(pArgs.classSelector, initializer);
			IVertexSelector vertexSelector = (IVertexSelector) application.getBean(pArgs.vertexSelector, initializer);
			graphGenerator = (IGraphGenerator) application.getBean(pArgs.mode, initializer, classSelector,
					vertexSelector);
		} else {
			ISimplexClass simplexClass = (ISimplexClass) application.getBean(pArgs.simplexClass, initializer);
			ISimplexProperty simplexProperty = (ISimplexProperty) application.getBean(pArgs.simplexProperty,
					initializer);
			graphGenerator = (IGraphGenerator) application.getBean(pArgs.mode, initializer, simplexClass,
					simplexProperty);
		}
		graphGenerator.initializeMimicGraph(mimicGraph, pArgs.noThreads);
//		graphGenerator.loadOrGenerateGraph(mDatasetManager, pArgs.loadMimicGraph); TODO

		// finish initial mimic graph and save it for comparison
		LOGGER.info("Saving the initial mimic graph...");
		GraphLexicalization lexicalizer = new GraphLexicalization(graphs);
		String initialFile = graphGenerator.finishSaveMimicGraph(mimicGraph, valuesCarrier, lexicalizer, initializer,
				mDatasetManager);

		// Optimization with constant expressions
		LOGGER.info("Optimizing the mimic graph ...");
		List<SingleValueMetric> metrics = valuesCarrier.getMetrics();
		GraphOptimization grphOptimizer = new GraphOptimization(graphs, mimicGraph, graphGenerator, metrics,
				valuesCarrier, seedGenerator, pArgs.noOptimizationSteps);
		ColouredGraph refinedGraph = grphOptimizer.refineGraph(pArgs.noThreads);

		// output results to file "LemmingEx.result"
		// before we connect the RDF.type edges
		String savedFile = mDatasetManager.getSavedFileName("results");
		grphOptimizer.printResult(pArgs.getArguments(), startTime, savedFile, initialFile, pArgs.seed);

		// Lexicalization with word2vec
		LOGGER.info("Lexicalize the mimic graph ...");
		lexicalizer.connectVerticesWithRDFTypeEdges(refinedGraph, initializer);
		lexicalizer.lexicalizeGraph(refinedGraph, initializer.getmMapColourToVertexIDs());
		mDatasetManager.writeGraphsToFile(refinedGraph, savedFile);

	}
}
