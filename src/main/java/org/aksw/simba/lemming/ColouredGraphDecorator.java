/**
 * 
 */
package org.aksw.simba.lemming;

import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Base Decorator for Coloured Graph Class
 * 
 * @author Pranav
 */
public class ColouredGraphDecorator implements IColouredGraph {

    protected IColouredGraph graph;
    protected boolean isAddingEdge;
    protected TripleBaseSingleID triple;

    public ColouredGraphDecorator() {
        this.graph = null;
        this.isAddingEdge = false;
        this.triple = null;
    }

    /**
     * Class constructor
     * 
     * @param isAddingEdge
     */
    public ColouredGraphDecorator(ColouredGraph clonedGraph, boolean isAddingEdgeFlag) {
        this.graph = clonedGraph;
        this.isAddingEdge = isAddingEdgeFlag;
    }

    /**
     * Returns ColouredGraph object
     * 
     * @return ColouredGraph graph
     */
    public IColouredGraph getGraph() {
        return this.graph;
    }

    public void setTriple(TripleBaseSingleID edge) {
        this.triple = edge;
    }

    public TripleBaseSingleID getTriple() {
        return this.triple;
    }

    @Override
    public IntSet getEdgesIncidentTo(int verticeId) {
        return this.graph.getEdgesIncidentTo(verticeId);
    }

    @Override
    public int getInEdgeDegree(int vertexId) {
        return this.graph.getInEdgeDegree(vertexId);
    }

    @Override
    public int getOutEdgeDegree(int vertexId) {
        return this.graph.getOutEdgeDegree(vertexId);
    }

    @Override
    public double getMaxInEdgeDegrees() {
        return this.graph.getMaxInEdgeDegrees();
    }

    @Override
    public double getMaxOutEdgeDegrees() {
        return this.graph.getMaxOutEdgeDegrees();
    }

    @Override
    public IntArrayList getAllInEdgeDegrees() {
        return this.graph.getAllInEdgeDegrees();
    }

    @Override
    public IntArrayList getAllOutEdgeDegrees() {
        return this.graph.getAllOutEdgeDegrees();
    }

    @Override
    public double getNumberOfEdges() {
        return this.graph.getNumberOfEdges();
    }

    @Override
    public double getNumberOfVertices() {
        return this.graph.getNumberOfVertices();
    }

    @Override
    public IntSet getVerticesIncidentToEdge(int edgeId) {
        return this.graph.getVerticesIncidentToEdge(edgeId);
    }

    @Override
    public IntSet getInNeighbors(int vertexId) {
        return this.graph.getInNeighbors(vertexId);
    }

    @Override
    public IntSet getOutNeighbors(int vertexId) {
        return this.graph.getOutNeighbors(vertexId);
    }

}
