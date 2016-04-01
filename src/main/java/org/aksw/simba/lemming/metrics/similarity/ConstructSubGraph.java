package org.aksw.simba.lemming.metrics.similarity;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import java.util.HashMap;
import java.util.Map;
import org.aksw.simba.lemming.ColouredGraph;
import toools.set.IntSet;

/**
 * This class constructs a sample/sub coloured graph only with the selected vertices returned from the ranking
 * @author jsaveta
 */
public class ConstructSubGraph {
    private ColouredGraph colouredGraph;
    private ObjectObjectOpenHashMap vertices; //map with vertex id , colour of vertex
    
    public ConstructSubGraph(ColouredGraph g, ObjectObjectOpenHashMap v){
        this.colouredGraph = g;
        this.vertices = v;
    }
    
    public ColouredGraph construct(){
         //the reason I am keeping the map is to remove vertices that are not selected from ranking
         //and not having null entries to my arrays
        Map<Integer,Integer> vIdOldSample = new HashMap<Integer,Integer>(); // map ids from initial graph with ids from sample graph
        ColouredGraph sample = new ColouredGraph();
        int newVID = 0;
        boolean in = false;
        for (int i = 0; i < vertices.allocated.length; ++i) {
            if (vertices.allocated[i]) {
                //add coloured vertex to new coloured graph
                int vID = (Integer)vertices.keys[i];
//                System.out.println("vID " + vID);
//                System.out.println("vID colour " +(BitSet)vertices.values[i]);
                
                if(!vIdOldSample.containsKey(vID)){ 
                    vIdOldSample.put(vID, newVID);
                } else{ 
                    newVID--; 
                }
                sample.addVertex((BitSet)vertices.values[i]);
                sample.setVertexColour(vIdOldSample.get(vID), (BitSet)vertices.values[i]);
                
                //add out edges 
                IntSet outEdges = colouredGraph.getOutEdges(vID);
                for(IntCursor e: outEdges){
                    BitSet edgeColour = colouredGraph.getEdgeColour(e.value); 
                    IntSet tail = colouredGraph.getVerticesAccessibleThrough(vID, e.value);
                    for(IntCursor v: tail){
                        if(!vIdOldSample.containsKey(v.value)){
                            newVID++;
                            vIdOldSample.put(v.value, newVID);
                        }
                        //set colour for vertex accessible through the starting vertex
                        sample.setVertexColour(vIdOldSample.get(v.value), colouredGraph.getVertexColour(v.value));
                        sample.addEdge(vIdOldSample.get(vID), vIdOldSample.get(v.value), edgeColour);
                    }
                } 
                newVID++;
            }
        }
    return sample;
    }
}
