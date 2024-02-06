package org.aksw.simba.lemming.tools;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.configuration.Validator;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.generator.GraphOptimization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.beust.jcommander.JCommander;

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

		// Load RDF graphs into ColouredGraph models
		IDatasetManager mDatasetManager = (IDatasetManager) application.getBean(pArgs.dataset);
		ColouredGraph[] graphs = mDatasetManager.readGraphsFromFiles();

		// Load and verify metric values and constant expressions
		ConstantValueStorage valuesCarrier = application.getBean(ConstantValueStorage.class,
				mDatasetManager.getDatasetPath());
		valuesCarrier.getMetricsOfExpressions();
		valuesCarrier.isComputableMetrics();

		// Generation for a draft graph or loading from file
		long startTime = System.currentTimeMillis();
		IGraphGeneration mGrphGenerator = (IGraphGeneration) application.getBean(pArgs.typeGenerator, pArgs.noVertices,
				graphs, pArgs.noThreads, pArgs.seed);
		mGrphGenerator.loadOrGenerateGraph(mDatasetManager, pArgs.loadMimicGraph);

		/* Optimization with constant expressions */
		LOGGER.info("Optimizing the mimic graph ...");
		List<SingleValueMetric> metrics = valuesCarrier.getMetrics();
		GraphOptimization grphOptimizer = new GraphOptimization(graphs, mGrphGenerator, metrics, valuesCarrier,
				mGrphGenerator.getSeed(), pArgs.noOptimizationSteps);
		grphOptimizer.refineGraph();

		/* Lexicalization with word2vec */
		LOGGER.info("Lexicalize the mimic graph ...");
		GraphLexicalization graphLexicalization = new GraphLexicalization(graphs);
		String saveFiled = mDatasetManager.writeGraphsToFile(graphLexicalization
				.lexicalizeGraph(mGrphGenerator.getMimicGraph(), mGrphGenerator.getMappingColoursAndVertices()));

		// output results to file "LemmingEx.result"
		grphOptimizer.printResult(pArgs.getArguments(), startTime, saveFiled, pArgs.seed);
		LOGGER.info("Application exits!!!");
	}
}
