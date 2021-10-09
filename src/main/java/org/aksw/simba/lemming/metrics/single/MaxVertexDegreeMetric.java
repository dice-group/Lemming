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
    protected Map<Integer, Integer> inDegreeToNum = null;
    protected Map<Integer, Integer> outDegreeToNums = null;

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

    public double update(ColouredGraph graph, int vertice, int change){
        if (direction == DIRECTION.in) {
            return updateMaxInEdgeDegree(graph, vertice, change);
        } else {
            return updateMaxOutEdgeDegree(graph, vertice, change);
        }
    }

    private double computeMaxInEdgeDegree(ColouredGraph graph){
        IntSet vertices = graph.getGraph().getVertices();
        inDegreeToNum = new HashMap<>();
        int maxIn = -1;
        for(int vertice : vertices){
            int degree = graph.getGraph().getInEdgeDegree(vertice);
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

    private double computeMaxOutEdgeDegree(ColouredGraph graph){
        IntSet vertices = graph.getGraph().getVertices();
        outDegreeToNums = new HashMap<>();
        int maxOut = -1;
        for(int vertice : vertices){
            int degree = graph.getGraph().getOutEdgeDegree(vertice);
            if(outDegreeToNums.containsKey(degree)){
                outDegreeToNums.replace(degree, outDegreeToNums.get(degree) + 1);
            }else{
                outDegreeToNums.put(degree, 1);
            }
            if(degree > maxOut){
                maxOut = degree;
            }
        }
        maxOutDegree = maxOut;
        return maxOut;
    }

    private double updateMaxInEdgeDegree(ColouredGraph graph, int head, int change){
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

    private double updateMaxOutEdgeDegree(ColouredGraph graph, int tail, int change){
        int changedDegree = graph.getGraph().getOutEdgeDegree(tail);
        int degree = changedDegree - change;
        if(change == -1){
            if(degree==maxOutDegree && outDegreeToNums.get(degree)==1){
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

    private void updateOutDegreeMap(int degree, int changedDegree){
        if(!outDegreeToNums.containsKey(degree)){
            throw new RuntimeException("The given degree doesn't exist!");
        }
        int o = outDegreeToNums.get(degree);
        if(o==1){
            outDegreeToNums.remove(degree);
        }else {
            outDegreeToNums.replace(degree, o-1);
        }
        if(outDegreeToNums.containsKey(changedDegree)){
            int n = outDegreeToNums.get(changedDegree);
            outDegreeToNums.replace(changedDegree, n+1);
        }else{
            outDegreeToNums.put(changedDegree, 1);
        }
    }
}
