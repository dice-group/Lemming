package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.EdgeColos;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public interface ISimplexProperty {

	BitSet proposeColour(EdgeColorsSorted edgeColors);

	OfferedItemByRandomProb<EdgeColos> getIsolatedEdgeProposer();

	BitSet proposeColourForTri(EdgeColorsSorted edgeColors);

	EdgeColos proposeConnEdge();

	ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>> getEdgeColoursV1V2Dist();

	BitSet proposeConnPropColour(EdgeColorsSorted edgeColorsSorted);

	EdgeColos proposeTriangleToAddEdge(Set<EdgeColos> setEdgeColorsMimicGraph);

	BitSet proposeS1ConnProp(EdgeColorsSorted edgeColorsSorted);

	IPropertyDist getmPropDistselfLoopsInIsoS1();

	IPropertyDist getmPropDistselfLoopIsoTri();

	IPropertyDist getmPropDistselfLoopConnTri();

	IPropertyDist getmPropDistselfLoops1ConnToTri();

	IPropertyDist getmPropDistSelfLoopConnS1();

	IPropertyDist getmPropDistconnS1Analysis();

	IPropertyDist getmPropDistConnTri();

	IPropertyDist getmPropDistisoS1();

	IPropertyDist getmPropDistisoS1SelfLoop();

	IPropertyDist getmPropDistS1ConnectingTri();

	IPropertyDist getmPropDistIsoTri();

}
