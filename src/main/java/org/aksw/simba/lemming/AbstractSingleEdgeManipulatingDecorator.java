package org.aksw.simba.lemming;

import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Abstract decorator that simulates the manipulation of a single edge (i.e., a
 * single triple) in the graph.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public abstract class AbstractSingleEdgeManipulatingDecorator extends ColouredGraphDecorator {

    /**
     * Triple information that is supposed to be added/removed in the current
     * iteration
     */
    protected TripleBaseSingleID triple;

    /**
     * Class Constructor
     * 
     * @param graph - IColouredGraph object that is to be decorated
     */
    public AbstractSingleEdgeManipulatingDecorator(IColouredGraph graph) {
        super(graph);
    }

    /**
     * Store current triple data
     * 
     * @param edge - Triple data
     */
    public void setTriple(TripleBaseSingleID edge) {
        this.triple = edge;
    }

    /**
     * Get current triple offered for adding or removing to the ColouredGraph
     * 
     * @return TripleBaseSingleID
     */
    public TripleBaseSingleID getTriple() {
        return this.triple;
    }

    /**
     * Get new in edge degrees of all the vertices. All the vertices will have the
     * same in degree except the vertex to which edge has been added
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllInEdgeDegrees() {
        IntArrayList inDegrees = super.getAllInEdgeDegrees();
        inDegrees.set(this.triple.tailId, getInEdgeDegree(this.triple.tailId));
        return inDegrees;
    }

    /**
     * Get new out edge degrees of all the vertices. All the vertices will have the
     * same out degree except the vertex to which edge has been added
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllOutEdgeDegrees() {
        IntArrayList outDegrees = super.getAllOutEdgeDegrees();
        outDegrees.set(this.triple.headId, getOutEdgeDegree(this.triple.headId));
        return outDegrees;
    }

}
