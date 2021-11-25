/**
 * 
 */
package org.aksw.simba.lemming;

import org.aksw.simba.lemming.util.IntSetUtil;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * @author Pranav
 *
 */
public class AddEdgeDecorator extends ColouredGraphDecorator {

    /**
     * 
     */
    public AddEdgeDecorator() {
        super();
    }

    /**
     * @param graph
     * @param isAddingEdgeFlag
     */
    public AddEdgeDecorator(IColouredGraph graph, boolean isAddingEdgeFlag) {
        super(graph, isAddingEdgeFlag);
    }

    @Override
    public int getInEdgeDegree(int vertexId) {
        int inDegree = super.getInEdgeDegree(vertexId);
        if (this.triple.headId == vertexId) {
            inDegree++;
        }
        return inDegree;
    }

    @Override
    public int getOutEdgeDegree(int vertexId) {
        int outDegree = super.getOutEdgeDegree(vertexId);
        if (this.triple.tailId == vertexId) {
            outDegree++;
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
        IntArrayList vertices = (IntArrayList) super.getVertices();
        IntArrayList inDegrees = new IntArrayList();
        for (int i = 0; i < vertices.size(); i++) {
            inDegrees.add(i, getInEdgeDegree(vertices.getInt(i)));
        }
        return inDegrees;
    }

    @Override
    public IntArrayList getAllOutEdgeDegrees() {
        IntArrayList vertices = (IntArrayList) super.getVertices();
        IntArrayList outDegrees = new IntArrayList();
        for (int i = 0; i < vertices.size(); i++) {
            outDegrees.add(i, getOutEdgeDegree(vertices.getInt(i)));
        }
        return outDegrees;
    }

    @Override
    public double getNumberOfEdges() {
        return super.getNumberOfEdges() + 1;
    }

    @Override
    public IntSet getInNeighbors(int vertexId) {
        IntSet neighbors = super.getInNeighbors(vertexId);
        neighbors = addNeighbor(neighbors, vertexId);
        return neighbors;
    }

    @Override
    public IntSet getOutNeighbors(int vertexId) {
        IntSet neighbors = super.getOutNeighbors(vertexId);
        neighbors = addNeighbor(neighbors, vertexId);
        return neighbors;
    }

    private IntSet addNeighbor(IntSet neighbors, int vertexId) {
        if (vertexId == this.triple.tailId && !neighbors.contains(this.triple.headId)) {
            neighbors.add(this.triple.headId);
        } else if (vertexId == this.triple.headId && !neighbors.contains(this.triple.tailId)) {
            neighbors.add(this.triple.tailId);
        }
        return neighbors;
    }

    public int getNumberOfEdgesBetweenVertices() {
        return super.getNumberOfEdgesBetweenVertices() + 1;
    }
}
