package org.aksw.simba.lemming;

import grph.Grph;
import grph.GrphAlgorithmCache;
import grph.algo.MultiThreadProcessing;
import grph.in_memory.InMemoryGrph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	
	/**
	 * this is new for processing the literals in the original RDF dataset
	 */
	//mapping vertex's colours to datatyped edge's colours
	protected ObjectObjectOpenHashMap<BitSet, Set<BitSet>> mapVertexColoursToDataTypedEdgeColours;
	//mapping datatyped edge's colours to set of literals
	protected ObjectObjectOpenHashMap<BitSet, Set<String>> mapDTEdgeColoursToLiterals;
	// map data typed edge's colours to set of connected vertex' ids
	protected ObjectObjectOpenHashMap<BitSet, IntSet> mapDTEdgeColoursToVertexIDs;
	
	protected Map<Integer, Map<BitSet, List<String>>> mapVertexIdAndLiterals;
	
	
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
		
		mapVertexIdAndLiterals = new HashMap<Integer, Map<BitSet, List<String>>>();
		
		mapVertexColoursToDataTypedEdgeColours = new ObjectObjectOpenHashMap<BitSet, Set<BitSet>>();
		mapDTEdgeColoursToLiterals = new ObjectObjectOpenHashMap<BitSet, Set<String>>();
		mapDTEdgeColoursToVertexIDs = new ObjectObjectOpenHashMap<BitSet, IntSet>();
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
	
	/**
	 * Set new data to the mapping of vertex's ids and vertex's colours
	 * @param inVertexColours
	 */
	public void setVertexColours(ObjectArrayList<BitSet> inVertexColours){
		vertexColours = new ObjectArrayList<BitSet>();
		for(int i = 0 ; i < inVertexColours.size() ; ++i){
			vertexColours.add(inVertexColours.get(i));
		}
	}
	/**
	 * Set new data to the mapping of edge's ids and edge's colours 
	 * @param inEdgeColours
	 */
	public void setEdgeColours(ObjectArrayList<BitSet> inEdgeColours){
		edgeColours = new ObjectArrayList<BitSet>();
		for(int i = 0 ; i < inEdgeColours.size() ; ++i){
			edgeColours.add( inEdgeColours.get(i));
		}
	}
	
	/**
	 * Clone the current instance of coloured graph to a new object
	 */
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
		
		//--------------------------------------------------------
		// TODO set literal of the old graph to the new graph here
		// May be not necessary, since all original graphs are
		// not cloned instead of the mimic graph and the mimic graph
		// has not had any literals yet
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
	
	/**
	 * Get list of vertex ID's based on a colour
	 * @param vertexColour the colour whoes vertex IDs we want to get
	 * @return
	 */
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
	
	/**
	 * Add a literal associated with the data typed property to the data store
	 * @param literal a literal
	 * @param tailId the vertex id which has the data typed property
	 * @param dtEdgeColour the data typed property's colour connecting to the vertex
	 */
	public void addLiterals(String literal, int tailId, BitSet dtEdgeColour){
		
		BitSet vertColo = getVertexColour(tailId);
		
		Set<BitSet> setDTEdgeColours = mapVertexColoursToDataTypedEdgeColours.get(vertColo);
		if(setDTEdgeColours == null){
			setDTEdgeColours = new HashSet<BitSet>();
			mapVertexColoursToDataTypedEdgeColours.put(vertColo, setDTEdgeColours);
		}
		setDTEdgeColours.add(dtEdgeColour);
		
		Set<String> setLiterals = mapDTEdgeColoursToLiterals.get(dtEdgeColour);
		if(setLiterals == null){
			setLiterals = new HashSet<String>();
			mapDTEdgeColoursToLiterals.put(dtEdgeColour, setLiterals);
		}
		setLiterals.add(literal);
		
		IntSet setVertexIDs = mapDTEdgeColoursToVertexIDs.get(dtEdgeColour);
		if(setVertexIDs == null){
			setVertexIDs = new DefaultIntSet();
			mapDTEdgeColoursToVertexIDs.put(dtEdgeColour, setVertexIDs);
		}
		setVertexIDs.add(tailId);
	}
	
	/**
	 * Get list of all vertex IDs connecting to the the edgeId
	 * @param edgeId the id of an edge connecting 1-2 vertices together
	 * @return set of vertex ID's
	 */
	public IntSet getVerticesIncidentToEdge(int edgeId){
		return graph.getVerticesIncidentToEdge(edgeId);
	}
	
	/**
	 * Get a set of all associated data typed properties with the given vertex's colour
	 * @param vertexColour the colour of the vertex having data typed properties
	 * @return
	 */
	public Set<BitSet> getSetDTEdgeColours(BitSet vertexColour){
		if(mapVertexColoursToDataTypedEdgeColours != null){
			return mapVertexColoursToDataTypedEdgeColours.get(vertexColour);
		}
		return null;
	}
	
	/**
	 * Return all literals belonging to the data typed properties
	 * @param dtEdgeColour the data typed property's colour
	 * @return
	 */
	public Set<String> getSetLiterals(BitSet dtEdgeColour){
		if(mapDTEdgeColoursToLiterals != null ){
			return mapDTEdgeColoursToLiterals.get(dtEdgeColour);
		}
		return null;
	}
	
	/**
	 * Get a map of the data typed properties with their sets of associated literals based on the vertex's color
	 * 
	 * @param vertexColour the colour of the vertex which has the data typed properties
	 * @return
	 */
	public ObjectObjectOpenHashMap<BitSet, Set<String>> getMapDTEdgeColoursToLiterals(BitSet vertexColour){
		ObjectObjectOpenHashMap mapRes = new ObjectObjectOpenHashMap<BitSet, Set<String>>();
		if(mapVertexColoursToDataTypedEdgeColours != null && mapDTEdgeColoursToLiterals != null) {
			Set<BitSet> setDTEdgeColours = mapVertexColoursToDataTypedEdgeColours.get(vertexColour);
			if(setDTEdgeColours != null && setDTEdgeColours.size() > 0 ){
				for(BitSet dtEdgeColo : setDTEdgeColours){
					Set<String> setLiterals = mapDTEdgeColoursToLiterals.get(dtEdgeColo);
					if(setLiterals != null && setLiterals.size() > 0 ){
						//just for testing
						if(mapRes.containsKey(dtEdgeColo)){
							System.err.println("getMapDTEdgeColoursToLiterals has serious errors");
							return null;
						}
						
						mapRes.put(dtEdgeColo, setLiterals);
					}
				}
			}
		}
		return mapRes;
	}
	
	/**
	 * Get the map of data typed properties with the set of all associated literals
	 * @return
	 */
	public ObjectObjectOpenHashMap<BitSet, Set<String>> getMapDTEdgeColoursToLiterals(){
		return mapDTEdgeColoursToLiterals;
	}
	
	/**
	 * get the map of data typed properties with the set of vertex IDs
	 * @return a map
	 */
	public ObjectObjectOpenHashMap<BitSet, IntSet> getMapDTEdgeColoursToVertexIDs(){
		return mapDTEdgeColoursToVertexIDs;
	}
	
	public String getResourceURI(BitSet vColo){
		return vertexPalette.getURI(vColo);
	}
	
	public String getPropertyURI(BitSet eColo){
		return edgePalette.getURI(eColo);
	}
	
	public String getDataTypedPropertyURI(BitSet dteColo){
		return dtEdgePalette.getURI(dteColo); 
	}
	
}
