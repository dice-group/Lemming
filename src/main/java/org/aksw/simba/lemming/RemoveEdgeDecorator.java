/**
 * 
 */
package org.aksw.simba.lemming;

import org.aksw.simba.lemming.grph.DiameterAlgorithm;
import org.apache.commons.lang3.ArrayUtils;

import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * @author Pranav
 *
 */
public class RemoveEdgeDecorator extends ColouredGraphDecorator {

    /**
     * 
     */
    public RemoveEdgeDecorator() {
        super();
    }

    /**
     * @param graph
     * @param isAddingEdgeFlag
     */
    public RemoveEdgeDecorator(IColouredGraph graph, boolean isAddingEdgeFlag) {
        super(graph, isAddingEdgeFlag);
    }

    @Override
    public int getInEdgeDegree(int vertexId) {
        int inDegree = super.getInEdgeDegree(vertexId);
        if (this.triple.headId == vertexId) {
            inDegree--;
        }
        return inDegree;
    }

    @Override
    public int getOutEdgeDegree(int vertexId) {
        int outDegree = super.getOutEdgeDegree(vertexId);
        if (this.triple.tailId == vertexId) {
            outDegree--;
        }
        return outDegree;
    }

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

    @Override
    public IntArrayList getAllInEdgeDegrees() {
        int[] vertices = super.getVertices().toIntArray();
        IntArrayList inDegrees = new IntArrayList();
        for (int i = 0; i < vertices.length; i++) {
            inDegrees.add(i, getInEdgeDegree(vertices[i]));
        }
        return inDegrees;
    }

    @Override
    public IntArrayList getAllOutEdgeDegrees() {
        int[] vertices = super.getVertices().toIntArray();
        IntArrayList outDegrees = new IntArrayList();
        for (int i = 0; i < vertices.length; i++) {
            outDegrees.add(i, getOutEdgeDegree(vertices[i]));
        }
        return outDegrees;
    }

    @Override
    public double getNumberOfEdges() {
        return super.getNumberOfEdges() - 1;
    }

    @Override
    public IntSet getInNeighbors(int vertexId) {
        IntSet neighbors = super.getInNeighbors(vertexId);
        neighbors = removeNeighbor(neighbors, vertexId);
        return neighbors;
    }

    @Override
    public IntSet getOutNeighbors(int vertexId) {
        IntSet neighbors = super.getOutNeighbors(vertexId);
        neighbors = removeNeighbor(neighbors, vertexId);
        return neighbors;
    }

    private IntSet removeNeighbor(IntSet neighbors, int vertexId) {
        if (vertexId == this.triple.tailId && neighbors.contains(this.triple.headId)) {
            neighbors.remove(this.triple.headId);
        } else if (vertexId == this.triple.headId && neighbors.contains(this.triple.tailId)) {
            neighbors.remove(this.triple.tailId);
        }
        return neighbors;
    }

    public int getNumberOfEdgesBetweenVertices() {
        return super.getNumberOfEdgesBetweenVertices() - 1;
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
        neighbors[triple.tailId] = ArrayUtils.removeElements(neighbors[triple.tailId], triple.headId);
        return neighbors;
    }
    
    
    @Override
    public int getDiameter() {
        DiameterAlgorithm diameterAlgorithm = new DiameterAlgorithm();
        return diameterAlgorithm.performSearch(this, this.getVertices());
    }

}
