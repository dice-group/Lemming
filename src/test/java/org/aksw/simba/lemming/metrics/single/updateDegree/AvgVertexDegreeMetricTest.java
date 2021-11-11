package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.junit.Test;

public class AvgVertexDegreeMetricTest extends UpdateMetricTest{

    @Test
    public void testGraph1(){
        AvgVertexDegreeMetric metric = new AvgVertexDegreeMetric();
        ColouredGraph graph = buildGraph1();
        //test here
    }

    @Test
    public void testGraph2(){
        AvgVertexDegreeMetric metric = new AvgVertexDegreeMetric();
        ColouredGraph graph = buildGraph2();
        //test here
    }
}