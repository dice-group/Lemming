package org.aksw.simba.lemming.algo.expression;

public class ExpressionUtils {

    /**
     * Clones the given {@link Expression} <b>if</b> it is an {@link Operation}
     * instance. Otherwise the original instance is returned since a copy would
     * behave exactly as the original and the internal state of a
     * {@link Constant} or an {@link AtomicVariable} can not be changed.
     * 
     * @param original
     *            the {@link Expression} that should be cloned.
     * @return a clone of the original {@link Expression}
     */
    public static Expression clone(Expression original) {
        if (original.isOperation()) {
            Operation o = (Operation) original;
            return new Operation(clone(o.getLeft()), clone(o.getRight()), o.getOperator());
        } else {
            return original;
        }
    }

}
