/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement;

import java.util.Map;
import java.util.Set;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Interface for fitness functions
 * @author ngonga
 */
public interface FitnessFunction {
    /**
     * Computes the fitness function for a given node
     * @param tree
     * @param node
     * @param values
     * @return 
     */
    double getFitness(RefinementTree tree, RefinementNode node, Set<Map<String, Double>> values);

    public double getFitness(RefinementNode node, ObjectDoubleOpenHashMap<String>[] graphVectors);
}
