package org.aksw.simba.lemming.creation;

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
import java.util.concurrent.atomic.AtomicInteger;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.InMemoryPalette;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgEdgeColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgVertColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.constraints.ColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class is responsible for initializing the mimicked graph based on the
 * input graphs. It copies the colouring and computes the colour distributions
 * to be used.
 * 
 * @author Ana Silva
 */
@Scope(value = "prototype")
public class GraphInitializer {
	// logging object
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphInitializer.class);

	// vertex and edge colours distribution
	private ObjectDistribution<BitSet> vertexColourDist;
	private ObjectDistribution<BitSet> edgeColourDist;

	// class to vertices map and reverse
	private Map<BitSet, Integer> mapClassVertices;
	private Map<Integer, BitSet> reversedMapClassVertices;

	private Map<BitSet, Map<Integer, IntSet>> mapEdgeColoursToConnectedVertices;

	// vertex and edge colours to IDs maps
	private Map<BitSet, IntSet> mapColourToVertexIDs;
	private Map<BitSet, IntSet> mapColourToEdgeIDs;

	// edge IDs to edge colour map
	private Map<Integer, BitSet> mapEdgeIdsToColour;

	// rdf edge colour
	private BitSet rdfTypePropertyColour;

	// colouring rules
	private IColourMappingRules colourMapper;

	// restricted edge colours
	private Set<BitSet> setOfRestrictedEdgeColours;

	// desired number of edges
	private int desiredNoOfEdges;

	// seed for rng
	private long seed;

	/**
	 * Constructor.
	 * 
	 * @param seed
	 */
	public GraphInitializer(long seed) {
		this.seed = seed;
		mapEdgeIdsToColour = new HashMap<Integer, BitSet>();
		mapClassVertices = new HashMap<BitSet, Integer>();
		reversedMapClassVertices = new HashMap<Integer, BitSet>();
		mapEdgeColoursToConnectedVertices = new HashMap<BitSet, Map<Integer, IntSet>>();
		mapColourToVertexIDs = new ConcurrentHashMap<BitSet, IntSet>();
		mapColourToEdgeIDs = new ConcurrentHashMap<BitSet, IntSet>();
		colourMapper = new ColourMappingRules();
		setOfRestrictedEdgeColours = new HashSet<BitSet>();
	}

	/**
	 * Initializes the synthetic graph by copying the colour scheme from the input
	 * graphs, compute the vertices and edge colour distribution.
	 * 
	 * @param origGrphs    The input graphs
	 * @param noOfVertices The desired number of vertices
	 * @param noOfThreads  The number of threads
	 * 
	 * @return The initialized synthetic graph
	 */
	public ColouredGraph initialize(ColouredGraph[] origGrphs, int noOfVertices, int noOfThreads) {
		// copy colour scheme from input graphs to the synthetic graph
		ColouredGraph mimicGraph = new ColouredGraph();
		copyColourPalette(origGrphs, mimicGraph);
		colourMapper.analyzeRules(origGrphs);
		rdfTypePropertyColour = mimicGraph.getRDFTypePropertyColour();

		// compute average distribution of vertex's and edge's colors
		vertexColourDist = AvrgVertColoDistMetric.apply(origGrphs);
		edgeColourDist = AvrgEdgeColoDistMetric.apply(origGrphs);

		// estimate # edges from # vertices and average degree of input graphs
		desiredNoOfEdges = estimateNoEdges(origGrphs, noOfVertices);

		// assign colors to vertices
		paintVertices(mimicGraph, noOfVertices);

		// TODO remove it from here, this is only needed for BCS, not UIS or CCS
		// mEdgeColoProposer = new OfferedItemByRandomProb<>(mEdgeColoDist,
		// mSetOfRestrictedEdgeColours, seed);

		// assign colors to edges
		paintEdges(mimicGraph, noOfThreads);

		// TODO move this to the final stage instead, ignore RDF edges completely
		// defining type of resource vertices
		connectVerticesWithRDFTypeEdges(mimicGraph);

		return mimicGraph;
	}

	protected void copyColourPalette(ColouredGraph[] origGraphs, ColouredGraph mimicGraph) {
		if (Constants.IS_EVALUATION_MODE) {
			ColourPalette newVertexPalette = new InMemoryPalette();
			ColourPalette newEdgePalette = new InMemoryPalette();
			ColourPalette newDTEdgePalette = new InMemoryPalette();

			// copy colour palette of all the original graphs to the new one
			for (ColouredGraph grph : origGraphs) {
				if (grph != null) {
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

	private void paintVertices(ColouredGraph mimicGraph, int noVertices) {
		LOGGER.info("Assign colors to vertices.");
		IOfferedItem<BitSet> colorProposer = new OfferedItemByRandomProb<BitSet>(vertexColourDist, seed);
		for (int i = 0; i < noVertices; i++) {
			BitSet offeredColor = (BitSet) colorProposer.getPotentialItem();
			int vertId = mimicGraph.addVertex(offeredColor);
			IntSet setVertIDs = mapColourToVertexIDs.get(offeredColor);
			if (setVertIDs == null) {
				setVertIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mapColourToVertexIDs.put(offeredColor, setVertIDs);
			}
			setVertIDs.add(vertId);
		}

		/*
		 * get restricted edge's colours can exist along with these created vertex's
		 * colours
		 */
		Set<BitSet> setVertColours = mapColourToVertexIDs.keySet();
		for (BitSet tailColo : setVertColours) {
			for (BitSet headColo : setVertColours) {
				Set<BitSet> lstPossEdgeColours = colourMapper.getPossibleLinkingEdgeColours(tailColo, headColo);
				if (lstPossEdgeColours != null && lstPossEdgeColours.size() > 0) {
					for (BitSet edgeColo : lstPossEdgeColours) {
						setOfRestrictedEdgeColours.add(edgeColo);
					}
				}
			}
		}
	}

	private void paintEdges(ColouredGraph mimicGraph, int mNumberOfThreads) {

		List<IntSet> lstAssignedEdges = getLstTransparenEdgesForPainting(mimicGraph, mNumberOfThreads);

		/*
		 * assign edges to each thread and run
		 */

		ExecutorService service = Executors.newFixedThreadPool(mNumberOfThreads);

		LOGGER.info("Create " + lstAssignedEdges.size() + " threads for painting edges !");

		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		final Set<BitSet> setOfRestrictedEdgeColours = new HashSet<BitSet>(this.setOfRestrictedEdgeColours);
		setOfRestrictedEdgeColours.remove(rdfTypePropertyColour);

		if (setOfRestrictedEdgeColours.size() == 0) {
			LOGGER.error("Cound not find any edge colour except rdf:type edges!");
			return;
		}

		// concurrent hash map shared between multi-threads
		// final Map<BitSet, IntSet> mapColouredEdgeIds = new ConcurrentHashMap<BitSet,
		// IntSet>();
		final Map<BitSet, AtomicInteger> mapEdgeColourCounter = new HashMap<BitSet, AtomicInteger>();

		for (BitSet eColo : setOfRestrictedEdgeColours) {
			mapEdgeColourCounter.put(eColo, new AtomicInteger());
		}

		for (int i = 0; i < lstAssignedEdges.size(); i++) {
			final IntSet setOfEdges = lstAssignedEdges.get(i);
			final int indexOfThread = i + 1;

			final IOfferedItem<BitSet> eColoProposer = new OfferedItemByRandomProb<>(
					new ObjectDistribution<BitSet>(edgeColourDist.sampleSpace, edgeColourDist.values), seed);
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					// set of edges for painting
					int[] arrOfEdges = setOfEdges.toIntArray();

					// LOGGER.info("Thread " + indexOfThread +" is painting " + arrOfEdges.length +"
					// edges with "
					// + setOfRestrictedEdgeColours.size()+" colours... ");
					int j = 0;

					while (j < arrOfEdges.length) {
						BitSet offeredColor = (BitSet) eColoProposer.getPotentialItem(setOfRestrictedEdgeColours, true);

						if (offeredColor == null) {
							LOGGER.warn("Skip edge " + arrOfEdges[j]);
							j++;
							continue;
						}

						/**
						 * not add edge with the offered color to the graph since we have to determine
						 * the head and tail for the connection ==> just track the edge's color
						 */
						AtomicInteger counter = mapEdgeColourCounter.get(offeredColor);
						counter.incrementAndGet();
						j++;
					}
				}
			};
			tasks.add(Executors.callable(worker));
		}

		try {
			service.invokeAll(tasks);
			service.shutdown();
			service.awaitTermination(48, TimeUnit.HOURS);
			LOGGER.info("All threads are finised --> Copy result back to map");

			/*
			 * copy back to global variable
			 */
			Set<BitSet> setEdgeColours = mapEdgeColourCounter.keySet();
			int fakeEdgeID = 0;
			for (BitSet eColo : setEdgeColours) {
				int j = 0;
				AtomicInteger counter = mapEdgeColourCounter.get(eColo);
				while (j < counter.get()) {
					mapEdgeIdsToColour.put(fakeEdgeID, eColo);
					IntSet setEdges = mapColourToEdgeIDs.get(eColo);
					if (setEdges == null) {
						setEdges = new DefaultIntSet(Constants.DEFAULT_SIZE);
						mapColourToEdgeIDs.put(eColo, setEdges);
					}
					setEdges.add(fakeEdgeID);
					j++;
					fakeEdgeID++;
				}
			}

		} catch (InterruptedException e) {
			LOGGER.error("Could not shutdown the service executor!");
			e.printStackTrace();
		}
	}

	private List<IntSet> getLstTransparenEdgesForPainting(ColouredGraph mMimicGraph, int mNumberOfThreads) {
		/*
		 * calculate number of [rdf:type] edges first. these edges will be used to
		 * define classes of resources in vertices.
		 */
		int iNumberOfRdfTypeEdges = 0;
		Set<BitSet> setVertexColours = mapColourToVertexIDs.keySet();
		for (BitSet vColo : setVertexColours) {
			Set<BitSet> definedColours = mMimicGraph.getClassColour(vColo);
			IntSet setOfVertices = mapColourToVertexIDs.get(vColo);
			if (definedColours != null) {
				iNumberOfRdfTypeEdges += definedColours.size() * setOfVertices.size();
			}
		}

		LOGGER.info("There are " + iNumberOfRdfTypeEdges + " edges of rdf:type!");

		/*
		 * process normal edges
		 */
		int iNumberOfOtherEdges = desiredNoOfEdges;
		desiredNoOfEdges += iNumberOfRdfTypeEdges;

		LOGGER.info("Assigning colours to " + iNumberOfOtherEdges + " .......");

		int iNoOfEdgesPerThread = 0;
		int iNoOfSpareEdges = 0;
		if (iNumberOfOtherEdges % mNumberOfThreads == 0) {
			iNoOfEdgesPerThread = iNumberOfOtherEdges / mNumberOfThreads;
		} else {
			iNoOfEdgesPerThread = iNumberOfOtherEdges / (mNumberOfThreads - 1);
			iNoOfSpareEdges = iNumberOfOtherEdges - (iNoOfEdgesPerThread * (mNumberOfThreads - 1));
		}

		List<IntSet> lstAssignedEdges = new ArrayList<IntSet>();

		if (iNoOfSpareEdges == 0) {
			for (int i = 0; i < mNumberOfThreads; i++) {
				IntSet tmpSetEdges = new DefaultIntSet(iNoOfEdgesPerThread);
				for (int j = 0; j < iNoOfEdgesPerThread; j++) {
					int iEdgeId = (i * iNoOfEdgesPerThread) + j;
					tmpSetEdges.add(iEdgeId);
				}
				lstAssignedEdges.add(tmpSetEdges);
			}
		} else {
			for (int i = 0; i < mNumberOfThreads - 1; i++) {
				IntSet tmpSetEdges = new DefaultIntSet(iNoOfEdgesPerThread);
				for (int j = 0; j < iNoOfEdgesPerThread; j++) {
					int iEdgeId = (i * iNoOfEdgesPerThread) + j;
					tmpSetEdges.add(iEdgeId);
				}
				lstAssignedEdges.add(tmpSetEdges);
			}

			// add remaining edges
			int iEdgeId = (mNumberOfThreads - 1) * iNoOfEdgesPerThread;
			IntSet spareSetEdges = new DefaultIntSet(iNumberOfOtherEdges - iEdgeId);
			spareSetEdges.add(iEdgeId);
			while (iEdgeId < iNumberOfOtherEdges) {
				iEdgeId++;
				spareSetEdges.add(iEdgeId);
			}
			lstAssignedEdges.add(spareSetEdges);
		}

		return lstAssignedEdges;
	}

	/**
	 * connection typed resource vertices to its class with edge of rdf:type if a
	 * vertex has a colour, then it connect to some vertices with rdf:type edges.
	 * the number of connected heads is dependent on the number of colour the target
	 * has
	 */
	private void connectVerticesWithRDFTypeEdges(ColouredGraph mMimicGraph) {

		Random mRandom = new Random(seed);

		/*
		 * filter colour and empty colour vertices
		 */
		Set<BitSet> setVertexColours = mapColourToVertexIDs.keySet();
		IntSet colourVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
		IntSet emptyColourVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
		for (BitSet vColo : setVertexColours) {
			IntSet setVertices = mapColourToVertexIDs.get(vColo);

			if (vColo.isEmpty()) {
				// get vertices with empty colours
				emptyColourVertices.addAll(setVertices);
			} else {
				// get vertices with non-empty colour
				colourVertices.addAll(setVertices);
			}
		}

		int[] arrColourVertices = colourVertices.toIntArray();
		int[] arrEmptyColourVertices = emptyColourVertices.toIntArray();
		Set<Integer> trackedClassVertices = new HashSet<Integer>();
		// traverse through all coloured vertices and add classes to them
		for (int vId : arrColourVertices) {
			BitSet vColo = mMimicGraph.getVertexColour(vId);
			Set<BitSet> setClassColours = mMimicGraph.getClassColour(vColo);

			for (BitSet classColo : setClassColours) {
				if (!mapClassVertices.containsKey(classColo)) {
					int hId = arrEmptyColourVertices[mRandom.nextInt(arrEmptyColourVertices.length)];

					while (trackedClassVertices.contains(hId)
							&& trackedClassVertices.size() < arrEmptyColourVertices.length) {
						hId = arrEmptyColourVertices[mRandom.nextInt(arrEmptyColourVertices.length)];
					}
					if (trackedClassVertices.size() <= arrEmptyColourVertices.length) {
						trackedClassVertices.add(hId);
						mapClassVertices.put(classColo, hId);
						reversedMapClassVertices.put(hId, classColo);
						// connect the vId and hId using the edge rdf:type
						mMimicGraph.addEdge(vId, hId, rdfTypePropertyColour);
					} else {
						LOGGER.warn("Cannot find any empty colour head for consideration as a class of resources!");
					}
				} else {
					int hId = mapClassVertices.get(classColo);
					// connect the vId and hId using the edge rdf:type
					mMimicGraph.addEdge(vId, hId, rdfTypePropertyColour);
				}
			}
		}
	}

	public Map<Integer, BitSet> getmReversedMapClassVertices() {
		return reversedMapClassVertices;
	}

	public Map<BitSet, Map<Integer, IntSet>> getmMapEdgeColoursToConnectedVertices() {
		return mapEdgeColoursToConnectedVertices;
	}

	public Map<BitSet, Integer> getmMapClassVertices() {
		return mapClassVertices;
	}

	public Map<Integer, BitSet> getmMapEdgeIdsToColour() {
		return mapEdgeIdsToColour;
	}

	public Map<BitSet, IntSet> getmMapColourToVertexIDs() {
		return mapColourToVertexIDs;
	}

	public Map<BitSet, IntSet> getmMapColourToEdgeIDs() {
		return mapColourToEdgeIDs;
	}

	public BitSet getmRdfTypePropertyColour() {
		return rdfTypePropertyColour;
	}

	public int getActualNumberOfEdges() {
		return mapEdgeIdsToColour.size();
	}

	public Set<BitSet> getAvailableVertexColours() {
		return new HashSet<BitSet>(mapColourToVertexIDs.keySet());
	}

	public BitSet getEdgeColour(int fakeEdgeId) {
		return mapEdgeIdsToColour.get(fakeEdgeId);
	}
}
