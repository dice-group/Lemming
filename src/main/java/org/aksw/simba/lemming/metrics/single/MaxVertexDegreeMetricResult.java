package org.aksw.simba.lemming.metrics.single;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * The class stores the candidate's set and their metric values, which is used
 * for computing the max vertex degrees for different metrics.
 * 
 * @author Atul
 *
 */
public class MaxVertexDegreeMetricResult extends SimpleMetricResult {

	private HashMap<GRAPHOPERATION, IntSet> mMapCandidatesMetric = new HashMap<>();
	// Map for storing candidate vertices for Max Degree metric computation

	private HashMap<GRAPHOPERATION, Double> mMapCandidatesMetricValues = new HashMap<>();
	// Map for storing max degree of vertices

	/**
	 * Initializes the Maps, metric name, and result.
	 * 
	 * @param metricName - Name of the metric.
	 * @param result     - Metric value.
	 */
	public MaxVertexDegreeMetricResult(String metricName, double result) {
		super(metricName, result);
		List<GRAPHOPERATION> metrics = new ArrayList<>();
		metrics.add(GRAPHOPERATION.RemoveAnEdgeIndegree);
		metrics.add(GRAPHOPERATION.RemoveAnEdgeOutdegree);
		metrics.add(GRAPHOPERATION.AddAnEdgeIndegree);
		metrics.add(GRAPHOPERATION.AddAnEdgeOutdegree);

		// Initialize Hash map for candidate vertices (MaxVertexDegreeMetric).
		mMapCandidatesMetric = new HashMap<>();
		for (GRAPHOPERATION metric : metrics) {
			mMapCandidatesMetric.put(metric, new IntOpenHashSet());
		}

		// Initialize Hash map for storing maximum vertex degrees for different cases.
		mMapCandidatesMetricValues = new HashMap<>();
		for (GRAPHOPERATION metric : metrics) {
			mMapCandidatesMetricValues.put(metric, 0.0);
		}
	}

	/**
	 * ENUM for different graph operations.
	 * 
	 * @author Atul
	 *
	 */
	public static enum GRAPHOPERATION {
		RemoveAnEdgeIndegree, RemoveAnEdgeOutdegree, AddAnEdgeIndegree, AddAnEdgeOutdegree
	}

	/**
	 * Update the temporary candidate map.
	 * 
	 * @param mMapCandidatesMetricTemp1 - Input map containing the candidates that
	 *                                  needs to be updated.
	 * @param key                       - GRAPHOPERATION ENUM denoting the key that
	 *                                  needs to be updated.
	 */
	public void setmMapCandidatesMetric(HashMap<GRAPHOPERATION, IntSet> mMapCandidatesMetricTemp1) {
		for (GRAPHOPERATION graphOptKey : GRAPHOPERATION.values()) {
			mMapCandidatesMetric.replace(graphOptKey, mMapCandidatesMetricTemp1.get(graphOptKey));
		}
	}

	/**
	 * Returns the current temporary Map which stores the metric values.
	 * 
	 * @return - Map.
	 */
	public HashMap<GRAPHOPERATION, Double> getmMapCandidatesMetricValues() {
		return mMapCandidatesMetricValues;
	}

	/**
	 * Update the metric values in temporary map.
	 * 
	 * @param mMapCandidatesMetricValuesTemp1 - - Input map containing the metric
	 *                                        values that needs to be updated.
	 * @param key                             - GRAPHOPERATION ENUM denoting the key
	 *                                        that needs to be updated.
	 */
	public void setmMapCandidatesMetricValues(HashMap<GRAPHOPERATION, Double> mMapCandidatesMetricValuesTemp1) {
		for (GRAPHOPERATION graphOptKey : GRAPHOPERATION.values()) {
			mMapCandidatesMetricValues.replace(graphOptKey, mMapCandidatesMetricValuesTemp1.get(graphOptKey));
		}
	}

	/**
	 * Returns the map containing the candiate set.
	 * 
	 * @return - Map.
	 */
	public HashMap<GRAPHOPERATION, IntSet> getmMapCandidatesMetric() {
		return mMapCandidatesMetric;
	}

}
