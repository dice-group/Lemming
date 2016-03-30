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
    public double getConstantValue() {
        return Double.NaN;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
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
        Operation other = (Operation) obj;
        if (left == null) {
            if (other.left != null)
                return false;
        } else if (!left.equals(other.left))
            return false;
        if (operation != other.operation)
            return false;
        if (right == null) {
            if (other.right != null)
                return false;
        } else if (!right.equals(other.right))
            return false;
        return true;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    @Override
    public int getSize() {
        return left.getSize() + right.getSize() + 1;
    }
}
