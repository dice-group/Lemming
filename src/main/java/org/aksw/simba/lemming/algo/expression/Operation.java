package org.aksw.simba.lemming.algo.expression;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * This class is a complex {@link Expression} comprising a left
 * {@link Expression}, a right {@link Expression} and an {@link Operator}
 * connecting both.
 * 
 */
public class Operation implements Expression {

    /**
     * The left {@link Expression} of this operation.
     */
    private Expression left;
    /**
     * The right {@link Expression} of this operation.
     */
    private Expression right;
    /**
     * The {@link Operator} connecting the {@link #left} and {@link #right}
     * {@link Expression}s.
     */
    private Operator operation;

    /**
     * Initializes a complex expression with a right and left expression
     *
     * @param left
     *            Left expression
     * @param right
     *            Right expression
     * @param operation
     *            Operator
     */
    public Operation(Expression left, Expression right, Operator operation) {
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    @Override
    public SingleValueMetric getMetric() {
        return null;
    }

    @Override
    public Expression getLeft() {
        return left;
    }

    @Override
    public Expression getRight() {
        return right;
    }

    @Override
    public Operator getOperator() {
        return operation;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean isOperation() {
        return true;
    }

    @Override
    public double getValue(ColouredGraph cg) {
        double leftValue = left.getValue(cg);
        double rightValue = right.getValue(cg);
        switch (operation) {
        case DIV:
            return leftValue / rightValue;
        case MINUS:
            return leftValue - rightValue;
        case PLUS:
            return leftValue + rightValue;
        case TIMES:
            return leftValue * rightValue;
        default:
            return Double.NaN;
        }
    }

    @Override
    public double getValue(ObjectDoubleOpenHashMap<String> graphMetrics) {
        double leftValue = left.getValue(graphMetrics);
        double rightValue = right.getValue(graphMetrics);
        switch (operation) {
        case DIV:
            return leftValue / rightValue;
        case MINUS:
            return leftValue - rightValue;
        case PLUS:
            return leftValue + rightValue;
        case TIMES:
            return leftValue * rightValue;
        default:
            return Double.NaN;
        }
    }

    /**
     * String representation of an expression
     * 
     * @return
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(left.toString());
        builder.append(' ');
        builder.append(operation);
        builder.append(' ');
        builder.append(right.toString());
        builder.append(')');
        return builder.toString();
    }
}
