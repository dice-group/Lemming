package org.aksw.simba.lemming.mimicgraph.colourselection;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.creation.GraphInitializer;
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
	public BitSet getTailClass(BitSet edgeColour) {
		Random random = new Random(seedGenerator.getNextSeed());
		// get all possible classes and filter them with what we know is possible
		Set<BitSet> setTailColours = new HashSet<BitSet>(
				graphInit.getColourMapper().getTailColoursFromEdgeColour(edgeColour));
		Set<BitSet> setAvailableVertexColours = graphInit.getAvailableVertexColours();
		setTailColours.retainAll(setAvailableVertexColours);

		// if empty, return
		if (setTailColours.isEmpty()) {
			return null;
		}

		// get a random tail colour
		BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);
		return arrTailColours[random.nextInt(arrTailColours.length)];
	}
	
	@Override
	public BitSet getHeadClass(BitSet tailColour, BitSet edgeColour) {
		Random random = new Random(seedGenerator.getNextSeed());
		// get all possible classes and filter them with what we know is possible
		Set<BitSet> setHeadColours = new HashSet<BitSet>(
				graphInit.getColourMapper().getHeadColours(tailColour, edgeColour));
		Set<BitSet> setAvailableVertexColours = graphInit.getAvailableVertexColours();
		setHeadColours.retainAll(setAvailableVertexColours);

		// if empty, return
		if (setHeadColours.isEmpty()) {
			return null;
		}

		// get a random tail colour
		BitSet[] arrHeadColours = setHeadColours.toArray(new BitSet[0]);
		return arrHeadColours[random.nextInt(arrHeadColours.length)];
	}

}
