package org.aksw.simba.lemming.metrics.similarity;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.HashMap;
import java.util.Map;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.MapUtil;
import static org.aksw.simba.lemming.util.MapUtil.getKeyByValue;

/**
 * This class constructs a sample/sub coloured graph only with the selected vertices returned from the ranking
 * @author jsaveta
 */
public class ConstructSubGraph {
    private ColouredGraph colouredGraph;
    private ObjectObjectOpenHashMap vertices; //map with vertex id , colour of vertex
    Map<Integer,Integer> vIdOldSample; // map ids from initial graph with ids from sample graph
        
    
    public ConstructSubGraph(ColouredGraph g, ObjectObjectOpenHashMap v){
        this.colouredGraph = g;
        this.vertices = v;
    }

    public ColouredGraph construct(){
         //the reason I am keeping the map is to remove vertices that are not selected from ranking
         //and not having null entries to my arrays
        vIdOldSample = new HashMap<Integer,Integer>(); // ids from sample graph, ids from initial graph 
        ColouredGraph sample = new ColouredGraph();
        int newVID = 0;
        for (int i = 0; i < vertices.allocated.length; ++i) {
            if (vertices.allocated[i]) {
                //add coloured vertex to new coloured graph if it has out edges
                int vID = (Integer)vertices.keys[i];
                IntSet outEdges = colouredGraph.getOutEdges(vID);
                if(!outEdges.isEmpty()){
                    if(!vIdOldSample.containsKey(newVID)){ 
                        vIdOldSample.put(newVID, vID);
                    } else{ 
                        newVID--; 
                    }
                    sample.addVertex((BitSet)vertices.values[i]);
                    sample.setVertexColour(getKeyByValue(vIdOldSample, vID), (BitSet)vertices.values[i]);

                    //add out edges          
                    for(int e: outEdges){
                        BitSet edgeColour = colouredGraph.getEdgeColour(e); 
                        for(int v:colouredGraph.getVerticesAccessibleThrough(vID, e)){
                            if(!vIdOldSample.containsValue(v)){
                                newVID++;
                                vIdOldSample.put(newVID, v);
                            }
                            sample.setVertexColour(MapUtil.getKeyByValue(vIdOldSample,v), colouredGraph.getVertexColour(v));
                            sample.addEdge(MapUtil.getKeyByValue(vIdOldSample, vID), MapUtil.getKeyByValue(vIdOldSample,v), edgeColour);
                        }
                    } 
                newVID++;
                }    
            }
        }
    return sample;
    }
    
    public Map<Integer,Integer> getIdCorrespondence(){
        return this.vIdOldSample;
    }
    
    

}
