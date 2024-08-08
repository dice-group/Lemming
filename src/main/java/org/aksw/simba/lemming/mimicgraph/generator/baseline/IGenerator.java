package org.aksw.simba.lemming.mimicgraph.generator.baseline;

import grph.Grph;

public interface IGenerator {
	
	public Grph generateGraph(int noVertices, double avgDegree, long seed);

}
