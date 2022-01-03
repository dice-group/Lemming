package org.aksw.simba.lemming.metrics.single.nodetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.metricselection.NodeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetricResult;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodeTriangleMetric extends AbstractMetric implements SingleValueMetric{

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeTriangleMetric.class);
	
	public NodeTriangleMetric(){
		super("#nodetriangles");
	}

	@Override
	public double apply(ColouredGraph graph) {
		return applyUpdatable(graph).getResult();
	}

	/**
	 * The method is used to initialize the node triangle metric with the given graph.
	 * @param graph the given graph
	 * @return number of node triangles
	 */
	@Override
	public UpdatableMetricResult applyUpdatable(ColouredGraph graph) {
		NodeTriangleMetricSelection selector = new NodeTriangleMetricSelection();
		SingleValueMetric nodeTriangleMetric = selector.getMinComplexityMetric(graph);

		double triangleMetric = nodeTriangleMetric.apply(graph);
		return new SingleValueMetricResult(getName(), triangleMetric);
	}

	/**
	 * @param graph the given graph is already modified!
	 */
	@Override
	public UpdatableMetricResult update(@Nonnull ColouredGraph graph, @Nonnull TripleBaseSingleID triple, @Nonnull Operation opt,
										@Nullable UpdatableMetricResult previousResult) {

		if(previousResult==null){
			return applyUpdatable(graph);
		}
		int headId = triple.headId;
		int tailId = triple.tailId;

		//if headId = tailId, result is not change.
		if(headId == tailId){
			return previousResult;
		}

		int numEdgesBetweenVertices = IntSetUtil.intersection(graph.getEdgesIncidentTo(tailId), graph.getEdgesIncidentTo(headId)).size();

		int numberOfCommon = MetricUtils.getVerticesInCommon(graph, headId, tailId).size();

		//the previous result could be maintained except for 2 cases:
		double newResult = previousResult.getResult();

		//1.case: remove an edge, and number of edges between head and tail is 1
		// -> new metric = old metric - number of common vertices
		if(numEdgesBetweenVertices==0 && opt == Operation.REMOVE){
			newResult = newResult - numberOfCommon;

		//2.case: add an edge, and number of edges between head and tail is 0
		// -> new metric = old metric + number of common vertices
		}else if (numEdgesBetweenVertices==1 && opt == Operation.ADD){
			newResult = newResult + numberOfCommon;
		}
		if(newResult < 0){
			LOGGER.error("The new result of node triangle metric is negative : " + newResult );
			newResult = 0;
		}

		return new SingleValueMetricResult(previousResult.getMetricName(), newResult);
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
     *            - Seed Value used to generate random triple.
     * @param changeMetricValue
     *            - boolean variable to indicate if the metric value should be decreased
     *            or not. If the variable is true, then the method will return a
     *            triple that reduces the metric value.
     * @return
     */
    @Override
    public TripleBaseSingleID getTripleRemove(ColouredGraph graph, List<UpdatableMetricResult> previousResult, long seed,
            boolean changeMetricValue) {
        TripleBaseSingleID tripleRemove = null;

            for (Integer edge : graph.getEdges()) {// Iterating edges

                // Get vertices incident to edge
                IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edge);
                IntIterator iterator = verticesIncidentToEdge.iterator();
                int firstIncidentVertex = iterator.nextInt();
                int secondIncidentVertex = iterator.nextInt();

                if ((IntSetUtil.intersection(graph.getEdgesIncidentTo(firstIncidentVertex),
                        graph.getEdgesIncidentTo(secondIncidentVertex)).size() == 1)
                        && (MetricUtils.getVerticesInCommon(graph, firstIncidentVertex, secondIncidentVertex)
                                .size() > 0) && changeMetricValue) {
                    // if only one edge is present between the vertices then removing the edge will
                    // reduce the node triangle
                    tripleRemove = new TripleBaseSingleID();
                    tripleRemove.tailId = graph.getTailOfTheEdge(edge);
                    tripleRemove.headId = graph.getHeadOfTheEdge(edge);
                    tripleRemove.edgeId = edge;
                    tripleRemove.edgeColour = graph.getEdgeColour(edge);
                    break;
                }else if((IntSetUtil.intersection(graph.getEdgesIncidentTo(firstIncidentVertex),
                        graph.getEdgesIncidentTo(secondIncidentVertex)).size() > 1)
                         && !changeMetricValue) {
                    tripleRemove = new TripleBaseSingleID();
                    tripleRemove.tailId = graph.getTailOfTheEdge(edge);
                    tripleRemove.headId = graph.getHeadOfTheEdge(edge);
                    tripleRemove.edgeId = edge;
                    tripleRemove.edgeColour = graph.getEdgeColour(edge);
                    break;
                }
            }

        

        if (tripleRemove == null) { // If triple couldn't be found such that the node triangle metric can be
                                    // reduced.
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
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param changeMetricValue
     *            - boolean variable to indicate if the metric value should be increased
     *            or not. If the variable is true, then the method will return a
     *            triple that increases the metric value.
     * @return
     */
    @Override
    public TripleBaseSingleID getTripleAdd(ColouredGraph graph, IGraphGeneration mGrphGenerator, boolean mProcessRandomly, List<UpdatableMetricResult> previousResult, boolean changeMetricValue) {
        TripleBaseSingleID tripleAdd = getTripleAdd(graph, mGrphGenerator, mProcessRandomly);
        
        if (changeMetricValue) {// Need to increase the metric

            for (Integer edge : graph.getEdges()) {// Iterating edges

                // Get vertices incident to edge
                IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edge);
                int firstIncidentVertex = verticesIncidentToEdge.iterator().nextInt();
                int secondIncidentVertex = verticesIncidentToEdge.iterator().nextInt();

                
                IntSet difference = IntSetUtil.difference(graph.getEdgesIncidentTo(firstIncidentVertex), graph.getEdgesIncidentTo(secondIncidentVertex));
                if ( difference.size() > 0) {
                    // if only one edge is present between the vertices then removing the edge will reduce the node triangle
                    
                    
                    tripleAdd = new TripleBaseSingleID();
                    tripleAdd.tailId = graph.getTailOfTheEdge(edge);
                    tripleAdd.headId = graph.getHeadOfTheEdge(edge);
                    tripleAdd.edgeId = edge;
                    tripleAdd.edgeColour = graph.getEdgeColour(edge);
                    break;
                }
            }

        }
       
        return tripleAdd;
    }
}
