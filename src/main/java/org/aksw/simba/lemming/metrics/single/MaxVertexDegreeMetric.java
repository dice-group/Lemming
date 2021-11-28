package org.aksw.simba.lemming.metrics.single;

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
        if (direction == DIRECTION.in) {
            return graph.getGraph().getMaxInEdgeDegrees();
        } else {
            return graph.getGraph().getMaxOutEdgeDegrees();
        }
    }

    /**
     * Returns metric results that can be reused for further computations. Here, the
     * metric result object is initialized. Storing the maximum vertex degree and
     * the vertex having that degree.
     * 
     * @param graph
     *            - input graph.
     * @return - metric result.
     */
    @Override
    public UpdatableMetricResult applyUpdatable(ColouredGraph graph) {
        MaxVertexDegreeMetricResult metricResultTempObj = new MaxVertexDegreeMetricResult(getName(), Double.MIN_VALUE);

        IntSet vertices = graph.getGraph().getVertices();
        if (direction == DIRECTION.in) {
            for (int vertex : vertices) {
                int degree = graph.getGraph().getInEdgeDegree(vertex);
                if (degree > metricResultTempObj.getResult()) {
                    metricResultTempObj.setResult(degree);
                    metricResultTempObj.setVertexID(vertex);
                }
            }
        } else {
            for (int vertex : vertices) {
                int degree = graph.getGraph().getOutEdgeDegree(vertex);
                if (degree > metricResultTempObj.getResult()) {
                    metricResultTempObj.setResult(degree);
                    metricResultTempObj.setVertexID(vertex);
                }
            }
        }

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
        // Need to compute MaxVertexInDegree metric or MaxVertexOutDegree

        int vertexID = direction == DIRECTION.in ? triple.headId : triple.tailId;
        // For MaxVertexInDegree, need to use triple.headId else triple.tailId
        int updateVertexDegree = graphOperation == Operation.ADD ? 1 : -1;
        // variable to track remove an edge or add an edge operation.

        double metVal = ((MaxVertexDegreeMetricResult) previousResult).getResult();
        int changedDegree = getChangedDegree(graph, vertexID, direction);
        int degree = changedDegree - updateVertexDegree;
        if (updateVertexDegree == -1) { // Remove an edge
            if (degree == metVal) {// If degree of a vertex is equal to maximum vertex then apply method is called.
                previousResult = applyUpdatable(graph);
            }
        } else { // Add an edge
            if (degree == metVal) {// If degree of a vertex is equal to maximum vertex then max vertex degree is
                                   // changed.
                ((MaxVertexDegreeMetricResult) previousResult).setResult(changedDegree);
            }
        }

        return previousResult;

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
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param seed
     *            - Seed Value
     * @param indicator
     *            - boolean variable to indicate if metric value should be decreased or not.
     * @return
     */
    @Override
    public TripleBaseSingleID getTripleRemove(ColouredGraph graph, UpdatableMetricResult previousResult, long seed,
            boolean indicator) {
        TripleBaseSingleID tripleRemove = null;

        if (indicator) {// Need to reduce the metric

            // Initialization
            IntSet edges = null;
            int edgeId = -1;
            BitSet edgeColour = null;

            // Checking the metric, if in-degree or out-degree
            if ((direction == DIRECTION.in)) {
                edges = graph.getInEdges(((MaxVertexDegreeMetricResult) previousResult).getVertexID());
                System.out.println("Maximum Vertex In Degree");
             // Getting the edge of a vertex having maximum degree
                for (int edge : edges) {
                    edgeColour = graph.getEdgeColour(edge);
                    if (!edgeColour.equals(graph.getRDFTypePropertyColour()) && graph.getHeadOfTheEdge(edge) == ((MaxVertexDegreeMetricResult) previousResult).getVertexID() ) {
                        edgeId = edge;
                        break;
                    }
                }
                
            } else if ((direction == DIRECTION.out)) {
                edges = graph.getOutEdges(((MaxVertexDegreeMetricResult) previousResult).getVertexID());
                System.out.println("Maximum Vertex Out Degree");
             // Getting the edge of a vertex having maximum degree
                for (int edge : edges) {
                    edgeColour = graph.getEdgeColour(edge);
                    if (!edgeColour.equals(graph.getRDFTypePropertyColour()) && graph.getTailOfTheEdge(edge) == ((MaxVertexDegreeMetricResult) previousResult).getVertexID()) {
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
        
        if(tripleRemove == null) { // If triple couldn't be found for the maximum vertex degree or we don't need to reduce the metric
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
        
        if ((direction == DIRECTION.in) && indicator) {
            tripleAdd.headId = ((MaxVertexDegreeMetricResult) previousResult).getVertexID();
        } else if ((direction == DIRECTION.out) && indicator) {
            tripleAdd.tailId = ((MaxVertexDegreeMetricResult) previousResult).getVertexID();
        }
        return tripleAdd;
    }
}
