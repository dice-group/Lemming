/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import org.aksw.simba.lemming.algo.expression.Expression;

/**
 * Implements a fitness based on mean squared error
 *
 * @author ngonga
 */
public class MinSquaredError implements FitnessFunction {

    /**
     * Computes min squared error
     * @param expression Expression
     * @param graphVectors Values for given graphs
     * @return Fitness of expression
     */
    public double getFitness(Expression expression, ObjectDoubleOpenHashMap<String>[] graphVectors) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < graphVectors.length; i++) {
            values.add(expression.getValue(graphVectors[i]));
        }

        double max = getMax(values);
        double fitness = 0d;
        for(int i=0; i<values.size() - 1; i++)
        {
            for(int j= i+1; j < values.size(); j++)
            {
                fitness = fitness + Math.pow((values.get(i)  - values.get(j)), 2);
            }
        }
        fitness = fitness / (max*max); // norm to 1
        fitness = 2*fitness/(values.size()*(values.size()-1)); //compute average
        return (1d-fitness);
    }

    /**
     * Get maximal value from a list of values
     * @param list
     * @return 
     */
    public double getMax(List<Double> list) {
        if (list == null) {
            return Double.NaN;
        }
        if (list.isEmpty()) {
            return Double.NaN;
        }
         double value = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (value < list.get(i)) {
                value = list.get(i);
            }
        }
        return value;
    }
}
