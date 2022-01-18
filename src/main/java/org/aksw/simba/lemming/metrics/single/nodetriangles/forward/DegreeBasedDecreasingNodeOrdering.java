package org.aksw.simba.lemming.metrics.single.nodetriangles.forward;

import java.util.List;
import java.util.stream.Collectors;

import org.aksw.simba.lemming.IColouredGraph;

/**
 * This class models a descending ordering on the node set of a given graph,
 * which is based on the degree of a node. Thus the node with the highest degree
 * is first in the ordering whereas the one with the lowest degree is last on
 * the ordering. Ties are broken according to the sorting mechanism of streams
 * combined with the {@link Integer#compare(int, int)} function.
 *
 * @author Alexander Hetzer
 *         https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/metrics/single/triangle/forward/DegreeBasedDecreasingNodeOrdering.java
 *
 */
public class DegreeBasedDecreasingNodeOrdering {
    private IColouredGraph coloredGraph;

    private List<Integer> orderedNodes;
    private int[] nodeToOrderPositionMapping;

    /**
     * Creates a new {@link DegreeBasedDecreasingNodeOrdering} based on the given
     * {@link IColouredGraph}.
     *
     * @param coloredGraph The {@link IColouredGraph} which this ordering should be
     *                     computed for.
     */
    public DegreeBasedDecreasingNodeOrdering(IColouredGraph coloredGraph) {
        super();
        this.coloredGraph = coloredGraph;
        initialize();
    }

    /**
     * Initializes this {@link DegreeBasedDecreasingNodeOrdering}.
     */
    private void initialize() {
        initializeOrderedNodes();
        initializeNodeToOrderPositionMapping();
    }

    /**
     * Initializes the actual ordering of the nodes.
     */
    private void initializeOrderedNodes() {
        orderedNodes = coloredGraph.getVertices().stream().sorted((node1, node2) -> Integer
                .compare(getTotalAmountOfNeighborsOfNode(node2), getTotalAmountOfNeighborsOfNode(node1)))
                .collect(Collectors.toList());
    }

    /**
     * Returns the total amount of neighbors of a node, i.e. the sum of out and in
     * neighbors.
     *
     * @param nodeId The node whose neighbor count should be returned.
     * @return The total amount of neighbors of a node, i.e. the sum of out and in
     *         neighbors.
     */
    private int getTotalAmountOfNeighborsOfNode(int nodeId) {
        return coloredGraph.getInNeighbors(nodeId).size() + coloredGraph.getOutNeighbors(nodeId).size();
    }

    /**
     * Initializes the node to order position mapping, which can be used to
     * determine which position a given node has in the computed ordering in
     * constant time. Note: This method has to be called after
     * {@link #initializeOrderedNodes()}.
     */
    private void initializeNodeToOrderPositionMapping() {
        nodeToOrderPositionMapping = new int[coloredGraph.getVertices().size()];
        for (int i = 0; i < orderedNodes.size(); i++) {
            nodeToOrderPositionMapping[orderedNodes.get(i)] = i;
        }
    }

    /**
     * Checks whether the first node id is smaller than the second node id with
     * respect to the ordering on the nodes.
     *
     * @param firstNodeId  The first node id to check.
     * @param secondNodeId The second node id to check.
     * @return {@code true}, if the first node id is smaller than the second node id
     *         with respect to the ordering on the nodes.
     */
    public boolean isFirstSmallerWithRespectToOrder(int firstNodeId, int secondNodeId) {
        return nodeToOrderPositionMapping[firstNodeId] < nodeToOrderPositionMapping[secondNodeId];
    }

    /**
     * Returns the nodes of the associated graph sorted according to this order.
     *
     * @return The nodes of the associated graph sorted according to this order.
     */
    public List<Integer> getOrderedNodes() {
        return orderedNodes;
    }

}
