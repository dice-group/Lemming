package org.aksw.simba.lemming.tools;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.datasets.AbstractDatasetManager;
import org.aksw.simba.lemming.creation.datasets.IDatasetManager;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.binary.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.generator.factory.GraphGeneratorFactoryRegistry;
import org.aksw.simba.lemming.mimicgraph.generator.factory.IGraphGeneratorFactory;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.aksw.simba.lemming.tools.results.Result;
import org.aksw.simba.lemming.util.MetricTester;
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
 * This class is meant to run Lemming on a single-graph instance.
 * It doesn't require the invariant expressions store and it foregoes the
 * optimization stage.
 * 
 * @author Ana Silva
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "org.aksw.simba.lemming")
public class SingleGraphGeneration {

	/** Logging object */
	private static final Logger LOGGER = LoggerFactory.getLogger(SingleGraphGeneration.class);

	public static void main(String[] args) {
		// Start spring
		ConfigurableApplicationContext application = new SpringApplicationBuilder(GraphGenerationTest.class)
				.web(WebApplicationType.NONE).run(args);

		// Parse arguments
		GraphGenerationArgs pArgs = new GraphGenerationArgs();
		JCommander.newBuilder().addObject(pArgs).build().parse(args);
		SeedGenerator seedGenerator = new SequentialSeedGenerator(pArgs.seed, 0, 5000);

		// Load RDF graphs into ColouredGraph model
		IDatasetManager mDatasetManager = new AbstractDatasetManager(pArgs.dataset) {
			@Override
			public String getDatasetPath() {
				return pArgs.datasetPath;
			}
		};
		ColouredGraph[] graph = { mDatasetManager.readGraphsFromFolder(pArgs.datasetPath) };
		
		// Generation of a draft graph or loads it from file
		long startTime = System.currentTimeMillis();
		LOGGER.info("Generating the mimic graph...");
		GraphInitializer initializer = (GraphInitializer) application.getBean(pArgs.mode.toLowerCase(), seedGenerator);
		ColouredGraph mimicGraph = initializer.initialize(graph, pArgs.noVertices, pArgs.noThreads);
		IGraphGeneratorFactory factory = GraphGeneratorFactoryRegistry.getFactory(pArgs.mode);
	    IGraphGenerator graphGenerator = factory.createGraphGenerator(initializer, application, pArgs);
		graphGenerator.initializeMimicGraph(mimicGraph, pArgs.noThreads);
		long elapsedTime = (System.currentTimeMillis() - startTime)/1000;
		LOGGER.info("Graph generation took {} seconds", elapsedTime);

		// Compute metrics of generated graph
		List<SingleValueMetric> metrics = (List<SingleValueMetric>) application.getBean("metrics");
		List<UpdatableMetricResult> results = MetricTester.getMetricInformation(metrics, mimicGraph);
		LOGGER.info("Results:");
		for(UpdatableMetricResult result: results) {
			LOGGER.info(result.getMetricName()+"\t"+result.getResult());
		}
		
		// Lexicalize graph, add RDF type triples, and save the graph
		LOGGER.info("Lexicalize the mimic graph ...");
		GraphLexicalization lexicalizer = new GraphLexicalization(graph);
		String savedFile = mDatasetManager.getSavedFileName("single");
		lexicalizer.connectVerticesWithRDFTypeEdges(mimicGraph, initializer);
		lexicalizer.lexicalizeGraph(mimicGraph, initializer.getmMapColourToVertexIDs());
		mDatasetManager.writeGraphsToFile(mimicGraph, savedFile);		
		LOGGER.info("Graph generation took {} seconds", elapsedTime);
		String resultPathConfig = application.getBean("resultFile", String.class);
		Result result = new Result(pArgs, savedFile, elapsedTime, results);
		result.saveResults(resultPathConfig);
	}

}
