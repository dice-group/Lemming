package org.aksw.simba.lemming.metrics.single.triangle;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.collect.Sets;

import toools.set.IntSet;
import toools.set.IntSets;


public class ForwardNumberOfTriangleMetric extends AbstractMetric implements SingleValueMetric {
   private ColouredGraph coloredGraph;

   private List<HashSet<Integer>> adjacencyDatastructure;

   private List<Integer> orderedNodes;
   private int[] nodeToOrderPositionMapping;

   private int amountOfTriangles;


   public ForwardNumberOfTriangleMetric() {
      super("forward #triangles");
   }


   @Override
   public double apply(ColouredGraph coloredGraph) {
      initialize(coloredGraph);
      for (Integer nodeId : orderedNodes) {
         IntSet neighborsOfNodeId = IntSets.union(coloredGraph.getOutNeighbors(nodeId), coloredGraph.getInNeighbors(nodeId));
         processNeighbors(nodeId, neighborsOfNodeId);
      }
      return amountOfTriangles;
   }


   private void processNeighbors(int nodeId, IntSet neighborSet) {
      for (IntCursor adjacentNodeIdCursor : neighborSet) {
         int adjacentNodeId = adjacentNodeIdCursor.value;
         if (isFirstSmallerWithRespectToOrder(nodeId, adjacentNodeId)) {
            amountOfTriangles += Sets.intersection(adjacencyDatastructure.get(nodeId), adjacencyDatastructure.get(adjacentNodeId)).size();
            adjacencyDatastructure.get(adjacentNodeId).add(nodeId);
         }
      }
   }


   /**
    * Checks whether the first node id is smaller than the second node id with respect to the
    * ordering on the nodes.
    * 
    * @param firstNodeId The first node id to check.
    * @param secondNodeId The second node id to check.
    * @return {@code true}, if the first node id is smaller than the second node id with respect to
    *         the ordering on the nodes.
    */
   private boolean isFirstSmallerWithRespectToOrder(int firstNodeId, int secondNodeId) {
      return nodeToOrderPositionMapping[firstNodeId] < nodeToOrderPositionMapping[secondNodeId];
   }


   private void initialize(ColouredGraph coloredGraph) {
      amountOfTriangles = 0;
      this.coloredGraph = coloredGraph;
      adjacencyDatastructure = new ArrayList<>(coloredGraph.getVertices().size());
      for (int i = 0; i < coloredGraph.getVertices().size(); i++) {
         adjacencyDatastructure.add(new HashSet<Integer>());
      }
      initializeNodeOrder();
   }


   private void initializeNodeOrder() {
      orderedNodes = getOrderedNodes();
      nodeToOrderPositionMapping = new int[coloredGraph.getVertices().size()];
      for (int i = 0; i < orderedNodes.size(); i++) {
         nodeToOrderPositionMapping[orderedNodes.get(i)] = i;
      }
   }


   private List<Integer> getOrderedNodes() {
      List<Integer> orderedNodes = coloredGraph.getVertices().toIntegerArrayList().stream()
            .sorted((node1, node2) -> Integer.compare(getTotalAmountOfNeighbors(node2), getTotalAmountOfNeighbors(node1)))
            .collect(Collectors.toList());
      return orderedNodes;
   }


   private int getTotalAmountOfNeighbors(int nodeId) {
      return coloredGraph.getInNeighbors(nodeId).size() + coloredGraph.getOutNeighbors(nodeId).size();
   }


}
