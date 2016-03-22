/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.algo.refinement;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class RefinementTree {

    private Set<RefinementNode> nodes;
    private Set<RefinementNode> firstStageNodes;

    public RefinementTree(Set<RefinementNode> firstStageNodes) {
        this.firstStageNodes = firstStageNodes;
        nodes = new HashSet<RefinementNode>(firstStageNodes);
    }

    public Set<RefinementNode> getFirstStageNodes() {
        return firstStageNodes;
    }

    public Set<RefinementNode> getNodes() {
        return nodes;
    }
}
