package org.aksw.simba.lemming.metrics.single.result;

import org.aksw.simba.lemming.metrics.single.nodetriangles.EdgeIteratorMetric;

/**
 * Store edge triangle metric and intermediate information for updating edge triangle metric
 *
 * @author Zun Wang
 */
public class EdgeTriangleMetricResult implements SingleValueMetricResult{

     private double edgeTriangle;
     private double newEdgeTriangle;

     public EdgeTriangleMetricResult(double edgeTriangle, double newEdgeTriangle){
         this.edgeTriangle = edgeTriangle;
         this.newEdgeTriangle = newEdgeTriangle;
     }

     public double metric(){
         return this.edgeTriangle;
     }

     public void update(){
         if(newEdgeTriangle == -1){
             throw new RuntimeException("There's no new edge triangle to update!");
         }
         edgeTriangle = newEdgeTriangle;
     }

     public double getUpdatedMetric(){
         return this.newEdgeTriangle;
     }

     public void setNewEdgeTriangle(int newEdgeTriangle){
         this.newEdgeTriangle = newEdgeTriangle;
     }

}
