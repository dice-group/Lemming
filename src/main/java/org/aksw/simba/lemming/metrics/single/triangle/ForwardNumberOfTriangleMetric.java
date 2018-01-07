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

   private List<HashSet<Integer>> adjacencyDatastructure;
   private ColouredGraph coloredGraph;

   private int amountOfTriangles;


   public ForwardNumberOfTriangleMetric() {
      super("forward #triangles");
   }


   @Override
   public double apply(ColouredGraph coloredGraph) {
      initialize(coloredGraph);
      for (Integer nodeId : getOrderedNodes()) {
         processNeighbors(nodeId, IntSets.union(coloredGraph.getOutNeighbors(nodeId), coloredGraph.getInNeighbors(nodeId)));
      }
      return amountOfTriangles;
   }


   private void processNeighbors(int nodeId, IntSet neighborSet) {
      for (IntCursor adjacentNodeIdCursor : neighborSet) {
         int adjacentNodeId = adjacentNodeIdCursor.value;
         if (nodeId < adjacentNodeId) {
            amountOfTriangles += Sets.intersection(adjacencyDatastructure.get(nodeId), adjacencyDatastructure.get(adjacentNodeId)).size();
            adjacencyDatastructure.get(adjacentNodeId).add(nodeId);
         }
      }
   }


   private void initialize(ColouredGraph coloredGraph) {
      amountOfTriangles = 0;
      this.coloredGraph = coloredGraph;
      adjacencyDatastructure = new ArrayList<>(coloredGraph.getVertices().size());
      for (int i = 0; i < coloredGraph.getVertices().size(); i++) {
         adjacencyDatastructure.add(new HashSet<Integer>());
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
