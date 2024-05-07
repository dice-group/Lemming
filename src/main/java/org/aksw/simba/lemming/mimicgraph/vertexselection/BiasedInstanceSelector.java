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

	public BiasedInstanceSelector() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param origGrphs             Input graphs
	 * @param mMapColourToEdgeIDs   Map from colours to edge IDs
	 * @param mMapColourToVertexIDs Map from colours to vertex Ids
	 * @param mColourMapper         Colour mapper
	 * @param seed                  Seed
	 */
	public BiasedInstanceSelector(ColouredGraph[] origGrphs, Map<BitSet, IntSet> mMapColourToEdgeIDs,
			Map<BitSet, IntSet> mMapColourToVertexIDs, IColourMappingRules mColourMapper, long seed) {
		computePotentialIODegreePerVert(origGrphs, mMapColourToEdgeIDs, mMapColourToVertexIDs, mColourMapper, seed);
	}

	@Override
	public IOfferedItem<Integer> getProposedVertex(BitSet edgecolour, BitSet vertexColour, VERTEX_TYPE type) {
		ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> proposers = getProposers(edgecolour, type);
		return proposers.get(vertexColour);
	}

	private void computePotentialIODegreePerVert(ColouredGraph[] origGrphs, Map<BitSet, IntSet> mMapColourToEdgeIDs,
			Map<BitSet, IntSet> mMapColourToVertexIDs, IColourMappingRules mColourMapper, long seed) {
		// compute for each vertex's colour, the average out-degree associated with a
		// specific edge's colour
		AvrgDegreeDistBaseVEColour avrgInDegreeAnalyzer = new AvrgInDegreeDistBaseVEColo(origGrphs);
		// compute for each vertex's colour, the average in-degree associated with a
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
							mMapColourToVertexIDs, seed);
				}
			}

			Set<BitSet> setHeadColours = mColourMapper.getHeadColoursFromEdgeColour(edgeColo);
			for (BitSet headColo : setHeadColours) {
				if (setVertexColours.contains(headColo)) {
					computeProposedColours(avrgInDegreeAnalyzer, mapPossibleIDegreePerIEColo, headColo, edgeColo,
							mMapColourToVertexIDs, seed);
				}
			}
		}
	}

	private void computeProposedColours(AvrgDegreeDistBaseVEColour avrgOutDegreeAnalyzer,
			ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>> mapPossibleDegreePerEColour,
			BitSet vertexColour, BitSet edgeColour, Map<BitSet, IntSet> mMapColourToVertexIDs, long seed) {

		double avrgDegree = avrgOutDegreeAnalyzer.getAverageDegreeOf(vertexColour, edgeColour);

		// get list tailIDs
		int[] arrVertexIDs = mMapColourToVertexIDs.get(vertexColour).toIntArray();
		double[] possDegreePerVertexIDs = new double[arrVertexIDs.length];
		Integer[] objTailIDs = new Integer[arrVertexIDs.length];
		// for each tail id, we compute the potential out degree for it
		Random random = new Random(seed);
		seed++;
		for (int i = 0; i < arrVertexIDs.length; i++) {
			objTailIDs[i] = arrVertexIDs[i];
			// generate a random out degree for each vertex in its set based on the computed
			// average out-degree
			int possDeg = PoissonDistribution.randomXJunhao(avrgDegree, random);
			if (possDeg == 0)
				possDeg = 1;

			possDegreePerVertexIDs[i] = (double) possDeg;
		}

		ObjectDistribution<Integer> potentialDegree = new ObjectDistribution<Integer>(objTailIDs,
				possDegreePerVertexIDs);
		OfferedItemByRandomProb<Integer> potentialDegreeProposer = new OfferedItemByRandomProb<Integer>(potentialDegree,
				random);
		seed = potentialDegreeProposer.getSeed() + 1;
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

	@Override
	public int selectTailFromColour(BitSet tailColour) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int selectHeadFromColour(BitSet headColour, BitSet edgeColour, int candidateTailId) {
		// TODO Auto-generated method stub
		return 0;
	}
}