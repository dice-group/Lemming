/**
 * This file is part of NIF transfer library for the General Entity Annotator Benchmark.
 *
 * NIF transfer library for the General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NIF transfer library for the General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NIF transfer library for the General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.simba.lemming.algo.refinement.redberry;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Constant;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.Operation;
import org.aksw.simba.lemming.algo.expression.Operator;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RedberryBasedFactoryTest {

    @Parameters
    public static List<Object[]> data() {
        AtomicVariable x = new AtomicVariable(new TestMetric("x"));
        AtomicVariable y = new AtomicVariable(new TestMetric("y"));
        List<Object[]> tests = new ArrayList<Object[]>();

        // x+1 == x+1
        tests.add(new Object[] { new Operation(x, new Constant(1), Operator.PLUS),
                new Operation(x, new Constant(1), Operator.PLUS), true });
        // x+1 == 1+x
        tests.add(new Object[] { new Operation(x, new Constant(1), Operator.PLUS),
                new Operation(new Constant(1), x, Operator.PLUS), true });
        // x+1 != x+2
        tests.add(new Object[] { new Operation(x, new Constant(1), Operator.PLUS),
                new Operation(x, new Constant(2), Operator.PLUS), false });
        // x+y == x+y
        tests.add(new Object[] { new Operation(x, y, Operator.PLUS), new Operation(x, y, Operator.PLUS), true });
        // x+y == y+x
        tests.add(new Object[] { new Operation(x, y, Operator.PLUS), new Operation(y, x, Operator.PLUS), true });
        // x+1 != y+1
        tests.add(new Object[] { new Operation(x, new Constant(1), Operator.PLUS),
                new Operation(y, new Constant(1), Operator.PLUS), false });
        // 1*x == x
        tests.add(new Object[] { new Operation(x, new Constant(1), Operator.TIMES), x, true });
        // (x*x)/x == x
        tests.add(new Object[] { new Operation(new Operation(x, x, Operator.TIMES), x, Operator.DIV), x, true });
        // x*(x/x) == x
        tests.add(new Object[] { new Operation(x, new Operation(x, x, Operator.DIV), Operator.TIMES), x, true });
        // x+x == 2*x
        tests.add(new Object[] { new Operation(x, x, Operator.PLUS), new Operation(new Constant(2), x, Operator.TIMES),
                true });
        // x*y == y*x
        tests.add(new Object[] { new Operation(x, y, Operator.TIMES), new Operation(x, y, Operator.TIMES), true });
        // 1 == null (because it is a constant)
        tests.add(new Object[] { new Constant(1), null, true });
        // 1+1 == null (because it is a constant)
        tests.add(new Object[] { new Operation(new Constant(1), new Constant(1), Operator.PLUS), null, true });
        // x-x == null (because it is a constant)
        tests.add(new Object[] { new Operation(x, x, Operator.MINUS), null, true });
        // x/x == null (because it is a constant)
        tests.add(new Object[] { new Operation(x, x, Operator.DIV), null, true });
        return tests;
    }

    private Expression expression1;
    private Expression expression2;
    private boolean expectExpressionsToBeEqual;

    public RedberryBasedFactoryTest(Expression expression1, Expression expression2,
            boolean expectExpressionsToBeEqual) {
        this.expression1 = expression1;
        this.expression2 = expression2;
        this.expectExpressionsToBeEqual = expectExpressionsToBeEqual;
    }

    @Test
    public void test() throws Exception {
        RedberryBasedFactory factory = new RedberryBasedFactory();
        RefinementNode node1 = factory.createNode(expression1);
        // if we expect null as result for expression1
        if (expression2 == null) {
            Assert.assertNull(node1);
            return;
        } else {
            // make sure that it is not null
            Assert.assertNotNull(node1);
        }
        RefinementNode node2 = factory.createNode(expression2);
        Assert.assertNotNull(node2);
        if (expectExpressionsToBeEqual) {
            Assert.assertTrue("\"" + node1.toString() + "\" should be equal to \"" + node2.toString() + "\"",
                    node1.equals(node2));
        } else {
            Assert.assertFalse("\"" + node1.toString() + "\" shouldn't be equal to \"" + node2.toString() + "\"",
                    node1.equals(node2));
        }
    }

    protected static class TestMetric implements SingleValueMetric {

        private String name;

        public TestMetric(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public double apply(IColouredGraph graph) {
            return 0;
        }

    }
}
