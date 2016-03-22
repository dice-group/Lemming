/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement;

import org.aksw.simba.lemming.algo.expression.Expression;

/**
 * Node for the refinement tree. Contains an expression.
 * @author ngonga
 */
public class RefinementNode implements Comparable {
    public double fitness = -1d; 
    public Expression expression;
    
    public RefinementNode(Expression expression)
    {
        this.expression = expression;
    }
    
    /**
     * Implements comparison
     * @param o Some object
     * @return 0 if o is not an instance of refinement node. Else compares the fitness values.
     */
    public int compareTo(Object o) {
        if(o instanceof RefinementNode)
        {
            RefinementNode node = (RefinementNode)o;
            if(fitness < node.fitness) return -1;
            if(fitness > node.fitness) return 1;            
        }
        return 0;
    }
}
