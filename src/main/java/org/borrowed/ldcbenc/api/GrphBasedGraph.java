package org.borrowed.ldcbenc.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntIterator;
import toools.set.IntSet;

/**
 *  TODO add dependency when available in mvn repo, borrowed from
 * {@link  <a href="https://github.com/dice-group/orca/blob/develop/ldcbench.api/src/main/java/org/dice_research/ldcbench/graph/GrphBasedGraph.java">org.dice_research.ldcbench.graph.GrphBasedGraph</a>}
 *
 */
public class GrphBasedGraph implements GraphBuilder {

    /**
     * Index of the graph ID in the array used as value in {@link #externalNodes}.
     */
    protected static final int EXTERNAL_NODE_GRAPH_ID_INDEX = 0;
    /**
     * Index of the external node ID in the array used as value in
     * {@link #externalNodes}.
     */
    protected static final int EXTERNAL_NODE_NODE_ID_INDEX = 1;

    protected Grph graph = new InMemoryGrph();
    protected ArrayList<Integer> edgeTypes = new ArrayList<>();
    /**
     * Entrance nodes of this graph.
     */
    protected int[] entranceNodes = new int[0];
    /**
     * Storage of additional information (graph ID and node ID in this graph) of
     * external nodes.
     */
    protected Map<Integer, int[]> externalNodes = new HashMap<>();

    /**
     * Constructor for an empty GraphBuilder.
     *
     * @return an instance with an empty graph.
     */
    public GrphBasedGraph() {
    }

    /**
     * Clones speficied graph's data into this GraphBuilder.
     *
     * @return an instance with the specified graph cloned.
     */
    public GrphBasedGraph(Graph other) {
        int nodes = other.getNumberOfNodes();
        addNodes(nodes);
        for (int i = 0; i < nodes; i++) {
            int[] targets = other.outgoingEdgeTargets(i);
            int[] types = other.outgoingEdgeTypes(i);
            for (int j = 0; j < targets.length; j++) {
                addEdge(i, targets[j], types[j]);
            }
            if (other.getGraphId(i) != Graph.INTERNAL_NODE_GRAPH_ID) {
                setGraphIdOfNode(i, other.getGraphId(i), other.getExternalNodeId(i));
            }
        }
        setEntranceNodes(other.getEntranceNodes());
    }

    /**
     * Given an edge ID, returns type of that edge.
     *
     * @param edge
     *            the edge ID
     * @return the edge type
     */
    private int getEdgeType(int edge) {
        return edgeTypes.get(edge);
    }

    /**
     * Returns a Stream containing edges that have the given node as source sorted
     * by edge ID. Used to construct arrays of edge targets and edge types in the
     * same order.
     *
     * @param nodeId
     *            the source node ID of the edges
     * @return the IDs of the edges
     */
    private IntStream orderedOutgoingEdges(int nodeId) {
        int[] edges = graph.getOutEdges(nodeId).toIntArray();
        Arrays.sort(edges);
        return Arrays.stream(edges);
    }

    /**
     * Returns a Stream containing edges that have the given node as target sorted
     * by edge ID. Used to construct arrays of edge sources and edge types in the
     * same order.
     *
     * @param nodeId
     *            the target node ID of the edges
     * @return the IDs of the edges
     */
    private IntStream orderedIncomingEdges(int nodeId) {
        int[] edges = graph.getInEdges(nodeId).toIntArray();
        Arrays.sort(edges);
        return Arrays.stream(edges);
    }

    public int[] outgoingEdgeTargets(int nodeId) {
        return orderedOutgoingEdges(nodeId).map(graph::getDirectedSimpleEdgeHead).toArray();
    }

    public int[] outgoingEdgeTypes(int nodeId) {
        return orderedOutgoingEdges(nodeId).map(this::getEdgeType).toArray();
    }

    public int[] incomingEdgeSources(int nodeId) {
        return orderedIncomingEdges(nodeId).map(graph::getDirectedSimpleEdgeTail).toArray();
    }

    public int[] incomingEdgeTypes(int nodeId) {
        return orderedIncomingEdges(nodeId).map(this::getEdgeType).toArray();
    }

    public int getNumberOfNodes() {
        return graph.getNumberOfVertices();
    }

    public int getNumberOfEdges() {
        return graph.getNumberOfEdges();
    }

    public boolean addEdge(int sourceId, int targetId, int typeId) {
        // Do not add the edge if any of incident vertices are missing.
        if (!(graph.containsVertex(sourceId) && graph.containsVertex(targetId))) {
            return false;
        }

        // Do not add the edge if there's already another edge with the same type.
        for (int edge : graph.getEdgesConnecting(sourceId, targetId).toIntArray()) {
            if (edgeTypes.get(edge) == typeId) {
                return false;
            }
        }

        int edge = graph.addSimpleEdge(sourceId, targetId, true);
        try {
            edgeTypes.set(edge, typeId);
        } catch (IndexOutOfBoundsException e) {
            // Assume new edge IDs are sequential. Otherwise this will throw.
            edgeTypes.add(edge, typeId);
        }
        return true;
    }

    public IntSet getEdgesConnecting(int sourceId, int targetId) {
    	return graph.getEdgesConnecting(sourceId, targetId);
    }

    public int addNode() {
        return graph.addVertex();
    }

    /**
     * Adds a new node with specified ID to the graph. Returns {@code true} if the
     * node could be added.
     *
     * @param nodeId
     *            the id of the node
     * @return {@code true} if the node could be added. Otherwise {@code false} is
     *         returned.
     */
    public boolean addNode(int nodeId) {
        if (graph.containsVertex(nodeId)) {
            return false;
        }

        graph.addVertex(nodeId);
        return true;
    }



    @Override
    public int[] addNodes(int nodeCount) {
        IntSet ids = graph.addNVertices(nodeCount);
        int min = Integer.MAX_VALUE, max = -1;
        Iterator<IntCursor> iterator = ids.iterator();
        int id;
        while (iterator.hasNext()) {
            id = iterator.next().value;
            if (min > id) {
                min = id;
            }
            if (max < id) {
                max = id;
            }
        }
        return new int[] { min, max + 1 };
    }

    @Override
    public void setEntranceNodes(int[] entranceNodes) {
        this.entranceNodes = entranceNodes;
    }

    @Override
    public int[] getEntranceNodes() {
        return entranceNodes;
    }

    @Override
    public int getGraphId(int nodeId) {
        Integer n = nodeId;
        if (externalNodes.containsKey(n)) {
            int[] externalNode = externalNodes.get(n);
            if ((externalNode != null) && (externalNode.length > EXTERNAL_NODE_GRAPH_ID_INDEX)) {
                return externalNode[EXTERNAL_NODE_GRAPH_ID_INDEX];
            }
        }
        return INTERNAL_NODE_GRAPH_ID;
    }

    @Override
    public int getExternalNodeId(int nodeId) {
        Integer n = nodeId;
        if (externalNodes.containsKey(n)) {
            int[] externalNode = externalNodes.get(n);
            if ((externalNode != null) && (externalNode.length > EXTERNAL_NODE_NODE_ID_INDEX)) {
                return externalNode[EXTERNAL_NODE_NODE_ID_INDEX];
            }
        }
        return nodeId;
    }

    @Override
    public void setGraphIdOfNode(int nodeId, int graphId, int externalId) {
        Integer n = nodeId;
        if (externalNodes.containsKey(n)) {
            // Reuse the existing array if possible
            int[] externalNode = externalNodes.get(n);
            if ((externalNode != null) && (externalNode.length > EXTERNAL_NODE_NODE_ID_INDEX)) {
                externalNode[EXTERNAL_NODE_GRAPH_ID_INDEX] = graphId;
                externalNode[EXTERNAL_NODE_NODE_ID_INDEX] = externalId;
                // The information has been updated. So let's leave the method
                return;
            }
        }
        // There is nothing we can reuse so create a new array
        externalNodes.put(n, new int[] { graphId, externalId });
    }

    @Override
    public String toString() {
        return graph.toString();
    }

	public IntSet getVerticesIncidentToEdge(int edgeId) {
		return graph.getVerticesIncidentToEdge(edgeId);
	}

	public Grph getGraph() {
		return graph;
	}
}