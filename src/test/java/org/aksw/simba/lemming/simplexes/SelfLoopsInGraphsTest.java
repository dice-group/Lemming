package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Test;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

public class SelfLoopsInGraphsTest {
	
	//@Test
	public void test1() {
		
		int iNoOfVersions = 1;
		
		ColouredGraph[] origGrphs = new ColouredGraph[iNoOfVersions];
		ColouredGraph graphInput = IOHelper.readGraphFromResource("isolatedtri_selfloop.n3", "N3");
		origGrphs[0] = graphInput;
		
		SelfLoopsInGraphs selfLoopsObj = new SelfLoopsInGraphs();
		selfLoopsObj.analyze(origGrphs);
		
		ObjectObjectOpenHashMap<Integer,IntSet> getmGraphIdEdgeIdsForSelfLoop = selfLoopsObj.getmGraphIdEdgeIdsForSelfLoop();
		System.out.println(getmGraphIdEdgeIdsForSelfLoop);
		
		System.out.println(getmGraphIdEdgeIdsForSelfLoop.get(1).size());
		
	}
	
	@Test
	public void test2() {
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 5;
		
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
		
		SelfLoopsInGraphs selfLoopsObj = new SelfLoopsInGraphs();
		selfLoopsObj.analyze(graphs);
		
		ObjectObjectOpenHashMap<Integer,IntSet> getmGraphIdEdgeIdsForSelfLoop = selfLoopsObj.getmGraphIdEdgeIdsForSelfLoop();
		System.out.println(getmGraphIdEdgeIdsForSelfLoop);
		
		Object[] keys = getmGraphIdEdgeIdsForSelfLoop.keys;
		for (int i=0; i < keys.length; i++) {
			if (getmGraphIdEdgeIdsForSelfLoop.allocated[i]) {
				Integer integer = (Integer) keys[i];
				System.out.println(getmGraphIdEdgeIdsForSelfLoop.get(integer).size());
			}
		}
		
	}

}
