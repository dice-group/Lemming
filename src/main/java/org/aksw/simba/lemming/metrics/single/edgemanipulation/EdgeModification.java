package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import com.carrotsearch.hppc.BitSet;
import grph.Grph;
import it.unimi.dsi.fastutil.ints.IntSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author DANISH AHMED on 8/10/2018
 */
public class EdgeModification {
    private ColouredGraph graph;
    private SingleValueMetric nodeMetric;
    private SingleValueMetric edgeMetric;
    private int oldNodeTriangles = 0;
    private int newNodeTriangles;
    private int oldEdgeTriangles = 0;
    private int newEdgeTriangles;

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeModification.class);

    public EdgeModification(ColouredGraph graph, SingleValueMetric nodeMetric, SingleValueMetric edgeMetric) {
        this.graph = graph;
        this.nodeMetric = nodeMetric;
        this.edgeMetric = edgeMetric;

        this.oldNodeTriangles = (int) getNumberOfNodeTriangles();
        this.oldEdgeTriangles = (int) getNumberOfEdgeTriangles();
    }

    public EdgeModification(ColouredGraph graph, int numberOfNodeTriangles, int numberOfEdgeTriangles) {
        this.graph = graph;

        this.oldNodeTriangles = numberOfNodeTriangles;
        this.oldEdgeTriangles = numberOfEdgeTriangles;
    }

    public ColouredGraph getGraph() {
        return graph;
    }

    public int getOldNodeTriangles() {
        return oldNodeTriangles;
    }

    public int getOldEdgeTriangles() {
        return oldEdgeTriangles;
    }

    public int getNewNodeTriangles() {
        return newNodeTriangles;
    }

    public int getNewEdgeTriangles() {
        return newEdgeTriangles;
    }

    public void setNodeMetric(SingleValueMetric nodeMetric) {
        this.nodeMetric = nodeMetric;
    }

    public void setEdgeMetric(SingleValueMetric edgeMetric) {
        this.edgeMetric = edgeMetric;
    }

    public void setGraph(ColouredGraph graph) {
        this.graph = graph;
    }

    private double getNumberOfNodeTriangles() {
        return nodeMetric.apply(graph);
    }

    private double getNumberOfEdgeTriangles() {
        return edgeMetric.apply(graph);
    }

    void removeEdgeFromGraph(int edgeId) {
        if (newNodeTriangles != 0 && newEdgeTriangles != 0) {
            oldNodeTriangles = newNodeTriangles;
            oldEdgeTriangles = newEdgeTriangles;
        }

        Grph grph = graph.getGraph();

        IntSet verticesConnectedToRemovingEdge = grph.getVerticesIncidentToEdge(edgeId);
        int headId = verticesConnectedToRemovingEdge.size() > 1 ? verticesConnectedToRemovingEdge.toIntArray()[1] 
        		: verticesConnectedToRemovingEdge.toIntArray()[0];
        int tailId = verticesConnectedToRemovingEdge.toIntArray()[0];

        //it's possible: headId = tailId. The oldNodeTriangles and oldEdgeTriangles don't change.
        if(headId == tailId){
           newNodeTriangles = oldNodeTriangles;
           newEdgeTriangles = oldEdgeTriangles;
        }else{
           int numEdgesBetweenConnectedVertices = IntSetUtil.intersection(grph.getEdgesIncidentTo(tailId),
                    grph.getEdgesIncidentTo(headId)).size();

           if (numEdgesBetweenConnectedVertices > 1) {
               //calculate newNodeTriangles
               /* Same number of node triangles */
               newNodeTriangles = oldNodeTriangles;

               //calculate newEdgeTriangles
               int differenceOfSubGraphEdgeTriangles = calculateDifferenceOfSubGraphEdgeTriangles(headId, tailId, numEdgesBetweenConnectedVertices, -1);
               newEdgeTriangles = oldEdgeTriangles - differenceOfSubGraphEdgeTriangles;
               newEdgeTriangles = newEdgeTriangles >= 0 ? newEdgeTriangles: 0;

           } else {
               /* edge size = 1 */
               //calculate newNodeTriangles
               /* remove number of triangles formed by subgraph*/
               IntSet commonVertices = getVerticesInCommon(headId, tailId);
               int oldSubGraphNodeTriangles = commonVertices.size();
               newNodeTriangles = oldNodeTriangles - oldSubGraphNodeTriangles;
               newNodeTriangles = newNodeTriangles >= 0 ? newNodeTriangles: 0;

               //calculate newEdgeTriangles
               int numSubGraphEdgeTriangles = calculateSubGraphEdgeTriangles(headId, tailId, commonVertices);
               newEdgeTriangles = oldEdgeTriangles - numSubGraphEdgeTriangles;
               newEdgeTriangles = newEdgeTriangles >= 0 ? newEdgeTriangles: 0;
           }
        }

        this.graph.removeEdge(edgeId);
    }

    int addEdgeToGraph(int tail, int head, BitSet color) {
        if (newNodeTriangles != 0 && newEdgeTriangles != 0) {
            oldNodeTriangles = newNodeTriangles;
            oldEdgeTriangles = newEdgeTriangles;
        }

        Grph grph = graph.getGraph();

        //it's possible: headId = tailId. The oldNodeTriangles and oldEdgeTriangles don't change.
        if(head == tail){
            newNodeTriangles = oldNodeTriangles;
            newEdgeTriangles = oldEdgeTriangles;
        }else {
            int numEdgesBetweenVertices = IntSetUtil.intersection(grph.getEdgesIncidentTo(tail),
                    grph.getEdgesIncidentTo(head)).size();

            if (numEdgesBetweenVertices > 0) {
                // number of Node Triangles remains same
                newNodeTriangles = oldNodeTriangles;

                int differenceOfSubGraphEdgeTriangles = calculateDifferenceOfSubGraphEdgeTriangles(head, tail, numEdgesBetweenVertices, 1);
                newEdgeTriangles = oldEdgeTriangles + differenceOfSubGraphEdgeTriangles;

            } else {
                // no connection between vertices
                IntSet verticesInCommon = getVerticesInCommon(tail, head);
                newNodeTriangles = oldNodeTriangles + verticesInCommon.size();

                int subGraphEdgeTriangles = calculateSubGraphEdgeTriangles(head, tail, verticesInCommon);
                newEdgeTriangles = oldEdgeTriangles + subGraphEdgeTriangles;
            }
        }
        return graph.addEdge(tail, head, color);

    }

    private IntSet getVerticesInCommon(int v1, int v2) {
        Grph grph = graph.getGraph();
        IntSet[] neighborsOfConnectedVertices = new IntSet[2];

        neighborsOfConnectedVertices[0] = grph.getInNeighbors(v1);
        neighborsOfConnectedVertices[0].addAll(grph.getOutNeighbors(v1));

        if (neighborsOfConnectedVertices[0].contains(v1))
            neighborsOfConnectedVertices[0].remove(v1);
        if (neighborsOfConnectedVertices[0].contains(v2))
            neighborsOfConnectedVertices[0].remove(v2);

        neighborsOfConnectedVertices[1] = grph.getInNeighbors(v2);
        neighborsOfConnectedVertices[1].addAll(grph.getOutNeighbors(v2));

        if (neighborsOfConnectedVertices[1].contains(v1))
            neighborsOfConnectedVertices[1].remove(v1);
        if (neighborsOfConnectedVertices[1].contains(v2))
            neighborsOfConnectedVertices[1].remove(v2);

        return IntSetUtil.intersection(neighborsOfConnectedVertices[0], neighborsOfConnectedVertices[1]);
    }

    private int calculateSubGraphEdgeTriangles(int headId, int tailId, IntSet commonVertices) {
        int subGraphEdgeTriangles = 0;

        Grph grph = graph.getGraph();
        
        for (int vertex : commonVertices) {
            int numEdgesV1ToTriangleVertex = IntSetUtil.intersection(grph.getEdgesIncidentTo(tailId),
            		grph.getEdgesIncidentTo(vertex)).size();
            int numEdgesV2ToTriangleVertex = IntSetUtil.intersection(grph.getEdgesIncidentTo(headId),
                    grph.getEdgesIncidentTo(vertex)).size();
            int mul = numEdgesV1ToTriangleVertex * numEdgesV2ToTriangleVertex;
            subGraphEdgeTriangles += mul;
        }
        return subGraphEdgeTriangles;
    }

    /**
     * It is used to calculate the difference of edge triangles in subgraph after removing or adding an edge
     * @param headId head of the to modified edge
     * @param tailId tail of the to modified edge
     * @param numEdgesBetweenConnectedVertices number of the edges between head and tail
     * @param change removing an edge, then -1, add an edge, then +1
     * @return the difference of edge triangles in subgraph after removing or adding an edge
     */
    private int calculateDifferenceOfSubGraphEdgeTriangles(int headId, int tailId, int numEdgesBetweenConnectedVertices, int change) {
        int oldSubGraphEdgeTriangles = 0;
        int subGraphTrianglesAfterRemovingEdge = 0;
        int numAfterRemovingEdge = numEdgesBetweenConnectedVertices + change;

        Grph grph = graph.getGraph();

        for (int vertex : getVerticesInCommon(tailId, headId)) {
            int numEdgesV1ToTriangleVertex = IntSetUtil.intersection(grph.getEdgesIncidentTo(tailId),
                    grph.getEdgesIncidentTo(vertex)).size();
            int numEdgesV2ToTriangleVertex = IntSetUtil.intersection(grph.getEdgesIncidentTo(headId),
                    grph.getEdgesIncidentTo(vertex)).size();
            int mul = numEdgesV1ToTriangleVertex * numEdgesV2ToTriangleVertex;
            oldSubGraphEdgeTriangles += (mul * numEdgesBetweenConnectedVertices);
            subGraphTrianglesAfterRemovingEdge += (mul * numAfterRemovingEdge);
        }

        return oldSubGraphEdgeTriangles-subGraphTrianglesAfterRemovingEdge;
    }
    
    public void removeEdgeFromGraph(int edgeId, int newNodeTriangles, int newEdgeTriangles) {
        if (this.newNodeTriangles != 0 && this.newEdgeTriangles != 0) {
            oldNodeTriangles = this.newNodeTriangles;
            oldEdgeTriangles = this.newEdgeTriangles;
        }
        this.newNodeTriangles = newNodeTriangles;
        this.newEdgeTriangles = newEdgeTriangles;
        this.graph.removeEdge(edgeId);
    }

   public int addEdgeToGraph(int tail, int head, BitSet color, int newNodeTriangles, int newEdgeTriangles) {
        if (this.newNodeTriangles != 0 && this.newEdgeTriangles != 0) {
            oldNodeTriangles = this.newNodeTriangles;
            oldEdgeTriangles = this.newEdgeTriangles;
        }
        this.newNodeTriangles = newNodeTriangles;
        this.newEdgeTriangles = newEdgeTriangles;
        return graph.addEdge(tail, head, color);
    }
}

