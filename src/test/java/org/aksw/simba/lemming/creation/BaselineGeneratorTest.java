package org.aksw.simba.lemming.creation;

import org.aksw.simba.lemming.util.Constants;
import org.borrowed.ldcbenc.api.GraphGenerator;
import org.borrowed.ldcbenc.api.GrphBasedGraph;
import org.borrowed.ldcbenc.api.ParallelBarabasiRDF;
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
