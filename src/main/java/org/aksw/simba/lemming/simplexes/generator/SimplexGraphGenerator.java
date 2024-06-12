package org.aksw.simba.lemming.simplexes.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphInitializer;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.SimplexGraphInitializer;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.EdgeColos;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.simplexes.analysis.SimplexAnalysis;
import org.aksw.simba.lemming.simplexes.distribution.IPropertyDist;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexClass;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexProperty;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Sets.SetView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class is responsible for the graph generation process of the
 * simplex-based approaches.
 *
 */
@Component("Simplex")
@Scope(value = "prototype")
public class SimplexGraphGenerator implements IGraphGenerator{
	private static final Logger LOGGER = LoggerFactory.getLogger(SimplexGraphGenerator.class);

	/** Initializes the graph. Serves as a store. */
	private SimplexGraphInitializer initializer;

	/** Class Selector instance. Dictates the class sampling strategy. */
	private ISimplexClass simplexClass;

	/** Property Selector instance. Dictates the property sampling strategy. */
	private ISimplexProperty simplexProperty;

	private Random mRandom;

	/**
	 * Constructor.
	 * 
	 * @param initializer
	 * @param simplexClass
	 * @param simplexProperty
	 */
	public SimplexGraphGenerator(SimplexGraphInitializer initializer, ISimplexClass simplexClass,
			ISimplexProperty simplexProperty) {
		this.initializer = initializer;
		this.simplexClass = simplexClass;
		this.simplexProperty = simplexProperty;
		mRandom = new Random(initializer.getSeedGenerator().getNextSeed());
	}

	@Override
	public void initializeMimicGraph(ColouredGraph mimicGraph, int noOfThreads) {
		SimplexAnalysis analysis = initializer.getSimplexAnalysis();
		int estimatedEdgesTriangle = analysis.getConnTriAnalysis().getEstEdges();
		int estimatedVerticesTriangle = analysis.getConnTriAnalysis().getEstVertices();
		initializer.setmTriColosCountsAvgProb(simplexClass.getTriangleDistribution().getmTriangleColorsv1v2v3());
		ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> mTriColosCountsAvgProb = initializer
				.getmTriColosCountsAvgProb(); 

		// *** 2-simplex creation (that could be connected to each other) ***
		LOGGER.info("Case 1: Model higher dimensional simplexes with 2-simplexes");
		LOGGER.info("Estimated Edges: " + estimatedEdgesTriangle);
		LOGGER.info("Estimated Vertices: " + estimatedVerticesTriangle);

		// get random triangle
		TriColours initialRandomTriangle = simplexClass.getTriangleProposal();
		int maximumIteration = initializer.getMaximumNoIterations();

		// Variable to track number of edges added to mimic graph in previous iteration.
		// Note: This variable is used to stop the iteration if no new edges could be
		// added to the mimic graph after trying for predefined number of iterations
		int numOfIterationAddingEdgesToGraph = 0;

		if ((initialRandomTriangle != null) && (estimatedEdgesTriangle >= 3) && (estimatedVerticesTriangle >= 3)) {

			// Update the count of triangles in the map
			double[] arrTriProbCount = mTriColosCountsAvgProb.get(initialRandomTriangle.getA())
					.get(initialRandomTriangle.getB()).get(initialRandomTriangle.getC());
			arrTriProbCount[3] = arrTriProbCount[3] - 1; // Triangle count is stored at first index, updating its count.
			// Note: As it is the first triangle that is added not validating if we are
			// allowed to add a triangle or not.

			// Variables to track number of edges and vertices added in triangle
			int actualEdgesInTriangles = 0;
			int actualVerticesInTriangles = 0;

			// Variable to track edges that cannot form triangle
			// IntSet edgesNotFormingTriangle = new DefaultIntSet(Constants.DEFAULT_SIZE);
			Set<EdgeColorsSorted> edgeColosSet = new HashSet<EdgeColorsSorted>();
			edgeColosSet.add(new EdgeColorsSorted(initialRandomTriangle.getA(), initialRandomTriangle.getB()));
			edgeColosSet.add(new EdgeColorsSorted(initialRandomTriangle.getA(), initialRandomTriangle.getC()));
			edgeColosSet.add(new EdgeColorsSorted(initialRandomTriangle.getB(), initialRandomTriangle.getC()));

			// Variable to track set of triangle added to the mimic graph (i.e. set of
			// Colors of the vertices forming the triangle)
			Set<TriColours> setTriangleColorsMimicGraph = new HashSet<TriColours>();

			// add the selected random triangle to mimic graph
			addTriangleToMimicGraphWithPropProb(mimicGraph, initialRandomTriangle,
					analysis.getConnTriAnalysis().getmColourMapperSimplexes(),
					initializer.getmMapColourToVertexIDs2Simplex(), initializer.getmMapColourToEdgeIDs2Simplex(),
					initializer.getmTriangleColorsVertexIds(), simplexProperty.getmPropDistConnTri());

			// increment no of vertices in triangle
			actualVerticesInTriangles = actualVerticesInTriangles + 3;

			// increment no. of edges in triangle
			actualEdgesInTriangles = actualEdgesInTriangles + 3;

			// Add the triangle colors to set variable
			setTriangleColorsMimicGraph.add(initialRandomTriangle);

			//
			numOfIterationAddingEdgesToGraph = 0;
			while (actualEdgesInTriangles < estimatedEdgesTriangle) {
				if (actualVerticesInTriangles == 16826)
					System.out.println();
				if ((actualVerticesInTriangles < estimatedVerticesTriangle)
						&& (numOfIterationAddingEdgesToGraph < maximumIteration)) {
					// If we can add more triangles when we are allowed to include additional
					// vertices otherwise edges need to be added to existing triangles

					// get all edges of the mimic graph
					// IntSet edgesMimicGraph = mimicGraph.getEdges();
					// edgesMimicGraph = IntSetUtil.difference(edgesMimicGraph,
					// edgesNotFormingTriangle);

					if (edgeColosSet.size() != 0) {
						// Continue to randomly select an edge and grow the graph by adding triangle
						// only if candidate edges are found. These candidate edges will be evaluated to
						// check if triangles can be created for them.

						// get vertex colors with high probability from the
						// colors added to the mimic graph
						EdgeColorsSorted potentialItem = simplexClass.getEdgeProposalFromTriangleDist(edgeColosSet);

						IntSet setVertices1 = initializer.getmMapColourToVertexIDs2Simplex().get(potentialItem.getA());
						IntSet setVertices2 = initializer.getmMapColourToVertexIDs2Simplex().get(potentialItem.getB());

						if ((setVertices1 == null) || (setVertices2 == null)) {
							numOfIterationAddingEdgesToGraph++;
							if (edgeColosSet.contains(potentialItem)) {
								edgeColosSet.remove(potentialItem);
							}
							continue;
						}

						int selectedVertex1 = setVertices1.toArray(new Integer[setVertices1.size()])[mRandom
								.nextInt(setVertices1.size())],
								selectedVertex2 = setVertices2.toArray(new Integer[setVertices2.size()])[mRandom
										.nextInt(setVertices2.size())];

						BitSet selectedVertex1Colo = mimicGraph.getVertexColour(selectedVertex1);
						BitSet selectedVertex2Colo = mimicGraph.getVertexColour(selectedVertex2);

						// Get the color for the third vertex
						BitSet proposedVertex3Colo = simplexClass.proposeVertex3Colour(selectedVertex1Colo,
								selectedVertex2Colo);

//								triangleDistribution
//								.proposeVertexColorForVertex3();

						// Add new Triangle for the selected vertices

						// boolean variable to track if new edge are added
						boolean newEdgesNotAddedToTriangle = true;

						if (proposedVertex3Colo != null) {
							// If third vertex color is proposed, create a triangle with it

							// create a temporary triangle colors object
							TriColours newPossibleTriangle = new TriColours(selectedVertex1Colo, selectedVertex2Colo,
									proposedVertex3Colo);
							// System.out.println(newPossibleTriangle.getA());
							// System.out.println(newPossibleTriangle.getB());
							// System.out.println(newPossibleTriangle.getC());

							// get triangle count
							double[] arrNewPossTriProbCount = mTriColosCountsAvgProb.get(newPossibleTriangle.getA())
									.get(newPossibleTriangle.getB()).get(newPossibleTriangle.getC());// get count of
																										// triangle for
																										// the proposed
																										// new triangle

							// temporary variable to track count of loops
							int numOfLoopsTri = 0;

							// try to propose a color for third vertex multiple times if it is not possible
							// to create a triangle
							while ((arrNewPossTriProbCount[3] < 1) && (numOfLoopsTri < 500)) { // trying to create a new
																								// triangle 100 times
								proposedVertex3Colo = simplexClass.proposeVertex3Colour(selectedVertex1Colo,
										selectedVertex2Colo);
								newPossibleTriangle = new TriColours(selectedVertex1Colo, selectedVertex2Colo,
										proposedVertex3Colo);
								arrNewPossTriProbCount = mTriColosCountsAvgProb.get(newPossibleTriangle.getA())
										.get(newPossibleTriangle.getB()).get(newPossibleTriangle.getC());// get count of
																											// triangle
																											// for the
																											// proposed
																											// new
																											// triangle
								numOfLoopsTri++;
							}

							// if (( !setTrianglesForEdge.contains( newPossibleTriangle ) )) {
							if (arrNewPossTriProbCount[3] >= 1) {
								// Update the count of triangle since a triangle will be created
								arrNewPossTriProbCount[3] = arrNewPossTriProbCount[3] - 1;

								// create vertex for the proposed color
								int proposedVertId = addVertexToMimicGraph(mimicGraph, proposedVertex3Colo,
										initializer.getmMapColourToVertexIDs2Simplex());

								// add edges among selected vertices and proposed color
								// Note: Ideally properties should exist among them. since they were also
								// forming a triangle in input graphs
								addEdgeTriangleWithPropProb(mimicGraph, selectedVertex1Colo, proposedVertex3Colo,
										selectedVertex1, proposedVertId, initializer.getmMapColourToEdgeIDs2Simplex(),
										analysis.getConnTriAnalysis().getmColourMapperSimplexes(), newPossibleTriangle,
										simplexProperty.getmPropDistConnTri());
								addEdgeTriangleWithPropProb(mimicGraph, selectedVertex2Colo, proposedVertex3Colo,
										selectedVertex2, proposedVertId, initializer.getmMapColourToEdgeIDs2Simplex(),
										analysis.getConnTriAnalysis().getmColourMapperSimplexes(), newPossibleTriangle,
										simplexProperty.getmPropDistConnTri());
								// increment number of vertices and edges added in the mimic graph for triangles
								actualVerticesInTriangles = actualVerticesInTriangles + 1;
								actualEdgesInTriangles = actualEdgesInTriangles + 2;

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
						}

						if (newEdgesNotAddedToTriangle) {
							// Logic if no third vertex color could be proposed
							// Don't consider the randomly selected edge, since it is not able to form a
							// triangle
							edgeColosSet.remove(potentialItem);
							numOfIterationAddingEdgesToGraph++;
						}

					} // end if condition - check if triangles can be added to the edges
					else {
						LOGGER.info("Growing 2-simplexes not possible.... Proposing new 2-simplex");
						// If no candidate edges exist then new random triangle should be added to the
						// mimic graph
						TriColours randomTriangle = simplexClass.getTriangleProposal();

						// get triangle count
						double[] arrNewTriProbCount = mTriColosCountsAvgProb.get(randomTriangle.getA())
								.get(randomTriangle.getB()).get(randomTriangle.getC());// get count of triangle for the
																						// proposed new triangle

						// variable to track number of times a random triangle was selected
						int numOfIterationRandomTri = 1;

						// check if it is possible to add new triangle
						while ((arrNewTriProbCount[3] < 1) && (numOfIterationRandomTri < maximumIteration)) { // discontinue after
																									// trying 500 times
							randomTriangle = getRandomTriangle(initializer.getSetAllTriangleColours()); // FIXME
							arrNewTriProbCount = mTriColosCountsAvgProb.get(randomTriangle.getA())
									.get(randomTriangle.getB()).get(randomTriangle.getC());// get count of triangle for
																							// the proposed new triangle
							numOfIterationRandomTri++;
						}

						if (arrNewTriProbCount[3] > 1) {
							// Update the triangle count
							arrNewTriProbCount[3] = arrNewTriProbCount[3] - 1;

							// Add the triangle to mimic graph
							addTriangleToMimicGraphWithPropProb(mimicGraph, randomTriangle,
									analysis.getConnTriAnalysis().getmColourMapperSimplexes(),
									initializer.getmMapColourToVertexIDs2Simplex(),
									initializer.getmMapColourToEdgeIDs2Simplex(),
									initializer.getmTriangleColorsVertexIds(), simplexProperty.getmPropDistConnTri());

							// increment no of vertices in triangle
							actualVerticesInTriangles = actualVerticesInTriangles + 3;

							// increment no. of edges in triangle
							actualEdgesInTriangles = actualEdgesInTriangles + 3;

							edgeColosSet.add(new EdgeColorsSorted(randomTriangle.getA(), randomTriangle.getB()));
							edgeColosSet.add(new EdgeColorsSorted(randomTriangle.getA(), randomTriangle.getC()));
							edgeColosSet.add(new EdgeColorsSorted(randomTriangle.getB(), randomTriangle.getC()));

							// Add the triangle colors to set variable
							setTriangleColorsMimicGraph.add(new TriColours(randomTriangle.getA(), randomTriangle.getB(),
									randomTriangle.getC()));
							numOfIterationAddingEdgesToGraph = 0;
						} else {
							break; // terminate while condition if it is not possible to add random triangle
						}

					}
				} else {
					break; // Cannot add more vertices
				}
			} // end while condition checking if actual number of edges is less than estimated
				// number of edges
			LOGGER.info("Growing 2-simplexes phase completed");
			LOGGER.info("Added Edges: " + actualEdgesInTriangles);
			LOGGER.info("Added Vertices: " + actualVerticesInTriangles);

			LOGGER.info("Adding additional Edges to created 2-simplexes");

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
					IntSet selectedVertices = selectedTrianglesList.toArray(
							new IntSet[selectedTrianglesList.size()])[mRandom.nextInt(selectedTrianglesList.size())];

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

						boolean edgeAdded = addEdgeInAnyDirectionDuplCheckWithPropProb(mimicGraph, existingVertexColo1,
								existingVertexColo2, pairOfVertices.get(0), pairOfVertices.get(1),
								initializer.getmMapColourToEdgeIDs2Simplex(),
								analysis.getConnTriAnalysis().getmColourMapperSimplexes(),
								simplexProperty.getmPropDistConnTri());

						if (edgeAdded) {
							// increment no. of edges in triangle
							actualEdgesInTriangles = actualEdgesInTriangles + 1;
							numOfIterationAddingEdgesToGraph = 0;// update count to 0 since edge was added successfully
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
		LOGGER.info("Case 1 completed!");
		LOGGER.info("Added Edges: " + mimicGraph.getEdges().size());
		LOGGER.info("Added Vertices: " + mimicGraph.getVertices().size());

		// ************************ Logic for isolated 1-simplexes
		// ***********************************//

		// temporary variables to track addition of vertices and edges for 1-simplexes
		int actualVerticesSimplexes = 0;
		int actualEdgesSimplexes = 0;

		// initialize variable tracking iteration count for this case
		numOfIterationAddingEdgesToGraph = 0;

		// get head proposer defined for 1-simplex distribution
		IOfferedItem<EdgeColos> potentialEdgeColoProposer = simplexProperty.getIsolatedEdgeProposer();
//				s1ConnDist
//				.getPotentialIsolatedEdgeColoProposer();

		int estimatedEdges1Simplexes = analysis.getIsoS1Analysis().getEstEdges();
		int estimatedVertices1Simplexes = analysis.getIsoS1Analysis().getEstVertices();
		LOGGER.info("Case 2a: Isolated 1-simplexes (with different source and target node)");
		LOGGER.info("Estimated Edges: " + estimatedEdges1Simplexes);
		LOGGER.info("Estimated Vertices: " + estimatedVertices1Simplexes);

		while ((estimatedEdges1Simplexes > actualEdgesSimplexes) && (potentialEdgeColoProposer != null)
				&& (numOfIterationAddingEdgesToGraph < maximumIteration)) { // additional condition that color proposer
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

				while ((vertexIDshead.size() > 0) && (vertexIDTail == -1)) { // randomly select a head vertex and check
																				// if it has a tail with proposed color

					vertexIDHead = vertexIDshead.toArray(new Integer[vertexIDshead.size()])[mRandom
							.nextInt(vertexIDshead.size())];

					// get neighbors of selected vertex head
					IntSet neighbors = IntSetUtil.union(mimicGraph.getInNeighbors(vertexIDHead),
							mimicGraph.getOutNeighbors(vertexIDHead));

					IntSet vertexIDstail = new DefaultIntSet(Constants.DEFAULT_SIZE);// temporary set for storing
																						// potential tail IDs

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

			boolean edgeAdded = addEdgeIsoS1WithTriCheck(mimicGraph, potentialheadColo, potentialTailColo, vertexIDHead,
					vertexIDTail, initializer.getmMapColourToEdgeIDs1Simplex(),
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
		LOGGER.info("Case 2a completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);

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
		LOGGER.info("Case 2b: Isolated self loop");
		LOGGER.info("Estimated Edges: " + estimatedEdgesIsoSelfLoop);
		LOGGER.info("Estimated Vertices: " + estimatedVerticesIsoSelfLoop);

		numOfIterationAddingEdgesToGraph = 0; // initialize iteration count
		while ((estimatedEdgesIsoSelfLoop > actualEdgesSimplexes) && (potentialHeadColoProposer != null)
				&& (numOfIterationAddingEdgesToGraph < maximumIteration)) { // additional condition that color proposer
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

			boolean edgeAdded = addEdgeIsoS1WithTriCheck(mimicGraph, potentialColoSelfLoop, potentialColoSelfLoop,
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

		LOGGER.info("Case 2b completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);

		// ************************ Logic for 0-simplexes
		// ***********************************//
		int estimatedVertices0Simplexes = analysis.getS0Analysis().getEstVertices();
		LOGGER.info("Case 3: Isolated 0-simplexes");
		LOGGER.info("Estimated Vertices: " + estimatedVertices0Simplexes);
		// define proposer for 0-simplexes
		IOfferedItem<BitSet> potentialColoProposer0Simplex = simplexClass.getColourPointProposer();

		// initialize tracking variable
		actualVerticesSimplexes = 0;

		while ((actualVerticesSimplexes < estimatedVertices0Simplexes) && (potentialColoProposer0Simplex != null)) {
			// get possible color
			BitSet potentialColo0Simplex = potentialColoProposer0Simplex.getPotentialItem();

			// Add 0-simplex to mimic graph
			addVertexToMimicGraph(mimicGraph, potentialColo0Simplex, initializer.getmMapColourToVertexIDs0Simplex());
			actualVerticesSimplexes++;
		}
		LOGGER.info("Case 3 completed!");
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);

		// *********************** Logic for connecting triangles using 1-simplexes
		// *********************//
		// Initially, find isolated triangles. We can proceed with this case only if
		// isolated triangles are added to the mimic graph by previous process
		int estimatedEdges1SimplexesConnect2Simplexes = analysis.getS1ConnectingTri().getEstEdges();
		LOGGER.info("Case 4: Connecting two-simplexes with 1-simplex");
		LOGGER.info("Estimated Edges: " + estimatedEdges1SimplexesConnect2Simplexes);

		// ********************** Connect Isolated triangles
		// *********************************//
		LOGGER.info("Finding Isolated triangles to connect (Case 4a)");
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
			LOGGER.info("Isolated triangles found connecting them with 1-simplex.");

			// initialize variable to track number of edges added in the mimic graph
			actualEdgesSimplexes = 0;

			// initialize variable tracking iteration count for this case
			numOfIterationAddingEdgesToGraph = 0;

			IOfferedItem<EdgeColos> headColoProposerIsolatedTri = simplexClass.getIsolatedHeadProposer();

			while ((actualEdgesSimplexes < estimatedEdges1SimplexesConnect2Simplexes)
					&& (numOfIterationAddingEdgesToGraph < maximumIteration) && (headColoProposerIsolatedTri != null)) {

				// get head color from the colors available in the triangle
				BitSet potentialHeadColoIsolatedTri = null; // initialize head color

				EdgeColos potentialItem = headColoProposerIsolatedTri.getPotentialItem();

				if (potentialItem != null)// check if head color proposer is not null
					potentialHeadColoIsolatedTri = potentialItem.getA();

				if (potentialHeadColoIsolatedTri != null) { // check for tail color only if head color is not null

					// get tail colors based on head color and available colors in the triangle
					BitSet potentialTailColoIsolatedTri = potentialItem.getB(); // initialize tail color

					if (potentialTailColoIsolatedTri != null) { // try to connect triangles using 1-simplexes if tail
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
						List<IntSet> possVerticesListhead = mTriangleColorsVertexIdsIsolated.get(potentialTriangleHead);
						IntSet verticesTriIsolatedhead = possVerticesListhead
								.get(mRandom.nextInt(possVerticesListhead.size()));

						// get vertices for tail color
						List<IntSet> possVerticesListtail = mTriangleColorsVertexIdsIsolated.get(potentialTriangleTail);
						IntSet verticesTriIsolatedtail = possVerticesListtail
								.get(mRandom.nextInt(possVerticesListtail.size()));

						// compute concrete vertex id for head color and tail color
						for (int vertexIDheadIsoTri : verticesTriIsolatedhead) {
							if (mimicGraph.getVertexColour(vertexIDheadIsoTri).equals(potentialHeadColoIsolatedTri)) {
								possHeadIDIsolatedTri = vertexIDheadIsoTri;
								break;
							}
						}

						for (int vertexIDTailIsoTri : verticesTriIsolatedtail) {
							if (mimicGraph.getVertexColour(vertexIDTailIsoTri).equals(potentialTailColoIsolatedTri)) {
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
							updateMappingOfEdgeColoHeadTailColo(possEdgeColo, possHeadIDIsolatedTri,
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
							LOGGER.info("Two isolated triangles connected with 1-simplex");
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
		LOGGER.info("Trying to connect set of connected triangles using 1-simplex (Case 4b)");

		// initialize variable tracking iteration count for this case
		numOfIterationAddingEdgesToGraph = 0;
		IOfferedItem<EdgeColos> headColoProposercase4b = simplexClass.getIsolatedHeadProposer();

		while ((actualEdgesSimplexes < estimatedEdges1SimplexesConnect2Simplexes)
				&& (numOfIterationAddingEdgesToGraph < maximumIteration) && (headColoProposercase4b != null)) { // check
																												// if we
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
						List<IntSet> possVerticesListhead = mTriangleColorsVertexIds.get(potentialTriangleHeadCase4b);
						IntSet verticesheadcase4b = possVerticesListhead
								.get(mRandom.nextInt(possVerticesListhead.size()));

						// get vertices for tail color
						List<IntSet> possVerticesListtail = mTriangleColorsVertexIds.get(potentialTriangleTailCase4b);
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
						boolean edgeAdded = addEdgeIsoS1WithTriCheck(mimicGraph, potentialHeadColocase4b,
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

		LOGGER.info("Case 4 completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);

		// ******************* Logic for creating isolated 2-simplexes
		// ***********************//
		int estimatedEdgesIsolatedTriangle = analysis.getIsoTriAnalysis().getEstEdges();
		int estimatedVerticesIsolatedTriangle = analysis.getIsoTriAnalysis().getEstVertices();
		LOGGER.info("Case 5: Isolated 2-simplexes");
		LOGGER.info("Estimated Edges: " + estimatedEdgesIsolatedTriangle);
		LOGGER.info("Estimated Vertices: " + estimatedVerticesIsolatedTriangle);
		IOfferedItem<TriColours> potentialIsolatedTriangleProposer = simplexClass
				.getIsolatedTriangleProposer();
		ObjectObjectOpenHashMap<EdgeColos, double[]> mEdgesColorsCountDistAvg = analysis.getConnS1Analysis()
				.getmColoEdgesCountDistAvg();

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
			addTriangleToMimicGraphWithPropProb(mimicGraph, possIsoTri,
					analysis.getIsoTriAnalysis().getmColourMapperSimplexes(),
					initializer.getmMapColourToVertexIDs2SimplexIsolated(),
					initializer.getmMapColourToEdgeIDs2SimplexIsolated(),
					initializer.getmIsolatedTriangleColorsVertexIds(), simplexProperty.getmPropDistIsoTri());

			setIsoTriInMimicGraph.add(possIsoTri);

			// increment no of vertices in triangle
			actualVerticesSimplexes = actualVerticesSimplexes + 3;

			// increment no. of edges in triangle
			actualEdgesSimplexes = actualEdgesSimplexes + 3;
		}

		int iterationCount = 0;

		while ((estimatedEdgesIsolatedTriangle > actualEdgesSimplexes) && (iterationCount < maximumIteration)) {

			if (setIsoTriInMimicGraph.size() > 0) {

				// Logic for adding edges to existing triangles
				TriColours proposeTriangleToAddEdge = simplexClass.proposeIsoTriangleToAddEdge(setIsoTriInMimicGraph);

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
				differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[1]));
				differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[1], vertexIDExistingTriangle[2]));
				differentPairsOfVertices.add(Arrays.asList(vertexIDExistingTriangle[0], vertexIDExistingTriangle[2]));

				for (List<Integer> pairOfVertices : differentPairsOfVertices) {

					// Get vertex colours for a pair
					BitSet existingVertexColo1 = mimicGraph.getVertexColour(pairOfVertices.get(0));
					BitSet existingVertexColo2 = mimicGraph.getVertexColour(pairOfVertices.get(1));

					boolean edgeAdded = addEdgeInAnyDirectionDuplCheckWithPropProb(mimicGraph, existingVertexColo1,
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

		LOGGER.info("Case 5 completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);

		// ********************** Growing 1-simplexes (connected 1-simplexes)
		// ****************************************//
		int estEdgesConnected1Simplexes = analysis.getConnS1Analysis().getEstEdges();
		int actualVerticesMimicGraph = mimicGraph.getVertices().size();

		// subtracting estimated number of vertices for 1-simplexes connected only to
		// triangles. Such vertices are created in next step.
		int estVerticesConnected1Simplexes = initializer.getDesiredNoOfVertices() - actualVerticesMimicGraph
				- analysis.getS1ConnToTri().getEstVertices();

		LOGGER.info("Case 6: Connected 1-simplexes");
		LOGGER.info("Estimated Edges: " + estEdgesConnected1Simplexes);
		LOGGER.info("Estimated Vertices: " + estVerticesConnected1Simplexes);

		// initialize variable tracking iteration count for this case
		numOfIterationAddingEdgesToGraph = 0;

		EdgeColos initialRandomEdge = simplexProperty.proposeConnEdge();

		// Variables to track number of edges and vertices added in triangle
		int actualEdgesInConnS1 = 0;
		int actualVerticesInConnS1 = 0;

		if ((initialRandomEdge != null) && (estEdgesConnected1Simplexes >= 1)
				&& (estVerticesConnected1Simplexes >= 2)) {

			// Update the count in the map
			ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> mEdgeColosv1v2Dist = simplexProperty
					.getEdgeColoursV1V2Dist();

			double[] arrTriProbCount = mEdgeColosv1v2Dist.get(initialRandomEdge.getA()).get(initialRandomEdge.getB());
			arrTriProbCount[2] = arrTriProbCount[2] - 1;

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

			addEdgeIsoS1WithTriCheck(mimicGraph, potentialHeadColo, potentialTailColo, vertexIDHead, vertexIDTail,
					initializer.getmMapColourToEdgeIDs1Simplex(),
					analysis.getConnS1Analysis().getmColourMapperSimplexes(), false,
					simplexProperty.getmPropDistconnS1Analysis());

			updateMapEdgeColorsVertices(vertexIDHead, vertexIDTail, initialRandomEdge); // update the map to track edges
																						// along with vertices added

			// set to track class colors. New vertices need to be created for them later
			Set<BitSet> vertexClassColoSet = new HashSet<BitSet>();

			// check class nodes for potential head color (Note: This check is essential
			// since we need to add required number of vertices in the output graph)
			Set<BitSet> classColourSet = mimicGraph.getClassColour(potentialHeadColo);
			int numberOfClassVertices = 0;
			for (BitSet classColour : classColourSet) {
				Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes().get(classColour);
				if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
					numberOfClassVertices++;// class vertex need to be created for this vertex
					vertexClassColoSet.add(classColour);
				}
			}

			// update vertices count
			estVerticesConnected1Simplexes = estVerticesConnected1Simplexes - numberOfClassVertices;

			classColourSet = mimicGraph.getClassColour(potentialTailColo);
			numberOfClassVertices = 0;
			for (BitSet classColour : classColourSet) {
				Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes().get(classColour);
				if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
					numberOfClassVertices++;// class vertex need to be created for this vertex
					vertexClassColoSet.add(classColour);
				}
			}

			estVerticesConnected1Simplexes = estVerticesConnected1Simplexes - numberOfClassVertices;

			// increment no of vertices in triangle
			actualVerticesInConnS1 = actualVerticesInConnS1 + 2;

			// increment no. of edges in triangle
			actualEdgesInConnS1 = actualEdgesInConnS1 + 1;

			// Add the triangle colors to set variable
			setEdgeColorsMimicGraph.add(initialRandomEdge);

			// initial head color proposer
			IOfferedItem<BitSet> potentialHeadColoProposerConnS1 = simplexClass.getConnHeadProposer();

			numOfIterationAddingEdgesToGraph = 0; // initialize iteration count
			while (actualEdgesInConnS1 < estEdgesConnected1Simplexes) {

				// if(actualVerticesInConnS1 < estVerticesConnected1Simplexes) {
				if ((actualVerticesInConnS1 < estVerticesConnected1Simplexes)
						&& (numOfIterationAddingEdgesToGraph < maximumIteration)) {
					// If we can add more edges when we are allowed to include additional vertices
					// otherwise edges need to be added to existing 1-simplexes

					if (setOfColosInGraph.size() != 0) {
						// Propose a head color from existing colors in the mimic graph
						BitSet proposedHeadColo = potentialHeadColoProposerConnS1.getPotentialItem(setOfColosInGraph);

						IntSet verticesWithProposedColor = initializer.getmMapColourToVertexIDs1SimplexConnected()
								.get(proposedHeadColo);
						if (verticesWithProposedColor == null) {// Propose a new head color when none present
							if (setOfColosInGraph.contains(proposedHeadColo)) {
								setOfColosInGraph.remove(proposedHeadColo);
							}
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
							double[] arrNewPossTriProbCount = mEdgesColorsCountDistAvg
									.get(new EdgeColos(proposedHeadColo, proposedTail));

							// temporary variable to track count of loops
							int numOfLoopsTri = 0;

							// check class vertices to create
							Set<BitSet> vertexClassColoSetTemp = new HashSet<BitSet>(); // temporary class colors

							classColourSet = mimicGraph.getClassColour(proposedTail);
							numberOfClassVertices = 0;
							for (BitSet classColour : classColourSet) {
								Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes()
										.get(classColour);
								if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
									numberOfClassVertices++;// class vertex need to be created for this vertex
									vertexClassColoSetTemp.add(classColour);
								}
							}

							// try to propose a color for tail vertex multiple times if it is not possible
							// to create an edge
							while ((arrNewPossTriProbCount[2] < 1) && (numOfLoopsTri < 500) && ((actualVerticesInConnS1
									+ numberOfClassVertices) < estVerticesConnected1Simplexes)) {
								proposedTail = simplexClass.proposeColour(proposedHeadColo).getPotentialItem();
								// arrNewPossTriProbCount =
								// mEdgeColosv1v2Dist.get(proposedHeadColo).get(proposedTail);// get the count
								// for the proposed new 1-simplex
								arrNewPossTriProbCount = mEdgesColorsCountDistAvg
										.get(new EdgeColos(proposedHeadColo, proposedTail));
								numOfLoopsTri++;

								// check class vertices
								classColourSet = mimicGraph.getClassColour(proposedTail);
								numberOfClassVertices = 0;
								vertexClassColoSetTemp = new HashSet<BitSet>();
								for (BitSet classColour : classColourSet) {
									Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes()
											.get(classColour);
									if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
										numberOfClassVertices++;// class vertex need to be created for this vertex
										vertexClassColoSetTemp.add(classColour);
									}
								}
							}

							// propose a property based on probability distribution for the found head and
							// tail color
							BitSet proposePropColor = simplexProperty
									.proposeConnPropColour(new EdgeColorsSorted(proposedHeadColo, proposedTail));

							if ((arrNewPossTriProbCount[2] >= 1)
									&& ((actualVerticesInConnS1
											+ numberOfClassVertices) < estVerticesConnected1Simplexes)
									&& (analysis.getConnS1Analysis().getmColourMapperSimplexes()
											.canConnect(proposedHeadColo, proposedTail, proposePropColor))) {
								// Update the count of triangle since a triangle will be created
								arrNewPossTriProbCount[2] = arrNewPossTriProbCount[2] - 1;

								// create vertex for the proposed color
								int proposedVertId = addVertexToMimicGraph(mimicGraph, proposedTail,
										initializer.getmMapColourToVertexIDs1SimplexConnected());

								setOfColosInGraph.add(proposedTail);

								// add edges among selected vertices and proposed color
								// Note: Ideally properties should exist among them. since they were also
								// forming an edge in input graphs
								addEdgeConnS1WithTriCheck(mimicGraph, proposedHeadColo, proposedTail, randomVertexID,
										proposedVertId, initializer.getmMapColourToEdgeIDs1Simplex(),
										analysis.getConnS1Analysis().getmColourMapperSimplexes(), false,
										proposePropColor);

								// increment number of vertices and edges added in the mimic graph for triangles
								actualVerticesInConnS1 = actualVerticesInConnS1 + 1;
								actualEdgesInConnS1 = actualEdgesInConnS1 + 2;

								EdgeColos tempEdgeColors = new EdgeColos(proposedHeadColo, proposedTail);

								// Add the created triangle Colors to set
								setEdgeColorsMimicGraph.add(tempEdgeColors);

								// update the map to track edges along with vertices added
								updateMapEdgeColorsVertices(randomVertexID, proposedVertId, tempEdgeColors);

								// update the boolean variable
								newEdgesNotAddedToTriangle = false;

								// update class colors to consider
								vertexClassColoSet.addAll(vertexClassColoSetTemp);

								estVerticesConnected1Simplexes = estVerticesConnected1Simplexes - numberOfClassVertices;

								numOfIterationAddingEdgesToGraph = 0;
							}
						}

						if (newEdgesNotAddedToTriangle) {
							// Logic if no tail vertex color could be proposed
							// Don't consider the proposed head color, since it is not able to form a
							// 1-simplex
							setOfColosInGraph.remove(proposedHeadColo);
							numOfIterationAddingEdgesToGraph++;
						}

					} // end if condition - check if edges can grow
					else {
						LOGGER.info("Growing 1-simplexes not possible.... Proposing new 1-simplex");

						// If no tail color could be included for the existing head colors in the mimic
						// graph. Add a new 1-simplex

						EdgeColos randomEdge = simplexProperty.proposeConnEdge();

						// get count
						// double[] arrNewTriProbCount =
						// mEdgeColosv1v2Dist.get(randomEdge.getA()).get(randomEdge.getB());
						double[] arrNewTriProbCount = mEdgesColorsCountDistAvg
								.get(new EdgeColos(randomEdge.getA(), randomEdge.getB()));

						// variable to track number of times a random edge was selected
						int numOfIterationRandomTri = 1;

						Set<BitSet> vertexClassColoSetTemp = new HashSet<BitSet>(); // temporary class colors

						// check number of class vertices required for proposed head and tail colors
						classColourSet = mimicGraph.getClassColour(randomEdge.getA());
						int numberOfClassHeadVertices = 0;
						for (BitSet classColour : classColourSet) {
							Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes()
									.get(classColour);
							if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
								numberOfClassHeadVertices++;// class vertex need to be created for this vertex
								vertexClassColoSetTemp.add(classColour);
							}
						}

						classColourSet = mimicGraph.getClassColour(randomEdge.getA());
						int numberOfClassTailVertices = 0;
						for (BitSet classColour : classColourSet) {
							Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes()
									.get(classColour);
							if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))
									&& (!vertexClassColoSetTemp.contains(classColour))) {
								numberOfClassTailVertices++;// class vertex need to be created for this vertex
								vertexClassColoSetTemp.add(classColour);
							}
						}

						// check if it is possible to add new triangle
						while ((arrNewTriProbCount[2] < 1) && (numOfIterationRandomTri < 500)
								&& ((actualVerticesInConnS1 + numberOfClassHeadVertices
										+ numberOfClassTailVertices) < estVerticesConnected1Simplexes)) { // discontinue
																											// after
																											// trying
																											// 500 times
							randomEdge = simplexProperty.proposeConnEdge();
							// arrNewTriProbCount =
							// mEdgeColosv1v2Dist.get(randomEdge.getA()).get(randomEdge.getB());// get count
							// of triangle for the proposed new triangle
							arrNewTriProbCount = mEdgesColorsCountDistAvg
									.get(new EdgeColos(randomEdge.getA(), randomEdge.getB()));

							numOfIterationRandomTri++;

							vertexClassColoSetTemp = new HashSet<BitSet>(); // temporary class colors

							// check number of class vertices required for proposed head and tail colors
							classColourSet = mimicGraph.getClassColour(randomEdge.getA());
							numberOfClassHeadVertices = 0;
							for (BitSet classColour : classColourSet) {
								Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes()
										.get(classColour);
								if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
									numberOfClassHeadVertices++;// class vertex need to be created for this vertex
									vertexClassColoSetTemp.add(classColour);
								}
							}

							classColourSet = mimicGraph.getClassColour(randomEdge.getA());
							numberOfClassTailVertices = 0;
							for (BitSet classColour : classColourSet) {
								Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes()
										.get(classColour);
								if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))
										&& (!vertexClassColoSetTemp.contains(classColour))) {
									numberOfClassTailVertices++;// class vertex need to be created for this vertex
									vertexClassColoSetTemp.add(classColour);
								}
							}
						}

						// check property color for selected head and tail colors
						BitSet proposePropColor = simplexProperty
								.proposeColour(new EdgeColorsSorted(randomEdge.getA(), randomEdge.getB()));

						if ((arrNewTriProbCount[2] > 1)
								&& ((actualVerticesInConnS1 + numberOfClassHeadVertices
										+ numberOfClassTailVertices) < estVerticesConnected1Simplexes)
								&& (analysis.getConnS1Analysis().getmColourMapperSimplexes()
										.canConnect(randomEdge.getA(), randomEdge.getB(), proposePropColor))) {
							// Update the triangle count
							arrNewTriProbCount[2] = arrNewTriProbCount[2] - 1;

							potentialHeadColo = randomEdge.getA();
							potentialTailColo = randomEdge.getB();

							setOfColosInGraph.add(potentialHeadColo);
							setOfColosInGraph.add(potentialTailColo);

							vertexIDTail = addVertexToMimicGraph(mimicGraph, potentialTailColo,
									initializer.getmMapColourToVertexIDs1SimplexConnected());
							vertexIDHead = addVertexToMimicGraph(mimicGraph, potentialHeadColo,
									initializer.getmMapColourToVertexIDs1SimplexConnected());

							addEdgeWithTriangleCheck(mimicGraph, potentialHeadColo, potentialTailColo, vertexIDHead,
									vertexIDTail, initializer.getmMapColourToEdgeIDs1Simplex(),
									analysis.getConnS1Analysis().getmColourMapperSimplexes(), false);

							// increment no of vertices in triangle
							actualVerticesInConnS1 = actualVerticesInConnS1 + 2;

							// increment no. of edges in triangle
							actualEdgesInConnS1 = actualEdgesInConnS1 + 1;

							// Add the edge colors to set variable
							setEdgeColorsMimicGraph.add(randomEdge);

							// update class colors to consider
							vertexClassColoSet.addAll(vertexClassColoSetTemp);

							updateMapEdgeColorsVertices(vertexIDHead, vertexIDTail, randomEdge); // update the map to
																									// track edges along
																									// with vertices
																									// added

							estVerticesConnected1Simplexes = estVerticesConnected1Simplexes - numberOfClassHeadVertices
									- numberOfClassTailVertices;

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
			LOGGER.info("Growing 1-simplexes phase completed");
			LOGGER.info("Added Edges: " + actualEdgesInConnS1);
			LOGGER.info("Added Vertices: " + actualVerticesInConnS1);

			LOGGER.info("Adding additional Edges to created 1-simplexes");

			numOfIterationAddingEdgesToGraph = 0; // initialize iteration count
			while ((actualEdgesInConnS1 < estEdgesConnected1Simplexes)
					&& (numOfIterationAddingEdgesToGraph < maximumIteration)) { // Case: Triangles cannot be added to
																				// the mimic graph but edges can be
																				// added to existing triangles

				if (setEdgeColorsMimicGraph.size() != 0) {

					// Logic for adding edges to existing connected 1-simplexes
					EdgeColos proposeS1 = simplexProperty.proposeTriangleToAddEdge(setEdgeColorsMimicGraph);

					// Best case: A triangle is returned
					List<IntSet> selectedEdgesList = initializer.getmEdgeColorsVertexIds().get(proposeS1);

					// randomly selecting one of these triangles
					IntSet selectedVertices = selectedEdgesList.toArray(new IntSet[selectedEdgesList.size()])[mRandom
							.nextInt(selectedEdgesList.size())];

					// Convert vertices to Array
					Integer[] vertexIDExisting = selectedVertices.toArray(new Integer[selectedVertices.size()]);

					Integer existingVertexID1 = vertexIDExisting[0];
					Integer existingVertexID2 = vertexIDExisting[1];
					BitSet existingVertexColo1 = mimicGraph.getVertexColour(existingVertexID1);
					BitSet existingVertexColo2 = mimicGraph.getVertexColour(existingVertexID2);

					// triangle check not required since working with 1-simplexes instead of
					// sampling head and tail
					boolean edgeAdded = addEdgeIsoS1WithTriCheck(mimicGraph, existingVertexColo1, existingVertexColo2,
							existingVertexID1, existingVertexID2, initializer.getmMapColourToEdgeIDs1Simplex(),
							analysis.getConnS1Analysis().getmColourMapperSimplexes(), false,
							simplexProperty.getmPropDistconnS1Analysis());

					if (edgeAdded) {
						actualEdgesInConnS1++;
						numOfIterationAddingEdgesToGraph = 0;
					} else {
						// try to add edge in different direction
						edgeAdded = addEdgeIsoS1WithTriCheck(mimicGraph, existingVertexColo2, existingVertexColo1,
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
		LOGGER.info("Case 6 completed!");
		LOGGER.info("Added Edges: " + actualEdgesInConnS1);
		LOGGER.info("Added Vertices: " + actualVerticesInConnS1);

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

		LOGGER.info("Case 7: Connect 2-simplexes to other vertices");
		LOGGER.info("Estimated Vertices: " + estimatedVerticesCommon);

		// get proposer of Vertex color (to create vertices connected to triangles)
		IOfferedItem<BitSet> potentialColoProposerForVertConnectedToTriangle = simplexClass
				.getColourProposerVertConnTriangle();

		// set to track class colors. New vertices need to be created for them later
		Set<BitSet> vertexClassColoSet = new HashSet<BitSet>();

		// Add vertices if not enough vertices
		while ((actualVerticesSimplexes < estimatedVerticesCommon)
				&& (potentialColoProposerForVertConnectedToTriangle != null)) {
			BitSet potentialvertexColo = potentialColoProposerForVertConnectedToTriangle.getPotentialItem();

			// check class node exists for the proposed color
			Set<BitSet> classColourSet = mimicGraph.getClassColour(potentialvertexColo);
			int numberOfClassVertices = 0;
			for (BitSet classColour : classColourSet) {
				Integer vertexIdClass = initializer.getmMapClassColourToVertexIDSimplexes().get(classColour);
				if ((vertexIdClass == null) && (!vertexClassColoSet.contains(classColour))) {
					numberOfClassVertices++;
					vertexClassColoSet.add(classColour);
				}
			}

			// create a new vertex only if we can add class nodes
			if ((actualVerticesSimplexes + numberOfClassVertices) < estimatedVerticesCommon) {
				addVertexToMimicGraph(mimicGraph, potentialvertexColo,
						initializer.getmMapColourToVertexIDsConnectedTo2Simplex());
				actualVerticesSimplexes++;
				// updating estimated vertices for this case. Note: class nodes are created in
				// next step
				estimatedVerticesCommon = estimatedVerticesCommon - numberOfClassVertices;
			}
		}

		LOGGER.info("Vertex addition completed");
		LOGGER.info("Added Vertices: " + actualVerticesSimplexes);

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
		LOGGER.info("Estimated Edges: " + estimatedEdgesCommon);

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

			if (!analysis.getS1ConnToTri().getmColourMapperSimplexes().canConnect(potentialheadColo, potentialTailColo,
					proposePropColor)) {
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
					vertsWithHeadColo = initializer.getmMapColourToVertexIDs1SimplexConnected().get(potentialheadColo);

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
					vertsWithTailColo = initializer.getmMapColourToVertexIDs1SimplexConnected().get(potentialTailColo);
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
			possibleLinkingEdgeColours = removeDuplicateEdgeColors(mimicGraph, vertexIDWithHeadColo,
					vertexIDWithTailColo, possibleLinkingEdgeColours);

			boolean havingVertices = commonVertices(mimicGraph, vertexIDWithHeadColo, vertexIDWithTailColo);

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
				updateMappingOfEdgeColoHeadTailColo(possEdgeColo, vertexIDWithHeadColo, vertexIDWithTailColo);

			} else {
				numOfIterationAddingEdgesToGraph++;
			}

		}
		LOGGER.info("Case 7 completed!");
		LOGGER.info("Added Edges: " + actualEdgesSimplexes);

		// ************************* Logic to add self loops for simplexes created for
		// different cases *********************************//
		LOGGER.info("Adding Self loops...........");

		// Isolated 2-simplexes
		LOGGER.info("Isolated 1-simplexes");
		addSelfLoops(mimicGraph, estimatedEdgesSelfLoopIn1Simplex, simplexClass.getIsoS1Proposer(),
				initializer.getmMapColourToVertexIDs1Simplex(),
				analysis.getSelfLoopsInIsoS1().getmColourMapperSimplexes(),
				initializer.getmMapColourToEdgeIDs1Simplex(), simplexProperty.getmPropDistselfLoopsInIsoS1());

		// Isolated 2-simplexes
		LOGGER.info("Isolated 2-simplexes");
		addSelfLoops(mimicGraph, estEdgesSelfLoopIsoTri, simplexClass.getIsoS2Proposer(),
				initializer.getmMapColourToVertexIDs2SimplexIsolated(),
				analysis.getSelfLoopIsoTri().getmColourMapperSimplexes(),
				initializer.getmMapColourToEdgeIDs2SimplexIsolated(), simplexProperty.getmPropDistselfLoopIsoTri());

		// Connected 2-simplexes
		LOGGER.info("Connected 2-simplexes");
		addSelfLoops(mimicGraph, estEdgesSelfLoopConnTri, simplexClass.getConnS2Proposer(),
				initializer.getmMapColourToVertexIDs2Simplex(),
				analysis.getSelfLoopConnTri().getmColourMapperSimplexes(), initializer.getmMapColourToEdgeIDs2Simplex(),
				simplexProperty.getmPropDistselfLoopConnTri());

		// 1-simplexes only connected to triangles
		LOGGER.info("1-simplexes connected only to triangles");
		addSelfLoops(mimicGraph, estEdgesSelfLoop1SimplexConnToTri, simplexClass.getConnS1TriProposer(),
				initializer.getmMapColourToVertexIDsConnectedTo2Simplex(),
				analysis.getSelfLoops1ConnToTri().getmColourMapperSimplexes(),
				initializer.getmMapColourToEdgeIDs1Simplex(), simplexProperty.getmPropDistselfLoops1ConnToTri());

		// Connected 1-simplexes
		LOGGER.info("Connected 1-simplexes");
		addSelfLoops(mimicGraph, estEdgesSelfLoopConn1Simplexes, simplexClass.getConnS1Proposer(),
				initializer.getmMapColourToVertexIDs1SimplexConnected(),
				analysis.getSelfLoopsInConnS1().getmColourMapperSimplexes(),
				initializer.getmMapColourToEdgeIDs1Simplex(), simplexProperty.getmPropDistSelfLoopConnS1());

		LOGGER.info("Number of edges in the mimic graph: " + mimicGraph.getEdges().size());
		LOGGER.info("Number of vertices in the mimic graph: " + mimicGraph.getVertices().size());

		// Update mMapColourToVertexIDs used for adding edges when improving the graph
		// in next phase
		updateVertexColoMap(initializer.getmMapColourToVertexIDs1Simplex()); // isolated 1-simplexes
		updateVertexColoMap(initializer.getmMapColourToVertexIDs2Simplex()); // connected 2-simplexes
		updateVertexColoMap(initializer.getmMapColourToVertexIDsIsoSelfLoop()); // isolated self loop
		updateVertexColoMap(initializer.getmMapColourToVertexIDs2SimplexIsolated()); // isolated 2-simplexes
		updateVertexColoMap(initializer.getmMapColourToVertexIDs1SimplexConnected()); // connected 1-simplexes
		updateVertexColoMap(initializer.getmMapColourToVertexIDsConnectedTo2Simplex()); // 1-simplexes connected to
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
			ObjectObjectOpenHashMap<TriColours, List<IntSet>> mTriangleColorsVertexIdsToUpdate) {
		// storing colors of vertices for initial triangle
		BitSet vertex1Color = inputTriangleColours.getA();
		BitSet vertex2Color = inputTriangleColours.getB();
		BitSet vertex3Color = inputTriangleColours.getC();

		// create vertex for the triangle colors in the mimic graph
		int vert1Id = addVertexToMimicGraph(mimicGraph, vertex1Color, mMapColourToVertexIDsToUpdate);
		int vert2Id = addVertexToMimicGraph(mimicGraph, vertex2Color, mMapColourToVertexIDsToUpdate);
		int vert3Id = addVertexToMimicGraph(mimicGraph, vertex3Color, mMapColourToVertexIDsToUpdate);

		// Add edges between found vertices
		addEdgeTriangle(mimicGraph, vertex1Color, vertex2Color, vert1Id, vert2Id, mMapColourToEdgeIDsToUpdate,
				mColourMapperToUse, inputTriangleColours);
		addEdgeTriangle(mimicGraph, vertex1Color, vertex3Color, vert1Id, vert3Id, mMapColourToEdgeIDsToUpdate,
				mColourMapperToUse, inputTriangleColours);
		addEdgeTriangle(mimicGraph, vertex2Color, vertex3Color, vert2Id, vert3Id, mMapColourToEdgeIDsToUpdate,
				mColourMapperToUse, inputTriangleColours);

		// update the map for trackign the colours of the triangle
		updateMapTriangleColorsVertices(vert1Id, vert2Id, vert3Id, inputTriangleColours,
				mTriangleColorsVertexIdsToUpdate);

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
	 * 
	 * This method add an edge for the input vertex 1 and vertex 2. It utilizes
	 * colormapper object to determine edge color and the head and tail vertex. For
	 * the edge color found, an edge is added between vertex 1 and vertex 2, and the
	 * edge id is stored with its color information in the map. In contrast with
	 * earlier method, this method specifically evaluates the existing edge colors
	 * between input vertices and does not add an edge with duplicate color
	 * 
	 * @param inputVertex1Colo - Color for input vertex 1.
	 * @param inputVertex2Colo - Color for input vertex 2.
	 * @param inputVertex1ID   - ID for input vertex 1.
	 * @param inputVertex2ID   - ID for input vertex 2.
	 */
	private boolean addEdgeTriangle(ColouredGraph mimicGraph, BitSet inputVertex1Colo, BitSet inputVertex2Colo,
			int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate,
			IColourMappingRules mColourMapperToUse, TriColours inputTriangleColours) {
		boolean isEdgeFromFirstToSecondVertex = true;
		// Get edge between vertex1 and vertex 2, assuming vertex 1 is tail and vertex 2
		// is head
		Set<BitSet> possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex1Colo,
				inputVertex2Colo);
		if (possEdgeColov1tailv2head.size() == 0) {
			// When vertex 1 is not tail and vertex 2 is not head
			// get edge assuming vertex 1 is head and vertex 2 is tail
			possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex2Colo,
					inputVertex1Colo);
			isEdgeFromFirstToSecondVertex = false;
		}

		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices

			// randomly select edge colo
			BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(
					new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];

			// Get the map storing edge colors and corresponding tail and head ids
			Map<Integer, IntSet> mTailHead = initializer.getmMapEdgeColoursToConnectedVertices()
					.get(randomEdgeColov1v2);
			if (mTailHead == null) {
				mTailHead = new HashMap<Integer, IntSet>();
			}

			// initialize head ids for the map
			IntSet headIds = null;

			// add the edge to mimic graph
			int edgeIdTemp;
			if (isEdgeFromFirstToSecondVertex) {
				edgeIdTemp = mimicGraph.addEdge(inputVertex1ID, inputVertex2ID, randomEdgeColov1v2);

				// get existing head ids if present
				headIds = mTailHead.get(inputVertex1ID);
				if (headIds == null) {
					headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				}
				headIds.add(inputVertex2ID);

				// update the map for tail id and head ids
				mTailHead.put(inputVertex1ID, headIds);

			} else {
				edgeIdTemp = mimicGraph.addEdge(inputVertex2ID, inputVertex1ID, randomEdgeColov1v2);

				headIds = mTailHead.get(inputVertex2ID);
				if (headIds == null) {
					headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				}
				headIds.add(inputVertex1ID);

				mTailHead.put(inputVertex2ID, headIds);

			}

			initializer.getmMapEdgeColoursToConnectedVertices().put(randomEdgeColov1v2, mTailHead);

			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared
			// to previously developed generators.
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);// mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); // mMapColourToEdgeIDs.put(randomEdgeColov1v2,
																					// setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);

			return true;

		} else {
			return false;
		}
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

	/**
	 * 
	 * This method add an edge for the input vertex 1 and vertex 2. It utilizes
	 * colormapper object to determine edge color and the head and tail vertex. For
	 * the edge color found, an edge is added between vertex 1 and vertex 2, and the
	 * edge id is stored with its color information in the map. In contrast with
	 * earlier method, this method specifically evaluates the existing edge colors
	 * between input vertices and does not add an edge with duplicate color
	 * 
	 * @param inputVertex1Colo - Color for input vertex 1.
	 * @param inputVertex2Colo - Color for input vertex 2.
	 * @param inputVertex1ID   - ID for input vertex 1.
	 * @param inputVertex2ID   - ID for input vertex 2.
	 */
	private boolean addEdgeInAnyDirectionWithDuplicateCheck(ColouredGraph mimicGraph, BitSet inputVertex1Colo,
			BitSet inputVertex2Colo, int inputVertex1ID, int inputVertex2ID,
			Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse) {
		boolean isEdgeFromSecondToFirstVertex = true;
		// Get edge between vertex1 and vertex 2, assuming vertex 1 is tail and vertex 2
		// is head
		Set<BitSet> possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex2Colo,
				inputVertex1Colo);
		if (possEdgeColov1tailv2head.size() == 0) {
			// When vertex 1 is not tail and vertex 2 is not head
			// get edge assuming vertex 1 is head and vertex 2 is tail
			possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex1Colo,
					inputVertex2Colo);
			isEdgeFromSecondToFirstVertex = false;
		}

		// Check for duplicate edge color if it is essential
		// Note: This check is not required when a triangle is created for the first
		// time or a edge is created between vertices for the first time
		possEdgeColov1tailv2head = removeDuplicateEdgeColors(mimicGraph, inputVertex2ID, inputVertex1ID,
				possEdgeColov1tailv2head);

		// when no edge can be added and second assumption is not evaluated (vertex 1 -
		// head and vertex 2 - tail) get edge colors for the second case
		if ((possEdgeColov1tailv2head.size() == 0) && isEdgeFromSecondToFirstVertex) {
			possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(inputVertex1Colo,
					inputVertex2Colo);
			isEdgeFromSecondToFirstVertex = false;

			possEdgeColov1tailv2head = removeDuplicateEdgeColors(mimicGraph, inputVertex2ID, inputVertex1ID,
					possEdgeColov1tailv2head);
		}

		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices

			// randomly select edge colo
			BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(
					new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];

			// Get the map storing edge colors and corresponding tail and head ids
			Map<Integer, IntSet> mTailHead = initializer.getmMapEdgeColoursToConnectedVertices()
					.get(randomEdgeColov1v2);
			if (mTailHead == null) {
				mTailHead = new HashMap<Integer, IntSet>();
			}

			// initialize head ids for the map
			IntSet headIds = null;

			// add the edge to mimic graph
			int edgeIdTemp;
			if (isEdgeFromSecondToFirstVertex) {
				edgeIdTemp = mimicGraph.addEdge(inputVertex2ID, inputVertex1ID, randomEdgeColov1v2);

				// get existing head ids if present
				headIds = mTailHead.get(inputVertex2ID);
				if (headIds == null) {
					headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				}
				headIds.add(inputVertex1ID);

				// update the map for tail id and head ids
				mTailHead.put(inputVertex2ID, headIds);
			} else {
				edgeIdTemp = mimicGraph.addEdge(inputVertex1ID, inputVertex2ID, randomEdgeColov1v2);

				headIds = mTailHead.get(inputVertex1ID);
				if (headIds == null) {
					headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				}
				headIds.add(inputVertex2ID);

				mTailHead.put(inputVertex1ID, headIds);
			}

			initializer.getmMapEdgeColoursToConnectedVertices().put(randomEdgeColov1v2, mTailHead);

			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared
			// to previously developed generators.
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);// mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); // mMapColourToEdgeIDs.put(randomEdgeColov1v2,
																					// setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);

			return true;

		} else {
			return false;
		}
	}

	/**
	 * This method adds a unique edge between given head and tail ids. Additionally,
	 * it will check that edge should be added only such that no triangles are
	 * formed between input head and tail ids if the last parameter of the method is
	 * true.
	 * 
	 * @param headColo                    - input color for the head vertex.
	 * @param tailColo                    - input color for the tail vertex.
	 * @param headID                      - id for the head vertex.
	 * @param tailID                      - id for the tail vertex.
	 * @param mMapColourToEdgeIDsToUpdate - map to update the edge color and ids if
	 *                                    edge is added.
	 * @param mColourMapperToUse          - Color mapper to use for edge colors.
	 * @param triangleCheck               - boolean variable to indicate if it
	 *                                    should check that triangle is not formed.
	 *                                    When set to true the edge is added between
	 *                                    input vertices only if it does not form a
	 *                                    triangle.
	 * @return - will return true if edge is added to the mimic graph else false.
	 */
	private boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID,
			int tailID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			boolean triangleCheck) {
		// Get edge between head and tail, assuming vertex 1 is tail and vertex 2 is
		// head
		Set<BitSet> possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(tailColo, headColo);

		// Check for duplicate edge color if it is essential
		// Note: This check is not required when a triangle is created for the first
		// time or a edge is created between vertices for the first time
		possEdgeColov1tailv2head = removeDuplicateEdgeColors(mimicGraph, tailID, headID, possEdgeColov1tailv2head);

		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices

			// check the head id and tail id does not have a vertex in common. Adding an
			// edge could form a triangle
			if (triangleCheck) {
				if (commonVertices(mimicGraph, headID, tailID))
					return false;// do not add an edge and return false. if input vertices have a vertex in
									// common.
			}

			// randomly select edge colo
			BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(
					new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];

			// add the edge to mimic graph
			int edgeIdTemp;
			edgeIdTemp = mimicGraph.addEdge(tailID, headID, randomEdgeColov1v2);

			// update the map of edge colors and tail, head IDs
			updateMappingOfEdgeColoHeadTailColo(randomEdgeColov1v2, headID, tailID);

			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared
			// to previously developed generators.
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);// mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); // mMapColourToEdgeIDs.put(randomEdgeColov1v2,
																					// setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);

			return true;

		} else {
			return false;
		}
	}

	/**
	 * For the input set of edge colors between the given vertices, the function
	 * removes edge colors that already exist among input vertices. It returns the
	 * updated set.
	 * 
	 * @param inputVertex1ID           - input vertex id 1
	 * @param inputVertex2ID           - input vertex id 2
	 * @param possEdgeColov1tailv2head - possible set of edge colors between input
	 *                                 vertices
	 * @return - updated input set with not already existing edge colors
	 */
	private Set<BitSet> removeDuplicateEdgeColors(ColouredGraph mimicGraph, int inputVertex1ID, int inputVertex2ID,
			Set<BitSet> possEdgeColov1tailv2head) {
		// check existing edge colors between vertices and remove them from the possible
		// edge colors set
		IntSet edgesIncidentVert1 = mimicGraph.getEdgesIncidentTo(inputVertex1ID);
		IntSet edgesIncidentVert2 = mimicGraph.getEdgesIncidentTo(inputVertex2ID);
		IntSet edgesIncidentExistingVertices = IntSetUtil.intersection(edgesIncidentVert1, edgesIncidentVert2);

		Set<BitSet> existingEdgeColours = new HashSet<BitSet>();
		for (int existingEdgeId : edgesIncidentExistingVertices) {
			existingEdgeColours.add(mimicGraph.getEdgeColour(existingEdgeId));
		}

		// Difference between possible colors and existing colors
		SetView<BitSet> differenceSet = Sets.difference(possEdgeColov1tailv2head, existingEdgeColours);
		possEdgeColov1tailv2head = new HashSet<BitSet>();
		possEdgeColov1tailv2head.addAll(differenceSet);

		return possEdgeColov1tailv2head;
	}

	/**
	 * The method returns true if the input vertices have one or more vertices in
	 * common.
	 * 
	 * @param headID
	 * @param tailID
	 * @return
	 */
	public boolean commonVertices(ColouredGraph mimicGraph, int headID, int tailID) {
		// get vertices incident on input ids
		IntSet verticesIncidentHead = IntSetUtil.union(mimicGraph.getInNeighbors(headID),
				mimicGraph.getOutNeighbors(headID));
		IntSet verticesIncidentTail = IntSetUtil.union(mimicGraph.getInNeighbors(tailID),
				mimicGraph.getOutNeighbors(tailID));

		// find vertices incident to both
		IntSet commonVertices = IntSetUtil.intersection(verticesIncidentHead, verticesIncidentTail);
		// do not consider class vertices
		Set<Integer> classVertices = initializer.getmReversedMapClassVertices().keySet();
		Set<Integer> commonVerticesSet = new HashSet<Integer>();
		for (int vertexId : commonVertices)
			commonVerticesSet.add(vertexId);
		commonVerticesSet = Sets.difference(commonVerticesSet, classVertices);
		if (commonVerticesSet.size() > 0)
			return true;
		return false;
	}

	/**
	 * This method updates the global map for Edge color => tail ID => head IDs
	 * 
	 * @param possEdgeColo
	 * @param headID
	 * @param tailID
	 */
	public void updateMappingOfEdgeColoHeadTailColo(BitSet possEdgeColo, int headID, int tailID) {
		// Get the map storing edge colors and corresponding tail and head ids
		Map<Integer, IntSet> mTailHead = initializer.getmMapEdgeColoursToConnectedVertices().get(possEdgeColo);
		if (mTailHead == null) {
			mTailHead = new HashMap<Integer, IntSet>();
		}

		// initialize head ids for the map
		IntSet headIds = mTailHead.get(tailID);
		if (headIds == null) {
			headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
		}
		headIds.add(headID);

		// update the map for tail id and head ids
		mTailHead.put(tailID, headIds);

		initializer.getmMapEdgeColoursToConnectedVertices().put(possEdgeColo, mTailHead);
	}

	private void addSelfLoops(ColouredGraph mimicGraph, int estEdgesInput,
			IOfferedItem<BitSet> distColoProposerSelfLoopInput,
			Map<BitSet, IntSet> mMapColourToVertexIDsInput, IColourMappingRules mColourMapperSelfLoopInput,
			Map<BitSet, IntSet> mMapColourToEdgeIDsInput, IPropertyDist mPropDistInput) {
		LOGGER.info("Estimated edges: " + estEdgesInput);
		int actualEdgesSimplexes = 0;
		int iterationCountSelf = 0;
		while ((estEdgesInput > actualEdgesSimplexes) && (iterationCountSelf < initializer.getMaximumNoIterations())
				&& (distColoProposerSelfLoopInput != null)) {
			BitSet proposedVertexColor = distColoProposerSelfLoopInput.getPotentialItem();
			IntSet possVertices = mMapColourToVertexIDsInput.get(proposedVertexColor);
			if (possVertices != null) {
				Integer vertexID = possVertices.toArray(new Integer[possVertices.size()])[mRandom
						.nextInt(possVertices.size())];
				boolean edgeAdded = addEdgeInAnyDirectionDuplCheckWithPropProb(mimicGraph, proposedVertexColor,
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
		LOGGER.info("Added edges: " + actualEdgesSimplexes);
	}

	/**
	 * 
	 * This method add an edge for the input vertex 1 and vertex 2. It utilizes
	 * colormapper object to determine edge color and the head and tail vertex. For
	 * the edge color found, an edge is added between vertex 1 and vertex 2, and the
	 * edge id is stored with its color information in the map. In contrast with
	 * earlier method, this method specifically evaluates the existing edge colors
	 * between input vertices and does not add an edge with duplicate color
	 * 
	 * @param inputVertex1Colo - Color for input vertex 1.
	 * @param inputVertex2Colo - Color for input vertex 2.
	 * @param inputVertex1ID   - ID for input vertex 1.
	 * @param inputVertex2ID   - ID for input vertex 2.
	 */
	private boolean addEdgeInAnyDirectionDuplCheckWithPropProb(ColouredGraph mimicGraph, BitSet inputVertex1Colo,
			BitSet inputVertex2Colo, int inputVertex1ID, int inputVertex2ID,
			Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			IPropertyDist mPropDistInput) {
		boolean isEdgeFromSecondToFirstVertex = true;

		EdgeColorsSorted edgeColors = new EdgeColorsSorted(inputVertex1Colo, inputVertex2Colo);
		BitSet propColor = mPropDistInput.proposePropColor(edgeColors);
		if (mColourMapperToUse.isTailColourOf(inputVertex1Colo, inputVertex2Colo)) {
			isEdgeFromSecondToFirstVertex = false;
		}

		Set<BitSet> possEdgeColov1tailv2head = new HashSet<BitSet>();
		possEdgeColov1tailv2head.add(propColor);

		// Check for duplicate edge color if it is essential
		// Note: This check is not required when a triangle is created for the first
		// time or a edge is created between vertices for the first time
		possEdgeColov1tailv2head = removeDuplicateEdgeColors(mimicGraph, inputVertex2ID, inputVertex1ID,
				possEdgeColov1tailv2head);

		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices

			// randomly select edge colo
			BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(
					new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];

			// Get the map storing edge colors and corresponding tail and head ids
			Map<Integer, IntSet> mTailHead = initializer.getmMapEdgeColoursToConnectedVertices()
					.get(randomEdgeColov1v2);
			if (mTailHead == null) {
				mTailHead = new HashMap<Integer, IntSet>();
			}

			// initialize head ids for the map
			IntSet headIds = null;

			// add the edge to mimic graph
			int edgeIdTemp;
			if (isEdgeFromSecondToFirstVertex) {
				edgeIdTemp = mimicGraph.addEdge(inputVertex2ID, inputVertex1ID, randomEdgeColov1v2);

				// get existing head ids if present
				headIds = mTailHead.get(inputVertex2ID);
				if (headIds == null) {
					headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				}
				headIds.add(inputVertex1ID);

				// update the map for tail id and head ids
				mTailHead.put(inputVertex2ID, headIds);
			} else {
				edgeIdTemp = mimicGraph.addEdge(inputVertex1ID, inputVertex2ID, randomEdgeColov1v2);

				headIds = mTailHead.get(inputVertex1ID);
				if (headIds == null) {
					headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				}
				headIds.add(inputVertex2ID);

				mTailHead.put(inputVertex1ID, headIds);
			}

			initializer.getmMapEdgeColoursToConnectedVertices().put(randomEdgeColov1v2, mTailHead);

			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared
			// to previously developed generators.
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);// mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); // mMapColourToEdgeIDs.put(randomEdgeColov1v2,
																					// setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);

			return true;

		} else {
			return false;
		}
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
	private void addTriangleToMimicGraphWithPropProb(ColouredGraph mimicGraph, TriColours inputTriangleColours,
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
		addEdgeTriangleWithPropProb(mimicGraph, vertex1Color, vertex2Color, vert1Id, vert2Id,
				mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours, mPropDistInput);
		addEdgeTriangleWithPropProb(mimicGraph, vertex1Color, vertex3Color, vert1Id, vert3Id,
				mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours, mPropDistInput);
		addEdgeTriangleWithPropProb(mimicGraph, vertex2Color, vertex3Color, vert2Id, vert3Id,
				mMapColourToEdgeIDsToUpdate, mColourMapperToUse, inputTriangleColours, mPropDistInput);

		// update the map for trackign the colours of the triangle
		updateMapTriangleColorsVertices(vert1Id, vert2Id, vert3Id, inputTriangleColours,
				mTriangleColorsVertexIdsToUpdate);

	}

	/**
	 * 
	 * This method add an edge for the input vertex 1 and vertex 2. It utilizes
	 * colormapper object to determine edge color and the head and tail vertex. For
	 * the edge color found, an edge is added between vertex 1 and vertex 2, and the
	 * edge id is stored with its color information in the map. In contrast with
	 * earlier method, this method specifically evaluates the existing edge colors
	 * between input vertices and does not add an edge with duplicate color
	 * 
	 * @param inputVertex1Colo - Color for input vertex 1.
	 * @param inputVertex2Colo - Color for input vertex 2.
	 * @param inputVertex1ID   - ID for input vertex 1.
	 * @param inputVertex2ID   - ID for input vertex 2.
	 */
	private boolean addEdgeTriangleWithPropProb(ColouredGraph mimicGraph, BitSet inputVertex1Colo,
			BitSet inputVertex2Colo, int inputVertex1ID, int inputVertex2ID,
			Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			TriColours inputTriangleColours, IPropertyDist mPropDistInput) {
		boolean isEdgeFromFirstToSecondVertex = true;
		// Get edge between vertex1 and vertex 2, assuming vertex 1 is tail and vertex 2
		// is head

		EdgeColorsSorted edgeColors = new EdgeColorsSorted(inputVertex1Colo, inputVertex2Colo);
		BitSet propColor = simplexProperty.proposeColour(edgeColors);
		if (mColourMapperToUse.isHeadColourOf(inputVertex2Colo, inputVertex1Colo)) { // is v1 head colo?
			isEdgeFromFirstToSecondVertex = false;
		}

		if (!propColor.isEmpty()) { // Add edge if edge color is found for the vertices

			// randomly select edge colo
			BitSet randomEdgeColov1v2 = propColor;

			// Get the map storing edge colors and corresponding tail and head ids
			Map<Integer, IntSet> mTailHead = initializer.getmMapEdgeColoursToConnectedVertices()
					.get(randomEdgeColov1v2);
			if (mTailHead == null) {
				mTailHead = new HashMap<Integer, IntSet>();
			}

			// initialize head ids for the map
			IntSet headIds = null;

			// add the edge to mimic graph
			int edgeIdTemp;
			if (isEdgeFromFirstToSecondVertex) {
				edgeIdTemp = mimicGraph.addEdge(inputVertex1ID, inputVertex2ID, randomEdgeColov1v2);

				// get existing head ids if present
				headIds = mTailHead.get(inputVertex1ID);
				if (headIds == null) {
					headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				}
				headIds.add(inputVertex2ID);

				// update the map for tail id and head ids
				mTailHead.put(inputVertex1ID, headIds);

			} else {
				edgeIdTemp = mimicGraph.addEdge(inputVertex2ID, inputVertex1ID, randomEdgeColov1v2);

				headIds = mTailHead.get(inputVertex2ID);
				if (headIds == null) {
					headIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				}
				headIds.add(inputVertex1ID);

				mTailHead.put(inputVertex2ID, headIds);

			}

			initializer.getmMapEdgeColoursToConnectedVertices().put(randomEdgeColov1v2, mTailHead);

			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared
			// to previously developed generators.
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);// mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); // mMapColourToEdgeIDs.put(randomEdgeColov1v2,
																					// setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);

			return true;

		} else {
			return false;
		}
	}

	/**
	 * This method adds a unique edge between given head and tail ids. Additionally,
	 * it will check that edge should be added only such that no triangles are
	 * formed between input head and tail ids if the last parameter of the method is
	 * true.
	 * 
	 * @param headColo                    - input color for the head vertex.
	 * @param tailColo                    - input color for the tail vertex.
	 * @param headID                      - id for the head vertex.
	 * @param tailID                      - id for the tail vertex.
	 * @param mMapColourToEdgeIDsToUpdate - map to update the edge color and ids if
	 *                                    edge is added.
	 * @param mColourMapperToUse          - Color mapper to use for edge colors.
	 * @param triangleCheck               - boolean variable to indicate if it
	 *                                    should check that triangle is not formed.
	 *                                    When set to true the edge is added between
	 *                                    input vertices only if it does not form a
	 *                                    triangle.
	 * @return - will return true if edge is added to the mimic graph else false.
	 */
	private boolean addEdgeIsoS1WithTriCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID,
			int tailID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			boolean triangleCheck, IPropertyDist mPropDistInput) {

		EdgeColorsSorted edgesColors = new EdgeColorsSorted(headColo, tailColo);

		BitSet proposePropColor = mPropDistInput.proposePropColor(edgesColors);

		// Get edge between head and tail, assuming vertex 1 is tail and vertex 2 is
		// head
		Set<BitSet> possEdgeColov1tailv2head = new HashSet<BitSet>();
		possEdgeColov1tailv2head.add(proposePropColor);

		// Check for duplicate edge color if it is essential
		// Note: This check is not required when a triangle is created for the first
		// time or a edge is created between vertices for the first time
		possEdgeColov1tailv2head = removeDuplicateEdgeColors(mimicGraph, tailID, headID, possEdgeColov1tailv2head);

		if ((possEdgeColov1tailv2head.size() != 0)
				&& (mColourMapperToUse.canConnect(headColo, tailColo, proposePropColor))) { // Add edge if edge color is
																							// found for the vertices

			// check the head id and tail id does not have a vertex in common. Adding an
			// edge could form a triangle
			if (triangleCheck) {
				if (commonVertices(mimicGraph, headID, tailID))
					return false;// do not add an edge and return false. if input vertices have a vertex in
									// common.
			}

			// randomly select edge colo
			BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(
					new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];

			// add the edge to mimic graph
			int edgeIdTemp;
			edgeIdTemp = mimicGraph.addEdge(tailID, headID, randomEdgeColov1v2);

			// update the map of edge colors and tail, head IDs
			updateMappingOfEdgeColoHeadTailColo(randomEdgeColov1v2, headID, tailID);

			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared
			// to previously developed generators.
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);// mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); // mMapColourToEdgeIDs.put(randomEdgeColov1v2,
																					// setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);

			return true;

		} else {
			return false;
		}
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

	/**
	 * This method adds a unique edge between given head and tail ids. Additionally,
	 * it will check that edge should be added only such that no triangles are
	 * formed between input head and tail ids if the last parameter of the method is
	 * true.
	 * 
	 * @param headColo                    - input color for the head vertex.
	 * @param tailColo                    - input color for the tail vertex.
	 * @param headID                      - id for the head vertex.
	 * @param tailID                      - id for the tail vertex.
	 * @param mMapColourToEdgeIDsToUpdate - map to update the edge color and ids if
	 *                                    edge is added.
	 * @param mColourMapperToUse          - Color mapper to use for edge colors.
	 * @param triangleCheck               - boolean variable to indicate if it
	 *                                    should check that triangle is not formed.
	 *                                    When set to true the edge is added between
	 *                                    input vertices only if it does not form a
	 *                                    triangle.
	 * @return - will return true if edge is added to the mimic graph else false.
	 */
	private boolean addEdgeConnS1WithTriCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID,
			int tailID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			boolean triangleCheck, BitSet propertyColo) {
		// Get edge between head and tail, assuming vertex 1 is tail and vertex 2 is
		// head
		Set<BitSet> possEdgeColov1tailv2head = new HashSet<BitSet>();
		possEdgeColov1tailv2head.add(propertyColo);

		// Check for duplicate edge color if it is essential
		// Note: This check is not required when a triangle is created for the first
		// time or a edge is created between vertices for the first time
		possEdgeColov1tailv2head = removeDuplicateEdgeColors(mimicGraph, tailID, headID, possEdgeColov1tailv2head);

		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices

			// check the head id and tail id does not have a vertex in common. Adding an
			// edge could form a triangle
			if (triangleCheck) {
				if (commonVertices(mimicGraph, headID, tailID))
					return false;// do not add an edge and return false. if input vertices have a vertex in
									// common.
			}

			// randomly select edge colo
			BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(
					new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];

			// add the edge to mimic graph
			int edgeIdTemp;
			edgeIdTemp = mimicGraph.addEdge(tailID, headID, randomEdgeColov1v2);

			// update the map of edge colors and tail, head IDs
			updateMappingOfEdgeColoHeadTailColo(randomEdgeColov1v2, headID, tailID);

			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared
			// to previously developed generators.
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);// mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); // mMapColourToEdgeIDs.put(randomEdgeColov1v2,
																					// setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);

			return true;

		} else {
			return false;
		}
	}

	/**
	 * The method selects a random triangle by using Random object. It will return
	 * null, if no triangles are found in the input graphs.
	 * 
	 * @return - Triangle Colours Set of colors for each vertex of a randomly
	 *         selected triangle along with their occurrence count in input graphs.
	 */
	private TriColours getRandomTriangle(Set<TriColours> setAllTriangleColours) {
		TriColours randomTriangleColor = null;
		if (setAllTriangleColours.size() != 0) {
			randomTriangleColor = setAllTriangleColours.toArray(new TriColours[setAllTriangleColours.size()])[mRandom
					.nextInt(setAllTriangleColours.size())];
		}

		return randomTriangleColor;
	}

	@Override
	public String finishSaveMimicGraph(ColouredGraph mimicGraph, ConstantValueStorage valuesCarrier,
			GraphLexicalization lexicalizer, GraphInitializer initializer, IDatasetManager mDatasetManager) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TripleBaseSingleID getProposedTriple() {
		// TODO Auto-generated method stub
		return null;
	}

}
