package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import grph.Grph.DIRECTION;

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

    public MaxVertexDegreeMetric(DIRECTION direction) {
        super(direction == DIRECTION.in ? "maxInDegree" : "maxOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {

        if (direction == DIRECTION.in) {
            maxInDegree = graph.getGraph().getMaxInEdgeDegrees();
            return this.maxInDegree;
        } else {
            maxOutDegree = graph.getGraph().getMaxOutEdgeDegrees();
            return this.maxOutDegree;
        }
    }

    /**
     * This method can be used after running apply one time, it computes the maxInEdge and maxOutEdge after
     * removing/adding an edge.
     * @param graph an instance of ColouredGraph from where an edge should be removed or added.
     * @param vertex it is a vertexId of the removed/added edge.
     *               If DIRECTION.in: vertexId = headId, else vertexId = tailId.
     * @param change removing edge: change = -1, adding edge: change = +1
     * @param update if need to update the field maxInDegree/maxOutDegree update=true. Else, update=false;
     * @return maxInEdge/maxOutEdge degree of the given modified graph
     */
    public double recompute(ColouredGraph graph, int vertex, int change, boolean update){
        if(direction == DIRECTION.in){
            //note: the given graph has been already modified
            int changedDegree = graph.getGraph().getInEdgeDegree(vertex);
            int oriDegree = changedDegree - change;
            int newMaxInDegree = maxInDegree;
            if(change == -1){
                if(oriDegree==maxInDegree){
                    newMaxInDegree = graph.getGraph().getMaxInEdgeDegrees();
                }
            }else{
                if(oriDegree==maxInDegree){
                    newMaxInDegree = changedDegree;
                }
            }
            if(update){
                maxInDegree = newMaxInDegree;
            }
            return newMaxInDegree;
        }else{
            int changedDegree = graph.getGraph().getOutEdgeDegree(vertex);
            int oriDegree = changedDegree - change;
            int newMaxOutDegree = maxOutDegree;
            if(change == -1){
                if(oriDegree==maxOutDegree){
                    newMaxOutDegree = graph.getGraph().getMaxOutEdgeDegrees();
                }
            }else{
                if(oriDegree==maxOutDegree){
                    newMaxOutDegree = changedDegree;
                }
            }
            if(update){
                maxOutDegree = newMaxOutDegree;
            }
            return newMaxOutDegree;
        }
    }

    public double getCachedMaximumInDegree(){
        return this.maxInDegree;
    }

    public double getCachedMaximumOutDegree(){
        return this.maxOutDegree;
    }
}
