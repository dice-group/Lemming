package org.aksw.simba.lemming.algo.refinement.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Constant;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.Operation;
import org.aksw.simba.lemming.algo.expression.Operator;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LeaveNodeReplacingRefinementOperatorTest {

    private static final AtomicVariable VARIABLES[] = new AtomicVariable[] { new AtomicVariable(new SimpleMetric("a")),
            new AtomicVariable(new SimpleMetric("b")), new AtomicVariable(new SimpleMetric("c")) };

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> tests = new ArrayList<Object[]>();
        // a constant "0" is not refined
        tests.add(new Object[] { new Constant(0), new Expression[] {} });
        // a single variable "a"
        tests.add(new Object[] { VARIABLES[0],
                new Expression[] { new Operation(VARIABLES[0], new Constant(1), Operator.PLUS),
                        new Operation(VARIABLES[0], new Constant(1), Operator.MINUS),
                        new Operation(VARIABLES[0], VARIABLES[0], Operator.PLUS),
                        new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                        new Operation(VARIABLES[0], VARIABLES[2], Operator.PLUS),
                        new Operation(VARIABLES[0], VARIABLES[0], Operator.TIMES),
                        new Operation(VARIABLES[0], VARIABLES[1], Operator.TIMES),
                        new Operation(VARIABLES[0], VARIABLES[2], Operator.TIMES),
                        new Operation(VARIABLES[0], VARIABLES[1], Operator.MINUS),
                        new Operation(VARIABLES[0], VARIABLES[2], Operator.MINUS),
                        new Operation(VARIABLES[1], VARIABLES[0], Operator.MINUS),
                        new Operation(VARIABLES[2], VARIABLES[0], Operator.MINUS),
                        new Operation(VARIABLES[0], VARIABLES[1], Operator.DIV),
                        new Operation(VARIABLES[0], VARIABLES[2], Operator.DIV),
                        new Operation(VARIABLES[1], VARIABLES[0], Operator.DIV),
                        new Operation(VARIABLES[2], VARIABLES[0], Operator.DIV) } });
        // a single Operation "a + b"
        tests.add(new Object[] { new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS), new Expression[] {
                new Operation(new Operation(VARIABLES[0], new Constant(1), Operator.PLUS), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], new Constant(1), Operator.MINUS), VARIABLES[1],
                        Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[0], Operator.PLUS), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[2], Operator.PLUS), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[0], Operator.TIMES), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.TIMES), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[2], Operator.TIMES), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.MINUS), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[2], Operator.MINUS), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[1], VARIABLES[0], Operator.MINUS), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[2], VARIABLES[0], Operator.MINUS), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.DIV), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[0], VARIABLES[2], Operator.DIV), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[1], VARIABLES[0], Operator.DIV), VARIABLES[1], Operator.PLUS),
                new Operation(new Operation(VARIABLES[2], VARIABLES[0], Operator.DIV), VARIABLES[1], Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], new Constant(1), Operator.PLUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], new Constant(1), Operator.MINUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[0], Operator.PLUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[1], Operator.PLUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[2], Operator.PLUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[0], Operator.TIMES), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[1], Operator.TIMES), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[2], Operator.TIMES), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[0], Operator.MINUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[2], Operator.MINUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[0], VARIABLES[1], Operator.MINUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[2], VARIABLES[1], Operator.MINUS), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[0], Operator.DIV), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[1], VARIABLES[2], Operator.DIV), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[0], VARIABLES[1], Operator.DIV), Operator.PLUS),
                new Operation(VARIABLES[0], new Operation(VARIABLES[2], VARIABLES[1], Operator.DIV),
                        Operator.PLUS) } });
        // a complex Operation "(a + b) + c"
        tests.add(
                new Object[] {
                        new Operation(
                                new Operation(VARIABLES[0], VARIABLES[1],
                                        Operator.PLUS),
                                VARIABLES[2], Operator.PLUS),
                        new Expression[] {
                                new Operation(new Operation(new Operation(VARIABLES[0], new Constant(1), Operator.PLUS),
                                        VARIABLES[1], Operator.PLUS), VARIABLES[2], Operator.PLUS),
                                new Operation(new Operation(new Operation(VARIABLES[0], new Constant(1), Operator.MINUS),
                                        VARIABLES[1], Operator.PLUS), VARIABLES[2], Operator.PLUS),
                        new Operation(
                                new Operation(new Operation(VARIABLES[0], VARIABLES[0], Operator.PLUS), VARIABLES[1],
                                        Operator.PLUS),
                                VARIABLES[2], Operator.PLUS), new Operation(
                                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                                VARIABLES[1], Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(new Operation(VARIABLES[0], VARIABLES[2], Operator.PLUS),
                                                VARIABLES[1], Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(new Operation(new Operation(VARIABLES[0], VARIABLES[0], Operator.TIMES),
                                        VARIABLES[1], Operator.PLUS), VARIABLES[2], Operator.PLUS),
                        new Operation(new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.TIMES),
                                VARIABLES[1], Operator.PLUS), VARIABLES[2], Operator.PLUS),
                        new Operation(
                                new Operation(new Operation(VARIABLES[0], VARIABLES[2], Operator.TIMES), VARIABLES[1],
                                        Operator.PLUS),
                                VARIABLES[2], Operator.PLUS), new Operation(
                                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.MINUS),
                                                VARIABLES[1], Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(new Operation(VARIABLES[0], VARIABLES[2], Operator.MINUS),
                                                VARIABLES[1], Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(new Operation(new Operation(VARIABLES[1], VARIABLES[0], Operator.MINUS),
                                        VARIABLES[1], Operator.PLUS), VARIABLES[2], Operator.PLUS),
                        new Operation(new Operation(new Operation(VARIABLES[2], VARIABLES[0], Operator.MINUS),
                                VARIABLES[1], Operator.PLUS), VARIABLES[2], Operator.PLUS),
                        new Operation(new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.DIV),
                                VARIABLES[1], Operator.PLUS), VARIABLES[2], Operator.PLUS),
                        new Operation(
                                new Operation(new Operation(VARIABLES[0], VARIABLES[2], Operator.DIV), VARIABLES[1],
                                        Operator.PLUS),
                                VARIABLES[2], Operator.PLUS), new Operation(
                                        new Operation(new Operation(VARIABLES[1], VARIABLES[0], Operator.DIV),
                                                VARIABLES[1], Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(new Operation(VARIABLES[2], VARIABLES[0], Operator.DIV),
                                                VARIABLES[1], Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(new Operation(VARIABLES[0],
                                        new Operation(VARIABLES[1], new Constant(1), Operator.PLUS), Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(new Operation(VARIABLES[0],
                                        new Operation(VARIABLES[1], new Constant(1), Operator.MINUS), Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[1], VARIABLES[0], Operator.PLUS),
                                                Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[1], VARIABLES[1], Operator.PLUS),
                                                Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[1], VARIABLES[2], Operator.PLUS),
                                                Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[1], VARIABLES[0], Operator.TIMES),
                                                Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[1], VARIABLES[1], Operator.TIMES),
                                                Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(new Operation(VARIABLES[0],
                                        new Operation(VARIABLES[1], VARIABLES[2], Operator.TIMES), Operator.PLUS),
                                VARIABLES[2], Operator.PLUS), new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[1], VARIABLES[0], Operator.MINUS),
                                                Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(new Operation(VARIABLES[0],
                                        new Operation(VARIABLES[1], VARIABLES[2], Operator.MINUS), Operator.PLUS),
                                VARIABLES[2], Operator.PLUS), new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[0], VARIABLES[1], Operator.MINUS),
                                                Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(new Operation(VARIABLES[0],
                                        new Operation(VARIABLES[2], VARIABLES[1], Operator.MINUS), Operator.PLUS),
                                VARIABLES[2], Operator.PLUS), new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[1], VARIABLES[0], Operator.DIV), Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                                new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[1], VARIABLES[2], Operator.DIV), Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                        new Operation(
                                new Operation(VARIABLES[0], new Operation(VARIABLES[0], VARIABLES[1], Operator.DIV),
                                        Operator.PLUS),
                                VARIABLES[2], Operator.PLUS), new Operation(
                                        new Operation(VARIABLES[0],
                                                new Operation(VARIABLES[2], VARIABLES[1], Operator.DIV), Operator.PLUS),
                                        VARIABLES[2], Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], new Constant(1), Operator.PLUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], new Constant(1), Operator.MINUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[0], Operator.PLUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[1], Operator.PLUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[2], Operator.PLUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[0], Operator.TIMES), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[1], Operator.TIMES), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[2], Operator.TIMES), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[0], Operator.MINUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[1], Operator.MINUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[0], VARIABLES[2], Operator.MINUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[1], VARIABLES[2], Operator.MINUS), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[0], Operator.DIV), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[2], VARIABLES[1], Operator.DIV), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[0], VARIABLES[2], Operator.DIV), Operator.PLUS),
                        new Operation(new Operation(VARIABLES[0], VARIABLES[1], Operator.PLUS),
                                new Operation(VARIABLES[1], VARIABLES[2], Operator.DIV), Operator.PLUS) } });
        return tests;
    }

    private Expression expression;
    private Set<Expression> expectedExpressions;

    public LeaveNodeReplacingRefinementOperatorTest(Expression expression, Expression expectedExpressions[]) {
        this.expression = expression;
        this.expectedExpressions = new HashSet<Expression>(Arrays.asList(expectedExpressions));
    }

    @Test
    public void test() {
        LeaveNodeReplacingRefinementOperator refinementOperator = new LeaveNodeReplacingRefinementOperator(VARIABLES);

        Set<Expression> refinedExpressions = refinementOperator.refine(expression);
        String expectedExpString = expectedExpressions.toString();
        for (Expression e : refinedExpressions) {
            Assert.assertTrue("Couldn't find the Expression " + e.toString()
                    + " inside the list of expected Expressions " + expectedExpString, expectedExpressions.contains(e));
        }
        Assert.assertEquals(
                "The returned list of expressions (" + refinedExpressions.toString()
                        + ") contains not all expected elements (" + expectedExpString + ").",
                expectedExpressions.size(), refinedExpressions.size());
    }

    private static class SimpleMetric extends AbstractMetric implements SingleValueMetric {

        public SimpleMetric(String name) {
            super(name);
        }

        @Override
        public double apply(IColouredGraph graph) {
            return 0;
        }

    }
}
