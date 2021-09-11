package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.EdgeModifier;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * The class will check the expressions and compute the edge id that can be used
 * by GraphGenerationTest class.
 * 
 * @author Atul
 *
 */
public class ExpressionChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionChecker.class);

	/*
	 * mean value of each expression key: expression
	 */
	private ObjectDoubleOpenHashMap<String> mMapOfMeanValues;

	private HashMap<String, UpdatableMetricResult> mMapPrevMetricsResult; // Map to store previous metric results

	private ConstantValueStorage mValueCarrier;
	
	/*
	 * Expressions to Increase along with difference from the mean.
	 * key: expression
	 */
	private ObjectDoubleOpenHashMap<Expression> mMapexpressionsToIncrease = new ObjectDoubleOpenHashMap<Expression>();
	
	/*
	 * Expressions to Decrease along with difference from the mean.
	 * key: expression
	 */
	private ObjectDoubleOpenHashMap<Expression> mMapexpressionsToDecrease = new ObjectDoubleOpenHashMap<Expression>();

	public ExpressionChecker(ErrorScoreCalculator_new errScoreCalculator, EdgeModifier edgeModifier,
			ConstantValueStorage valueCarrier) {

		mValueCarrier = valueCarrier;

		mMapOfMeanValues = errScoreCalculator.getmMapOfMeanValues();
		mMapPrevMetricsResult = edgeModifier.getmMapPrevMetricsResult();

	}

	public void checkExpressions(ObjectDoubleOpenHashMap<String> mapMetricValues) {
		if (mapMetricValues != null && (mapMetricValues.size()) > 0) {

			Map<Expression, Map<String, Double>> mapConstantValues = mValueCarrier.getMapConstantValues();
			if (mapConstantValues != null) {
				Set<Expression> setExpressions = mapConstantValues.keySet();
				for (Expression expr : setExpressions) {
					String key = expr.toString();

					double meanValue = mMapOfMeanValues.get(key);
					double constVal = expr.getValue(mapMetricValues);
					double difference = constVal - meanValue;
					if (difference > 0.0) {
						mMapexpressionsToDecrease.put(expr, difference);
					} else {
						mMapexpressionsToIncrease.put(expr, difference);
					}

				}
			}

		}
		LOGGER.warn("The map metric values is invalid");

	}
}
