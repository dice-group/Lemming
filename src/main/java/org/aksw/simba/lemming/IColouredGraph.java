/**
 * 
 */
package org.aksw.simba.lemming;

import it.unimi.dsi.fastutil.ints.IntArrayList;

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
    // IntSet getEdgesIncidentTo(int verticeId);

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
     * Get list of all vertex IDs connecting to the the edgeId
     *
     * @param edgeId the id of an edge connecting 1-2 vertices together
     * @return set of vertex ID's
     */
    // IntSet getVerticesIncidentToEdge(int edgeId);

}
