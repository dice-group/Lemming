package org.aksw.simba.lemming.mimicgraph.colourselection;

import org.aksw.simba.lemming.creation.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemWrapper;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

/**
 * 
 */
@Component("UCS")
@Scope(value = "prototype")
public class UniformClassSelector implements IClassSelector {

	private GraphInitializer graphInit;
	private SeedGenerator seedGenerator;

	public UniformClassSelector(GraphInitializer graphInit) {
		this.graphInit = graphInit;
		this.seedGenerator = graphInit.getSeedGenerator();
	}

	@Override
	public IOfferedItem<BitSet> getTailClass(BitSet edgeColour) {		
		IOfferedItem<BitSet> proposer = new OfferedItemWrapper<BitSet>(
				graphInit.getColourMapper().getTailColoursFromEdgeColour(edgeColour).toArray(BitSet[]::new),
				seedGenerator);
		return proposer;
	}
	
	@Override
	public IOfferedItem<BitSet> getHeadClass(BitSet tailColour, BitSet edgeColour) {
		IOfferedItem<BitSet> proposer = new OfferedItemWrapper<BitSet>(
				graphInit.getColourMapper().getHeadColours(tailColour, edgeColour).toArray(BitSet[]::new),
				seedGenerator);
		return proposer;
	}
	
	@Override
	public BitSet getEdgeColourProposal() {
		BitSet[] possibilities = graphInit.getAvailableEdgeColours().toArray(BitSet[]::new);
		OfferedItemWrapper<BitSet> proposer = new OfferedItemWrapper<BitSet>(possibilities, seedGenerator);
		return tryValidColour(proposer, 500);
	}
//
//	@Override
//	public BitSet getTailClass() {
//		BitSet[] possibilities = graphInit.getAvailableVertexColours().toArray(BitSet[]::new);
//		OfferedItemWrapper<BitSet> proposer = new OfferedItemWrapper<BitSet>(possibilities, seedGenerator);
//		return tryValidColour(proposer, 500);
//	}
//
//	@Override
//	public BitSet getHeadClassFromTailColour(BitSet tailColour) {
//		Set<BitSet> availableColours = graphInit.getAvailableVertexColours();
//		BitSet[] possibilities =  graphInit.getColourMapper().getHeadColours(tailColour).toArray(BitSet[]::new);
//		OfferedItemWrapper<BitSet> proposer = new OfferedItemWrapper<BitSet>(possibilities, seedGenerator);
//		return tryValidColour(proposer, availableColours, 500);
//	}
//
//	@Override
//	public BitSet getEdgeColourFromTailHeadColour(BitSet tailColour, BitSet headColour) {
//		Set<BitSet> availableColours = graphInit.getAvailableEdgeColours();
//		BitSet[] possibilities =  graphInit.getColourMapper().getPossibleLinkingEdgeColours(tailColour, headColour).toArray(BitSet[]::new);
//		OfferedItemWrapper<BitSet> proposer = new OfferedItemWrapper<BitSet>(possibilities, seedGenerator);
//		return tryValidColour(proposer, availableColours, 500);
//	}
}
