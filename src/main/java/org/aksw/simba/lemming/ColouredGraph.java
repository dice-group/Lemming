package org.aksw.simba.lemming;

import grph.Grph;
import grph.GrphAlgorithmCache;
import grph.in_memory.InMemoryGrph;

import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.grph.DiameterAlgorithm;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;

public class ColouredGraph {

	protected Grph graph;
	protected ObjectArrayList<BitSet> vertexColours = new ObjectArrayList<BitSet>();
	protected ObjectArrayList<BitSet> edgeColours = new ObjectArrayList<BitSet>();
	protected ColourPalette vertexPalette;
	protected ColourPalette edgePalette;

	protected GrphAlgorithmCache<Integer> diameterAlgorithm;

	public ColouredGraph() {
		this(null, null);
	}

	public ColouredGraph(ColourPalette vertexPalette, ColourPalette edgePalette) {
		this(new InMemoryGrph(), vertexPalette, edgePalette);
	}

	public ColouredGraph(Grph graph, ColourPalette vertexPalette, ColourPalette edgePalette) {
		setGraph(graph);
		this.vertexPalette = vertexPalette;
		this.edgePalette = edgePalette;

	}

	public Grph getGraph() {
		return graph;
	}

	protected void setGraph(Grph graph) {
		this.graph = graph;
		diameterAlgorithm = new DiameterAlgorithm().cacheResultForGraph(graph);
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

	public int[][] getInNeighborhoodsArray() {
		return graph.getInNeighborhoods();
	}

	public int[][] getOutNeighborhoodsArray() {
		return graph.getOutNeighborhoods();
	}

	public IntSet getInNeighbors(int v) {
		return graph.getInNeighbors(v);
	}

	public IntSet getOutNeighbors(int v) {
		return graph.getOutNeighbors(v);
	}

	public IntSet getVertices() {
		return graph.getVertices();
	}

	public IntSet getOutEdges(int vertexId) {
		return graph.getOutEdges(vertexId);
	}

	public IntSet getVerticesAccessibleThrough(int vertexId, int edgeId) {
		return graph.getVerticesAccessibleThrough(vertexId, edgeId);
	}

	public int getDiameter() {
		return diameterAlgorithm.compute(graph);
	}

}
