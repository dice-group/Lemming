/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.expression;

import java.util.Map;
import org.aksw.simba.lemming.ColouredGraph;
import static org.aksw.simba.lemming.algo.expression.Expression.Operator.DIV;
import static org.aksw.simba.lemming.algo.expression.Expression.Operator.MINUS;
import static org.aksw.simba.lemming.algo.expression.Expression.Operator.PLUS;
import static org.aksw.simba.lemming.algo.expression.Expression.Operator.TIMES;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

/**
 * Models expressions that are learned by our algorithm
 *
 * @author ngonga
 */
public class Expression {

    private double constantValue;
    private SingleValueMetric metric;
    private Expression left;
    private Expression right;
    private Operator op;
    private boolean constant;
    private boolean atomic;

    public enum Operator {

        PLUS, MINUS, TIMES, DIV
    };

    public double getConstantValue() {
        return constantValue;
    }

    public SingleValueMetric getMetric() {
        return metric;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public Operator getOp() {
        return op;
    }

    public boolean isConstant() {
        return constant;
    }

    public boolean isAtomic() {
        return atomic;
    }

    /**
     * Initializes a complex expression with a right and left expression
     *
     * @param left Left expression
     * @param right Right expression
     * @param op Operator
     */
    public Expression(Expression left, Expression right, Operator op) {
        this.left = left;
        this.right = right;
        this.op = op;
        constant = false;
        atomic = false;
    }

    public Expression(double value) {
        constantValue = value;
        constant = true;
        atomic = false;
    }

    public Expression(SingleValueMetric metric) {
        this.metric = metric;
        constant = false;
        atomic = true;
    }

    /**
     * Get value for expression given a graph
     *
     * @param cg Coloured Graph
     * @return Double value of expression
     */
    public double getValue(ColouredGraph cg) {
        //if constant return constant value
        if (isConstant()) {
            return constantValue;
        } 
        // if atomic call function
        else if (isAtomic()) {
            return metric.apply(cg);
        } 
        //else combine values
        else {
            double leftValue = left.getValue(cg);
            double rightValue = right.getValue(cg);

            if (op.equals(TIMES)) {
                return leftValue * rightValue;
            }
            if (op.equals(DIV)) {
                return leftValue / rightValue;
            }
            if (op.equals(MINUS)) {
                return leftValue - rightValue;
            }
            return leftValue + rightValue;
        }
    }

    /**
     * Get value for expression given a value map. Useful when values have been
     * precomputed
     *
     * @param valueMap Maps expression names to values
     * @return Double value of expression
     */
    public double getValue(Map<String, Double> valueMap) {
        //if constant return constant value
        if (isConstant()) {
            return constantValue;
        } 
        //if atomic get value for function
        else if (isAtomic()) {
            if (valueMap.containsKey(metric.getName())) {
                return valueMap.get(metric.getName());
            } else {
                return 0d;
            }
        //else combine values
        } else {
            double leftValue = left.getValue(valueMap);
            double rightValue = right.getValue(valueMap);

            if (op.equals(TIMES)) {
                return leftValue * rightValue;
            }
            if (op.equals(DIV)) {
                return leftValue / rightValue;
            }
            if (op.equals(MINUS)) {
                return leftValue - rightValue;
            }
            if (op.equals(MINUS)) {
                return leftValue + rightValue;
            }
            else return Double.NaN;
        }
    }
    
    /**
     * String representation of an expression
     * @return 
     */
    public String toString()
    {
        if(isConstant()) return constantValue+"";
        if(isAtomic()) return metric.getName();
        else return "("+left.toString() + " "+ op + " "+right.toString()+")";
    }
    
    public static void main(String args[])
    {
        Expression e1 = new Expression(new DiameterMetric());
        Expression e2 = new Expression(new NumberOfEdgesMetric());
        Expression e3 = new Expression(e1, e2, PLUS);
        System.out.println(e3.toString());
        ColouredGraph g = new ColouredGraph();
        int i = g.addVertex();
        int j = g.addVertex();
        g.addEdge(i, j);        
        System.out.println(e1.getValue(g));
        System.out.println(e2.getValue(g));
        System.out.println(e3.getValue(g));
    }
}
