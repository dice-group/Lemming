package org.aksw.simba.lemming.metrics.single;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.simba.lemming.metrics.single.edgemanipulation.VertexDegrees;

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

	private HashMap<GRAPHOPERATION, IntSet> mMapCandidatesMetricTemp;
	// Temp Map for storing candidate vertices for metric computation

	private HashMap<GRAPHOPERATION, Double> mMapCandidatesMetricValuesTemp;
	// Temp Map for storing metric values

	VertexDegrees mVertexDegrees;

	private HashMap<GRAPHOPERATION, IntSet> mMapCandidatesMetric = new HashMap<>();
	// Map for storing candidate vertices for Max Degree metric computation

	private HashMap<GRAPHOPERATION, Double> mMapCandidatesMetricValues = new HashMap<>();
	// Map for storing max degree of vertices

	/**
	 * Initializes the Maps, metric name, and result.
	 * 
	 * @param metricName
	 *            - Name of the metric.
	 * @param result
	 *            - Metric value.
	 */
	public MaxVertexDegreeMetricResult(String metricName, double result) {
		super(metricName, result);
		List<GRAPHOPERATION> metrics = new ArrayList<>();
		metrics.add(GRAPHOPERATION.RemoveAnEdgeIndegree);
		metrics.add(GRAPHOPERATION.RemoveAnEdgeOutdegree);
		metrics.add(GRAPHOPERATION.AddAnEdgeIndegree);
		metrics.add(GRAPHOPERATION.AddAnEdgeOutdegree);

		// Initialize Hash map for candidate vertices (MaxVertexDegreeMetric).
		mMapCandidatesMetricTemp = new HashMap<>();
		mMapCandidatesMetric = new HashMap<>();
		for (GRAPHOPERATION metric : metrics) {
			mMapCandidatesMetric.put(metric, new IntOpenHashSet());
			mMapCandidatesMetricTemp.put(metric, new IntOpenHashSet());
		}

		// Initialize Hash map for storing maximum vertex degrees for different cases.
		mMapCandidatesMetricValuesTemp = new HashMap<>();
		mMapCandidatesMetricValues = new HashMap<>();
		for (GRAPHOPERATION metric : metrics) {
			mMapCandidatesMetricValues.put(metric, 0.0);
			mMapCandidatesMetricValuesTemp.put(metric, 0.0);
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
	 * Returns the current temporary Map which stores the candidate vertices.
	 * 
	 * @return - Map.
	 */
	public HashMap<GRAPHOPERATION, IntSet> getmMapCandidatesMetricTemp() {
		return mMapCandidatesMetricTemp;
	}

	/**
	 * Update the temporary candidate map.
	 * 
	 * @param mMapCandidatesMetricTemp1
	 *            - Input map containing the candidates that needs to be updated.
	 * @param key
	 *            - GRAPHOPERATION ENUM denoting the key that needs to be updated.
	 */
	public void setmMapCandidatesMetricTemp(HashMap<GRAPHOPERATION, IntSet> mMapCandidatesMetricTemp1,
			GRAPHOPERATION key) {
		mMapCandidatesMetricTemp.replace(key, mMapCandidatesMetricTemp1.get(key));
	}

	/**
	 * Returns the current temporary Map which stores the metric values.
	 * 
	 * @return - Map.
	 */
	public HashMap<GRAPHOPERATION, Double> getmMapCandidatesMetricValuesTemp() {
		return mMapCandidatesMetricValuesTemp;
	}

	/**
	 * Update the metric values in temporary map.
	 * 
	 * @param mMapCandidatesMetricValuesTemp1
	 *            - - Input map containing the metric values that needs to be
	 *            updated.
	 * @param key
	 *            - GRAPHOPERATION ENUM denoting the key that needs to be updated.
	 */
	public void setmMapCandidatesMetricValuesTemp(HashMap<GRAPHOPERATION, Double> mMapCandidatesMetricValuesTemp1,
			GRAPHOPERATION key) {
		mMapCandidatesMetricValuesTemp.replace(key, mMapCandidatesMetricValuesTemp1.get(key));
	}

	/**
	 * Returns the map containing the candiate set.
	 * 
	 * @return - Map.
	 */
	public HashMap<GRAPHOPERATION, IntSet> getmMapCandidatesMetric() {
		return mMapCandidatesMetric;
	}

	/**
	 * Storing values from temporary maps for remove an edge operation.
	 * 
	 * @param mMapCandidatesMetric1
	 *            - Map storing the candidates.
	 * @param mMapCandidatesMetricValues1
	 *            - Map storing the candidate values.
	 */
	public void setCandidatesMetricRemoveAnEdge(HashMap<GRAPHOPERATION, IntSet> mMapCandidatesMetric1,
			HashMap<GRAPHOPERATION, Double> mMapCandidatesMetricValues1) {

		mMapCandidatesMetric.replace(GRAPHOPERATION.RemoveAnEdgeIndegree,
				mMapCandidatesMetric1.get(GRAPHOPERATION.RemoveAnEdgeIndegree));
		mMapCandidatesMetric.replace(GRAPHOPERATION.RemoveAnEdgeOutdegree,
				mMapCandidatesMetric1.get(GRAPHOPERATION.RemoveAnEdgeOutdegree));
		mMapCandidatesMetricValues.replace(GRAPHOPERATION.RemoveAnEdgeIndegree,
				mMapCandidatesMetricValues1.get(GRAPHOPERATION.RemoveAnEdgeIndegree));
		mMapCandidatesMetricValues.replace(GRAPHOPERATION.RemoveAnEdgeOutdegree,
				mMapCandidatesMetricValues1.get(GRAPHOPERATION.RemoveAnEdgeOutdegree));

	}

	/**
	 * Storing values from temporary maps for add an edge operation.
	 * 
	 * @param mMapCandidatesMetric1
	 *            - Map storing the candidates.
	 * @param mMapCandidatesMetricValues1
	 *            - Map storing the candidate values.
	 */
	public void setCandidatesMetricAddAnEdge(HashMap<GRAPHOPERATION, IntSet> mMapCandidatesMetric1,
			HashMap<GRAPHOPERATION, Double> mMapCandidatesMetricValues1) {

		mMapCandidatesMetric.replace(GRAPHOPERATION.AddAnEdgeIndegree,
				mMapCandidatesMetric1.get(GRAPHOPERATION.AddAnEdgeIndegree));
		mMapCandidatesMetric.replace(GRAPHOPERATION.AddAnEdgeOutdegree,
				mMapCandidatesMetric1.get(GRAPHOPERATION.AddAnEdgeOutdegree));
		mMapCandidatesMetricValues.replace(GRAPHOPERATION.AddAnEdgeIndegree,
				mMapCandidatesMetricValues1.get(GRAPHOPERATION.AddAnEdgeIndegree));
		mMapCandidatesMetricValues.replace(GRAPHOPERATION.AddAnEdgeOutdegree,
				mMapCandidatesMetricValues1.get(GRAPHOPERATION.AddAnEdgeOutdegree));

	}

	/**
	 * Returns the map containing the previously computed metric values.
	 * 
	 * @return - Map.
	 */
	public HashMap<GRAPHOPERATION, Double> getmMapCandidatesMetricValues() {
		return mMapCandidatesMetricValues;
	}

}
