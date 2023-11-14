package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.mimicgraph.constraints.ColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.ColourMappingRulesSimplexes;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

public class ColourMapperSimplexesTest {
	
	private NodeIteratorMetric metric;
	
	@Test
	public void checkColorMapper() {
		
		//TODO: Create a simple graph and analyze on it? May be not required since Colormapper was already implemented earlier
		
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		int numOfGraphsToAnalyze = 1;
		
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(numOfGraphsToAnalyze);
		ColouredGraph graphs[] = mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		removeTypeEdgesFromGraphs(graphs);
		
		// call metric
		callMetricToGetTriangleInformation(graphs);
		
		// get edge ids for triangle
		ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsTriangle;
		mGraphsEdgesIdsTriangle = metric.getmGraphsEdgesIdsTriangle();
		
		// Define color mapper
		IColourMappingRules mColourMapperTriangles;
		mColourMapperTriangles = new ColourMappingRulesSimplexes(mGraphsEdgesIdsTriangle);
		
		mColourMapperTriangles.analyzeRules(graphs);
		
		
		// Earlier Color mapper, check lengths of map in debug mode
		IColourMappingRules mColourMapper = new ColourMappingRules();
		mColourMapper.analyzeRules(graphs);
		
		System.out.println();
		
	}

	private void callMetricToGetTriangleInformation(ColouredGraph[] origGrphs) {
		// Initialize metric, which will collect triangles found in Input graphs.
		metric = new NodeIteratorMetric();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
	}
	
	private void removeTypeEdgesFromGraphs(ColouredGraph[] origGrphs) {
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				
				// get rdf type edge
				BitSet rdfTypePropertyColour = graph.getRDFTypePropertyColour();
				
				// get all edges
				IntSet allEdges = graph.getEdges();
		
				//Iterate over all edges
				for(int edgeId:allEdges) {
					// remove if rdf type edge
					if (graph.getEdgeColour(edgeId).equals(rdfTypePropertyColour)) {
						graph.removeEdge(edgeId);
					}
				}
			}
		}
	}
	
}
