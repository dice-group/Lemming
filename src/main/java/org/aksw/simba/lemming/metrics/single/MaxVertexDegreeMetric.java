package org.aksw.simba.lemming.metrics.single;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;

import com.carrotsearch.hppc.BitSet;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import grph.Grph.DIRECTION;

/**
 * This metric is the highest degree of in or outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MaxVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

    protected DIRECTION direction;

    public MaxVertexDegreeMetric(DIRECTION direction) {
        super(direction == DIRECTION.in ? "maxInDegree" : "maxOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {
        return applyUpdatable(graph).getResult();
    }
    
    /**
     * Returns metric results that can be reused for further computations. Here, the
     * metric result object is initialized. Storing the maximum vertex degree and
     * the number of vertices having the maximum degree.
     * 
     * @param graph
     *            - input graph.
     * @return - metric result.
     */
    @Override
    public UpdatableMetricResult applyUpdatable(ColouredGraph graph) {
        MaxVertexDegreeMetricResult metricResultTempObj = new MaxVertexDegreeMetricResult(getName(), Double.MIN_VALUE);        
        metricResultTempObj.setDirection(direction);//set direction
        
        IntSet vertices = graph.getGraph().getVertices();

        int numOfVerticesWithMaxDegree = 0;// Variable to track number of vertices with maximum degree.
        int tempMinDegree = Integer.MAX_VALUE;

        for (int vertex : vertices) {
            int degree = getChangedDegree(graph, vertex, direction);
            if (degree == metricResultTempObj.getResult()) {
                metricResultTempObj.setResult(degree);
                metricResultTempObj.setMaxVertexID(vertex);
                numOfVerticesWithMaxDegree++;
            } else if (degree > metricResultTempObj.getResult()) {
                metricResultTempObj.setResult(degree);
                metricResultTempObj.setMaxVertexID(vertex);
                numOfVerticesWithMaxDegree = 1;
            }
            
            if((degree < tempMinDegree) && (degree > 0)) {
                //Logic to store vertex having minimum degree
                metricResultTempObj.setMinVertexID(vertex);
                tempMinDegree = degree;
            }

        }

        metricResultTempObj.setNumOfVerticesWithMaxDeg(numOfVerticesWithMaxDegree);
        return metricResultTempObj;
    }
    
    /**
     * The method contains logic that computes the maximum vertex degree metric
     * efficiently. It will check the previously stored maximum vertex degree and
     * the vertex on which add or remove an edge graph operation is performed. If
     * the vertex has the same degree as the maximum vertex degree then the metric
     * value will be updated depending upon the graph operation.
     * 
     * @param triple         - edge on which graph operation is performed.
     * @param graph          - input graph.
     * @param graphOperation - Enum indicating graph operation. ("ADD" for adding an
     *                       edge and "REMOVE" for removing an edge)
     * @param previousResult - UpdatableMetricResult object containing the previous
     *                       computed results.
     * @return
     */
    @Override
    public UpdatableMetricResult update(ColouredGraph graph, TripleBaseSingleID triple, Operation graphOperation,
            UpdatableMetricResult previousResult) {
        
        if(previousResult == null) {
            return applyUpdatable(graph);
        }
        
        MaxVertexDegreeMetricResult metricResultTempObj = new MaxVertexDegreeMetricResult(getName(), ((MaxVertexDegreeMetricResult) previousResult).getResult());
        metricResultTempObj.setNumOfVerticesWithMaxDeg(((MaxVertexDegreeMetricResult) previousResult).getNumOfVerticesWithMaxDeg());

        // Need to compute MaxVertexInDegree metric or MaxVertexOutDegree
        int vertexID = direction == DIRECTION.in ? triple.headId : triple.tailId;
        // For MaxVertexInDegree, need to use triple.headId else triple.tailId
        int updateVertexDegree = graphOperation == Operation.ADD ? 1 : -1;
        // variable to track remove an edge or add an edge operation.


        int changedDegree = getChangedDegree(graph, vertexID, direction);
        int degree = changedDegree - updateVertexDegree;
        if (updateVertexDegree == -1) { // Remove an edge
            if (degree == ((MaxVertexDegreeMetricResult) previousResult).getResult()
                    && ((MaxVertexDegreeMetricResult) previousResult).getNumOfVerticesWithMaxDeg() - 1 == 0) {
                // If degree of a vertex is equal to maximum vertex and there is only one vertex with maximum degree
                // then recalculate
                return applyUpdatable(graph);
            } else if (degree == ((MaxVertexDegreeMetricResult) previousResult).getResult()) {
                // If degree of a vertex is equal to maximum vertex and there are mpre than one vertex with maximum degree
                // then reuse previous result and update the number of vertices with maximum degree
                metricResultTempObj.setNumOfVerticesWithMaxDeg(
                        ((MaxVertexDegreeMetricResult) previousResult).getNumOfVerticesWithMaxDeg() - 1);
                metricResultTempObj.setResult(((MaxVertexDegreeMetricResult) previousResult).getResult());
            }
        } else { // Add an edge
            if (degree == ((MaxVertexDegreeMetricResult) previousResult).getResult()) {
                // If degree of a vertex is equal to maximum vertex then max vertex degree is
                // changed.
                metricResultTempObj.setResult(changedDegree);
                metricResultTempObj.setNumOfVerticesWithMaxDeg(1);
            }
        }

        metricResultTempObj.setDirection(direction);
        metricResultTempObj.setMaxVertexID(((MaxVertexDegreeMetricResult) previousResult).getMaxVertexID());
        metricResultTempObj.setMinVertexID(((MaxVertexDegreeMetricResult) previousResult).getMinVertexID());
        return metricResultTempObj;

    }

    private int getChangedDegree(ColouredGraph graph, int vertexID, DIRECTION direction) {
        if (direction == DIRECTION.in) {
            return graph.getGraph().getInEdgeDegree(vertexID);
        } else {
            return graph.getGraph().getOutEdgeDegree(vertexID);
        }
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
    public TripleBaseSingleID getTripleRemove(ColouredGraph graph, List<UpdatableMetricResult> previousResultList, long seed,
            boolean changeMetricValue) {
        TripleBaseSingleID tripleRemove = null;

        if (changeMetricValue) {// Need to reduce the metric

            MaxVertexDegreeMetricResult maxResultObject = null;
            //Logic to iterate over metric result object list and get the required object
            for (UpdatableMetricResult resultObject : previousResultList) {
                if (resultObject instanceof MaxVertexDegreeMetricResult) {
                    maxResultObject = (MaxVertexDegreeMetricResult) resultObject;
                    break;

                }
            }

            // Initialization
            IntSet edges = null;
            int edgeId = -1;
            BitSet edgeColour = null;

            // Checking the metric, if in-degree or out-degree
            if ((direction == DIRECTION.in)) {
                edges = graph.getInEdges(maxResultObject.getMaxVertexID());
                // System.out.println("Maximum Vertex In Degree");
                // Getting the edge of a vertex having maximum degree
                for (int edge : edges) {
                    edgeColour = graph.getEdgeColour(edge);
                    if (!edgeColour.equals(graph.getRDFTypePropertyColour())
                            && graph.getHeadOfTheEdge(edge) == maxResultObject.getMaxVertexID()) {
                        edgeId = edge;
                        break;
                    }
                }

            } else if ((direction == DIRECTION.out)) {
                edges = graph.getOutEdges(maxResultObject.getMaxVertexID());
                // System.out.println("Maximum Vertex Out Degree");
                // Getting the edge of a vertex having maximum degree
                for (int edge : edges) {
                    edgeColour = graph.getEdgeColour(edge);
                    if (!edgeColour.equals(graph.getRDFTypePropertyColour())
                            && graph.getTailOfTheEdge(edge) == maxResultObject.getMaxVertexID()) {
                        edgeId = edge;
                        break;
                    }
                }
            }

            if (edgeId != -1) {// If Edge is found
                tripleRemove = new TripleBaseSingleID();
                tripleRemove.tailId = graph.getTailOfTheEdge(edgeId);
                tripleRemove.headId = graph.getHeadOfTheEdge(edgeId);
                tripleRemove.edgeId = edgeId;
                tripleRemove.edgeColour = edgeColour;
            }
        }

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
     *            - boolean value If the variable is false, then it will generate a
     *            triple as per the implementation defined in the Generator class,
     *            else the triple is generated as per the default implementation.
     * @param previousResultList
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param changeMetricValue
     *            - boolean variable to indicate if the metric value should be decreased
     *            or not. If the variable is true, then the method will return a
     *            triple that reduces the metric value.
     * @return - triple to add.
     */
    @Override
    public TripleBaseSingleID getTripleAdd(ColouredGraph graph, IGraphGeneration mGrphGenerator, boolean mProcessRandomly, List<UpdatableMetricResult> previousResultList, boolean changeMetricValue) {
        TripleBaseSingleID tripleAdd = getTripleAdd(graph, mGrphGenerator, mProcessRandomly);
        
        if (!changeMetricValue) {
            
            MaxVertexDegreeMetricResult maxResultObject = null;
            //Logic to iterate over metric result object list and get the required object
            for (UpdatableMetricResult resultObject : previousResultList) {
                if (resultObject instanceof MaxVertexDegreeMetricResult) {
                    maxResultObject = (MaxVertexDegreeMetricResult) resultObject;
                    break;

                }
            }
            
            if (direction == DIRECTION.in) {
                tripleAdd.headId = maxResultObject.getMaxVertexID();
            } else if (direction == DIRECTION.out) {
                tripleAdd.tailId = maxResultObject.getMaxVertexID();
            }
        }
        return tripleAdd;
    }
}
