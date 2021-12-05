/**
 * 
 */
package org.aksw.simba.lemming;

import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.IntSetUtil;

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
     * @param isAddingEdge - represents the edge operation.Flag is true if edge is
     *                     being added. Flag is false If edge is being removed.
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

    /**
     * Get list of all Edge IDs connecting to vertex
     *
     * @param verticeId - verticeId the id of an vertex
     * @return IntSet - set of edge IDs
     */
    @Override
    public IntSet getEdgesIncidentTo(int verticeId) {
        return this.graph.getEdgesIncidentTo(verticeId);
    }

    /**
     * Get in edge degree of a vertex
     *
     * @param verticeId - the id of an vertex
     * @return int - in edge degree value
     */
    @Override
    public int getInEdgeDegree(int vertexId) {
        return this.graph.getInEdgeDegree(vertexId);
    }

    /**
     * Get out edge degree of a vertex
     *
     * @param verticeId - the id of an vertex
     * @return int - out edge degree value
     */
    @Override
    public int getOutEdgeDegree(int vertexId) {
        return this.graph.getOutEdgeDegree(vertexId);
    }

    /**
     * Get max in edge degree of the graph
     * 
     * @return double
     */
    @Override
    public double getMaxInEdgeDegrees() {
        return this.graph.getMaxInEdgeDegrees();
    }

    /**
     * Get max in edge degree of the graph
     * 
     * @return double
     */
    @Override
    public double getMaxOutEdgeDegrees() {
        return this.graph.getMaxOutEdgeDegrees();
    }

    /**
     * Get in degrees of all the vertices in the graph
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllInEdgeDegrees() {
        return this.graph.getAllInEdgeDegrees();
    }

    /**
     * Get out degrees of all the vertices in the graph
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllOutEdgeDegrees() {
        return this.graph.getAllOutEdgeDegrees();
    }

    /**
     * Get number of edges in the graph
     * 
     * @return double
     */
    @Override
    public double getNumberOfEdges() {
        return this.graph.getNumberOfEdges();
    }

    /**
     * Get number of nodes in the graph
     * 
     * @return double
     */
    @Override
    public double getNumberOfVertices() {
        return this.graph.getNumberOfVertices();
    }

    public void setGraph(IColouredGraph graph) {
        this.graph = graph;

    }

    /**
     * Add given edge to the graph
     * 
     * @param tailId
     * @param headId
     * @param edgeColour
     * @return int - the edgeId of newly added edge
     */
    @Override
    public int addEdge(int tailId, int headId, BitSet edgeColour) {
        return this.graph.addEdge(tailId, headId, edgeColour);
    }

    /**
     * Remove edge from the graph
     * 
     * @param edgeId
     */
    @Override
    public void removeEdge(int edgeId) {
        this.graph.removeEdge(edgeId);
    }

    /**
     * Get Set of all edges in the graph
     * 
     * @return IntSet
     */
    @Override
    public IntSet getEdges() {
        return this.graph.getEdges();
    }

    /**
     * Get edge color
     * 
     * @param edgeId
     * @return BitSet
     */
    @Override
    public BitSet getEdgeColour(int edgeId) {
        return this.graph.getEdgeColour(edgeId);
    }

    /**
     * Get property color
     * 
     * @return Object
     */
    @Override
    public Object getRDFTypePropertyColour() {
        return this.graph.getRDFTypePropertyColour();
    }

    /**
     * Get the vertex id of the tail if the edge
     * 
     * @param edgeId
     * @return int
     */
    @Override
    public int getTailOfTheEdge(int edgeId) {
        return this.graph.getTailOfTheEdge(edgeId);
    }

    /**
     * Get the vertex id of the head if the edge
     * 
     * @param edgeId
     * @return int
     */
    @Override
    public int getHeadOfTheEdge(int edgeId) {
        return this.graph.getHeadOfTheEdge(edgeId);
    }

    /**
     * Get list of all vertex IDs connecting to the the edgeId
     *
     * @param edgeId - the id of an edge connecting the vertices together
     * @return set of vertex ID's
     */
    @Override
    public IntSet getVerticesIncidentToEdge(int edgeId) {
        return this.graph.getVerticesIncidentToEdge(edgeId);
    }

    /**
     * Get set of all in neighbors to a vertex
     * 
     * @param vertexId
     * @return set of all in neighbors
     */
    @Override
    public IntSet getInNeighbors(int vertexId) {
        return this.graph.getInNeighbors(vertexId);
    }

    /**
     * Get set of all out neighbors to a vertex
     * 
     * @param vertexId
     * @return set of all out neighbors
     */
    @Override
    public IntSet getOutNeighbors(int vertexId) {
        return this.graph.getOutNeighbors(vertexId);
    }

    /**
     * Get set of all vertices
     * 
     * @return set of all vertices
     */
    @Override
    public IntSet getVertices() {
        return this.graph.getVertices();
    }

    /**
     * Get number of edges between two vertices
     * 
     * @return int - number of edges
     */
    public int getNumberOfEdgesBetweenVertices() {
        return IntSetUtil.intersection(this.graph.getEdgesIncidentTo(this.triple.tailId),
                this.graph.getEdgesIncidentTo(this.triple.headId)).size();
    }

}
