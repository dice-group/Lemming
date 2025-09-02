package org.aksw.simba.lemming.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;

public class MetricTester {
	
	public static void printMetricInformation(List<SingleValueMetric> metrics, ColouredGraph[] grphs){
		
		int ith = 1;
		for(ColouredGraph grph: grphs){
			System.out.println("Graph " + ith + " has metric values: ");
			printMetricInformation(metrics, grph);
			ith++;
		}
	}
	
	
	public static void printMetricInformation(List<SingleValueMetric> metrics, ColouredGraph grph) {
	    System.out.println("Single graph has values: ");

	    // Create a thread pool with a number of threads equal to the number of metrics
	    ExecutorService executorService = Executors.newFixedThreadPool(metrics.size());

	    // Use a CompletionService to manage the results
	    CompletionService<String> completionService = new ExecutorCompletionService<>(executorService);

	    // Submit each metric computation as a separate task
	    for (SingleValueMetric metric : metrics) {
	        completionService.submit(() -> {
	            UpdatableMetricResult metricResultTemp = metric.applyUpdatable(grph);
	            double metVal = metricResultTemp.getResult();
	            return "Value of " + metric.getName() + " is " + metVal;
	        });
	    }

	    // Retrieve and print the results as they complete
	    for (int i = 0; i < metrics.size(); i++) {
	        try {
	            Future<String> resultFuture = completionService.take();
	            System.out.println(resultFuture.get());
	        } catch (InterruptedException | ExecutionException e) {
	            System.err.println("Error while calculating metric: " + e.getMessage());
	        }
	    }

	    // Shutdown the executor service
	    executorService.shutdown();
	    System.out.println();
	}
	
	public static List<UpdatableMetricResult> getMetricInformation(List<SingleValueMetric> metrics, ColouredGraph grph) {
	    // Create a thread pool with a number of threads equal to the number of metrics
	    ExecutorService executorService = Executors.newFixedThreadPool(metrics.size());

	    // Use a CompletionService to manage the results
	    CompletionService<UpdatableMetricResult> completionService = new ExecutorCompletionService<>(executorService);

	    // Submit each metric computation as a separate task
	    for (SingleValueMetric metric : metrics) {
	    	completionService.submit(() -> metric.applyUpdatable(grph));
	    }

	    // Retrieve and print the results as they complete
	    List<UpdatableMetricResult> results = new ArrayList<>();
	    for (int i = 0; i < metrics.size(); i++) {
	        try {
	            Future<UpdatableMetricResult> resultFuture = completionService.take();
	            UpdatableMetricResult metricResult = resultFuture.get();
                results.add(metricResult);
	        } catch (InterruptedException | ExecutionException e) {
	            System.err.println("Error while calculating metric: " + e.getMessage());
	        }
	    }

	    // Shutdown the executor service
	    executorService.shutdown();
	    return results;
	}
	
}
