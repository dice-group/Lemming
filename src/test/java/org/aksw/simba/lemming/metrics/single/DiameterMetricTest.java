package org.aksw.simba.lemming.metrics.single;

import grph.Grph;
import grph.algo.topology.GridTopologyGenerator;
import grph.algo.topology.RingTopologyGenerator;
import grph.in_memory.InMemoryGrph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DiameterMetricTest {
	
	public static final double DELTA = 0.00001;

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> testConfigs = new ArrayList<Object[]>();
		Grph graph;

		GridTopologyGenerator gridGenerator = new GridTopologyGenerator();
		graph = new InMemoryGrph();
		gridGenerator.setWidth(10);
		gridGenerator.setHeight(10);
		gridGenerator.compute(graph);
		testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 18 });
		graph = new InMemoryGrph();
		gridGenerator.setWidth(20);
		gridGenerator.setHeight(20);
		gridGenerator.compute(graph);
		testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 38 });
		graph = new InMemoryGrph();
		gridGenerator.setWidth(101);
		gridGenerator.setHeight(101);
		gridGenerator.compute(graph);
		testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 200 });

        RingTopologyGenerator ringGenerator = new RingTopologyGenerator();
		graph = new InMemoryGrph();
		graph.addNVertices(10);
        ringGenerator.compute(graph);
		testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 5 });
		graph = new InMemoryGrph();
		graph.addNVertices(100);
        ringGenerator.compute(graph);
		testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 50 });
		graph = new InMemoryGrph();
		graph.addNVertices(1000);
        ringGenerator.compute(graph);
		testConfigs.add(new Object[] { new ColouredGraph(graph, null, null), 500 });

		return testConfigs;
	}

	private ColouredGraph graph;
	private int expectedDiameter;

	public DiameterMetricTest(ColouredGraph graph, Integer expectedDiameter) {
		this.graph = graph;
		this.expectedDiameter = expectedDiameter;
	}

	@Test
	public void run() {
		DiameterMetric metric = new DiameterMetric();
		double diameter = metric.apply(graph);
		Assert.assertEquals(expectedDiameter, diameter, DELTA);
	}
}
