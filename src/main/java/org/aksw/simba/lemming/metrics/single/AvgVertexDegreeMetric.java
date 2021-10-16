package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * This metric determines the average degree of outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class AvgVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

    public AvgVertexDegreeMetric() {
        super("avgDegree");
    }

    protected AvgVertexDegreeMetric(String name) {
        super(name);
    }

    protected int numOfVertices;

    protected double sum;

    @Override
    public double apply(ColouredGraph graph) {
        return calculateAvg(graph.getGraph().getAllInEdgeDegrees());
    }

    protected double calculateAvg(IntArrayList degrees) {
        sum = 0;
        for (int i = 0; i < degrees.size(); ++i) {
            sum += degrees.getInt(i);
        }
        numOfVertices = degrees.size();
        return sum / degrees.size();
    }

    /**
     * This method can be used after running apply one time, it computes the avgInEdgeDegree after
     * removing/adding an edge.
     * @param change removing edge: change = -1, adding edge: change = +1
     * @param update if need to update the filed sum (sum of all inEdgeDegrees). Else, update=false;
     * @return avgVertexDegree after modifying graph
     */
    public double recompute(int change, boolean update){
        double newAvgVertexDegree = (sum + change)/numOfVertices;
        if(update){
            sum += change;
        }
        return newAvgVertexDegree;
    }

    public int getCachedNumOfVertices(){
        return this.numOfVertices;
    }
    public double getCachedSum(){
        return this.sum;
    }
}
