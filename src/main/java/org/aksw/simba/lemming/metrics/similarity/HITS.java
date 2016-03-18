/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.lemming.metrics.similarity;

import grph.Grph;
import java.util.ArrayList;
import org.aksw.simba.lemming.ColouredGraph;

/**
 * Implementation of Hubs and Authorities (HITS) algorithm.
 * @author jsaveta
 */
public class HITS {
    private Grph graph;
   
    public HITS (ColouredGraph colouredGraph){
        this.graph = colouredGraph.getGraph();
        
        ArrayList<HITSNode> S = new ArrayList<HITSNode>();
                
        for(int i: graph.getVertices().toIntegerArrayList()){
            ArrayList<Integer> incoming = graph.getInNeighbors(i).toIntegerArrayList();
            ArrayList<Integer> outgoing = graph.getOutNeighbors(i).toIntegerArrayList();
            S.add(new HITSNode(i, incoming, outgoing));
            //System.out.println("i: "+i +" incoming: "+incoming.toString() +" outgoing: " +outgoing.toString());
        }
        
        computeHITS(S,25); 
//        for (HITSNode x : S) {
//            System.out.println(x.toString());
//        }

        
    }
   
    public void computeHITS(ArrayList<HITSNode> S, int iterations) {
        double norm;
        for (int i = 0; i < iterations; i++) {
            norm = 0;
            for (HITSNode n : S) {
                n.auth = 0;
                for (int j : n.incoming) {
                    n.auth += getNodeWithID(S, j).hub;
                }
                norm += Math.pow(n.auth,2);
            }
            // Normalize authority scores
	    norm = Math.sqrt(norm);
            for (HITSNode n : S) {
                n.auth = n.auth / norm;
            }
            
            
            norm = 0;
            for (HITSNode n : S) {
                n.hub = 0;
                for (int j : n.outgoing) {
                    n.hub += getNodeWithID(S, j).auth;
                }
                norm += Math.pow(n.hub,2);
            }
            
            // Normalize hub scores
	    norm = Math.sqrt(norm);
            for (HITSNode p : S) {
                p.hub = p.hub / norm;
            }
        }
    }
 
    private HITSNode getNodeWithID(ArrayList<HITSNode> S, int ID) {
        for (HITSNode x : S) {
            if (x.ID == ID) {
                return x;
            }
        }
        return null;
    }
}
