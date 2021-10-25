/**
 * 
 */
package org.aksw.simba.lemming;

import grph.Grph;

/**
 * Stub Decorator of the ColouredGraph Class
 * 
 * @author Pranav
 */
public class ColouredGraphDecorator extends ColouredGraph {

    protected ColouredGraph graph;
    protected boolean isAddingEdge;

    /**
     * Class constructor
     * 
     * @param isAddingEdge
     */
    public ColouredGraphDecorator(ColouredGraph graph, boolean isAddingEdge) {
        this.graph = graph;
        this.isAddingEdge = isAddingEdge;
    }

    /**
     * Returns the Grph object enclosed within ColouredGraph
     * 
     * @return Grph
     */
    public Grph getGraph() {
        return this.graph.getGraph();
    }

    /**
     * Returns ColouredGraph object
     * 
     * @return ColouredGraph graph
     */
    public ColouredGraph getDecoratedGraph() {
        return this.graph;
    }

}
