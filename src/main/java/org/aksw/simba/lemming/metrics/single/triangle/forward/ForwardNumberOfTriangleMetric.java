package org.aksw.simba.lemming.metrics.single.triangle.forward;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.collect.Sets;

import toools.set.IntSet;
import toools.set.IntSets;


/**
 * This class models an algorithm for computing the amount of node triangles in a given graph. This
 * is done using the so called forward algorithm proposed by Schank and Wagner in their work
 * "Finding, Counting and Listing all Triangles in Large Graphs, An Experimental Study" (see below).
 * 
 * @see <a href=
 *      "https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study">https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study</a>.
 * 
 * @author Alexander Hetzer
 *
 */
public class ForwardNumberOfTriangleMetric extends AbstractMetric implements SingleValueMetric {

   private ColouredGraph coloredGraph;

   private List<HashSet<Integer>> adjacencyDatastructure;
   private DegreeBasedDecreasingNodeOrdering nodeOrdering;

   private int amountOfTriangles;


   /**
    * Creates a new {@link ForwardNumberOfTriangleMetric}.
    */
   public ForwardNumberOfTriangleMetric() {
      super("forward #node triangles");
   }


   @Override
   public double apply(ColouredGraph coloredGraph) {
      initialize(coloredGraph);
      for (Integer nodeId : nodeOrdering.getOrderedNodes()) {
         processNeighborsOf(nodeId);
      }
      return amountOfTriangles;
   }


   /**
    * Processes the given neighbors of the given node.
    * 
    * @param nodeId The id of the node whose neighbors should be processed.
    */
   private void processNeighborsOf(int nodeId) {
      IntSet neighborSet = IntSets.union(coloredGraph.getOutNeighbors(nodeId), coloredGraph.getInNeighbors(nodeId));
      for (IntCursor adjacentNodeIdCursor : neighborSet) {
         int adjacentNodeId = adjacentNodeIdCursor.value;
         if (nodeOrdering.isFirstSmallerWithRespectToOrder(nodeId, adjacentNodeId)) {
            amountOfTriangles += Sets.intersection(adjacencyDatastructure.get(nodeId), adjacencyDatastructure.get(adjacentNodeId)).size();
            adjacencyDatastructure.get(adjacentNodeId).add(nodeId);
         }
      }
   }


   /**
    * Initializes this {@link ForwardNumberOfTriangleMetric} for the given {@link ColouredGraph}.
    * 
    * @param coloredGraph The {@link ColouredGraph} whose triangles should be computed by this
    *           metric.
    */
   private void initialize(ColouredGraph coloredGraph) {
      this.coloredGraph = coloredGraph;
      amountOfTriangles = 0;
      adjacencyDatastructure = new ArrayList<>(coloredGraph.getVertices().size());
      for (int i = 0; i < coloredGraph.getVertices().size(); i++) {
         adjacencyDatastructure.add(new HashSet<Integer>());
      }
      nodeOrdering = new DegreeBasedDecreasingNodeOrdering(coloredGraph);
   }

}
