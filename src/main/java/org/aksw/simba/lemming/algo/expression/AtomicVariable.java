package org.aksw.simba.lemming.algo.expression;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * This class implements an {@link Expression} that comprises an atomic variable
 * representing a {@link SingleValueMetric}.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class AtomicVariable implements Expression {

    private final SingleValueMetric metric;

    public AtomicVariable(SingleValueMetric metric) {
        this.metric = metric;
    }

    @Override
    public SingleValueMetric getMetric() {
        return metric;
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
        return Double.NaN;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public boolean isOperation() {
        return false;
    }

    @Override
    public double getValue(ColouredGraph cg) {
        return metric.apply(cg);
    }

    @Override
    public double getValue(ObjectDoubleOpenHashMap<String> graphMetrics) {
        if (graphMetrics.containsKey(metric.getName())) {
            return graphMetrics.get(metric.getName());
        } else {
            // this is an error.
            return Double.NaN;
        }
    }

    /**
     * String representation of an expression
     * 
     * @return
     */
    public String toString() {
        return metric.getName();
    }

    @Override
    public int hashCode() {
        return (metric == null) ? 0 : metric.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AtomicVariable other = (AtomicVariable) obj;
        if (metric == null) {
            if (other.metric != null)
                return false;
        } else if (!metric.equals(other.metric))
            return false;
        return true;
    }

    public int getSize() {
        return 0;
    }

}
