package org.aksw.simba.lemming.mimicgraph.generator;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgEdgeColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgVertColoDistMetric;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 */
@Component("bl")
@Scope(value = "prototype")
public class BaselineInitializer extends GraphInitializer {


	/**
	 * 
	 * @param seedGenerator
	 */
	public BaselineInitializer(SeedGenerator seedGenerator) {
		super(seedGenerator);
	}

	/**
	 * 
	 */
	@Override
	public ColouredGraph initialize(ColouredGraph[] origGrphs, int noOfVertices, int noOfThreads) {
		this.originalGraphs = origGrphs;
		this.desiredNoOfVertices = noOfVertices;
		this.desiredNoOfEdges = estimateNoEdges(origGrphs, noOfVertices);
		
		ColouredGraph mimicGraph = new ColouredGraph();
		
		// colour distribution
		vertexColourDist = AvrgVertColoDistMetric.apply(origGrphs);
		edgeColourDist = AvrgEdgeColoDistMetric.apply(origGrphs);

		return mimicGraph;
	}

}
