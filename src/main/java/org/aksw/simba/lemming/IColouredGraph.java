/**
 * 
 */
package org.aksw.simba.lemming;

import com.carrotsearch.hppc.BitSet;

import grph.path.ArrayListPath;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This interface is used for the decorator pattern to simplify multithreading
 * in the graph amendment phase
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
     * @return int -
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
     * Get edge colour
     * 
     * @param edgeId
     * @return BitSet
     */
    BitSet getEdgeColour(int edgeId);

    /**
     * Get property colour
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

    
    ArrayListPath getNodesInDiameter();

    int getDiameter();

    int getDiameterFromVertex(int source);

}
