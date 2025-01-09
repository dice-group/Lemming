package org.aksw.simba.lemming.mimicgraph.vertexselection;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;
import org.aksw.simba.lemming.mimicgraph.generator.GraphInitializer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

/**
 * Implementation of the Uniform Instance Selection (UIS). Retrieves a random
 * vertex ID from a set of all possible IDs.
 * 
 * @author Ana Silva
 *
 */
@Component("UIS")
@Scope(value = "prototype")
public class UniformInstanceSelection implements IVertexSelector {

	private GraphInitializer graphInit;

	/**
	 * Constructor
	 * 
	 */
	public UniformInstanceSelection(GraphInitializer graphInit) {
		this.graphInit = graphInit;
	}

	@Override
	public OfferedItemWrapper<Integer> getProposedVertex(BitSet edgecolour, BitSet vertexColour, VERTEX_TYPE type) {
		return getProposedVertex(vertexColour);
	}

	/**
	 * Retrieves a random vertex instance from the set of vertices of a given
	 * colour.
	 * 
	 * @param vertexColour The desired vertex colour
	 * @return The vertex instance
	 */
	public OfferedItemWrapper<Integer> getProposedVertex(BitSet vertexColour) {
		Integer[] arrIDs = graphInit.getmMapColourToVertexIDs().get(vertexColour).toArray(Integer[]::new);
		OfferedItemWrapper<Integer> item = new OfferedItemWrapper<Integer>(arrIDs, graphInit.getSeedGenerator());
		return item;
	}

}
