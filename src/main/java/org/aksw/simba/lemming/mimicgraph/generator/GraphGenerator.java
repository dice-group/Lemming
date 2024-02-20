package org.aksw.simba.lemming.mimicgraph.generator;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;

public class GraphGenerator {
	
	/** Class Selector implementation */
	protected IClassSelector classSelector;
	
	/** Instance Selector implementation */
	protected IVertexSelector vertexSelector;
	
	
	/**
	 * Constructor.
	 * 
	 * @param classSelector
	 * @param vertexSelector
	 */
	public GraphGenerator(IClassSelector classSelector, IVertexSelector vertexSelector) {
		this.classSelector = classSelector;
		this.vertexSelector = vertexSelector;
	}
	
	public ColouredGraph initializeMimicGraph() {
		// 
		return null;
	}

}
