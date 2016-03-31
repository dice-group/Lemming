/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement.fitness;

import org.aksw.simba.lemming.algo.expression.Expression;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Implements a fitness based on mean squared error
 *
 * @author ngonga
 */
public class MinSquaredError implements FitnessFunction {

    /**
     * Computes min squared error
     * 
     * @param expression
     *            Expression
     * @param graphVectors
     *            Values for given graphs
     * @return Fitness of expression
     */
    public double getFitness(Expression expression, ObjectDoubleOpenHashMap<String>[] graphVectors) {
        double values[] = new double[graphVectors.length];
        for (int i = 0; i < graphVectors.length; i++) {
            values[i] = Math.abs(expression.getValue(graphVectors[i]));
        }

        double max = getMax(values);
        double fitness = 0d;
        for (int i = 0; i < values.length - 1; i++) {
            for (int j = i + 1; j < values.length; j++) {
                fitness = fitness + Math.pow(values[i] - values[j], 2);
            }
        }
        if (fitness == 0) {
            return 1;
        }
        fitness = fitness / (max * max); // norm to 1
        fitness = 2 * fitness / (values.length * (values.length - 1)); // compute
                                                                       // average
        return (1d - fitness);
    }

    /**
     * Get maximal value from a list of values
     * 
     * @param list
     * @return
     */
    public double getMax(double values[]) {
        if (values == null) {
            return Double.NaN;
        }
        if (values.length == 0) {
            return Double.NaN;
        }
        double value = values[0];
        for (int i = 1; i < values.length; i++) {
            if (value < values[i]) {
                value = values[i];
            }
        }
        return value;
    }
}
