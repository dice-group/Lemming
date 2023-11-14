package org.aksw.simba.lemming.simplexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.junit.Test;

import grph.Grph.DIRECTION;

public class ReadExpressions {
	
	@Test
	public void getSWDFExp() {
		/*---------------------------------------------------
        Loading metrics values and constant expressions 
        ----------------------------------------------------*/
		String datasetPath = "SemanticWebDogFood/";
		/*---------------------------------------------------
        Definition of metrics to form constant expression
        ----------------------------------------------------*/
       List<SingleValueMetric> metrics = new ArrayList<>();
       //these are two fixed metrics: NodeTriangleMetric and EdgeTriangleMetric
       metrics.add(new NodeTriangleMetric());
       metrics.add(new EdgeTriangleMetric());
      
       //these are optional metrics
       metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
       metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
       metrics.add(new AvgVertexDegreeMetric());
       metrics.add(new StdDevVertexDegree(DIRECTION.in));
       metrics.add(new StdDevVertexDegree(DIRECTION.out));
       metrics.add(new NumberOfEdgesMetric());
       metrics.add(new NumberOfVerticesMetric());
       metrics.add(new DiameterMetric());
       
        ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);
        metrics = valuesCarrier.getMetricsOfExpressions(metrics);
        Set<Expression> constantExpressions = valuesCarrier.getConstantExpressions();
        if(!valuesCarrier.isComputableMetrics(metrics)){
        	System.out.println("The list of metrics has some metrics that are not existing in the precomputed metric values.");
        	System.out.println("Please generate the file [value_store.val] again!");
        	return ;
        }
	}

}
