/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.Expression;

/**
 * Node for the refinement tree. Contains an expression.
 * 
 * @author ngonga
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 */
public class RefinementNode implements Comparable<RefinementNode> {

    public double fitness = -1d;
    public final Expression expression;
    public final String reducedExpression;
    public final Set<RefinementNode> children;
    public RefinementNode parent;

    public RefinementNode(Expression expression, String reducedExpression) {
        this(expression, reducedExpression, null);
    }

    public RefinementNode(Expression expression, String reducedExpression, RefinementNode parent) {
        this.expression = expression;
        this.reducedExpression = reducedExpression;
        children = new HashSet<RefinementNode>();
        this.parent = parent;
    }

    /**
     * Implements comparison
     * 
     * @param o
     *            a {@link RefinementNode} with which this object should be
     *            compared.
     * @return -1 if the fitness value of this object is smaller than the one of
     *         the other value; 1 if the fitness value of this object is larger;
     *         else 0.
     */
    @Override
    public int compareTo(RefinementNode o) {
        RefinementNode node = (RefinementNode) o;
        if (fitness < node.fitness)
            return -1;
        if (fitness > node.fitness)
            return 1;
        return 0;
    }

    @Override
    public int hashCode() {
        return reducedExpression.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RefinementNode other = (RefinementNode) obj;
        if (!reducedExpression.equals(other.reducedExpression))
            return false;
        return true;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    public void setParent(RefinementNode parent) {
        this.parent = parent;
    }

    public RefinementNode getParent() {
        return parent;
    }

    public Set<RefinementNode> getChildren() {
        return children;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return reducedExpression;
    }
}
