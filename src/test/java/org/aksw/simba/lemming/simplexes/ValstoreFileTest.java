package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.LinkedGeoDataset;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.ErrorScoreCalculator;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.junit.Test;

public class ValstoreFileTest {

	//@Test
	public void readLGD() {
		IDatasetManager mDatasetManager = new LinkedGeoDataset();
		String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";
		String datasetPath = LINKED_GEO_DATASET_FOLDER_PATH;
		ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);
		ErrorScoreCalculator mErrScoreCalculator;
		
		ColouredGraph graphs[] = new ColouredGraph[20];
		graphs = mDatasetManager.readGraphsFromFiles(datasetPath);
		
		mErrScoreCalculator = new ErrorScoreCalculator(graphs, valuesCarrier);
	}
	
	@Test
	public void readSWDF() {
		IDatasetManager mDatasetManager = new SemanticWebDogFoodDataset();
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		String datasetPath = SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH;
		ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);
		ErrorScoreCalculator mErrScoreCalculator;
		
		ColouredGraph graphs[] = new ColouredGraph[20];
		graphs = mDatasetManager.readGraphsFromFiles(datasetPath);
		
		mErrScoreCalculator = new ErrorScoreCalculator(graphs, valuesCarrier);
	}
}
