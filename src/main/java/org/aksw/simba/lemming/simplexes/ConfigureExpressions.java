package org.aksw.simba.lemming.simplexes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Constant;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.Operation;
import org.aksw.simba.lemming.algo.expression.Operator;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import grph.Grph.DIRECTION;

public class ConfigureExpressions {
	
	// temporary map to store metric information for input graphs
    Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues = new HashMap<String, ObjectDoubleOpenHashMap<String>>();
    
    // temporary map to store expression, graph key and expression value
    Map<Expression, Map<String, Double>> mapConstantValues = new HashMap<Expression, Map<String,Double>>();
	
	public ConfigureExpressions(ColouredGraph graphs[], String dataset){
		List<SingleValueMetric> metrics;
		
		
		
		Set<Expression> setOfExpressions;
		if(dataset.equalsIgnoreCase("swdf")) {
			setOfExpressions = getExpressionsSWDF();
			metrics = getMetricsSWDF();
		}else {
			setOfExpressions = getExpressionsLGD();
			metrics = getMetricsLGD();
		}
		
		
		// Compute mapMetricValues
        for (ColouredGraph grph: graphs) {
        	if (grph != null) {
        		// generate graph key
        		String keyTemp = ConstantValueStorage.generateGraphKey(grph);
        		
        		ObjectDoubleOpenHashMap<String> mMetricNameValueTemp = new ObjectDoubleOpenHashMap<String>();
        		
        		for (SingleValueMetric metric: metrics) { // iterate over all metrics, compute their values and store it in temporary map
        			String metricName = metric.getName();
        			double metricValue = metric.apply(grph);
        			
        			mMetricNameValueTemp.put(metricName, metricValue);
        		}
        		
        		// store computed metric values in a map
        		mapMetricValues.put(keyTemp, mMetricNameValueTemp);
        	}
        }
        
        // Compute mapConstantValues
        for (Expression expr: setOfExpressions) {
        	Map<String, Double> mGraphKeyExprValueTemp = new HashMap<String, Double>(); // temporary map to store graph key and expression value
        	
        	Set<String> graphKeys = mapMetricValues.keySet(); // Iterate over metrics of every map and compute expression value
        	for (String graphKeyTemp: graphKeys) {
        		ObjectDoubleOpenHashMap<String> mMetricNameValue = mapMetricValues.get(graphKeyTemp);
        		double exprValue = expr.getValue(mMetricNameValue);
        		mGraphKeyExprValueTemp.put(graphKeyTemp, exprValue);
        	}
        	
        	//Store expr value in the map
        	mapConstantValues.put(expr, mGraphKeyExprValueTemp);
        }
		
	}
	
	public Map<String, ObjectDoubleOpenHashMap<String>> getMapMetricValues() {
		return mapMetricValues;
	}

	public Map<Expression, Map<String, Double>> getMapConstantValues() {
		return mapConstantValues;
	}

	/**
	 * Returns all metrics 
	 */
	private List<SingleValueMetric> getAllMetrics() {
		List<SingleValueMetric> metrics = new ArrayList<>();
		metrics.add(new NodeTriangleMetric());
		metrics.add(new EdgeTriangleMetric());
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
		metrics.add(new AvgVertexDegreeMetric());
		metrics.add(new StdDevVertexDegree(DIRECTION.in));
		metrics.add(new StdDevVertexDegree(DIRECTION.out));
		metrics.add(new NumberOfEdgesMetric());
		metrics.add(new NumberOfVerticesMetric());
		
		return metrics;
	}
	
	/**
	 * Returns metrics for SWDF dataset 
	 */
	private List<SingleValueMetric> getMetricsSWDF() {
		List<SingleValueMetric> metrics = new ArrayList<>();
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
		metrics.add(new StdDevVertexDegree(DIRECTION.out));
		metrics.add(new NumberOfEdgesMetric());
		metrics.add(new NumberOfVerticesMetric());
		
		return metrics;
	}
	
	/**
	 * Returns all metrics 
	 */
	private List<SingleValueMetric> getMetricsLGD() {
		List<SingleValueMetric> metrics = new ArrayList<>();
		//metrics.add(new NodeTriangleMetric());
		//metrics.add(new EdgeTriangleMetric());
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
		metrics.add(new AvgVertexDegreeMetric());
		//metrics.add(new StdDevVertexDegree(DIRECTION.in));
		//metrics.add(new StdDevVertexDegree(DIRECTION.out));
		metrics.add(new NumberOfEdgesMetric());
		metrics.add(new NumberOfVerticesMetric());
		
		return metrics;
	}
	
	/**
	 * Returns expressions for SWDF
	 */
	private Set<Expression> getExpressionsSWDF() {
		Set<Expression> setOfExpressions = new HashSet<>();
		
		Operation exp1 = new Operation(
                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfVerticesMetric()), 
                                new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), 
                                Operator.TIMES), 
                        new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS)
                , Operator.DIV);
        
        setOfExpressions.add(exp1);
        
        Operation exp2 = new Operation(
                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfVerticesMetric()), 
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), 
                                Operator.PLUS), 
                        new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), Operator.MINUS)
                , Operator.DIV);
        
        setOfExpressions.add(exp2);
        
        Operation exp3 = new Operation(
                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfVerticesMetric()), 
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), 
                                Operator.DIV), 
                        new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS)
                , Operator.DIV);
        
        setOfExpressions.add(exp3);
        
        Operation exp4 = new Operation(
                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfEdgesMetric()), 
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), 
                                Operator.PLUS), 
                        new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), Operator.MINUS)
                , Operator.DIV);
        
        setOfExpressions.add(exp4);
        
        Operation exp5 = new Operation(
                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfVerticesMetric()), 
                                new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), 
                                Operator.DIV), 
                        new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS)
                , Operator.DIV);
        
        setOfExpressions.add(exp5);
        
        System.out.println(exp1.toString());
        System.out.println(exp2.toString());
        System.out.println(exp3.toString());
        System.out.println(exp4.toString());
        System.out.println(exp5.toString());
        
        return setOfExpressions;
	}
	
	/**
	 * Returns expressions for LGD dataset
	 * 
	 */
	private Set<Expression> getExpressionsLGD() {
		Set<Expression> setOfExpressions = new HashSet<>();
		
		Operation exp1 = new Operation(new Operation(
                new Operation(new Constant(2.0),
                        new AtomicVariable(new NumberOfVerticesMetric()), Operator.TIMES),
                new AtomicVariable(new NumberOfEdgesMetric()), Operator.MINUS),
                
                new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                        new AtomicVariable(new AvgVertexDegreeMetric()), Operator.TIMES),
                
                Operator.DIV);
        
        setOfExpressions.add(exp1);

        Operation exp2 = new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                                new AtomicVariable(new AvgVertexDegreeMetric()), Operator.TIMES),
                        new AtomicVariable(new AvgVertexDegreeMetric()), Operator.TIMES),
                
                Operator.DIV);
        
        setOfExpressions.add(exp2);

        Operation exp3 = new Operation( new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                new AtomicVariable(new NumberOfEdgesMetric()), Operator.MINUS),
                
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.out)), Operator.PLUS),
                        new AtomicVariable(new NumberOfVerticesMetric()), Operator.MINUS),
                
                Operator.DIV);
        
        setOfExpressions.add(exp3);

        Operation exp4 = new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                
                new Operation(
                        new AtomicVariable(new NumberOfEdgesMetric()),
                        new Operation(new AtomicVariable(new AvgVertexDegreeMetric()),
                                new Constant(1.0), Operator.MINUS), Operator.TIMES),
                Operator.DIV);
        
        setOfExpressions.add(exp4);

        Operation exp5 = new Operation( new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                new AtomicVariable(new NumberOfEdgesMetric()), Operator.MINUS),
                
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS),
                        new AtomicVariable(new NumberOfVerticesMetric()), Operator.MINUS),
                
                Operator.DIV);
        
        setOfExpressions.add(exp5);
        
        System.out.println(exp1.toString());
        System.out.println(exp2.toString());
        System.out.println(exp3.toString());
        System.out.println(exp4.toString());
        System.out.println(exp5.toString());
        
        return setOfExpressions;
	}

}
