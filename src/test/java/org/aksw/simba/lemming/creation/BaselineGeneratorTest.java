package org.aksw.simba.lemming.creation;

import org.aksw.simba.lemming.util.Constants;
import org.dice_research.ldcbench.generate.GraphGenerator;
import org.dice_research.ldcbench.generate.ParallelBarabasiRDF;
import org.dice_research.ldcbench.graph.GrphBasedGraph;
import org.junit.Test;

public class BaselineGeneratorTest {
	
	@Test
    public void test() {
    	long seed = System.currentTimeMillis();
    	GrphBasedGraph graph = new GrphBasedGraph();
		GraphGenerator generator = new ParallelBarabasiRDF(Constants.BASELINE_STRING);
		generator.generateGraph(1423, 7.652143359100492, seed, graph);
	}
}
