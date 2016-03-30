/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.expression;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Models expressions that are learned by our algorithm
 *
 * @author ngonga
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 */
public interface Expression {

    /**
     * Returns the {@link SingleValueMetric} if this expression is atomic.
     * Otherwise <code>null</code> is returned.
     * 
     * @return the {@link SingleValueMetric} or <code>null</code>
     */
    public SingleValueMetric getMetric();

    /**
     * Returns the left part of the expression if it is an operation. Otherwise
     * <code>null</code> is returned.
     * 
     * @return the left part of the expression or <code>null</code>
     */
    public Expression getLeft();

    /**
     * Returns the right part of the expression if it is an operation. Otherwise
     * <code>null</code> is returned.
     * 
     * @return the right part of the expression or <code>null</code>
     */
    public Expression getRight();

    /**
     * Returns the operator of the expression if it is an operation. Otherwise
     * <code>null</code> is returned.
     * 
     * @return the operator of the expression or <code>null</code>
     */
    public Operator getOperator();

    /**
     * Returns the constant value if this is a constant. Note that it is not
     * defined which value is returned if the {@link Expression} instance is not
     * a constant, i.e., returns <code>true</code> for {@link #isConstant()}.
     * 
     * @return the constant value if this is a constant.
     */
    public double getConstantValue();

    /**
     * Returns true if this expression is a constant.
     * 
     * @return true if this expression is a constant
     */
    public boolean isConstant();

    /**
     * Returns true if this expression is atomic.
     * 
     * @return true if this expression is atomic
     */
    public boolean isAtomic();

    /**
     * Returns true if this expression is an operation.
     * 
     * @return true if this expression is an operation
     */
    public boolean isOperation();

    /**
     * Get the value of this expression for the given graph.
     *
     * @param cg
     *            Coloured Graph
     * @return Double value of expression
     */
    public double getValue(ColouredGraph cg);

    /**
     * Get the value of this expression for the given precomputed values of the
     * graph metrics.
     *
     * @param graphMetrics
     *            a Map containing the values of the metrics calculated for a
     *            certain graph.
     * @return The value of the expression.
     */
    public double getValue(ObjectDoubleOpenHashMap<String> graphMetrics);

    /**
     * String representation of an expression
     * 
     * @return
     */
    public String toString();
    
    /**
     * Size of an expression
     * @return 
     */
    public int getSize();

}
