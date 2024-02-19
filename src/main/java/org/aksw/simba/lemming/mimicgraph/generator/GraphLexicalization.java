package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgColouredVDistPerDTEColour;
import org.aksw.simba.lemming.mimicgraph.literals.RDFLiteralGenertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphLexicalization {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphLexicalization.class);

	private RDFLiteralGenertor mLiteralProposer;
	private AvrgColouredVDistPerDTEColour mAvrgVDistPerDREColourMetric;

	//private RDFLiteralProposerFactory mLiteralProposer2;
	
	public GraphLexicalization(ColouredGraph[] origGrphs) {
		
		// average vertex distribution per data typed property
		mAvrgVDistPerDREColourMetric = new AvrgColouredVDistPerDTEColour(origGrphs);

		// literal proposer
		mLiteralProposer = new RDFLiteralGenertor(origGrphs);
	}

	public ColouredGraph lexicalizeGraph(ColouredGraph mimicGraph, Map<BitSet, IntSet> mapVColoToVertices) {
		LOGGER.info("Start lexicalizing the mimic graph");

		/*
		 * get a list of data typed edge's colours
		 */
		Map<BitSet, ObjectDoubleOpenHashMap<BitSet>> mapVColoDistPerDTEColo = mAvrgVDistPerDREColourMetric
				.getMapAvrgColouredVDist();

		/*
		 * set of all data typed edge's colours
		 */
		Set<BitSet> setOfDTEColours = mapVColoDistPerDTEColo.keySet();

		LOGGER.info("Generate "+ setOfDTEColours.size()+ " datatype edge colours (datatype properties)");
		int iCounter = 0;
		/*
		 * accordingly to each data typed edge's colour, we get an average
		 * number of vertices in a particular vertex's colour
		 */
		for (BitSet dteColo : setOfDTEColours) {
			ObjectDoubleOpenHashMap<BitSet> vColoDistPerDTEColour = mapVColoDistPerDTEColo.get(dteColo);
			
			LOGGER.info("-- Process datatype edge: " + dteColo +"("+iCounter+"/"+setOfDTEColours.size()+")");
			//System.err.println("-- Process datatype edge: " + dteColo +"("+iCounter+"/"+setOfDTEColours.size()+")");
			iCounter++;
			if (vColoDistPerDTEColour != null) {
				Object[] arrOfProcessedVColours = vColoDistPerDTEColour.keys;
				for (int i = 0; i < arrOfProcessedVColours.length; ++i) {
					if (vColoDistPerDTEColour.allocated[i]) {
						BitSet vColo = (BitSet) arrOfProcessedVColours[i];
						double avrgNoOfVertices = vColoDistPerDTEColour.values[i];

						if (mapVColoToVertices.containsKey(vColo)) {
							int[] arrOfVertices = mapVColoToVertices.get(vColo).toIntArray();

							if (arrOfVertices.length > 0 ) { 
								double numOfConsidedVertices = avrgNoOfVertices	* arrOfVertices.length;
								if (numOfConsidedVertices == 0) {
									numOfConsidedVertices = 1;
								}
								numOfConsidedVertices = Math.round(numOfConsidedVertices);
								//System.out.println("[Test] Number of considered vertices: " + numOfConsidedVertices);
								Random rand = new Random();
								int counterVertices = 0;
								while (counterVertices < numOfConsidedVertices) {
									
									// get a
									int vId = arrOfVertices[rand.nextInt(arrOfVertices.length)];
									LOGGER.info("---- Generate literals for vertex " + vId +" ("+(counterVertices+1)+"/"+numOfConsidedVertices+ ")...");
									// get literal
									String literal = mLiteralProposer.getValue(vColo, dteColo);
	
									// add it to the coloured graph
									mimicGraph.addLiterals(literal, vId, dteColo, mLiteralProposer.getLiteralType(dteColo) );
									counterVertices++;
								}
							
							} else {
								LOGGER.warn("Expected a type of node: " + vColo + ", but none found");
							}
						}
					}
				}
			}
		}
		LOGGER.info("End lexicalizing the mimic graph");
		return mimicGraph;
	}
}
