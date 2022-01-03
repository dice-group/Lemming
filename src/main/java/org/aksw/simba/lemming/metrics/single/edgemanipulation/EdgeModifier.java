package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import javax.annotation.Nonnull;

public class EdgeModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeModifier.class);

    private ColouredGraph graph;

    private List<SingleValueMetric> mLstMetrics;
    private ObjectDoubleOpenHashMap<String> mMapMetricValues;
    private ObjectDoubleOpenHashMap<String> mMapOriginalMetricValues;

    private TripleBaseSingleID tryToRemovedEdge;
    private TripleBaseSingleID tryToAddedEdge;

    private HashMap<String, UpdatableMetricResult> mMapPrevMetricsResult; // Map to store previous metric results
    private HashMap<String, UpdatableMetricResult> mMapMetricsResultRemoveEdge; // Map to store results for remove
                                                                                // an edge
    private HashMap<String, UpdatableMetricResult> mMapMetricsResultAddEdge; // Map to store results for add an edge

    public EdgeModifier(ColouredGraph clonedGraph, List<SingleValueMetric> lstMetrics) {
        graph = clonedGraph;
        // list of metric
        mLstMetrics = lstMetrics;

        // Initialize the UpdatableMetricResult for all metrics
        mMapPrevMetricsResult = new HashMap<>();
        mMapMetricsResultRemoveEdge = new HashMap<>();
        mMapMetricsResultAddEdge = new HashMap<>();

        // compute metric values
        computeMetricValues(graph, lstMetrics);
    }

    private void computeMetricValues(ColouredGraph graph, @Nonnull List<SingleValueMetric> lstMetrics) {

        LOGGER.info("Compute " + lstMetrics.size() + " metrics on the current mimic graph!");

        mMapMetricValues = new ObjectDoubleOpenHashMap<>();
        if (lstMetrics.size() > 0) {

            for (SingleValueMetric metric : lstMetrics) {
                // Calling applyUpdatable
                UpdatableMetricResult metricResultTemp = metric.applyUpdatable(graph);

                double metVal = metricResultTemp.getResult();

                String name = metric.getName();
                LOGGER.info("Value of " + metric.getName() + " is " + metVal);

                mMapMetricValues.put(name, metVal);
                mMapPrevMetricsResult.put(name, metricResultTemp);
            }
        }
        // create a backup map metric values
        mMapOriginalMetricValues = mMapMetricValues.clone();
    }

    public ColouredGraph getGraph() {
        return this.graph;
    }

    public ObjectDoubleOpenHashMap<String> tryToRemoveAnEdge(TripleBaseSingleID triple) {
        if (triple != null && triple.edgeId != -1 && triple.edgeColour != null && triple.tailId != -1
                && triple.headId != -1) {

            tryToRemovedEdge = triple;
            ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();
            //remove an edge
            graph.removeEdge(triple.edgeId);

            for (SingleValueMetric metric : mLstMetrics) {
                // Calling update method to get the metric values based on previous results
                UpdatableMetricResult result = metric.update(graph, triple, Operation.REMOVE,
                        mMapPrevMetricsResult.get(metric.getName()));
                mMapMetricsResultRemoveEdge.put(metric.getName(), result);
                mapMetricValues.put(metric.getName(), result.getResult());
            }
            //reverse the graph, note: the edgeId could be changed after reversion
            triple.edgeId = graph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

            return mapMetricValues;
        } else {
            LOGGER.warn("Invalid triple for removing an edge!");
            return null;
        }
    }

    public ObjectDoubleOpenHashMap<String> tryToAddAnEdge(TripleBaseSingleID triple) {
        if (triple != null && triple.edgeColour != null && triple.headId != -1 && triple.tailId != -1) {

            tryToAddedEdge = triple;
            ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();

            //add an edge, note: the edgeId could be changed after adding triple into graph
            triple.edgeId = graph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

            for (SingleValueMetric metric : mLstMetrics) {
                // Calling update method to get the metric values based on previous results
                UpdatableMetricResult result = metric.update(graph, triple, Operation.ADD,
                        mMapPrevMetricsResult.get(metric.getName()));
                mMapMetricsResultAddEdge.put(metric.getName(), result);
                mapMetricValues.put(metric.getName(), result.getResult());
            }

            //reverse the graph
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
        if (tryToRemovedEdge != null) {
            // store metric values got from trial
            updateMapMetricValues(newMetricValues);

            // remove the edge from graph again
            this.graph.removeEdge(tryToRemovedEdge.edgeId);
            tryToRemovedEdge = null;

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
        if (tryToAddedEdge != null) {
            // store metric values got from trial
            updateMapMetricValues(newMetricValues);

            // add the edge to graph again
            this.graph.addEdge(tryToAddedEdge.tailId, tryToAddedEdge.headId, tryToAddedEdge.edgeColour);
            tryToAddedEdge = null;

            // Update the previously computed values
            mMapPrevMetricsResult = new HashMap<>(mMapMetricsResultAddEdge);
            mMapMetricsResultAddEdge.clear();
        }
    }

    private void updateMapMetricValues(ObjectDoubleOpenHashMap<String> newMetricValues) {
        mMapMetricValues = newMetricValues;
    }

    public ObjectDoubleOpenHashMap<String> getOriginalMetricValues() {
        return mMapOriginalMetricValues;
    }

    public ObjectDoubleOpenHashMap<String> getOptimizedMetricValues() {
        return mMapMetricValues;
    }

    public HashMap<String, UpdatableMetricResult> getMapPrevMetricsResult() {
        return mMapPrevMetricsResult;
    }
    
    public List<UpdatableMetricResult> getListPrevMetricsResult(){
        List<UpdatableMetricResult> prevMetricResultList = new ArrayList<>();
        
        for(String metricName : mMapPrevMetricsResult.keySet()) {
            prevMetricResultList.add(mMapPrevMetricsResult.get(metricName));
        }
        
        return prevMetricResultList;
    }

    public List<SingleValueMetric> getmLstMetrics() {
        return mLstMetrics;
    }

}
