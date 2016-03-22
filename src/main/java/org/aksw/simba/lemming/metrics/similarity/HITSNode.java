package org.aksw.simba.lemming.metrics.similarity;

import java.util.ArrayList;

/**
 *
 * @author jsaveta
 */
public class HITSNode {
    public int ID;
    public ArrayList<Integer> incoming;
    public ArrayList<Integer> outgoing;
    public double hub;
    public double auth;
 
    public HITSNode(int ID, ArrayList<Integer> incoming, ArrayList<Integer> outgoing) {
        this.ID = ID;
        this.incoming = incoming;
        this.outgoing = outgoing;
        this.hub = 1d;
        this.auth = 1d;
    }
     
    @Override
    public String toString(){
        return "Node "+ID+" - Hub: "+this.hub+" Auth: "+this.auth;
    }
}
