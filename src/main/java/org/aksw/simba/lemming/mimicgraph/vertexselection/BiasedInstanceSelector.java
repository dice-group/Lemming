package org.aksw.simba.lemming.mimicgraph.vertexselection;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgDegreeDistBaseVEColour;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgInDegreeDistBaseVEColo;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgOutDegreeDistBaseVEColo;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.PoissonDistribution;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.generator.binary.GraphInitializer;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Implementation of the Biased Instance Selection (UIS). Retrieves a vertex ID
 * based on the in and out degree distribution.
 * 
 * @author Ana Silva
 *
 */
@Component("BIS")
@Scope(value = "prototype")
public class BiasedInstanceSelector implements IVertexSelector {
	private static final Logger LOGGER = LoggerFactory.getLogger(BiasedInstanceSelector.class);

	/** Map of head vertices proposers per edge */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>> mapPossibleIDegreePerIEColo;

	/** Map of tail vertices proposers per edge */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>> mapPossibleODegreePerOEColo;

	/**
	 * Constructor.
	 * 
	 * @param graphInit The {@link GraphInitializer} object
	 */
	public BiasedInstanceSelector(GraphInitializer graphInit) {
		mapPossibleIDegreePerIEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		mapPossibleODegreePerOEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();

		// computes in and out degree distribution based on input graphs
		computePotentialIODegreePerVert(graphInit.getOriginalGraphs(), graphInit.getmMapColourToEdgeIDs(),
				graphInit.getmMapColourToVertexIDs(), graphInit.getColourMapper(), graphInit.getSeedGenerator());
//		computePotentialIODegreePerVertMT(graphInit.getOriginalGraphs(), graphInit.getmMapColourToEdgeIDs(),
//				graphInit.getmMapColourToVertexIDs(), graphInit.getColourMapper(), graphInit.getSeedGenerator());
	}

	/**
	 * Retrieves a vertex instance according to the corresponding degree
	 * distribution
	 */
	@Override
	public IOfferedItem<Integer> getProposedVertex(BitSet edgecolour, BitSet vertexColour, VERTEX_TYPE type) {
		return getProposers(edgecolour, type).get(vertexColour);
	}
	
	/**
	 * Computes in and out degree distribution based on input graphs
	 * 
	 * @param origGrphs             The input graphs
	 * @param mMapColourToEdgeIDs   The colour to edge IDs mapping
	 * @param mMapColourToVertexIDs The colour to vertex IDs mapping
	 * @param mColourMapper         The {@link ColourMapper} object
	 * @param seedGenerator         The seed generator
	 */
	private void computePotentialIODegreePerVert(ColouredGraph[] origGrphs, Map<BitSet, IntSet> mMapColourToEdgeIDs,
			Map<BitSet, IntSet> mMapColourToVertexIDs, IColourMappingRules mColourMapper, SeedGenerator seedGenerator) {
		// compute for each vertex's colour, the average in-degree associated with a
		// specific edge's colour
		AvrgDegreeDistBaseVEColour avrgInDegreeAnalyzer = new AvrgInDegreeDistBaseVEColo(origGrphs);
		// compute for each vertex's colour, the average out-degree associated with a
		// specific edge's colour
		AvrgDegreeDistBaseVEColour avrgOutDegreeAnalyzer = new AvrgOutDegreeDistBaseVEColo(origGrphs);

		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();

		// for each edge colour, compute possible tails and heads
		for (BitSet edgeColo : setEdgeColours) {
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);

			for (BitSet tailColo : setTailColours) {
				if (setVertexColours.contains(tailColo)) {
					computeProposedColours(avrgOutDegreeAnalyzer, mapPossibleODegreePerOEColo, tailColo, edgeColo,
							mMapColourToVertexIDs, seedGenerator.getNextSeed());
				}
			}

			Set<BitSet> setHeadColours = mColourMapper.getHeadColoursFromEdgeColour(edgeColo);
			for (BitSet headColo : setHeadColours) {
				if (setVertexColours.contains(headColo)) {
					computeProposedColours(avrgInDegreeAnalyzer, mapPossibleIDegreePerIEColo, headColo, edgeColo,
							mMapColourToVertexIDs, seedGenerator.getNextSeed());
				}
			}
		}
	}

	/**
	 * Computes in and out degree distribution based on input graphs
	 * 
	 * @param origGrphs             The input graphs
	 * @param mMapColourToEdgeIDs   The colour to edge IDs mapping
	 * @param mMapColourToVertexIDs The colour to vertex IDs mapping
	 * @param mColourMapper         The {@link ColourMapper} object
	 * @param seedGenerator         The seed generator
	 */
	private void computePotentialIODegreePerVertMT(ColouredGraph[] origGrphs, Map<BitSet, IntSet> mMapColourToEdgeIDs,
			Map<BitSet, IntSet> mMapColourToVertexIDs, IColourMappingRules mColourMapper, SeedGenerator seedGenerator) {
		// compute for each vertex's colour, the average in-degree associated with a
		// specific edge's colour
		LOGGER.debug("Computing average in-degree distribution");
		AvrgDegreeDistBaseVEColour avrgInDegreeAnalyzer = new AvrgInDegreeDistBaseVEColo(origGrphs);
		// compute for each vertex's colour, the average out-degree associated with a
		// specific edge's colour
		LOGGER.debug("Computing average out-degree distribution");
		AvrgDegreeDistBaseVEColour avrgOutDegreeAnalyzer = new AvrgOutDegreeDistBaseVEColo(origGrphs);

		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();

		// for each edge colour, compute possible tails and heads
		for (BitSet edgeColo : setEdgeColours) {
			final BitSet finalEdgeColo = edgeColo;
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			Set<BitSet> setHeadColours = mColourMapper.getHeadColoursFromEdgeColour(edgeColo);

			Thread tailThread = new Thread(() -> {
                for (BitSet tailColo : setTailColours) {
                    if (setVertexColours.contains(tailColo)) {
                        computeProposedColours(avrgOutDegreeAnalyzer, mapPossibleODegreePerOEColo, tailColo, finalEdgeColo,
                                mMapColourToVertexIDs, seedGenerator.getNextSeed());
                    }
                }
            });

            Thread headThread = new Thread(() -> {
                for (BitSet headColo : setHeadColours) {
                    if (setVertexColours.contains(headColo)) {
                        computeProposedColours(avrgInDegreeAnalyzer, mapPossibleIDegreePerIEColo, headColo, finalEdgeColo,
                                mMapColourToVertexIDs, seedGenerator.getNextSeed());
                    }
                }
            });

            tailThread.start();
            headThread.start();

            try {
                tailThread.join();
                headThread.join();
            } catch (InterruptedException e) {
                LOGGER.error("Thread interrupted", e);
            }
        }
		
	}

	/**
	 * Computes degrees for each vertex based on the average colour degree
	 * {@link AvrgDegreeDistBaseVEColour}
	 * 
	 * @param avrgDegreeAnalyzer
	 * @param mapPossibleDegreePerEColour
	 * @param vertexColour
	 * @param edgeColour
	 * @param mMapColourToVertexIDs
	 * @param seed
	 */
	private void computeProposedColours(AvrgDegreeDistBaseVEColour avrgDegreeAnalyzer,
			ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>> mapPossibleDegreePerEColour,
			BitSet vertexColour, BitSet edgeColour, Map<BitSet, IntSet> mMapColourToVertexIDs, long seed) {

		double avrgDegree = avrgDegreeAnalyzer.getAverageDegreeOf(vertexColour, edgeColour);

		// get list tailIDs
		int[] arrVertexIDs = mMapColourToVertexIDs.get(vertexColour).toIntArray();
		double[] possDegreePerVertexIDs = new double[arrVertexIDs.length];
		Integer[] objIDs = new Integer[arrVertexIDs.length];
		// for each tail id, we compute the potential out degree for it
		Random random = new Random(seed);
		for (int i = 0; i < arrVertexIDs.length; i++) {
			objIDs[i] = arrVertexIDs[i];
			// generate a random out degree for each vertex in its set based on the computed
			// average out-degree
			int possDeg = PoissonDistribution.randomXJunhao(avrgDegree, random);
			if (possDeg == 0)
				possDeg = 1;

			possDegreePerVertexIDs[i] = (double) possDeg;
		}

		ObjectDistribution<Integer> potentialDegree = new ObjectDistribution<Integer>(objIDs,
				possDegreePerVertexIDs);
		OfferedItemByRandomProb<Integer> potentialDegreeProposer = new OfferedItemByRandomProb<Integer>(potentialDegree,
				random);
		// put to map potential degree proposer
		ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapPossDegree = mapPossibleDegreePerEColour
				.get(edgeColour);
		if (mapPossDegree == null) {
			mapPossDegree = new ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>();
			mapPossibleDegreePerEColour.put(edgeColour, mapPossDegree);
		}

		IOfferedItem<Integer> outDegreeProposer = mapPossDegree.get(vertexColour);
		if (outDegreeProposer == null) {
			mapPossDegree.put(vertexColour, potentialDegreeProposer);
		} else {
			LOGGER.error("Something is seriously happening for head/tail colours, since " + vertexColour
					+ " can not have more than 2 proposers");
		}
	}

	public ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> getProposers(BitSet edgeColour, VERTEX_TYPE type) {
		switch (type) {
		case HEAD: {
			return mapPossibleIDegreePerIEColo.get(edgeColour);
		}
		case TAIL: {
			return mapPossibleODegreePerOEColo.get(edgeColour);
		}
		default:
			throw new IllegalArgumentException("Unknown vertex type " + type);
		}
	}
}