package org.aksw.simba.lemming.simplexes.analysis;

/**
 * Interface for analyzing simplexes present in input graphs. 
 * For different simplexes, the logic for finding them needs to be defined.
 * The color mapper needs to be computed to create properties for them, and logic for estimations of edges and vertices also needs to be defined.
 */
public interface ISimplexesAnalysis {

	/**
	 * This method defines the logic for finding simplexes.
	 */
	public void findSimplexes();
	
	/**
	 * The method defines the logic to determine color mapper. It needs to be computed for creating properties for simplexes.
	 */
	public void computeColorMapper();
	
	/**
	 * The calculation of estimated edges for simplexes is defined in this method.
	 */
	public void estimateEdges();
	
	/**
	 * The calculation of estimated vertices for simplexes is defined in this method.
	 */
	public void estimateVertices();
}
