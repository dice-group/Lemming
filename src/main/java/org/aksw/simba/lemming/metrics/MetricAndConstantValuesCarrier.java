package org.aksw.simba.lemming.metrics;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.tools.RefinementTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class MetricAndConstantValuesCarrier implements Serializable	{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RefinementTest.class);
	private static final String METRIC_CACHE_NAME = "value_store.val"; 
	private Map<String, ValueStorage> mMapValueStorage ;
	
	private String mDataSetPath;
	
	public MetricAndConstantValuesCarrier(String datasetPath){
		//load value from file
		LOGGER.info("Load metric values and constants values from file: " + METRIC_CACHE_NAME);
		mDataSetPath = datasetPath;
		loadValues();
		if(mMapValueStorage == null){
			mMapValueStorage = new HashMap<String, ValueStorage>();	
		}
		
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		if(mValueStorage == null ){
			mValueStorage = new ValueStorage();
			mMapValueStorage.put(mDataSetPath, mValueStorage);
		}
	}
	
	public boolean havingData(){
		if(mMapValueStorage.containsKey(mDataSetPath))
			return true;
		return false;
	}
	
	
	public void addMetricValues(Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues, boolean isOverride){
		
		if(mapMetricValues!= null){
			
			ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
			Set<String> setKeyOfGraphs = mapMetricValues.keySet();
			
			for(String keyGraph : setKeyOfGraphs){
				ObjectDoubleOpenHashMap<String> mapValues = mapMetricValues.get(keyGraph);
				if(mapValues!= null){
					Object[] arrMetricName = mapValues.keys;
					for(int i = 0 ; i < arrMetricName.length; i++){
						if(mapValues.allocated[i]){
							String metricName = (String) arrMetricName[i];
							double val = mapValues.get(metricName);
							mValueStorage.addMetricValues(keyGraph, metricName, val ,isOverride);
						}
					}
				}
			}
		}
	}
	
	public boolean isComputableMetrics(List<SingleValueMetric> lstMetrics){
		boolean isExistingMetric = true;
		if(lstMetrics != null){
			ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
			for(SingleValueMetric metric: lstMetrics){
				if(!mValueStorage.isExistingMetric(metric.getName())){
					isExistingMetric = false;
					break;
				}
			}
		}
		
		return isExistingMetric;
	}
	
	public ObjectDoubleOpenHashMap<String>[] getGraphMetricsVector(ColouredGraph[] graphs, List<SingleValueMetric> metrics){
		if(graphs != null && graphs.length > 0 && metrics != null & metrics.size() > 0){
			
			ObjectDoubleOpenHashMap<String> graphVectors[] = new ObjectDoubleOpenHashMap[graphs.length];
			int i = 0 ; 
			for(ColouredGraph grph: graphs){
				ObjectDoubleOpenHashMap<String> objMapMetricValues = getMetricValues(grph, metrics);
				if(objMapMetricValues!= null){
					graphVectors[i]=objMapMetricValues;
				}else{
					graphVectors[i] = new ObjectDoubleOpenHashMap<String>();
				}
				i++;
			}
			return graphVectors;
		}
		return null;		
	}
	
	public void addConstantValue(Expression expr, ColouredGraph grph, double constVal){
		if(grph !=null && expr != null ){
			ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
			String keyOfGraph = grph.getGraph().getNumberOfVertices()+"-"+grph.getGraph().getNumberOfEdges();
			mValueStorage.addConstantValue(expr, keyOfGraph, constVal);			
		}
	}
	
	public void setMetricValues(Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues){
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		mValueStorage.setMetricValues(mapMetricValues);
	}
	
	public void setConstantValues(Map<Expression, Map<String, Double>> mapConstantValues){
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		mValueStorage.setConstantValues(mapConstantValues);
	}
	
	public ObjectDoubleOpenHashMap<String> getMetricValues(ColouredGraph grph, List<SingleValueMetric> metrics){
		String keyOfGraph = grph.getGraph().getNumberOfVertices() +"-" + grph.getGraph().getNumberOfEdges();
		//get list of metric name
		Set<String> metricNames = new HashSet<String>();
		for (SingleValueMetric metric : metrics){
			metricNames.add(metric.getName());
		}
		
		return getMetricValues(keyOfGraph, metricNames);
	}
	
	public ObjectDoubleOpenHashMap<String> getMetricValues(String keyOfGraph, Set<String> metricNames){
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		Map<String, Double> mapMetricValues = mValueStorage.getMetricValues(keyOfGraph);
		
		ObjectDoubleOpenHashMap<String> objMapMetricValues = new ObjectDoubleOpenHashMap<String>();
		if(mapMetricValues!= null){
			Set<String> setMetricNames = mapMetricValues.keySet();
			for(String metricName : setMetricNames){
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
	
	public Map<Expression, Map<String, Double>> getMapConstantValues(){
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		return mValueStorage.getMapConstantValues();
	}
	
	public Set<Expression> getConstantExpressions(){
		ValueStorage mValueStorage = mMapValueStorage.get(mDataSetPath);
		return mValueStorage.mMapConstantValues.keySet();
	}
	
	public void storeValues() {
		// Serialization
		try {

			// Saving of object in a file
			FileOutputStream file = new FileOutputStream(METRIC_CACHE_NAME);
			ObjectOutputStream out = new ObjectOutputStream(file);
			// Method for serialization of object
			out.writeObject(mMapValueStorage);

			out.close();
			file.close();
			LOGGER.info("Object has been saved");
		}

		catch (IOException ex) {
			LOGGER.warn("IOException: " + ex.getMessage());
		}
	}
	
	public void loadValues(){
		try
        {   
            // Reading the object from a file
            FileInputStream file = new FileInputStream(METRIC_CACHE_NAME);
            ObjectInputStream in = new ObjectInputStream(file);
             
            // Method for deserialization of object
            mMapValueStorage = (Map<String, ValueStorage>)in.readObject();
             
            in.close();
            file.close();
            LOGGER.info("Object has been loaded");
        }         
        catch(IOException ex)
        {
        	LOGGER.warn("IOException: " + ex.getMessage());
        }catch(ClassNotFoundException ex){
        	LOGGER.warn("IOException: " + ex.getMessage());
        }
	}
	
	private class ValueStorage implements Serializable{
		//1st key: expression, 2nd key: key of graph, value is the constant value of the graph
		private Map<Expression, Map<String, Double>> mMapConstantValues;
		
		//1st key: key of graph, 2nd key: metric name, value is the value of the metric
		private Map<String, Map<String, Double>> mMapMetricValues;
		
		ValueStorage(){
			mMapConstantValues = new HashMap<Expression, Map<String, Double>>();
			mMapMetricValues = new HashMap<String, Map<String, Double>>();
		}
		
		void setConstantValues(Map<Expression, Map<String, Double>> mapConstantValues){
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
					}
				}
			}
			return isExist;
		}
		
		void setMetricValues(Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues){
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
		
		Map<Expression, Map<String, Double>> getMapConstantValues(){
			return mMapConstantValues;
		}
		
		Map<String, Map<String, Double>> getMapMetricValues(){
			return mMapMetricValues;
		}
	
		Map<String, Double> getMetricValues(String key){
			return mMapMetricValues.get(key);
		}
		
		void addConstantValue(Expression expr, String keyOfGraph, double constVal){
			Map<String, Double> mapValues = mMapConstantValues.get(expr);
			if(mapValues == null){
				mapValues = new HashMap<String, Double>();
				mMapConstantValues.put(expr, mapValues);
			}
			
			mapValues.put(keyOfGraph, constVal);
		}
		
		void addMetricValues(String keyGraph, String metricName, double val, boolean isOverride){
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
}
