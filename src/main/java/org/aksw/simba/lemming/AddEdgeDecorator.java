/**
 * 
 */
package org.aksw.simba.lemming;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * 
 * Concrete Decorator class to simulate the addition of an edge to the
 * IColouredGraph object in the current iteration
 * 
 * @author Pranav
 *
 */
public class AddEdgeDecorator extends ColouredGraphDecorator {

    /**
     * Class Constructor
     * 
     * @param graph            - IColouredGraph object that is to be decorated
     * @param isAddingEdgeFlag - represents if an edge is being added to the
     *                         ColouredGraph. flag is true if edge is being added
     *                         and false if the edge is being removed
     */
    public AddEdgeDecorator(IColouredGraph graph, boolean isAddingEdgeFlag) {
        super(graph, isAddingEdgeFlag);
    }

    /**
     * Get in edge degree of a vertex after a new edge is added to it
     *
     * @param verticeId - the id of an vertex
     * @return int - in edge degree value
     */
    @Override
    public int getInEdgeDegree(int vertexId) {
        int inDegree = super.getInEdgeDegree(vertexId);
        if (this.triple.headId == vertexId) {
            inDegree++;
        }
        return inDegree;
    }

    /**
     * Get out edge degree of a vertex after a new edge is added to it
     *
     * @param verticeId - the id of an vertex
     * @return int - out edge degree value
     */
    @Override
    public int getOutEdgeDegree(int vertexId) {
        int outDegree = super.getOutEdgeDegree(vertexId);
        if (this.triple.tailId == vertexId) {
            outDegree++;
        }
        return outDegree;
    }

    /**
     * Get max in edge degree of the graph after given edge has been added
     * 
     * @return double
     */
    @Override
    public double getMaxInEdgeDegrees() {
        int[] vertices = super.getVertices().toIntArray();
        double maxValue = 0.0;
        for (int i = 0; i < vertices.length; i++) {
            int nodeDegree = getInEdgeDegree(vertices[i]);
            if (nodeDegree > maxValue) {
                maxValue = nodeDegree;
            }
        }
        return maxValue;
    }

    /**
     * Get max out edge degree of the graph after given edge has been added
     * 
     * @return double
     */
    @Override
    public double getMaxOutEdgeDegrees() {
        int[] vertices = super.getVertices().toIntArray();
        double maxValue = 0.0;
        for (int i = 0; i < vertices.length; i++) {
            int nodeDegree = getOutEdgeDegree(vertices[i]);
            if (nodeDegree > maxValue) {
                maxValue = nodeDegree;
            }
        }
        return maxValue;
    }

    /**
     * Get new in edge degrees of all the vertices. All the vertices will have the
     * same in degree except the vertex to which edge has been added
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllInEdgeDegrees() {
        int[] vertices = super.getVertices().toIntArray();
        IntArrayList inDegrees = new IntArrayList();
        for (int i = 0; i < vertices.length; i++) {
            inDegrees.add(i, getInEdgeDegree(vertices[i]));
        }
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
        int[] vertices = super.getVertices().toIntArray();
        IntArrayList outDegrees = new IntArrayList();
        for (int i = 0; i < vertices.length; i++) {
            outDegrees.add(i, getOutEdgeDegree(vertices[i]));
        }
        return outDegrees;
    }

    /**
     * Get number of edges in the graph after adding an edge
     * 
     * @return double - number of edges
     */
    @Override
    public double getNumberOfEdges() {
        return super.getNumberOfEdges() + 1;
    }

    /**
     * Get all in neighbors of a vertex after adding an edge
     * 
     * @param vertexId
     * @return IntSet - set of vertexIds of all in neighbors
     */
    @Override
    public IntSet getInNeighbors(int vertexId) {
        IntSet neighbors = super.getInNeighbors(vertexId);
        neighbors = addNeighbor(neighbors, vertexId);
        return neighbors;
    }

    /**
     * Get all out neighbors of a vertex after adding an edge
     * 
     * @param vertexId
     * @return IntSet - set of vertexIds of all in neighbors
     */
    @Override
    public IntSet getOutNeighbors(int vertexId) {
        IntSet neighbors = super.getOutNeighbors(vertexId);
        neighbors = addNeighbor(neighbors, vertexId);
        return neighbors;
    }

    /**
     * Add new neighbor to the set of all neighbors because an edge has now been
     * added to the vertex
     * 
     * @param vertexId
     * @return IntSet - set of vertexIds of all in neighbors
     */
    private IntSet addNeighbor(IntSet neighbors, int vertexId) {
        if (vertexId == this.triple.tailId && !neighbors.contains(this.triple.headId)) {
            neighbors.add(this.triple.headId);
        } else if (vertexId == this.triple.headId && !neighbors.contains(this.triple.tailId)) {
            neighbors.add(this.triple.tailId);
        }
        return neighbors;
    }

    /**
     * Get number of edges between two vertices after given edge has been added to
     * them
     * 
     * @return int - number of edges
     */
    @Override
    public int getNumberOfEdgesBetweenVertices(int tailId, int headId) {
        // Check if the Ids passed as arguments contain the added edge
        if (tailId == this.triple.tailId || tailId == this.triple.headId && headId == this.triple.tailId
                || headId == this.triple.headId) {
            return super.getNumberOfEdgesBetweenVertices(tailId, headId) + 1;
        } else {
            System.out.println("Somethings Wrong! Input node Ids not part of selected edge");
            return -1;
        }
    }
}
