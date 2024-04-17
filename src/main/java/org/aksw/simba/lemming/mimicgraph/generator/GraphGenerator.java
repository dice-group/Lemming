package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class is responsible for the general graph generation process
 * 
 * @author Ana Silva
 *
 */
public class GraphGenerator {

	/** Logging object */
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerator.class);

	/** Initializes the graph */
	protected GraphInitializer graphInitializer;

	/** Class Selector implementation */
	protected IClassSelector classSelector;

	/** Instance Selector implementation */
	protected IVertexSelector vertexSelector;

	/**
	 * Constructor.
	 * 
	 * @param graphInitializer
	 * @param classSelector
	 * @param vertexSelector
	 */
	public GraphGenerator(GraphInitializer graphInitializer, IClassSelector classSelector,
			IVertexSelector vertexSelector) {
		this.graphInitializer = graphInitializer;
		this.classSelector = classSelector;
		this.vertexSelector = vertexSelector;
	}

	/**
	 * 
	 * @param noOfThreads
	 * @return
	 */
	public ColouredGraph initializeMimicGraph(ColouredGraph[] origGrphs, int noOfVertices, int noOfThreads, long seed) {

		// create new coloured graph for the synthetic graph from input graphs
		ColouredGraph mimicGraph = graphInitializer.initialize(origGrphs, noOfVertices, noOfThreads);

		// use default if invalid number of threads
		if (noOfThreads < 1) {
			noOfThreads = Runtime.getRuntime().availableProcessors() * 4;
			LOGGER.warn("Invalid number of threads. Defaulting to {} threads", noOfThreads);
		}

		// get set of edges each thread will process
		List<IntSet> lstAssignedEdges = getColouredEdgesForConnecting(noOfThreads);
		ExecutorService service = Executors.newFixedThreadPool(noOfThreads);
		LOGGER.info("Creating {} threads for processing graph generation!", lstAssignedEdges.size());

		// iterate each set of edges and assign to a thread
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		for (int i = 0; i < lstAssignedEdges.size(); i++) {
			final IntSet setOfEdges = lstAssignedEdges.get(i);
			final Set<BitSet> setAvailableVertexColours = graphInitializer.getAvailableVertexColours();

			Runnable worker = new Runnable() {
				@Override
				public void run() {
					int iterationsLeftOfCurrentEdge = Constants.MAX_EXPLORING_TIME;
					// track the index of previous iteration
					int iIndexOfProcessingEdge = -1;
					int[] arrOfEdges = setOfEdges.toIntArray();

					// set of failed edge colours
					Set<BitSet> failedEdgeColours = new HashSet<BitSet>();

					// iterate through assignededge
					for (int j = 0; j < arrOfEdges.length;) {
						// get an edge id
						int fakeEdgeId = arrOfEdges[j];
						BitSet edgeColour = graphInitializer.getEdgeColour(fakeEdgeId);

						// skip failed edge colours
						if (edgeColour == null || failedEdgeColours.contains(edgeColour)) {
							j++;
							continue;
						}

						if (iIndexOfProcessingEdge != j) {
							iterationsLeftOfCurrentEdge = Constants.MAX_EXPLORING_TIME;
							iIndexOfProcessingEdge = j;
						} else {
							if (iterationsLeftOfCurrentEdge == 0) {
								LOGGER.error(
										"Could not create an edge of {} colour since it could not find any approriate vertices to connect.",
										edgeColour);
								failedEdgeColours.add(edgeColour);
								j++;
								continue;
							}
						}
						// get tail and head colours from edge colour
						BitSet tailColour = classSelector.getTailClass(edgeColour);
						BitSet headColour = classSelector.getHeadClass(edgeColour);

						// if invalid, skip edge
						if (tailColour == null || headColour == null) {
							failedEdgeColours.add(edgeColour);
							j++;
							continue;
						}

						// get instances
						int tailId = vertexSelector.selectTailFromColour(tailColour);
						int headId = vertexSelector.selectHeadFromColour(headColour);

						// connect instances
						boolean isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColour, mimicGraph);
						if (isFoundVerticesConnected) {
							j++;
							continue;
						}

						// if it failed to connect, and we ran out of iterations, add to failed colours
						iterationsLeftOfCurrentEdge--;
						if (iterationsLeftOfCurrentEdge == 0) {
							LOGGER.error("Could not create {} edges in the {} colour since it could not find any "
									+ "approriate vertices to connect.", arrOfEdges.length - j, edgeColour);
							failedEdgeColours.add(edgeColour);
							j++;
						}
					}
				}
			};
			tasks.add(Executors.callable(worker));
		}

		// start
		try {
			service.invokeAll(tasks);
			service.shutdown();
			service.awaitTermination(48, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			LOGGER.error("Could not shutdown the service executor! Be careful.");
			e.printStackTrace();
		}
		return mimicGraph;
	}

	public synchronized boolean connectIfPossible(int tailId, int headId, BitSet eColo, ColouredGraph mimicGraph) {
		if (connectableVertices(tailId, headId, eColo)) {
			mimicGraph.addEdge(tailId, headId, eColo);
			return true;
		}
		return false;
	}

	public boolean connectableVertices(int tailId, int headId, BitSet eColo) {
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

	/**
	 * Assign edges to each thread
	 * 
	 * @param numberOfThreads
	 * @return
	 */
	public List<IntSet> getColouredEdgesForConnecting(int numberOfThreads) {
		List<IntSet> lstAssingedEdges = new ArrayList<IntSet>();

		int iNoOfEdges = graphInitializer.getActualNumberOfEdges();

		int iNoOfEdgesPerThread = 0;
		int iNoOfSpareEdges = 0;
		if (numberOfThreads == 1) {
			iNoOfEdgesPerThread = iNoOfEdges;
		} else if (numberOfThreads > 1) {
			if (iNoOfEdges % numberOfThreads == 0) {
				iNoOfEdgesPerThread = iNoOfEdges / numberOfThreads;
			} else {
				iNoOfEdgesPerThread = iNoOfEdges / (numberOfThreads - 1);
				iNoOfSpareEdges = iNoOfEdges - (iNoOfEdgesPerThread * (numberOfThreads - 1));
			}
		}

		if (iNoOfSpareEdges == 0) {
			for (int i = 0; i < numberOfThreads; i++) {
				IntSet tmpSetEdges = new DefaultIntSet(iNoOfEdgesPerThread);
				for (int j = 0; j < iNoOfEdgesPerThread; j++) {
					int iEdgeId = (i * iNoOfEdgesPerThread) + j;
					tmpSetEdges.add(iEdgeId);
				}
				lstAssingedEdges.add(tmpSetEdges);
			}
		} else {
			for (int i = 0; i < numberOfThreads - 1; i++) {
				IntSet tmpSetEdges = new DefaultIntSet(iNoOfEdgesPerThread);
				for (int j = 0; j < iNoOfEdgesPerThread; j++) {
					int iEdgeId = (i * iNoOfEdgesPerThread) + j;
					tmpSetEdges.add(iEdgeId);
				}
				lstAssingedEdges.add(tmpSetEdges);
			}

			// add remaining edges
			int iEdgeId = (numberOfThreads - 1) * iNoOfEdgesPerThread;
			IntSet spareSetEdges = new DefaultIntSet(iNoOfEdges - iEdgeId);
			spareSetEdges.add(iEdgeId);
			while (iEdgeId < iNoOfEdges) {
				iEdgeId++;
				spareSetEdges.add(iEdgeId);
			}
			lstAssingedEdges.add(spareSetEdges);
		}

		return lstAssingedEdges;
	}

}
