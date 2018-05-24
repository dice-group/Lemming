package org.aksw.simba.lemming;

import java.util.HashSet;
import java.util.Set;

import grph.Grph;
import grph.GrphAlgorithmCache;
import grph.algo.MultiThreadProcessing;
import grph.in_memory.InMemoryGrph;

import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.grph.DiameterAlgorithm;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class ColouredGraph{

	protected Grph graph;
	protected ObjectArrayList<BitSet> vertexColours = new ObjectArrayList<BitSet>();
	protected ObjectArrayList<BitSet> edgeColours = new ObjectArrayList<BitSet>();
	protected ColourPalette vertexPalette;
	protected ColourPalette edgePalette;
	protected ColourPalette dtEdgePalette;
	
	//mapping vertex's colours to datatyped edge's colours
	protected ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapVertexColourToDataTypedEdge;
	//mapping datatyped edge's colours to set of literals
	protected ObjectObjectOpenHashMap<BitSet, Set<String>> mapDataTypedEdgeColourToLiterals;	
	
	protected GrphAlgorithmCache<Integer> diameterAlgorithm;

	public ColouredGraph() {
		this(null, null, null);
	}
	
//	public ColouredGraph() {
//		this(null, null);
//	}

//	public ColouredGraph(ColourPalette vertexPalette, ColourPalette edgePalette) {
//		this(new InMemoryGrph(), vertexPalette, edgePalette);
//	}
	
//	public ColouredGraph(Grph graph, ColourPalette vertexPalette, ColourPalette edgePalette) {
//		setGraph(graph);
//		this.vertexPalette = vertexPalette;
//		this.edgePalette = edgePalette;
//	}

//	public ColouredGraph(ColourPalette vertexPalette, ColourPalette edgePalette, ColourPalette datatypedEdgePalette) {
//		this(new InMemoryGrph(), vertexPalette, edgePalette);
//		dtEdgePalette = datatypedEdgePalette;
//	}
	
	public ColouredGraph(ColourPalette vertexPalette, ColourPalette edgePalette, ColourPalette datatypedEdgePalette) {
		this(new InMemoryGrph(), vertexPalette, edgePalette, datatypedEdgePalette);
	}
	
	public ColouredGraph(Grph graph, ColourPalette vertexPalette, ColourPalette edgePalette, ColourPalette datatypedEdgePalette) {
		setGraph(graph);
		this.vertexPalette = vertexPalette;
		this.edgePalette = edgePalette;
		this.dtEdgePalette = datatypedEdgePalette;
		
		mapVertexColourToDataTypedEdge = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
		mapDataTypedEdgeColourToLiterals = new ObjectObjectOpenHashMap<BitSet, Set<String>>();
	}
	
	public void removeEdge(int edgeId){
		edgeColours.set(edgeId, null);
		IntSet edgeIDs = graph.getEdges();
		if(edgeIDs.contains(edgeId)){
			graph.removeEdge(edgeId);
		}
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
		int edgeId = graph.addDirectedSimpleEdge(tail, head);

		if(edgeColours.elementsCount > edgeId){
			edgeColours.set(edgeId, colour);
		}else{
			edgeColours.add(colour);
		}
		
		return edgeId;
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
		if (edgeId < edgeColours.elementsCount) {
			return (BitSet) ((Object[]) edgeColours.buffer)[edgeId];
		} else {
			//return new BitSet();
			return null;
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

	public IntSet getEdges(){
		return graph.getEdges();
	}
	
	public IntSet getOutEdges(int vertexId) {
		return graph.getOutEdges(vertexId);
	}

	public IntSet getInEdges(int vertexId){
		return graph.getInEdges(vertexId);
	}
	
	public IntSet getVerticesAccessibleThrough(int vertexId, int edgeId) {
		return graph.getVerticesAccessibleThrough(vertexId, edgeId);
	}

	public int getDiameter() {
		return diameterAlgorithm.compute(graph);
	}

	/**
	 * get edges connecting two specific vertices
	 * 
	 * @param tailId
	 * @param headId
	 * @return
	 */
	public IntSet getEdgesConnecting(int tailId, int headId){
		return graph.getEdgesConnecting(tailId, headId);
	}
	
	/**
	 * for testing graph with visualization
	 * 
	 */
	public void visualizeGraph(){
		graph.display();
	}
	
	public void setVertexColours(ObjectArrayList<BitSet> inVertexColours){
		vertexColours = new ObjectArrayList<BitSet>();
		for(int i = 0 ; i < inVertexColours.size() ; ++i){
			vertexColours.add(inVertexColours.get(i));
		}
	}
	
	public void setEdgeColours(ObjectArrayList<BitSet> inEdgeColours){
		edgeColours = new ObjectArrayList<BitSet>();
		for(int i = 0 ; i < inEdgeColours.size() ; ++i){
			edgeColours.add( inEdgeColours.get(i));
		}
	}
	
	@Override
	public ColouredGraph clone(){
		Grph rawClonedGrph = new InMemoryGrph();
		rawClonedGrph.addVertices(graph.getVertices());		
		new MultiThreadProcessing(graph.getEdges()) {
			
			@Override
			protected void run(int threadID, int edgeID) {
				synchronized(rawClonedGrph){
					IntSet vertexIDs = graph.getVerticesIncidentToEdge(edgeID);
					int[] arrVertIDs = vertexIDs.toIntArray();
					if(arrVertIDs.length == 2){
						rawClonedGrph.addDirectedSimpleEdge(arrVertIDs[0], edgeID, arrVertIDs[1]);
					}else{
						if(arrVertIDs.length == 1){
							rawClonedGrph.addDirectedSimpleEdge(arrVertIDs[0], edgeID, arrVertIDs[0]);
						}else{
							System.out.println(" -- edge id : "+ edgeID +" has only " + arrVertIDs.length+ "");
						}
					}
				}
			}
		};
		
		ColouredGraph cloneGrph = new ColouredGraph(rawClonedGrph, vertexPalette, edgePalette, dtEdgePalette);
		cloneGrph.setVertexColours(vertexColours);
		cloneGrph.setEdgeColours(edgeColours);
		// TODO set literal of the old graph to the new graph here
		//--------------------------------------------------------
		
		return cloneGrph;
	}
	
	public int getTailOfTheEdge(int edgeId){
		IntSet vertices = graph.getVerticesIncidentToEdge(edgeId);
		if(vertices.size() > 0){
			int [] arrVertIDs = vertices.toIntArray();
			return arrVertIDs[0];
		}
		return -1;
	}
	
	public int getHeadOfTheEdge(int edgeId){
		IntSet vertices = graph.getVerticesIncidentToEdge(edgeId);
		if(vertices.size() > 0 ){
		int [] arrVertIDs = vertices.toIntArray();
			if( arrVertIDs.length == 1 ){
				return arrVertIDs[0];
			}else{
				return arrVertIDs[1];
			}
		}
		return -1;
	}
	
	public IntSet getVertices(BitSet vertexColour){
		IntSet setVertices = new DefaultIntSet();
		
		new MultiThreadProcessing(this.getVertices()) {
			
			@Override
			protected void run(int threadID, int vertId) {
				BitSet vertColo = getVertexColour(vertId);
				if(vertexColour.equals(vertColo)){
					synchronized(setVertices){
						setVertices.add(vertId);
					}
				}
			}
		};
		
		return setVertices;
	}
	
	public void addLiterals(String literal, BitSet subjectColour, BitSet datatypedEdgeColour){
		
		Set<BitSet> setDTEdgeColours = mapVertexColourToDataTypedEdge.get(subjectColour);
		if(setDTEdgeColours == null){
			setDTEdgeColours = new HashSet<BitSet>();
			mapVertexColourToDataTypedEdge.put(subjectColour, setDTEdgeColours);
		}
		setDTEdgeColours.add(datatypedEdgeColour);
		
		Set<String> setLiterals = mapDataTypedEdgeColourToLiterals.get(datatypedEdgeColour);
		if(setLiterals == null){
			setLiterals = new HashSet<String>();
			mapDataTypedEdgeColourToLiterals.put(datatypedEdgeColour, setLiterals);
		}
		
		setLiterals.add(literal);
	}
	
	public IntSet getVerticesIncidentToEdge(int edgeId){
		return graph.getVerticesIncidentToEdge(edgeId);
	}
}
