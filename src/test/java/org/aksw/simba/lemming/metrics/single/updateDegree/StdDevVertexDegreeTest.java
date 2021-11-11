package org.aksw.simba.lemming.metrics.single.updateDegree;

import grph.Grph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.junit.Test;

public class StdDevVertexDegreeTest extends UpdateMetricTest{

    @Test
    public void testGraph1(){
        StdDevVertexDegree metric = new StdDevVertexDegree(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph1();
        //test here

        metric = new StdDevVertexDegree(Grph.DIRECTION.out);
        graph = buildGraph1();
        //test here

    }

    @Test
    public void testGraph2(){
        StdDevVertexDegree metric = new StdDevVertexDegree(Grph.DIRECTION.in);
        ColouredGraph graph = buildGraph2();
        //test here

        metric = new StdDevVertexDegree(Grph.DIRECTION.out);
        graph = buildGraph1();
        //test here
    }

}
