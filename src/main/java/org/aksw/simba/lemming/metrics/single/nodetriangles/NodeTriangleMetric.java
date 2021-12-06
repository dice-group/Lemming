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
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;

import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntSet;

import javax.annotation.Nonnull;

public class NodeTriangleMetric extends AbstractMetric implements SingleValueMetric{
	
	public NodeTriangleMetric(){
		super("#nodetriangles");
	}

	@Override
	public double apply(ColouredGraph graph) {
		
		NodeTriangleMetricSelection selector = new NodeTriangleMetricSelection();
		SingleValueMetric nodeTriangleMetric = selector.getMinComplexityMetric(graph);
		
		return nodeTriangleMetric.apply(graph);
	}


	/**
	 * @param graph   the given graph is already modified!
	 */
	@Override
	public UpdatableMetricResult update(@Nonnull ColouredGraph graph, @Nonnull TripleBaseSingleID triple, @Nonnull Operation opt,
										@Nonnull UpdatableMetricResult previousResult) {

		int headId = triple.headId;
		int tailId = triple.edgeId;

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

		newResult = newResult>=0 ? newResult : 0;

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
    public TripleBaseSingleID getTripleRemove(ColouredGraph graph, UpdatableMetricResult previousResult, long seed,
            boolean changeMetricValue) {
        TripleBaseSingleID tripleRemove = null;

        if (changeMetricValue) {// Need to reduce the metric

            for(int i = 0; i < graph.getVertices().size(); i++) {
                
                IntSet neighborSet = IntSetUtil.union(graph.getOutNeighbors(i), graph.getInNeighbors(i));
                for (int adjacentNodeId:neighborSet) {
                    IntSet edgesBetweenVertices = IntSetUtil.intersection(graph.getEdgesIncidentTo(i), graph.getEdgesIncidentTo(adjacentNodeId));
                    if(edgesBetweenVertices.size() == 1) {
                        int numberOfCommon = MetricUtils.getVerticesInCommon(graph, i, adjacentNodeId).size();
                        if(numberOfCommon > 0) {
                            int edgeId = edgesBetweenVertices.iterator().nextInt();
                            tripleRemove = new TripleBaseSingleID();
                            tripleRemove.tailId = graph.getTailOfTheEdge(edgeId);
                            tripleRemove.headId = graph.getHeadOfTheEdge(edgeId);
                            tripleRemove.edgeId = edgeId;
                            tripleRemove.edgeColour = graph.getEdgeColour(edgeId);
                            break;
                        }
                    }
                }
            }
            
        }
        
        if(tripleRemove == null) { // If triple couldn't be found such that the node triangle metric can be reduced.
            tripleRemove = getTripleRemove(graph, seed);
        }
        

        return tripleRemove;
    }
}
