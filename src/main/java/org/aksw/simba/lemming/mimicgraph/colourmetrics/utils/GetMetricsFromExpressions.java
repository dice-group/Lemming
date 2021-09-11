package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.Operator;

/**
 * The class evaluates the expressions and identifies metrics which are directly
 * proportional or inversely proportional.
 * 
 * @author Atul
 *
 */
public class GetMetricsFromExpressions {

	private Set<String> directProportionalMetricsSet = new HashSet<>(); // Store metrics which should be increased.
	private Set<String> inverseProportionalMetricsSet = new HashSet<>(); // Store metrics which should be decreased.

	/**
	 * Constructor
	 * 
	 * @param constantExpressions
	 *            - set of expressions.
	 */
	public GetMetricsFromExpressions() {

	}

	/**
	 * Method returns the directly proportional metrics.
	 * 
	 * @return - set of metrics.
	 */
	public Set<String> getDirectProportionalMetricsSet() {
		return directProportionalMetricsSet;
	}

	/**
	 * Method updates the directly proportional metrics.
	 * 
	 * @param directProportionalMetricsSet
	 *            - set of metrics.
	 */
	public void setDirectProportionalMetricsSet(Set<String> directProportionalMetricsSet) {
		this.directProportionalMetricsSet = directProportionalMetricsSet;
	}

	/**
	 * Method returns the inverse proportional metrics.
	 * 
	 * @return - set of metrics.
	 */
	public Set<String> getInverseProportionalMetricsSet() {
		return inverseProportionalMetricsSet;
	}

	/**
	 * Method updates the inverse proportional metrics.
	 * 
	 * @param inverseProportionalMetricsSet
	 *            - set of metrics.
	 */
	public void setInverseProportionalMetricsSet(Set<String> inverseProportionalMetricsSet) {
		this.inverseProportionalMetricsSet = inverseProportionalMetricsSet;
	}

	/**
	 * Method evaluates the expressions and further calls the methods
	 * checkDirectProportionalMetric and checkInverseProportionalMetric with correct
	 * parameters.
	 * 
	 * @param constantExpressions
	 *            - set of expressions.
	 */
	public void compute(Set<Expression> constantExpressions) {

		for (Expression exp : constantExpressions) {

			directProportionalMetricsSet = new HashSet<>(); // Adding for testing purposes
			inverseProportionalMetricsSet = new HashSet<>(); // Adding for testing purposes

			System.out.println("Expession : " + exp);
			if (exp.getOperator() == Operator.DIV) { // If operator is division then we have numerator and denominator.
				checkDirectProportionalMetric(exp.getLeft());
				checkInverseProportionalMetric(exp.getRight());
			} else {
				checkDirectProportionalMetric(exp.getLeft());
				checkDirectProportionalMetric(exp.getRight());
			}

			System.out.println("Direct Proportional metrics : " + directProportionalMetricsSet);
			System.out.println("Inverse Proportional metrics : " + inverseProportionalMetricsSet);
			System.out.println();
		}

	}

	/**
	 * Method evaluates the expressions and further calls the methods
	 * checkDirectProportionalMetric and checkInverseProportionalMetric with correct
	 * parameters.
	 * 
	 * @param constantExpressions
	 *            - set of expressions.
	 */
	public void compute(Expression exp) {

		directProportionalMetricsSet = new HashSet<>(); 
		inverseProportionalMetricsSet = new HashSet<>();

		System.out.println("Expession : " + exp);
		if (exp.getOperator() == Operator.DIV) { // If operator is division then we have numerator and denominator.
			checkDirectProportionalMetric(exp.getLeft());
			checkInverseProportionalMetric(exp.getRight());
		} else {
			checkDirectProportionalMetric(exp.getLeft());
			checkDirectProportionalMetric(exp.getRight());
		}

		System.out.println("Direct Proportional metrics : " + directProportionalMetricsSet);
		System.out.println("Inverse Proportional metrics : " + inverseProportionalMetricsSet);
		System.out.println();

	}

	/**
	 * Method checks the expression and adds the metrics in set -
	 * directProportionalMetricsSet.
	 * 
	 * @param exp
	 *            - expression.
	 */
	private void checkDirectProportionalMetric(Expression exp) {
		if (exp.isAtomic() && !exp.isConstant()) {
			directProportionalMetricsSet.add(exp.toString());
		} else if ((exp.getOperator() == Operator.PLUS) || (exp.getOperator() == Operator.TIMES)) {
			checkDirectProportionalMetric(exp.getLeft());
			checkDirectProportionalMetric(exp.getRight());
		} else {
			checkDirectProportionalMetric(exp.getLeft());
			checkInverseProportionalMetric(exp.getRight());
		}
	}

	/**
	 * Method checks the expression and adds the metrics in set -
	 * inverseProportionalMetricsSet.
	 * 
	 * @param exp
	 *            - expression.
	 */
	private void checkInverseProportionalMetric(Expression exp) {
		if (exp.isAtomic() && !exp.isConstant()) {
			inverseProportionalMetricsSet.add(exp.toString());
		} else if ((exp.getOperator() == Operator.PLUS) || (exp.getOperator() == Operator.TIMES)) {
			checkInverseProportionalMetric(exp.getLeft());
			checkInverseProportionalMetric(exp.getRight());
		} else {
			checkInverseProportionalMetric(exp.getLeft());
			checkDirectProportionalMetric(exp.getRight());
		}
	}

}
