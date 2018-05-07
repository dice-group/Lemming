package org.aksw.simba.lemming.dist.utils;

import java.util.Random;

import org.aksw.simba.lemming.util.Constants;
import org.apache.jena.ext.com.google.common.math.BigIntegerMath;

public class PoissonDistribution {

	private static Random rand = new Random();
	
	/**
	 * probability mass function for a random X value
	 * @param randomX a random X value
	 * @return the probability
	 */
	public static double getProbabilityOf (int randomX, double mean){
		double e = Math.E;
		long factorialX = BigIntegerMath.factorial(randomX).longValue();
		double meanPowerX = Math.pow(mean, randomX);
		double res = Math.pow(e, -mean) * (meanPowerX/factorialX) ;
		return res;
	}
	
	/**
	 * generate a random number based on Knuth
	 * @return
	 */
	public static int randomXKnuth(double mean){
		double L = Math.pow(Math.E, -mean) ;
		int randomX = 0 ;
		double p = 1;
		do{
			randomX++;
			double u = rand.nextDouble();
			p *=u;
		}while(p > L);
		
		return randomX -1;
	}
	
	/**
	 * generate a random number based on Knuth
	 * @return
	 */
	public static int randomXKnuth(double mean, Random inRamdom){
		double L = Math.pow(Math.E, -mean) ;
		int randomX = 0 ;
		double p = 1;
		do{
			randomX++;
			double u = inRamdom.nextDouble();
			p *=u;
		}while(p > L);
		if(randomX -1 != 0)
			return (randomX -1);
		return 1;
	}
	
	/**
	 * generate a random number based on Junhao and Knuth
	 * @return
	 */
	public static int randomXJunhao(double mean){
		double meanLeft = mean;
		int x = 0 ; 
		double p = 1;
		
		do{
			x++;
			double u = rand.nextDouble();
			p *= u;
			while(p < 1 && meanLeft > 0){
				if(meanLeft > Constants.STEP_JUNHAO_POISSON){
					p = p * Math.pow(Math.E, Constants.STEP_JUNHAO_POISSON);
					meanLeft = meanLeft - Constants.STEP_JUNHAO_POISSON;
				}else{
					p = p * Math.pow(Math.E, meanLeft);
					meanLeft = 0 ;
				}
			}
		}while(p > 1);
		
//		if(x -1 != 0)
//			return (x -1);
//		return 1;
		
		return x - 1;	
	}
	
	/**
	 * generate a random number based on Junhao and Knuth
	 * @return
	 */
	public static int randomXJunhao(double mean, Random inRand){
		double meanLeft = mean;
		int x = 0 ; 
		double p = 1;
		
		do{
			x++;
			double u = inRand.nextDouble();
			p *= u;
			while(p < 1 && meanLeft > 0){
				if(meanLeft > Constants.STEP_JUNHAO_POISSON){
					p = p * Math.pow(Math.E, Constants.STEP_JUNHAO_POISSON);
					meanLeft = meanLeft - Constants.STEP_JUNHAO_POISSON;
				}else{
					p = p * Math.pow(Math.E, meanLeft);
					meanLeft = 0 ;
				}
			}
		}while(p > 1);
		
		
		//if(x -1 != 0)
			//return (x -1);
		//return 1;
		return x - 1;	
	}
	
	/**
	 * generate a random number based upon the inversion by sequential search
	 * @return a random X
	 */
	public static int randomXITS(double mean){
		int x = 0 ;
		double p = Math.pow(Math.E, -mean);
		double s = p;
		
		double u = rand.nextDouble();
		while(u > s){
			x++; 
			p = p *(mean /x);
			s = s + p;
		}		
		return x;
	}
	
	/**
	 * generate a random number based upon the inversion by sequential search
	 * @return a random X
	 */
	public static int randomXITS(double mean, Random inRand){
		int x = 0 ;
		double p = Math.pow(Math.E, -mean);
		double s = p;
		
		double u = inRand.nextDouble();
		while(u > s){
			x++; 
			p = p *(mean /x);
			s = s + p;
		}		
		//if(x > 0)
			//return x;
		//return 1;
		return x;
	}
	
}
