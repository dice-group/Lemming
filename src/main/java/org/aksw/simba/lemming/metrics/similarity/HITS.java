package org.aksw.simba.lemming.metrics.similarity;

import java.util.ArrayList;
import org.aksw.simba.lemming.ColouredGraph;

/**
 * Implementation of Hubs and Authorities (HITS) algorithm.
 * @author jsaveta
 */
public class HITS {
   
    public HITS (ColouredGraph colouredGraph){
        ArrayList<HITSNode> S = new ArrayList<HITSNode>();
                
        for(int i: colouredGraph.getVertices().toIntArray()){
            ArrayList<Integer> incoming = colouredGraph.getInNeighbors(i).toIntegerArrayList();
            ArrayList<Integer> outgoing = colouredGraph.getOutNeighbors(i).toIntegerArrayList();
            S.add(new HITSNode(i, incoming, outgoing));
            //System.out.println("i: "+i +" incoming: "+incoming.toString() +" outgoing: " +outgoing.toString());
        }
        
        computeHITS(S,25); 
        
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
