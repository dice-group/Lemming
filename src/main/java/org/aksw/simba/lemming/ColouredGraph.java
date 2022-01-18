package org.aksw.simba.lemming;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.grph.DiameterAlgorithm;
import org.aksw.simba.lemming.util.Constants;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;

import grph.DefaultIntSet;
import grph.Grph;
import grph.GrphAlgorithmCache;
import grph.algo.MultiThreadProcessing;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ColouredGraph implements IColouredGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(ColouredGraph.class);

    protected Grph graph;
    protected ObjectArrayList<BitSet> vertexColours = new ObjectArrayList<BitSet>();
    protected ObjectArrayList<BitSet> edgeColours = new ObjectArrayList<BitSet>();
    protected ColourPalette vertexPalette;
    protected ColourPalette edgePalette;
    protected ColourPalette dtEdgePalette;

    /**
     * 1st key: vertex ID, 2nd key: data typed property and the values is the value
     * of the literal
     */
    protected Map<Integer, Map<BitSet, List<String>>> mapVertexIdAndLiterals;

    /**
     * map for storing type of literal accordingly to the data typed property edge
     */
    protected Map<BitSet, String> mapLiteralTypes;

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

        mapVertexIdAndLiterals = new HashMap<>();
        mapLiteralTypes = new HashMap<>();
    }

    public ColouredGraph(ColourPalette vertexPalette, ColourPalette edgePalette, ColourPalette datatypedEdgePalette) {
        this(new InMemoryGrph(), vertexPalette, edgePalette, datatypedEdgePalette);
    }

    public ColouredGraph(Grph graph, ColourPalette vertexPalette, ColourPalette edgePalette,
            ColourPalette datatypedEdgePalette) {
        setGraph(graph);
        this.vertexPalette = vertexPalette;
        this.edgePalette = edgePalette;
        this.dtEdgePalette = datatypedEdgePalette;

        mapVertexIdAndLiterals = new HashMap<Integer, Map<BitSet, List<String>>>();
        mapLiteralTypes = new HashMap<BitSet, String>();
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

    public void removeEdge(int edgeId) {
        // Since we are using an array list, we can not simply remove the edge as this
        // would move all the other edge colours. Therefore, we set the colour to null
        // to indicate that the edge does not exist.
        edgeColours.set(edgeId, null);
        graph.removeEdge(edgeId);
    }

    public int addEdge(int tail, int head, BitSet colour) {
        int edgeId = graph.addDirectedSimpleEdge(tail, head);

        if (edgeColours.elementsCount > edgeId) {
            edgeColours.set(edgeId, colour);
        } else {
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
            // return new BitSet();
            return null;
        }
    }

    public ColourPalette getVertexPalette() {
        return vertexPalette;
    }

    public ColourPalette getEdgePalette() {
        return edgePalette;
    }

    public ColourPalette getDataTypedEdgePalette() {
        return dtEdgePalette;
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

    public IntSet getEdges() {
        return graph.getEdges();
    }
    
    public double getAverageDegree() {
    	return graph.getAverageDegree();
    }

    public IntSet getOutEdges(int vertexId) {
        return graph.getOutEdges(vertexId);
    }

    public IntSet getInEdges(int vertexId) {
        return graph.getInEdges(vertexId);
    }

    public IntSet getVerticesAccessibleThrough(int vertexId, int edgeId) {
    	return graph.getVerticesAccessibleThrough(vertexId, edgeId);
    }

    public double getDiameter() {
        return diameterAlgorithm.compute(graph);
    }

    /**
     * get edges connecting two specific vertices
     * 
     * @param tailId
     * @param headId
     * @return
     */
    public IntSet getEdgesConnecting(int tailId, int headId) {
        return graph.getEdgesConnecting(tailId, headId);
    }

    /**
     * for testing graph with visualization
     * 
     */
    public void visualizeGraph() {
        graph.display();
    }

    /**
     * Set new data to the mapping of vertex's ids and vertex's colours
     * 
     * @param inVertexColours
     */
    public void setVertexColours(ObjectArrayList<BitSet> inVertexColours) {
        vertexColours = new ObjectArrayList<>(inVertexColours);
    }

    /**
     * Set new data to the mapping of edge's ids and edge's colours
     * 
     * @param inEdgeColours
     */
    public void setEdgeColours(ObjectArrayList<BitSet> inEdgeColours) {
        edgeColours = new ObjectArrayList<>(inEdgeColours);
    }
    
    /**
     * Set new data to the mapping of vertex's ids and vertex's colours
     * Note: vertex ids should be from 0 to map's size-1 continuously
     * @param inVertexColours
     */
    public void setVertexColours(Map<Integer, BitSet> inVertexColours) {
        vertexColours = new ObjectArrayList<>();
        for (int i = 0; i < inVertexColours.size(); ++i) {
            vertexColours.add(inVertexColours.get(i));
        }
    }

    /**
     * Set new data to the mapping of edge's ids and edge's colours
     * Note: edge ids should be from 0 to map'size-1 continuously
     * @param inEdgeColours
     */
    public void setEdgeColours(Map<Integer, BitSet> inEdgeColours) {
        edgeColours = new ObjectArrayList<>();
        for (int i = 0; i < inEdgeColours.size(); ++i) {
            edgeColours.add(inEdgeColours.get(i));
        }
    }

    /**
     * Clone the current instance of coloured graph to a new object
     */
    @Override
    public ColouredGraph clone() {
        Grph rawClonedGrph = new InMemoryGrph();
        rawClonedGrph.addVertices(graph.getVertices());
        new MultiThreadProcessing(graph.getEdges()) {

            @Override
            protected void run(int threadID, int edgeID) {
                synchronized (rawClonedGrph) {
                    IntSet vertexIDs = graph.getVerticesIncidentToEdge(edgeID);
                    int[] arrVertIDs = vertexIDs.toIntArray();
                    if (arrVertIDs.length == 2) {
                        rawClonedGrph.addDirectedSimpleEdge(arrVertIDs[0], edgeID, arrVertIDs[1]);
                    } else {
                        if (arrVertIDs.length == 1) {
                            rawClonedGrph.addDirectedSimpleEdge(arrVertIDs[0], edgeID, arrVertIDs[0]);
                        } else {
                            System.out.println(" -- edge id : " + edgeID + " has only " + arrVertIDs.length + "");
                        }
                    }
                }
            }
        };

        ColouredGraph cloneGrph = new ColouredGraph(rawClonedGrph, vertexPalette, edgePalette, dtEdgePalette);
        cloneGrph.setVertexColours(vertexColours);
        cloneGrph.setEdgeColours(edgeColours);

        // --------------------------------------------------------
        // TODO set literal of the old graph to the new graph here
        // May be not necessary, since all original graphs are
        // not cloned instead of the mimic graph and the mimic graph
        // has not had any literals yet
        // --------------------------------------------------------

        return cloneGrph;
    }

    public ColouredGraph copy() {
        return new ColouredGraph(graph.clone(), vertexPalette, edgePalette);
    }

    public int getTailOfTheEdge(int edgeId) {
        return graph.getDirectedSimpleEdgeTail(edgeId);
    }

    public int getHeadOfTheEdge(int edgeId) {
        return graph.getDirectedSimpleEdgeHead(edgeId);
    }

    /**
     * Get list of vertex ID's based on a colour
     * 
     * @param vertexColour
     *            the colour whoes vertex IDs we want to get
     * @return
     */
    public IntSet getVertices(BitSet vertexColour) {
        IntSet setVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
     
        new MultiThreadProcessing(this.getVertices()) {

            @Override
            protected void run(int threadID, int vertId) {
                BitSet vertColo = getVertexColour(vertId);
                if (vertexColour.equals(vertColo)) {
                    synchronized (setVertices) {
                        setVertices.add(vertId);
                    }
                }
            }
        };

        return setVertices;
    }

    /**
     * Add a literal associated with its data typed property to the data store
     * 
     * @param literal
     *            a literal
     * @param tId
     *            the vertex id which has the data typed property
     * @param dteColo
     *            the colour of the data typed property connecting to the vertex.
     */
    public void addLiterals(String literal, int tId, BitSet dteColo, String datatype) {

        if (dteColo == null) {
            LOGGER.warn("Cannot generate for datatype property " + dteColo + "(" + tId + ")");
            return;
        }

        Map<BitSet, List<String>> mapDTEColoursToLiterals = mapVertexIdAndLiterals.get(tId);
        if (mapDTEColoursToLiterals == null) {
            mapDTEColoursToLiterals = new HashMap<BitSet, List<String>>();
            mapVertexIdAndLiterals.put(tId, mapDTEColoursToLiterals);
        }

        List<String> lstOfLiterals = mapDTEColoursToLiterals.get(dteColo);
        if (lstOfLiterals == null) {
            lstOfLiterals = new ArrayList<String>();
            mapDTEColoursToLiterals.put(dteColo, lstOfLiterals);
        }

        lstOfLiterals.add(literal);

        if (datatype == null || datatype.isEmpty()) {
            LOGGER.error("datatype is null");
            return;
        }

        // TODO check this part again, since one literal may have different data type.
        // this should load from the supporting file which contains all datatype of
        // literal
        String origDataType = mapLiteralTypes.get(dteColo);
        if (origDataType == null
                || (origDataType != null && !origDataType.equals(datatype) && origDataType.contains("#string"))) {
            mapLiteralTypes.put(dteColo, datatype);
        }
    }

    /**
     * Get list of all vertex IDs connecting to the the edgeId
     * 
     * @param edgeId
     *            the id of an edge connecting 1-2 vertices together
     * @return set of vertex ID's
     */
    public IntSet getVerticesIncidentToEdge(int edgeId) {
        return graph.getVerticesIncidentToEdge(edgeId);
    }

    /**
     * Get the set of data typed edge's colours which are connected to the vertex's
     * colour
     * 
     * @param vertexColour
     *            the colour of the target vertex
     * @return a set of linked edge's colours
     */
    public Set<BitSet> getDataTypedEdgeColours(BitSet vertexColour) {
        Map<BitSet, List<String>> mapDTEColoToLiterals = mapVertexIdAndLiterals.get(vertexColour);
        if (mapDTEColoToLiterals != null) {
            return mapDTEColoToLiterals.keySet();
        }
        return null;
    }

    /**
     * get a dummy URI for a specific vertex
     * 
     * @param vId
     *            the identification of the vertex
     * 
     * @return a dummy URI
     */
    public String getResourceDummyURI(int vId) {
        return Constants.SIMULATED_URI + vId;
    }

    /**
     * get classes of a resource based on it colour
     * 
     * @param vColo
     *            the resource's colour
     * 
     * @return a list of URIs
     */
    public Set<String> getResourceClass(BitSet vColo) {
        // isDebugging for evaluation
        Set<String> setOfURIs = vertexPalette.getURIs(vColo, false);
        if (Constants.IS_EVALUATION_MODE) {
            return setOfURIs;
        }
        // in case for practical, we will use a dummy Class URL
        else {
            Set<String> dummyURIs = new HashSet<String>();
            for (int i = 0; i < setOfURIs.size(); i++) {
                dummyURIs.add(Constants.SIMULATED_CLASS_URI + vColo + "_" + i);
            }
            return dummyURIs;
        }
    }

    public Set<BitSet> getClassColour(BitSet vColo) {
        Set<String> setOfURIs = vertexPalette.getURIs(vColo, false);
        Set<BitSet> setClassColours = new HashSet<BitSet>();

        if (setOfURIs != null && setOfURIs.size() > 0) {
            for (String uri : setOfURIs) {
                setClassColours.add(vertexPalette.getColour(uri));
            }
        }

        return setClassColours;
    }

    public String getPropertyURI(BitSet eColo) {
        // isDebugging for evaluation
        if (Constants.IS_EVALUATION_MODE) {
            Set<String> setOfURIs = edgePalette.getURIs(eColo, true);
            if (setOfURIs.size() > 0) {
                for (String uri : setOfURIs)
                    return uri;
            }
        }
        return Constants.SIMULATED_PROPERTY_URI + eColo;
    }

    public String getDataTypedPropertyURI(BitSet dteColo) {
        // isDebugging for evaluation
        if (Constants.IS_EVALUATION_MODE) {
            Set<String> setofURIs = dtEdgePalette.getURIs(dteColo, true);
            if (setofURIs.size() == 1) {
                for (String uri : setofURIs) {
                    return uri;
                }
            } else {
                LOGGER.warn("This " + dteColo + " colour (datatype edge colour) has more than 1 URI");
            }
        }
        return Constants.SIMULATED_DATA_TYPED_PROPERTY_URI + dteColo;
    }

    public Map<BitSet, IntSet> getMapDTEdgeColoursToVertexIDs() {
        Map<BitSet, IntSet> res = new HashMap<BitSet, IntSet>();
        if (mapVertexIdAndLiterals != null && mapVertexIdAndLiterals.size() > 0) {
            IntSet setOfVIDs = getVertices();
            int[] arrOfVIDs = setOfVIDs.toIntArray();

            for (int vId : arrOfVIDs) {
                Map<BitSet, List<String>> mapDTEColoursToVIDs = mapVertexIdAndLiterals.get(vId);
                if (mapDTEColoursToVIDs != null && mapDTEColoursToVIDs.size() > 0) {
                    Set<BitSet> setOfDTEColours = mapDTEColoursToVIDs.keySet();

                    for (BitSet dteColo : setOfDTEColours) {

                        IntSet setOfLinkedVIDs = res.get(dteColo);
                        if (setOfLinkedVIDs == null) {
                            setOfLinkedVIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
                            res.put(dteColo, setOfLinkedVIDs);
                        }
                        setOfLinkedVIDs.add(vId);
                    }
                }
            }
        }
        return res;
    }

    public Map<BitSet, List<String>> getMapDTEdgeColoursToLiterals(int vertexId) {
        if (mapVertexIdAndLiterals.containsKey(vertexId)) {
            return mapVertexIdAndLiterals.get(vertexId);
        }
        return null;
    }

    public Map<BitSet, Set<String>> getMapDTEdgeColoursToLiterals() {
        Map<BitSet, Set<String>> res = new HashMap<BitSet, Set<String>>();
        // set of vertices that have literals
        Set<Integer> setOfVIDs = mapVertexIdAndLiterals.keySet();
        for (Integer vId : setOfVIDs) {
            // map of literals and their corresponding different values
            Map<BitSet, List<String>> mapDTEColoursToLiterals = mapVertexIdAndLiterals.get(vId);

            // set of literals
            Set<BitSet> setOfDTEColours = mapDTEColoursToLiterals.keySet();

            for (BitSet dteColo : setOfDTEColours) {
                List<String> lstOfLiterals = mapDTEColoursToLiterals.get(dteColo);

                Set<String> setOfAllLiterals = res.get(dteColo);
                if (setOfAllLiterals == null) {
                    setOfAllLiterals = new HashSet<String>();
                    res.put(dteColo, setOfAllLiterals);
                }

                setOfAllLiterals.addAll(lstOfLiterals);
            }
        }

        return res;
    }

    /**
     * get literals based on the datatype edge colours associated with a specific
     * tail colour.
     * 
     * @return a map of literals where, 1st key is datatype edge colour, 2nd key is
     *         tail colour, and value is a set of string values.
     */
    public Map<BitSet, Map<BitSet, Set<String>>> getMapLiterals() {

        // 1st key: dteColo, 2nd key: tColo, value: set of literals
        Map<BitSet, Map<BitSet, Set<String>>> res = new HashMap<BitSet, Map<BitSet, Set<String>>>();
        // set of vertices that have literals
        Set<Integer> setOfVIDs = mapVertexIdAndLiterals.keySet();
        for (Integer vId : setOfVIDs) {
            // map of literals and their corresponding different values
            Map<BitSet, List<String>> mapDTEColoursToLiterals = mapVertexIdAndLiterals.get(vId);

            if (mapDTEColoursToLiterals != null && mapDTEColoursToLiterals.size() > 0) {
                BitSet vColo = getVertexColour(vId);
                // set of literals
                Set<BitSet> setOfDTEColours = mapDTEColoursToLiterals.keySet();

                for (BitSet dteColo : setOfDTEColours) {
                    List<String> lstOfLiterals = mapDTEColoursToLiterals.get(dteColo);
                    if (lstOfLiterals == null || lstOfLiterals.isEmpty()) {
                        continue;
                    }

                    Map<BitSet, Set<String>> setOfAllLiterals = res.get(dteColo);
                    if (setOfAllLiterals == null) {
                        setOfAllLiterals = new HashMap<BitSet, Set<String>>();
                        res.put(dteColo, setOfAllLiterals);
                    }

                    Set<String> setOfLiterals = setOfAllLiterals.get(vColo);
                    if (setOfLiterals == null) {
                        setOfLiterals = new HashSet<String>();
                        setOfAllLiterals.put(vColo, setOfLiterals);
                    }
                    setOfLiterals.addAll(lstOfLiterals);
                }
            }
        }

        return res;
    }

    public void setEdgePalette(ColourPalette newEdgePalette) {
        edgePalette = newEdgePalette;
    }

    public void setVertexPalette(ColourPalette newVertexPalette) {
        vertexPalette = newVertexPalette;
    }

    public void setDataTypeEdgePalette(ColourPalette newDTEdgePalette) {
        dtEdgePalette = newDTEdgePalette;
    }

    public String getLiteralType(BitSet dteColo) {
        return mapLiteralTypes.get(dteColo);
    }

    public boolean isRDFTypeEdge(int outEdgeId) {
        BitSet oeColo = getEdgeColour(outEdgeId);
        if (oeColo != null) {
            return edgePalette.isColourOfRDFType(oeColo);
        }
        return false;
    }

    public BitSet getRDFTypePropertyColour() {
        return edgePalette.getColour(RDF.type.toString());
    }

    public Set<String> getOrginalResourceURIs(BitSet vColo) {
        return vertexPalette.getURIs(vColo, false);
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ColouredGraph other = (ColouredGraph) obj;
		if (dtEdgePalette == null) {
			if (other.dtEdgePalette != null) {
				return false;
			}
		} else if (!dtEdgePalette.equals(other.dtEdgePalette)) {
			return false;
		}
		if (edgeColours == null) {
			if (other.edgeColours != null) {
				return false;
			}
		} else if (!edgeColours.equals(other.edgeColours)) {
			return false;
		}
		if (edgePalette == null) {
			if (other.edgePalette != null) {
				return false;
			}
		} else if (!edgePalette.equals(other.edgePalette)) {
			return false;
		}
		if (graph == null) {
			if (other.graph != null) {
				return false;
			}
		} else if (!graph.equals(other.graph)) {
			return false;
		}
		if (vertexColours == null) {
			if (other.vertexColours != null) {
				return false;
			}
		} else if (!vertexColours.equals(other.vertexColours)) {
			return false;
		}
		if (vertexPalette == null) {
			if (other.vertexPalette != null) {
				return false;
			}
		} else if (!vertexPalette.equals(other.vertexPalette)) {
			return false;
		}
		return true;
	}


    /**
     * Get list of all Edge IDs connecting to vertex
     *
     * @param verticeId - verticeId the id of an vertex
     * @return IntSet - set of edge IDs
     */
    public IntSet getEdgesIncidentTo(int verticeId) {
        return graph.getEdgesIncidentTo(verticeId);
    }

    /**
     * Get in edge degree of a vertex
     *
     * @param verticeId - the id of an vertex
     * @return int - in edge degree value
     */
    @Override
    public int getInEdgeDegree(int vertexId) {
        return graph.getInEdgeDegree(vertexId);
    }

    /**
     * Get out edge degree of a vertex
     *
     * @param verticeId - the id of an vertex
     * @return int - out edge degree value
     */
    @Override
    public int getOutEdgeDegree(int vertexId) {
        return graph.getOutEdgeDegree(vertexId);
    }

    /**
     * Get max in edge degree of the graph
     * 
     * @return double
     */
    @Override
    public double getMaxInEdgeDegrees() {
        return graph.getMaxInEdgeDegrees();
    }

    /**
     * Get max out edge degree of the graph
     * 
     * @return double
     */
    @Override
    public double getMaxOutEdgeDegrees() {
        return graph.getMaxOutEdgeDegrees();
    }

    /**
     * Get in edge degrees of all the vertices
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllInEdgeDegrees() {
        return graph.getAllInEdgeDegrees();
    }

    /**
     * Get out edge degrees of all the vertices
     * 
     * @return IntArrayList
     */
    @Override
    public IntArrayList getAllOutEdgeDegrees() {
        return graph.getAllOutEdgeDegrees();
    }

    /**
     * Get number of edges in the graph
     * 
     * @return double
     */
    @Override
    public double getNumberOfEdges() {
        return graph.getNumberOfEdges();
    }

    /**
     * Get number of nodes in the graph
     * 
     * @return double
     */
    @Override
    public double getNumberOfVertices() {
        return graph.getNumberOfVertices();
    }

    @Override
    public int getNumberOfEdgesBetweenVertices(int headId, int tailId) {
        int counter = 0;
        for (int edgeId : getEdgesIncidentTo(tailId)) {
            if (getEdgesIncidentTo(headId).contains(edgeId)) {
                counter++;
            }
        }
        return counter;
    }
}
