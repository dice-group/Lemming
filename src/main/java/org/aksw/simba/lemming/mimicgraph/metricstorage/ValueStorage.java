package org.aksw.simba.lemming.mimicgraph.metricstorage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.Expression;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class ValueStorage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	//1st key: expression, 2nd key: key of graph, value is the constant value of the graph
	private Map<Expression, Map<String, Double>> mMapConstantValues;
	
	//1st key: key of graph, 2nd key: metric name, value is the value of the metric
	private Map<String, Map<String, Double>> mMapMetricValues;
	
	public ValueStorage(){
		mMapConstantValues = new HashMap<Expression, Map<String, Double>>();
		mMapMetricValues = new HashMap<String, Map<String, Double>>();
	}
	
	public void setConstantValues(Map<Expression, Map<String, Double>> mapConstantValues){
		mMapConstantValues = mapConstantValues;
	}
	
	public boolean isExistingMetric(String metricName){
		boolean isExist = false;
		if(mMapMetricValues != null && mMapMetricValues.size() >0){
			Set<String> setKeyOfGraphs = mMapMetricValues.keySet();
			for(String key: setKeyOfGraphs){
				Map<String, Double> mapMetricValues = mMapMetricValues.get(key);
				if(mapMetricValues.containsKey(metricName)){
					isExist = true;
					break;
				}
			}
		}
		return isExist;
	}
	
	public void setMetricValues(Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues){
		if(mapMetricValues != null){
			Set<String> setOfkeyOfGraphs = mapMetricValues.keySet();
			for(String keyOfGraph : setOfkeyOfGraphs){
				ObjectDoubleOpenHashMap<String> objMapValues = mapMetricValues.get(keyOfGraph);
				
				Map<String, Double> newMapValues = new HashMap<String, Double>();
				Object[] arrOfMetricNames = objMapValues.keys;
				for(int i = 0 ; i < arrOfMetricNames.length; i++){
					if(objMapValues.allocated[i]){
						String metricName = (String) arrOfMetricNames[i];
						double val = objMapValues.get(metricName);
						
						newMapValues.put(metricName, val);
					}
				}
				
				//put to store
				mMapMetricValues.put(keyOfGraph, newMapValues);
			}
		}
	}
	
	public Map<Expression, Map<String, Double>> getMapConstantValues(){
		return mMapConstantValues;
	}
	
	public Map<String, Map<String, Double>> getMapMetricValues(){
		return mMapMetricValues;
	}

	public Map<String, Double> getMetricValues(String key){
		return mMapMetricValues.get(key);
	}
	
	public void addConstantValue(Expression expr, String keyOfGraph, double constVal){
		Map<String, Double> mapValues = mMapConstantValues.get(expr);
		if(mapValues == null){
			mapValues = new HashMap<String, Double>();
			mMapConstantValues.put(expr, mapValues);
		}
		
		mapValues.put(keyOfGraph, constVal);
	}
	
	public void addMetricValues(String keyGraph, String metricName, double val, boolean isOverride){
		if(mMapMetricValues == null)
		{
			mMapMetricValues = new HashMap<String, Map<String, Double>>();
		}
		
		Map<String, Double> mapValues = mMapMetricValues.get(keyGraph);
		if(mapValues == null){
			mapValues = new HashMap<String, Double>();
			mMapMetricValues.put(keyGraph, mapValues);
		}
		
		if(mapValues.containsKey(metricName) && !isOverride){
			return;
		}
		mapValues.put(metricName,val);
	}
}
