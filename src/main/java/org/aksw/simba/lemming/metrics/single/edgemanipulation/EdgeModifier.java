package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import java.util.HashMap;
import java.util.List;

import org.aksw.simba.lemming.AddEdgeDecorator;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphDecorator;
import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.RemoveEdgeDecorator;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class EdgeModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeModifier.class);

    private IColouredGraph graph;

    private List<SingleValueMetric> mLstMetrics;
    private ObjectDoubleOpenHashMap<String> mMapMetricValues;
    private ObjectDoubleOpenHashMap<String> mMapOriginalMetricValues;

    private HashMap<String, UpdatableMetricResult> mMapPrevMetricsResult; // Map to store previous metric results
    private HashMap<String, UpdatableMetricResult> mMapMetricsResultRemoveEdge; // Map to store results for remove
                                                                                // an edge
    private HashMap<String, UpdatableMetricResult> mMapMetricsResultAddEdge; // Map to store results for add an edge

    private ColouredGraphDecorator mRemoveEdgeDecorator;
    private ColouredGraphDecorator mAddEdgeDecorator;

    public EdgeModifier(ColouredGraph clonedGraph, List<SingleValueMetric> lstMetrics) {
        graph = clonedGraph;
        // list of metric
        mLstMetrics = lstMetrics;

        // Initialize the UpdatableMetricResult for all metrics
        mMapPrevMetricsResult = new HashMap<>();
        mMapMetricsResultRemoveEdge = new HashMap<>();
        mMapMetricsResultAddEdge = new HashMap<>();

        mAddEdgeDecorator = new AddEdgeDecorator(graph, true);
        mRemoveEdgeDecorator = new RemoveEdgeDecorator(graph, false);

        // compute metric values
        computeMetricValues(graph, lstMetrics);
    }

    private void computeMetricValues(IColouredGraph graph, List<SingleValueMetric> lstMetrics) {

        LOGGER.info("Compute " + lstMetrics.size() + " metrics on the current mimic graph!");

        mMapMetricValues = new ObjectDoubleOpenHashMap<>();
        if (lstMetrics.size() > 0) {
            ColouredGraphDecorator mGraphDecorator = new ColouredGraphDecorator(graph);
            for (SingleValueMetric metric : lstMetrics) {
                // Calling applyUpdatable
                UpdatableMetricResult metricResultTemp = metric.applyUpdatable(mGraphDecorator);

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

    /**
     * Returns decorator object for Edge addition thread
     */
    public ColouredGraphDecorator getAddEdgeDecorator() {
        return this.mAddEdgeDecorator;
    }

    /**
     * Returns decorator object for Edge removal thread
     */
    public ColouredGraphDecorator getRemoveEdgeDecorator() {
        return this.mRemoveEdgeDecorator;
    }

    public ColouredGraph getGraph() {
        return (ColouredGraph) this.graph;
    }

    /**
     * Update the {@link IColouredGraph} object and reset the triple stored in each
     * decorator object after an iteration has completed
     */
    public void updateDecorators() {

        this.mAddEdgeDecorator.setGraph(this.graph);
        this.mAddEdgeDecorator.setTriple(null);

        this.mRemoveEdgeDecorator.setGraph(this.graph);
        this.mAddEdgeDecorator.setTriple(null);
    }

    public ObjectDoubleOpenHashMap<String> tryToRemoveAnEdge(TripleBaseSingleID triple) {
        if (triple != null && triple.edgeId != -1 && triple.edgeColour != null && triple.tailId != -1
                && triple.headId != -1) {

            this.mRemoveEdgeDecorator.setTriple(triple);
            ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();

            for (SingleValueMetric metric : mLstMetrics) {
                // Calling update method to get the metric values based on previous results
                UpdatableMetricResult result = metric.update(this.mRemoveEdgeDecorator, triple, Operation.REMOVE,
                        mMapPrevMetricsResult.get(metric.getName()));
                mMapMetricsResultRemoveEdge.put(metric.getName(), result);
                mapMetricValues.put(metric.getName(), result.getResult());
            }

            return mapMetricValues;
        } else {
            LOGGER.warn("Invalid triple for removing an edge!");
            return null;
        }
    }

    public ObjectDoubleOpenHashMap<String> tryToAddAnEdge(TripleBaseSingleID triple) {
        if (triple != null && triple.edgeColour != null && triple.headId != -1 && triple.tailId != -1) {
            this.mAddEdgeDecorator.setTriple(triple);

            ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();

            for (SingleValueMetric metric : mLstMetrics) {
                // Calling update method to get the metric values based on previous results
                UpdatableMetricResult result = metric.update(this.mAddEdgeDecorator, triple, Operation.ADD,
                        mMapPrevMetricsResult.get(metric.getName()));
                mMapMetricsResultAddEdge.put(metric.getName(), result);
                mapMetricValues.put(metric.getName(), result.getResult());
            }

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
        // store metric values got from trial
        updateMapMetricValues(newMetricValues);

        // get the last try to removed edge
        TripleBaseSingleID lastTriple = this.mRemoveEdgeDecorator.getTriple();
        // remove the edge from graph again
        this.graph.removeEdge(lastTriple.edgeId);

        // Update the previously computed values
        mMapPrevMetricsResult = new HashMap<>(mMapMetricsResultRemoveEdge);
        mMapMetricsResultRemoveEdge.clear();
        updateDecorators();
    }

    /**
     * execute adding an edge
     * 
     * @param newMetricValues the already calculated metric from trial
     */
    public void executeAddingAnEdge(ObjectDoubleOpenHashMap<String> newMetricValues) {
        // store metric values got from trial
        updateMapMetricValues(newMetricValues);
        // get the last added edge
        TripleBaseSingleID lastTriple = this.mAddEdgeDecorator.getTriple();
        // add the edge to graph again
        this.graph.addEdge(lastTriple.tailId, lastTriple.headId, lastTriple.edgeColour);

        // Update the previously computed values
        mMapPrevMetricsResult = new HashMap<>(mMapMetricsResultAddEdge);
        mMapMetricsResultAddEdge.clear();
        updateDecorators();
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

    public HashMap<String, UpdatableMetricResult> getmMapPrevMetricsResult() {
        return mMapPrevMetricsResult;
    }
}
