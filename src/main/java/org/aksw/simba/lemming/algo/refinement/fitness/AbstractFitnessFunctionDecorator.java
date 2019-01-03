package org.aksw.simba.lemming.algo.refinement.fitness;

import org.aksw.simba.lemming.algo.expression.Expression;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Abstract class implementing the basic functionality of a {@link FitnessFunctionDecorator}. 
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class AbstractFitnessFunctionDecorator implements FitnessFunctionDecorator {

    protected FitnessFunction decorated;
    
    public AbstractFitnessFunctionDecorator(FitnessFunction decorated) {
        this.decorated = decorated;
    }

    @Override
    public double getFitness(Expression expression, ObjectDoubleOpenHashMap<String>[] graphVectors) {
        return decorated.getFitness(expression, graphVectors);
    }

    @Override
    public FitnessFunction getDecorated() {
        return decorated;
    }

}
