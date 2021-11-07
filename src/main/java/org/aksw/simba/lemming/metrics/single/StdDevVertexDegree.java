package org.aksw.simba.lemming.metrics.single;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class StdDevVertexDegree extends AvgVertexDegreeMetric {

    protected DIRECTION direction;

    public StdDevVertexDegree(DIRECTION direction) {
        super(direction == DIRECTION.in ? "stdDevInDegree" : "stdDevOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {
        IntArrayList degrees = null;
        if (direction == DIRECTION.in) {
            degrees = graph.getGraph().getAllInEdgeDegrees();
        } else {
            degrees = graph.getGraph().getAllOutEdgeDegrees();
        }
        return calculateStdDev(degrees, calculateAvg(degrees));
    }

    protected double calculateStdDev(IntArrayList degrees, double avg) {
        double temp, sum = 0;
        for (int i = 0; i < degrees.size(); ++i) {
            temp = avg - degrees.getInt(i);
            temp *= temp;
            sum += temp;
        }
        return Math.sqrt(sum / degrees.size());
    }

    /**
     * The method calculates the StdDev of an array of updated degrees. It fetches
     * the previous variance and mean and uses formula described in the link to
     * calculate new StdDev Value. If previously variance, average were not
     * calculated, it will calculate them once
     * 
     * {@link //https://math.stackexchange.com/q/3112715}
     * 
     * @param graph          - input graph.
     * @param triple         - edge on which graph operation is performed.
     * @param graphOperation - boolean value indicating graph operation. ("true" for
     *                       adding an edge and "false" for removing an edge)
     * @param previousResult - UpdatableMetricResult object containing the previous
     *                       computed results.
     * @return UpdatableMetricResult object.
     */
    @Override
    public UpdatableMetricResult update(ColouredGraph graph, TripleBaseSingleID triple, Operation graphOperation,
            UpdatableMetricResult previousResult) {

        StdDevVertexDegreeMetricResult metricResultObj = new StdDevVertexDegreeMetricResult(getName(), 0.0);
        if (previousResult instanceof StdDevVertexDegreeMetricResult) {
            // Copying previously computed values in temporary variables
            metricResultObj.setVarianceVertexDegree(
                    ((StdDevVertexDegreeMetricResult) previousResult).getVarianceVertexDegree());
            metricResultObj.setAvgVertexDegree(((StdDevVertexDegreeMetricResult) previousResult).getAvgVertexDegree());
            metricResultObj
                    .setNumberOfVertices(((StdDevVertexDegreeMetricResult) previousResult).getNumberOfVertices());
        }

        double avg = 0;
        double variance = 0;
        double numberOfVertices = 1;
        double oldDegree = 0;
        List<Double> avgAndVariance;
        if (metricResultObj.getAvgVertexDegree() == 0.0) {
            // no previous result is found
            List<Double> listOfValues = computeValuesForFirstTime(graph);
            // get values of average and variance from the list
            numberOfVertices = listOfValues.get(0);
            avg = listOfValues.get(1);
            variance = listOfValues.get(2);
        } else {
            // get values of average and variance from previous result
            numberOfVertices = metricResultObj.getNumberOfVertices();
            avg = metricResultObj.getAvgVertexDegree();
            variance = metricResultObj.getVarianceVertexDegree();
            oldDegree = (this.direction == DIRECTION.in) ? graph.getGraph().getInEdgeDegree(triple.headId)
                    : graph.getGraph().getOutEdgeDegree(triple.tailId);
            avgAndVariance = computeAvgVarianceFromPreviousResult(numberOfVertices, avg, variance, oldDegree,
                    graphOperation);
            avg = avgAndVariance.get(0);
            variance = avgAndVariance.get(1);
        }

        metricResultObj.setAvgVertexDegree(avg);
        metricResultObj.setVarianceVertexDegree(variance);
        metricResultObj.setNumberOfVertices(numberOfVertices);
        metricResultObj.setResult(Math.sqrt(variance));
        return metricResultObj;
    }

    /**
     * This method calculates the StdDev of an array given the number of vertices
     * and the previous variance and mean are known. It uses the formula mentioned
     * in the link
     * 
     * {@link //https://math.stackexchange.com/q/3112715}
     * 
     * @param numberOfVertices - total number of nodes in the graph
     * @param avg              - average of previous set of nodes in the graph
     * @param variance         - variance of previous set of nodes
     * @param oldDegree        - the degree which was updated after adding or
     *                         removing an edge
     * @param graphOperation   - denotes if an edge was added or removed
     * @return List<Double> - a list containing average and variance in that order.
     */
    private List<Double> computeAvgVarianceFromPreviousResult(double numberOfVertices, double avg, double variance,
            double oldDegree, Operation graphOperation) {
        List<Double> list = new ArrayList<Double>();
        double flag = graphOperation == Operation.ADD ? 1 : -1;
        double newDegree = oldDegree + flag;
        double newAvg = avg + (flag / numberOfVertices);
        double newVariance = (variance + Math.pow(numberOfVertices, -2)
                + (Math.pow((newDegree - newAvg), 2) - Math.pow((oldDegree - newAvg), 2)) / numberOfVertices);
        list.add(newAvg);
        list.add(newVariance);
        return list;
    }

    /**
     * TODO: Check if this can be replaced by apply method (The apply method does
     * not return average or number of nodes)
     * 
     * This method calculates the average and Variance for the first time.
     * 
     * @param graph - the graph object
     * @return List<Double> - a list containing number of nodes, average and
     *         variance in that order.
     */
    private List<Double> computeValuesForFirstTime(ColouredGraph graph) {
        List<Double> listOfValues = new ArrayList<Double>();
        IntArrayList vertexDegrees;

        if (this.direction == DIRECTION.in) {
            vertexDegrees = graph.getGraph().getAllInEdgeDegrees();
        } else {
            vertexDegrees = graph.getGraph().getAllOutEdgeDegrees();
        }

        double averageOfDegrees = super.calculateAvg(vertexDegrees);
        double sumForVariance = 0.0;
        double temp;
        for (Integer vertexDegree : vertexDegrees) {
            temp = averageOfDegrees - vertexDegree;
            temp *= temp;
            sumForVariance += temp;
        }

        double variance = sumForVariance / vertexDegrees.size();

        listOfValues.add((double) vertexDegrees.size());
        listOfValues.add(averageOfDegrees);
        listOfValues.add(variance);

        return listOfValues;
    }
}
