package org.aksw.simba.lemming.mimicgraph.colourselection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphInitializer;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.TripleColourDistributionMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSetOfIDs;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.google.common.primitives.Doubles;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

@Component("CCS")
@Scope(value = "prototype")
public class ClusteredClassSelector implements IClassSelector {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredClassSelector.class);

	private GraphInitializer graphInit;
	private Random random;

	private Map<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>> mTrippleMapOfTailHeadEdgeRates;
	private List<TripleColourDistributionMetric> mLstEVColorMapping;
	private Map<Integer, List<BitSet>> mMapEdgeIdsToTripleColours;

	public ClusteredClassSelector(GraphInitializer graphInit) {
		this.graphInit = graphInit;
		this.random = new Random(graphInit.getSeed());
		mTrippleMapOfTailHeadEdgeRates = new HashMap<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>>();
		mLstEVColorMapping = new ArrayList<TripleColourDistributionMetric>();
		mMapEdgeIdsToTripleColours = new HashMap<Integer, List<BitSet>>();

		// compute edges and vertices distribution over each triple's colour
		computeEVColoDist(graphInit.getOriginalGraphs());

		// compute average distribution of edges/ vertices of each triple's colour
		computeAverageEVColoDistribution();

		// assign specific number of edges to each grouped triple
		computeNoOfEdgesInTriples();

		// assign specific number of vertices to each grouped triple
		computeNoOfVerticesInTriples();
		
		assignVerticesToTriples();
	}

	/**
	 * @deprecated TODO This is to be removed. Create a different interface for when both
	 *             tail and head classes need to be sampled at the same time
	 */
	@Override
	@Deprecated
	public BitSet getTailClass(BitSet edgeColour) {
		return null;
	}

	/**
	 * @deprecated TODO This is to be removed. Create a different interface for when both
	 *             tail and head classes need to be sampled at the same time
	 */
	@Override
	@Deprecated
	public BitSet getHeadClass(BitSet tailColour, BitSet edgeColour) {
		return null;
	}

	@Override
	public ClassProposal getProposal(BitSet edgeColour, int fakeEdgeId) {
		List<BitSet> tripleColours = mMapEdgeIdsToTripleColours.get(fakeEdgeId);
		if (tripleColours == null || tripleColours.size() != 3) {
			return null;
		}

		BitSet tailColour = tripleColours.get(0);
		BitSet checkedColour = tripleColours.get(1);
		BitSet headColour = tripleColours.get(2);

		return new ClassProposal(tailColour, checkedColour, headColour);
	}

	private void computeEVColoDist(ColouredGraph[] origGrphs) {
		for (ColouredGraph grph : origGrphs) {
			TripleColourDistributionMetric colorMapping = new TripleColourDistributionMetric();
			colorMapping.applyWithSingleThread(grph);
			mLstEVColorMapping.add(colorMapping);
		}
	}

	/**
	 * compute average distribution of edges/ vertices of each triple's colour
	 */
	private void computeAverageEVColoDistribution() {
		Set<BitSet> vertColors = graphInit.getAvailableVertexColours();
		Set<BitSet> edgeColors = graphInit.getAvailableEdgeColours();

		for (BitSet tailColo : vertColors) {
			for (BitSet headColo : vertColors) {
				for (BitSet edgeColo : edgeColors) {

					int avrgDenominator = 0;
					double totalTailPercentage = 0;
					double totalEdgePercentage = 0;
					double totalHeadPercentage = 0;

					int iNoOfSamples = mLstEVColorMapping.size();
					for (int i = 0; i < iNoOfSamples; i++) {
						TripleColourDistributionMetric tripleColourMapper = mLstEVColorMapping.get(i);

						double noOfHeads = tripleColourMapper.getNoOfIncidentHeads(tailColo, edgeColo, headColo);
						double noOfTails = tripleColourMapper.getNoOfIncidentTails(tailColo, edgeColo, headColo);
						double noOfEdges = tripleColourMapper.getNoOfIncidentEdges(tailColo, edgeColo, headColo);

						if (noOfHeads != 0 && noOfTails != 0 && noOfEdges != 0) {
							avrgDenominator++;
							totalHeadPercentage += (noOfHeads / tripleColourMapper.getTotalNoOfVerticesIn(headColo));
							totalTailPercentage += (noOfTails / tripleColourMapper.getTotalNoOfVerticesIn(tailColo));
							totalEdgePercentage += (noOfEdges / tripleColourMapper.getTotalNoOfEdgesIn(edgeColo));
						} else {
							// for testing only
							if ((noOfHeads + noOfTails + noOfHeads) != 0) {
								LOGGER.error("Found a triple missing of either tail, head or edges!");
							}
						}
					}

					if (avrgDenominator != 0) {
						avrgDenominator = mLstEVColorMapping.size();

						totalTailPercentage = totalTailPercentage / avrgDenominator;
						totalEdgePercentage = totalEdgePercentage / avrgDenominator;
						totalHeadPercentage = totalHeadPercentage / avrgDenominator;

						TripleBaseSetOfIDs trippleForEdge = new TripleBaseSetOfIDs(tailColo, totalTailPercentage,
								edgeColo, totalEdgePercentage, headColo, totalHeadPercentage);
						putToMap(tailColo, headColo, edgeColo, trippleForEdge, mTrippleMapOfTailHeadEdgeRates);
					}
				}
			}
		}
	}

	/**
	 * put information of tail, head, and edge into triple
	 * 
	 * @param firstKey
	 * @param secondKey
	 * @param thirdKey
	 * @param val
	 * @param changedMap
	 */
	private void putToMap(BitSet firstKey, BitSet secondKey, BitSet thirdKey, TripleBaseSetOfIDs val,
			Map<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>> changedMap) {
		if (changedMap == null) {
			changedMap = new HashMap<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>>();
		}

		Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapSecondThird = changedMap.get(firstKey);

		if (mapSecondThird == null) {
			mapSecondThird = new HashMap<BitSet, Map<BitSet, TripleBaseSetOfIDs>>();
			changedMap.put(firstKey, mapSecondThird);
		}

		Map<BitSet, TripleBaseSetOfIDs> mapThird = mapSecondThird.get(secondKey);

		if (mapThird == null) {
			mapThird = new HashMap<BitSet, TripleBaseSetOfIDs>();
			mapSecondThird.put(secondKey, mapThird);
		}

		if (!mapThird.containsKey(thirdKey)) {
			mapThird.put(thirdKey, val);
		} else {
			LOGGER.error("[putToMap] Something is wrong!");
			System.err.println("[putToMap] Something is wrong!");
		}
	}

	/**
	 * compute average number of edges in each triple
	 */
	private void computeNoOfEdgesInTriples() {
		Set<BitSet> setVertColo = graphInit.getAvailableVertexColours();
		Set<BitSet> setEdgeColo = graphInit.getAvailableEdgeColours();

		long seed = graphInit.getSeed();
		for (BitSet edgeColo : setEdgeColo) {

			List<TripleBaseSetOfIDs> lstGrpTriples = new ArrayList<TripleBaseSetOfIDs>();
			List<Double> lstEdgeRatePerGrpTriple = new ArrayList<Double>();
			for (BitSet tailColo : setVertColo) {
				if (mTrippleMapOfTailHeadEdgeRates.containsKey(tailColo)) {
					Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates
							.get(tailColo);

					Set<BitSet> setHeadColours = mapHeadEdgeToGrpTriples.keySet();

					for (BitSet headColo : setHeadColours) {

						if (!mapHeadEdgeToGrpTriples.containsKey(headColo))
							continue;

						Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);

						if (mapEdgeToGrpTriples.containsKey(edgeColo)) {
							TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);

							if (triple != null) {
								lstGrpTriples.add(triple);
								lstEdgeRatePerGrpTriple.add(triple.noOfEdges);
							}
						}
					}
				}
			}

			// assign specific number of edges in "edgeColo" to each of grouped triples
			if (lstGrpTriples.size() > 0) {
				TripleBaseSetOfIDs[] arrGrpTriples = lstGrpTriples.toArray(new TripleBaseSetOfIDs[0]);
				double[] arrEdgeRatePerTriple = Doubles.toArray(lstEdgeRatePerGrpTriple);

				ObjectDistribution<TripleBaseSetOfIDs> objDist = new ObjectDistribution<TripleBaseSetOfIDs>(
						arrGrpTriples, arrEdgeRatePerTriple);

				OfferedItemByRandomProb<TripleBaseSetOfIDs> grpTripleProposer = new OfferedItemByRandomProb<TripleBaseSetOfIDs>(
						objDist, seed);
				IntSet setEdges = graphInit.getmMapColourToEdgeIDs().get(edgeColo);

				if (setEdges != null && setEdges.size() > 0) {
					for (int eId : setEdges) {
						TripleBaseSetOfIDs offeredGrpTriple = grpTripleProposer.getPotentialItem();
						offeredGrpTriple.edgeIDs.add(eId);

						// add to map fake edge ids and triple colours
						List<BitSet> setTripleColours = mMapEdgeIdsToTripleColours.get(eId);
						setTripleColours = new ArrayList<BitSet>();
						mMapEdgeIdsToTripleColours.put(eId, setTripleColours);

						setTripleColours.add(offeredGrpTriple.tailColour);
						setTripleColours.add(offeredGrpTriple.edgeColour);
						setTripleColours.add(offeredGrpTriple.headColour);
					}
				}
			}
		}

		LOGGER.info("Done assing edges to grouped triples");
	}

	/**
	 * compute possible number of vertices in triples
	 */
	private void computeNoOfVerticesInTriples() {
		Set<BitSet> setVertColo = graphInit.getAvailableVertexColours();
		Set<BitSet> setEdgeColo = graphInit.getAvailableEdgeColours();

		for (BitSet tailColo : setVertColo) {
			// get all tails
			IntSet setTails = graphInit.getmMapColourToVertexIDs().get(tailColo);
			// tail distribution
			Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates
					.get(tailColo);

			if (mapHeadEdgeToGrpTriples == null)
				continue;

			for (BitSet headColo : setVertColo) {

				if (mapHeadEdgeToGrpTriples.containsKey(headColo)) {

					// get all heads
					IntSet setHeads = graphInit.getmMapColourToVertexIDs().get(headColo);

					for (BitSet edgeColo : setEdgeColo) {
						Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
						TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
						if (triple != null && triple.edgeIDs.size() > 0) {

							/// tails
							double noOfTails = Math.round(triple.noOfTails * setTails.size() + 0.1);
							if (noOfTails > triple.edgeIDs.size()) {
								noOfTails = triple.edgeIDs.size();
							}

							if (noOfTails == 0)
								noOfTails = 1;

							triple.noOfTails = noOfTails;

							/// heads
							double noOfHeads = Math.round(triple.noOfHeads * setHeads.size() + 0.1);

							if (noOfHeads > triple.edgeIDs.size()) {
								noOfHeads = triple.edgeIDs.size();
							}

							if (noOfHeads == 0)
								noOfHeads = 1;

							triple.noOfHeads = noOfHeads;

							triple.noOfEdges = triple.edgeIDs.size();
						}
					}
				}
			}
		}
	}

	/**
	 * assign vertices to triples
	 */
	private void assignVerticesToTriples() {
		Set<BitSet> setVertColo = graphInit.getAvailableVertexColours();

		for (BitSet tailColo : setVertColo) {

			if (!mTrippleMapOfTailHeadEdgeRates.containsKey(tailColo)) {
				continue;
			}

			Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates
					.get(tailColo);

			for (BitSet headColo : setVertColo) {

				if (!mapHeadEdgeToGrpTriples.containsKey(headColo)) {
					continue;
				}

				Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);

				Set<BitSet> setEdgeColours = mapEdgeToGrpTriples.keySet();

				for (BitSet edgeColo : setEdgeColours) {

					if (!mapEdgeToGrpTriples.containsKey(edgeColo)) {
						continue;
					}

					TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);

					if (triple.edgeIDs.size() > 0) {
						double noOfEdges = triple.edgeIDs.size();
						IntSet setOfRandomTailIds = getRandomVertices(triple.tailColour, triple.noOfTails);
						IntSet setOfRandomHeadIds = getRandomVertices(triple.headColour, triple.noOfHeads);

						if (setOfRandomHeadIds == null || setOfRandomTailIds == null) {
							LOGGER.warn("There exists no vertices in " + triple.headColour + " or " + triple.tailColour
									+ " colour. Skip: " + noOfEdges + " edges!");
							continue;
						}

						triple.tailIDs.addAll(setOfRandomTailIds);
						triple.headIDs.addAll(setOfRandomHeadIds);
						/*
						 * standardize the amount of edges and vertices this makes sure there is no pair
						 * of vertices are connected by 2 edges in same colour
						 */
						double totalEdges = (double) (setOfRandomTailIds.size() * setOfRandomHeadIds.size());

						if (totalEdges < noOfEdges) {
							LOGGER.warn("Not generate " + (noOfEdges - totalEdges) + " edges in " + edgeColo);
							noOfEdges = totalEdges;
						}

						triple.noOfEdges = noOfEdges;
					}
				}
			}
		}
	}

	private IntSet getRandomVertices(BitSet vertColo, double iNoOfVertices) {
		IntSet setVertices = graphInit.getmMapColourToVertexIDs().get(vertColo);
		if (setVertices != null) {

			IntSet res = new DefaultIntSet(Constants.DEFAULT_SIZE);

			if (iNoOfVertices >= setVertices.size()) {
				iNoOfVertices = setVertices.size();
				return setVertices;
			}

			// store the array indices for which the vertID should not match in order to
			// exclude these
			// array indexes from the random number generation
			IntSet exclusionSet = new DefaultIntSet(Constants.DEFAULT_SIZE);
			for (int e : graphInit.getmReversedMapClassVertices().keySet()) {
				if (setVertices.contains(e)) {
					exclusionSet.add(e);
				}
			}
			if (exclusionSet.size() >= setVertices.size()) {
				LOGGER.warn("No possible vertices to connect of " + vertColo);
				return null;
			}

			while (iNoOfVertices > 0) {
				int vertId = RandomUtil.getRandomWithExclusion(random, setVertices.size(), exclusionSet);
				if (!res.contains(vertId)) {
					res.add(vertId);
					iNoOfVertices--;
				}

				if (res.size() == (setVertices.size() - 1)) {
					if (iNoOfVertices != 0)
						LOGGER.warn("Could not get " + iNoOfVertices + " vertices of " + vertColo);
					break;
				}
			}

			return res;
		}
		return null;
	}

}
