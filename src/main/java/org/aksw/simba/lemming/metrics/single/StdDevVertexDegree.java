package org.aksw.simba.lemming.metrics.single;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.AbstractGraphGeneration;
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
        return applyUpdatable(graph).getResult();
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

        if (previousResult == null) {
            return applyUpdatable(graph);
        }

        StdDevVertexDegreeMetricResult metricResultObj = new StdDevVertexDegreeMetricResult(getName(), Double.NaN);

        double avg = ((StdDevVertexDegreeMetricResult) previousResult).getAvgVertexDegree();
        double variance = ((StdDevVertexDegreeMetricResult) previousResult).getVarianceVertexDegree();
        double numberOfVertices = ((StdDevVertexDegreeMetricResult) previousResult).getNumberOfVertices();
        double newDegree = (this.direction == DIRECTION.in) ? graph.getGraph().getInEdgeDegree(triple.headId)
                : graph.getGraph().getOutEdgeDegree(triple.tailId);
        double[] newAvgAndVariance = computeAvgVarianceFromPreviousResult(numberOfVertices, avg, variance, newDegree,
                graphOperation);
        avg = newAvgAndVariance[0];
        variance = newAvgAndVariance[1];

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
     * @return double[] - double array containing average and variance in that
     *         order.
     */
    private double[] computeAvgVarianceFromPreviousResult(double numberOfVertices, double avg, double variance,
            double newDegree, Operation graphOperation) {
        double[] list = new double[2];
        double changeInDegree = graphOperation == Operation.ADD ? 1 : -1;
        double oldDegree = newDegree - changeInDegree;
        double newAvg = avg + (changeInDegree / numberOfVertices);
        double newVariance = (variance + Math.pow(numberOfVertices, -2)
                + (Math.pow((newDegree - newAvg), 2) - Math.pow((oldDegree - newAvg), 2)) / numberOfVertices);
        list[0] = newAvg;
        list[1] = newVariance;
        return list;
    }
    
    /**
     * The method returns the triple to remove by using the previous metric result object.
     *      * 
     * @param graph
     *            - Input Graph
     * @param previousResultList
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param seed
     *            - Seed Value used to generate random triple.
     * @param changeMetricValue
     *            - boolean variable to indicate if the metric value should be decreased
     *            or not. If the variable is true, then the method will return a
     *            triple that reduces the metric value.
     * @return - triple to remove.
     */
    @Override
    public TripleBaseSingleID getTripleRemove(ColouredGraph graph, List<UpdatableMetricResult> previousResultList, long seed, boolean changeMetricValue) {
        TripleBaseSingleID tripleRemove = null;
        
        MaxVertexDegreeMetricResult maxResultObject = null;
        AvgVertexDegreeMetricResult avgResultObject = null;
        //Logic to iterate over metric result object list and get the required object
        for (UpdatableMetricResult resultObject : previousResultList) {
            if (resultObject instanceof MaxVertexDegreeMetricResult) {
                if(((MaxVertexDegreeMetricResult) resultObject).getDirection() == direction)
                    maxResultObject = (MaxVertexDegreeMetricResult) resultObject;
            }else if(resultObject instanceof AvgVertexDegreeMetricResult) {
                avgResultObject = (AvgVertexDegreeMetricResult) resultObject;
            }
        }
        
        int vertexID = -1;
        if (changeMetricValue) {// Need to reduce the metric
            vertexID = maxResultObject.getMaxVertexID();
        } else {// Need to increase the metric
            vertexID = maxResultObject.getMinVertexID();
        }

        // Initialization
        IntSet edges = null;
        int edgeId = -1;
        BitSet edgeColour = null;

        // Checking the metric, if in-degree or out-degree
        if (direction == DIRECTION.in) {
            edges = graph.getInEdges(vertexID);
        } else {
            edges = graph.getOutEdges(vertexID);
        }

        // Iterating over edges
        for (int edge : edges) {
            edgeColour = graph.getEdgeColour(edge);

            // Logic to compare vertex degrees with average vertex degree.
            boolean compareDegrees = false;
            IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edge);
            
            if(verticesIncidentToEdge.size() > 1) // in case of self loop, can get an exception
                verticesIncidentToEdge.remove(vertexID); // Remove Max vertex or Min vertex
            
            Integer vertexForEdge = verticesIncidentToEdge.iterator().nextInt(); // Store the other vertex in a variable

            int vertexDegreeTocheck = -1; // Variable to store vertex which should be compared with average degree
            if (direction == DIRECTION.in) {
                vertexDegreeTocheck = graph.getGraph().getInEdgeDegree(vertexForEdge);
            } else {
                vertexDegreeTocheck = graph.getGraph().getOutEdgeDegree(vertexForEdge);
            }
            
            //Compare vertex with average vertex degree
            if (changeMetricValue) {
                // If metric should be decreased, the degree of other vertex should be large
                // than average vertex degree
                compareDegrees = vertexDegreeTocheck > avgResultObject.getResult() ? true : false;

            } else {
                // If metric should be increased, the degree of other vertex should be less than
                // average vertex degree
                compareDegrees = vertexDegreeTocheck < avgResultObject.getResult() ? true : false;

            }

            // Logic to assign triple details
            if (!edgeColour.equals(graph.getRDFTypePropertyColour()) && graph.getHeadOfTheEdge(edge) == vertexID
                    && compareDegrees) {
                edgeId = edge;
                tripleRemove = new TripleBaseSingleID();
                tripleRemove.tailId = graph.getTailOfTheEdge(edgeId);
                tripleRemove.headId = graph.getHeadOfTheEdge(edgeId);
                tripleRemove.edgeId = edgeId;
                tripleRemove.edgeColour = edgeColour;
                break;
            } else if (compareDegrees) {
                edgeId = edge;
                tripleRemove = new TripleBaseSingleID();
                tripleRemove.headId = graph.getTailOfTheEdge(edgeId);
                tripleRemove.tailId = graph.getHeadOfTheEdge(edgeId);
                tripleRemove.edgeId = edgeId;
                tripleRemove.edgeColour = edgeColour;
                break;
            }
            
        }

        if (tripleRemove == null) { // If triple couldn't be found 
            tripleRemove = getTripleRemove(graph, seed);
        }

        return tripleRemove;
    }
    
    /**
     * The method returns the triple to add by using the previous metric result object.
     *      * 
     * @param mGrphGenerator
     *            - Graph Generator used during execution
     * @param mProcessRandomly
     *            - boolean value
     * @param previousResultList
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param indicator
     *            - boolean variable to indicate if the metric value should be decreased
     *            or not. If the variable is true, then the method will return a
     *            triple that reduces the metric value.
     * @return - triple to add.
     */
    @Override
    public TripleBaseSingleID getTripleAdd(ColouredGraph graph, IGraphGeneration mGrphGenerator,
            boolean mProcessRandomly, List<UpdatableMetricResult> previousResultList, boolean indicator) {
        TripleBaseSingleID tripleAdd = null;

        MaxVertexDegreeMetricResult maxResultObject = null;
        AvgVertexDegreeMetricResult avgResultObject = null;
        // Logic to iterate over metric result object list and get the required object
        for (UpdatableMetricResult resultObject : previousResultList) {
            if (resultObject instanceof MaxVertexDegreeMetricResult) {
                if (((MaxVertexDegreeMetricResult) resultObject).getDirection() == direction)
                    maxResultObject = (MaxVertexDegreeMetricResult) resultObject;
            } else if (resultObject instanceof AvgVertexDegreeMetricResult) {
                avgResultObject = (AvgVertexDegreeMetricResult) resultObject;
            }
        }

        int vertexID = -1;
        if (!indicator) {// Need to increase the metric
            vertexID = maxResultObject.getMaxVertexID();
        } else {// Need to reduce the metric
            vertexID = maxResultObject.getMinVertexID();
        }

        for (Integer graphVertex : graph.getVertices()) {
            if (!indicator && (graph.getGraph().getInEdgeDegree(graphVertex) > avgResultObject.getResult())) {
                //Need to increase the metric both the vertices degree greater than average degree
                if (direction == DIRECTION.in) {
                    tripleAdd = ((AbstractGraphGeneration) mGrphGenerator).getProposedTripleForHeadIdAndTailId(graphVertex, vertexID);
                } else {
                    tripleAdd = ((AbstractGraphGeneration) mGrphGenerator).getProposedTripleForHeadIdAndTailId(vertexID, graphVertex);
                }
            } else if (indicator && (graph.getGraph().getInEdgeDegree(graphVertex) < avgResultObject.getResult())) {
                //Need to decrease the metric both the vertices degree less than average degree
                if (direction == DIRECTION.in) {
                    tripleAdd = ((AbstractGraphGeneration) mGrphGenerator).getProposedTripleForHeadIdAndTailId(graphVertex, vertexID);
                } else {
                    tripleAdd = ((AbstractGraphGeneration) mGrphGenerator).getProposedTripleForHeadIdAndTailId(vertexID, graphVertex);
                }
            }
            
            if(tripleAdd != null) {
                break;
            }

        }

        if (tripleAdd == null) {
            tripleAdd = getTripleAdd(graph, mGrphGenerator, mProcessRandomly);
        }

        return tripleAdd;
    }
    
    /**
     * The method returns a list of metrics on which standard deviation metric
     * depends
     * 
     * @return - List of metrics.
     */
    @Override
    public List<SingleValueMetric> getDependentMetricsList(){
        if(direction == DIRECTION.in) {
            return Arrays.asList(new MaxVertexDegreeMetric(DIRECTION.in), new AvgVertexDegreeMetric());
        }else {
            return Arrays.asList(new MaxVertexDegreeMetric(DIRECTION.out), new AvgVertexDegreeMetric());
        }
    }

}
