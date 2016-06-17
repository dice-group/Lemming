/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement.fitness;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import org.aksw.simba.lemming.algo.expression.Expression;

/**
 * Extension of min squared error fitness
 * 
 * @author ngonga
 */
public class LengthAwareMinSquaredError extends MinSquaredError {
    public static double FACTOR = 0.1;

    public double getFitness(Expression expression, ObjectDoubleOpenHashMap<String>[] graphVectors) {
        double value = super.getFitness(expression, graphVectors);
        return value - expression.getSize() * FACTOR;
    }
}
