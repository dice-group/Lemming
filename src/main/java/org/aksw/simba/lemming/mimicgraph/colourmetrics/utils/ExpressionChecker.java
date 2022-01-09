package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.Expression;
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

    /**
     *  Set to store manually defined expressions.
     */
    private Set<Expression> manualExpressionsSet = new HashSet<>();

    /*
     * mean value of each expression key: expression
     */
    private ObjectDoubleOpenHashMap<String> mMapOfMeanValues;
    
    /**
     * Map for expressions along with different values for input graphs.
     */
    private Map<Expression, Map<String, Double>> mapConstantValues;

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
     * Constructor to intialize maps of Expression mean values and expression values for input graphs.
     * 
     * @param mMapOfMeanValuesOrg - Map for expressions along with their mean values
     * @param mapConstantValuesOrg - Map for expressions along with different values for input graphs.
     */
    public ExpressionChecker(ObjectDoubleOpenHashMap<String> mMapOfMeanValuesOrg, Map<Expression, Map<String, Double>> mapConstantValuesOrg) {

        mapConstantValues = mapConstantValuesOrg;
        mMapOfMeanValues = mMapOfMeanValuesOrg;

    }

    /**
     * Constructor to intialize objects of ErrorScoreCalculator, EdgeModifier and
     * ConstantValueStorage classes.
     * 
     * Note - This constructor is created for testing purposes. In order to check logic
     * for manually configured expressions.
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
     * Note - This method is created for testing purposes. In order to check logic
     * for manually configured expressions.
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
     *            - Map which contains expression as key and expression result as
     *            value
     */
    public void storeExpressions(ObjectDoubleOpenHashMap<String> mapMetricValues) {
        if (mapMetricValues != null && (mapMetricValues.size()) > 0) {

            if (mapConstantValues != null) {
                Set<Expression> setExpressions = new HashSet<>();
                setExpressions = mapConstantValues.keySet();

                for (Expression expr : setExpressions) {
                    String key = expr.toString();

                    double meanValue = mMapOfMeanValues.get(key);
                    double constVal = expr.getValue(mapMetricValues);
                    double difference = constVal - meanValue;

                    //System.out.println("Expression : " + key + ", Difference with mean : " + difference);
                    //System.out.println("Mean Value : " + meanValue);
                    //System.out.println("Expression Value : " + constVal);
                    //System.out.println();

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
     *            - expression
     * @param setMetricsToDecrease
     *            - set of metrics that can be decreased
     * @param setMetricsToIncrease
     *            - set of metrics that can be increase
     * @param mMapexpressions
     *            - Map which contains expression as key and difference of the
     *            expression with mean as value
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

    /**
     * The method returns the metric name that should be increased.
     * 
     * @return - metric name
     */
    public String getMetricToIncrease() {
        return metricToIncrease;
    }

    /**
     * The method returns the metric name that should be decreased.
     * 
     * @return - metric name
     */
    public String getMetricToDecrease() {
        return metricToDecrease;
    }

    /**
     * From a given set of expressions, we will identify the metric that should be
     * increased and the corresponding expression which has the metric. The method
     * returns the maximum difference of the such an expression value with the mean
     * value.
     * 
     * @return - difference as double value
     */
    public double getMaxDifferenceIncreaseMetric() {
        return maxDifferenceIncreaseMetric;
    }

    /**
     * From a given set of expressions, we will identify the metric that should be
     * decreased and the corresponding expression which has the metric. The method
     * returns the maximum difference of the such an expression value with the mean
     * value.
     * 
     * @return - difference as double value
     */
    public double getMaxDifferenceDecreaseMetric() {
        return maxDifferenceDecreaseMetric;
    }
    
    /**
     * Method to updates map of mean values. This map contains the expression along with
     * their mean values.
     * 
     * @param mMapOfMeanValues
     *            - Hash Map with expression key and mean as value.
     */
    public void setmMapOfMeanValues(ObjectDoubleOpenHashMap<String> mMapOfMeanValues) {
        this.mMapOfMeanValues = mMapOfMeanValues;
    }
    
    /**
     * The method to updates manually defined expressions.
     * 
     * Note - This method is created for testing purposes. In order to check logic
     * for manually configured expressions.
     * 
     * @param manualExpressionsSet
     *            - set of expressions
     */
    public void setManualExpressionsSet(Set<Expression> manualExpressionsSet) {
        this.manualExpressionsSet = manualExpressionsSet;
    }

    /**
     * From a given set of expressions, we will identify the metric that should be
     * increased and the corresponding expression which has the metric. The method
     * updates the maximum difference of the such an expression value with the mean
     * value.
     * @param maxDifferenceIncreaseMetric
     */
    public void setMaxDifferenceIncreaseMetric(double maxDifferenceIncreaseMetric) {
        this.maxDifferenceIncreaseMetric = maxDifferenceIncreaseMetric;
    }


    /**
     * From a given set of expressions, we will identify the metric that should be
     * decreased and the corresponding expression which has the metric. The method
     * returns the maximum difference of the such an expression value with the mean
     * value.
     * @param maxDifferenceDecreaseMetric - double value
     */
    public void setMaxDifferenceDecreaseMetric(double maxDifferenceDecreaseMetric) {
        this.maxDifferenceDecreaseMetric = maxDifferenceDecreaseMetric;
    }

}
