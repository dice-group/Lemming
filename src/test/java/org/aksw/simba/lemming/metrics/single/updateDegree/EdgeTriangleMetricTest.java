package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.junit.Test;

public class EdgeTriangleMetricTest extends UpdateMetricTest{

    @Test
    public void testGraph1(){
        EdgeTriangleMetric metric = new EdgeTriangleMetric();
        ColouredGraph graph = buildGraph1();
        //test here
    }

    @Test
    public void testGraph2(){
        EdgeTriangleMetric metric = new EdgeTriangleMetric();
        ColouredGraph graph = buildGraph2();
        //test here
    }
}
