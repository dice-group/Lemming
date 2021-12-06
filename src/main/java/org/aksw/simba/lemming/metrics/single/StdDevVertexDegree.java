package org.aksw.simba.lemming.metrics.single;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;

import com.carrotsearch.hppc.BitSet;

import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

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
     * This method calculates the average and Variance for the first time. It
     * returns a UpdatableMetricResult object that can be reused to compute
     * StdDeviation in the next iterations
     * 
     * @param graph - the graph object
     * @return UpdatableMetricResult - metric result object
     */
    @Override
    public UpdatableMetricResult applyUpdatable(ColouredGraph graph) {
        StdDevVertexDegreeMetricResult metricResultObj = new StdDevVertexDegreeMetricResult(getName(), Double.NaN);
        IntArrayList vertexDegrees = (this.direction == DIRECTION.in) ? graph.getGraph().getAllInEdgeDegrees()
                : graph.getGraph().getAllOutEdgeDegrees();

        double averageOfDegrees = super.calculateAvg(vertexDegrees);
        double sumForVariance = 0.0;
        double temp;
        for (Integer vertexDegree : vertexDegrees) {
            temp = averageOfDegrees - vertexDegree;
            temp *= temp;
            sumForVariance += temp;
        }
        double variance = sumForVariance / vertexDegrees.size();

        metricResultObj.setAvgVertexDegree(averageOfDegrees);
        metricResultObj.setVarianceVertexDegree(variance);
        metricResultObj.setNumberOfVertices(vertexDegrees.size());
        metricResultObj.setResult(Math.sqrt(variance));

        return metricResultObj;

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

        StdDevVertexDegreeMetricResult metricResultObj = new StdDevVertexDegreeMetricResult(getName(), Double.NaN);

        double avg = ((StdDevVertexDegreeMetricResult) previousResult).getAvgVertexDegree();
        double variance = ((StdDevVertexDegreeMetricResult) previousResult).getVarianceVertexDegree();
        double numberOfVertices = ((StdDevVertexDegreeMetricResult) previousResult).getNumberOfVertices();
        double newDegree = (this.direction == DIRECTION.in) ? graph.getGraph().getInEdgeDegree(triple.headId)
                : graph.getGraph().getOutEdgeDegree(triple.tailId);
        List<Double> newAvgAndVariance = computeAvgVarianceFromPreviousResult(numberOfVertices, avg, variance,
                newDegree, graphOperation);
        avg = newAvgAndVariance.get(0);
        variance = newAvgAndVariance.get(1);

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
            double newDegree, Operation graphOperation) {
        List<Double> list = new ArrayList<Double>();
        double flag = graphOperation == Operation.ADD ? 1 : -1;
        double oldDegree = newDegree - flag;
        double newAvg = avg + (flag / numberOfVertices);
        double newVariance = (variance + Math.pow(numberOfVertices, -2)
                + (Math.pow((newDegree - newAvg), 2) - Math.pow((oldDegree - newAvg), 2)) / numberOfVertices);
        list.add(newAvg);
        list.add(newVariance);
        return list;
    }
    
    /**
     * The method returns the triple to remove by using the previous metric result object.
     *      * 
     * @param graph
     *            - Input Graph
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param seed
     *            - Seed Value
     * @param indicator
     *            - boolean variable to indicate if metric value should be increased or not.
     * @return
     */
    @Override
    public TripleBaseSingleID getTripleRemove(ColouredGraph graph, UpdatableMetricResult previousResult, long seed,
            boolean indicator) {
        TripleBaseSingleID tripleRemove = null;
        int vertexID = -1;
        if (indicator) {// Need to increase the metric
            vertexID = ((MaxVertexDegreeMetricResult) previousResult).getMaxVertexID();
        } else {//Need to reduce the metric
            vertexID = ((MaxVertexDegreeMetricResult) previousResult).getMinVertexID();
        }

        // Initialization
        IntSet edges = null;
        int edgeId = -1;
        BitSet edgeColour = null;

        // Checking the metric, if in-degree or out-degree
        if ((direction == DIRECTION.in)) {
            edges = graph.getInEdges(vertexID);
            for (int edge : edges) {
                edgeColour = graph.getEdgeColour(edge);
                tripleRemove = new TripleBaseSingleID();
                if (!edgeColour.equals(graph.getRDFTypePropertyColour()) && graph.getHeadOfTheEdge(edge) == vertexID) {
                    edgeId = edge;
                    tripleRemove.tailId = graph.getTailOfTheEdge(edgeId);
                    tripleRemove.headId = graph.getHeadOfTheEdge(edgeId);
                    tripleRemove.edgeId = edgeId;
                    tripleRemove.edgeColour = edgeColour;
                    break;
                }else {
                    edgeId = edge;
                    tripleRemove.headId = graph.getTailOfTheEdge(edgeId);
                    tripleRemove.tailId = graph.getHeadOfTheEdge(edgeId);
                    tripleRemove.edgeId = edgeId;
                    tripleRemove.edgeColour = edgeColour;
                    break;
                }
            }

        } else if ((direction == DIRECTION.out)) {
            edges = graph.getOutEdges(vertexID);
            for (int edge : edges) {
                edgeColour = graph.getEdgeColour(edge);
                tripleRemove = new TripleBaseSingleID();
                if (!edgeColour.equals(graph.getRDFTypePropertyColour()) && graph.getTailOfTheEdge(edge) == vertexID) {
                    edgeId = edge;
                    tripleRemove.tailId = graph.getTailOfTheEdge(edgeId);
                    tripleRemove.headId = graph.getHeadOfTheEdge(edgeId);
                    tripleRemove.edgeId = edgeId;
                    tripleRemove.edgeColour = edgeColour;
                    break;
                }else {
                    edgeId = edge;
                    tripleRemove.headId = graph.getTailOfTheEdge(edgeId);
                    tripleRemove.tailId = graph.getHeadOfTheEdge(edgeId);
                    tripleRemove.edgeId = edgeId;
                    tripleRemove.edgeColour = edgeColour;
                    break;
                }
            }
        }
        
        /*if (edgeId != -1) {// If Edge is found
            tripleRemove = new TripleBaseSingleID();
            tripleRemove.tailId = graph.getTailOfTheEdge(edgeId);
            tripleRemove.headId = graph.getHeadOfTheEdge(edgeId);
            tripleRemove.edgeId = edgeId;
            tripleRemove.edgeColour = edgeColour;
        }*/


        if (tripleRemove == null) { // If triple couldn't be found for the maximum vertex degree or we don't need to
                                    // reduce the metric
            tripleRemove = getTripleRemove(graph, seed);
        }

        return tripleRemove;
    }
    
    /**
     * The method returns the triple to remove by using the previous metric result object.
     *      * 
     * @param mGrphGenerator
     *            - Graph Generator used during execution
     * @param mProcessRandomly
     *            - boolean value
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param indicator
     *            - boolean variable to indicate if metric value should be increased or not.
     * @return
     */
    @Override
    public TripleBaseSingleID getTripleAdd(IGraphGeneration mGrphGenerator, boolean mProcessRandomly, UpdatableMetricResult previousResult, boolean indicator) {
        TripleBaseSingleID tripleAdd = getTripleAdd(mGrphGenerator, mProcessRandomly);
        
        int vertexID = -1;
        if (indicator) {// Need to reduce the metric
            vertexID = ((MaxVertexDegreeMetricResult) previousResult).getMaxVertexID();
        } else {//Need to increase the metric
            vertexID = ((MaxVertexDegreeMetricResult) previousResult).getMinVertexID();
        }
        
        if (direction == DIRECTION.in) {
            tripleAdd.headId = vertexID;
        } else if (direction == DIRECTION.out) {
            tripleAdd.tailId = vertexID;
        }
        return tripleAdd;
    }

}
