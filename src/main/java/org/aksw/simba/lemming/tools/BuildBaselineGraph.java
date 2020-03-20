package org.aksw.simba.lemming.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
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
import org.aksw.simba.lemming.mimicgraph.generator.BaselineGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import grph.Grph.DIRECTION;

public class BuildBaselineGraph {
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildBaselineGraph.class);
	private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
	private static final String PERSON_GRAPH = "PersonGraph/";
	private static final String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";

	public static void main(String[] args) {
		IDatasetManager mDatasetManager;
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);
		Map<String, String> mapArgs = parseArguments(args);
		String dataset = mapArgs.get("-ds");
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
		} else {
			LOGGER.error("Got an unknown dataset name: \"{}\". Aborting", dataset);
			return;
		}
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

		int noVertices = 0;
		String strNoOfVertices = mapArgs.get("-nv");
		if (strNoOfVertices != null) {
			noVertices = Integer.parseInt(strNoOfVertices);
		}

		long seed = System.currentTimeMillis();
		String seedString = mapArgs.get("-s");
		if (seedString != null) {
			seed = Long.parseLong(seedString);
		}

		ColouredGraph[] graphs = mDatasetManager.readGraphsFromFiles(datasetPath);
		double startTime = System.currentTimeMillis();
		BaselineGenerator mGrphGenerator = new BaselineGenerator(noVertices, graphs, seed);
		double duration = System.currentTimeMillis() - startTime;
		LOGGER.info("Finished graph generation process in " + duration + " ms");

		LOGGER.info("Lexicalize the mimic graph ...");
		GraphLexicalization graphLexicalization = new GraphLexicalization(graphs);

		mDatasetManager.writeGraphsToFile(graphLexicalization.lexicalizeGraph(mGrphGenerator.getMimicGraph(),
				mGrphGenerator.getColourVertexIds()));

		// calculates the 
		EdgeModifier edgeModifier = new EdgeModifier(mGrphGenerator.getMimicGraph(), metrics);

		LOGGER.info("Application finished!");
	}

	private static Map<String, String> parseArguments(String[] args) {
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
					// seed
					else if (param.equalsIgnoreCase("-s")) {
						mapArgs.put("-s", value);
					}
				}
			}
		}
		return mapArgs;
	}

}
