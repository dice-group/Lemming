package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * The class will check the expressions and compute the metrics which should be
 * increased and decreased so that the expression value is close to the mean.
 * 
 * @author Atul
 *
 */
public class ExpressionChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionChecker.class);

    private Set<Expression> manualExpressionsSet = new HashSet<>();

    public void setManualExpressionsSet(Set<Expression> manualExpressionsSet) {
        this.manualExpressionsSet = manualExpressionsSet;
    }

    /*
     * mean value of each expression key: expression
     */
    private ObjectDoubleOpenHashMap<String> mMapOfMeanValues;

    public void setmMapOfMeanValues(ObjectDoubleOpenHashMap<String> mMapOfMeanValues) {
        this.mMapOfMeanValues = new ObjectDoubleOpenHashMap<>();
        this.mMapOfMeanValues = mMapOfMeanValues;
    }

    private ConstantValueStorage mValueCarrier;

    /*
     * Expressions to Increase along with difference from the mean. key: expression
     */
    private ObjectDoubleOpenHashMap<Expression> mMapexpressionsToIncrease = new ObjectDoubleOpenHashMap<Expression>();

    /*
     * Expressions to Decrease along with difference from the mean. key: expression
     */
    private ObjectDoubleOpenHashMap<Expression> mMapexpressionsToDecrease = new ObjectDoubleOpenHashMap<Expression>();

    /**
     * Variable to store which metric to increase.
     */
    private String metricToIncrease = "";

    /**
     * Variable to store which metric to decrease.
     */
    private String metricToDecrease = "";

    /**
     * Variable for storing maximum difference between expression value and mean.
     */
    private double maxDifferenceIncreaseMetric = Double.MIN_VALUE;
    private double maxDifferenceDecreaseMetric = Double.MIN_VALUE;

    /**
     * Constructor to intialize objects of ErrorScoreCalculator, EdgeModifier and
     * ConstantValueStorage classes.
     * 
     * @param errScoreCalculator
     * @param edgeModifier
     * @param valueCarrier
     */
    public ExpressionChecker(ErrorScoreCalculator errScoreCalculator, ConstantValueStorage valueCarrier) {

        mValueCarrier = valueCarrier;

        mMapOfMeanValues = errScoreCalculator.getmMapOfMeanValues();

    }

    /**
     * Constructor to intialize objects of ErrorScoreCalculator, EdgeModifier and
     * ConstantValueStorage classes.
     * 
     * @param errScoreCalculator
     * @param edgeModifier
     * @param valueCarrier
     */
    public ExpressionChecker(Set<Expression> manualExpressionsSet, ObjectDoubleOpenHashMap<String> mMapOfMeanValues) {
        // Constructor for testing purposes
        setManualExpressionsSet(manualExpressionsSet);
        setmMapOfMeanValues(mMapOfMeanValues);

    }

    /**
     * Method to store the expressions along with difference between the expression
     * value and mean value. This is an updated method which uses manually
     * configured expression directly.
     * 
     * @param mapMetricValues
     */
    public void storeExpressions1(ObjectDoubleOpenHashMap<String> mapMetricValues) {
        if (mapMetricValues != null && (mapMetricValues.size()) > 0) {

            for (Expression expr : manualExpressionsSet) {
                String key = expr.toString();

                double meanValue = mMapOfMeanValues.get(key);
                double constVal = expr.getValue(mapMetricValues);
                double difference = constVal - meanValue;

                System.out.println("Expression : " + key + ", Difference with mean : " + difference);
                System.out.println("Mean Value : " + meanValue);
                System.out.println("Expression Value : " + constVal);
                System.out.println();

                if (difference > 0.0) {
                    mMapexpressionsToDecrease.put(expr, difference);
                } else {
                    mMapexpressionsToIncrease.put(expr, Math.abs(difference));
                }

            }

        }
    }

    /**
     * Method to store the expressions along with difference between the expression
     * value and mean value.
     * 
     * @param mapMetricValues
     */
    public void storeExpressions(ObjectDoubleOpenHashMap<String> mapMetricValues) {
        if (mapMetricValues != null && (mapMetricValues.size()) > 0) {

            Map<Expression, Map<String, Double>> mapConstantValues = mValueCarrier.getMapConstantValues();
            if (mapConstantValues != null) {
                Set<Expression> setExpressions = new HashSet<>();
                setExpressions = mapConstantValues.keySet();

                for (Expression expr : setExpressions) {
                    String key = expr.toString();

                    double meanValue = mMapOfMeanValues.get(key);
                    double constVal = expr.getValue(mapMetricValues);
                    double difference = constVal - meanValue;

                    System.out.println("Expression : " + key + ", Difference with mean : " + difference);
                    System.out.println("Mean Value : " + meanValue);
                    System.out.println("Expression Value : " + constVal);
                    System.out.println();

                    if (difference > 0.0) {
                        mMapexpressionsToDecrease.put(expr, difference);
                    } else {
                        mMapexpressionsToIncrease.put(expr, Math.abs(difference));
                    }

                }
            } else {
                LOGGER.warn("The map metric values is invalid");
            }

        }
    }

    /**
     * Method iterates over all the expressions and further calls computeMetrics to
     * compute which metrics can increased or decreased.
     */
    public void checkExpressions() {

        // Logic for Expressions for which values need to be reduced.
        Object[] keys = mMapexpressionsToDecrease.keys;
        GetMetricsFromExpressions getMetrics = new GetMetricsFromExpressions();
        for (Object expr : keys) {
            if (expr != null) {
                getMetrics.compute((Expression) expr);
                Set<String> tempMetrics1 = getMetrics.getDirectProportionalMetricsSet();
                Set<String> tempMetrics2 = getMetrics.getInverseProportionalMetricsSet();

                computeMetrics((Expression) expr, tempMetrics1, tempMetrics2, mMapexpressionsToDecrease);
            }

        }

        // Logic for Expressions for which values need to be increased.
        keys = mMapexpressionsToIncrease.keys;
        getMetrics = new GetMetricsFromExpressions();
        for (Object expr : keys) {
            if (expr != null) {
                getMetrics.compute((Expression) expr);
                Set<String> tempMetrics1 = getMetrics.getDirectProportionalMetricsSet();
                Set<String> tempMetrics2 = getMetrics.getInverseProportionalMetricsSet();

                computeMetrics((Expression) expr, tempMetrics2, tempMetrics1, mMapexpressionsToIncrease);
            }

        }

    }

    /**
     * Method to computes the metrics to increase or decrease based on difference
     * value.
     * 
     * @param expr
     * @param setMetricsToDecrease
     * @param setMetricsToIncrease
     * @param mMapexpressions
     */
    private void computeMetrics(Expression expr, Set<String> setMetricsToDecrease, Set<String> setMetricsToIncrease,
            ObjectDoubleOpenHashMap<Expression> mMapexpressions) {

        // Remove Duplicate from both set
        Set<String> duplicateSet = new HashSet<String>(setMetricsToDecrease);
        duplicateSet.retainAll(setMetricsToIncrease);

        setMetricsToDecrease.removeAll(duplicateSet);
        setMetricsToIncrease.removeAll(duplicateSet);

        for (String metric : setMetricsToDecrease) {
            if (maxDifferenceDecreaseMetric < mMapexpressions.get(expr) && !NumberUtils.isParsable(metric)) {
                if (!metric.equals("#vertices") && !metric.equals("#edges")) {
                    metricToDecrease = metric;
                    maxDifferenceDecreaseMetric = mMapexpressions.get(expr);
                }
            }
        }

        for (String metric : setMetricsToIncrease) {
            if (maxDifferenceIncreaseMetric < mMapexpressions.get(expr)) {
                if (!metric.equals("#vertices") && !metric.equals("#edges") && !NumberUtils.isParsable(metric)) {
                    metricToIncrease = metric;
                    maxDifferenceIncreaseMetric = mMapexpressions.get(expr);
                }
            }
        }
    }

    public String getMetricToIncrease() {
        return metricToIncrease;
    }

    public String getMetricToDecrease() {
        return metricToDecrease;
    }

    public double getMaxDifferenceIncreaseMetric() {
        return maxDifferenceIncreaseMetric;
    }

    public double getMaxDifferenceDecreaseMetric() {
        return maxDifferenceDecreaseMetric;
    }

}
