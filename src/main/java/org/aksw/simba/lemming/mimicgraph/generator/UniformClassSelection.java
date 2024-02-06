package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Implementation of the Uniform Class Selection - Uniform Instance Selection
 * (UCS-UIS) from the paper.
 *
 */
@Component("UCSUIS")
@Scope(value = "prototype")
public class UniformClassSelection extends AbstractGraphGeneration implements IGraphGeneration {

	/** Logger object */
	private static final Logger LOGGER = LoggerFactory.getLogger(UniformClassSelection.class);

	/** Counter for the number of iterations for each edge colour */
	private int maxIterationFor1EdgeColour;

	/** Random generator object */
	private Random mRandom;

	/**
	 * Constructor.
	 * 
	 * @param iNumberOfVertices Number of vertices
	 * @param origGrphs         Original/Input/Versioned graphs
	 * @param iNumberOfThreads  Number of threads for graph generation
	 * @param seed              Seed for the RNG
	 */
	public UniformClassSelection(int iNumberOfVertices, ColouredGraph[] origGrphs, int iNumberOfThreads, long seed) {
		super(iNumberOfVertices, origGrphs, iNumberOfThreads, seed);
		mRandom = new Random(this.seed);
		maxIterationFor1EdgeColour = Constants.MAX_ITERATION_FOR_1_COLOUR;
	}

	/**
	 * 	Generates the graph in a multi-threaded way
	 */
	protected void generateGraphMultiThreads() {
		
		// divides the coloured edges across the available threads
		List<IntSet> lstAssignedEdges = getColouredEdgesForConnecting(mNumberOfThreads);
		LOGGER.info("Create " + lstAssignedEdges.size() + " threads for processing graph generation!");
		
		// Create thread pool, assign each thread a set of edges to work with
		ExecutorService service = Executors.newFixedThreadPool(mNumberOfThreads);
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		for (int i = 0; i < lstAssignedEdges.size(); i++) {
			// get the set of edges for the thread
			final IntSet setOfEdges = lstAssignedEdges.get(i);
			final Set<BitSet> setAvailableVertexColours = new HashSet<BitSet>(mMapColourToVertexIDs.keySet());

			Runnable worker = new Runnable() {
				@Override
				public void run() {
					// random
					Random random = new Random(seed);
					seed++;
					// max iteration of 1 edge
					int maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
					// track the index of previous iteration
					int iProcessingEdgeIndex = -1;
					// set of process edges
					int[] arrOfEdges = setOfEdges.toIntArray();
					// set of failed edge colours
					Set<BitSet> failedEdgeColours = new HashSet<BitSet>();

					int j = 0;
					while (j < arrOfEdges.length) {
						// get an edge id
						int fakeEdgeId = arrOfEdges[j];
						BitSet edgeColo = getEdgeColour(fakeEdgeId);

						if (edgeColo == null) {
							// skip the edge that has failed edge colour
							j++;
							continue;
						}

						if (failedEdgeColours.contains(edgeColo)) {
							// skip the edge that has failed edge colour
							j++;
							continue;
						}

						if (iProcessingEdgeIndex != j) {
							maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
							iProcessingEdgeIndex = j;
						} else {
							if (maxIterationFor1Edge == 0) {
								LOGGER.error("Could not create an edge of " + edgeColo
										+ " colour since it could not find any approriate vertices to connect.");
								// skip the edge that has failed attempt after MAX_EXPLORING_TIME
								failedEdgeColours.add(edgeColo);
								j++;
								continue;
							}
						}

						// get potential tail colours
						Set<BitSet> setTailColours = new HashSet<BitSet>(
								mColourMapper.getTailColoursFromEdgeColour(edgeColo));
						setTailColours.retainAll(setAvailableVertexColours);

						/*
						 * in case there is no tail colours => the edge colour should not be considered
						 * again
						 */
						if (setTailColours.size() == 0) {
							failedEdgeColours.add(edgeColo);
							j++;
							continue;
						}

						// get random a tail colour
						BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);

						BitSet tailColo = arrTailColours[random.nextInt(arrTailColours.length)];
						Set<BitSet> setHeadColours = new HashSet<BitSet>(
								mColourMapper.getHeadColours(tailColo, edgeColo));

						if (setHeadColours == null || setHeadColours.size() == 0) {
							maxIterationFor1Edge--;
							continue;
						}
						setHeadColours.retainAll(setAvailableVertexColours);

						if (setHeadColours.size() == 0) {
							maxIterationFor1Edge--;
							continue;
						}

						BitSet[] arrHeadColours = setHeadColours.toArray(new BitSet[0]);

						BitSet headColo = arrHeadColours[random.nextInt(arrHeadColours.length)];

						// get set of tail ids and head ids
						IntSet setTailIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
						IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);

						if (mMapColourToVertexIDs.containsKey(tailColo)) {
							setTailIDs.addAll(mMapColourToVertexIDs.get(tailColo));
						}

						if (mMapColourToVertexIDs.containsKey(headColo)) {
							setHeadIDs.addAll(mMapColourToVertexIDs.get(headColo));
						}

						if (setTailIDs != null && setTailIDs.size() > 0 && setHeadIDs != null
								&& setHeadIDs.size() > 0) {
							int[] arrTailIDs = setTailIDs.toIntArray();
							int tailId = -1;
							int iAttemptToGetTailIds = 1000;
							while (iAttemptToGetTailIds > 0) {
								tailId = arrTailIDs[random.nextInt(arrTailIDs.length)];
								if (!mReversedMapClassVertices.containsKey(tailColo))
									break;
								tailId = -1;
								iAttemptToGetTailIds--;
							}

							if (tailId == -1) {
								maxIterationFor1Edge--;
								continue;
							}

							IntSet tmpSetOfConnectedHeads = getConnectedHeads(tailId, edgeColo);
							if (tmpSetOfConnectedHeads != null && tmpSetOfConnectedHeads.size() > 0) {
								// int[] arrConnectedHeads = tmpSetOfConnectedHeads.toIntArray();
								for (int connectedHead : tmpSetOfConnectedHeads) {
									if (setHeadIDs.contains(connectedHead))
										setHeadIDs.remove(connectedHead);
								}
							}

							if (setHeadIDs.size() == 0) {
								maxIterationFor1Edge--;
								continue;
							}

							int[] arrHeadIDs = setHeadIDs.toIntArray();
							int headId = arrHeadIDs[random.nextInt(arrHeadIDs.length)];
							boolean isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColo);
							if (isFoundVerticesConnected) {
								j++;
								continue;
							}
						}

						maxIterationFor1Edge--;
						if (maxIterationFor1Edge == 0) {
							LOGGER.error("Could not create an edge of " + edgeColo
									+ " colour since it could not find any approriate vertices to connect.");

							failedEdgeColours.add(edgeColo);
							j++;
						}

					} // end of for of edge ids
				}
			};
			tasks.add(Executors.callable(worker));
		}

		try {
			service.invokeAll(tasks);
			service.shutdown();
			service.awaitTermination(48, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			LOGGER.error("Could not shutdown the service executor! Be careful");
			e.printStackTrace();
		}
	}

	/**
	 * Generate graph randomly with a single thread
	 */
	protected void generateGraphSingleThread() {

		/*
		 * mMapColourToEdgeIDs contains only normal edges (not datatype property edges
		 * and rdf:type edges)
		 */
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setAvailableVertexColours = mMapColourToVertexIDs.keySet();
		int iColoCounter = 0;
		for (BitSet edgeColo : keyEdgeColo) {
			iColoCounter++;
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			setTailColours.retainAll(setAvailableVertexColours);

			BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);

			/*
			 * the setFakeEdgeIDs helps us to know how many edges existing in a specific
			 * edge's colour
			 */
			IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
			// use each edge to connect vertices
			LOGGER.info("Generate edges for " + edgeColo + " edge colour (" + iColoCounter + "/" + keyEdgeColo.size()
					+ ")");

			int i = 0;
			while (i < setFakeEdgeIDs.size()) {

				boolean isFoundVerticesConnected = false;
				BitSet tailColo = arrTailColours[mRandom.nextInt(arrTailColours.length)];
				Set<BitSet> setHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);

				if (setHeadColours == null || setHeadColours.size() == 0) {
					continue;
				}

				setHeadColours.retainAll(setAvailableVertexColours);

				if (setHeadColours.size() == 0)
					continue;

				BitSet[] arrHeadColours = setHeadColours.toArray(new BitSet[0]);
				BitSet headColo = arrHeadColours[mRandom.nextInt(arrHeadColours.length)];
				IntSet setTailIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
				IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);

				if (mMapColourToVertexIDs.containsKey(tailColo)) {
					setTailIDs.addAll(mMapColourToVertexIDs.get(tailColo));
				}

				if (mMapColourToVertexIDs.containsKey(headColo)) {
					setHeadIDs.addAll(mMapColourToVertexIDs.get(headColo));
				}

				if (setTailIDs != null && setTailIDs.size() > 0 && setHeadIDs != null && setHeadIDs.size() > 0) {
					int[] arrTailIDs = setTailIDs.toIntArray();
					int tailId = -1;
					while (true) {
						tailId = arrTailIDs[mRandom.nextInt(arrTailIDs.length)];
						if (!mReversedMapClassVertices.containsKey(tailColo))
							break;
					}

					int[] arrConnectedHeads = getConnectedHeads(tailId, edgeColo).toIntArray();
					for (int connectedHead : arrConnectedHeads) {
						if (setHeadIDs.contains(connectedHead))
							setHeadIDs.remove(connectedHead);
					}

					if (setHeadIDs.size() == 0) {
						continue;
					}

					int[] arrHeadIDs = setHeadIDs.toIntArray();
					int headId = arrHeadIDs[mRandom.nextInt(arrHeadIDs.length)];
					isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColo);
					if (isFoundVerticesConnected) {
						isFoundVerticesConnected = true;
						i++;
					}
				}

				if (!isFoundVerticesConnected) {
					maxIterationFor1EdgeColour--;
					if (maxIterationFor1EdgeColour == 0) {
						LOGGER.error("Could not create " + (setFakeEdgeIDs.size() - i) + " edges in the " + edgeColo
								+ " colour since it could not find any approriate vertices to connect.");
						break;
					}
				}
			}
			// reset
			maxIterationFor1EdgeColour = Constants.MAX_ITERATION_FOR_1_COLOUR;
		}
	}

}
