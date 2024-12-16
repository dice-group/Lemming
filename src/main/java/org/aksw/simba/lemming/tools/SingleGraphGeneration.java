package org.aksw.simba.lemming.tools;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.AbstractDatasetManager;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.generator.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.baseline.IGenerator;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexClass;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexProperty;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.aksw.simba.lemming.util.MetricTester;
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

@SpringBootApplication
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
		IGraphGenerator graphGenerator;
		GraphInitializer initializer = (GraphInitializer) application.getBean(pArgs.mode.toLowerCase(), seedGenerator);
		ColouredGraph mimicGraph = initializer.initialize(graph, pArgs.noVertices, pArgs.noThreads);

		// FIXME
		if (pArgs.mode.toLowerCase().equals("binary")) {
			IClassSelector classSelector = (IClassSelector) application.getBean(pArgs.classSelector, initializer);
			IVertexSelector vertexSelector = (IVertexSelector) application.getBean(pArgs.vertexSelector, initializer);
			graphGenerator = (IGraphGenerator) application.getBean(pArgs.mode, initializer, classSelector,
					vertexSelector);
		} else if (pArgs.mode.toLowerCase().equals("simplex")) {
			IClassSelector classSelector = (IClassSelector) application.getBean(pArgs.classSelector, initializer);
			IVertexSelector vertexSelector = (IVertexSelector) application.getBean(pArgs.vertexSelector, initializer);
			ISimplexClass simplexClass = (ISimplexClass) application.getBean(pArgs.simplexClass, initializer);
			ISimplexProperty simplexProperty = (ISimplexProperty) application.getBean(pArgs.simplexProperty,
					initializer);
			graphGenerator = (IGraphGenerator) application.getBean(pArgs.mode, initializer, simplexClass,
					simplexProperty, classSelector, vertexSelector);
		} else {
			IGenerator baseline = (IGenerator) application.getBean(pArgs.baselineModel);
			graphGenerator = (IGraphGenerator) application.getBean(pArgs.mode, initializer, baseline);
		}
		graphGenerator.initializeMimicGraph(mimicGraph, pArgs.noThreads);
		long elapsedTime = (System.currentTimeMillis() - startTime)/1000;
		

		// Compute metrics of generated graph
		List<SingleValueMetric> metrics = (List<SingleValueMetric>) application.getBean("metrics");
//		MetricTester.printMetricInformation(metrics, graph);
		MetricTester.printMetricInformation(metrics, mimicGraph);
		LOGGER.info("Lexicalize the mimic graph ...");
		GraphLexicalization lexicalizer = new GraphLexicalization(graph);
		String savedFile = mDatasetManager.getSavedFileName("single");
		lexicalizer.connectVerticesWithRDFTypeEdges(mimicGraph, initializer);
		lexicalizer.lexicalizeGraph(mimicGraph, initializer.getmMapColourToVertexIDs());
		mDatasetManager.writeGraphsToFile(mimicGraph, savedFile);
		
		LOGGER.info("Graph generation took {} seconds", elapsedTime);

	}

}
