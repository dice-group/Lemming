package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.junit.Test;

public class NodeTriangleMetricTest extends UpdateMetricTest{

    @Test
    public void testGraph1(){
        NodeTriangleMetric metric = new NodeTriangleMetric();
        ColouredGraph graph = buildGraph1();
        //test here
    }

    @Test
    public void testGraph2(){
        NodeTriangleMetric metric = new NodeTriangleMetric();
        ColouredGraph graph = buildGraph2();
        //test here
    }
}
