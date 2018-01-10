package org.aksw.simba.lemming.metrics.single.triangle.matrix;


import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import toools.math.IntMatrix;


public class MatrixMultiplicationNumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

   private ColouredGraph graph;


   public MatrixMultiplicationNumberOfTrianglesMetric() {
      super("matrix multiplication #triangles");
   }


   @Override
   public double apply(ColouredGraph graph) {
      if (graph.getVertices().size() < 3 || graph.getGraph().getEdges().size() < 3) {
         return 0;
      }
      // NOTE: This implementation only works under the assumption that no two nodes are connected
      // by more than one edge
      this.graph = graph;

      IntMatrix cubicAdjacencyMatrix = getCubicAdjacencyMatrix();

      double diagonalSumOfCubicAdjacencyMatrix = getSumOfDiagonal(cubicAdjacencyMatrix);
      return diagonalSumOfCubicAdjacencyMatrix / 6;
   }


   public IntMatrix getCubicAdjacencyMatrix() {
      MultiEdgeIgnoringAdjacencyMatrix adjacencyMatrix = new MultiEdgeIgnoringAdjacencyMatrix(graph.getGraph());
      return MultiEdgeIgnoringAdjacencyMatrix.power(adjacencyMatrix, 3);
   }


   private double getSumOfDiagonal(IntMatrix matrix) {
      double sum = 0;
      for (int i = 0; i < matrix.width; i++) {
         sum += matrix.get(i, i);
      }
      return sum;
   }


}
