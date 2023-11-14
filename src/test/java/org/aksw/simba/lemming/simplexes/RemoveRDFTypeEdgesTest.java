package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;

import it.unimi.dsi.fastutil.ints.IntSet;

public class RemoveRDFTypeEdgesTest {
	
	@Test
	public void estimateEdgesTest() {
		
		// TODO: Read only specific graph or all graphs and include assertions to validate the result of removed edges.
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		SemanticWebDogFoodDataset mDatasetManager = new SemanticWebDogFoodDataset();
		ColouredGraph graphs[] = new ColouredGraph[20];
		graphs= mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		ColouredGraph graphTemp = graphs[1];
		
		IntSet allEdges = graphTemp.getEdges();
		
		System.out.println("Graph details (Before removing type edges): ");
		System.out.println("Number of edges: " + allEdges.size());
		System.out.println("Number of vertices: " + graphTemp.getVertices().size());
		
		//*************** Logic to remove rdftype edge *********************************//
		BitSet rdfTypePropertyColour = graphTemp.getRDFTypePropertyColour();
		int countOfTypeEdges = 0;
		
		//Iterate over all edges
		for(int edgeId:allEdges) {
			if (graphTemp.getEdgeColour(edgeId).equals(rdfTypePropertyColour)) {
				graphTemp.removeEdge(edgeId);
				countOfTypeEdges++;
			}
		}
		
		System.out.println("Number Of RDF type edges removed: " + countOfTypeEdges);
		
		System.out.println("Graph details (After removing type edges): ");
		System.out.println("Number of edges: " + graphTemp.getEdges().size());
		System.out.println("Number of vertices: " + graphTemp.getVertices().size());
		
	}

}
