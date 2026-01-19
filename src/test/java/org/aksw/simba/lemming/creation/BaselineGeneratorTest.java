package org.aksw.simba.lemming.creation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.ExtGrphBasedGraph;
import org.aksw.simba.lemming.mimicgraph.generator.baseline.DirectedWattsStrogatz;
import org.aksw.simba.lemming.util.Constants;
import org.dice_research.ldcbench.generate.GraphGenerator;
import org.dice_research.ldcbench.generate.ParallelBarabasiRDF;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import grph.Grph;
import grph.in_memory.InMemoryGrph;

@RunWith(Parameterized.class)
public class BaselineGeneratorTest {

	private double degree;
	private int noVertices;
	private long seed;

	public BaselineGeneratorTest(double degree, int noVertices, long seed) {
		this.degree = degree;
		this.noVertices = noVertices;
		this.seed = seed;

	}

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> testConfigs = new ArrayList<Object[]>();
		testConfigs.add(new Object[] { 7.652143359100492, 1423, 42 });
		testConfigs.add(new Object[] { 8.25, 1423, 42 });
		testConfigs.add(new Object[] { 3.5, 10, 42 });
		testConfigs.add(new Object[] { 8, 1423, 42 });
		testConfigs.add(new Object[] { 2, 1423, 42 });
		testConfigs.add(new Object[] { 2.2, 1423, 42 });
		testConfigs.add(new Object[] { 10, 1423, 42 });
		testConfigs.add(new Object[] { 7.449170872, 1387, 42 });
		testConfigs.add(new Object[] { 8.055910754, 45358, 42 });
		return testConfigs;
	}

	@Test
    public void testAB() {
    	ExtGrphBasedGraph graph = new ExtGrphBasedGraph();
		GraphGenerator generator = new ParallelBarabasiRDF(Constants.BASELINE_STRING);
		generator.generateGraph(noVertices, degree/2, seed, graph);
		double avgDegree = graph.getGrph().getAverageDegree();
		Assert.assertTrue(avgDegree > 0);
	}

	@Test
	public void testWS() {
		Grph g = new InMemoryGrph();
		g.addNVertices(noVertices);
		DirectedWattsStrogatz.compute(g, degree, 0.5, seed);
		double avgDegree = g.getAverageDegree();
		Assert.assertTrue(avgDegree > 0);
	}
}
