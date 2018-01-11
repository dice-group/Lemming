package org.aksw.simba.lemming.metrics.single.triangle;


import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import toools.set.IntSet;
import toools.set.IntSets;


public class NodeIteratorNumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

   private IntSet highDegreeVertices;


   public NodeIteratorNumberOfTrianglesMetric() {
      super("node-iterator #node triangles");
      this.highDegreeVertices = IntSets.emptySet;
   }


   public NodeIteratorNumberOfTrianglesMetric(IntSet highDegreeVertices) {
      this();
      this.highDegreeVertices = highDegreeVertices;
   }


   @Override
   public double apply(ColouredGraph graph) {
      IntSet visitedVertices = IntSets.from(new int[] {});
      Grph grph = getUndirectedGraph(graph.getGraph());

      int numberOfTriangles = 0;
      for (IntCursor vertex : graph.getVertices()) {
         IntSet neighbors = IntSets.difference(IntSets.union(grph.getInNeighbors(vertex.value), grph.getOutNeighbors(vertex.value)),
               visitedVertices);
         for (IntCursor neighbor1 : neighbors) {
            IntSet neighbors1 = IntSets
                  .difference(IntSets.union(grph.getInNeighbors(neighbor1.value), grph.getOutNeighbors(neighbor1.value)), visitedVertices);
            for (IntCursor neighbor2 : neighbors) {
               if (vertex.value != neighbor1.value && vertex.value != neighbor2.value && neighbor1.value < neighbor2.value
                     && neighbors1.contains(neighbor2.value)) {
                  if (!highDegreeVertices.contains(vertex.value) || !highDegreeVertices.contains(neighbor1.value)
                        || !highDegreeVertices.contains(neighbor2.value)) {
                     numberOfTriangles++;
                  }
               }
            }
         }
         visitedVertices.add(vertex.value);
      }
      return numberOfTriangles;
   }


   private Grph getUndirectedGraph(Grph graph) {
      Grph undirectedGraph = new InMemoryGrph();
      for (IntCursor e : graph.getEdges()) {
         int sourceNode = graph.getOneVertex(e.value);
         int targetNode = graph.getTheOtherVertex(e.value, sourceNode);
         undirectedGraph.addUndirectedSimpleEdge(sourceNode, targetNode);
      }
      return undirectedGraph;
   }

}
