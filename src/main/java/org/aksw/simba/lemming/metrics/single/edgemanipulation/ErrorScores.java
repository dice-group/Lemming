package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class ErrorScores {
	Boolean isRemoveAnEdge = false;
	double errorScore = Double.NaN;
	ObjectDoubleOpenHashMap<String> metricValues = null;
	
	public ErrorScores(ErrorScores that) {
	    this(that.getAction(),that.getErrorScore(), that.getMetricValues());
	}
	
	public ErrorScores(Boolean action, double score, ObjectDoubleOpenHashMap<String> values) {
		this.isRemoveAnEdge = action;
		this.errorScore = score;
		this.metricValues = values;
	}
	
	public Boolean getAction() {
		return this.isRemoveAnEdge;
	}
	
	public double getErrorScore() {
		return this.errorScore;
	}
	
	public ObjectDoubleOpenHashMap<String> getMetricValues(){
		return this.metricValues;
	}
}
