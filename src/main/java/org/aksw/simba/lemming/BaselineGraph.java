package org.aksw.simba.lemming;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.borrowed.ldcbench.api.GraphGenerator;
import org.aksw.simba.borrowed.ldcbench.api.GrphBasedGraph;
import org.aksw.simba.borrowed.ldcbench.api.ParallelBarabasiRDF;
import org.aksw.simba.lemming.util.Constants;
import com.carrotsearch.hppc.BitSet;

public class BaselineGraph {
	private GrphBasedGraph graph;
	private Map<Integer, BitSet> vertexColourMap;
	private Map<Integer, BitSet> edgeColourMap;
	private long seed;

	public BaselineGraph(int noNodes, double avgDegree, long seed) {
		this.graph = new GrphBasedGraph();
		this.seed = seed;
		this.vertexColourMap = new HashMap<Integer, BitSet>();
		this.edgeColourMap = new HashMap<Integer, BitSet>();

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

	public void setGraph(GrphBasedGraph graph) {
		this.graph = graph;
	}

	public Map<Integer, BitSet> getVertexColourMap() {
		return vertexColourMap;
	}

	public void setVertexColourMap(Map<Integer, BitSet> vertexColourMap) {
		this.vertexColourMap = vertexColourMap;
	}

	public Map<Integer, BitSet> getEdgeColourMap() {
		return edgeColourMap;
	}

	public void setEdgeColourMap(Map<Integer, BitSet> edgeColourMap) {
		this.edgeColourMap = edgeColourMap;
	}

	public long getSeed() {
		return seed;
	}

}
