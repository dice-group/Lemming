package org.aksw.simba.lemming.mimicgraph.vertexselection;

import java.util.Set;

import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
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

	public OfferedItemWrapper<Integer> getProposedVertex(BitSet vertexColour) {
		Integer[] arrIDs = graphInit.getmMapColourToVertexIDs().get(vertexColour).toArray(Integer[]::new);
		OfferedItemWrapper<Integer> item = new OfferedItemWrapper<Integer>(arrIDs,
				graphInit.getSeedGenerator());
		return item;
	}

	@Override
	public Integer selectTailFromColour(BitSet tailColour) {
		IOfferedItem<Integer> tailIdProposer = getProposedVertex(tailColour);
		return tailIdProposer.getPotentialItem();
	}

	@Override
	public Integer selectHeadFromColour(BitSet headColour, BitSet edgeColour, int candidateTailId) {
		OfferedItemWrapper<Integer> headIdProposer = getProposedVertex(headColour);
		Set<Integer> tmpSetOfConnectedHeads = graphInit.getConnectedHeadsSet(candidateTailId, edgeColour);
		return headIdProposer.getPotentialItemRemove(tmpSetOfConnectedHeads);
	}

}
