package org.aksw.simba.borrowed.ldcbench.api;

/**
 * TODO add dependency when available in mvn repo, borrowed from
 * {@link  <a href="https://github.com/dice-group/orca/blob/develop/ldcbench.api/src/main/java/org/dice_research/ldcbench/generate/Graph.java">org.dice_research.ldcbench.generate.GraphGenerator</a>}
 * 
 * Interface of a class that is able to generate a graph based on the given
 * parameters.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface GraphGenerator {

    /**
     * Generates a graph based on the given parameters using the given
     * {@link GraphBuilder}.
     * 
     * @param numberOfNodes
     *            the number of nodes the generated graph should have
     * @param avgDegree
     *            the average degree the nodes in the graph should have
     * @param seed
     *            the seed that should be used to initialize pseudo random processes
     *            to enable reproducibility of graphs.
     * @param builder
     *            the {@link GraphBuilder} that should be used to create the graph.
     */
    public void generateGraph(int numberOfNodes, double avgDegree, long seed, GraphBuilder builder);

    /**
     * Generates a graph based on the given parameters using the given
     * {@link GraphBuilder}.
     * 
     * @param avgDegree
     *            the average degree the nodes in the graph should have
     * @param numberOfEdges
     *            the number of edges the generated graph should have
     * @param seed
     *            the seed that should be used to initialize pseudo random processes
     *            to enable reproducibility of graphs.
     * @param builder
     *            the {@link GraphBuilder} that should be used to create the graph.
     */
    public void generateGraph(double avgDegree, int numberOfEdges, long seed, GraphBuilder builder);
}
