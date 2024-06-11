package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.simplexes.TriColours;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * Interface for Triangle Distribution implementations.
 */
public interface ITriDist {

	/**
	 * Returns a vertex colour given 2 already existing colours in the triangle.
	 * 
	 * @param vertex1Color
	 * @param vertex2Color
	 * @return 3rd Vertex Colour
	 */
	BitSet proposeVertexColorForVertex3(BitSet vertex1Color, BitSet vertex2Color);

	/**
	 * Returns a set of 3 vertex colours
	 * 
	 * @param setTriangleColorsMimicGraph
	 * @return
	 */
	TriColours proposeTriangleToAddEdge(Set<TriColours> setTriangleColorsMimicGraph);

	IOfferedItem<TriColours> getPotentialIsolatedTriangleProposer();

	TriColours proposeIsoTriToAddEdge(Set<TriColours> setIsoTriInMimicGraph);

	ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>> getmTriangleColorsv1v2v3();

}
