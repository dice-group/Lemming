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
    protected int maxDegree = -1;

    public MaxVertexDegreeMetric(DIRECTION direction) {
        super(direction == DIRECTION.in ? "maxInDegree" : "maxOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {

        if (direction == DIRECTION.in) {
            maxDegree = graph.getGraph().getMaxInEdgeDegrees();
            return this.maxDegree;
        } else {
            maxDegree = graph.getGraph().getMaxOutEdgeDegrees();
            return this.maxDegree;
        }
    }

    /**
     * This method can be used after running apply one time, it computes the maxInEdge and maxOutEdge after
     * removing/adding an edge.
     * @param graph an instance of ColouredGraph from where an edge should be removed or added.
     * @param vertex it is a vertexId of the removed/added edge.
     *               If DIRECTION.in: vertexId = headId, else vertexId = tailId.
     * @param change removing edge: change = -1, adding edge: change = +1
     * @param update if need to update the field maxDegree update=true. Else, update=false;
     * @return maxInEdge/maxOutEdge degree of the given modified graph
     */
    public double recompute(ColouredGraph graph, int vertex, int change, boolean update){

        int changedDegree, oriDegree;
        if(direction == DIRECTION.in) {
            //note: the given graph has been already modified
            changedDegree = graph.getGraph().getInEdgeDegree(vertex);
        }else {
            changedDegree = graph.getGraph().getOutEdgeDegree(vertex);
        }
        oriDegree = changedDegree - change;
        int newMaxDegree = maxDegree;
        if(change == -1) {
            if(oriDegree==maxDegree) {
                if(direction == DIRECTION.in) {
                    newMaxDegree = graph.getGraph().getMaxInEdgeDegrees();
                }else {
                    newMaxDegree = graph.getGraph().getMaxOutEdgeDegrees();
                }
            }
        }else {
            if(oriDegree==maxDegree) {
                newMaxDegree = changedDegree;
            }
        }
        if(update){
            maxDegree = newMaxDegree;
        }
        return newMaxDegree;
    }

    public double getCachedMaximumDegree(){
        return this.maxDegree;
    }
}
