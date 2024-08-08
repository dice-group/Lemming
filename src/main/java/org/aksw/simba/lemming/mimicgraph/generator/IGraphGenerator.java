package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.aksw.simba.lemming.util.Constants;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

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
	
	/**
	 * 
	 * @param tailId
	 * @param headId
	 * @param eColo
	 * @param graphInitializer
	 * @return
	 */
	default public boolean connectableVertices(int tailId, int headId, BitSet eColo, GraphInitializer graphInitializer) {
		Map<Integer, BitSet> mReversedMapClassVertices = graphInitializer.getmReversedMapClassVertices();
		Map<BitSet, Map<Integer, IntSet>> mMapEdgeColoursToConnectedVertices = graphInitializer
				.getmMapEdgeColoursToConnectedVertices();

		if (mReversedMapClassVertices.containsKey(headId)) {
			return false;
		}
		boolean canConnect = false;

		Map<Integer, IntSet> mapTailToHeads = mMapEdgeColoursToConnectedVertices.get(eColo);
		if (mapTailToHeads == null) {
			mapTailToHeads = new HashMap<Integer, IntSet>();
			mMapEdgeColoursToConnectedVertices.put(eColo, mapTailToHeads);
		}

		IntSet setOfHeads = mapTailToHeads.get(tailId);
		if (setOfHeads == null) {
			setOfHeads = new DefaultIntSet(Constants.DEFAULT_SIZE);
			mapTailToHeads.put(tailId, setOfHeads);
		}

		if (!setOfHeads.contains(headId)) {
			setOfHeads.add(headId);
			canConnect = true;
		}

		return canConnect;
	}


}
