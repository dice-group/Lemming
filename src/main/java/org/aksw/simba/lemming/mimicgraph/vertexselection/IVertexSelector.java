package org.aksw.simba.lemming.mimicgraph.vertexselection;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;

import com.carrotsearch.hppc.BitSet;

/**
 * Interface that represents the Instance Selectors for vertices
 * 
 * @author Ana Silva
 *
 */
public interface IVertexSelector {

	/**
	 * Vertex type of the link we want to create, it can only be head or tail.
	 */
	public enum VERTEX_TYPE {
		HEAD, TAIL
	}

	/**
	 * Retrieves the proposed instances of a vertex Colour
	 * 
	 * @param edgecolour   edge colour
	 * @param vertexColour vertex colour
	 * @param type         vertex type
	 * @return
	 */
	public IOfferedItem<Integer> getProposedVertex(BitSet edgeColour, BitSet vertexColour, VERTEX_TYPE type);

}
