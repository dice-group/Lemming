/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement.fitness;

import org.aksw.simba.lemming.algo.expression.Expression;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Interface for fitness functions
 * @author ngonga
 */
public interface FitnessFunction {
    /**
     * Computes the fitness function for a given node
     * 
     * @param expression
     * @param graphVectors
     * @return
     */
    public double getFitness(Expression expression, ObjectDoubleOpenHashMap<String>[] graphVectors);
}
