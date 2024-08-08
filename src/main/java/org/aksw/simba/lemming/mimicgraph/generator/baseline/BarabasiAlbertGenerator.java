package org.aksw.simba.lemming.mimicgraph.generator.baseline;

import org.aksw.simba.lemming.ExtGrphBasedGraph;
import org.aksw.simba.lemming.util.Constants;
import org.dice_research.ldcbench.generate.ParallelBarabasiRDF;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import grph.Grph;

@Component("BA")
@Scope(value = "prototype")
public class BarabasiAlbertGenerator implements IGenerator {

	@Override
	public Grph generateGraph(int noVertices, double avgDegree, long seed) {
		ExtGrphBasedGraph baselineGraph = new ExtGrphBasedGraph();
		ParallelBarabasiRDF generator = new ParallelBarabasiRDF(Constants.BASELINE_STRING);
		if (avgDegree < 1) {
			avgDegree = 1;
		}
		generator.generateGraph(noVertices, avgDegree, seed, baselineGraph);
		return baselineGraph.getGrph();
	}
}
