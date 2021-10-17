package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;


import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class StdDevVertexDegree extends AvgVertexDegreeMetric {

    protected DIRECTION direction;
    double sumOfSquare;

    public StdDevVertexDegree(DIRECTION direction) {
        super(direction == DIRECTION.in ? "stdDevInDegree" : "stdDevOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {
        IntArrayList degrees;
        if (direction == DIRECTION.in) {
            degrees = graph.getGraph().getAllInEdgeDegrees();
        } else {
            degrees = graph.getGraph().getAllOutEdgeDegrees();
        }
        return calculateStdDev(degrees, calculateAvg(degrees));
    }

    protected double calculateStdDev(IntArrayList degrees, double avg) {
        for (int i = 0; i < degrees.size(); ++i) {
            sumOfSquare += Math.pow(degrees.getInt(i), 2);
        }
        return Math.sqrt((sumOfSquare / numOfVertices) - Math.pow(avg, 2));
    }

    /**
     * This method can be used after running apply one time, it computes the stdDevInEdgeDegree and stdDevOutEdgeDegree
     * after removing/adding an edge.
     * @param graph an instance of ColouredGraph from where an edge should be removed or added.
     * @param vertex it is a vertexId of the removed/added edge.
     *               If DIRECTION.in: vertexId = headId, else vertexId = tailId.
     * @param change removing edge: change = -1, adding edge: change = +1
     * @param update if need to update the fields sum, sumOfSquare update=true. Else, update=false;
     * @return stdDevInEdgeDegree/stdDevOutEdgeDegree degree of the given modified graph
     */
    public double recompute(ColouredGraph graph, int vertex, int change, boolean update){
        //calculate new mean:
        double newAvg = super.recompute(change, update);
        int changedDegree, oriDegree;

        if (direction == DIRECTION.in) {
            //note: the given graph has been already modified
            changedDegree = graph.getGraph().getInEdgeDegree(vertex);
        }else {
            changedDegree = graph.getGraph().getOutEdgeDegree(vertex);
        }
        oriDegree = changedDegree - change;
        double newSumOfSquare = sumOfSquare - Math.pow(oriDegree, 2) + Math.pow(changedDegree, 2);
        if(update){
            sumOfSquare = newSumOfSquare;
        }
        return Math.sqrt((newSumOfSquare / numOfVertices) - Math.pow(newAvg, 2));
    }

    public double getCachedSumOfSquare(){
        return this.sumOfSquare;
    }
}
