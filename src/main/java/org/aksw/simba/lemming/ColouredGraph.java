package org.aksw.simba.lemming;

import grph.Grph;
import grph.GrphAlgorithmCache;
import grph.algo.MultiThreadProcessing;
import grph.in_memory.InMemoryGrph;

import java.util.ArrayList;
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
	 * @param tId the vertex id which has the data typed property
	 * @param dteColo the data typed property's colour connecting to the vertex
	 */
	public void addLiterals(String literal, int tId, BitSet dteColo){
		
		Map<BitSet, List<String>> mapDTEColoursToLiterals = mapVertexIdAndLiterals.get(tId);
		if(mapDTEColoursToLiterals == null){
			mapDTEColoursToLiterals = new HashMap<BitSet, List<String>> ();
			mapVertexIdAndLiterals.put(tId, mapDTEColoursToLiterals);
		}
		
		List<String> lstOfLiterals = mapDTEColoursToLiterals.get(dteColo);
		if(lstOfLiterals == null){
			lstOfLiterals = new ArrayList<String>();
			mapDTEColoursToLiterals.put(dteColo, lstOfLiterals);
		}
		
		lstOfLiterals.add(literal);
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
	 * Get the set of data typed edge's colours which are connected to the vertex's colour
	 * @param vertexColour 
	 * @return a set of linked edge's colours
	 */
	public Set<BitSet> getDataTypedEdgeColours (BitSet vertexColour){
		Map<BitSet, List<String>> mapDTEColoToLiterals = mapVertexIdAndLiterals.get(vertexColour);
		if(mapDTEColoToLiterals != null){
			return mapDTEColoToLiterals.keySet();
		}
		return null;
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
	
	public Map<BitSet, IntSet> getMapDTEdgeColoursToVertexIDs(){
		Map<BitSet, IntSet> res =new HashMap<BitSet, IntSet>();
		if(mapVertexIdAndLiterals != null && mapVertexIdAndLiterals.size() > 0 ){
			IntSet setOfVIDs = getVertices();
			int[] arrOfVIDs = setOfVIDs.toIntArray();
			
			for(int vId: arrOfVIDs){
				Map<BitSet, List<String>> mapDTEColoursToVIDs = mapVertexIdAndLiterals.get(vId); 
				if(mapDTEColoursToVIDs != null && mapDTEColoursToVIDs.size() > 0 ){
					Set<BitSet> setOfDTEColours = mapDTEColoursToVIDs.keySet();
					
					for(BitSet dteColo : setOfDTEColours){
						
						IntSet setOfLinkedVIDs = res.get(dteColo);
						if(setOfLinkedVIDs ==null){
							setOfLinkedVIDs = new DefaultIntSet();
							res.put(dteColo, setOfLinkedVIDs);
						}
						setOfLinkedVIDs.add(vId);
					}
				}
			}
		}
		return res;
	}
	
	public Map<BitSet, List<String>> getMapDTEdgeColoursToLiterals(int vertexId){
		if(mapVertexIdAndLiterals.containsKey(vertexId) ){
			return mapVertexIdAndLiterals.get(vertexId);
		}
		return null;
	}
	
	public Map<BitSet, Set<String>> getMapDTEdgeColoursToLiterals(){
		Map<BitSet, Set<String>> res = new HashMap<BitSet, Set<String>>();
		Set<Integer> setOfVIDs = mapVertexIdAndLiterals.keySet();
		for(Integer vId: setOfVIDs){
			Map<BitSet, List<String>> mapDTEColoursToLiterals = mapVertexIdAndLiterals.get(vId);
			Set<BitSet> setOfDTEColours = mapDTEColoursToLiterals.keySet();
			
			for(BitSet dteColo: setOfDTEColours){
				List<String> lstOfLiterals = mapDTEColoursToLiterals.get(dteColo);
				
				Set<String> setOfAllLiterals = res.get(dteColo);
				if(setOfAllLiterals == null){
					setOfAllLiterals = new HashSet<String>();	
					res.put(dteColo, setOfAllLiterals);
				}
				
				setOfAllLiterals.addAll(lstOfLiterals);
			}
		}
		
		return res;
	}
}
