package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Sets.SetView;

import com.carrotsearch.hppc.BitSet;

import it.unimi.dsi.fastutil.ints.IntSet;

public interface ISimplexProperty {

	BitSet proposeColour(EdgeColorsSorted edgeColors);

	BitSet proposeColourForTri(EdgeColorsSorted edgeColors);

	BitSet proposeConnPropColour(EdgeColorsSorted edgeColorsSorted);

	BitSet proposeS1ConnProp(EdgeColorsSorted edgeColorsSorted);

	IPropertyDist getmPropDistConnTri();

	IPropertyDist getmPropDistisoS1SelfLoop();

	IPropertyDist getmPropDistisoS1();

	IPropertyDist getmPropDistS1ConnectingTri();

	IPropertyDist getmPropDistIsoTri();

	IPropertyDist getmPropDistconnS1Analysis();

	IPropertyDist getmPropDistselfLoopsInIsoS1();

	IPropertyDist getmPropDistselfLoopIsoTri();

	IPropertyDist getmPropDistselfLoopConnTri();

	IPropertyDist getmPropDistselfLoops1ConnToTri();

	IPropertyDist getmPropDistSelfLoopConnS1();

	boolean addEdgeToMimicGraph(ColouredGraph mimicGraph, BitSet inputVertex1Colo, BitSet inputVertex2Colo,
			int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate,
			IColourMappingRules mColourMapperToUse, TriColours inputTriangleColours, IPropertyDist mPropDistInput);

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
	boolean addEdgeInAnyDirection(ColouredGraph mimicGraph, BitSet inputVertex1Colo, BitSet inputVertex2Colo,
			int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate,
			IColourMappingRules mColourMapperToUse, IPropertyDist mPropDistInput);

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
	boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID, int tailID,
			Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			boolean triangleCheck, IPropertyDist mPropDistInput);

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
	boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID, int tailID,
			Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			boolean triangleCheck, BitSet propertyColo);

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
	default Set<BitSet> removeDuplicateEdgeColors(ColouredGraph mimicGraph, int inputVertex1ID, int inputVertex2ID,
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

}
