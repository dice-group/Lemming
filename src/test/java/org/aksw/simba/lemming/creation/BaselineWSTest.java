package org.aksw.simba.lemming.creation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.aksw.simba.lemming.mimicgraph.generator.baseline.DirectedWattsStrogatz;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import it.unimi.dsi.fastutil.ints.IntSet;

@RunWith(Parameterized.class)
public class BaselineWSTest {

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> testConfigs = new ArrayList<Object[]>();
		testConfigs.add(new Object[] { 7, 0, 4, new int[] { 5, 6 }, new int[] { 1, 2 } });
		testConfigs.add(new Object[] { 7, 2, 4, new int[] { 0, 1 }, new int[] { 3, 4 } });
		testConfigs.add(new Object[] { 7, 6, 4, new int[] { 4, 5 }, new int[] { 0, 1 } });
		testConfigs.add(new Object[] { 7, 6, 5, new int[] { 3, 4, 5 }, new int[] { 0, 1, 2 } });
		testConfigs.add(new Object[] { 2, 0, 2, new int[] { 1 }, new int[] { 1 } });
		testConfigs.add(new Object[] { 7, 6, 2.32, new int[] { 4, 5 }, new int[] { 0, 1 } });
		testConfigs.add(new Object[] { 7, 6, 1.32, new int[] { 5 }, new int[] { 0 } });
		return testConfigs;
	}

	private int maxVertices;
	private int vertex;
	private double neighbours;
	private int[] expectedLeft;
	private int[] expectedRight;

	public BaselineWSTest(int maxVertices, int vertex, double neighbours, int[] expectedLeft, int[] expectedRight) {
		this.maxVertices = maxVertices;
		this.vertex = vertex;
		this.neighbours = neighbours;
		this.expectedLeft = expectedLeft;
		this.expectedRight = expectedRight;
	}

	@Test
	public void testCycleRange() {
		DirectedWattsStrogatz ws = new DirectedWattsStrogatz();
		IntSet[] n = ws.getKClosestNeighbours(maxVertices, vertex, neighbours);
		Assert.assertArrayEquals(expectedLeft, n[0].toIntArray());
		Assert.assertArrayEquals(expectedRight, n[1].toIntArray());
	}
	
	@Test
	public void testEdgeContributions() {
		DirectedWattsStrogatz ws = new DirectedWattsStrogatz();
		ws.setPRNG(new Random(123));
		int[] edgeContributions = ws.assignEdges(maxVertices, neighbours);
		int sum =  Arrays.stream(edgeContributions).sum();
		Assert.assertTrue(sum <= Math.floor(maxVertices*neighbours));
	}
}
