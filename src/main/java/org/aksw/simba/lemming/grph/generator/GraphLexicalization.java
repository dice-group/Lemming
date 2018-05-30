package org.aksw.simba.lemming.grph.generator;

import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.LiteralProcessor;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgColouredVDistPerDTEColour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class GraphLexicalization {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphLexicalization.class);

	private IRDFLiteralGenerator mWordProposer;
	private IGraphGeneration mGraphGenerator;
	private LiteralProcessor mLiteralProcessor;
	private AvrgColouredVDistPerDTEColour mAvrgVDistPerDREColourMetric;

	public GraphLexicalization(ColouredGraph[] origGrphs,
			IGraphGeneration graphGenerator) {
		mGraphGenerator = graphGenerator;
		// average vertex distribution per data typed property
		mAvrgVDistPerDREColourMetric = new AvrgColouredVDistPerDTEColour(
				origGrphs);

		// literal collection
		mLiteralProcessor = new LiteralProcessor(origGrphs);

		// word2vec model to obtain closest
		mWordProposer = new RDFLiteralGenerator(
				mLiteralProcessor.getWordsOfEachDTEColour());
		
		//mWordProposer = new RDFLiteralGeneratorTest(mLiteralProcessor.getWordsOfEachDTEColour());
	}

	public ColouredGraph lexicalizeGraph() {
		LOGGER.info("Start Lexicalizing the mimic graph");
		/*
		 * get the already refined graph
		 */
		ColouredGraph mimicGraph = mGraphGenerator.getMimicGraph();

		/*
		 * get a list of data typed edge's colours
		 */
		Map<BitSet, ObjectDoubleOpenHashMap<BitSet>> mapVColoDistPerDTEColo = mAvrgVDistPerDREColourMetric
				.getMapAvrgColouredVDist();

		/*
		 * set of all data typed edge's colours
		 */
		Set<BitSet> setOfDTEColours = mapVColoDistPerDTEColo.keySet();

		/*
		 * accordingly to each data typed edge's colour, we get the average
		 * number of vertices in a particular vertex's colour
		 */
		for (BitSet dteColo : setOfDTEColours) {
			ObjectDoubleOpenHashMap<BitSet> vColoDistPerDTEColour = mapVColoDistPerDTEColo
					.get(dteColo);
			
			System.err.println("Process dte: " + dteColo);
			
			if (vColoDistPerDTEColour != null) {
				Object[] arrOfProcessedVColours = vColoDistPerDTEColour.keys;
				for (int i = 0; i < arrOfProcessedVColours.length; ++i) {
					if (vColoDistPerDTEColour.allocated[i]) {
						BitSet vColo = (BitSet) arrOfProcessedVColours[i];
						double avrgNoOfVertices = vColoDistPerDTEColour.values[i];

						// get all vertices in all specific colours
						Map<BitSet, IntSet> mapVColoToVertices = mGraphGenerator
								.getMappingColoursAndVertices();
						if (mapVColoToVertices.containsKey(vColo)) {
							int[] arrOfVertices = mapVColoToVertices.get(vColo)
									.toIntArray();

							double numOfConsidedVertices = avrgNoOfVertices
									* arrOfVertices.length;
							if (numOfConsidedVertices == 0) {
								numOfConsidedVertices = 1;
							}
							System.out.println("[Test] Number of considered vertices: " + numOfConsidedVertices);
							numOfConsidedVertices = Math
									.round(numOfConsidedVertices);
							int indexOfVertex = 0;
							while (indexOfVertex < numOfConsidedVertices) {
								
								System.out.println("[Test] The vertex: " + indexOfVertex);
								// get a
								int vId = arrOfVertices[indexOfVertex];
								// get literal
								String literal = mWordProposer.getWords(
										dteColo, (int) mLiteralProcessor
												.getAvrgNoOfWords(dteColo));
								
								// add it to the coloured graph
								mimicGraph.addLiterals(literal, vId, dteColo);
								indexOfVertex++;
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
