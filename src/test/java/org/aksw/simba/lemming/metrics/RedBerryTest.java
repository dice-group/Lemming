package org.aksw.simba.lemming.metrics;

import cc.redberry.core.solver.ReduceEngine;
import cc.redberry.core.solver.ReducedSystem;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;

public class RedBerryTest {

    public static void main(String[] args) {
        Expression[] equations = { Tensors.parseExpression("x / x = 0"), };

        SimpleTensor[] vars = { Tensors.parseSimple("x"), Tensors.parseSimple("y") };

        Transformation[] transformations = {};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        System.out.println(rd.getEquations()[0].toString());
    }
}
