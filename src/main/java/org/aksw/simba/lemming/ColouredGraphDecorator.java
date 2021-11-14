/**
 * 
 */
package org.aksw.simba.lemming;

import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import com.carrotsearch.hppc.BitSet;

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
    public ColouredGraphDecorator(IColouredGraph graph, boolean isAddingEdgeFlag) {
        this.graph = graph;
        this.isAddingEdge = isAddingEdgeFlag;
    }

    public ColouredGraphDecorator(IColouredGraph graph) {
        this.graph = graph;
    }

    /**
     * Returns ColouredGraphDecorator object
     * 
     * @return ColouredGraphDecorator graph
     */
    public IColouredGraph getGraph() {
        return this.graph;
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

    public void setGraph(IColouredGraph graph) {
        this.graph = graph;

    }

    @Override
    public int addEdge(int tailId, int headId, BitSet edgeColour) {
        return this.graph.addEdge(tailId, headId, edgeColour);
    }

    @Override
    public void removeEdge(int edgeId) {
        this.graph.removeEdge(edgeId);
    }

    @Override
    public IntSet getEdges() {
        return this.graph.getEdges();
    }

    @Override
    public BitSet getEdgeColour(int edgeId) {
        return this.graph.getEdgeColour(edgeId);
    }

    @Override
    public Object getRDFTypePropertyColour() {
        return this.graph.getRDFTypePropertyColour();
    }

    @Override
    public int getTailOfTheEdge(int edgeId) {
        return this.graph.getTailOfTheEdge(edgeId);
    }

    @Override
    public int getHeadOfTheEdge(int edgeId) {
        return this.graph.getHeadOfTheEdge(edgeId);
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
