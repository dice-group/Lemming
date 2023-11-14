package org.aksw.simba.lemming.simplexes;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.PoissonDistribution;
import java.util.Random;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;

public class TriangleFormationTest {

	//@Test
	public void poissonDistributionTest() {
		//long seed = System.currentTimeMillis();
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		for (int i =0; i < 8; i++) {
			System.out.println( PoissonDistribution.randomXJunhao(78, mRandom));
		}
	}
	
	/**
	 * Test to understand how to use ObjectDistribution class and generate values using Poisson
	 */
	@Test
	public void computeColorsUsingPoissonDistributionTest() {
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		
		// create sample bitsets denoting possible color for the third vertex
		BitSet bitSet = new BitSet();
		bitSet.set(1);
		System.out.println("BitSet value 1: " + bitSet);
		
		BitSet bitSet1 = new BitSet();
		bitSet1.set(2);
		System.out.println("BitSet value 2: " + bitSet1);
		
		BitSet bitSet2 = new BitSet();
		bitSet2.set(3);
		System.out.println("BitSet value 3: " + bitSet2);
		
		// create sample average number of edges for these bitsets
		double noOfEdges = 4;
		double noOfEdges1 = 6;
		double noOfEdges2 = 8;
		
		// generate values based on average using Poisson distribution
		double poisNoOfEdges = PoissonDistribution.randomXJunhao(noOfEdges, mRandom);
		double poisNoOfEdges1 = PoissonDistribution.randomXJunhao(noOfEdges1, mRandom);
		double poisNoOfEdges2 = PoissonDistribution.randomXJunhao(noOfEdges2, mRandom);
		System.out.println("Number of edges determined using Poisson distribution:");
		System.out.println(poisNoOfEdges);
		System.out.println(poisNoOfEdges1);
		System.out.println(poisNoOfEdges2);
		
		// Create an array for bitsets and number of edges
		BitSet[] bitarr = new BitSet[3];
		bitarr[0] = bitSet;
		bitarr[1] = bitSet1;
		bitarr[2] = bitSet2;
		
		double[] possEdges = new double[3];
		possEdges[0] = poisNoOfEdges;
		possEdges[1] = poisNoOfEdges1;
		possEdges[2] = poisNoOfEdges2;
		
		ObjectDistribution<BitSet> potentialColo = new ObjectDistribution<BitSet>(bitarr, possEdges);
		OfferedItemByRandomProb<BitSet> potentialColoProposer = new OfferedItemByRandomProb<BitSet>(potentialColo, mRandom);
		System.out.println(potentialColoProposer.getPotentialItem());
		System.out.println(potentialColoProposer.getPotentialItem());
		System.out.println(potentialColoProposer.getPotentialItem());
		System.out.println(potentialColoProposer.getPotentialItem());
		
		
		
	}
	
}
