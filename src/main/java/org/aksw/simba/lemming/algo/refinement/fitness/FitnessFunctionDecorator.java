/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement.fitness;

public interface FitnessFunctionDecorator extends FitnessFunction {

    public FitnessFunction getDecorated();
}
