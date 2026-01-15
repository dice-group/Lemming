package org.aksw.simba.lemming.tools;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.configuration.Validator;
import org.aksw.simba.lemming.creation.datasets.IDatasetManager;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.generator.GraphOptimization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.baseline.BaselineGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.binary.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.generator.factory.GraphGeneratorFactoryRegistry;
import org.aksw.simba.lemming.mimicgraph.generator.factory.IGraphGeneratorFactory;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.dice_research.ldcbench.generate.SequentialSeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
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
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
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

		// Load and verify store
		LOGGER.info("Verifying store...");
		IDatasetManager mDatasetManager = (IDatasetManager) application.getBean(pArgs.dataset);
		ConstantValueStorage valuesCarrier = application.getBean(ConstantValueStorage.class,
				mDatasetManager.getDatasetPath());
		// only recomputing the store if the dataset is missing entirely to prevent accidental overwrites
		if (valuesCarrier.isEmpty()) {
			LOGGER.info("Current store does not have data for this dataset, computing store.");
			PrecomputingValues.main("-ds", pArgs.dataset);
			valuesCarrier = application.getBean(ConstantValueStorage.class, mDatasetManager.getDatasetPath());
		}

		// Load graphs from file
		LOGGER.info("Loading the input graphs...");
		ColouredGraph[] graphs = mDatasetManager.readGraphsFromFiles();

		// Generation of a draft graph or loads it from file
		long startTime = System.currentTimeMillis();
		LOGGER.info("Generating the mimic graph...");
		GraphInitializer initializer = (GraphInitializer) application.getBean(pArgs.mode.toLowerCase(), seedGenerator);
		ColouredGraph mimicGraph = initializer.initialize(graphs, pArgs.noVertices, pArgs.noThreads);
		IGraphGeneratorFactory factory = GraphGeneratorFactoryRegistry.getFactory(pArgs.mode);
	    IGraphGenerator graphGenerator = factory.createGraphGenerator(initializer, application, pArgs);
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

		
		if(pArgs.noOptimizationSteps==0) {
			LOGGER.info("Skipping lexicalizing a second time...");
			System.exit(0);
		}
		// Lexicalization with word2vec
		LOGGER.info("Lexicalize the mimic graph ...");
		lexicalizer.connectVerticesWithRDFTypeEdges(refinedGraph, initializer);
		lexicalizer.lexicalizeGraph(refinedGraph, initializer.getmMapColourToVertexIDs());
		mDatasetManager.writeGraphsToFile(refinedGraph, savedFile);

	}
}
