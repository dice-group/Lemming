package org.aksw.simba.lemming.algo.refinement.redberry;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.aksw.simba.lemming.algo.refinement.RefinementNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.number.Complex;
import cc.redberry.core.solver.ReduceEngine;
import cc.redberry.core.solver.ReducedSystem;
import cc.redberry.core.tensor.ExpressionFactory;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;

/**
 * This class uses the Redberry library (http://redberry.cc/) to check whether
 * nodes to generate unique representations for nodes.
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
        // String reducedExpression = exp.get(0).toString(OutputFormat.LaTeX);
        String reducedExpression = TensorPrintUtils.print(exp.get(0), OutputFormat.LaTeX);
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
            double value = expression.getConstantValue();
            int valueAsInt = (int) value;
            if (valueAsInt == value) {
                return new Complex(valueAsInt);
            } else {
                return new Complex(value);
            }
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
                return Tensors.divide(transformToTensor(expression.getLeft()),
                        transformToTensor(expression.getRight()));
            case MINUS:
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

    /**
     * The {@link ReduceEngine} replaces all variables. Since we have inserted
     * only one single expression, it is very easy to restore the original
     * variable names.
     * 
     * @param rd
     *            the {@link ReducedSystem} object created by the
     *            {@link ReduceEngine}.
     * @return the reduced expression but with the original variable names.
     */
    private cc.redberry.core.tensor.Expression restoreVariableNames(ReducedSystem rd) {
        cc.redberry.core.tensor.Expression variableMappings[] = rd.getGeneralSolutions();
        cc.redberry.core.tensor.Expression invert;
        cc.redberry.core.tensor.Expression expression = rd.getEquations()[0];
        for (int i = 0; i < variableMappings.length; ++i) {
            invert = ExpressionFactory.FACTORY.create(variableMappings[i].get(1), variableMappings[i].get(0));
            expression = (cc.redberry.core.tensor.Expression) invert.transform(expression);
        }
        return expression;
    }
}
