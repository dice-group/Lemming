package org.aksw.simba.lemming.simplexes.generator;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphInitializer;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;

/**
 * Interface for the different Graph Generator procedures.
 * 
 */
public interface IGraphGenerator {

	/**
	 * Generates the synthetic graph
	 * 
	 * @param mimicGraph  Initialized mimic graph
	 * @param noOfThreads Number of threads to be used in the process
	 */
	public void initializeMimicGraph(ColouredGraph mimicGraph, int noOfThreads);

	/**
	 * Adds RDF:type edges to the mimic graph, lexicalizes the literals and saves it
	 * to file.
	 * 
	 * @param mimicGraph
	 * @param valuesCarrier
	 * @param graphLexicalization
	 * @param graphInitializer
	 * @param datasetManager
	 * @return Final location of the saved mimicked file
	 */
	default public String finishSaveMimicGraph(ColouredGraph mimicGraph, ConstantValueStorage valuesCarrier,
			GraphLexicalization graphLexicalization, GraphInitializer graphInitializer,
			IDatasetManager datasetManager) {
		ColouredGraph initial = mimicGraph.clone();
		graphLexicalization.connectVerticesWithRDFTypeEdges(initial, graphInitializer);
		graphLexicalization.lexicalizeGraph(initial, graphInitializer.getmMapColourToVertexIDs());
		String initialFile = datasetManager.getSavedFileName("initial");
		datasetManager.writeGraphsToFile(initial, initialFile);
		return initialFile;

	}

	/**
	 * 
	 * @return
	 */
	public TripleBaseSingleID getProposedTriple();

}
