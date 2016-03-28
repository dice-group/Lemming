package org.aksw.simba.lemming.metrics.similarity;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import org.aksw.simba.lemming.ColouredGraph;
import toools.set.IntSet;

/**
 * This class constructs a sample/sub coloured graph only with the selected vertices returned from the ranking
 * @author jsaveta
 */
public class ConstructSubGraph {
    private ColouredGraph colouredGraph;
    private ObjectObjectOpenHashMap vertices;
    
    public ConstructSubGraph(ColouredGraph g, ObjectObjectOpenHashMap v){
        this.colouredGraph = g;
        this.vertices = v;
    }
    
    public ColouredGraph construct(){
        ColouredGraph sample = new ColouredGraph();
        
        for (int i = 0; i < vertices.allocated.length; ++i) {
            if (vertices.allocated[i]) {
                //add coloured vertex to new coloured graph
                int vID = (Integer)vertices.keys[i];
                sample.setVertexColour(vID, (BitSet)vertices.values[i]);
                
                //add out edges 
                IntSet outEdges = colouredGraph.getOutEdges(vID);
                for(IntCursor e: outEdges){
                    BitSet edgeColour = colouredGraph.getEdgeColour(e.value); //check this
                    IntSet tail = colouredGraph.getVerticesAccessibleThrough(vID, e.value);
                    for(IntCursor v: tail){
                        sample.addEdge(vID, v.value, edgeColour);
                    }
                }
            }
        }
    return sample;
    }
}
