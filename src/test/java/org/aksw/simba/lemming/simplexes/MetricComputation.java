package org.aksw.simba.lemming.simplexes;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.junit.Test;

import grph.Grph.DIRECTION;

public class MetricComputation {

	@Test
	public void computeResultsForTargetGraph() {
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		SWDFReadYearFile mDatasetManager = new SWDFReadYearFile(2015);
		ColouredGraph graphs[] = new ColouredGraph[20];
		graphs= mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		// Define metrics to compute
		List<SingleValueMetric> metrics = new ArrayList<>();
	     
	        //these are optional metrics
	    metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
	    metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
	    metrics.add(new StdDevVertexDegree(DIRECTION.out));
	    metrics.add(new NumberOfEdgesMetric());
	    metrics.add(new NumberOfVerticesMetric());
	    
	    for (SingleValueMetric metric: metrics) {
	    	double metricResult = metric.apply(graphs[0]);
	    	System.out.println("Metric: " + metric.getName() + ", result: " + metricResult);
	    }
	}
}
