package org.aksw.simba.lemming.algo.refinement.operator;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.ExpressionIterator;

import com.carrotsearch.hppc.BitSet;

public class LeaveNodeReplacingRefinementOperator implements RefinementOperator {

    /**
     * List of metrics that can be used by the refinement.
     */
    private AtomicVariable atomicVariables[];

    @Override
    public Set<Expression> refine(Expression expression) {
        Set<Expression> newExpressions = new HashSet<Expression>();
        ExpressionIterator iterator = new ExpressionIterator(expression);
        Expression e;
        while (iterator.hasNext()) {
            e = iterator.next();
            if (e.isAtomic()) {
                refine(e, expression, iterator.getRoute(), iterator.getRouteLength());
            }
        }
        return newExpressions;
    }

    private void refine(Expression variable, Expression root, BitSet route, int routeLength) {
        
    }

}
