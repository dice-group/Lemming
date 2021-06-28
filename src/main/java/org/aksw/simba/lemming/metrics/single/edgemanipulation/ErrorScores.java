package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class ErrorScores {
	double errorScore = Double.NaN;
	ObjectDoubleOpenHashMap<String> metricValues = null;
	
	public ErrorScores(ErrorScores that) {
	    this(that.getErrorScore(), that.getMetricValues());
	}
	
	public ErrorScores(double score, ObjectDoubleOpenHashMap<String> values) {
		this.errorScore = score;
		this.metricValues = values;
	}
	
	public double getErrorScore() {
		return this.errorScore;
	}
	
	public ObjectDoubleOpenHashMap<String> getMetricValues(){
		return this.metricValues;
	}
}
