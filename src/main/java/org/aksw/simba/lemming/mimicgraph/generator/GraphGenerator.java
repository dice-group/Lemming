package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourselection.ClassProposal;
import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
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
 * This class is responsible for the general graph generation process
 * 
 * 
 * @author Ana Silva
 *
 */
@Component("Binary")
@Scope(value = "prototype")
public class GraphGenerator implements IGraphGenerator{

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
	@Override
	public void initializeMimicGraph(ColouredGraph mimicGraph, int noOfThreads) {
		// get set of edges each thread will process
		List<IntSet> lstAssignedEdges = getColouredEdgesForConnecting(noOfThreads);
		ExecutorService service = Executors.newFixedThreadPool(noOfThreads);
		LOGGER.info("Creating {} threads for processing graph generation!", lstAssignedEdges.size());

		// keep track of failed colours for all threads
		Set<BitSet> failedEdgeColours = ConcurrentHashMap.newKeySet();

		// iterate each set of edges and assign to a thread
		Set<BitSet> setAvailableVertexColours = graphInitializer.getAvailableVertexColours();
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		for (int i = 0; i < lstAssignedEdges.size(); i++) {
			final IntSet setOfEdges = lstAssignedEdges.get(i);
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					int curEdgeIterations = Constants.MAX_EXPLORING_TIME;
					int[] arrOfEdges = setOfEdges.toIntArray();

					// iterate through assigned edges
					for (int j = 0; j < arrOfEdges.length;) {
						// get an edge id
						int fakeEdgeId = arrOfEdges[j];

						// skip if we previously failed to find a triple for this edge colour
						BitSet edgeColour = graphInitializer.getEdgeColour(fakeEdgeId);
						if (edgeColour == null || failedEdgeColours.contains(edgeColour)) {
							j++;
							continue;
						}

						// get tail and head colour proposers from edge colour with n attempts
						ClassProposal proposal = classSelector.getProposal(edgeColour, fakeEdgeId, 1000,
								setAvailableVertexColours);
						BitSet tailColour = proposal.getTailColour();
						BitSet headColour = proposal.getHeadColour();

						// if it's still invalid, skip the edge colour entirely for next times
						if (tailColour == null || headColour == null) {
							LOGGER.error("Could not find valid tail and head colours for {} edge colour.", edgeColour);
							failedEdgeColours.add(edgeColour);
							j++;
							continue;
						}

						// get instance proposers
						IOfferedItem<Integer> tailProposer = vertexSelector.getProposedVertex(edgeColour, tailColour,
								VERTEX_TYPE.TAIL);
						IOfferedItem<Integer> headProposer = vertexSelector.getProposedVertex(edgeColour, headColour,
								VERTEX_TYPE.HEAD);

						// get instances from proposers
						int maxAttempts = 1000;
						boolean isFoundVerticesConnected = false;
						for (int i = 0; i < maxAttempts; i++) {
							// get candidate tail, skip if null
							Integer tailId = tailProposer.getPotentialItem();
							if (tailId == null)
								continue;

							// get candidate head filtered by the existing connections, skip if null
							Set<Integer> connectedHeads = graphInitializer.getConnectedHeadsSet(tailId, edgeColour);
							Integer headId = headProposer.getPotentialItemRemove(connectedHeads);
							if (headId == null)
								continue;

							// connect instances if possible and break from it
							isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColour, mimicGraph);
							if (isFoundVerticesConnected) {
								break;
							}
						}

						// if it failed to connect, and we ran out of iterations, add to failed colours
						// and move on
						if (isFoundVerticesConnected) {
							// reset iterations and advance
							curEdgeIterations = Constants.MAX_EXPLORING_TIME;
							j++;
						} else {
							curEdgeIterations--;
							if (curEdgeIterations == 0) {
								LOGGER.error("Could not create edges with the {} colour since it could not find any "
										+ "approriate vertices to connect.", edgeColour);
								failedEdgeColours.add(edgeColour);
								curEdgeIterations = Constants.MAX_EXPLORING_TIME;
								j++;
							}
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
	}

	public synchronized boolean connectIfPossible(int tailId, int headId, BitSet eColo, ColouredGraph mimicGraph) {
		if (connectableVertices(tailId, headId, eColo, graphInitializer)) {
			mimicGraph.addEdge(tailId, headId, eColo);
			return true;
		}
		return false;
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


	@Override
	public TripleBaseSingleID getProposedTriple() {		
		int max = 1000;
		for (int j = 0; j < max; j++) {

			// get proposed edge colour
			BitSet edgeColour = classSelector.getEdgeColourProposal();

			// let if fall if edgeColour is null, there's something really wrong if it's
			// null

			// get tail and head colour proposers from edge colour with n attempts
			Set<BitSet> availableColours = graphInitializer.getAvailableVertexColours();
			ClassProposal proposal = classSelector.getProposal(edgeColour, -1, 1000, availableColours);
			if (proposal == null)
				continue;
			BitSet tailColour = proposal.getTailColour();
			BitSet headColour = proposal.getHeadColour();

			// get instance proposers
			IOfferedItem<Integer> tailProposer = vertexSelector.getProposedVertex(edgeColour, tailColour,
					VERTEX_TYPE.TAIL);
			IOfferedItem<Integer> headProposer = vertexSelector.getProposedVertex(edgeColour, headColour,
					VERTEX_TYPE.HEAD);

			// get instances from proposers
			int maxAttempts = 1000;
			for (int i = 0; i < maxAttempts; i++) {
				// get candidate tail, skip if null
				Integer tailId = tailProposer.getPotentialItem();
				if (tailId == null)
					continue;

				// get candidate head filtered by the existing connections, skip if null
				Set<Integer> connectedHeads = graphInitializer.getConnectedHeadsSet(tailId, edgeColour);
				Integer headId = headProposer.getPotentialItemRemove(connectedHeads);
				if (headId == null)
					continue;

				// check if they can connect
				if (connectableVertices(tailId, headId, edgeColour, graphInitializer)) {
					TripleBaseSingleID triple = new TripleBaseSingleID();
					triple.tailId = tailId;
					triple.tailColour = tailColour;
					triple.headId = headId;
					triple.headColour = headColour;
					triple.edgeColour = edgeColour;
					return triple;
				}
			}
		}
		return null;
	}

}
