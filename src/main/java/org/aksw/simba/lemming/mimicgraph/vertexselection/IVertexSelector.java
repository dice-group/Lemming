package org.aksw.simba.lemming.mimicgraph.vertexselection;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;

import com.carrotsearch.hppc.BitSet;

/**
 * Interface that represents the Instance Selectors for vertices
 * 
 * @author Ana Silva
 *
 */
public interface IVertexSelector {

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
	public OfferedItemWrapper<Integer> getProposedVertex(BitSet edgeColour, BitSet vertexColour, VERTEX_TYPE type);

	public Integer selectTailFromColour(BitSet tailColour);

	public Integer selectHeadFromColour(BitSet headColour, BitSet edgeColour, int candidateTailId);
	
}
