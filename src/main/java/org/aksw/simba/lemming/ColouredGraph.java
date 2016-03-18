package org.aksw.simba.lemming;

import org.aksw.simba.lemming.colour.ColourPalette;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import java.util.ArrayList;
import java.util.List;

public class ColouredGraph {

    protected Grph graph = new InMemoryGrph();
    protected ObjectArrayList<BitSet> vertexColours = new ObjectArrayList<BitSet>();
    protected ObjectArrayList<BitSet> edgeColours = new ObjectArrayList<BitSet>();
    protected ColourPalette vertexPalette;
    protected ColourPalette edgePalette;

    public ColouredGraph() {
    }

    public ColouredGraph(ColourPalette vertexPalette, ColourPalette edgePalette) {
        this.vertexPalette = vertexPalette;
        this.edgePalette = edgePalette;
    }

    public Grph getGraph() {
        return graph;
    }

    public ObjectArrayList<BitSet> getVertexColours() {
        return vertexColours;
    }

    public ObjectArrayList<BitSet> getEdgeColours() {
        return edgeColours;
    }

    public int addVertex() {
        return addVertex(new BitSet());
    }

    public int addVertex(BitSet colour) {
        int id = graph.addVertex();
        vertexColours.add(colour);
        return id;
    }

    public int addEdge(int tail, int head) {
        return addEdge(tail, head, new BitSet());
    }

    public int addEdge(int tail, int head, BitSet colour) {
        int id = graph.addDirectedSimpleEdge(tail, head);
        edgeColours.add(new BitSet());
        return id;
    }

    public void setVertexColour(int vertexId, BitSet colour) {
        if (vertexId < vertexColours.elementsCount) {
            ((Object[]) vertexColours.buffer)[vertexId] = colour;
        }
    }

    public void setEdgeColour(int edgeId, BitSet colour) {
        if (edgeId < edgeColours.elementsCount) {
            ((Object[]) edgeColours.buffer)[edgeId] = colour;
        }
    }

    public BitSet getVertexColour(int vertexId) {
        if (vertexId < vertexColours.elementsCount) {
            return (BitSet) ((Object[]) vertexColours.buffer)[vertexId];
        } else {
            return new BitSet();
        }
    }

    public BitSet getEdgeColour(int edgeId) {
        if (edgeId < vertexColours.elementsCount) {
            return (BitSet) ((Object[]) vertexColours.buffer)[edgeId];
        } else {
            return new BitSet();
        }
    }

    public ColourPalette getVertexPalette() {
        return vertexPalette;
    }

    public ColourPalette getEdgePalette() {
        return edgePalette;
    }
    
    public int[][] getInDegreeNodeArray() {
        return graph.getInNeighborhoods();
    }

    public int[][] getOutDegreeNodeArray() {
        return graph.getOutNeighborhoods();
    }
    
    public int getGraphSize(){
        return graph.getSize();
    }
    
    public int[] getNodeArray() {
        return graph.getVertices().toIntArray();
    }
    
      
}
