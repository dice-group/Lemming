package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class ErrorScoreCalculator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ErrorScoreCalculator.class);
	
	/*
	 * mean value of each expression
	 * key: expression
	 */
	private ObjectDoubleOpenHashMap<String> mMapOfMeanValues;
	
	/*
	 * standard deviation of constant expression
	 * key: expression
	 */
	private ObjectDoubleOpenHashMap<String> mMapOfStandardDeviations;
	
	/*
	 * map of constant values of each graph
	 * 1st key: expression, 2nd key: key of graph and value is a 
	 * constant value
	 */
	private Map<Expression, Map<String, Double>> mMapConstantValues;
	
	/*
	 *  this map consists of error score of each graph for each 
	 *  constant expression
	 *  1st key: expression, 2nd key: key of graph and value is 
	 *  the error score
	 */
	private Map<Expression, Map<String, Double>> mMapErrorScores;
	
	/*
	 * this map consists of max sum error score for each 
	 * constant expression 
	 */
	private Map<String, Double> mMapSumErrorScore;
	
	private double mMaxErrorScore;
	private double mAvgErrorScore;
	private double mMinErrorScore;
	
	
	private ConstantValueStorage mValueCarrier;
	private int mINoOfGraphs = 0 ;
	/**
	 * Constructor
	 * @param sampleGraphs the array of sampled graph
	 * @param constExprs the set of constant expressions
	 */
	public ErrorScoreCalculator(ColouredGraph[] sampleGraphs, ConstantValueStorage valueCarrier){
		mValueCarrier = valueCarrier;
		mINoOfGraphs = sampleGraphs.length;
		
		mMapOfMeanValues = new ObjectDoubleOpenHashMap<String>();
		mMapOfStandardDeviations = new ObjectDoubleOpenHashMap<String>();
		
		mMapConstantValues = new HashMap<Expression, Map<String, Double>>();
		
		mMapErrorScores = new HashMap<Expression, Map<String, Double>>();
		mMapSumErrorScore = new HashMap<String, Double> ();
		
		mMaxErrorScore = Double.NaN;
		mAvgErrorScore = Double.NaN;
		mMinErrorScore = Double.NaN;
		
		/*
		 * compute the mean values and the standard deviation for each constant expression
		 * applied to different sampled graph
		 */
		computeMeanAndStandardDeviation(sampleGraphs);
		
		
		/*
		 * compute the max and average error score
		 */
		computeErrorScoreOfInputGraphs(sampleGraphs);
		
	}
	
	private void computeErrorScoreOfInputGraphs(ColouredGraph[] orgiGrphs){
		if( mMapConstantValues != null && mMapConstantValues.size() > 0 && 
				orgiGrphs != null && orgiGrphs.length >0 ) {
			
			LOGGER.info("Compute error score for all input grpahs!");
			
			Set<String> setOfKeyGraph = new HashSet<String>();
			
			/*
			 * compute error score of each graph according to each constant expression 
			 */
			for(ColouredGraph gprh : orgiGrphs){
				//key of graph
				String keyGraph = gprh.getGraph().getNumberOfVertices()+"-"+ gprh.getGraph().getNumberOfEdges();
				setOfKeyGraph.add(keyGraph);
				
				//get all constant expressions
				Set<Expression> setConstantExpressions = mMapConstantValues.keySet();
				
				for(Expression expr : setConstantExpressions){
					String keyExpr  = expr.toString();
					//get saved map constant values of graphs
					Map<String, Double> mapOfGraphAndConstVal = mMapConstantValues.get(expr);
					
					if(mapOfGraphAndConstVal.containsKey(keyGraph)){
						
						//get mean of the const expression
						double mean = mMapOfMeanValues.get(keyExpr);
						//get standard deviation of const expression
						double std = mMapOfStandardDeviations.get(keyExpr);
						//get constant value
						double constVal = mapOfGraphAndConstVal.get(keyGraph);
						
						double errorScore = computeSingleErrorScore(mean, std, constVal);
						
						Map<String, Double> mapGraphAndErrorScores = mMapErrorScores.get(expr);
						if(mapGraphAndErrorScores == null ){
							mapGraphAndErrorScores= new HashMap<String, Double>();
							mMapErrorScores.put(expr, mapGraphAndErrorScores);
						}
						
						mapGraphAndErrorScores.put(keyGraph, errorScore);						
					}
				}
			}
			
			
			double avrgErrorScore = 0;
			double minErrorScore = Double.MAX_VALUE;
			double maxErrorScore = Double.MIN_VALUE;
			
			/*
			 * compute max sum error score and average sum error score for each of graph 
			 */
			Set<Expression> setConstExpressions = mMapErrorScores.keySet();
			
			
			for(String keyGraph: setOfKeyGraph){
				//sum error score
				double sumErrorScore = 0;
				//count on each expression
				for(Expression expr: setConstExpressions){
					Map<String, Double> mapGraphAndErrorScore = mMapErrorScores.get(expr);
					if(mapGraphAndErrorScore.containsKey(keyGraph)){
						sumErrorScore += mapGraphAndErrorScore.get(keyGraph);
					}
				}	
				
				//put the sum error score to map
				mMapSumErrorScore.put(keyGraph, sumErrorScore);
				
				avrgErrorScore += sumErrorScore;
				
				if(maxErrorScore < sumErrorScore){
					maxErrorScore = sumErrorScore;
				}
				
				if(sumErrorScore < minErrorScore ){
					minErrorScore = sumErrorScore;
				}
			}
			
			mAvgErrorScore = avrgErrorScore/setOfKeyGraph.size();
			mMaxErrorScore = maxErrorScore;
			mMinErrorScore = minErrorScore;
		}
	}
	
	/**
	 * compute for each constant expression, the mean and the standard deviation
	 * @param origGrphs the array of sampled graph
	 */
	private void computeMeanAndStandardDeviation(ColouredGraph[] origGrphs){
		if(origGrphs != null && mValueCarrier!=null){
			LOGGER.info("Compute mean value vector and standard deviation vector!");
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
					//System.out.println("Expr: " + expr.toString() + "| Mean=" + meanValue + ", SD=" + standardDeviation + ", constant val=" + constVal);
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
		LOGGER.warn("The map metric values is invalid");
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
			LOGGER.warn("Standard deviation is 0");
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
	
	private Map<Expression, Map<String, Double>> getMapErrorScore(){
		return mMapErrorScores;
	}
	
	public Map<String, Double> getMapSumErrorScore(){
		return mMapSumErrorScore;
	}
	
	public double getMinErrorScore(){
		return mMinErrorScore;
	}
	
	public double getMaxErrorScore(){
		return mMaxErrorScore;
	}
	
	public double getAverageErrorScore(){
		return mAvgErrorScore;
	}

	public ObjectDoubleOpenHashMap<String> getmMapOfMeanValues() {
		return mMapOfMeanValues;
	}
}
