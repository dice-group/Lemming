package org.aksw.simba.lemming.metrics.single.degree;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.aksw.simba.lemming.ColouredGraph;

public class MetricTest {

    protected ColouredGraph buildGraph1(){
        Grph graph = new InMemoryGrph();
        int v1 = graph.addVertex();
        int v2 = graph.addVertex();
        int v3 = graph.addVertex();
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v3);
        return new ColouredGraph(graph, null, null);
    }

    protected ColouredGraph buildGraph2(){
        Grph graph = new InMemoryGrph();
        int v1 = graph.addVertex();
        int v2 = graph.addVertex();
        graph.addDirectedSimpleEdge(v1, v1);
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v2);
        return new ColouredGraph(graph, null, null);
    }

    protected ColouredGraph removeEdge(ColouredGraph graph, int edge){
        Grph grph = graph.getGraph();
        grph.removeEdge(edge);
        return new ColouredGraph(grph, null, null);
    }

    protected ColouredGraph addEdge(ColouredGraph graph, int tail, int head){
        Grph grph = graph.getGraph();
        grph.addDirectedSimpleEdge(tail, head);
        return new ColouredGraph(grph, null, null);
    }

}
