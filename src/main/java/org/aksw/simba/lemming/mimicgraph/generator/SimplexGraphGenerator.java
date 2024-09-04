package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;
import org.aksw.simba.lemming.mimicgraph.colourselection.ClassProposal;
import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector.VERTEX_TYPE;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.simplexes.analysis.SimplexAnalysis;
import org.aksw.simba.lemming.simplexes.distribution.IPropertyDist;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexClass;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexProperty;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

/**
 * This class is responsible for the graph generation process of the
 * simplex-based approaches.
 *
 */
@Component("Simplex")
@Scope(value = "prototype")
public class SimplexGraphGenerator implements IGraphGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimplexGraphGenerator.class);

	/** Initializes the graph. Serves as a store. */
	private SimplexGraphInitializer initializer;

	/** Class Selector instance. Dictates the class sampling strategy. */
	private ISimplexClass simplexClass;

	/** Property Selector instance. Dictates the property sampling strategy. */
	private ISimplexProperty simplexProperty;

	/**
	 * Class Selector. Only used in the optimization phase of simplex-based
	 * approaches
	 */
	private IClassSelector classSelector;

	/**
	 * Vertex Selector. Only used in the optimization phase of simplex-based
	 * approaches
	 */
	private IVertexSelector vertexSelector;

	/** Random sequence generator object */
	private Random mRandom;

	/**
	 * Constructor.
	 * 
	 * @param initializer
	 * @param simplexClass
	 * @param simplexProperty
	 * @param classSelector
	 * @param vertexSelector
	 */
	public SimplexGraphGenerator(SimplexGraphInitializer initializer, ISimplexClass simplexClass,
			ISimplexProperty simplexProperty, IClassSelector classSelector, IVertexSelector vertexSelector) {
		this.initializer = initializer;
		this.simplexClass = simplexClass;
		this.simplexProperty = simplexProperty;
		this.classSelector = classSelector;
		this.vertexSelector = vertexSelector;
		mRandom = new Random(initializer.getSeedGenerator().getNextSeed());
	}

	@Override
	public void initializeMimicGraph(ColouredGraph mimicGraph, int noOfThreads) {
		SimplexAnalysis analysis = initializer.getSimplexAnalysis();
		initializer.setmTriColosCountsAvgProb(simplexClass.getTriangleDistribution().getmTriangleColorsv1v2v3());
//		ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> mTriColosCountsAvgProb = initializer
//				.getmTriColosCountsAvgProb();

		// *** 2-simplex creation (that could be connected to each other) ***
		LOGGER.debug("Case 1: Model higher dimensional simplexes with 2-simplexes");
		int estimatedEdgesTriangle = analysis.getConnTriAnalysis().getEstEdges();
		int estimatedVerticesTriangle = analysis.getConnTriAnalysis().getEstVertices();
		LOGGER.debug("Estimated Edges: " + estimatedEdgesTriangle);
		LOGGER.debug("Estimated Vertices: " + estimatedVerticesTriangle);

		// get random triangle
		IOfferedItem<TriColours> initialProposer = simplexClass.getTriangleProposal();
		int maximumIteration = initializer.getMaximumNoIterations();

		// Variable to track number of edges added to mimic graph in previous iteration.
		// Note: This variable is used to stop the iteration if no new edges could be
		// added to the mimic graph after trying for predefined number of iterations
		int numOfIterationAddingEdgesToGraph = 0;

		int desiredVertices = initializer.getDesiredNoOfVertices();

		try (ProgressBar vertsPB = new ProgressBarBuilder().setTaskName("#vertices").setInitialMax(desiredVertices)
				.setConsumer(new DelegatingProgressBarConsumer(LOGGER::info)).build()) {
			vertsPB.setExtraMessage("Case 1: Model higher dimensional simplexes with 2-simplexes");
			if ((initialProposer != null) && (estimatedEdgesTriangle >= 3) && (estimatedVerticesTriangle >= 3)) {
				// Variables to track number of edges and vertices added in triangle
				int actualEdgesInTriangles = 0;
				int actualVerticesInTriangles = 0;

				TriColours initialTriangle = initialProposer.getPotentialItem();

				// Variable to track edges that cannot form triangle
				// IntSet edgesNotFormingTriangle = new DefaultIntSet(Constants.DEFAULT_SIZE);
				Set<EdgeColorsSorted> edgeColosSet = new HashSet<EdgeColorsSorted>();
				edgeColosSet.add(new EdgeColorsSorted(initialTriangle.getA(), initialTriangle.getB()));
				edgeColosSet.add(new EdgeColorsSorted(initialTriangle.getA(), initialTriangle.getC()));
				edgeColosSet.add(new EdgeColorsSorted(initialTriangle.getB(), initialTriangle.getC()));

				// Variable to track set of triangle added to the mimic graph (i.e. set of
				// Colors of the vertices forming the triangle)
				Set<TriColours> setTriangleColorsMimicGraph = new HashSet<TriColours>();

				// add the selected triangle to mimic graph
				addTriangleToMimicGraph(mimicGraph, initialTriangle,
						analysis.getConnTriAnalysis().getmColourMapperSimplexes(),
						initializer.getmMapColourToVertexIDs2Simplex(), initializer.getmMapColourToEdgeIDs2Simplex(),
						initializer.getmTriangleColorsVertexIds(), simplexProperty.getmPropDistConnTri());
				
				

				vertsPB.stepBy(3);
				actualVerticesInTriangles += 3;
				actualEdgesInTriangles += 3;
				setTriangleColorsMimicGraph.add(initialTriangle);

				numOfIterationAddingEdgesToGraph = 0;
				while (actualEdgesInTriangles < estimatedEdgesTriangle) {
					if ((actualVerticesInTriangles < estimatedVerticesTriangle)
							&& (numOfIterationAddingEdgesToGraph < maximumIteration)) {
						// If we can add more triangles when we are allowed to include additional
						// vertices otherwise edges need to be added to existing triangles

						if (edgeColosSet.size() != 0) {
							EdgeColorsSorted potentialItem = simplexClass.getEdgeProposalFromTriangleDist(edgeColosSet);
							if (potentialItem == null) {
								numOfIterationAddingEdgesToGraph++;
								continue;
							}

							// get vertex colours that connect this face
							BitSet selectedVertex1Colo = potentialItem.getA();
							BitSet selectedVertex2Colo = potentialItem.getB();

							// Get the color for the third vertex
							BitSet proposedVertex3Colo = simplexClass.proposeVertex3Colour(selectedVertex1Colo,
									selectedVertex2Colo);

							// Add new Triangle for the selected vertices
							boolean newEdgesNotAddedToTriangle = true;
							if (proposedVertex3Colo != null) {
								// If third vertex color is proposed, create a triangle with it
								TriColours newPossibleTriangle = new TriColours(selectedVertex1Colo,
										selectedVertex2Colo, proposedVertex3Colo);

								// create vertex for the proposed color
								int proposedVertId = addVertexToMimicGraph(mimicGraph, proposedVertex3Colo,
										initializer.getmMapColourToVertexIDs2Simplex());

								// select vertex instances
								// TODO change this to UIS methods
								int selectedVertex1 = getProposedVertex(initializer.getmMapColourToVertexIDs2Simplex(),
										selectedVertex1Colo);
								int selectedVertex2 = getProposedVertex(initializer.getmMapColourToVertexIDs2Simplex(),
										selectedVertex2Colo);

								// add edges among selected vertices and proposed color
								// Note: Ideally properties should exist among them. since they were also
								// forming a triangle in input graphs
								simplexProperty.addEdgeToMimicGraph(mimicGraph, selectedVertex2Colo, proposedVertex3Colo,
										selectedVertex2, proposedVertId, initializer.getmMapColourToEdgeIDs2Simplex(),
										analysis.getConnTriAnalysis().getmColourMapperSimplexes(), newPossibleTriangle,
										simplexProperty.getmPropDistConnTri());

								// increment number of vertices and edges added in the mimic graph for triangles
								actualVerticesInTriangles = actualVerticesInTriangles + 1;
								actualEdgesInTriangles = actualEdgesInTriangles + 2;
								vertsPB.stepBy(1);

								// Add the created triangle Colors to set
								setTriangleColorsMimicGraph.add(newPossibleTriangle);

								edgeColosSet.add(new EdgeColorsSorted(selectedVertex1Colo, proposedVertex3Colo));
								edgeColosSet.add(new EdgeColorsSorted(selectedVertex2Colo, proposedVertex3Colo));

								// Track the triangle colors along with vertex ids
								updateMapTriangleColorsVertices(selectedVertex1, selectedVertex2, proposedVertId,
										newPossibleTriangle, initializer.getmTriangleColorsVertexIds());

								// update the boolean variable
								newEdgesNotAddedToTriangle = false;

								numOfIterationAddingEdgesToGraph = 0;

							}
							if (newEdgesNotAddedToTriangle) {
								numOfIterationAddingEdgesToGraph++;
								if (numOfIterationAddingEdgesToGraph == 5) { // FIXME
									edgeColosSet.remove(potentialItem);
									numOfIterationAddingEdgesToGraph = 0;
								}
							}

						} // end if condition - check if triangles can be added to the edges
						else {
							vertsPB.setExtraMessage("Growing 2-simplexes not possible.... Proposing new 2-simplex");

							// If no candidate edges exist then new random triangle should be added to the
							// mimic graph
							IOfferedItem<TriColours> randomProposer = simplexClass.getTriangleProposal();
							if (randomProposer == null) {
								numOfIterationAddingEdgesToGraph++;
								continue;
							}
							TriColours randomTriangle = randomProposer.getPotentialItem();

							// Update the triangle count
//							arrNewTriProbCount[3] = arrNewTriProbCount[3] - 1;
							// Add the triangle to mimic graph
							addTriangleToMimicGraph(mimicGraph, randomTriangle,
									analysis.getConnTriAnalysis().getmColourMapperSimplexes(),
									initializer.getmMapColourToVertexIDs2Simplex(),
									initializer.getmMapColourToEdgeIDs2Simplex(),
									initializer.getmTriangleColorsVertexIds(), simplexProperty.getmPropDistConnTri());

							// increment s
							actualVerticesInTriangles += 3;
							actualEdgesInTriangles += 3;
							vertsPB.stepBy(3);

							edgeColosSet.add(new EdgeColorsSorted(randomTriangle.getA(), randomTriangle.getB()));
							edgeColosSet.add(new EdgeColorsSorted(randomTriangle.getA(), randomTriangle.getC()));
							edgeColosSet.add(new EdgeColorsSorted(randomTriangle.getB(), randomTriangle.getC()));

							// Add the triangle colors to set variable
							setTriangleColorsMimicGraph.add(new TriColours(randomTriangle.getA(), randomTriangle.getB(),
									randomTriangle.getC()));
							numOfIterationAddingEdgesToGraph = 0;
						}
					} else {
						break; // Cannot add more vertices
					}
				} // end while condition checking if actual number of edges is less than estimated
					// number of edges
				LOGGER.debug("Growing 2-simplexes phase completed");
				LOGGER.debug("Added Edges: " + actualEdgesInTriangles);
				LOGGER.debug("Added Vertices: " + actualVerticesInTriangles);

				vertsPB.setExtraMessage("Adding additional Edges to created 2-simplexes");

				numOfIterationAddingEdgesToGraph = 0; // initialize iteration count
				while ((actualEdgesInTriangles < estimatedEdgesTriangle)
						&& (numOfIterationAddingEdgesToGraph < maximumIteration)) { // Case: Triangles
																					// cannot be added
																					// to
					// the mimic graph but edges can be
					// added to existing triangles

					if (setTriangleColorsMimicGraph.size() != 0) {

						// Logic for adding edges to existing triangles
						TriColours proposeTriangleToAddEdge = simplexClass
								.proposeTriangleToAddEdge(setTriangleColorsMimicGraph);

						// Best case: A triangle is returned
						List<IntSet> selectedTrianglesList = initializer.getmTriangleColorsVertexIds()
								.get(proposeTriangleToAddEdge);

						// randomly selecting one of these triangles
						IntSet selectedVertices = selectedTrianglesList
								.toArray(new IntSet[selectedTrianglesList.size()])[mRandom
										.nextInt(selectedTrianglesList.size())];

						// Considering different vertex pairs to add an edge

						// Convert vertices to Array
						Integer[] vertexIDExistingTriangle = selectedVertices
								.toArray(new Integer[selectedVertices.size()]);

						if (vertexIDExistingTriangle.length < 3) {
							numOfIterationAddingEdgesToGraph++;
							continue;
						}

						// creating different pairs of combinations for 3 vertices of a triangle
						List<List<Integer>> differentPairsOfVertices = new ArrayList<List<Integer>>();
						differentPairsOfVertices
								.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[1]));
						differentPairsOfVertices
								.add(Arrays.asList(vertexIDExistingTriangle[1], vertexIDExistingTriangle[2]));
						differentPairsOfVertices
								.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[2]));

						for (List<Integer> pairOfVertices : differentPairsOfVertices) {

							// Get vertex colours for a pair
							BitSet existingVertexColo1 = mimicGraph.getVertexColour(pairOfVertices.get(0));
							BitSet existingVertexColo2 = mimicGraph.getVertexColour(pairOfVertices.get(1));

							boolean edgeAdded = simplexProperty.addEdgeInAnyDirection(mimicGraph,
									existingVertexColo1, existingVertexColo2, pairOfVertices.get(0),
									pairOfVertices.get(1), initializer.getmMapColourToEdgeIDs2Simplex(),
									analysis.getConnTriAnalysis().getmColourMapperSimplexes(),
									simplexProperty.getmPropDistConnTri());

							if (edgeAdded) {
								// increment no. of edges in triangle
								actualEdgesInTriangles = actualEdgesInTriangles + 1;
								numOfIterationAddingEdgesToGraph = 0;// update count to 0 since edge was added
																		// successfully
								// break iterating over pair of vertices, since an edge is found
								break;
							} else {
								numOfIterationAddingEdgesToGraph++;
							}

						} // end iterating over vertex pairs

					} // end if condition - check if candidate triangles exists for adding edges to
						// them
					else {

						// No edges can be added to existing triangles
						// Give warning and break while loop
						LOGGER.warn(
								"Not able to add edges to existing triangles and add new triangles to the mimic graph. Estimated number is greater than actual number of edges! The process of adding triangle ends. ");
						break; // terminate out of while condition

					}

				} // end else condition adding edges to existing triangles
			} // end if condition - initial random triangle is not null
			LOGGER.debug("Case 1 completed!");
			LOGGER.debug("Added Edges: " + mimicGraph.getEdges().size());
			LOGGER.debug("Added Vertices: " + mimicGraph.getVertices().size());

			// ************************ Logic for isolated 1-simplexes
			// ***********************************//

			// temporary variables to track addition of vertices and edges for 1-simplexes
			int actualVerticesSimplexes = 0;
			int actualEdgesSimplexes = 0;

			// initialize variable tracking iteration count for this case
			numOfIterationAddingEdgesToGraph = 0;

			// get head proposer defined for 1-simplex distribution
			IOfferedItem<EdgeColos> potentialEdgeColoProposer = simplexClass.getIsolatedEdgeProposer();
//				s1ConnDist
//				.getPotentialIsolatedEdgeColoProposer();

			int estimatedEdges1Simplexes = analysis.getIsoS1Analysis().getEstEdges();
			int estimatedVertices1Simplexes = analysis.getIsoS1Analysis().getEstVertices();
			vertsPB.setExtraMessage("Case 2a: Isolated 1-simplexes (with different source and target node)");
			LOGGER.debug("Estimated Edges: " + estimatedEdges1Simplexes);
			LOGGER.debug("Estimated Vertices: " + estimatedVertices1Simplexes);

			while ((estimatedEdges1Simplexes > actualEdgesSimplexes) && (potentialEdgeColoProposer != null)
					&& (numOfIterationAddingEdgesToGraph < maximumIteration)) { // additional condition that color
																				// proposer
																				// should not be null
				// check until it is possible to add more edges for 1-simplexes

				// variable to track if new vertex is not added
				// boolean newVertexNotAdded = true;

				EdgeColos proposedEdgeColor = potentialEdgeColoProposer.getPotentialItem();

				// get potential head for it and create vertex
				BitSet potentialheadColo = proposedEdgeColor.getA();

				// Get potential tail color and add edge for it
				BitSet potentialTailColo = proposedEdgeColor.getB();

				// temporary assignment for head vertex id
				int vertexIDHead = -1;
				// temporary assignment for tail vertex id
				int vertexIDTail = -1;

				// check if vertex can be added or existing vertex should be selected
				if ((estimatedVertices1Simplexes - actualVerticesSimplexes) >= 2) {
					// add a vertex if enough vertices are not created for 1-simplexes
					vertexIDHead = addVertexToMimicGraph(mimicGraph, potentialheadColo,
							initializer.getmMapColourToVertexIDs1Simplex());
					vertexIDTail = addVertexToMimicGraph(mimicGraph, potentialTailColo,
							initializer.getmMapColourToVertexIDs1Simplex());
					actualVerticesSimplexes = actualVerticesSimplexes + 2;
					vertsPB.stepBy(2);

					// newVertexNotAdded = false;//set the variable to false, since new variable is
					// added

				} else {
					// New vertex cannot be created, select an existing vertex of the potential head
					// color at random
					IntSet vertexIDshead = initializer.getmMapColourToVertexIDs1Simplex().get(potentialheadColo);
					if (vertexIDshead == null) {
						numOfIterationAddingEdgesToGraph++;
						continue; // it is possible there is no vertex exist for the proposed head color, continue
									// to check with new head color in that case
					}

					while ((vertexIDshead.size() > 0) && (vertexIDTail == -1)) { // randomly select a head vertex and
																					// check
																					// if it has a tail with proposed
																					// color

						vertexIDHead = vertexIDshead.toArray(new Integer[vertexIDshead.size()])[mRandom
								.nextInt(vertexIDshead.size())];

						// get neighbors of selected vertex head
						IntSet neighbors = IntSetUtil.union(mimicGraph.getInNeighbors(vertexIDHead),
								mimicGraph.getOutNeighbors(vertexIDHead));

						IntSet vertexIDstail = new DefaultIntSet(Constants.DEFAULT_SIZE);

						for (int potentialTail : neighbors) {
							BitSet vertexColour = mimicGraph.getVertexColour(potentialTail);
							if (vertexColour.equals(potentialTailColo))
								vertexIDstail.add(potentialTail);
						}

						if (vertexIDstail.size() > 0)
							vertexIDTail = vertexIDstail.toArray(new Integer[vertexIDstail.size()])[mRandom
									.nextInt(vertexIDstail.size())];
						else
							vertexIDshead.remove(vertexIDHead); // randomly select head does not have the tail node with
																// proposed colors
					}

					if (vertexIDTail == -1) {
						numOfIterationAddingEdgesToGraph++;
						continue;// propose a new head and tail colors when proposed colors are not found.
					}

				}

				boolean edgeAdded = simplexProperty.addEdgeWithTriangleCheck(mimicGraph, potentialheadColo, potentialTailColo,
						vertexIDHead, vertexIDTail, initializer.getmMapColourToEdgeIDs1Simplex(),
						analysis.getIsoS1Analysis().getmColourMapperSimplexes(), false,
						simplexProperty.getmPropDistisoS1()); // last
				// parameter should always be false since we arselecting isolated 1-simplexes

				if (edgeAdded) {
					actualEdgesSimplexes++; // increment count if edge was added
					numOfIterationAddingEdgesToGraph = 0;
				} else {
					numOfIterationAddingEdgesToGraph++;
				}

			}
			LOGGER.debug("Case 2a completed!");
			LOGGER.debug("Added Edges: " + actualEdgesSimplexes);
			LOGGER.debug("Added Vertices: " + actualVerticesSimplexes);

			// ****************************** Logic for isolated self loop (i.e. 1-simplexes
			// with same source and target node)
			// *******************************************//

			actualVerticesSimplexes = 0;
			actualEdgesSimplexes = 0;

			// initialize variable tracking iteration count for this case
			numOfIterationAddingEdgesToGraph = 0;

			// get head proposer defined for 1-simplex distribution
			IOfferedItem<BitSet> potentialHeadColoProposer = simplexClass.getHeadProposer();

			int estimatedEdgesIsoSelfLoop = analysis.getIsoS1SelfLoopAnalysis().getEstEdges();
			int estimatedVerticesIsoSelfLoop = analysis.getIsoS1SelfLoopAnalysis().getEstVertices();
			vertsPB.setExtraMessage("Case 2b: Isolated self loop");
			LOGGER.debug("Estimated Edges: " + estimatedEdgesIsoSelfLoop);
			LOGGER.debug("Estimated Vertices: " + estimatedVerticesIsoSelfLoop);

			numOfIterationAddingEdgesToGraph = 0; // initialize iteration count
			while ((estimatedEdgesIsoSelfLoop > actualEdgesSimplexes) && (potentialHeadColoProposer != null)
					&& (numOfIterationAddingEdgesToGraph < maximumIteration)) { // additional condition that color
																				// proposer
																				// should not be null

				// variable to track if new vertex is not added
				boolean newVertexNotAdded = true;

				// get potential head for it and create vertex
				BitSet potentialColoSelfLoop = potentialHeadColoProposer.getPotentialItem();

				// temporary assignment for head vertex id
				int vertexIDHead;

				// check if vertex can be added or existing vertex should be selected
				if (actualVerticesSimplexes < estimatedVerticesIsoSelfLoop) {
					// add a vertex if enough vertices are not created for 1-simplexes
					vertexIDHead = addVertexToMimicGraph(mimicGraph, potentialColoSelfLoop,
							initializer.getmMapColourToVertexIDsIsoSelfLoop());
					actualVerticesSimplexes++;
					vertsPB.stepBy(1);

					newVertexNotAdded = false;// set the variable to false, since new variable is added

				} else {
					// New vertex cannot be created, select an existing vertex of the potential head
					// color at random
					IntSet vertexIDshead = initializer.getmMapColourToVertexIDsIsoSelfLoop().get(potentialColoSelfLoop);
					if (vertexIDshead == null) {
						numOfIterationAddingEdgesToGraph++;
						continue; // it is possible there is no vertex exist for the proposed head color, continue
									// to check with new head color in that case
					}
					vertexIDHead = vertexIDshead.toArray(new Integer[vertexIDshead.size()])[mRandom
							.nextInt(vertexIDshead.size())];
				}

				// temporary assignment for tail vertex id
				int vertexIDTail = vertexIDHead;

				boolean edgeAdded = simplexProperty.addEdgeWithTriangleCheck(mimicGraph, potentialColoSelfLoop, potentialColoSelfLoop,
						vertexIDHead, vertexIDTail, initializer.getmMapColourToEdgeIDs1Simplex(),
						analysis.getIsoS1SelfLoopAnalysis().getmColourMapperSimplexes(), newVertexNotAdded,
						simplexProperty.getmPropDistisoS1SelfLoop());

				if (edgeAdded) {
					actualEdgesSimplexes++; // increment count if edge was added
					numOfIterationAddingEdgesToGraph = 0;
				} else {
					numOfIterationAddingEdgesToGraph++;
				}

			}

			LOGGER.debug("Case 2b completed!");
			LOGGER.debug("Added Edges: " + actualEdgesSimplexes);
			LOGGER.debug("Added Vertices: " + actualVerticesSimplexes);

			// ************************ Logic for 0-simplexes
			// ***********************************//
			int estimatedVertices0Simplexes = analysis.getS0Analysis().getEstVertices();
			vertsPB.setExtraMessage("Case 3: Isolated 0-simplexes");
			LOGGER.debug("Estimated Vertices: " + estimatedVertices0Simplexes);
			// define proposer for 0-simplexes
			IOfferedItem<BitSet> potentialColoProposer0Simplex = simplexClass.getColourPointProposer();

			// initialize tracking variable
			actualVerticesSimplexes = 0;

			while ((actualVerticesSimplexes < estimatedVertices0Simplexes) && (potentialColoProposer0Simplex != null)) {
				// get possible color
				BitSet potentialColo0Simplex = potentialColoProposer0Simplex.getPotentialItem();

				// Add 0-simplex to mimic graph
				addVertexToMimicGraph(mimicGraph, potentialColo0Simplex,
						initializer.getmMapColourToVertexIDs0Simplex());
				actualVerticesSimplexes++;
				vertsPB.stepBy(1);
			}
			LOGGER.debug("Case 3 completed!");
			LOGGER.debug("Added Vertices: " + actualVerticesSimplexes);

			// *********************** Logic for connecting triangles using 1-simplexes
			// *********************//
			// Initially, find isolated triangles. We can proceed with this case only if
			// isolated triangles are added to the mimic graph by previous process
			int estimatedEdges1SimplexesConnect2Simplexes = analysis.getS1ConnectingTri().getEstEdges();
			vertsPB.setExtraMessage("Case 4: Connecting two-simplexes with 1-simplex");
			LOGGER.debug("Estimated Edges: " + estimatedEdges1SimplexesConnect2Simplexes);

			// ********************** Connect Isolated triangles
			// *********************************//
			vertsPB.setExtraMessage("Finding Isolated triangles to connect (Case 4a)");
			// temporary map for storing isolated triangle colors and their vertices
			ObjectObjectOpenHashMap<TriColours, List<IntSet>> mTriangleColorsVertexIdsIsolated = new ObjectObjectOpenHashMap<TriColours, List<IntSet>>();

			// Variable to store colors found in isolated triangles
			Set<BitSet> allColoIsolatedTri = new HashSet<BitSet>();

			// iterate over all store triangle colors
			ObjectObjectOpenHashMap<TriColours, List<IntSet>> mTriangleColorsVertexIds = initializer
					.getmTriangleColorsVertexIds();
			Object[] triColoskeys = mTriangleColorsVertexIds.keys;
			for (int i = 0; i < triColoskeys.length; i++) {
				if (mTriangleColorsVertexIds.allocated[i]) {
					TriColours triColosToCheck = (TriColours) triColoskeys[i];

					// get vertices related to this triangle color
					List<IntSet> listVerticesTri = mTriangleColorsVertexIds.get(triColosToCheck);

					// check vertices neighbors and find isolated triangles
					for (IntSet setVerticesTri : listVerticesTri) {
						// collect all neighbors
						IntSet setNeighbors = new DefaultIntSet(Constants.DEFAULT_SIZE);
						for (int vertexIdTri : setVerticesTri) {
							setNeighbors = IntSetUtil.union(setNeighbors, mimicGraph.getInNeighbors(vertexIdTri));
							setNeighbors = IntSetUtil.union(setNeighbors, mimicGraph.getOutNeighbors(vertexIdTri));
						}

						// Remove vertices forming triangles
						setNeighbors = IntSetUtil.difference(setNeighbors, setVerticesTri);

						if (setNeighbors.size() == 0) {
							// isolated triangle found

							// Convert vertices to Array
							Integer[] vertexIDTriArr = setVerticesTri.toArray(new Integer[setVerticesTri.size()]);

							// update map
							updateMapTriangleColorsVertices(vertexIDTriArr[0], vertexIDTriArr[1], vertexIDTriArr[2],
									triColosToCheck, mTriangleColorsVertexIdsIsolated);

							allColoIsolatedTri.add(triColosToCheck.getA());
							allColoIsolatedTri.add(triColosToCheck.getB());
							allColoIsolatedTri.add(triColosToCheck.getC());

						}

					}

				}

			}

			if (mTriangleColorsVertexIdsIsolated.size() > 0) {
				// try to connect isolated triangles using 1-simplexes
				LOGGER.debug("Isolated triangles found connecting them with 1-simplex.");

				// initialize variable to track number of edges added in the mimic graph
				actualEdgesSimplexes = 0;

				// initialize variable tracking iteration count for this case
				numOfIterationAddingEdgesToGraph = 0;

				IOfferedItem<EdgeColos> headColoProposerIsolatedTri = simplexClass.getIsolatedHeadProposer();

				while ((actualEdgesSimplexes < estimatedEdges1SimplexesConnect2Simplexes)
						&& (numOfIterationAddingEdgesToGraph < maximumIteration)
						&& (headColoProposerIsolatedTri != null)) {

					// get head color from the colors available in the triangle
					BitSet potentialHeadColoIsolatedTri = null; // initialize head color

					EdgeColos potentialItem = headColoProposerIsolatedTri.getPotentialItem();

					if (potentialItem != null)// check if head color proposer is not null
						potentialHeadColoIsolatedTri = potentialItem.getA();

					if (potentialHeadColoIsolatedTri != null) { // check for tail color only if head color is not null

						// get tail colors based on head color and available colors in the triangle
						BitSet potentialTailColoIsolatedTri = potentialItem.getB(); // initialize tail color

						if (potentialTailColoIsolatedTri != null) { // try to connect triangles using 1-simplexes if
																	// tail
																	// color is also not null

							// temporary variable to store the Triangle for proposed head color
							TriColours potentialTriangleHead = null;

							// temporary variable to store the Triangle for proposed tail color
							TriColours potentialTriangleTail = null;

							// find triangle with a head color
							Object[] triColoIsolatedkeys = mTriangleColorsVertexIdsIsolated.keys;
							for (int i = 0; i < triColoIsolatedkeys.length; i++) {
								if (mTriangleColorsVertexIdsIsolated.allocated[i]) {
									TriColours triColoIsolated = (TriColours) triColoIsolatedkeys[i];

									// variable to track same triangles are not selected for head and tail colors
									boolean triangleFoundForHead = false;

									// check triangle for head color
									if (triColoIsolated.getA().equals(potentialHeadColoIsolatedTri)
											|| triColoIsolated.getB().equals(potentialHeadColoIsolatedTri)
											|| triColoIsolated.getC().equals(potentialHeadColoIsolatedTri)
													&& (potentialTriangleHead == null)) {
										potentialTriangleHead = triColoIsolated;
									}

									// check triangle for tail color
									if (triColoIsolated.getA().equals(potentialTailColoIsolatedTri)
											|| triColoIsolated.getB().equals(potentialTailColoIsolatedTri)
											|| triColoIsolated.getC().equals(potentialTailColoIsolatedTri)
													&& !triangleFoundForHead && (potentialTriangleTail == null)) {
										potentialTriangleTail = triColoIsolated;
									}

									// stop looking for triangles of head and tail colors once found
									if ((potentialTriangleHead != null) && (potentialTriangleTail != null))
										break;
								}
							}

							if ((potentialTriangleHead == null) || (potentialTriangleTail == null)) {
								numOfIterationAddingEdgesToGraph++;
								continue;// retry to propose a different head or tail and find triangle for it
							}

							// variables storing head and tail IDs for Triangles to connect
							int possHeadIDIsolatedTri = 0;
							int possTailIDIsolatedTri = 0;

							// get vertices for head color
							List<IntSet> possVerticesListhead = mTriangleColorsVertexIdsIsolated
									.get(potentialTriangleHead);
							IntSet verticesTriIsolatedhead = possVerticesListhead
									.get(mRandom.nextInt(possVerticesListhead.size()));

							// get vertices for tail color
							List<IntSet> possVerticesListtail = mTriangleColorsVertexIdsIsolated
									.get(potentialTriangleTail);
							IntSet verticesTriIsolatedtail = possVerticesListtail
									.get(mRandom.nextInt(possVerticesListtail.size()));

							// compute concrete vertex id for head color and tail color
							for (int vertexIDheadIsoTri : verticesTriIsolatedhead) {
								if (mimicGraph.getVertexColour(vertexIDheadIsoTri)
										.equals(potentialHeadColoIsolatedTri)) {
									possHeadIDIsolatedTri = vertexIDheadIsoTri;
									break;
								}
							}

							for (int vertexIDTailIsoTri : verticesTriIsolatedtail) {
								if (mimicGraph.getVertexColour(vertexIDTailIsoTri)
										.equals(potentialTailColoIsolatedTri)) {
									possTailIDIsolatedTri = vertexIDTailIsoTri;
									break;
								}
							}

							EdgeColorsSorted edgesColors = new EdgeColorsSorted(potentialHeadColoIsolatedTri,
									potentialTailColoIsolatedTri);
							BitSet proposePropColor = simplexProperty.proposeColourForTri(edgesColors);

							IColourMappingRules mColourMapper1SimplexesConnTri = analysis.getS1ConnectingTri()
									.getmColourMapperSimplexes();
							if (mColourMapper1SimplexesConnTri.isHeadColourOf(potentialTailColoIsolatedTri,
									potentialHeadColoIsolatedTri)) {

								BitSet possEdgeColo = proposePropColor;

								// add edge to the graph
								mimicGraph.addEdge(possTailIDIsolatedTri, possHeadIDIsolatedTri, possEdgeColo);
								actualEdgesSimplexes++;
								numOfIterationAddingEdgesToGraph = 0;

								// update the mapping of edge color and tail-head IDs
								initializer.updateMappingOfEdgeColoHeadTailColo(possEdgeColo, possHeadIDIsolatedTri,
										possTailIDIsolatedTri);

								// update the map storing isolated triangles
								// get vertices for isolated triangles having head color and remove them. Since,
								// the triangle is connected with an edge to another edge.
								List<IntSet> listHeadVerticesIsoTri = mTriangleColorsVertexIdsIsolated
										.get(potentialTriangleHead);
								listHeadVerticesIsoTri.remove(verticesTriIsolatedhead);

								// get vertices for isolated triangles having tail color and remove them.
								List<IntSet> listTailVerticesIsoTri = mTriangleColorsVertexIdsIsolated
										.get(potentialTriangleTail);
								listTailVerticesIsoTri.remove(verticesTriIsolatedtail);
								LOGGER.debug("Two isolated triangles connected with 1-simplex");
							}
						} else {
							numOfIterationAddingEdgesToGraph++;
						}

					} else {
						numOfIterationAddingEdgesToGraph++;
					}
				}

			}

			// ******************************** Link set of connected triangles with
			// 1-simplex ***************************************************//
			// Note: This is a second scenario possible for connecting 2-simplexes using
			// 1-simplexes. Here, we use a sampling approach to select created triangles and
			// try to link them without creating a new triangle
			vertsPB.setExtraMessage("Trying to connect set of connected triangles using 1-simplex (Case 4b)");

			// initialize variable tracking iteration count for this case
			numOfIterationAddingEdgesToGraph = 0;
			IOfferedItem<EdgeColos> headColoProposercase4b = simplexClass.getIsolatedHeadProposer();

			while ((actualEdgesSimplexes < estimatedEdges1SimplexesConnect2Simplexes)
					&& (numOfIterationAddingEdgesToGraph < maximumIteration) && (headColoProposercase4b != null)) { // check
																													// if
																													// we
																													// can
																													// add
																													// more
																													// edge

				// Propose a possible colors for head
				EdgeColos potentialItem = headColoProposercase4b.getPotentialItem();

				BitSet potentialHeadColocase4b = null; // initialize head color

				if (potentialItem != null) {
					potentialHeadColocase4b = potentialItem.getA();

					// get tail colors based on head color

					BitSet potentialTailColocase4b = potentialItem.getB();

					// search for triangles when potential head and tail colors are not null
					if ((potentialHeadColocase4b != null) && (potentialTailColocase4b != null)) {

						// Temporary triangles for head and tail color
						TriColours potentialTriangleHeadCase4b = null;
						TriColours potentialTriangleTailCase4b = null;

						// Iterate over triangles to find head and tail colors
						Object[] connTrianglesColos = mTriangleColorsVertexIds.keys;
						for (int i = 0; i < connTrianglesColos.length; i++) {
							if (mTriangleColorsVertexIds.allocated[i]) {
								TriColours tempTriColo = (TriColours) connTrianglesColos[i];

								// variable to track same triangles are not selected for head and tail colors
								boolean triangleFoundForHead = false;

								// check triangle for head color (if not already found)
								if ((tempTriColo.getA().equals(potentialHeadColocase4b)
										|| tempTriColo.getB().equals(potentialHeadColocase4b)
										|| tempTriColo.getC().equals(potentialHeadColocase4b))
										&& (potentialTriangleHeadCase4b == null)) {
									potentialTriangleHeadCase4b = tempTriColo;
									triangleFoundForHead = true;
								}

								// check triangle for tail color
								if ((tempTriColo.getA().equals(potentialTailColocase4b)
										|| tempTriColo.getB().equals(potentialTailColocase4b)
										|| tempTriColo.getC().equals(potentialTailColocase4b)) && !triangleFoundForHead
										&& (potentialTriangleTailCase4b == null)) {
									potentialTriangleTailCase4b = tempTriColo;
								}

								// stop looking for triangles of head and tail colors once found
								if ((potentialTriangleHeadCase4b != null) && (potentialTriangleTailCase4b != null))
									break;

							} // end if condition: Triangle color allocated to Open hash map

						} // end for loop

						// If triangles are not found retry by proposing a head and a tail color
						if ((potentialTriangleHeadCase4b == null) || (potentialTriangleTailCase4b == null)) {
							numOfIterationAddingEdgesToGraph++;
							continue;
						}

						// ***************** Potential head and tail colors found for triangles to
						// connect ***********//
						// Select triangles at random to connect
						// There could be multiple triangles for the found triangle colors, selecting
						// one of them at random

						// We try to add edges in multiple iterations for the found head and tail
						// colors.
						// It is possible that such iterations do not allow to add edges between two
						// triangles since it forms a new triangle.

						int numIterationsCase4b = 0; // temporary variable to track number of iterations

						while ((numIterationsCase4b < 51)) {

							numIterationsCase4b++;// increment number of edges

							// get vertices for head color
							List<IntSet> possVerticesListhead = mTriangleColorsVertexIds
									.get(potentialTriangleHeadCase4b);
							IntSet verticesheadcase4b = possVerticesListhead
									.get(mRandom.nextInt(possVerticesListhead.size()));

							// get vertices for tail color
							List<IntSet> possVerticesListtail = mTriangleColorsVertexIds
									.get(potentialTriangleTailCase4b);
							IntSet verticestailcase4b = possVerticesListtail
									.get(mRandom.nextInt(possVerticesListtail.size()));

							// variables storing head and tail IDs for Triangles to connect
							int possHeadIDcase4b = 0;
							int possTailIDcase4b = 0;

							// compute concrete vertex id for head color and tail color
							for (int vertexIDheadtemp : verticesheadcase4b) {
								if (mimicGraph.getVertexColour(vertexIDheadtemp).equals(potentialHeadColocase4b)) {
									possHeadIDcase4b = vertexIDheadtemp;
									break;
								}
							}

							for (int vertexIDTailTemp : verticestailcase4b) {
								if (mimicGraph.getVertexColour(vertexIDTailTemp).equals(potentialTailColocase4b)) {
									possTailIDcase4b = vertexIDTailTemp;
									break;
								}
							}

							// Add edge between selected vertices if they do not have a vertex in common
							// using below function call
							boolean edgeAdded = simplexProperty.addEdgeWithTriangleCheck(mimicGraph, potentialHeadColocase4b,
									potentialTailColocase4b, possHeadIDcase4b, possTailIDcase4b,
									initializer.getmMapColourToEdgeIDs1Simplex(),
									analysis.getS1ConnectingTri().getmColourMapperSimplexes(), true,
									simplexProperty.getmPropDistS1ConnectingTri());
							if (edgeAdded) {
								actualEdgesSimplexes++;
								LOGGER.debug("Successfully connected triangles using 1-simplex (Case 4b)");
								numOfIterationAddingEdgesToGraph = 0;

								break;// terminate while loop trying to add edge for the found head and tail color
							} else {
								numOfIterationAddingEdgesToGraph++;
							}

						}

					} // end if condition: null check for head and tail color
					else {
						numOfIterationAddingEdgesToGraph++;
					}
				} else {
					numOfIterationAddingEdgesToGraph++;
				}
			}

			LOGGER.debug("Case 4 completed!");
			LOGGER.debug("Added Edges: " + actualEdgesSimplexes);

			// ******************* Logic for creating isolated 2-simplexes
			// ***********************//
			int estimatedEdgesIsolatedTriangle = analysis.getIsoTriAnalysis().getEstEdges();
			int estimatedVerticesIsolatedTriangle = analysis.getIsoTriAnalysis().getEstVertices();
			vertsPB.setExtraMessage("Case 5: Isolated 2-simplexes");
			LOGGER.debug("Estimated Edges: " + estimatedEdgesIsolatedTriangle);
			LOGGER.debug("Estimated Vertices: " + estimatedVerticesIsolatedTriangle);
			IOfferedItem<TriColours> potentialIsolatedTriangleProposer = simplexClass.getIsolatedTriangleProposer();

			// initialize tracker variable
			actualVerticesSimplexes = 0;
			actualEdgesSimplexes = 0;

			// Variable to track set of triangle added to the mimic graph (i.e. set of
			// Colors of the vertices forming the triangle)
			Set<TriColours> setIsoTriInMimicGraph = new HashSet<TriColours>();

			while (((estimatedVerticesIsolatedTriangle - actualVerticesSimplexes) >= 3)
					&& ((estimatedEdgesIsolatedTriangle - actualEdgesSimplexes) >= 3)
					&& (potentialIsolatedTriangleProposer != null)) {
				TriColours possIsoTri = potentialIsolatedTriangleProposer.getPotentialItem();
				// add the selected random triangle to mimic graph
				addTriangleToMimicGraph(mimicGraph, possIsoTri,
						analysis.getIsoTriAnalysis().getmColourMapperSimplexes(),
						initializer.getmMapColourToVertexIDs2SimplexIsolated(),
						initializer.getmMapColourToEdgeIDs2SimplexIsolated(),
						initializer.getmIsolatedTriangleColorsVertexIds(), simplexProperty.getmPropDistIsoTri());

				setIsoTriInMimicGraph.add(possIsoTri);

				// increment no of vertices in triangle
				actualVerticesSimplexes = actualVerticesSimplexes + 3;

				// increment no. of edges in triangle
				actualEdgesSimplexes = actualEdgesSimplexes + 3;
				vertsPB.stepBy(3);
			}

			int iterationCount = 0;

			while ((estimatedEdgesIsolatedTriangle > actualEdgesSimplexes) && (iterationCount < maximumIteration)) {

				if (setIsoTriInMimicGraph.size() > 0) {

					// Logic for adding edges to existing triangles
					TriColours proposeTriangleToAddEdge = simplexClass
							.proposeIsoTriangleToAddEdge(setIsoTriInMimicGraph);

					// Best case: A triangle is returned
					List<IntSet> selectedTrianglesList = initializer.getmIsolatedTriangleColorsVertexIds()
							.get(proposeTriangleToAddEdge);

					// randomly selecting one of these triangles
					IntSet selectedVertices = selectedTrianglesList.toArray(
							new IntSet[selectedTrianglesList.size()])[mRandom.nextInt(selectedTrianglesList.size())];

					// temporary variable to track if edge was added to existing triangle
					boolean edgeNotAddedToExistingTriangle = true;

					// Considering different vertex pairs to add an edge

					// Convert vertices to Array
					Integer[] vertexIDExistingTriangle = selectedVertices.toArray(new Integer[selectedVertices.size()]);

					// creating different pairs of combinations for 3 vertices of a triangle
					List<List<Integer>> differentPairsOfVertices = new ArrayList<List<Integer>>();
					differentPairsOfVertices
							.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[1]));
					differentPairsOfVertices
							.add(Arrays.asList(vertexIDExistingTriangle[1], vertexIDExistingTriangle[2]));
					differentPairsOfVertices
							.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[2]));

					for (List<Integer> pairOfVertices : differentPairsOfVertices) {

						// Get vertex colours for a pair
						BitSet existingVertexColo1 = mimicGraph.getVertexColour(pairOfVertices.get(0));
						BitSet existingVertexColo2 = mimicGraph.getVertexColour(pairOfVertices.get(1));

						boolean edgeAdded = simplexProperty.addEdgeInAnyDirection(mimicGraph, existingVertexColo1,
								existingVertexColo2, pairOfVertices.get(0), pairOfVertices.get(1),
								initializer.getmMapColourToEdgeIDs2SimplexIsolated(),
								analysis.getIsoTriAnalysis().getmColourMapperSimplexes(),
								simplexProperty.getmPropDistIsoTri());

						if (edgeAdded) {
							// increment no. of edges in triangle
							actualEdgesSimplexes = actualEdgesSimplexes + 1;

							// update boolean tracker variable
							edgeNotAddedToExistingTriangle = false;
							iterationCount = 0;

							// break iterating over pair of vertices, since an edge is found
							break;
						}

					}

					if (edgeNotAddedToExistingTriangle) {
						iterationCount++;
					}

				} else {
					break; // no isolated triangles are present in the mimic graph
				}
			}

			LOGGER.debug("Case 5 completed!");
			LOGGER.debug("Added Edges: " + actualEdgesSimplexes);
			LOGGER.debug("Added Vertices: " + actualVerticesSimplexes);

			// ********************** Growing 1-simplexes (connected 1-simplexes)
			// ****************************************//
			int estEdgesConnected1Simplexes = analysis.getConnS1Analysis().getEstEdges();
			int actualVerticesMimicGraph = mimicGraph.getVertices().size();

			// subtracting estimated number of vertices for 1-simplexes connected only to
			// triangles. Such vertices are created in next step.
			int estVerticesConnected1Simplexes = initializer.getDesiredNoOfVertices() - actualVerticesMimicGraph
					- analysis.getS1ConnToTri().getEstVertices();

			vertsPB.setExtraMessage("Case 6: Connected 1-simplexes");
			LOGGER.debug("Estimated Edges: " + estEdgesConnected1Simplexes);
			LOGGER.debug("Estimated Vertices: " + estVerticesConnected1Simplexes);

			// initialize variable tracking iteration count for this case
			numOfIterationAddingEdgesToGraph = 0;

			EdgeColos initialRandomEdge = simplexClass.proposeConnEdge();

			// Variables to track number of edges and vertices added in triangle
			int actualEdgesInConnS1 = 0;
			int actualVerticesInConnS1 = 0;

			if ((initialRandomEdge != null) && (estEdgesConnected1Simplexes >= 1)
					&& (estVerticesConnected1Simplexes >= 2)) {

				// Variable to track set of triangle added to the mimic graph (i.e. set of
				// Colors of the vertices forming the triangle)
				Set<EdgeColos> setEdgeColorsMimicGraph = new HashSet<EdgeColos>();

				BitSet potentialHeadColo = initialRandomEdge.getA();
				BitSet potentialTailColo = initialRandomEdge.getB();

				Set<BitSet> setOfColosInGraph = new HashSet<BitSet>();
				setOfColosInGraph.add(potentialHeadColo);
				setOfColosInGraph.add(potentialTailColo);

				int vertexIDTail = addVertexToMimicGraph(mimicGraph, potentialTailColo,
						initializer.getmMapColourToVertexIDs1SimplexConnected());
				int vertexIDHead = addVertexToMimicGraph(mimicGraph, potentialHeadColo,
						initializer.getmMapColourToVertexIDs1SimplexConnected());

				simplexProperty.addEdgeWithTriangleCheck(mimicGraph, potentialHeadColo, potentialTailColo, vertexIDHead, vertexIDTail,
						initializer.getmMapColourToEdgeIDs1Simplex(),
						analysis.getConnS1Analysis().getmColourMapperSimplexes(), false,
						simplexProperty.getmPropDistconnS1Analysis());

				// update the map to track edges along with vertices added
				updateMapEdgeColorsVertices(vertexIDHead, vertexIDTail, initialRandomEdge);

				// increment no of vertices in triangle
				actualVerticesInConnS1 = actualVerticesInConnS1 + 2;

				// increment no. of edges in triangle
				actualEdgesInConnS1 = actualEdgesInConnS1 + 1;

				vertsPB.stepBy(2);

				// Add the triangle colors to set variable
				setEdgeColorsMimicGraph.add(initialRandomEdge);

				// initial head color proposer
				IOfferedItem<BitSet> potentialHeadColoProposerConnS1 = simplexClass.getConnHeadProposer();

				numOfIterationAddingEdgesToGraph = 0; // initialize iteration count
				while (actualVerticesInConnS1 < estVerticesConnected1Simplexes) {

					// if(actualVerticesInConnS1 < estVerticesConnected1Simplexes) {
					if ((actualVerticesInConnS1 < estVerticesConnected1Simplexes)
							&& (numOfIterationAddingEdgesToGraph < maximumIteration)) {
						// If we can add more edges when we are allowed to include additional vertices
						// otherwise edges need to be added to existing 1-simplexes

						if (setOfColosInGraph.size() != 0) {
							// Propose a head color from existing colors in the mimic graph
							BitSet proposedHeadColo = potentialHeadColoProposerConnS1
									.getPotentialItem(setOfColosInGraph);

							IntSet verticesWithProposedColor = initializer.getmMapColourToVertexIDs1SimplexConnected()
									.get(proposedHeadColo);
							if (verticesWithProposedColor == null) {// Propose a new head color when none present
								setOfColosInGraph.remove(proposedHeadColo);
								numOfIterationAddingEdgesToGraph++;
								continue;
							}

							// select a vertex at random from the mimic graph
							int randomVertexID = verticesWithProposedColor
									.toArray(new Integer[verticesWithProposedColor.size()])[mRandom
											.nextInt(verticesWithProposedColor.size())];

							IOfferedItem<BitSet> proposeVertColo = simplexClass.proposeColour(proposedHeadColo);
							if (proposeVertColo == null) {
								setOfColosInGraph.remove(proposedHeadColo);
								numOfIterationAddingEdgesToGraph++;
								continue;
							}

							// Get a tail color randomly from it
							BitSet proposedTail = proposeVertColo.getPotentialItem();

							// boolean variable to track if new edge are added
							boolean newEdgesNotAddedToTriangle = true;
							if (proposedTail != null) {
								// propose a property based on probability distribution for the found head and
								// tail color
								BitSet proposePropColor = simplexProperty
										.proposeConnPropColour(new EdgeColorsSorted(proposedHeadColo, proposedTail));

								if (actualVerticesInConnS1 < estVerticesConnected1Simplexes
										&& (analysis.getConnS1Analysis().getmColourMapperSimplexes()
												.canConnect(proposedHeadColo, proposedTail, proposePropColor))) {

									// create vertex for the proposed color
									int proposedVertId = addVertexToMimicGraph(mimicGraph, proposedTail,
											initializer.getmMapColourToVertexIDs1SimplexConnected());

									setOfColosInGraph.add(proposedTail);

									// add edges among selected vertices and proposed color
									// Note: Ideally properties should exist among them. since they were also
									// forming an edge in input graphs
									newEdgesNotAddedToTriangle = !simplexProperty.addEdgeWithTriangleCheck(mimicGraph,
											proposedHeadColo, proposedTail, randomVertexID, proposedVertId,
											initializer.getmMapColourToEdgeIDs1Simplex(),
											analysis.getConnS1Analysis().getmColourMapperSimplexes(), false,
											proposePropColor);

									// try max tries, and then remove colour from set
									if (newEdgesNotAddedToTriangle) {
										if (numOfIterationAddingEdgesToGraph < (int) (maximumIteration / 2)) {
											numOfIterationAddingEdgesToGraph++;
											continue;
										} else {
											setOfColosInGraph.remove(proposedHeadColo);
											continue;
										}
									} else {
										// increment number of vertices and edges added in the mimic graph for triangles
										actualVerticesInConnS1 = actualVerticesInConnS1 + 1;
										actualEdgesInConnS1 = actualEdgesInConnS1 + 1;
										vertsPB.stepBy(1);

										EdgeColos tempEdgeColors = new EdgeColos(proposedHeadColo, proposedTail);

										// Add the created triangle Colors to set
										setEdgeColorsMimicGraph.add(tempEdgeColors);

										// update the map to track edges along with vertices added
										updateMapEdgeColorsVertices(randomVertexID, proposedVertId, tempEdgeColors);

										numOfIterationAddingEdgesToGraph = 0;
									}
								}
							}

						} // end if condition - check if edges can grow
						else {
							vertsPB.setExtraMessage("Growing 1-simplexes not possible.... Proposing new 1-simplex");

							// If no tail color could be included for the existing head colors in the mimic
							// graph. Add a new 1-simplex

							EdgeColos randomEdge = simplexClass.proposeConnEdge();

							// variable to track number of times a random edge was selected
							int numOfIterationRandomTri = 1;

							// check if it is possible to add new triangle
							while (numOfIterationRandomTri < 500
									&& actualVerticesInConnS1 < estVerticesConnected1Simplexes) {
								randomEdge = simplexClass.proposeConnEdge();
								numOfIterationRandomTri++;
							}

							// check property color for selected head and tail colors
							BitSet proposePropColor = simplexProperty
									.proposeColour(new EdgeColorsSorted(randomEdge.getA(), randomEdge.getB()));

							if (actualVerticesInConnS1 < estVerticesConnected1Simplexes
									&& (analysis.getConnS1Analysis().getmColourMapperSimplexes()
											.canConnect(randomEdge.getA(), randomEdge.getB(), proposePropColor))) {

								potentialHeadColo = randomEdge.getA();
								potentialTailColo = randomEdge.getB();

								setOfColosInGraph.add(potentialHeadColo);
								setOfColosInGraph.add(potentialTailColo);

								vertexIDTail = addVertexToMimicGraph(mimicGraph, potentialTailColo,
										initializer.getmMapColourToVertexIDs1SimplexConnected());
								vertexIDHead = addVertexToMimicGraph(mimicGraph, potentialHeadColo,
										initializer.getmMapColourToVertexIDs1SimplexConnected());

								simplexProperty.addEdgeWithTriangleCheck(mimicGraph, potentialHeadColo, potentialTailColo, vertexIDHead,
										vertexIDTail, initializer.getmMapColourToEdgeIDs1Simplex(),
										analysis.getConnS1Analysis().getmColourMapperSimplexes(), false, (IPropertyDist)null);

								// increment no of vertices in triangle
								actualVerticesInConnS1 = actualVerticesInConnS1 + 2;

								// increment no. of edges in triangle
								actualEdgesInConnS1 = actualEdgesInConnS1 + 1;

								vertsPB.stepBy(2);

								// Add the edge colors to set variable
								setEdgeColorsMimicGraph.add(randomEdge);

								// update the map to track edges along with vertices added
								updateMapEdgeColorsVertices(vertexIDHead, vertexIDTail, randomEdge);

								numOfIterationAddingEdgesToGraph = 0;

							} else {
								break; // terminate while condition if it is not possible to add random edge
							}

						}
					} else {
						break; // Cannot add more vertices
					}
				} // end while condition checking if actual number of edges is less than estimated
					// number of edges
				LOGGER.debug("Growing 1-simplexes phase completed");
				LOGGER.debug("Added Edges: " + actualEdgesInConnS1);
				LOGGER.debug("Added Vertices: " + actualVerticesInConnS1);

				vertsPB.setExtraMessage("Case 6: Adding additional Edges to created 1-simplexes");

				numOfIterationAddingEdgesToGraph = 0; // initialize iteration count
				while ((actualEdgesInConnS1 < estEdgesConnected1Simplexes)
						&& (numOfIterationAddingEdgesToGraph < maximumIteration)) { // Case: Triangles cannot be added
																					// to
																					// the mimic graph but edges can be
																					// added to existing triangles

					if (setEdgeColorsMimicGraph.size() != 0) {

						// Logic for adding edges to existing connected 1-simplexes
						EdgeColos proposeS1 = simplexClass.proposeTriangleToAddEdgeColours(setEdgeColorsMimicGraph);

						// Best case: A triangle is returned
						List<IntSet> selectedEdgesList = initializer.getmEdgeColorsVertexIds().get(proposeS1);

						// randomly selecting one of these triangles
						IntSet selectedVertices = selectedEdgesList.toArray(
								new IntSet[selectedEdgesList.size()])[mRandom.nextInt(selectedEdgesList.size())];

						// Convert vertices to Array
						Integer[] vertexIDExisting = selectedVertices.toArray(new Integer[selectedVertices.size()]);

						Integer existingVertexID1 = vertexIDExisting[0];
						Integer existingVertexID2 = vertexIDExisting[1];
						BitSet existingVertexColo1 = mimicGraph.getVertexColour(existingVertexID1);
						BitSet existingVertexColo2 = mimicGraph.getVertexColour(existingVertexID2);

						// triangle check not required since working with 1-simplexes instead of
						// sampling head and tail
						boolean edgeAdded = simplexProperty.addEdgeWithTriangleCheck(mimicGraph, existingVertexColo1,
								existingVertexColo2, existingVertexID1, existingVertexID2,
								initializer.getmMapColourToEdgeIDs1Simplex(),
								analysis.getConnS1Analysis().getmColourMapperSimplexes(), false,
								simplexProperty.getmPropDistconnS1Analysis());

						if (edgeAdded) {
							actualEdgesInConnS1++;
							numOfIterationAddingEdgesToGraph = 0;
						} else {
							// try to add edge in different direction
							edgeAdded = simplexProperty.addEdgeWithTriangleCheck(mimicGraph, existingVertexColo2, existingVertexColo1,
									existingVertexID2, existingVertexID1, initializer.getmMapColourToEdgeIDs1Simplex(),
									analysis.getConnS1Analysis().getmColourMapperSimplexes(), false,
									simplexProperty.getmPropDistconnS1Analysis()); // triangle check not
							// required since
							// working with
							// 1-simplexes
							// instead of
							// sampling head and
							// tail
							if (edgeAdded) {
								actualEdgesInConnS1++;
								numOfIterationAddingEdgesToGraph = 0;
							} else {
								numOfIterationAddingEdgesToGraph++;
								// Not able to add edge increase the iteration count
							}
						}

					} // end if condition - check if candidate triangles exists for adding edges to
						// them
					else {

						// No edges can be added to existing
						// Give warning and break while loop
						LOGGER.warn(
								"Not able to add edges to existing and add new 1-simplexes to the mimic graph. Estimated number is greater than actual number of edges! The process of adding edge ends. ");
						break; // terminate out of while condition

					}

				} // end else condition adding edges to existing triangles

			} // end if condition - initial random triangle is not null
			LOGGER.debug("Case 6 completed!");
			LOGGER.debug("Added Edges: " + actualEdgesInConnS1);
			LOGGER.debug("Added Vertices: " + actualVerticesInConnS1);

			// **********************End Growing 1-simplexes (connected 1-simplexes)
			// ************************************//

			// ******************* Logic for 2-simplexes connected to rest of the graph
			// ********************//
			// update temporary variables
			actualVerticesSimplexes = 0;

			// get actual vertices count
			actualVerticesMimicGraph = mimicGraph.getVertices().size();

			// compute remaining vertices and edges
			// int remainingVertices = mIDesiredNoOfVertices - actualVerticesMimicGraph;

			// update the estimated counts for "2-simplexes connected" if remaining counts
			// is greater than estimated counts
			int estimatedVerticesCommon = analysis.getS1ConnToTri().getEstVertices();

			vertsPB.setExtraMessage("Case 7: Connect 2-simplexes to other vertices");
			LOGGER.debug("Estimated Vertices: " + estimatedVerticesCommon);

			// get proposer of Vertex color (to create vertices connected to triangles)
			IOfferedItem<BitSet> potentialColoProposerForVertConnectedToTriangle = simplexClass
					.getColourProposerVertConnTriangle();

			// set to track class colors. New vertices need to be created for them later
//		Set<BitSet> vertexClassColoSet = new HashSet<BitSet>();

			// Add vertices if not enough vertices
			while ((actualVerticesSimplexes < estimatedVerticesCommon)
					&& (potentialColoProposerForVertConnectedToTriangle != null)) {
				BitSet potentialvertexColo = potentialColoProposerForVertConnectedToTriangle.getPotentialItem();

				// check class node exists for the proposed color
//			Set<BitSet> classColourSet = mimicGraph.getClassColour(potentialvertexColo);
//			int numberOfClassVertices = 0;
//			for (BitSet classColour : classColourSet) {
//				Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes().get(classColour);
//				if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
//					numberOfClassVertices++;
//					vertexClassColoSet.add(classColour);
//				}
//			}

				// create a new vertex only if we can add class nodes
//			if ((actualVerticesSimplexes + numberOfClassVertices) < estimatedVerticesCommon) {
				addVertexToMimicGraph(mimicGraph, potentialvertexColo,
						initializer.getmMapColourToVertexIDsConnectedTo2Simplex());
				actualVerticesSimplexes++;
				vertsPB.stepBy(1);
				// updating estimated vertices for this case. Note: class nodes are created in
				// next step
//				estimatedVerticesCommon = estimatedVerticesCommon - numberOfClassVertices;
//			}
			}

			LOGGER.debug("Vertex addition completed");
			LOGGER.debug("Added Vertices: " + actualVerticesSimplexes);

			// get actual edges count and update temporary variable
			actualEdgesSimplexes = 0;

			// initialize variable tracking iteration count for this case
			numOfIterationAddingEdgesToGraph = 0;

			// compute remaining edges
			int estEdgesSelfLoopConnTri = analysis.getSelfLoopConnTri().getEstEdges();
			int estEdgesSelfLoop1SimplexConnToTri = analysis.getSelfLoops1ConnToTri().getEstEdges();
			int estEdgesSelfLoopIsoTri = analysis.getSelfLoopIsoTri().getEstEdges();
			int estimatedEdgesSelfLoopIn1Simplex = analysis.getSelfLoopsInIsoS1().getEstEdges();
			int estEdgesSelfLoopConn1Simplexes = analysis.getSelfLoopsInConnS1().getEstEdges();
			int estimatedEdgesCommon = analysis.getS1ConnToTri().getEstEdges();
			LOGGER.debug("Estimated Edges: " + estimatedEdgesCommon);

			// Get head color proposer for creating 1-simplexes connected to triangles
			IOfferedItem<EdgeColos> potentialHeadColoCommon2Simplex = simplexClass.getHeadConnEdgeProposer();

			while ((actualEdgesSimplexes < estimatedEdgesCommon) && (potentialHeadColoCommon2Simplex != null)
					&& (numOfIterationAddingEdgesToGraph < maximumIteration)) {

				EdgeColos potentialItem = potentialHeadColoCommon2Simplex.getPotentialItem();

				// Get possible head color from distribution
				BitSet potentialheadColo = potentialItem.getA();

				// Get possible tail Color for the head color
				BitSet potentialTailColo = potentialItem.getB();

				BitSet proposePropColor = simplexProperty
						.proposeS1ConnProp(new EdgeColorsSorted(potentialheadColo, potentialTailColo));

				if (!analysis.getS1ConnToTri().getmColourMapperSimplexes().canConnect(potentialheadColo,
						potentialTailColo, proposePropColor)) {
					numOfIterationAddingEdgesToGraph++;
					continue;
				}

				// Check if head Color is available in 2-Simplexes
				IntSet vertsWithHeadColo = initializer.getmMapColourToVertexIDs2Simplex().get(potentialheadColo);

				// variable to track if head color is in 2-simplex
				boolean headColoIn2Simplex = true;

				if (vertsWithHeadColo == null) {
					// check head color is available in connected 1-simplexes
					vertsWithHeadColo = initializer.getmMapColourToVertexIDs1SimplexConnected().get(potentialheadColo);

					// check head color is available in new created vertex for connecting with
					// triangle
					if (vertsWithHeadColo == null) {
						vertsWithHeadColo = initializer.getmMapColourToVertexIDsConnectedTo2Simplex()
								.get(potentialheadColo);
					}

					headColoIn2Simplex = false;
				}

				if (vertsWithHeadColo == null) {
					numOfIterationAddingEdgesToGraph++;
					// There does not exist any vertex with potential color, continue and try to use
					// other head color
					continue;
				}

				// Get vertices with tail color from 2-simplex or connected 1-simplex depending
				// where the vertex of head color exist
				IntSet vertsWithTailColo = null;
				if (headColoIn2Simplex) {
					vertsWithTailColo = initializer.getmMapColourToVertexIDs1SimplexConnected().get(potentialTailColo);

					// check tail color is available in new created vertex for connecting with
					// triangle
					if (vertsWithTailColo == null) {
						vertsWithTailColo = initializer.getmMapColourToVertexIDsConnectedTo2Simplex()
								.get(potentialTailColo);

					}
				} else
					vertsWithTailColo = initializer.getmMapColourToVertexIDs2Simplex().get(potentialTailColo);

				if (vertsWithTailColo == null) {
					// Vertex for tail color is not found, Look for head color and tail color in
					// different simplexes

					if (headColoIn2Simplex) {
						// Earlier head color was in 2-simplex, now looking in connected 1-simplex
						vertsWithHeadColo = initializer.getmMapColourToVertexIDs1SimplexConnected()
								.get(potentialheadColo);

						// check head color is available in new created vertex for connecting with
						// triangle
						if (vertsWithHeadColo == null) {
							vertsWithHeadColo = initializer.getmMapColourToVertexIDsConnectedTo2Simplex()
									.get(potentialheadColo);
						}

					} else {
						vertsWithHeadColo = initializer.getmMapColourToVertexIDs2Simplex().get(potentialheadColo);
					}

					// Continue if vertices are not found for head color even after swap
					if (vertsWithHeadColo == null) {
						numOfIterationAddingEdgesToGraph++;
						continue;
					}

					// Get vertices for tail color
					if (headColoIn2Simplex) {
						vertsWithTailColo = initializer.getmMapColourToVertexIDs1SimplexConnected()
								.get(potentialTailColo);
						// check tail color is available in new created vertex for connecting with
						// triangle
						if (vertsWithTailColo == null) {
							vertsWithTailColo = initializer.getmMapColourToVertexIDsConnectedTo2Simplex()
									.get(potentialTailColo);

						}
					} else
						vertsWithTailColo = initializer.getmMapColourToVertexIDs2Simplex().get(potentialTailColo);

					// Continue if vertices are not found for tail color even after swap
					if (vertsWithTailColo == null) {
						numOfIterationAddingEdgesToGraph++;
						continue;
					}

				}

				// Select any random vertex with head color
				int vertexIDWithHeadColo = vertsWithHeadColo.toArray(new Integer[vertsWithHeadColo.size()])[mRandom
						.nextInt(vertsWithHeadColo.size())];

				// Select any random vertex with head color
				int vertexIDWithTailColo = vertsWithTailColo.toArray(new Integer[vertsWithTailColo.size()])[mRandom
						.nextInt(vertsWithTailColo.size())];

				// Get possible edge color using the mapper
				Set<BitSet> possibleLinkingEdgeColours = new HashSet<BitSet>();
				possibleLinkingEdgeColours.add(proposePropColor);

				// check existing edge colors between vertices and remove them from the possible
				// edge colors set
				possibleLinkingEdgeColours = simplexProperty.removeDuplicateEdgeColors(mimicGraph, vertexIDWithHeadColo,
						vertexIDWithTailColo, possibleLinkingEdgeColours);

				boolean havingVertices = initializer.commonVertices(mimicGraph, vertexIDWithHeadColo, vertexIDWithTailColo);

				if ((possibleLinkingEdgeColours.size() > 0) && !havingVertices) {
					// select a random edge
					BitSet possEdgeColo = possibleLinkingEdgeColours
							.toArray(new BitSet[possibleLinkingEdgeColours.size()])[mRandom
									.nextInt(possibleLinkingEdgeColours.size())];

					// add the edge to graph
					mimicGraph.addEdge(vertexIDWithTailColo, vertexIDWithHeadColo, possEdgeColo);
					actualEdgesSimplexes++;

					numOfIterationAddingEdgesToGraph = 0;

					// update the map to track edge colors, tail id and head ids
					initializer.updateMappingOfEdgeColoHeadTailColo(possEdgeColo, vertexIDWithHeadColo, vertexIDWithTailColo);

				} else {
					numOfIterationAddingEdgesToGraph++;
				}

			}
			LOGGER.debug("Case 7 completed!");
			LOGGER.debug("Added Edges: " + actualEdgesSimplexes);

			// ************************* Logic to add self loops for simplexes created for
			// different cases *********************************//
			vertsPB.setExtraMessage("Adding Self loops...........");

			// Isolated 2-simplexes
			LOGGER.debug("Isolated 1-simplexes");
			addSelfLoops(mimicGraph, estimatedEdgesSelfLoopIn1Simplex, simplexClass.getIsoS1Proposer(),
					initializer.getmMapColourToVertexIDs1Simplex(),
					analysis.getSelfLoopsInIsoS1().getmColourMapperSimplexes(),
					initializer.getmMapColourToEdgeIDs1Simplex(), simplexProperty.getmPropDistselfLoopsInIsoS1());
			vertsPB.stepTo((long) mimicGraph.getNumberOfVertices());

			// Isolated 2-simplexes
			LOGGER.debug("Isolated 2-simplexes");
			addSelfLoops(mimicGraph, estEdgesSelfLoopIsoTri, simplexClass.getIsoS2Proposer(),
					initializer.getmMapColourToVertexIDs2SimplexIsolated(),
					analysis.getSelfLoopIsoTri().getmColourMapperSimplexes(),
					initializer.getmMapColourToEdgeIDs2SimplexIsolated(), simplexProperty.getmPropDistselfLoopIsoTri());

			// Connected 2-simplexes
			LOGGER.debug("Connected 2-simplexes");
			addSelfLoops(mimicGraph, estEdgesSelfLoopConnTri, simplexClass.getConnS2Proposer(),
					initializer.getmMapColourToVertexIDs2Simplex(),
					analysis.getSelfLoopConnTri().getmColourMapperSimplexes(),
					initializer.getmMapColourToEdgeIDs2Simplex(), simplexProperty.getmPropDistselfLoopConnTri());

			// 1-simplexes only connected to triangles
			LOGGER.debug("1-simplexes connected only to triangles");
			addSelfLoops(mimicGraph, estEdgesSelfLoop1SimplexConnToTri, simplexClass.getConnS1TriProposer(),
					initializer.getmMapColourToVertexIDsConnectedTo2Simplex(),
					analysis.getSelfLoops1ConnToTri().getmColourMapperSimplexes(),
					initializer.getmMapColourToEdgeIDs1Simplex(), simplexProperty.getmPropDistselfLoops1ConnToTri());

			// Connected 1-simplexes
			LOGGER.debug("Connected 1-simplexes");
			addSelfLoops(mimicGraph, estEdgesSelfLoopConn1Simplexes, simplexClass.getConnS1Proposer(),
					initializer.getmMapColourToVertexIDs1SimplexConnected(),
					analysis.getSelfLoopsInConnS1().getmColourMapperSimplexes(),
					initializer.getmMapColourToEdgeIDs1Simplex(), simplexProperty.getmPropDistSelfLoopConnS1());

			LOGGER.info("Number of edges in the mimic graph: " + mimicGraph.getEdges().size());
			LOGGER.info("Number of vertices in the mimic graph: " + mimicGraph.getVertices().size());

		}

		// Update mMapColourToVertexIDs used for adding edges when improving the graph
		// in next phase
		updateVertexColoMap(initializer.getmMapColourToVertexIDs1Simplex()); // isolated 1-simplexes
		updateVertexColoMap(initializer.getmMapColourToVertexIDs2Simplex()); // connected 2-simplexes
		updateVertexColoMap(initializer.getmMapColourToVertexIDsIsoSelfLoop()); // isolated self loop
		updateVertexColoMap(initializer.getmMapColourToVertexIDs2SimplexIsolated()); // isolated 2-simplexes
		updateVertexColoMap(initializer.getmMapColourToVertexIDs1SimplexConnected()); // connected 1-simplexes
		updateVertexColoMap(initializer.getmMapColourToVertexIDsConnectedTo2Simplex()); // 1-simplexes connected to

		// update maps to use during optimization
//		Map<BitSet, IntSet> mergedMap = new HashMap<>(initializer.getmMapColourToEdgeIDs2Simplex());
//		mergedMap.putAll(initializer.getmMapColourToEdgeIDs2SimplexIsolated());
//		mergedMap.putAll(initializer.getmMapColourToEdgeIDs1Simplex());
//		initializer.setMapColourToEdgeIDs(mergedMap);
	}
	
	/**
	 * The method adds a vertex of the input color to the mimic graph and returns
	 * the vertex id. It also updates a map, which stores the mapping of vertex
	 * colors and vertex IDs.
	 * 
	 * @param vertexColor - BitSet for the vertex color
	 * @return - vertex id
	 */
	private int addVertexToMimicGraph(ColouredGraph mimicGraph, BitSet vertexColor,
			Map<BitSet, IntSet> mMapColourToVertexIDsToUpdate) {
		int vertId = mimicGraph.addVertex(vertexColor);
		IntSet setVertIDs = mMapColourToVertexIDsToUpdate.get(vertexColor); // mMapColourToVertexIDs.get(vertexColor);
		if (setVertIDs == null) {
			setVertIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
			mMapColourToVertexIDsToUpdate.put(vertexColor, setVertIDs); // mMapColourToVertexIDs.put(vertexColor,
																		// setVertIDs);
			initializer.getmMapColourToVertexIDs().put(vertexColor, setVertIDs); // updating this map, required when
																					// refining the graph
		}
		setVertIDs.add(vertId);

		return vertId;
	}


	/**
	 * This method is used to update the map that tracks the triangle colours and
	 * vertices for those colours.
	 * 
	 * @param vert1Id              - Integer vertex id for the first vertex of the
	 *                             triangle
	 * @param vert2Id              - Integer vertex id for the second vertex of the
	 *                             triangle
	 * @param vert3Id              - Integer vertex id for the third vertex of the
	 *                             triangle
	 * @param inputTriangleColours - TriangleColours object with the colours for the
	 *                             vertices of the triangle
	 */
	private void updateMapTriangleColorsVertices(int vert1Id, int vert2Id, int vert3Id, TriColours inputTriangleColours,
			ObjectObjectOpenHashMap<TriColours, List<IntSet>> mTriColVertexIDsToUpdate) {

		// list of vertex ids, initially checks if the vertex ids exists for the input
		// triangle colours
		List<IntSet> tempVerticesList = mTriColVertexIDsToUpdate.get(inputTriangleColours);
		if (tempVerticesList == null) {
			tempVerticesList = new ArrayList<IntSet>();
		}

		// create set for vertex ids
		IntSet verticesOfNewTriangle = new DefaultIntSet(Constants.DEFAULT_SIZE);
		verticesOfNewTriangle.add(vert1Id);
		verticesOfNewTriangle.add(vert2Id);
		verticesOfNewTriangle.add(vert3Id);

		tempVerticesList.add(verticesOfNewTriangle);
		mTriColVertexIDsToUpdate.put(inputTriangleColours, tempVerticesList);
	}

	private void addSelfLoops(ColouredGraph mimicGraph, int estEdgesInput,
			IOfferedItem<BitSet> distColoProposerSelfLoopInput, Map<BitSet, IntSet> mMapColourToVertexIDsInput,
			IColourMappingRules mColourMapperSelfLoopInput, Map<BitSet, IntSet> mMapColourToEdgeIDsInput,
			IPropertyDist mPropDistInput) {
		LOGGER.debug("Estimated edges: " + estEdgesInput);
		int actualEdgesSimplexes = 0;
		int iterationCountSelf = 0;
		while ((estEdgesInput > actualEdgesSimplexes) && (iterationCountSelf < initializer.getMaximumNoIterations())
				&& (distColoProposerSelfLoopInput != null)) {
			BitSet proposedVertexColor = distColoProposerSelfLoopInput.getPotentialItem();
			IntSet possVertices = mMapColourToVertexIDsInput.get(proposedVertexColor);
			if (possVertices != null) {
				Integer vertexID = possVertices.toArray(new Integer[possVertices.size()])[mRandom
						.nextInt(possVertices.size())];
				boolean edgeAdded = simplexProperty.addEdgeInAnyDirection(mimicGraph, proposedVertexColor,
						proposedVertexColor, vertexID, vertexID, mMapColourToEdgeIDsInput, mColourMapperSelfLoopInput,
						mPropDistInput);
				if (edgeAdded) {
					actualEdgesSimplexes++;
					iterationCountSelf = 0;
				} else {
					iterationCountSelf++;
				}
			} else {
				iterationCountSelf++;
			}
		}
		LOGGER.debug("Added edges: " + actualEdgesSimplexes);
	}


	private void updateVertexColoMap(Map<BitSet, IntSet> mMapColourToVertexIDsinput) {
		Set<BitSet> keySetIso1Simplexes = mMapColourToVertexIDsinput.keySet();
		for (BitSet vertexColo : keySetIso1Simplexes) {
			IntSet tempSet = initializer.getmMapColourToVertexIDs().get(vertexColo);
			if (tempSet == null)
				tempSet = new DefaultIntSet(Constants.DEFAULT_SIZE);
			tempSet.addAll(mMapColourToVertexIDsinput.get(vertexColo));
		}
	}

	/**
	 * The method adds a triangle to mimic graph for the input TriangleColours
	 * Object. Edge colors are selected for the given vertex colors of the triangle
	 * to create edge and form the complete triangle
	 * 
	 * @param inputTriangleColours - TriangleColours Object
	 */
	private void addTriangleToMimicGraph(ColouredGraph mimicGraph, TriColours inputTriangleColours,
			IColourMappingRules mColourMapperToUse, Map<BitSet, IntSet> mMapColourToVertexIDsToUpdate,
			Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate,
			ObjectObjectOpenHashMap<TriColours, List<IntSet>> mTriangleColorsVertexIdsToUpdate,
			IPropertyDist mPropDistInput) {
		// storing colors of vertices for initial triangle
		BitSet vertex1Color = inputTriangleColours.getA();
		BitSet vertex2Color = inputTriangleColours.getB();
		BitSet vertex3Color = inputTriangleColours.getC();

		// create vertex for the triangle colors in the mimic graph
		int vert1Id = addVertexToMimicGraph(mimicGraph, vertex1Color, mMapColourToVertexIDsToUpdate);
		int vert2Id = addVertexToMimicGraph(mimicGraph, vertex2Color, mMapColourToVertexIDsToUpdate);
		int vert3Id = addVertexToMimicGraph(mimicGraph, vertex3Color, mMapColourToVertexIDsToUpdate);

		// Add edges between found vertices
		simplexProperty.addEdgeToMimicGraph(mimicGraph, vertex1Color, vertex2Color, vert1Id, vert2Id,
				mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours, mPropDistInput);
		simplexProperty.addEdgeToMimicGraph(mimicGraph, vertex1Color, vertex3Color, vert1Id, vert3Id,
				mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours, mPropDistInput);
		simplexProperty.addEdgeToMimicGraph(mimicGraph, vertex2Color, vertex3Color, vert2Id, vert3Id,
				mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours, mPropDistInput);

		// update the map for trackign the colours of the triangle
		updateMapTriangleColorsVertices(vert1Id, vert2Id, vert3Id, inputTriangleColours,
				mTriangleColorsVertexIdsToUpdate);

	}

	

	

	/**
	 * This method is used to update the map that tracks the triangle colours and
	 * vertices for those colours.
	 * 
	 * @param vert1Id          - Integer vertex id for the first vertex of the
	 *                         triangle
	 * @param vert2Id          - Integer vertex id for the second vertex of the
	 *                         triangle
	 * @param vert3Id          - Integer vertex id for the third vertex of the
	 *                         triangle
	 * @param inputEdgeColours - TriangleColours object with the colours for the
	 *                         vertices of the triangle
	 */
	private void updateMapEdgeColorsVertices(int vert1Id, int vert2Id, EdgeColos inputEdgeColours) {

		// list of vertex ids, initially checks if the vertex ids exists for the input
		// triangle colours
		List<IntSet> tempVerticesList = initializer.getmEdgeColorsVertexIds().get(inputEdgeColours);
		if (tempVerticesList == null) {
			tempVerticesList = new ArrayList<IntSet>();
		}

		// create set for vertex ids
		IntSet verticesOfNewEdge = new DefaultIntSet(Constants.DEFAULT_SIZE);
		verticesOfNewEdge.add(vert1Id);
		verticesOfNewEdge.add(vert2Id);

		tempVerticesList.add(verticesOfNewEdge);
		initializer.getmEdgeColorsVertexIds().put(inputEdgeColours, tempVerticesList);
	}


	@Override
	public TripleBaseSingleID getProposedTriple() {
		int max = 1000;
		for (int j = 0; j < max; j++) {

			// get proposed edge colour
			BitSet edgeColour = classSelector.getEdgeColourProposal();

			// let it fall if edgeColour is null, there's something really wrong if it's
			// null

			// get tail and head colour proposers from edge colour with n attempts
			Set<BitSet> availableColours = initializer.getAvailableVertexColours();
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
				Set<Integer> connectedHeads = initializer.getConnectedHeadsSet(tailId, edgeColour);
				Integer headId = headProposer.getPotentialItemRemove(connectedHeads);
				if (headId == null)
					continue;

				// check if they can connect
				if (connectableVertices(tailId, headId, edgeColour, initializer)) {
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

	public int getProposedVertex(Map<BitSet, IntSet> map, BitSet vertexColour) {
		Integer[] arrIDs = map.get(vertexColour).toArray(Integer[]::new);
		OfferedItemWrapper<Integer> item = new OfferedItemWrapper<Integer>(arrIDs, initializer.getSeedGenerator());
		Integer result = null;
		for (int i = 0; i < initializer.getMaximumNoIterations(); i++) {
			result = item.getPotentialItem();
			if (result != null) {
				return result;
			}
		}
		return result;
	}

}
