package org.aksw.simba.lemming.metrics.single.triangle;


import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import toools.math.IntMatrix;


public class MatrixMultiplicationNumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

   private ColouredGraph graph;


   public MatrixMultiplicationNumberOfTrianglesMetric() {
      super("matrix multiplication #triangles");
   }


   @Override
   public double apply(ColouredGraph graph) {
      // NOTE: This only works under the assumption that no two nodes are connected by more than one
      // edge
      this.graph = graph;

      IntMatrix cubicAdjacencyMatrix = getCubicAdjacencyMatrix();

      double diagonalSumOfCubicAdjacencyMatrix = getSumOfDiagonal(cubicAdjacencyMatrix);
      return diagonalSumOfCubicAdjacencyMatrix / 6;
   }


   public IntMatrix getCubicAdjacencyMatrix() {
      IntMatrix adjacencyMatrix = getAdjacencyMatrix(graph.getGraph());
      return power(adjacencyMatrix, 3);
   }


   private double getSumOfDiagonal(IntMatrix matrix) {
      double sum = 0;
      for (int i = 0; i < matrix.width; i++) {
         sum += matrix.get(i, i);
      }
      return sum;
   }


   private IntMatrix getAdjacencyMatrix(Grph graph) {
      IntMatrix adjacencyMatrix = new IntMatrix(graph.getNumberOfVertices(), graph.getNumberOfVertices());
      for (IntCursor e : graph.getEdges()) {
         int sourceNode = graph.getOneVertex(e.value);
         int targetNode = graph.getTheOtherVertex(e.value, sourceNode);
         if (sourceNode != targetNode) {
            adjacencyMatrix.set(sourceNode, targetNode, 1);
            adjacencyMatrix.set(targetNode, sourceNode, 1);
         }
      }
      return adjacencyMatrix;
   }


   private IntMatrix power(IntMatrix intMatrix, int k) {
      IntMatrix r = new IntMatrix(intMatrix.toIntArray());

      for (int i = 1; i < k; i++) {
         r = IntMatrix.multiplication(r, intMatrix);
      }

      return r;
   }


}
