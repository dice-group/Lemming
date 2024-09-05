package org.aksw.simba.lemming.creation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.ExtGrphBasedGraph;
import org.aksw.simba.lemming.mimicgraph.generator.baseline.DirectedWattsStrogatz;
import org.aksw.simba.lemming.util.Constants;
import org.dice_research.ldcbench.generate.GraphGenerator;
import org.dice_research.ldcbench.generate.ParallelBarabasiRDF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import grph.Grph;

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
		testConfigs.add(new Object[] { 8, 1423, 42 });
		testConfigs.add(new Object[] { 1, 1423, 42 });
		testConfigs.add(new Object[] { 1.2, 1423, 42 });
		testConfigs.add(new Object[] { 1.8, 1423, 42 });
		testConfigs.add(new Object[] { 0.8, 1423, 42 });
		testConfigs.add(new Object[] { 10, 1423, 42 });
		return testConfigs;
	}

	@Test
    public void testAB() {
    	ExtGrphBasedGraph graph = new ExtGrphBasedGraph();
		GraphGenerator generator = new ParallelBarabasiRDF(Constants.BASELINE_STRING);
		generator.generateGraph(noVertices, degree/2, seed, graph);
		double actualDegree = graph.getGrph().getAverageDegree();
	}

	@Test
	public void testWS() {
		DirectedWattsStrogatz tg = new DirectedWattsStrogatz();
		Grph g = tg.generateGraph(noVertices, degree, seed);
		double actualDegree = g.getAverageDegree();
	}
}
