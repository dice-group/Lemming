package org.aksw.simba.lemming.metrics.dist.multi;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import grph.Grph;
import org.aksw.simba.lemming.ColouredGraph;

/**
 * The set of vertices for each colour
 * @author jsaveta
 */
public class ColouredVerticesMetric {
 
    public ColouredVerticesMetric(){}
    
    public ObjectObjectOpenHashMap<BitSet,IntSet> getVerticesForEachColour(ColouredGraph graph){
        ObjectObjectOpenHashMap<BitSet, IntSet> coloursMap = new ObjectObjectOpenHashMap<BitSet, IntSet>();
        IntSet vertices;
        Grph g = graph.getGraph();
        IntArrayList allVertices = g.getVertices().toIntArrayList();
        BitSet colour;
        for (int i = 0; i < allVertices.elementsCount; ++i) {
            colour = graph.getVertexColour(i);
            if (coloursMap.containsKey(colour)) {
                vertices = coloursMap.lget();
            } else {
                vertices = new IntOpenHashSet();
                coloursMap.put(colour, vertices);
            }
            vertices.add(allVertices.buffer[i]);
        }
        return coloursMap;
    }
    
}
