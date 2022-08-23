/**
 * 
 */
package org.aksw.simba.lemming;

import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import com.carrotsearch.hppc.BitSet;

import grph.Grph.DIRECTION;
import grph.Grph;
import grph.path.ArrayListPath;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Base Decorator for {@link ColouredGraph} Class
 * 
 * @author Pranav
 */
public class ColouredGraphDecorator implements IColouredGraph {

    /**
     * Concrete object that will be decorated
     */
    protected IColouredGraph decoratedGraph;

    /**
     * A flag to denote whether the current decorator is used for addition or
     * removal of an edge
     */
    protected boolean isAddingEdge;

    /**
     * Triple information that is supposed to be added/removed in the current
     * iteration
     */
    protected TripleBaseSingleID triple;

    /**
     * Class constructor
     * 
     * @param graph        - the IColouredGraph graph object representing the Graph
     *                     in the current iteration
     * @param isAddingEdge - represents the edge operation. Flag is true if edge is
     *                     being added. Flag is false If edge is being removed.
     */
    public ColouredGraphDecorator(IColouredGraph graph, boolean isAddingEdgeFlag) {
        this.decoratedGraph = graph;
        this.isAddingEdge = isAddingEdgeFlag;
    }

    public ColouredGraphDecorator(IColouredGraph graph) {
        this.decoratedGraph = graph;
    }

    /**
     * Returns {@link Grph} object
     * 
     * @return {@link Grph} graph object
     */
    @Override
    public Grph getGraph() {
        return this.decoratedGraph.getGraph();
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
        return this.decoratedGraph.getEdgesIncidentTo(verticeId);
    }

    /**
     * Get in edge degree of a vertex
     *
     * @param verticeId - the id of an vertex
     * @return int - in edge degree value
     */
    @Override
    public int getInEdgeDegree(int vertexId) {
        return this.decoratedGraph.getInEdgeDegree(vertexId);
    }

    /**
     * Get out edge degree of a vertex
     *
     * @param verticeId - the id of an vertex
     * @return int - out edge degree value
     */
    @Override
    public int getOutEdgeDegree(int vertexId) {
        return this.decoratedGraph.getOutEdgeDegree(vertexId);
    }

    /**
     * Get max in edge degree of the graph
     * 
     * @return double
     */
    @Override
    public double getMaxInEdgeDegrees() {
        return this.decoratedGraph.getMaxInEdgeDegrees();
    }

    /**
     * Get max in edge degree of the graph
     * 
     * @return double
     */
    @Override
    public double getMaxOutEdgeDegrees() {
        return this.decoratedGraph.getMaxOutEdgeDegrees();
    }

    /**
     * Get in degrees of all the vertices in the graph
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllInEdgeDegrees() {
        return this.decoratedGraph.getAllInEdgeDegrees();
    }

    /**
     * Get out degrees of all the vertices in the graph
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllOutEdgeDegrees() {
        return this.decoratedGraph.getAllOutEdgeDegrees();
    }

    /**
     * Get number of edges in the graph
     * 
     * @return double
     */
    @Override
    public double getNumberOfEdges() {
        return this.decoratedGraph.getNumberOfEdges();
    }

    /**
     * Get number of nodes in the graph
     * 
     * @return double
     */
    @Override
    public double getNumberOfVertices() {
        return this.decoratedGraph.getNumberOfVertices();
    }

    public void setGraph(IColouredGraph graph) {
        this.decoratedGraph = graph;

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
        return this.decoratedGraph.addEdge(tailId, headId, edgeColour);
    }

    /**
     * Remove edge from the graph
     * 
     * @param edgeId
     */
    @Override
    public void removeEdge(int edgeId) {
        this.decoratedGraph.removeEdge(edgeId);
    }

    /**
     * Get Set of all edges in the graph
     * 
     * @return IntSet
     */
    @Override
    public IntSet getEdges() {
        return this.decoratedGraph.getEdges();
    }

    /**
     * Get edge color
     * 
     * @param edgeId
     * @return BitSet
     */
    @Override
    public BitSet getEdgeColour(int edgeId) {
        return this.decoratedGraph.getEdgeColour(edgeId);
    }

    /**
     * Get property color
     * 
     * @return Object
     */
    @Override
    public Object getRDFTypePropertyColour() {
        return this.decoratedGraph.getRDFTypePropertyColour();
    }

    /**
     * Get the vertex id of the tail if the edge
     * 
     * @param edgeId
     * @return int
     */
    @Override
    public int getTailOfTheEdge(int edgeId) {
        return this.decoratedGraph.getTailOfTheEdge(edgeId);
    }

    /**
     * Get the vertex id of the head if the edge
     * 
     * @param edgeId
     * @return int
     */
    @Override
    public int getHeadOfTheEdge(int edgeId) {
        return this.decoratedGraph.getHeadOfTheEdge(edgeId);
    }

    /**
     * Get list of all vertex IDs connecting to the the edgeId
     *
     * @param edgeId - the id of an edge connecting the vertices together
     * @return set of vertex ID's
     */
    @Override
    public IntSet getVerticesIncidentToEdge(int edgeId) {
        return this.decoratedGraph.getVerticesIncidentToEdge(edgeId);
    }

    /**
     * Get set of all in neighbors to a vertex
     * 
     * @param vertexId
     * @return set of all in neighbors
     */
    @Override
    public IntSet getInNeighbors(int vertexId) {
        return this.decoratedGraph.getInNeighbors(vertexId);
    }

    /**
     * Get set of all out neighbors to a vertex
     * 
     * @param vertexId
     * @return set of all out neighbors
     */
    @Override
    public IntSet getOutNeighbors(int vertexId) {
        return this.decoratedGraph.getOutNeighbors(vertexId);
    }

    /**
     * Get set of all vertices
     * 
     * @return set of all vertices
     */
    @Override
    public IntSet getVertices() {
        return this.decoratedGraph.getVertices();
    }

    @Override
    public int getNumberOfEdgesBetweenVertices(int headId, int tailId) {
        return this.decoratedGraph.getNumberOfEdgesBetweenVertices(headId, tailId);
    }

    @Override
    public double getDiameter() {
        return this.decoratedGraph.getDiameter();
    }

    @Override
    public BitSet getVertexColour(int vId) {
        return this.decoratedGraph.getVertexColour(vId);
    }

    @Override
    public ColouredGraph copy() {
        return this.decoratedGraph.copy();
    }

    @Override
    public ColourPalette getVertexPalette() {
        return this.decoratedGraph.getVertexPalette();
    }

    @Override
    public ColourPalette getEdgePalette() {
        return this.decoratedGraph.getEdgePalette();
    }

    @Override
    public int computeShorterDiameter(TripleBaseSingleID triple, ArrayListPath path) {
        return this.decoratedGraph.computeShorterDiameter(triple, path);
    }

    @Override
    public ArrayListPath getDiameterPath() {
        return this.decoratedGraph.getDiameterPath();
    }

    @Override
    public int[][] getNeighbors(DIRECTION direction) {
        return this.decoratedGraph.getNeighbors(direction);
    }

}
