package org.aksw.simba.lemming.metrics.single;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import grph.Grph.DIRECTION;

import java.util.HashMap;
import java.util.Map;

/**
 * This metric is the highest degree of in or outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MaxVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

    protected DIRECTION direction;
    protected int maxInDegree = -1;
    protected int maxOutDegree = -1;
    /**
     * A map that maps edge degree to the number of vertices which have the corresponding edge degree
     */
    protected Map<Integer, Integer> inDegreeToNum = null;
    protected Map<Integer, Integer> outDegreeToNum = null;

    public MaxVertexDegreeMetric(DIRECTION direction) {
        super(direction == DIRECTION.in ? "maxInDegree" : "maxOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {

        if (direction == DIRECTION.in) {
            return computeMaxInEdgeDegree(graph);
        } else {
            return computeMaxOutEdgeDegree(graph);
        }
    }

    /**
     * This method can be used after running apply one time, it computes the maxInEdge and maxOutEdge after
     * removing/adding an edge.
     * @param graph an instance of ColouredGraph from where an edge should be removed or added.
     * @param vertex it is a vertexId of the removed/added edge.
     *               If DIRECTION.in: vertexId = headId, else vertexId = tailId.
     * @param change removing edge: change = -1, adding edge: change = +1
     * @param update if need to update map inDegreeToNum/outDegreeToNum and field maxInDegree/maxOutDegree,
     *               update=true. Else, update=false;
     * @return maxInEdge/maxOutEdge degree of the given modified graph
     */
    public double recompute(ColouredGraph graph, int vertex, int change, boolean update){
        if(update){
            if (direction == DIRECTION.in) {
                return recomputeMaxInEdgeDegreeWithUpdate(graph, vertex, change);
            } else {
                return recomputeMaxOutEdgeDegreeWithUpdate(graph, vertex, change);
            }
        }else{
            if (direction == DIRECTION.in) {
                return recomputeMaxInEdgeDegree(graph, vertex, change);
            } else {
                return recomputeMaxOutEdgeDegree(graph, vertex, change);
            }
        }
    }

    /**
     * Compute maximum in edge degree, and create map inDegreeToNum
     */
    private double computeMaxInEdgeDegree(ColouredGraph graph){
        IntSet vertices = graph.getGraph().getVertices();
        inDegreeToNum = new HashMap<>();
        int maxIn = -1;
        for(int vertex : vertices){
            int degree = graph.getGraph().getInEdgeDegree(vertex);
            if(inDegreeToNum.containsKey(degree)){
                inDegreeToNum.replace(degree, inDegreeToNum.get(degree) + 1);
            }else{
                inDegreeToNum.put(degree, 1);
            }
            if(degree > maxIn){
                maxIn = degree;
            }
        }
        maxInDegree = maxIn;
        return maxIn;
    }

    /**
     * Compute maximum out edge degree, and create map outDegreeToNum
     */
    private double computeMaxOutEdgeDegree(ColouredGraph graph){
        IntSet vertices = graph.getGraph().getVertices();
        outDegreeToNum = new HashMap<>();
        int maxOut = -1;
        for(int vertice : vertices){
            int degree = graph.getGraph().getOutEdgeDegree(vertice);
            if(outDegreeToNum.containsKey(degree)){
                outDegreeToNum.replace(degree, outDegreeToNum.get(degree) + 1);
            }else{
                outDegreeToNum.put(degree, 1);
            }
            if(degree > maxOut){
                maxOut = degree;
            }
        }
        maxOutDegree = maxOut;
        return maxOut;
    }


    /**
     * recompute maximum in edge degree
     * @param head  headId of removed/added edge
     * @param change removing edge: -1, adding edge: +1
     */
    private double recomputeMaxInEdgeDegree(ColouredGraph graph, int head, int change){
        //note: the given graph has been already modified
        int changedDegree = graph.getGraph().getInEdgeDegree(head);
        int degree = changedDegree - change;
        if(change == -1){
            if(degree==maxInDegree && inDegreeToNum.get(degree)==1){
               return changedDegree;
            }
        }else{
            if(degree==maxInDegree){
               return changedDegree;
            }
        }
        return maxInDegree;
    }

    /**
     * compute maximum out edge degree
     * @param tail  tailId of removed/added edge
     * @param change removing edge: -1, adding edge: +1
     */
    private double recomputeMaxOutEdgeDegree(ColouredGraph graph, int tail, int change){
        //note: the given graph has been already modified
        int changedDegree = graph.getGraph().getOutEdgeDegree(tail);
        int degree = changedDegree - change;
        if(change == -1){
            if(degree==maxOutDegree && outDegreeToNum.get(degree)==1){
                return changedDegree;
            }
        }else{
            if(degree==maxOutDegree){
                return changedDegree;
            }
        }
        return maxOutDegree;
    }


    /**
     * compute maximum in edge degree and update map inDegreeToNum and field maxInDegree after removing or adding an edge
     * @param head  headId of removed/added edge
     * @param change removing edge: -1, adding edge: +1
     */
    private double recomputeMaxInEdgeDegreeWithUpdate(ColouredGraph graph, int head, int change){
        //note: the given graph has been already modified
        int changedDegree = graph.getGraph().getInEdgeDegree(head);
        int degree = changedDegree - change;
        if(change == -1){
            if(degree==maxInDegree && inDegreeToNum.get(degree)==1){
                maxInDegree = changedDegree;
            }
        }else{
            if(degree==maxInDegree){
                maxInDegree = changedDegree;
            }
        }
        updateInDegreeMap(degree, changedDegree);
        return maxInDegree;
    }

    /**
     * compute maximum out edge degree and update map outDegreeToNum and field maxOutDegree after removing or adding an edge
     * @param tail  tailId of removed/added edge
     * @param change removing edge: -1, adding edge: +1
     */
    private double recomputeMaxOutEdgeDegreeWithUpdate(ColouredGraph graph, int tail, int change){
        //note: the given graph has been already modified
        int changedDegree = graph.getGraph().getOutEdgeDegree(tail);
        int degree = changedDegree - change;
        if(change == -1){
            if(degree==maxOutDegree && outDegreeToNum.get(degree)==1){
                maxOutDegree = changedDegree;
            }
        }else{
            if(degree==maxOutDegree){
                maxOutDegree = changedDegree;
            }
        }
        updateOutDegreeMap(degree, changedDegree);
        return this.maxOutDegree;
    }

    /**
     * update map inDegreeToNum after removing or adding an edge
     * @param degree the original degree for a vertex
     * @param changedDegree the modified degree for a vertex after removing or adding an edge
     */
    private void updateInDegreeMap(int degree, int changedDegree){
        if(!inDegreeToNum.containsKey(degree)){
            throw new RuntimeException("The given degree doesn't exist!");
        }
        int o = inDegreeToNum.get(degree);
        if(o==1){
            inDegreeToNum.remove(degree);
        }else {
            inDegreeToNum.replace(degree, o-1);
        }
        if(inDegreeToNum.containsKey(changedDegree)){
            int n = inDegreeToNum.get(changedDegree);
            inDegreeToNum.replace(changedDegree, n+1);
        }else{
            inDegreeToNum.put(changedDegree, 1);
        }
    }

    /**
     * update map outDegreeToNum after removing or adding an edge
     * @param degree the original degree for a vertex
     * @param changedDegree the modified degree for a vertex after removing or adding an edge
     */
    private void updateOutDegreeMap(int degree, int changedDegree){
        if(!outDegreeToNum.containsKey(degree)){
            throw new RuntimeException("The given degree doesn't exist!");
        }
        int o = outDegreeToNum.get(degree);
        if(o==1){
            outDegreeToNum.remove(degree);
        }else {
            outDegreeToNum.replace(degree, o-1);
        }
        if(outDegreeToNum.containsKey(changedDegree)){
            int n = outDegreeToNum.get(changedDegree);
            outDegreeToNum.replace(changedDegree, n+1);
        }else{
            outDegreeToNum.put(changedDegree, 1);
        }
    }

    public Map<Integer, Integer> getInDegreeToNum(){
        return this.inDegreeToNum;
    }

    public Map<Integer, Integer> getOutDegreeToNum(){
        return this.outDegreeToNum;
    }
}
