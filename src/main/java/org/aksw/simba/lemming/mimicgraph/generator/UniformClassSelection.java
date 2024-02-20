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
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector.VERTEX_TYPE;
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
@Component("UCS")
@Scope(value = "prototype")
public class UniformClassSelection extends AbstractGraphGeneration implements IGraphGeneration {

	/** Logger object */
	private static final Logger LOGGER = LoggerFactory.getLogger(UniformClassSelection.class);

	/** Counter for the number of iterations for each edge colour */
	private int maxIterationFor1EdgeColour;

	/** Random generator object */
	private Random mRandom;

	/**
	 * 
	 * @param iNumberOfVertices
	 * @param origGrphs
	 * @param iNumberOfThreads
	 * @param seed
	 * @param vertexSelector
	 */
	public UniformClassSelection(int iNumberOfVertices, ColouredGraph[] origGrphs, int iNumberOfThreads, long seed,
			IVertexSelector vertexSelector) {
		super(iNumberOfVertices, origGrphs, iNumberOfThreads, seed);
		mRandom = new Random(this.seed);
		maxIterationFor1EdgeColour = Constants.MAX_ITERATION_FOR_1_COLOUR;

	}

	/**
	 * Generates the graph in a multi-threaded way
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
					// random number generator
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

					for (int j = 0; j < arrOfEdges.length;) {
						// get an edge id
						int fakeEdgeId = arrOfEdges[j];
						BitSet edgeColour = getEdgeColour(fakeEdgeId);

						// skip failed edge colours
						if (edgeColour == null || failedEdgeColours.contains(edgeColour)) {
							j++;
							continue;
						}

						if (iProcessingEdgeIndex != j) {
							maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
							iProcessingEdgeIndex = j;
						} else {
							if (maxIterationFor1Edge == 0) {
								LOGGER.error("Could not create an edge of " + edgeColour
										+ " colour since it could not find any approriate vertices to connect.");
								// skip the edge that has failed attempt after MAX_EXPLORING_TIME
								failedEdgeColours.add(edgeColour);
								j++;
								continue;
							}
						}

						// get potential tail colours
						Set<BitSet> setTailColours = new HashSet<BitSet>(
								mColourMapper.getTailColoursFromEdgeColour(edgeColour));
						setTailColours.retainAll(setAvailableVertexColours);

						/*
						 * in case there is no tail colours => the edge colour should not be considered
						 * again
						 */
						if (setTailColours.size() == 0) {
							failedEdgeColours.add(edgeColour);
							j++;
							continue;
						}

						// get random a tail colour
						BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);
						BitSet tailColo = arrTailColours[random.nextInt(arrTailColours.length)];
						
						// get potential head colours
						Set<BitSet> setHeadColours = new HashSet<BitSet>(
								mColourMapper.getHeadColours(tailColo, edgeColour));
						
						if (setHeadColours == null || setHeadColours.size() == 0) {
							maxIterationFor1Edge--;
							continue;
						}
						setHeadColours.retainAll(setAvailableVertexColours);

						if (setHeadColours.size() == 0) {
							maxIterationFor1Edge--;
							continue;
						}

						// get random head colour
						BitSet[] arrHeadColours = setHeadColours.toArray(new BitSet[0]);
						BitSet headColo = arrHeadColours[random.nextInt(arrHeadColours.length)];

						// We select the instances now
						IOfferedItem<Integer> tailIDProposer = vertexSelector.getProposedVertex(edgeColour, tailColo,
								VERTEX_TYPE.TAIL);
						IOfferedItem<Integer> headIDProposer = vertexSelector.getProposedVertex(edgeColour, headColo,
								VERTEX_TYPE.HEAD);
						
						// skip if proposers are invalid
						if (tailIDProposer == null || headIDProposer == null) {
							maxIterationFor1Edge--;
							continue;
						}
						
						// attempt to select tail ID
						int tailId = -1;
						int iAttemptToGetTailIds = 1000;
						while (iAttemptToGetTailIds > 0) {
							tailId = tailIDProposer.getPotentialItem();
							if (mMapClassVertices.containsKey(tailColo))
								break;
							tailId = -1;
							iAttemptToGetTailIds--;
						}

						if (tailId == -1) {
							maxIterationFor1Edge--;
							continue;
						}

						// get set of all existing tail and head ids
						IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
						if (mMapColourToVertexIDs.containsKey(headColo)) {
							setHeadIDs.addAll(mMapColourToVertexIDs.get(headColo));
						}

						// skip if invalid
						if (setHeadIDs == null || setHeadIDs.size() == 0) {
							maxIterationFor1Edge--;
							continue;
						}
						
						// get heads connected to a tail ID by the edge colour
						// remove these from the possible set
						IntSet tmpSetOfConnectedHeads = getConnectedHeads(tailId, edgeColour);
						setHeadIDs.removeAll(tmpSetOfConnectedHeads);
						
						// skip if possible set IDs is empty
						if (setHeadIDs.size() == 0) {
							maxIterationFor1Edge--;
							continue;
						}

						// get head id, filtered by the already connected
						Set<Integer> setFilteredHeadIDs = new HashSet<Integer>(setHeadIDs);
						int headId = headIDProposer.getPotentialItem(setFilteredHeadIDs);

						boolean isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColour);
						if (isFoundVerticesConnected) {
							j++;
							continue;
						}

						maxIterationFor1Edge--;
						if (maxIterationFor1Edge == 0) {
							LOGGER.error("Could not create an edge of " + edgeColour
									+ " colour since it could not find any approriate vertices to connect.");

							failedEdgeColours.add(edgeColour);
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

		// edge map excludes datatype property edges or rdf:type
		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setAvailableVertexColours = mMapColourToVertexIDs.keySet();
		int iColoCounter = 0;
		for (BitSet edgeColo : setEdgeColours) {

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
			LOGGER.info("Generate edges for " + edgeColo + " edge colour (" + iColoCounter + "/" + setEdgeColours.size()
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

				IOfferedItem<Integer> tailIDProposer = vertexSelector.getProposedVertex(edgeColo, tailColo,
						VERTEX_TYPE.TAIL);
				IOfferedItem<Integer> headIDProposer = vertexSelector.getProposedVertex(edgeColo, headColo,
						VERTEX_TYPE.HEAD);

				if (tailIDProposer != null && headIDProposer != null) {
					int tailId = tailIDProposer.getPotentialItem();
					if (mReversedMapClassVertices.containsKey(tailId)) {
						continue;
					}

					IntSet setTailIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
					if (mMapColourToVertexIDs.containsKey(tailColo)) {
						setTailIDs.addAll(mMapColourToVertexIDs.get(tailColo));
					}

					IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
					if (mMapColourToVertexIDs.containsKey(headColo)) {
						setHeadIDs.addAll(mMapColourToVertexIDs.get(headColo));
					}

					if (setHeadIDs == null || setHeadIDs.size() == 0) {
						continue;
					}

					int[] arrConnectedHeads = getConnectedHeads(tailId, edgeColo).toIntArray();
					for (int connectedHead : arrConnectedHeads) {
						if (setHeadIDs.contains(connectedHead))
							setHeadIDs.remove(connectedHead);
					}

					if (setHeadIDs.size() == 0) {
						continue;
					}

					int headId = headIDProposer.getPotentialItem(setHeadIDs);
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
