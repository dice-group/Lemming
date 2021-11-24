/**
 * 
 */
package org.aksw.simba.lemming;

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
    public IntSet getEdgesIncidentTo(int verticeId) {
        IntSet resultObject = super.getEdgesIncidentTo(verticeId);
        if (this.triple.headId == verticeId && resultObject.contains(this.triple.edgeId)) {
            resultObject.remove(this.triple.edgeId);
        }
        // TODO: Is tailId check also necessary ?
        if (this.triple.tailId == verticeId && resultObject.contains(this.triple.edgeId)) {
            resultObject.add(this.triple.edgeId);
        }

        return resultObject;
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
        return super.getNumberOfEdges() - 1;
    }

    /*
     * @Override public IntSet getVerticesIncidentToEdge(int edgeId) { return
     * this.graph.getVerticesIncidentToEdge(edgeId); }
     */

}
