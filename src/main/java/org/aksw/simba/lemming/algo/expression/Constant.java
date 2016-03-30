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

    private final double constantValue;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(constantValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Constant other = (Constant) obj;
        if (Double.doubleToLongBits(constantValue) != Double.doubleToLongBits(other.constantValue))
            return false;
        return true;
    }

    @Override
    public int getSize() {
        return 0;
    }

}
