package org.aksw.simba.lemming;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.simba.lemming.util.Constants;
import org.borrowed.ldcbenc.api.GraphGenerator;
import org.borrowed.ldcbenc.api.GrphBasedGraph;
import org.borrowed.ldcbenc.api.ParallelBarabasiRDF;

import com.carrotsearch.hppc.BitSet;

import grph.Grph;
import toools.set.IntSet;

public class BaselineGraph {
	private GrphBasedGraph graph;
	private Map<Integer, BitSet> vertexColourMap;
	private Map<Integer, BitSet> edgeColourMap;
	private Map<BitSet, IntSet> colourVertexIds;
	private Map<BitSet, IntSet> colourEdgeIds;
	private long seed;

	public BaselineGraph(int noNodes, double avgDegree, long seed) {
		this.graph = new GrphBasedGraph();
		this.seed = seed;
		this.vertexColourMap = new HashMap<Integer, BitSet>();
		this.edgeColourMap = new HashMap<Integer, BitSet>();
		this.colourVertexIds = new ConcurrentHashMap<BitSet, IntSet>();
		this.colourEdgeIds = new ConcurrentHashMap<BitSet, IntSet>();

		GraphGenerator generator = new ParallelBarabasiRDF(Constants.BASELINE_STRING);
		generator.generateGraph(noNodes, avgDegree, seed, graph);
	}

	public BaselineGraph(Map<Integer, BitSet> vertexColourMap, Map<Integer, BitSet> edgeColourMap) {
		this.graph = new GrphBasedGraph();
		this.vertexColourMap = vertexColourMap;
		this.edgeColourMap = edgeColourMap;
	}
	
	public GrphBasedGraph getGraph() {
		return graph;
	}

	public Grph getGrph() {
//		CompatibleGraph cGraph = new CompatibleGraph(graph);
//		return cGraph.getGrph();
		return graph.getGraph();
	}

	public Map<Integer, BitSet> getVertexColourMap() {
		return vertexColourMap;
	}

	public Map<Integer, BitSet> getEdgeColourMap() {
		return edgeColourMap;
	}

	public long getSeed() {
		return seed;
	}

	public void setVertexColourMap(Map<Integer, BitSet> vertexColourMap) {
		this.vertexColourMap = vertexColourMap;
	}

	public void setEdgeColourMap(Map<Integer, BitSet> edgeColourMap) {
		this.edgeColourMap = edgeColourMap;
	}

	public Map<BitSet, IntSet> getColourVertexIds() {
		return colourVertexIds;
	}

	public void setColourVertexIds(Map<BitSet, IntSet> colourVertexIds) {
		this.colourVertexIds = colourVertexIds;
	}

	public Map<BitSet, IntSet> getColourEdgeIds() {
		return colourEdgeIds;
	}

	public void setColourEdgeIds(Map<BitSet, IntSet> colourEdgeIds) {
		this.colourEdgeIds = colourEdgeIds;
	}
	
	
}
