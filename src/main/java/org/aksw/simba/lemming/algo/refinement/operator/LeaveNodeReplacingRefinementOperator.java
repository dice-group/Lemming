package org.aksw.simba.lemming.algo.refinement.operator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Constant;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.ExpressionIterator;
import org.aksw.simba.lemming.algo.expression.ExpressionUtils;
import org.aksw.simba.lemming.algo.expression.Operation;
import org.aksw.simba.lemming.algo.expression.Operator;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

/**
 * This class implements a {@link RefinementOperator} that replaces the leave
 * nodes of the given expression with operations that have two children. One of
 * the children is the old leave node while the other is a new
 * {@link AtomicVariable}.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class LeaveNodeReplacingRefinementOperator implements RefinementOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaveNodeReplacingRefinementOperator.class);
    private static final Constant ONE = new Constant(1);

    /**
     * List of {@link AtomicVariable} instances that can be used by the
     * refinement.
     */
    private AtomicVariable atomicVariables[];

    public LeaveNodeReplacingRefinementOperator(AtomicVariable[] atomicVariables) {
        this.atomicVariables = atomicVariables;
    }

    public LeaveNodeReplacingRefinementOperator(List<SingleValueMetric> metrics) {
        this.atomicVariables = new AtomicVariable[metrics.size()];
        for (int i = 0; i < atomicVariables.length; ++i) {
            atomicVariables[i] = new AtomicVariable(metrics.get(i));
        }
    }

    @Override
    public Set<Expression> refine(Expression expression) {
        Set<Expression> newExpressions = new HashSet<Expression>();
        ExpressionIterator iterator = new ExpressionIterator(expression);
        Expression e;
        while (iterator.hasNext()) {
            e = iterator.next();
            if (e.isAtomic()) {
                refine(e, expression, iterator.getRoute(), iterator.getRouteLength(), newExpressions);
            }
        }
        return newExpressions;
    }

    private void refine(Expression variable, Expression root, BitSet route, int routeLength,
            Set<Expression> newExpressions) {
        Expression newExpression;
        for (int i = 0; i < atomicVariables.length; ++i) {
            // v -> v + a
            newExpression = createNewExpression(new Operation(variable, atomicVariables[i], Operator.PLUS), root, route,
                    routeLength);
            if (newExpression != null) {
                newExpressions.add(newExpression);
            }
            // v -> v * a
            newExpression = createNewExpression(new Operation(variable, atomicVariables[i], Operator.TIMES), root,
                    route, routeLength);
            if (newExpression != null) {
                newExpressions.add(newExpression);
            }
            if (!atomicVariables[i].equals(variable)) {
                // v -> v - a
                newExpression = createNewExpression(new Operation(variable, atomicVariables[i], Operator.MINUS), root,
                        route, routeLength);
                if (newExpression != null) {
                    newExpressions.add(newExpression);
                }
                // v -> a - v
                newExpression = createNewExpression(new Operation(atomicVariables[i], variable, Operator.MINUS), root,
                        route, routeLength);
                if (newExpression != null) {
                    newExpressions.add(newExpression);
                }
                // v -> v / a
                newExpression = createNewExpression(new Operation(variable, atomicVariables[i], Operator.DIV), root,
                        route, routeLength);
                if (newExpression != null) {
                    newExpressions.add(newExpression);
                }
                // v -> a / v
                newExpression = createNewExpression(new Operation(atomicVariables[i], variable, Operator.DIV), root,
                        route, routeLength);
                if (newExpression != null) {
                    newExpressions.add(newExpression);
                }
            }
        }
        // v -> v + 1
        newExpression = createNewExpression(new Operation(variable, ONE, Operator.PLUS), root, route, routeLength);
        if (newExpression != null) {
            newExpressions.add(newExpression);
        }
        // v -> v - 1
        newExpression = createNewExpression(new Operation(variable, ONE, Operator.MINUS), root, route, routeLength);
        if (newExpression != null) {
            newExpressions.add(newExpression);
        }
    }

    private Expression createNewExpression(Operation newOperation, Expression root, BitSet route, int routeLength) {
        // if the root node has to be replaced
        if (routeLength == 1) {
            return newOperation;
        }
        // clone the root (and its children)
        Expression newExpression = ExpressionUtils.clone(root);
        // search for the child that should be replaced
        Expression node = newExpression;
        for (int i = 0; i < (routeLength - 2); ++i) {
            if ((node == null) || (!node.isOperation())) {
                LOGGER.error("Couldn't follow the given route. Returning null.");
                return null;
            }
            node = (route.get(i)) ? node.getRight() : node.getLeft();
        }
        if ((node == null) || (!node.isOperation())) {
            LOGGER.error("Couldn't follow the given route. Returning null.");
            return null;
        }
        // if the variable that we want to replace is a right child
        if (route.get(routeLength - 2)) {
            ((Operation) node).setRight(newOperation);
        } else {
            // the variable is the left child
            ((Operation) node).setLeft(newOperation);
        }
        return newExpression;
    }

}
