package org.aksw.simba.lemming.simplexes.distribution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.generator.simplex.SimplexGraphInitializer;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.simplexes.analysis.SimplexAnalysis;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * 
 */
@Component("BP")
@Scope(value = "prototype")
public class BiasedPropertySimplex implements ISimplexProperty {
	private static final Logger LOGGER = LoggerFactory.getLogger(BiasedPropertySimplex.class);
	private SimplexGraphInitializer initializer;

	private IPropertyDist mPropDistConnTri;
	private IPropertyDist mPropDistIsoTri;
	private IPropertyDist mPropDistS1ConnToTri;
	private IPropertyDist mPropDistS1ConnectingTri;
	private IPropertyDist mPropDistselfLoops1ConnToTri;
	private IPropertyDist mPropDistselfLoopConnTri;
	private IPropertyDist mPropDistselfLoopIsoTri;
	private IPropertyDist mPropDistconnS1Analysis;
	private IPropertyDist mPropDistselfLoopsInIsoS1;
	private IPropertyDist mPropDistisoS1SelfLoop;
	private IPropertyDist mPropDistisoS1;
	private IPropertyDist mPropDistSelfLoopConnS1;

	private Random mRandom;

	public BiasedPropertySimplex(SimplexGraphInitializer initializer) {
		this.initializer = initializer;
		SimplexAnalysis simplexAnalysis = initializer.getSimplexAnalysis();
		int iNoOfVersions = initializer.getiNoOfVersions();
		mRandom = new Random(initializer.getSeedGenerator().getNextSeed());
		mPropDistConnTri = new PropertyDistI(simplexAnalysis.getConnTriAnalysis().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistIsoTri = new PropertyDistI(simplexAnalysis.getIsoTriAnalysis().getmVertColosPropDist(), iNoOfVersions,
				mRandom);
		mPropDistS1ConnToTri = new PropertyDistI(simplexAnalysis.getS1ConnToTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistS1ConnectingTri = new PropertyDistI(simplexAnalysis.getS1ConnectingTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistS1ConnToTri = new PropertyDistI(simplexAnalysis.getS1ConnToTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistselfLoopIsoTri = new PropertyDistI(simplexAnalysis.getSelfLoopIsoTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistselfLoopConnTri = new PropertyDistI(simplexAnalysis.getSelfLoopConnTri().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistselfLoops1ConnToTri = new PropertyDistI(
				simplexAnalysis.getSelfLoops1ConnToTri().getmVertColosPropDist(), iNoOfVersions, mRandom);
		mPropDistisoS1 = new PropertyDistI(simplexAnalysis.getIsoS1Analysis().getmVertColosPropDist(), iNoOfVersions,
				mRandom);
		mPropDistisoS1SelfLoop = new PropertyDistI(simplexAnalysis.getIsoS1SelfLoopAnalysis().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistselfLoopsInIsoS1 = new PropertyDistI(simplexAnalysis.getSelfLoopsInIsoS1().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistconnS1Analysis = new PropertyDistI(simplexAnalysis.getConnS1Analysis().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
		mPropDistSelfLoopConnS1 = new PropertyDistI(simplexAnalysis.getSelfLoopsInConnS1().getmVertColosPropDist(),
				iNoOfVersions, mRandom);
	}

	@Override
	public BitSet proposeColour(EdgeColorsSorted edgeColors) {
		return mPropDistConnTri.proposePropColor(edgeColors);
	}

	@Override
	public BitSet proposeColourForTri(EdgeColorsSorted edgeColors) {
		return mPropDistS1ConnectingTri.proposePropColor(edgeColors);
	}

	@Override
	public BitSet proposeConnPropColour(EdgeColorsSorted edgeColorsSorted) {
		return mPropDistconnS1Analysis.proposePropColor(edgeColorsSorted);
	}

	@Override
	public BitSet proposeS1ConnProp(EdgeColorsSorted edgeColorsSorted) {
		return mPropDistS1ConnToTri.proposePropColor(edgeColorsSorted);
	}

	public IPropertyDist getmPropDistConnTri() {
		return mPropDistConnTri;
	}

	public IPropertyDist getmPropDistIsoTri() {
		return mPropDistIsoTri;
	}

	public IPropertyDist getmPropDistS1ConnToTri() {
		return mPropDistS1ConnToTri;
	}

	public IPropertyDist getmPropDistS1ConnectingTri() {
		return mPropDistS1ConnectingTri;
	}

	public IPropertyDist getmPropDistselfLoops1ConnToTri() {
		return mPropDistselfLoops1ConnToTri;
	}

	public IPropertyDist getmPropDistselfLoopConnTri() {
		return mPropDistselfLoopConnTri;
	}

	public IPropertyDist getmPropDistselfLoopIsoTri() {
		return mPropDistselfLoopIsoTri;
	}

	public IPropertyDist getmPropDistconnS1Analysis() {
		return mPropDistconnS1Analysis;
	}

	public IPropertyDist getmPropDistselfLoopsInIsoS1() {
		return mPropDistselfLoopsInIsoS1;
	}

	public IPropertyDist getmPropDistisoS1SelfLoop() {
		return mPropDistisoS1SelfLoop;
	}

	public IPropertyDist getmPropDistisoS1() {
		return mPropDistisoS1;
	}

	public IPropertyDist getmPropDistSelfLoopConnS1() {
		return mPropDistSelfLoopConnS1;
	}

	@Override
	public boolean addEdgeToMimicGraph(ColouredGraph mimicGraph, BitSet inputVertex1Colo, BitSet inputVertex2Colo,
			int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate,
			IColourMappingRules mColourMapperToUse, TriColours inputTriangleColours, IPropertyDist mPropDistInput) {

		boolean isEdgeFromFirstToSecondVertex = true;
		// Get edge between vertex1 and vertex 2, assuming vertex 1 is tail and vertex 2
		// is head

		EdgeColorsSorted edgeColors = new EdgeColorsSorted(inputVertex1Colo, inputVertex2Colo);
		BitSet propColor = proposeColour(edgeColors);
		if (mColourMapperToUse.isHeadColourOf(inputVertex2Colo, inputVertex1Colo)) { // is v1 head colo?
			isEdgeFromFirstToSecondVertex = false;
		}

		if (propColor != null && !propColor.isEmpty()) { // Add edge if edge color is found for the vertices

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

	@Override
	public boolean addEdgeInAnyDirection(ColouredGraph mimicGraph, BitSet inputVertex1Colo, BitSet inputVertex2Colo,
			int inputVertex1ID, int inputVertex2ID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate,
			IColourMappingRules mColourMapperToUse, IPropertyDist mPropDistInput) {
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

	@Override
	public boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID,
			int tailID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse,
			boolean triangleCheck, IPropertyDist mPropDistInput) {

		EdgeColorsSorted edgesColors = new EdgeColorsSorted(headColo, tailColo);

		BitSet proposePropColor = mPropDistInput.proposePropColor(edgesColors);

		return addEdgeWithTriangleCheck(mimicGraph, headColo, tailColo, headID, tailID, mMapColourToEdgeIDsToUpdate,
				mColourMapperToUse, triangleCheck, proposePropColor);
	}

	@Override
	public boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph, BitSet headColo, BitSet tailColo, int headID,
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

		if (possEdgeColov1tailv2head.size() != 0 && (mColourMapperToUse.canConnect(headColo, tailColo, propertyColo))) { // Add edge if edge color is found for the vertices

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
	
	@Override
	public boolean addEdgeWithTriangleCheck(ColouredGraph mimicGraph,BitSet headColo, BitSet tailColo, int headID, int tailID, Map<BitSet, IntSet> mMapColourToEdgeIDsToUpdate, IColourMappingRules mColourMapperToUse, boolean triangleCheck) {
		// Get edge between head and tail, assuming vertex 1 is tail and vertex 2 is head
		Set<BitSet> possEdgeColov1tailv2head = mColourMapperToUse.getPossibleLinkingEdgeColours(tailColo, headColo);
		
		// Check for duplicate edge color if it is essential
		// Note: This check is not required when a triangle is created for the first time or a edge is created between vertices for the first time
		possEdgeColov1tailv2head = removeDuplicateEdgeColors(mimicGraph, tailID, headID, possEdgeColov1tailv2head);	
		
		if (possEdgeColov1tailv2head.size() != 0) { // Add edge if edge color is found for the vertices
			
			// check the head id and tail id does not have a vertex in common. Adding an edge could form a triangle
			if (triangleCheck) {
				if (initializer.commonVertices(mimicGraph, headID, tailID))
					return false;//do not add an edge and return false. if input vertices have a vertex in common.
			}
			
			// randomly select edge colo
			BitSet randomEdgeColov1v2 = possEdgeColov1tailv2head.toArray(new BitSet[possEdgeColov1tailv2head.size()])[mRandom.nextInt(possEdgeColov1tailv2head.size())];
			
			// add the edge to mimic graph
			int edgeIdTemp;
			edgeIdTemp = mimicGraph.addEdge(tailID, headID, randomEdgeColov1v2);
			
			// update the map of edge colors and tail, head IDs
			initializer.updateMappingOfEdgeColoHeadTailColo(randomEdgeColov1v2, headID, tailID);
			
			// Update or Add to the mapping of edge color and edge Id
			// Note: This generator does uses Real Edge IDs instead of fake IDs, as compared to previously developed generators. 
			IntSet setOfEdgeIds = mMapColourToEdgeIDsToUpdate.get(randomEdgeColov1v2);//mMapColourToEdgeIDs.get(randomEdgeColov1v2);
			if (setOfEdgeIds == null) {
				setOfEdgeIds = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDsToUpdate.put(randomEdgeColov1v2, setOfEdgeIds); //mMapColourToEdgeIDs.put(randomEdgeColov1v2, setOfEdgeIds);
			}
			setOfEdgeIds.add(edgeIdTemp);
			
			return true;
		
		}else {
			return false;
		}
	}

}
