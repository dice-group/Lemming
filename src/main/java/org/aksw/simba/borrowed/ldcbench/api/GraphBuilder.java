package org.aksw.simba.borrowed.ldcbench.api;

/**
 *  * TODO add dependency when available in mvn repo, borrowed from
 * {@link  <a href="https://github.com/dice-group/orca/blob/develop/ldcbench.api/src/main/java/org/dice_research/ldcbench/graph/GraphBuilder.java">org.dice_research.ldcbench.graph.GraphBuilder</a>}
 * 
 * An extension of the {@link Graph} interface offering methods for the addition
 * of nodes and edges.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface GraphBuilder extends Graph {

    /**
     * Returns a {@link Graph} instance based on the graph data that the builder
     * collected until now. This instance might offer a faster implementation than
     * the builder class.
     * 
     * @return a {@link Graph} instance containing the built graph.
     */
    public default Graph build() {
        return this;
    }

    /**
     * Adds a new edge with the given information if possible. Returns {@code true}
     * if the edge could be added.
     * 
     * @param sourceId
     *            the id of the source node
     * @param targetId
     *            the id of the target node
     * @param typeId
     *            the type id of the edge
     * @return {@code true} if the edge could be added. Otherwise {@code false} is
     *         returned.
     */
    public boolean addEdge(int sourceId, int targetId, int typeId);

    /**
     * Adds a new node to the graph and returns its ID.
     * 
     * @return the ID of the newly created node
     */
    public int addNode();

    /**
     * Adds the given number of nodes to the graph and returns the range of node
     * IDs.
     * 
     * @param nodeCount
     *            the number of nodes that should be added
     * @return the range of IDs from the first added node ([0], inclusive) to the
     *         first node after the last added node ([1], exclusive).
     */
    public int[] addNodes(int nodeCount);

    /**
     * Sets the IDs of the entrance nodes (i.e., nodes from which all other nodes
     * can be reached).
     * 
     * @param entranceNodes
     *            the IDs of the entrance nodes
     */
    public void setEntranceNodes(int[] entranceNodes);

    /**
     * Sets the graph ID of the given node. By default, all nodes have
     * {@link #INTERNAL_NODE_GRAPH_ID} as graph ID meaning that they are part of this
     * graph. However, to mark external nodes (i.e., nodes that are not part of this
     * graph but are linked from one of the nodes of this graph), this method can be
     * used to store their graph ID as well as their ID in the other graph.
     * 
     * @param nodeId
     *            the ID of the node in this graph
     * @param graphId
     *            the ID of the graph in the other graph. Use
     *            {@link #INTERNAL_NODE_GRAPH_ID} to delete the marking of a node being
     *            external.
     * @param externalId
     *            the ID of this node inside the other graph (only used if the
     *            graphID != {@link #INTERNAL_NODE_GRAPH_ID})
     */
    public void setGraphIdOfNode(int nodeId, int graphId, int externalId);
}
