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

    private SingleValueMetric metric;

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

}
