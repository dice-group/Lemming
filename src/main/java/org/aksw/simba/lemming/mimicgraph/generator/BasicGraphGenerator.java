package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.InMemoryPalette;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

/**
 * Class with the methods in common between all graph generators (including
 * baseline's)
 * 
 * @author Alexandra Silva
 *
 */
public class BasicGraphGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicGraphGenerator.class);

	/**
	 * draft estimation of number edges
	 * 
	 * @param origGrphs
	 */
	protected int estimateNoEdges(ColouredGraph[] origGrphs, int noVertices) {
		LOGGER.info("Estimate the number of edges in the new graph.");
		int estimatedEdges = 0;
		if (origGrphs != null && origGrphs.length > 0) {
			int iNoOfVersions = origGrphs.length;
			double noEdges = 0;
			for (ColouredGraph graph : origGrphs) {
				int iNoEdges = graph.getEdges().size();
				int iNoVertices = graph.getVertices().size();
				noEdges += iNoEdges / (iNoVertices * 1.0);
			}
			noEdges *= noVertices;
			noEdges /= iNoOfVersions;
			estimatedEdges = (int) Math.round(noEdges);
			LOGGER.warn("Estimated the number of edges in the new graph is " + estimatedEdges);
		} else {
			LOGGER.warn("The array of original graphs is empty!");
		}
		return estimatedEdges;
	}

	/**
	 * Copies the merged colour palettes of the input graphs to another given graph
	 * 
	 * @param origGraphs input graphs' {@link ColouredGraph} objects
	 * @param mimicGraph the given graph where we want the colour palette to be
	 *                   copied to
	 */
	protected void copyColourPalette(ColouredGraph[] origGraphs, ColouredGraph mimicGraph) {
		if (Constants.IS_EVALUATION_MODE) {
			ColourPalette newVertexPalette = new InMemoryPalette();
			ColourPalette newEdgePalette = new InMemoryPalette();
			ColourPalette newDTEdgePalette = new InMemoryPalette();

			// copy colour palette of all the original graphs to the new one
			for (ColouredGraph grph : origGraphs) {
				// merge vertex colours
				ColourPalette vPalette = grph.getVertexPalette();
				Map<String, BitSet> mapVertexURIsToColours = vPalette.getMapOfURIAndColour();
				fillColourToPalette(newVertexPalette, mapVertexURIsToColours);

				// merge edge colours
				ColourPalette ePalette = grph.getEdgePalette();
				Map<String, BitSet> mapEdgeURIsToColours = ePalette.getMapOfURIAndColour();
				fillColourToPalette(newEdgePalette, mapEdgeURIsToColours);

				// merge data typed edge colours
				ColourPalette dtePalette = grph.getDataTypedEdgePalette();
				Map<String, BitSet> mapDTEdgeURIsToColours = dtePalette.getMapOfURIAndColour();
				fillColourToPalette(newDTEdgePalette, mapDTEdgeURIsToColours);
			}

			mimicGraph.setVertexPalette(newVertexPalette);
			mimicGraph.setEdgePalette(newEdgePalette);
			mimicGraph.setDataTypeEdgePalette(newDTEdgePalette);
		}
	}

	/**
	 * Updates a colour palette based on another palette's URI to colour map
	 * 
	 * @param palette             the colour palette
	 * @param mapOfURIsAndColours the other palette's URI to colour map
	 */
	protected void fillColourToPalette(ColourPalette palette, Map<String, BitSet> mapOfURIsAndColours) {
		Object[] arrObjURIs = mapOfURIsAndColours.keySet().toArray();
		for (int i = 0; i < arrObjURIs.length; i++) {
			String uri = (String) arrObjURIs[i];
			BitSet colour = mapOfURIsAndColours.get(uri);
			palette.updateColour(colour, uri);
		}
	}
}
