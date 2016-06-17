package org.aksw.simba.lemming.algo.refinement.fitness;

import org.aksw.simba.lemming.algo.expression.Expression;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class ReferenceGraphBasedFitnessDecorator implements FitnessFunctionDecorator {

    protected FitnessFunction decorated;
    protected ObjectDoubleOpenHashMap<String>[] referenceGraphVectors;

    public ReferenceGraphBasedFitnessDecorator(FitnessFunction decorated,
            ObjectDoubleOpenHashMap<String>[] referenceGraphVectors) {
        this.decorated = decorated;
        this.referenceGraphVectors = referenceGraphVectors;
    }

    @Override
    public double getFitness(Expression expression, ObjectDoubleOpenHashMap<String>[] graphVectors) {
        double fitness = decorated.getFitness(expression, graphVectors);
        double comparedFitness = fitness / decorated.getFitness(expression, referenceGraphVectors);
        return (2 * fitness * comparedFitness) / (fitness + comparedFitness);
        // return comparedFitness;
    }

    @Override
    public FitnessFunction getDecorated() {
        return decorated;
    }

}
