/**
 * 
 */
package org.aksw.simba.lemming;

import org.aksw.simba.lemming.grph.DiameterAlgorithm;
import org.apache.commons.lang3.ArrayUtils;

import grph.Grph.DIRECTION;
import grph.path.ArrayListPath;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * 
 * Concrete Decorator class to simulate the removal of an edge from the
 * IColouredGraph object in the current iteration
 * 
 * @author Pranav
 *
 */
public class RemoveEdgeDecorator extends AbstractSingleEdgeManipulatingDecorator {

    /**
     * Instance of Diameter algorithm that will run on the decorator handling edge
     * removal
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
    public RemoveEdgeDecorator(IColouredGraph graph) {
        super(graph);
    }

    /**
     * Get in edge degree of a vertex after a new edge is removed from it
     *
     * @param verticeId - the id of an vertex
     * @return int - in edge degree value
     */
    @Override
    public int getInEdgeDegree(int vertexId) {
        int inDegree = super.getInEdgeDegree(vertexId);
        if (this.triple.headId == vertexId) {
            inDegree--;
        }
        return inDegree;
    }

    /**
     * Get out edge degree of a vertex after a new edge is removed from it
     *
     * @param verticeId - the id of an vertex
     * @return int - out edge degree value
     */
    @Override
    public int getOutEdgeDegree(int vertexId) {
        int outDegree = super.getOutEdgeDegree(vertexId);
        if (this.triple.tailId == vertexId) {
            outDegree--;
        }
        return outDegree;
    }

//    /**
//     * Get max in edge degree of the graph after given edge has been removed
//     * 
//     * @return double
//     */
//    @Override
//    public double getMaxInEdgeDegrees() {
//        int[] vertices = super.getVertices().toIntArray();
//        double maxValue = 0.0;
//        for (int i = 0; i < vertices.length; i++) {
//            int nodeDegree = getInEdgeDegree(vertices[i]);
//            if (nodeDegree > maxValue) {
//                maxValue = nodeDegree;
//            }
//        }
//        return maxValue;
//    }
//
//    /**
//     * Get max out edge degree of the graph after given edge has been removed
//     * 
//     * @return double
//     */
//    @Override
//    public double getMaxOutEdgeDegrees() {
//        int[] vertices = super.getVertices().toIntArray();
//        double maxValue = 0.0;
//        for (int i = 0; i < vertices.length; i++) {
//            int nodeDegree = getOutEdgeDegree(vertices[i]);
//            if (nodeDegree > maxValue) {
//                maxValue = nodeDegree;
//            }
//        }
//        return maxValue;
//    }

    /**
     * Get number of edges in the graph after removing an edge
     * 
     * @return double - number of edges
     */
    @Override
    public double getNumberOfEdges() {
        return super.getNumberOfEdges() - 1;
    }

    /**
     * Get all in neighbors of a vertex after removing an edge
     * 
     * @param vertexId
     * @return IntSet - set of vertexIds of all in neighbors
     */
    @Override
    public IntSet getInNeighbors(int vertexId) {
        IntSet neighbors = super.getInNeighbors(vertexId);
        // FIXME This could cause a problem if there is more than one edge between these
        // nodes
        if (this.triple.tailId == vertexId) {
            neighbors.remove(this.triple.headId);
        }
        return neighbors;
    }

    /**
     * Get all out neighbors of a vertex after removing an edge
     * 
     * @param vertexId
     * @return IntSet - set of vertexIds of all in neighbors
     */
    @Override
    public IntSet getOutNeighbors(int vertexId) {
        IntSet neighbors = super.getOutNeighbors(vertexId);
        // FIXME This could cause a problem if there is more than one edge between these
        // nodes
        if (this.triple.headId == vertexId) {
            neighbors.remove(this.triple.tailId);
        }
        return neighbors;
    }

//    /**
//     * Remove a neighbor from the set of all neighbors because an edge has now been
//     * removed from the vertex
//     * 
//     * @param vertexId
//     * @return IntSet - set of vertexIds of all in neighbors
//     */
//    private IntSet removeNeighbor(IntSet neighbors, int vertexId) {
//        if (vertexId == this.triple.tailId && neighbors.contains(this.triple.headId)) {
//            neighbors.remove(this.triple.headId);
//        } else if (vertexId == this.triple.headId && neighbors.contains(this.triple.tailId)) {
//            neighbors.remove(this.triple.tailId);
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
            --count;
        }
        return count;
    }

    /**
     * Get all neighbors of all nodes in given direction after a pre-selected edge
     * is removed from the graph. Used to compute the diameter of given graph
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
            neighbors[triple.tailId] = ArrayUtils.removeElements(neighbors[triple.tailId], triple.headId);
        }
        if (direction == DIRECTION.out || direction == DIRECTION.in_out) {
            neighbors[triple.headId] = ArrayUtils.removeElements(neighbors[triple.headId], triple.tailId);
        }
        return neighbors;
    }

//    @Override
//    public double getDiameter() {
//        this.diameterAlgorithm = new DiameterAlgorithm();
//        return this.diameterAlgorithm.performSearch(this, this.getVertices());
//    }

    @Override
    public ArrayListPath getDiameterPath() {
        return this.diameterAlgorithm.getDiameterPath();
    }

}
