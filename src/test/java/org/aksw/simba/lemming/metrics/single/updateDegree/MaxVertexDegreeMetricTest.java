package org.aksw.simba.lemming.metrics.single.updateDegree;

import grph.Grph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.junit.Test;

public class MaxVertexDegreeMetricTest extends UpdateMetricTest{

    @Test
    public void testGraph1(){
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph1();
        //test here

        metric = new MaxVertexDegreeMetric(Grph.DIRECTION.out);
        graph = buildGraph1();
        //test here

    }

    @Test
    public void testGraph2(){
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph2();
        //test here

        metric = new MaxVertexDegreeMetric(Grph.DIRECTION.out);
        graph = buildGraph1();
        //test here
    }
}
