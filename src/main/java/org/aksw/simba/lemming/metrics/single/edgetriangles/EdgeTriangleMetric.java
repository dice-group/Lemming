package org.aksw.simba.lemming.metrics.single.edgetriangles;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.metricselection.EdgeTriangleMetricSelection;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.AbstractGraphGeneration;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.ints.IntSet;

public class EdgeTriangleMetric extends AbstractMetric implements SingleValueMetric{

	private static final Logger LOGGER = LoggerFactory.getLogger(EdgeTriangleMetric.class);

	public EdgeTriangleMetric() {
		super("#edgetriangles");
	}
	
	@Override
	public double apply(ColouredGraph graph) {
		return applyUpdatable(graph).getResult();
	}

	/**
	 * The method is used to initialize the edge triangle metric with the given graph.
	 * @param graph the given graph
	 * @return number of edge triangles
	 */
	@Override
	public UpdatableMetricResult applyUpdatable(ColouredGraph graph) {
		EdgeTriangleMetricSelection selector = new EdgeTriangleMetricSelection();
		SingleValueMetric edgeTriangleMetric = selector.getMinComplexityMetric(graph);

		double triangleMetric = edgeTriangleMetric.apply(graph);
		return new SingleValueMetricResult(getName(), triangleMetric);
	}

	/**
	 * @param graph the given graph is already modified!
	 */
	@Override
	public UpdatableMetricResult update(@Nonnull ColouredGraph graph, @Nonnull TripleBaseSingleID triple, @Nonnull Operation opt,
										@Nullable UpdatableMetricResult previousResult){

		if(previousResult==null){
			return applyUpdatable(graph);
		}
		int headId = triple.headId;
		int tailId = triple.tailId;

		//if headId = tailId, result is not change.
		if(headId == tailId){
			return previousResult;
		}

		int change = opt==Operation.REMOVE ? -1 : 1 ;

		int numEdgesBetweenVertices = IntSetUtil.intersection(graph.getEdgesIncidentTo(tailId), graph.getEdgesIncidentTo(headId)).size() - change;

		int differenceOfSubGraph = calculateDifferenceOfSubGraphEdge(graph, headId, tailId, numEdgesBetweenVertices, change);
		double newResult = previousResult.getResult() + change*differenceOfSubGraph;

		if(newResult < 0){
			LOGGER.error("The new result of edge triangle metric is negative : " + newResult );
			newResult = 0;
		}

		return new SingleValueMetricResult(previousResult.getMetricName(), newResult);
	}

	/**
	 * It is used to calculate the difference of edge triangles in subgraph after removing or adding an edge
	 * @param headId head of the modified edge
	 * @param tailId tail of the modified edge
	 * @param numEdgesBetweenVertices number of the edges between head and tail
	 * @param change removing an edge, then -1, add an edge, then +1
	 * @return the difference of edge triangles in subgraph after removing or adding an edge
	 */
	private int calculateDifferenceOfSubGraphEdge(ColouredGraph graph, int headId, int tailId, int numEdgesBetweenVertices, int change) {
		int oldSubGraphEdgeTriangles = 0;
		int newSubGraphTriangles = 0;
		int newNumEdgesBetweenVertices = numEdgesBetweenVertices + change;

		for (int vertex : MetricUtils.getVerticesInCommon(graph, headId, tailId)) {
			int numEdgesFromHead = IntSetUtil.intersection(graph.getEdgesIncidentTo(headId),
					graph.getEdgesIncidentTo(vertex)).size();
			int numEdgesFromTail = IntSetUtil.intersection(graph.getEdgesIncidentTo(tailId),
					graph.getEdgesIncidentTo(vertex)).size();
			int mul = numEdgesFromHead * numEdgesFromTail;
			oldSubGraphEdgeTriangles += (mul * numEdgesBetweenVertices);
			newSubGraphTriangles += (mul * newNumEdgesBetweenVertices);
		}

		return change==-1 ? (oldSubGraphEdgeTriangles-newSubGraphTriangles) : (newSubGraphTriangles-oldSubGraphEdgeTriangles);
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
     * @return - triple to remove.
     */
    @Override
    public TripleBaseSingleID getTripleRemove(ColouredGraph graph, List<UpdatableMetricResult> previousResult, long seed,
            boolean changeMetricValue) {
        TripleBaseSingleID tripleRemove = null;
        
        //Initializing edge id 
        int edgeId = -1;

        for (Integer edge : graph.getEdges()) {// Iterating edges

            // Get vertices incident to edge
            IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edge);

            int firstIncidentVertex = verticesIncidentToEdge.iterator().nextInt();
            int secondIncidentVertex = verticesIncidentToEdge.iterator().nextInt();

            if (MetricUtils.getVerticesInCommon(graph, firstIncidentVertex, secondIncidentVertex).size() > 0
                    && changeMetricValue) {
                // if vertices in common then edge triangle exists
                // Need to reduce the metric
                edgeId = edge;
                break;
            } else if (MetricUtils.getVerticesInCommon(graph, firstIncidentVertex, secondIncidentVertex).size() == 0
                    && !changeMetricValue) {
                // if vertices not in common then edge triangle doesn't exist
                edgeId = edge;
                break;
            }
        }
        
        if(edgeId != -1) {
            //If edge is found
            tripleRemove = new TripleBaseSingleID();
            tripleRemove.tailId = graph.getTailOfTheEdge(edgeId);
            tripleRemove.headId = graph.getHeadOfTheEdge(edgeId);
            tripleRemove.edgeId = edgeId;
            tripleRemove.edgeColour = graph.getEdgeColour(edgeId);
        }

        if (tripleRemove == null) { // If triple couldn't be found such that the node triangle metric can be
                                    // reduced.
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
     *            - boolean value If the variable is false, then it will generate a
     *            triple as per the implementation defined in the Generator class,
     *            else the triple is generated as per the default implementation.
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param changeMetricValue
     *            - boolean variable to indicate if the metric value should be increased
     *            or not. If the variable is true, then the method will return a
     *            triple that reduces the metric value.
     * @return - triple to add.
     */
    @Override
    public TripleBaseSingleID getTripleAdd(ColouredGraph graph, IGraphGeneration mGrphGenerator, boolean mProcessRandomly, List<UpdatableMetricResult> previousResult, boolean changeMetricValue) {
        TripleBaseSingleID tripleAdd = null;

        // Initializing edge id
        int edgeId = -1;

        for (Integer edge : graph.getEdges()) {// Iterating edges
            // Get vertices incident to edge
            IntSet verticesIncidentToEdge = graph.getVerticesIncidentToEdge(edge);
            int firstIncidentVertex = verticesIncidentToEdge.iterator().nextInt();
            int secondIncidentVertex = verticesIncidentToEdge.iterator().nextInt();

            int numberOfVerticesInCommon = MetricUtils
                    .getVerticesInCommon(graph, firstIncidentVertex, secondIncidentVertex).size();

            if (!changeMetricValue && numberOfVerticesInCommon > 0) {
                // Need to increase the metric
                // Vertices in common, thus edge triangle exist
                edgeId = edge;
            } else if (changeMetricValue && numberOfVerticesInCommon == 0) {
                // Need to decrease the metric or the metric should not be increased
                // No vertices in common, thus edge triangle does not exist
                edgeId = edge;
            }
            
            if (edgeId != -1) {
                // If edge is found
                tripleAdd = ((AbstractGraphGeneration) mGrphGenerator).getProposedTripleForHeadIdAndTailId(graph.getTailOfTheEdge(edgeId), graph.getHeadOfTheEdge(edgeId));
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
}
