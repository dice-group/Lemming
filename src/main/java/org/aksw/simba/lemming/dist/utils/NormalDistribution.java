package org.aksw.simba.lemming.dist.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalDistribution {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NormalDistribution.class);
	
	private List<Double> mLstOfMeanValues;
	private List<Double> mLstOfStandardDeviations;
	private SortedSet<RefinementNode> mSetConstExpr;
	
	/**
	 * Constructor
	 * @param sampleGraphs the array of sampled graph
	 * @param constExprs the set of constant expressions
	 */
	public NormalDistribution(ColouredGraph[] sampleGraphs, SortedSet<RefinementNode> constExprs){
		mSetConstExpr = constExprs;
		
		mLstOfMeanValues = new ArrayList<Double>();
		mLstOfStandardDeviations = new ArrayList<Double>();
		
		/*
		 * compute the mean values and the standard deviation for each constant expression
		 * applied to different sampled graph
		 */
		computeConstValuesOfOrigGprhs(sampleGraphs);
		
	}
	/**
	 * compute for each constant expression, the mean and the standard deviation
	 * @param origGrphs the array of sampled graph
	 */
	private void computeConstValuesOfOrigGprhs(ColouredGraph[] origGrphs){
		if(origGrphs != null && mSetConstExpr!=null){
			int iNoOfGrphs = origGrphs.length;
			for(RefinementNode expr: mSetConstExpr){
				double constValOfGrphs [] = new double[iNoOfGrphs];
				int iPos = 0 ;
				for(ColouredGraph grph: origGrphs){
					constValOfGrphs[iPos] = expr.expression.getValue(grph);
					iPos++;
				}
				
				//compute average constant values
				double avrgConstVal = computeMeanValue(constValOfGrphs);
				mLstOfMeanValues.add(avrgConstVal);
				
				//compute standard deviation
				double standardDeviation = computeStandardDeviation(constValOfGrphs, avrgConstVal);
				mLstOfStandardDeviations.add(standardDeviation);
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
	public double computeErrorScore(ColouredGraph comparedGrph){
		double [] constVals = computeConstantValues(comparedGrph);
		int iNoOfConstVals = 0;
		if(constVals != null && (iNoOfConstVals = constVals.length) > 0){
			double sumOfErrorScore = 0;
			//size of the constant values is the number of constant expressions
			for(int i = 0 ; i < iNoOfConstVals ; ++i){
				double meanValue = mLstOfMeanValues.get(i);
				double standardDeviation = mLstOfStandardDeviations.get(i);
				
				double singleErrorScore = computeSingleErrorScore(meanValue, standardDeviation, constVals[i]);
				if(singleErrorScore != Double.NaN){
					sumOfErrorScore += singleErrorScore;
				}else{
					sumOfErrorScore = Double.NaN;
					break;
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
	/**
	 * compute all constant values based on the set of constant expressions
	 * @param grph the graph that will be computed
	 * @return
	 */
	private double[] computeConstantValues(ColouredGraph grph){
		if(mSetConstExpr != null && mSetConstExpr.size() > 0){
			double[] arrValues = new double[mSetConstExpr.size()];
			int i = 0;
			for (RefinementNode n : mSetConstExpr) {
	            arrValues[i] = n.getExpression().getValue(grph);
	            i++;
	        }
			return arrValues;
		}
		return null;
	}
	
//	private void computeAverageConstValues(){
//	int iNoOfConstExpr = 0 ;
//	if((iNoOfConstExpr = mLstOrigConstValues.size()) > 0){
//		for(int i = 0 ; i < iNoOfConstExpr ; i++){
//			double[] constValOfGrphs = mLstOrigConstValues.get(i);
//			double sum = 0;
//			for(int j = 0 ; j< constValOfGrphs.length ; ++j){
//				sum+= constValOfGrphs[j];
//			}
//			mLstOfAvrgConstValues.add(sum/constValOfGrphs.length);
//		}
//	}
//}

//private void computeStandardDeviation(){
//	if(mLstOrigConstValues.size() > 0 && mLstOfAvrgConstValues.size() > 0 ){
//		int iNoAvrgConstVals = mLstOfAvrgConstValues.size();
//		double sum = 0 ; 
//		for(int i = 0 ; i < iNoAvrgConstVals ; ++i){
//			double avrgConstVal = mLstOfAvrgConstValues.get(i);
//			double[] constValOfGrphs = mLstOrigConstValues.get(i);
//			for(int j = 0 ; j < constValOfGrphs.length ; ++j){
//				sum += Math.pow(avrgConstVal-constValOfGrphs[j], 2);
//			}
//			double standDeviation = Math.sqrt(sum);
//			mLstOfStandardDeviations.add(standDeviation);
//		}
//	}
//}
	
	/**
	 * compute the error score
	 * @param constVals
	 *            the array of constant values computed by the set of constant
	 *            expressions for a graph
	 * @return an error score
	 */
//	private double computeErrorScore(double[] constVals ){
//		int iNoOfConstVals = 0;
//		double sumOfErrorScore = 0;
//		//size of the constant values is the number of constant expressions
//		if((iNoOfConstVals = constVals.length) > 0 ){
//			for(int i = 0 ; i < iNoOfConstVals ; ++i){
//				
//				double meanValue = mLstOfMeanValues.get(i);
//				double standardDeviation = mLstOfStandardDeviations.get(i);
//				double singleErrorScore = computeSingleErrorScore(meanValue, standardDeviation, constVals[i]);
//				if(singleErrorScore != Double.NaN){
//					sumOfErrorScore += singleErrorScore;
//				}else{
//					sumOfErrorScore = Double.NaN;
//				}
//			}
//		}
//		
//		return sumOfErrorScore;
//	}
	
}
