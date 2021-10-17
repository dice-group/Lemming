package org.aksw.simba.lemming.metrics.testdummy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.creation.GeologyDataset;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.LinkedGeoDataset;
import org.aksw.simba.lemming.creation.PersonGraphDataset;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.EdgeModifier;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.ErrorScoreCalculator_new;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.ExpressionChecker;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.GetMetricsFromExpressions;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import grph.Grph.DIRECTION;

public class CharacteristicExpressionsTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CharacteristicExpressionsTest.class);

	private static final String GEOLOGY_DATASET_FOLDER_PATH = "GeologyGraphs/";
	private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
	private static final String PERSON_GRAPH = "PersonGraph/";
	private static final String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";

	// Change below as per data set
	private String dataset = "geology";

	// @Test
	public void testToEvaluateCharacteristicExpressions() {

		// Initialize dataset path
		String datasetPath = GEOLOGY_DATASET_FOLDER_PATH;

		// Initialize ConstantValueStorage class
		ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);

		// Get the constant expressions
		Set<Expression> constantExpressions = valuesCarrier.getConstantExpressions();

		// Initialize the Expression optimization class
		GetMetricsFromExpressions clasExpOpt = new GetMetricsFromExpressions();
		clasExpOpt.compute(constantExpressions);

		System.out.println("Direct Proportional metrics : " + clasExpOpt.getDirectProportionalMetricsSet());
		System.out.println("Inverse Proportional metrics : " + clasExpOpt.getInverseProportionalMetricsSet());

	}

	@Test
	public void checkMetricsFromExpressions() {

		List<SingleValueMetric> metrics = new ArrayList<>();
		metrics.add(new NodeTriangleMetric());
		metrics.add(new EdgeTriangleMetric());
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
		metrics.add(new AvgVertexDegreeMetric());
		metrics.add(new StdDevVertexDegree(DIRECTION.in));
		metrics.add(new StdDevVertexDegree(DIRECTION.out));
		metrics.add(new NumberOfEdgesMetric());
		metrics.add(new NumberOfVerticesMetric());

		int mNumberOfDesiredVerticesGeology = 1281;
		int mNumberOfDesiredVerticeslgeo = 591649;
		int mNumberOfDesiredVerticesswdf = 45420;
		int mNumberOfDesiredVerticespg = 792923;
		int iNumberOfThreads = -1;

		long seed = System.currentTimeMillis();
		LOGGER.info("Current Seed is " + seed);

		ColouredGraph graphs[] = getInputGraphs();
		IGraphGeneration mGraphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVerticesGeology, graphs,
				iNumberOfThreads, seed);
		ColouredGraph clonedGrph = mGraphGenerator.getMimicGraph().clone();

		EdgeModifier mEdgeModifier = new EdgeModifier(clonedGrph, metrics);

		// Initialize dataset path
		String datasetPath = GEOLOGY_DATASET_FOLDER_PATH;

		// Initialize ConstantValueStorage class
		ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);

		// Get the constant expressions
		Set<Expression> constantExpressions = valuesCarrier.getConstantExpressions();

		// Initialize Error Score Calculator
		ErrorScoreCalculator_new mErrScoreCalculator = new ErrorScoreCalculator_new(graphs, valuesCarrier);

		// Initialize Expression Checker
		ExpressionChecker expressionChecker = new ExpressionChecker(mErrScoreCalculator, mEdgeModifier, valuesCarrier);

		ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();
		mapMetricValues.put("#vertices", 1281.0);
		mapMetricValues.put("stdDevInDegree", 16.556235861947844);
		mapMetricValues.put("#edgetriangles", 163699.0);
		mapMetricValues.put("maxInDegree", 219.0);
		mapMetricValues.put("avgDegree", 6.82903981264637);
		mapMetricValues.put("maxOutDegree", 539.0);
		mapMetricValues.put("stdDevOutDegree", 24.148731826015553);
		mapMetricValues.put("#nodetriangles", 17163.0);
		mapMetricValues.put("#edges", 8748.0);

		expressionChecker.storeExpressions(mapMetricValues);

		expressionChecker.checkExpressions();

		System.out.println("Metric Details : ");
		System.out.println("Metric to Increase : " + expressionChecker.getMetricToIncrease()
				+ ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceIncreaseMetric());

		System.out.println("Metric to Decrease : " + expressionChecker.getMetricToDecrease()
				+ ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceDecreaseMetric());

	}

	private ColouredGraph[] getInputGraphs() {

		IDatasetManager mDatasetManager = null;

		ColouredGraph graphs[] = new ColouredGraph[20];

		// load RDF data to coloured graph

		String datasetPath = "";
		if (dataset != null && dataset.equalsIgnoreCase("pg")) {
			LOGGER.info("Loading PersonGraph...");
			mDatasetManager = new PersonGraphDataset();
			datasetPath = PERSON_GRAPH;
		} else if (dataset.equalsIgnoreCase("swdf")) {
			LOGGER.info("Loading SemanticWebDogFood...");
			mDatasetManager = new SemanticWebDogFoodDataset();
			datasetPath = SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH;
		} else if (dataset.equalsIgnoreCase("lgeo")) {
			LOGGER.info("Loading LinkedGeo...");
			mDatasetManager = new LinkedGeoDataset();
			datasetPath = LINKED_GEO_DATASET_FOLDER_PATH;
		} else if (dataset.equalsIgnoreCase("geology")) {
			LOGGER.info("Loading Geology Dataset...");
			mDatasetManager = new GeologyDataset();
			datasetPath = GEOLOGY_DATASET_FOLDER_PATH;
		} else {
			LOGGER.error("Got an unknown dataset name: \"{}\". Aborting", dataset);

		}

		graphs = mDatasetManager.readGraphsFromFiles(datasetPath);
		return graphs;
	}

}
