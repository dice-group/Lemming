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
 * This interface describes the Graph Object. Currently the methods defined in
 * this interface are the ones used by the graph object in the graph amendment
 * phase. These methods have to be overridden in the
 * {@link ColouredGraphDecorator} class for them to support parallel execution
 * of the graph amendment phase.
 * 
 * @author Pranav
 */
public interface IColouredGraph {

    /**
     * Get list of all Edge IDs connecting to vertex
     *
     * @param verticeId - verticeId the id of an vertex
     * @return IntSet - set of edge IDs
     */
    IntSet getEdgesIncidentTo(int verticeId);

    /**
     * Get in edge degree of a vertex
     *
     * @param verticeId - the id of an vertex
     * @return int - in edge degree value
     */
    int getInEdgeDegree(int vertexId);

    /**
     * Get out edge degree of a vertex
     *
     * @param verticeId - the id of an vertex
     * @return int - out edge degree value
     */
    int getOutEdgeDegree(int vertexId);

    /**
     * Get max in edge degree of the graph
     * 
     * @return double
     */
    double getMaxInEdgeDegrees();

    /**
     * Get max out edge degree of the graph
     * 
     * @return double
     */
    double getMaxOutEdgeDegrees();

    /**
     * Get in edge degrees of all the vertices
     * 
     * @return IntArrayList
     */
    IntArrayList getAllInEdgeDegrees();

    /**
     * Get out edge degrees of all the vertices
     * 
     * @return IntArrayList
     */
    IntArrayList getAllOutEdgeDegrees();

    /**
     * Get number of edges in the graph
     * 
     * @return double
     */
    double getNumberOfEdges();

    /**
     * Get number of nodes in the graph
     * 
     * @return double
     */
    double getNumberOfVertices();

    /**
     * Add given edge to the graph
     * 
     * @param tailId
     * @param headId
     * @param edgeColour
     * @return int - the edgeId of newly added edge
     */
    int addEdge(int tailId, int headId, BitSet edgeColour);

    /**
     * Remove edge from the graph
     * 
     * @param edgeId
     */
    void removeEdge(int edgeId);

    /**
     * Get Set of all edges in the graph
     * 
     * @return IntSet
     */
    IntSet getEdges();

    /**
     * Get edge color
     * 
     * @param edgeId
     * @return BitSet
     */
    BitSet getEdgeColour(int edgeId);

    /**
     * Get property color
     * 
     * @return Object
     */
    Object getRDFTypePropertyColour();

    /**
     * Get the vertex id of the tail if the edge
     * 
     * @param edgeId
     * @return int
     */
    int getTailOfTheEdge(int edgeId);

    /**
     * Get the vertex id of the head if the edge
     * 
     * @param edgeId
     * @return int
     */
    int getHeadOfTheEdge(int edgeId);

    /**
     * Get list of all vertex IDs connecting to the the edgeId
     *
     * @param edgeId - the id of an edge connecting 1-2 vertices together
     * @return set of vertex ID's
     */
    IntSet getVerticesIncidentToEdge(int edgeId);

    /**
     * Get set of all in neighbors to a vertex
     * 
     * @param vertexId
     * @return set of all in neighbors
     */
    IntSet getInNeighbors(int vertexId);

    /**
     * Get set of all out neighbors to a vertex
     * 
     * @param vertexId
     * @return set of all out neighbors
     */
    IntSet getOutNeighbors(int vertexId);

    /**
     * Get set of all vertices
     * 
     * @return set of all vertices
     */
    IntSet getVertices();

    /**
     * Get underlying {@link Grph} object
     * 
     * @return Grph - graph object
     */
    Grph getGraph();

    /**
     * Get diameter length
     * 
     * @return length of diameter
     */
    double getDiameter();

    /**
     * Get Vertex Color
     * 
     * @param vId - vertex Id
     * @return BitSet - color of vertex
     */
    BitSet getVertexColour(int vId);

    /**
     * Get number of edges between the given two vertices
     * 
     * @param headId
     * @param tailId
     * @return int - number of edges which will be used in triangle metrics
     *         computation
     */
    int getNumberOfEdgesBetweenVertices(int headId, int tailId);

    /**
     * Copy {@link ColouredGraph} object
     * 
     * @return - copy of {@link ColouredGraph}
     */
    ColouredGraph copy();

    /**
     * Get vertex color palette
     * 
     * @return {@link ColourPalette}
     */
    ColourPalette getVertexPalette();

    /**
     * Get edge color palette
     * 
     * @return {@link ColourPalette}
     */
    ColourPalette getEdgePalette();

    /**
     * Get the nodes and edges forming the diameter.
     * 
     * @return ArrayListPath - node and edge information of the diameter path.
     */
    ArrayListPath getDiameterPath();

    /**
     * Get all neighbors of all the nodes in the given direction.
     * 
     * @param direction - Direction of edge to consider for neighbors. In-neighbors
     *                  or Out-neighbors depending on the direction.
     * @return int[][] - Two dimension integer array containing all neighbors of all
     *         nodes in the given direction.
     */
    int[][] getNeighbors(DIRECTION direction);

    /**
     * Method to check if addition of selected edge shortens the diameter.
     * 
     * @param triple - triple that is to be added
     * @param path   - existing diameter path
     * @return int - new diameter length
     */
    int computeShorterDiameter(TripleBaseSingleID triple, ArrayListPath oldPath);
}
