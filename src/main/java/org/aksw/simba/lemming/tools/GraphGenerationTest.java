package org.aksw.simba.lemming.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.creation.AbstractDatasetManager;
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
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

@SpringBootApplication
public class GraphGenerationTest {

	// Logging object
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationTest.class);
	
	// Load metrics from configuration
	@Resource(name="metrics")
	private static List<SingleValueMetric> metrics;

	public static void main(String[] args) {
		
		// Start spring
		ConfigurableApplicationContext application = new SpringApplicationBuilder(GraphGenerationTest.class)
				.web(WebApplicationType.NONE).run(args);
		
		// Parse arguments
		GraphGenerationArgs pArgs = new GraphGenerationArgs();
		JCommander.newBuilder().addObject(pArgs).build().parse(args);

		// Load RDF graphs into ColouredGraph models 
		AbstractDatasetManager mDatasetManager = (AbstractDatasetManager) application.getBean(pArgs.dataset);
		ColouredGraph graphs[] = new ColouredGraph[20];
		graphs = mDatasetManager.readGraphsFromFiles();

		// Load and verify metric values and constant expressions 
		ConstantValueStorage valuesCarrier = new ConstantValueStorage(mDatasetManager.getDataFolderPath());
		metrics = valuesCarrier.getMetricsOfExpressions(metrics);
		valuesCarrier.isComputableMetrics(metrics); 

		/*---------------------------------------------------
		Generation for a draft graph
		----------------------------------------------------*/

		// define generator
		String typeGenerator = pArgs.typeGenerator;
		int iNumberOfThreads = pArgs.noThreads;
		int mNumberOfDesiredVertices = pArgs.noVertices;
		IGraphGeneration mGrphGenerator;
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

	/**
	 * 
	 * @param args list of input arguments
	 * @return a map of key and values
	 */
	private static Map<String, String> parseArguments(String[] args) {
		/*
		 * -ds: dataset value: swdf (semanticwebdogfood), or pg (persongraph)
		 * 
		 * -nv: number of vertices
		 * 
		 * -t: methods of generator value: R: random approach, RD: random with degree
		 * approach value: D: distribution approach, DD: disitrbution and degree
		 * approach value: C: clustering approach, CD: clustering and degree approach
		 * 
		 * -r: random optimization -thrs: the number of threads by default, the
		 * application runs with a single thread
		 * 
		 * -op: (optional) number of optimization steps
		 */
		Map<String, String> mapArgs = new HashMap<String, String>();

		if (args.length != 0) {
			for (int i = 0; i < args.length; i++) {
				String param = args[i];
				if ((i + 1) < args.length) {
					String value = args[i + 1];
					// target dataset
					if (param.equalsIgnoreCase("-ds")) {
						mapArgs.put("-ds", value);
					}
					// number of vertices
					else if (param.equalsIgnoreCase("-nv")) {
						mapArgs.put("-nv", value);
					}
					// type of graph generator
					else if (param.equalsIgnoreCase("-t")) {
						mapArgs.put("-t", value);
					} else if (param.equalsIgnoreCase("-r")) {
						mapArgs.put("-r", value);
					} else if (param.equalsIgnoreCase("-thrs")) {
						mapArgs.put("-thrs", value);
					} else if (param.equalsIgnoreCase("-op")) {
						mapArgs.put("-op", value);
					} else if (param.equalsIgnoreCase("-l")) {
						mapArgs.put("-l", value);
					} else if (param.equalsIgnoreCase("-s")) {
						mapArgs.put("-s", value);
					}
				}
			}
		}
		return mapArgs;
	}

	private static void printConstantResults(List<SingleValueMetric> lstMetrics, ConstantValueStorage constStorage,
			ColouredGraph[] origGraphs) {
		BufferedWriter fWriter;
		try {
			LOGGER.warn("Output results to file!");

			fWriter = new BufferedWriter(new FileWriter("ConstantOfLatestGraph.result", true));

			for (ColouredGraph grph : origGraphs) {
				if (grph.getVertices().size() == 45387 || grph.getVertices().size() == 792921) {

					ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<String>();
					// compute list of metrics

					for (SingleValueMetric metric : lstMetrics) {
						double val = metric.apply(grph);
						mapMetricValues.putOrAdd(metric.getName(), 0, 0);
						mapMetricValues.put(metric.getName(), val);
					}

					Set<Expression> setExpressions = constStorage.getConstantExpressions();

					fWriter.write("#----------------------------------------------------------------------#\n");
					fWriter.write("# Graph " + 45387 + ".\n");
					for (Expression expr : setExpressions) {
						double constVal = expr.getValue(mapMetricValues);
						fWriter.write("\t Expr: " + expr.toString() + ":" + constVal + "\n");
					}
					fWriter.write("#----------------------------------------------------------------------#\n");
				}
			}
			// metric values of all graphs
			fWriter.close();
		} catch (Exception ex) {
			LOGGER.warn("Cannot output results to file! Please check: " + ex.getMessage());
		}
	}

	private static void printMetricResults(List<SingleValueMetric> lstMetrics, ColouredGraph[] origGraphs) {
		BufferedWriter fWriter;
		try {
			LOGGER.warn("Output results to file!");

			fWriter = new BufferedWriter(new FileWriter("MetricsOfLatestGraph.result", true));

			int iIndex = 1;

			for (ColouredGraph grph : origGraphs) {
				fWriter.write("#----------------------------------------------------------------------#\n");
				fWriter.write("# Graph " + iIndex + ".\n");
				for (SingleValueMetric metric : lstMetrics) {
					double val = metric.apply(grph);
					fWriter.write("\t Metric: " + metric.getName() + ": " + val + "\n");
				}
				iIndex++;
				fWriter.write("#----------------------------------------------------------------------#\n");
			}
			// metric values of all graphs
			fWriter.close();
		} catch (Exception ex) {
			LOGGER.warn("Cannot output results to file! Please check: " + ex.getMessage());
		}
	}
}
