package org.aksw.simba.lemming.mimicgraph.vertexselection;

import java.util.Map;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

import it.unimi.dsi.fastutil.ints.IntSet;

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
	/** Random number generator */
	private long seed;

	/** Map of colours to vertices IDs */
	private Map<BitSet, IntSet> mMapColourToVertexIDs;
	
	public UniformInstanceSelection() {
	}

	/**
	 * Constructor
	 * 
	 * @param mMapColourToVertexIDs Map of colours to vertex IDs
	 * @param seed                  Seed for the random number generator
	 */
	public UniformInstanceSelection(Map<BitSet, IntSet> mMapColourToVertexIDs, long seed) {
		this.mMapColourToVertexIDs = mMapColourToVertexIDs;
		this.seed = seed;
	}

	@Override
	public IOfferedItem<Integer> getProposedVertex(BitSet edgecolour, BitSet vertexColour, VERTEX_TYPE type) {
		return getProposedVertex(vertexColour);
	}

	public IOfferedItem<Integer> getProposedVertex(BitSet vertexColour) {
		Integer[] arrIDs = mMapColourToVertexIDs.get(vertexColour).toArray(Integer[]::new);
		IOfferedItem<Integer> item = new OfferedItemWrapper<Integer>(arrIDs, seed);
		return item;
	}

}
