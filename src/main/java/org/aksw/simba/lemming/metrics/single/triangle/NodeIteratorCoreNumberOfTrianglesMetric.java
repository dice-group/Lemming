package org.aksw.simba.lemming.metrics.single.triangle;


import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.cursors.IntCursor;


public class NodeIteratorCoreNumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

   public NodeIteratorCoreNumberOfTrianglesMetric() {
      super("node-iterator-core #triangles");
   }


   @Override
   public double apply(ColouredGraph graph) {
      Set<Integer> visitedVertices = new HashSet<>();
      int numberOfTriangles = 0;

      int[] degrees = new int[graph.getVertices().size()];
      for (IntCursor vertex : graph.getVertices()) {
         degrees[vertex.index] = graph.getInNeighbors(vertex.value).size();
         degrees[vertex.index] += graph.getOutNeighbors(vertex.value).size();
      }

      while (visitedVertices.size() < graph.getVertices().size() - 1) {
         int nodeWithMinimumDegree = getNodeWithMinimumDegree(degrees);
         Set<Integer> neighbors = getNeighbors(graph, visitedVertices, nodeWithMinimumDegree);

         for (Integer neighbor1 : neighbors) {
            Set<Integer> neighbors1 = getNeighbors(graph, visitedVertices, neighbor1);
            for (Integer neighbor2 : neighbors) {
               if (nodeWithMinimumDegree != neighbor2 && neighbor1 != neighbor2) {
                  if (neighbor1 < neighbor2 && neighbors1.contains(neighbor2)) {
                     numberOfTriangles++;
                  }
               }
            }
         }
         visitedVertices.add(nodeWithMinimumDegree);

         degrees[nodeWithMinimumDegree] = 0;
         for (Integer neighbor : neighbors) {
            if (!visitedVertices.contains(neighbor)) {
               degrees[neighbor]--;
            }
         }
      }
      return numberOfTriangles;
   }


   private int getNodeWithMinimumDegree(int[] degrees) {
      int nodeWithMinimumDegree = Integer.MAX_VALUE;
      int minimalDegree = Integer.MAX_VALUE;

      for (int vertex = 0; vertex < degrees.length; vertex++) {
         if (degrees[vertex] > 0 && degrees[vertex] < minimalDegree) {
            minimalDegree = degrees[vertex];
            nodeWithMinimumDegree = vertex;
         }
      }

      return nodeWithMinimumDegree;
   }


   private Set<Integer> getNeighbors(ColouredGraph graph, Set<Integer> visitedVertices, int vertex) {
      Set<Integer> neighbors = new HashSet<>();
      for (IntCursor neighbor : graph.getInNeighbors(vertex)) {
         if (!visitedVertices.contains(neighbor.value)) {
            neighbors.add(neighbor.value);
         }
      }
      for (IntCursor neighbor : graph.getOutNeighbors(vertex)) {
         if (!visitedVertices.contains(neighbor.value)) {
            neighbors.add(neighbor.value);
         }
      }

      return neighbors;
   }

}
