/**
 * 
 */
package org.aksw.simba.lemming;

import org.aksw.simba.lemming.grph.DiameterAlgorithm;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.apache.commons.lang3.ArrayUtils;

import grph.Grph.DIRECTION;
import grph.path.ArrayListPath;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Concrete Decorator class to simulate the addition of an edge to the
 * IColouredGraph object in the current iteration
 * 
 * @author Pranav
 *
 */
public class AddEdgeDecorator extends AbstractSingleEdgeManipulatingDecorator {
    /**
     * Instance of Diameter algorithm that will run on the decorator handling edge
     * addition
     */
    protected DiameterAlgorithm diameterAlgorithm;

    /**
     * Class Constructor
     * 
     * @param graph            - IColouredGraph object that is to be decorated
     * @param isAddingEdgeFlag - represents if an edge is being added to the
     *                         ColouredGraph. flag is true if edge is being added
     *                         and false if the edge is being removed
     */
    public AddEdgeDecorator(IColouredGraph graph) {
        super(graph);
        diameterAlgorithm = new DiameterAlgorithm();
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
        if (this.triple.tailId == vertexId) {
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
        if (this.triple.headId == vertexId) {
            outDegree++;
        }
        return outDegree;
    }

//    /**
//     * Get max in edge degree of the graph after given edge has been added
//     * 
//     * @return double
//     */
//    @Override
//    public double getMaxInEdgeDegrees() {
//        // We only need to change this value, if the added triple would increase it
//        return Math.max(super.getMaxInEdgeDegrees(), getInEdgeDegree(this.triple.tailId));
//    }
//
//    /**
//     * Get max out edge degree of the graph after given edge has been added
//     * 
//     * @return double
//     */
//    @Override
//    public double getMaxOutEdgeDegrees() {
//        // We only need to change this value, if the added triple would increase it
//        return Math.max(super.getMaxOutEdgeDegrees(), getOutEdgeDegree(this.triple.headId));
//    }

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
        if (this.triple.tailId == vertexId) {
            neighbors.add(this.triple.headId);
        }
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
        if (this.triple.headId == vertexId) {
            neighbors.add(this.triple.tailId);
        }
        return neighbors;
    }

//    /**
//     * Add new neighbor to the set of all neighbors because an edge has now been
//     * added to the vertex
//     * 
//     * @param vertexId
//     * @return IntSet - set of vertexIds of all in neighbors
//     */
//    private IntSet addNeighbor(IntSet neighbors, int vertexId) {
//        if (vertexId == this.triple.tailId && !neighbors.contains(this.triple.headId)) {
//            neighbors.add(this.triple.headId);
//        } else if (vertexId == this.triple.headId && !neighbors.contains(this.triple.tailId)) {
//            neighbors.add(this.triple.tailId);
//        }
//        return neighbors;
//    }

    /**
     * Get number of edges between two vertices after given edge has been added to
     * them
     * 
     * @return int - number of edges
     */
    @Override
    public int getNumberOfEdgesBetweenVertices(int tailId, int headId) {
        int count = super.getNumberOfEdgesBetweenVertices(tailId, headId);
        // Check if the Ids passed as arguments contain the added edge
        if ((tailId == this.triple.tailId || tailId == this.triple.headId)
                && (headId == this.triple.tailId || headId == this.triple.headId)) {
            ++count;
        }
        return count;
    }

    /**
     * Get all neighbors of all nodes in given direction after a pre-selected edge
     * is added to the graph. Used to compute the diameter of given graph
     * 
     * @param direction - Direction of edge to consider for neighbors. In-neighbors
     *                  or Out-neighbors depending on the direction.
     * @return int[][] - Two dimension integer array containing all neighbors of all
     *         nodes in the given direction.
     */
    @Override
    public int[][] getNeighbors(DIRECTION direction) {
        int[][] neighbors = super.getNeighbors(direction);
        // FIXME This could cause a problem if there is more than one edge between these
        // nodes
        if (direction == DIRECTION.in || direction == DIRECTION.in_out) {
            neighbors[triple.tailId] = addElementIfNotInside(neighbors[triple.tailId], triple.headId);
        }
        if (direction == DIRECTION.out || direction == DIRECTION.in_out) {
            neighbors[triple.headId] = addElementIfNotInside(neighbors[triple.headId], triple.tailId);
        }
        return neighbors;
    }

    private int[] addElementIfNotInside(int[] array, int element) {
        boolean searching = true;
        for (int i = 0; i < array.length && searching; ++i) {
            searching = array[i] != element;
        }
        // If the element has not been found
        if (searching) {
            return ArrayUtils.add(array, element);
        } else {
            return array;
        }
    }

    @Override
    public int computeShorterDiameter(TripleBaseSingleID triple, ArrayListPath path) {
        return diameterAlgorithm.computeShorterDiameter(this, triple, path);
    }

//    @Override
//    public double getDiameter() {
//        return this.diameterAlgorithm.performSearch(this, this.getVertices());
//    }

    @Override
    public ArrayListPath getDiameterPath() {
        this.diameterAlgorithm.performSearch(this, this.getVertices());
        return this.diameterAlgorithm.getDiameterPath();
    }

}
