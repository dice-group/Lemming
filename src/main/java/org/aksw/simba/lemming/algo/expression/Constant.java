package org.aksw.simba.lemming.algo.expression;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * This class represents an {@link Expression} comprising a single constant
 * value.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class Constant implements Expression {

    private double constantValue;

    public Constant(double value) {
        constantValue = value;
    }

    @Override
    public SingleValueMetric getMetric() {
        return null;
    }

    @Override
    public Expression getLeft() {
        return null;
    }

    @Override
    public Expression getRight() {
        return null;
    }

    @Override
    public Operator getOperator() {
        return null;
    }

    @Override
    public double getConstantValue() {
        return constantValue;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean isOperation() {
        return false;
    }

    @Override
    public double getValue(ColouredGraph cg) {
        return constantValue;
    }

    @Override
    public double getValue(ObjectDoubleOpenHashMap<String> graphMetrics) {
        return constantValue;
    }

    /**
     * String representation of an expression
     * 
     * @return
     */
    public String toString() {
        return Double.toString(constantValue);
    }

}
