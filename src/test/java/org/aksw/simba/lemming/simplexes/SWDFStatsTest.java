package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.ColouredGraph;
import org.junit.Test;

public class SWDFStatsTest {

	@Test
	public void getNumberOfVertEdgesSWDF() {
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 20;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		int graphNumber = 1;
		for (ColouredGraph grap: graphs) {
			if (grap != null) {
				System.out.println("Graph Id: " + graphNumber);
				System.out.println("Number of Vertices: " + grap.getNumberOfVertices());
				System.out.println("Number of Edges: " + grap.getNumberOfEdges());
				graphNumber++;
			}
		}
	}
}
