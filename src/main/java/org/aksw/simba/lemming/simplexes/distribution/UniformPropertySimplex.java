package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.generator.SimplexGraphInitializer;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.util.Constants;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Sample all colours randomly
 */
@Component("UPSI")
@Scope(value = "prototype")
public class UniformPropertySimplex implements ISimplexProperty {

	private SimplexGraphInitializer initializer;

	private Random mRandom;

	public UniformPropertySimplex(SimplexGraphInitializer initializer) {
		this.initializer = initializer;
		this.mRandom = new Random(initializer.getSeedGenerator().getNextSeed());
	}

	@Override
	public BitSet proposeColour(EdgeColorsSorted edgeColours) {
		return new OfferedItemWrapper<BitSet>(initializer.getAvailableEdgeColours().toArray(BitSet[]::new), mRandom)
				.getPotentialItem();
	}

	@Override
	public BitSet proposeColourForTri(EdgeColorsSorted edgeColours) {
		return proposeColour(edgeColours);
	}

	@Override
	public BitSet proposeConnPropColour(EdgeColorsSorted edgeColours) {
		return proposeColour(edgeColours);
	}

	@Override
	public BitSet proposeS1ConnProp(EdgeColorsSorted edgeColours) {
		return proposeColour(edgeColours);
	}

	@Override
	public boolean addEdgeToMimicGraph(ColouredGraph mimicGraph, BitSet inputVertex1Colo, BitSet inputVertex2Colo,
			int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate,
			IColourMappingRules mColourMapperToUse, TriColours inputTriangleColours, IPropertyDist mPropDistInput) {
		boolean isEdgeFromFirstToSecondVertex = true;
		// Get edge between vertex1 and vertex 2, assuming vertex 1 is tail and vertex 2
		// is head
		Map<BitSet, Map<Integer, IntSet>> mMapEdgeColoursToConnectedVertices = initializer
				.getmMapEdgeColoursToConnectedVertices();
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
			Map<Integer, IntSet> mTailHead = mMapEdgeColoursToConnectedVertices.get(randomEdgeColov1v2);
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

			mMapEdgeColoursToConnectedVertices.put(randomEdgeColov1v2, mTailHead);

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

	@Override
	public IPropertyDist getmPropDistConnTri() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistisoS1SelfLoop() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistisoS1() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistS1ConnectingTri() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistIsoTri() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistconnS1Analysis() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistselfLoopsInIsoS1() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistselfLoopIsoTri() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistselfLoopConnTri() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistselfLoops1ConnToTri() {
		return null;
	}

	@Override
	public IPropertyDist getmPropDistSelfLoopConnS1() {
		return null;
	}

	@Override
	public boolean addEdgeInAnyDirection(ColouredGraph mimicGraph, BitSet inputVertex1Colo, BitSet inputVertex2Colo,
			int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate,
			IColourMappingRules mColourMapperToUse, IPropertyDist mPropDistInput) {
		boolean isEdgeFromSecondToFirstVertex = true;
		Map<BitSet, Map<Integer, IntSet>> mMapEdgeColoursToConnectedVertices = initializer
				.getmMapEdgeColoursToConnectedVertices();
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
			Map<Integer, IntSet> mTailHead = mMapEdgeColoursToConnectedVertices.get(randomEdgeColov1v2);
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

			mMapEdgeColoursToConnectedVertices.put(randomEdgeColov1v2, mTailHead);

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

	@Override
	public boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID,
			int tailID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			boolean triangleCheck, IPropertyDist mPropDistInput) {
		return addEdgeWithTriangleCheck(mimicGraph, headColo, tailColo, headID, tailID, mMapColourToEdgeIDsToUpdate,
				mColourMapperToUse, triangleCheck);

	}

	@Override
	public boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID,
			int tailID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			boolean triangleCheck, BitSet mPropDistInput) {
		return addEdgeWithTriangleCheck(mimicGraph, headColo, tailColo, headID, tailID, mMapColourToEdgeIDsToUpdate,
				mColourMapperToUse, triangleCheck);

	}

	public boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID,
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
				if (initializer.commonVertices(mimicGraph, headID, tailID))
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
			initializer.updateMappingOfEdgeColoHeadTailColo(randomEdgeColov1v2, headID, tailID);

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
}