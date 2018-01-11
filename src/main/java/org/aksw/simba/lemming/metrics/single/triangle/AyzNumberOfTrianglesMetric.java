package org.aksw.simba.lemming.metrics.single.triangle;


import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.triangle.matrix.MatrixMultiplicationNumberOfTrianglesMetric;

import com.carrotsearch.hppc.cursors.IntCursor;

import toools.set.IntSet;
import toools.set.IntSets;


public class AyzNumberOfTrianglesMetric extends AbstractMetric implements SingleValueMetric {

   public double delta;


   public AyzNumberOfTrianglesMetric(double delta) {
      super("ayz #node triangles");
      this.delta = delta;
   }


   @Override
   public double apply(ColouredGraph graph) {
      double threshold = Math.pow(graph.getGraph().getNumberOfEdges(), (delta - 1) / (delta + 1));
      IntSet highDegreeVertices = IntSets.from(new int[] {});
      for (IntCursor vertex : graph.getVertices()) {
         if (graph.getGraph().getVertexDegree(vertex.value) > threshold) {
            highDegreeVertices.add(vertex.value);
         }
      }

      double numberOfTriangles = 0;
      numberOfTriangles += countTrianglesViaNodeIterator(graph, highDegreeVertices);
      numberOfTriangles += countTrianglesViaMatrixMultiplication(graph, highDegreeVertices);

      return numberOfTriangles;
   }


   private double countTrianglesViaNodeIterator(ColouredGraph graph, IntSet highDegreeVertices) {
      NodeIteratorNumberOfTrianglesMetric nodeIterator = new NodeIteratorNumberOfTrianglesMetric(highDegreeVertices);
      return nodeIterator.apply(graph);
   }


   private double countTrianglesViaMatrixMultiplication(ColouredGraph graph, IntSet highDegreeVertices) {
      ColouredGraph subgraph = new ColouredGraph(graph.getGraph().getSubgraphInducedByVertices(highDegreeVertices),
            graph.getVertexPalette(), graph.getEdgePalette());
      MatrixMultiplicationNumberOfTrianglesMetric matrixMultiplication = new MatrixMultiplicationNumberOfTrianglesMetric();
      return matrixMultiplication.apply(subgraph);
   }

}
