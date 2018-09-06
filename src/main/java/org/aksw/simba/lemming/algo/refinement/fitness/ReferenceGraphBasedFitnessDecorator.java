package org.aksw.simba.lemming.algo.refinement.fitness;

import org.aksw.simba.lemming.algo.expression.Expression;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class ReferenceGraphBasedFitnessDecorator extends AbstractFitnessFunctionDecorator {

    protected ObjectDoubleOpenHashMap<String>[] referenceGraphVectors;

    public ReferenceGraphBasedFitnessDecorator(FitnessFunction decorated,
            ObjectDoubleOpenHashMap<String>[] referenceGraphVectors) {
        super(decorated);
        this.referenceGraphVectors = referenceGraphVectors;
    }

    @Override
    public double getFitness(Expression expression, ObjectDoubleOpenHashMap<String>[] graphVectors) {
        double fitness = decorated.getFitness(expression, graphVectors);
        double comparedFitness = 1 - decorated.getFitness(expression, referenceGraphVectors);
//        double comparedFitness = fitness / decorated.getFitness(expression, referenceGraphVectors);
        // Messages needed for debugging
//        System.out.print(expression.toString());
//        System.out.print(" --> f=");
//        System.out.print(fitness);
//        System.out.print(" r=");
//        System.out.print(decorated.getFitness(expression, referenceGraphVectors));
//        System.out.print(" c=");
//        System.out.print(comparedFitness);
//        System.out.print(" v=");
//        System.out.println((2 * fitness * comparedFitness) / (fitness + comparedFitness));
        return (2 * fitness * comparedFitness) / (fitness + comparedFitness);
        // return comparedFitness;
    }

}
