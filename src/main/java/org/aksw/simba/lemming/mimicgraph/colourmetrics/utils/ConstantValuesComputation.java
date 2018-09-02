package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.mimicgraph.metricstorage.MetricAndConstantValuesCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class ConstantValuesComputation {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConstantValuesComputation.class);
	
	private ObjectDoubleOpenHashMap<String> mMapOfMeanValues;
	private ObjectDoubleOpenHashMap<String> mMapOfStandardDeviations;
	
	private Map<Expression, Map<String, Double>> mMapConstantValues;
	
	private MetricAndConstantValuesCarrier mValueCarrier;
	private int mINoOfGraphs = 0 ;
	/**
	 * Constructor
	 * @param sampleGraphs the array of sampled graph
	 * @param constExprs the set of constant expressions
	 */
	public ConstantValuesComputation(ColouredGraph[] sampleGraphs, MetricAndConstantValuesCarrier valueCarrier){
		mValueCarrier = valueCarrier;
		mINoOfGraphs = sampleGraphs.length;
		mMapOfMeanValues = new ObjectDoubleOpenHashMap<String>();
		mMapOfStandardDeviations = new ObjectDoubleOpenHashMap<String>();
		mMapConstantValues = new HashMap<Expression, Map<String, Double>>();
		/*
		 * compute the mean values and the standard deviation for each constant expression
		 * applied to different sampled graph
		 */
		computeMeanAndStandardDeviation(sampleGraphs);
		
	}
	/**
	 * compute for each constant expression, the mean and the standard deviation
	 * @param origGrphs the array of sampled graph
	 */
	private void computeMeanAndStandardDeviation(ColouredGraph[] origGrphs){
		if(origGrphs != null && mValueCarrier!=null){
			
			Map<Expression, Map<String, Double>> mapConstantValues = mValueCarrier.getMapConstantValues();
			if(mapConstantValues != null){
				Set<Expression> setExpressions = mapConstantValues.keySet();
				
				for(Expression expr: setExpressions){
					Map<String, Double> mapValues = mapConstantValues.get(expr);
					
					Map<String, Double> mapGraphAndValues = mMapConstantValues.get(expr);
					if(mapGraphAndValues == null){
						mapGraphAndValues = new HashMap<String, Double>();
						mMapConstantValues.put(expr, mapGraphAndValues);
					}
					
					double constValOfGrphs [] = new double[mINoOfGraphs];
					int iPos = 0 ;
					for(ColouredGraph grph: origGrphs){
						String key = grph.getGraph().getNumberOfVertices()+"-"+grph.getGraph().getNumberOfEdges();
						double constVal = mapValues.containsKey(key)? mapValues.get(key): 0;
						mapGraphAndValues.put(key, constVal);
						
						//add to array of constant values
						constValOfGrphs[iPos] = constVal;
						iPos++;
					}
					
					//compute average constant values
					double avrgConstVal = computeMeanValue(constValOfGrphs);
					mMapOfMeanValues.put(expr.toString(), avrgConstVal);
					
					//compute standard deviation
					double standardDeviation = computeStandardDeviation(constValOfGrphs, avrgConstVal);
					mMapOfStandardDeviations.put(expr.toString(), standardDeviation);
				}
			}
		}
	}
	
	/**
	 * compute the mean 
	 * @param constVals the array of sample values
	 * @return the mean value
	 */
	private double computeMeanValue(double[] constVals){
		
		if(constVals == null || constVals.length == 0)
			return 0;
		
		double sum = 0;
		int iNoOfConstVals = constVals.length;
		for(int j = 0 ; j< iNoOfConstVals; ++j){
			sum+= constVals[j];
		}
		return sum/iNoOfConstVals;
	}
	
	/**
	 * compute the standard deviation based on the mean value and the sample values
	 * @param constVals
	 * @param meanVal
	 * @return
	 */
	private double computeStandardDeviation(double[] constVals, double meanVal){
		if(constVals == null || constVals.length == 0){
			return 0;
		}
		
		double sum = 0;
		int iNoOfConstVals = constVals.length;
		
		for(int j = 0 ; j < iNoOfConstVals ; ++j){
			sum += Math.pow(meanVal- constVals[j], 2);
		}
		
		if(iNoOfConstVals == 1){
			return Math.sqrt(sum);
		}
		
		return Math.sqrt(sum/ (iNoOfConstVals -1));
	}
	
	/**
	 * compute the error score for the given graph using the set of constant
	 * expressions and the constant values of sampled graphs
	 * 
	 * @param comparedGrph
	 *            the given graph which we want to compute how far its constant
	 *            values are different to the constant values of the samples
	 *            graphs
	 * @return an error score
	 */
	public double computeErrorScore(ObjectDoubleOpenHashMap<String> mapMetricValues){

		if(mapMetricValues != null && ( mapMetricValues.size()) > 0){
			double sumOfErrorScore = 0;
			
			Map<Expression, Map<String, Double>> mapConstantValues = mValueCarrier.getMapConstantValues();
			if(mapConstantValues != null){
				Set<Expression> setExpressions = mapConstantValues.keySet();
				for(Expression expr: setExpressions){
					String key = expr.toString();
					
					double meanValue = mMapOfMeanValues.get(key);
					double standardDeviation = mMapOfStandardDeviations.get(key);
					
					double constVal = expr.getValue(mapMetricValues);
					
					double singleErrorScore = computeSingleErrorScore(meanValue, standardDeviation, constVal);
					if(singleErrorScore != Double.NaN){
						sumOfErrorScore += singleErrorScore;
					}else{
						sumOfErrorScore = Double.NaN;
						break;
					}
				}
			}
			
			return sumOfErrorScore;
		}
		
		return Double.NaN;
	}
	
	/**
	 * compute a single error score of a constant value to check how far it is
	 * from the average value
	 * 
	 * @param avrgConstVal
	 *            the average constant value
	 * @param standardDeviation
	 *            the standard deviation
	 * @param constVal
	 *            the constant value that will be checked
	 * @return
	 */
	private double computeSingleErrorScore(double avrgConstVal, double standardDeviation, double constVal){
		double res = Math.pow(avrgConstVal - constVal, 2);
		if(standardDeviation != 0){
			res = res/standardDeviation;
		}else{
			res = Double.NaN;
		}
		return res;
	}
	
	public int getNumberOfGraphs(){
		return mINoOfGraphs;
	}

	public Map<String, Map<String, Double>> getMapMetricValuesOfInputGraphs(){
		return mValueCarrier.getMapMetricValues();
	}
	
	public Map<Expression, Map<String, Double>> getMapConstantExpressions(){
		return mMapConstantValues;
	}
}
