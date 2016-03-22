/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement;

import org.aksw.simba.lemming.algo.expression.Expression;

/**
 * Node for the refinement tree. Contains an expression.
 * 
 * @author ngonga
 */
public class RefinementNode implements Comparable<RefinementNode> {
    public double fitness = -1d;
    public Expression expression;

    public RefinementNode(Expression expression) {
        this.expression = expression;
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

    // FIXME Where does the fitness come from???
}
