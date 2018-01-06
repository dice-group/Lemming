package org.aksw.simba.lemming.metrics.single.triangle;


import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.cursors.IntCursor;

import toools.set.IntSet;


public class NodeIteratorCoreNumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

   public NodeIteratorCoreNumberOfTrianglesMetric() {
      super("node-iterator-core #triangles");
   }


   @Override
   public double apply(ColouredGraph graph) {
      Set<Integer> visitedVertices = new HashSet<>();
      int numberOfTriangles = 0;

      while (visitedVertices.size() < graph.getVertices().size()) {
         int nodeWithMinimumDegree = getNodeWithMinimumDegree(graph, visitedVertices);
         IntSet neighbors = getNeighbors(graph, visitedVertices, nodeWithMinimumDegree);

         for (IntCursor neighbor1 : neighbors) {
            IntSet neighbors1 = getNeighbors(graph, visitedVertices, neighbor1.value);
            for (IntCursor neighbor2 : neighbors) {
               if (nodeWithMinimumDegree != neighbor2.value && neighbor1.value != neighbor2.value) {
                  if (neighbor1.value < neighbor2.value && neighbors1.contains(neighbor2.value)) {
                     numberOfTriangles++;
                  }
               }
            }
         }
         visitedVertices.add(nodeWithMinimumDegree);
      }
      return numberOfTriangles;
   }


   private int getNodeWithMinimumDegree(ColouredGraph graph, Set<Integer> visitedVertices) {
      int nodeWithMinimumDegree = Integer.MAX_VALUE;
      int minimalDegree = Integer.MAX_VALUE;

      for (IntCursor vertex : graph.getVertices()) {
         if (!visitedVertices.contains(vertex.value)) {
            IntSet neighbors = getNeighbors(graph, visitedVertices, vertex.value);

            if (neighbors.size() < minimalDegree) {
               minimalDegree = neighbors.size();
               nodeWithMinimumDegree = vertex.value;
            }
         }
      }
      return nodeWithMinimumDegree;
   }


   private IntSet getNeighbors(ColouredGraph graph, Set<Integer> visitedVertices, int vertex) {
      IntSet allNeighbors = graph.getInNeighbors(vertex);
      allNeighbors.addAll(graph.getOutNeighbors(vertex));

      IntSet neighbors = allNeighbors.clone();
      for (IntCursor neighbor : allNeighbors) {
         if (visitedVertices.contains(neighbor.value)) {
            neighbors.remove(neighbor.value);
         }
      }

      return neighbors;
   }

}
