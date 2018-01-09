package org.aksw.simba.lemming.metrics.single.triangle;


import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.cursors.IntCursor;

import toools.set.IntSets;


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
         degrees[vertex.value] = IntSets.union(graph.getInNeighbors(vertex.value), graph.getOutNeighbors(vertex.value)).size();
      }

      int nodeWithMinimumDegree = getNodeWithMinimumDegree(degrees);
      while (nodeWithMinimumDegree < Integer.MAX_VALUE && visitedVertices.size() < graph.getVertices().size() - 2) {
         Set<Integer> neighbors = getNeighbors(graph, visitedVertices, nodeWithMinimumDegree);

         for (Integer neighbor1 : neighbors) {
            Set<Integer> neighbors1 = getNeighbors(graph, visitedVertices, neighbor1);
            for (Integer neighbor2 : neighbors) {
               if (nodeWithMinimumDegree != neighbor1 && nodeWithMinimumDegree != neighbor2 && neighbor1 < neighbor2
                     && neighbors1.contains(neighbor2)) {
                  numberOfTriangles++;
               }
            }
         }
         visitedVertices.add(nodeWithMinimumDegree);

         degrees[nodeWithMinimumDegree] = 0;
         for (Integer neighbor : neighbors) {
            if (degrees[neighbor] > 0) {
               degrees[neighbor]--;
            }
         }

         nodeWithMinimumDegree = getNodeWithMinimumDegree(degrees);
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
