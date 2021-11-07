package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class EdgeModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeModifier.class);

    private ColouredGraph graph;

    private List<SingleValueMetric> mLstMetrics;
    private ObjectDoubleOpenHashMap<String> mMapMetricValues;
    private ObjectDoubleOpenHashMap<String> mMapOrignalMetricValues;

    // Todo: why we store all try to removed/added edges??
    private List<TripleBaseSingleID> mLstRemovedEdges;
    private List<TripleBaseSingleID> mLstAddedEdges;

    // TODO: should be optional???
    private boolean isCountingEdgeTriangles = false;
    private boolean isCountingNodeTriangles = false;

    private HashMap<String, UpdatableMetricResult> mMapPrevMetricsResult; // Map to store previous metric results
    private HashMap<String, UpdatableMetricResult> mMapMetricsResultRemoveEdge; // Map to store results for remove
                                                                                // an edge
    private HashMap<String, UpdatableMetricResult> mMapMetricsResultAddEdge; // Map to store results for add an edge

    public EdgeModifier(ColouredGraph clonedGraph, List<SingleValueMetric> lstMetrics) {
        graph = clonedGraph;
        // list of metric
        mLstMetrics = lstMetrics;
        // initialize two list removed edges and added edges
        mLstRemovedEdges = new ArrayList<>();
        mLstAddedEdges = new ArrayList<>();

        // Initialize the UpdatableMetricResult for all metrics
        mMapPrevMetricsResult = new HashMap<>();
        mMapMetricsResultRemoveEdge = new HashMap<>();
        mMapMetricsResultAddEdge = new HashMap<>();

        // compute metric values
        computeMetricValues(graph, lstMetrics);
    }

    private void computeMetricValues(ColouredGraph graph, List<SingleValueMetric> lstMetrics) {

        LOGGER.info("Compute " + lstMetrics.size() + " metrics on the current mimic graph!");

        mMapMetricValues = new ObjectDoubleOpenHashMap<>();
        if (lstMetrics != null && lstMetrics.size() > 0) {

            for (SingleValueMetric metric : lstMetrics) {
                if (metric.getName().equalsIgnoreCase("#edgetriangles")) {
                    isCountingEdgeTriangles = true;
                } else if (metric.getName().equalsIgnoreCase("#nodetriangles")) {
                    isCountingNodeTriangles = true;
                }

                // Calling applyUpdatable
                UpdatableMetricResult metricResultTemp = metric.applyUpdatable(graph);

                double metVal = metricResultTemp.getResult();

                String name = metric.getName();
                LOGGER.info("Value of " + metric.getName() + " is " + metVal);
                // compute value for each of metrics
                mMapMetricValues.put(name, metVal);
                mMapPrevMetricsResult.put(name, metricResultTemp);
            }
        }
        if (!isCountingNodeTriangles) {
            mMapMetricValues.put("#nodetriangles", 0);
        }
        if (!isCountingEdgeTriangles) {
            mMapMetricValues.put("#edgetriangles", 0);
        }

        // create a backup map metric values
        mMapOrignalMetricValues = mMapMetricValues.clone();
    }

    public ColouredGraph getGraph() {
        return this.graph;
    }

    public ObjectDoubleOpenHashMap<String> tryToRemoveAnEdge(TripleBaseSingleID triple) {
        if (triple != null && triple.edgeId != -1 && triple.edgeColour != null && triple.tailId != -1
                && triple.headId != -1) {

            mLstRemovedEdges.add(triple);
            ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();

            for (SingleValueMetric metric : mLstMetrics) {
                if (metric.getName().equalsIgnoreCase("#nodetriangles")) {
                    UpdatableMetricResult result = metric.update(graph, triple, Operation.REMOVE,
                            mMapPrevMetricsResult.get(metric.getName()));
                    mMapMetricsResultRemoveEdge.put("#nodetriangles", result);

                    // todo: should optional??
                    if (isCountingNodeTriangles) {
                        mapMetricValues.put("#nodetriangles", result.getResult());
                    }
                }
                if (metric.getName().equalsIgnoreCase("#edgetriangles")) {
                    UpdatableMetricResult result = metric.update(graph, triple, Operation.REMOVE,
                            mMapPrevMetricsResult.get(metric.getName()));
                    mMapMetricsResultRemoveEdge.put("#edgetriangles", result);

                    // todo: should optional??
                    if (isCountingNodeTriangles) {
                        mapMetricValues.put("#edgetriangles", result.getResult());
                    }
                }
            }

            this.graph.removeEdge(triple.edgeId);

            for (SingleValueMetric metric : mLstMetrics) {
                if (!metric.getName().equalsIgnoreCase("#edgetriangles")
                        && !metric.getName().equalsIgnoreCase("#nodetriangles")) {

                    // Calling update method to get the metric values based on previous results
                    UpdatableMetricResult result = metric.update(graph, triple, Operation.REMOVE,
                            mMapPrevMetricsResult.get(metric.getName()));
                    mMapMetricsResultRemoveEdge.put(metric.getName(), result);
                    mapMetricValues.put(metric.getName(), result.getResult());
                }
            }

            // reverse the graph
            graph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

            return mapMetricValues;
        } else {
            LOGGER.warn("Invalid triple for removing an edge!");
            return null;
        }
    }

    public ObjectDoubleOpenHashMap<String> tryToAddAnEdge(TripleBaseSingleID triple) {
        if (triple != null && triple.edgeColour != null && triple.headId != -1 && triple.tailId != -1) {
            // add to list of added edges
            mLstAddedEdges.add(triple);

            ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();

            for (SingleValueMetric metric : mLstMetrics) {
                if (metric.getName().equalsIgnoreCase("#nodetriangles")) {
                    UpdatableMetricResult result = metric.update(graph, triple, Operation.ADD,
                            mMapPrevMetricsResult.get(metric.getName()));
                    mMapMetricsResultRemoveEdge.put("#nodetriangles", result);

                    // todo: should optional??
                    if (isCountingNodeTriangles) {
                        mapMetricValues.put("#nodetriangles", result.getResult());
                    }
                }
                if (metric.getName().equalsIgnoreCase("#edgetriangles")) {
                    UpdatableMetricResult result = metric.update(graph, triple, Operation.ADD,
                            mMapPrevMetricsResult.get(metric.getName()));
                    mMapMetricsResultRemoveEdge.put("#edgetriangles", result);

                    // todo: should optional??
                    if (isCountingNodeTriangles) {
                        mapMetricValues.put("#edgetriangles", result.getResult());
                    }
                }
            }

            graph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

            for (SingleValueMetric metric : mLstMetrics) {
                if (!metric.getName().equalsIgnoreCase("#edgetriangles")
                        && !metric.getName().equalsIgnoreCase("#nodetriangles")) {

                    // Calling update method to get the metric values based on previous results
                    UpdatableMetricResult result = metric.update(graph, triple, Operation.ADD,
                            mMapPrevMetricsResult.get(metric.getName()));
                    mMapMetricsResultRemoveEdge.put(metric.getName(), result);
                    mapMetricValues.put(metric.getName(), result.getResult());
                }
            }

            // reverse the graph
            graph.removeEdge(triple.edgeId);

            return mapMetricValues;
        } else {
            LOGGER.warn("Invalid triple for adding an edge!");
            return null;
        }
    }

    /**
     * execute removing an edge
     * 
     * @param newMetricValues the already calculated metric from trial
     */
    public void executeRemovingAnEdge(ObjectDoubleOpenHashMap<String> newMetricValues) {
        if (mLstRemovedEdges.size() > 0) {
            // store metric values got from trial
            updateMapMetricValues(newMetricValues);

            // get the last try to removed edge
            TripleBaseSingleID lastTriple = mLstRemovedEdges.get(mLstRemovedEdges.size() - 1);
            // remove the edge from graph again
            this.graph.removeEdge(lastTriple.edgeId);

            // Update the previously computed values
            mMapPrevMetricsResult = new HashMap<>(mMapMetricsResultRemoveEdge);
            mMapMetricsResultRemoveEdge.clear();

        }
    }

    /**
     * execute adding an edge
     * 
     * @param newMetricValues the already calculated metric from trial
     */
    public void executeAddingAnEdge(ObjectDoubleOpenHashMap<String> newMetricValues) {
        if (mLstAddedEdges.size() > 0) {
            // store metric values got from trial
            updateMapMetricValues(newMetricValues);
            // get the last added edge
            TripleBaseSingleID lastTriple = mLstAddedEdges.get(mLstAddedEdges.size() - 1);
            // add the edge to graph again
            this.graph.addEdge(lastTriple.tailId, lastTriple.headId, lastTriple.edgeColour);

            // Update the previously computed values
            mMapPrevMetricsResult = new HashMap<>(mMapMetricsResultAddEdge);
            mMapMetricsResultAddEdge.clear();
        }
    }

    private void updateMapMetricValues(ObjectDoubleOpenHashMap<String> newMetricValues) {
        mMapMetricValues = newMetricValues;
    }

    public ObjectDoubleOpenHashMap<String> getOriginalMetricValues() {
        return mMapOrignalMetricValues;
    }

    public ObjectDoubleOpenHashMap<String> getOptimizedMetricValues() {
        return mMapMetricValues;
    }

    public HashMap<String, UpdatableMetricResult> getmMapPrevMetricsResult() {
        return mMapPrevMetricsResult;
    }
}
