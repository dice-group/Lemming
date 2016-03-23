package org.aksw.simba.lemming.algo.refinement.redberry;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.aksw.simba.lemming.algo.refinement.RefinementNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.redberry.core.number.Complex;
import cc.redberry.core.solver.ReduceEngine;
import cc.redberry.core.solver.ReducedSystem;
import cc.redberry.core.tensor.ExpressionFactory;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;

/**
 * This class uses the redberry library to check whether nodes to generate
 * unique hash values for nodes.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class RedberryBasedFactory implements RefinementNodeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedberryBasedFactory.class);

    private static final Complex ZERO = new Complex(0);

    private Map<String, SimpleTensor> knownMetrics = new HashMap<String, SimpleTensor>();

    @Override
    public RefinementNode createNode(Expression expression) {
        cc.redberry.core.tensor.Expression exp = transformExpression(expression);
        SimpleTensor variables[] = knownMetrics.values().toArray(new SimpleTensor[knownMetrics.size()]);
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(new cc.redberry.core.tensor.Expression[] { exp },
                variables, new Transformation[0]);
        exp = restoreVariableNames(rd);
        String reducedExpression = exp.toString();
        if (checkReducedExpression(reducedExpression)) {
            return new RefinementNode(expression, reducedExpression);
        } else {
            return null;
        }
    }

    /**
     * Transforms the given {@link Expression} object into a
     * {@link cc.redberry.core.tensor.Expression} object.
     * 
     * @param expression
     * @return Redberry expression object or null if an error occurred.
     */
    protected cc.redberry.core.tensor.Expression transformExpression(Expression expression) {
        return Tensors.expression(transformToTensor(expression), ZERO);
    }

    /**
     * Transforms the given {@link Expression} object into a {@link Tensor}
     * object.
     * 
     * @param expression
     * @return {@link Tensor} object or null if an error occurred.
     */
    private Tensor transformToTensor(Expression expression) {
        if (expression.isConstant()) {
            return new Complex(expression.getConstantValue());
        } else if (expression.isAtomic()) {
            String metricName = expression.getMetric().getName();
            SimpleTensor variable = Tensors.parseSimple(metricName);
            if (!knownMetrics.containsKey(metricName)) {
                knownMetrics.put(metricName, variable);
            }
            return variable;
        } else if (expression.isOperation()) {
            switch (expression.getOperator()) {
            case DIV:
                // Div is realized as product of the reciprocal
                return Tensors.pow(transformToTensor(expression.getLeft()),
                        Tensors.reciprocal(transformToTensor(expression.getRight())));
            case MINUS:
                // Minus is realized as the sum of the nodes while the second
                // node is multiplied with -1
                return Tensors.subtract(transformToTensor(expression.getLeft()),
                        transformToTensor(expression.getRight()));
            case PLUS:
                return Tensors.sum(transformToTensor(expression.getLeft()), transformToTensor(expression.getRight()));
            case TIMES:
                return Tensors.multiply(transformToTensor(expression.getLeft()),
                        transformToTensor(expression.getRight()));
            default: {
                LOGGER.error("Error. Got an unknown Operation. Returning null.");
                return null;
            }
            }
        }
        LOGGER.error("Error. Got an expression that has non of the known types. Returning null.");
        return null;
    }

    /**
     * Checks the given reduced expression to ensure that it contains variables.
     * 
     * @param reducedExpression
     *            the reduced expression that should be checked.
     * @return <code>true</code> if the expression is okay
     */
    private boolean checkReducedExpression(String reducedExpression) {
        char chars[] = reducedExpression.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            // if there is an alphabetic character, we have found a variable.
            switch (chars[i]) {
            case 'a': // falls through
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z': {
                return true;
            }
            }
        }
        return false;
    }

    private cc.redberry.core.tensor.Expression restoreVariableNames(ReducedSystem rd) {
        cc.redberry.core.tensor.Expression variableMappings[] = rd.getGeneralSolutions();
        cc.redberry.core.tensor.Expression invert;
        cc.redberry.core.tensor.Expression expression = rd.getEquations()[0];
        for (int i = 0; i < variableMappings.length; ++i) {
            invert = ExpressionFactory.FACTORY.create(variableMappings[0].get(1), variableMappings[0].get(0));
            expression = (cc.redberry.core.tensor.Expression) invert.transform(expression);
        }
        return expression;
    }
}
