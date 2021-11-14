/**
 * 
 */
package org.aksw.simba.lemming;

import it.unimi.dsi.fastutil.ints.IntArrayList;

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

    /*
     * @Override public IntSet getEdgesIncidentTo(int verticeId) { IntSet
     * resultObject = super.getEdgesIncidentTo(verticeId); if (this.triple.headId ==
     * verticeId && !resultObject.contains(this.triple.edgeId)) {
     * resultObject.add(this.triple.edgeId); }
     * 
     * TODO: is tailID check also required ??
     * 
     * if (this.triple.tailId == verticeId &&
     * !resultObject.contains(this.triple.edgeId)) {
     * resultObject.add(this.triple.edgeId); }
     * 
     * return resultObject; }
     */

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
        int outDegree = super.getInEdgeDegree(vertexId);
        if (this.triple.tailId == vertexId) {
            outDegree++;
        }
        return outDegree;
    }

    @Override
    public double getMaxInEdgeDegrees() {
        IntArrayList vertices = (IntArrayList) ((ColouredGraph) this.graph).getVertices();
        double maxValue = 0.0;
        for (int i = 0; i < vertices.size(); i++) {
            int nodeDegree = getInEdgeDegree(vertices.getInt(i));
            if (nodeDegree > maxValue) {
                maxValue = nodeDegree;
            }
        }
        return maxValue;
    }

    @Override
    public double getMaxOutEdgeDegrees() {
        IntArrayList vertices = (IntArrayList) ((ColouredGraph) this.graph).getVertices();
        double maxValue = 0.0;
        for (int i = 0; i < vertices.size(); i++) {
            int nodeDegree = getOutEdgeDegree(vertices.getInt(i));
            if (nodeDegree > maxValue) {
                maxValue = nodeDegree;
            }
        }
        return maxValue;
    }

    @Override
    public IntArrayList getAllInEdgeDegrees() {
        IntArrayList vertices = (IntArrayList) ((ColouredGraph) this.graph).getVertices();
        IntArrayList inDegrees = new IntArrayList();
        for (int i = 0; i < vertices.size(); i++) {
            inDegrees.add(i, getInEdgeDegree(vertices.getInt(i)));
        }
        return inDegrees;
    }

    @Override
    public IntArrayList getAllOutEdgeDegrees() {
        IntArrayList vertices = (IntArrayList) ((ColouredGraph) this.graph).getVertices();
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

    /*
     * @Override public IntSet getVerticesIncidentToEdge(int edgeId) { return
     * this.graph.getVerticesIncidentToEdge(edgeId); }
     */
}
