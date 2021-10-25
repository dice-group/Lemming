package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Class for storing the error score and metric values after each iteration in
 * Graph Amendment
 * 
 * @author Pranav
 * 
 */
public class ErrorScores {
    Boolean isRemoveAnEdge = false;
    double errorScore = Double.NaN;
    ObjectDoubleOpenHashMap<String> metricValues = null;

    /**
     * Copy COnstructor: Copies an error score object
     * 
     * @param that - Object to be copied
     */
    public ErrorScores(ErrorScores that) {
        this(that.getAction(), that.getErrorScore(), that.getMetricValues());
    }

    /**
     * Constructor: Initialize the error score object
     * 
     * @param action - Flag to denote if the score and metrics are obtained by
     *               adding or removing an edge
     * @param score  - Error Score
     * @param values - Metric values after performing edge addition/removal
     */
    public ErrorScores(Boolean action, double score, ObjectDoubleOpenHashMap<String> values) {
        this.isRemoveAnEdge = action;
        this.errorScore = score;
        this.metricValues = values;
    }

    /**
     * Returns flag denoting if object is obtained by adding or removing an edge
     * 
     * @return isRemoveAnEdge
     */
    public Boolean getAction() {
        return this.isRemoveAnEdge;
    }

    /**
     * Returns the error score
     * 
     * @return isRemoveAnEdge
     */
    public double getErrorScore() {
        return this.errorScore;
    }

    /**
     * Returns map of metric values
     * 
     * @return isRemoveAnEdge
     */
    public ObjectDoubleOpenHashMap<String> getMetricValues() {
        return this.metricValues;
    }
}
