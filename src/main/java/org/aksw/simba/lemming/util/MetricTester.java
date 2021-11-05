package org.aksw.simba.lemming.util;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.BitSet;

public class MetricTester {
	
	public static void printMetricInformation(List<SingleValueMetric> metrics, ColouredGraph[] grphs){
		
		int ith = 1;
		for(ColouredGraph grph: grphs){
			System.out.println("Graph " + ith + " has matric values: ");
			for(SingleValueMetric metric: metrics){
				double val = metric.apply(grph);
				System.out.print(metric.getName() + " = " + val + " \t ");
			}
			System.out.println();
			ith++;
		}
	}
	
	
	public static void printMetricInformation(List<SingleValueMetric> metrics, ColouredGraph grph){
		
		int ith = 1;
		System.out.println("Single graph has values: ");
		for(SingleValueMetric metric: metrics){
			double val = metric.apply(grph);
			System.out.print(metric.getName() + " = " + val + " \t ");
		}
		System.out.println();
		ith++;
	}
	
	public static void printEdgeColourDistribution(ObjectDistribution<BitSet> edgeDist){
		BitSet[] arrEdgeColoSamples = edgeDist.sampleSpace;
		double[] arrSampleValues = edgeDist.values;
		
		for(int i = 0 ; i< arrSampleValues.length ; ++i){
			
			
			
		}
	}
	
}
