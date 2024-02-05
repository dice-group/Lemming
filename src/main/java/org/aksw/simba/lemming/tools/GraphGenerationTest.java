package org.aksw.simba.lemming.tools;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.AbstractDatasetManager;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.mimicgraph.generator.BiasedClassBiasedInstance;
import org.aksw.simba.lemming.mimicgraph.generator.BiasedClassSelection;
import org.aksw.simba.lemming.mimicgraph.generator.ClusteredClassBiasedInstance;
import org.aksw.simba.lemming.mimicgraph.generator.ClusteredClassSelection;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.generator.GraphOptimization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.mimicgraph.generator.UniformClassBiasedInstance;
import org.aksw.simba.lemming.mimicgraph.generator.UniformClassSelection;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.beust.jcommander.JCommander;

import jakarta.annotation.Resource;

@SpringBootApplication
public class GraphGenerationTest {

	// Logging object
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationTest.class);

	// Load metrics from configuration
//	@Resource(name="metrics")
	private static List<SingleValueMetric> metrics;

	public static void main(String[] args) {

		// Start spring
		ConfigurableApplicationContext application = new SpringApplicationBuilder(GraphGenerationTest.class)
				.web(WebApplicationType.NONE).run(args);

		// Parse arguments
		GraphGenerationArgs pArgs = new GraphGenerationArgs();
		JCommander.newBuilder().addObject(pArgs).build().parse(args);

		// Load RDF graphs into ColouredGraph models
		IDatasetManager mDatasetManager = (IDatasetManager) application.getBean(pArgs.dataset);
		ColouredGraph graphs[] = new ColouredGraph[20];
		graphs = mDatasetManager.readGraphsFromFiles();

		// Load and verify metric values and constant expressions
		ConstantValueStorage valuesCarrier = new ConstantValueStorage(mDatasetManager.getDatasetPath());
		metrics = valuesCarrier.getMetricsOfExpressions(metrics);
		valuesCarrier.isComputableMetrics();

		/*---------------------------------------------------
		Generation for a draft graph
		----------------------------------------------------*/
		// TODO resume from here
		// define generator
		String typeGenerator = pArgs.typeGenerator;
		int iNumberOfThreads = pArgs.noThreads;
		int mNumberOfDesiredVertices = pArgs.noVertices;
		IGraphGeneration mGrphGenerator;
		mGrphGenerator = (IGraphGeneration) application.getBean(pArgs.typeGenerator);
		long seed = pArgs.seed;
		if (typeGenerator == null || typeGenerator.isEmpty() || typeGenerator.equalsIgnoreCase("UCSUIS")) {
			mGrphGenerator = new UniformClassSelection(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
		} else if (typeGenerator.equalsIgnoreCase("UCSBIS")) {
			mGrphGenerator = new UniformClassBiasedInstance(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
		} else if (typeGenerator.equalsIgnoreCase("BCSUIS")) {
			mGrphGenerator = new BiasedClassSelection(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
		} else if (typeGenerator.equalsIgnoreCase("BCSBIS")) {
			mGrphGenerator = new BiasedClassBiasedInstance(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
		} else if (typeGenerator.equalsIgnoreCase("CCSUIS")) {
			mGrphGenerator = new ClusteredClassSelection(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
		} else if (typeGenerator.equalsIgnoreCase("CCSBIS")) {
			mGrphGenerator = new ClusteredClassBiasedInstance(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
		} else {
			mGrphGenerator = new UniformClassSelection(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
		}

		double startTime = System.currentTimeMillis();
		boolean isLoaded = false;
		// if the file path exists, it will read from it otherwise, it will write on it
		if (pArgs.loadMimicGraph != null) {
			LOGGER.info("Loading previously determined Mimic Graph from file.");
			ColouredGraph colouredGraph = mDatasetManager.readIntResults(pArgs.loadMimicGraph);
			if (colouredGraph != null) {
				mGrphGenerator.setMimicGraph(colouredGraph);
				isLoaded = true;
			}
		}

		// in case the mimic graph is not loaded, regenerate it anyways
		if (isLoaded == false) {
			LOGGER.info("Generating a first version of mimic graph ...");
			// create a draft graph
			mGrphGenerator.generateGraph();
			// estimate the costed time for generation
			double duration = System.currentTimeMillis() - startTime;
			LOGGER.info("Finished graph generation process in " + duration + " ms");
			if (pArgs.loadMimicGraph == null) {
				pArgs.loadMimicGraph = "Initialized_MimicGraph.ser";
			}
			mDatasetManager.persistIntResults(mGrphGenerator.getMimicGraph(), pArgs.loadMimicGraph);
			LOGGER.info("Intermediate results saved under: " + pArgs.loadMimicGraph);

		}

		/*---------------------------------------------------
		Optimization with constant expressions
		----------------------------------------------------*/
		long secSeed = mGrphGenerator.getSeed() + 1;
		GraphOptimization grphOptimizer = new GraphOptimization(graphs, mGrphGenerator, metrics, valuesCarrier,
				secSeed);
		LOGGER.info("Optimizing the mimic graph ...");
		// TODO check if it is necessary to randomly refine graph
		grphOptimizer.setRefineGraphRandomly(false);
		grphOptimizer.setNumberOfOptimizations(pArgs.noOptimizationSteps);

		// optimize graph
		grphOptimizer.refineGraph();

		/*---------------------------------------------------
		Lexicalization with word2vec
		----------------------------------------------------*/
		LOGGER.info("Lexicalize the mimic graph ...");
		GraphLexicalization graphLexicalization = new GraphLexicalization(graphs);
		String saveFiled = mDatasetManager.writeGraphsToFile(graphLexicalization
				.lexicalizeGraph(mGrphGenerator.getMimicGraph(), mGrphGenerator.getMappingColoursAndVertices()));

		// output results to file "LemmingEx.result"
		grphOptimizer.printResult(pArgs.getArguments(), startTime, saveFiled, seed);
		LOGGER.info("Application exits!!!");
	}
}
