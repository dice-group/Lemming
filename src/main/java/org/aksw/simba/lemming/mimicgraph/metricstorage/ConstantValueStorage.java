package org.aksw.simba.lemming.mimicgraph.metricstorage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.ExpressionIterator;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Class that acts as a store of metrics and invariant expressions for the
 * reference graphs.
 *
 */
public class ConstantValueStorage implements Serializable {

	/** Logger object */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConstantValueStorage.class);

	/** ID for serialization */
	private static final long serialVersionUID = 1L;

	/** Name of the cache file */
	private final String METRIC_CACHE_NAME;

	/** Map from the dataset to the actual object */
	private Map<String, ValueStorage> mMapValueStorage;

	/** Dataset path to be considered */
	private String mDataSetPath;

	/** List of metrics to compute */
	private List<SingleValueMetric> metrics;

	/**
	 * Constructor.
	 * 
	 * @param cacheName   Name of the cache file
	 * @param datasetPath Path of the dataset
	 * @param metrics     Metrics to be computed
	 */
	public ConstantValueStorage(String cacheName, String datasetPath, List<SingleValueMetric> metrics) {
		this.METRIC_CACHE_NAME = cacheName;
		this.metrics = metrics;
		LOGGER.info("Load metric values and constants values from file: " + METRIC_CACHE_NAME);
		mDataSetPath = datasetPath;
		// load value from file
		loadData();
		if (mMapValueStorage == null) {
			mMapValueStorage = new HashMap<String, ValueStorage>();
		}
		// initialize a store for the dataset if not existing
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		if (mValueStorage == null) {
			mValueStorage = new ValueStorage();
			mMapValueStorage.put(mDataSetPath, mValueStorage);
		}
	}

	/**
	 * 
	 * @return true if the store contains the dataset's data
	 */
	public boolean havingData() {
		return mMapValueStorage.containsKey(mDataSetPath);
	}

	/**
	 * Adds computed metrics to the store
	 * 
	 * @param mapMetricValues Map from graph to metrics
	 * @param recalculateMetrics      Flag if we want to recalculate the metrics
	 */
	public void addMetricValues(Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues, boolean recalculateMetrics) {

		if (mapMetricValues != null) {

			ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
			Set<String> setKeyOfGraphs = mapMetricValues.keySet();

			for (String keyGraph : setKeyOfGraphs) {
				ObjectDoubleOpenHashMap<String> mapValues = mapMetricValues.get(keyGraph);
				if (mapValues != null) {
					Object[] arrMetricName = mapValues.keys;
					for (int i = 0; i < arrMetricName.length; i++) {
						if (mapValues.allocated[i]) {
							String metricName = (String) arrMetricName[i];
							double val = mapValues.get(metricName);
							mValueStorage.addMetricValues(keyGraph, metricName, val, recalculateMetrics);
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * @return true if the metrics exist in the store
	 */
	public boolean isComputableMetrics() {
		boolean isExistingMetric = true;
		if (metrics != null) {
			ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
			for (SingleValueMetric metric : metrics) {
				if (!mValueStorage.isExistingMetric(metric.getName())) {
					isExistingMetric = false;
					break;
				}
			}
		}
		// throw if metric is not found
		if (!isExistingMetric) {
			throw new IllegalStateException(
					"The list of metrics has some metrics that don't exist in the precomputed metric values.");
		}
		return isExistingMetric;
	}

	/**
	 * 
	 * @param graphs Coloured graphs
	 * @return Graph metric vectors
	 */
	@SuppressWarnings("unchecked")
	public ObjectDoubleOpenHashMap<String>[] getGraphMetricsVector(ColouredGraph[] graphs) {
		if (graphs != null && graphs.length > 0 && metrics != null & metrics.size() > 0) {

			ObjectDoubleOpenHashMap<String> graphVectors[] = new ObjectDoubleOpenHashMap[graphs.length];
			int i = 0;
			for (ColouredGraph grph : graphs) {
				ObjectDoubleOpenHashMap<String> objMapMetricValues = getMetricValues(grph);
				if (objMapMetricValues != null) {
					graphVectors[i] = objMapMetricValues;
				} else {
					graphVectors[i] = new ObjectDoubleOpenHashMap<String>();
				}
				i++;
			}
			return graphVectors;
		}
		return null;
	}

	/**
	 * Add value of expressions to the store, uses the <#vertices-#edges> as key to
	 * the graph
	 * 
	 * @param expr     Expression to be saved
	 * @param grph     Graph
	 * @param constVal Expression value to be saved
	 */
	public void addConstantValue(Expression expr, ColouredGraph grph, double constVal) {
		if (grph != null && expr != null) {
			ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
			String keyOfGraph = generateGraphKey(grph);
			mValueStorage.addConstantValue(expr, keyOfGraph, constVal);
		}
	}

	/**
	 * Setter for mapMetricValues
	 * 
	 * @param mapMetricValues
	 */
	public void setMetricValues(Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues) {
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		mValueStorage.setMetricValues(mapMetricValues);
	}

	/**
	 * Setter for mapConstantValues
	 * 
	 * @param mapConstantValues
	 */
	public void setConstantValues(Map<Expression, Map<String, Double>> mapConstantValues) {
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		mValueStorage.setConstantValues(mapConstantValues);
	}

	/**
	 * Getter for metric values of given graph
	 * 
	 * @param grph Coloured graph
	 * @return Metric values
	 */
	public ObjectDoubleOpenHashMap<String> getMetricValues(ColouredGraph grph) {
		String keyOfGraph = generateGraphKey(grph);
		// get list of metric name
		Set<String> metricNames = new HashSet<String>();
		for (SingleValueMetric metric : metrics) {
			metricNames.add(metric.getName());
		}

		return getMetricValues(keyOfGraph, metricNames);
	}

	/**
	 * Getter for selected metric values
	 * 
	 * @param keyOfGraph  Graph key generated by generateGraphKey()
	 * @param metricNames Set of metrics you want to retrieve
	 * @return
	 */
	public ObjectDoubleOpenHashMap<String> getMetricValues(String keyOfGraph, Set<String> metricNames) {
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		Map<String, Double> mapMetricValues = mValueStorage.getMetricValues(keyOfGraph);

		ObjectDoubleOpenHashMap<String> objMapMetricValues = new ObjectDoubleOpenHashMap<String>();
		if (mapMetricValues != null) {
			Set<String> setMetricNames = mapMetricValues.keySet();
			for (String metricName : setMetricNames) {
				double val = mapMetricValues.get(metricName);

				objMapMetricValues.put(metricName, val);
			}
		}

		return objMapMetricValues;
	}

//	public Map<String, ObjectDoubleOpenHashMap<String>> getMapMetricValues(){
//		Map<String, Map<String, Double>> mapMetricValues = mValueStorage.getMapMetricValues();
//		
//		Map<String, ObjectDoubleOpenHashMap<String>> mapObjMetricValues = new HashMap<String, ObjectDoubleOpenHashMap<String>>();
//		Set<String> setKeyOfGraphs = mapMetricValues.keySet();
//		
//		for(String keyOfGraph : setKeyOfGraphs){
//			ObjectDoubleOpenHashMap<String> objMetricValues = getMetricValues(keyOfGraph);
//			mapObjMetricValues.put(keyOfGraph, objMetricValues);
//		}
//		
//		return mapObjMetricValues;
//	}

	/**
	 * Getter for the constant values
	 * 
	 * @return Map from expressions to the constant values
	 */
	public Map<Expression, Map<String, Double>> getMapConstantValues() {
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		return mValueStorage.getMapConstantValues();
	}

	/**
	 * Getter for the constant values keys, i.e., the expressions
	 * 
	 * @return Set of expressions
	 */
	public Set<Expression> getConstantExpressions() {
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		return mValueStorage.getMapConstantValues().keySet();
	}

	/**
	 * Saves store to file
	 */
	public void storeData() {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(METRIC_CACHE_NAME));) {
			out.writeObject(mMapValueStorage);
			LOGGER.info("Object has been saved");
		} catch (IOException ex) {
			LOGGER.warn("IOException: " + ex.getMessage());
		}
	}

	/**
	 * Reads store from file
	 */
	@SuppressWarnings("unchecked")
	public void loadData() {
		// Reading the object from a file
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(METRIC_CACHE_NAME));) {
			// Method for deserialization of object
			mMapValueStorage = (Map<String, ValueStorage>) in.readObject();
			LOGGER.info("Object has been loaded");
		} catch (IOException ex) {
			LOGGER.warn("IOException: " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			LOGGER.warn("IOException: " + ex.getMessage());
		}
	}

	/**
	 * Getter
	 * 
	 * @return Metric values of current dataset
	 */
	public Map<String, Map<String, Double>> getMapMetricValues() {
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		return mValueStorage.getMapMetricValues();
	}

	/**
	 * Generates the graph key as <#vertices-#edges> to be used in the store map
	 * 
	 * @param graph Coloured graph
	 * @return #vertices-#edges key of given graph
	 */
	public static String generateGraphKey(ColouredGraph graph) {
		return graph.getGraph().getNumberOfVertices() + "-" + graph.getGraph().getNumberOfEdges();
	}

	/**
	 * The method returns the list of metrics which are present in characteristics
	 * expressions.
	 * 
	 * @param lstMetrics - The list which contains all the input metrics.
	 * @return List of metrics
	 */
	public List<SingleValueMetric> getMetricsOfExpressions() {
		List<SingleValueMetric> metrics = new ArrayList<>();// List which will contain metrics present in Expressions
		Set<Expression> constantExpressions = getConstantExpressions();
		Set<String> expressionsSet = new HashSet<>(); // Set to store atomic expression

		// Iterate over all expressions and add atomic expression in set.
		for (Expression expression : constantExpressions) {
			ExpressionIterator iterator = new ExpressionIterator(expression);
			while (iterator.hasNext()) {
				Expression subExpression = iterator.next();
				if (subExpression.isAtomic()) {
					expressionsSet.add(subExpression.toString());
				}
			}
		}

		// Iterate over input list of metrics and check which metrics are present in
		// expressions
		for (SingleValueMetric metric : metrics) {
			if (expressionsSet.contains(metric.getName())) {
				metrics.add(metric);
			}
		}

		return metrics;
	}

	/**
	 * get value of each metric applied on each graph
	 * 
	 * @param origGrphs
	 * @param lstMetrics
	 * @return
	 */
	public Map<String, ObjectDoubleOpenHashMap<String>> getMapMetricValues(ColouredGraph origGrphs[]) {
		Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues = new HashMap<String, ObjectDoubleOpenHashMap<String>>();

		for (ColouredGraph grph : origGrphs) {
			String key = ConstantValueStorage.generateGraphKey(grph);
			LOGGER.info("Consider graph: " + key);
			ObjectDoubleOpenHashMap<String> metricValues = MetricUtils.calculateGraphMetrics(grph, metrics);
			mapMetricValues.put(key, metricValues);
		}
		return mapMetricValues;
	}

	/**
	 * Compute metrics for all graphs
	 * 
	 * @param graphs    Array of coloured graphs
	 * @return
	 */
	public ObjectDoubleOpenHashMap<String>[] computeMetrics(ColouredGraph[] graphs) {
		Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues = getMapMetricValues(graphs);
		if (mapMetricValues == null) {
			setMetricValues(mapMetricValues);
		} else {
			addMetricValues(mapMetricValues, false);
		}
		return getGraphMetricsVector(graphs);
	}

	/**
	 * @return List of metrics
	 */
	public List<SingleValueMetric> getMetrics() {
		return metrics;
	}
	
	public ValueStorage getValueStorage() {
		return mMapValueStorage.get(mDataSetPath);
	}
	
}
