package org.aksw.simba.lemming.metrics.single.updateDegree;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import org.aksw.simba.lemming.ColouredGraph;

public class UpdateMetricTest {

    public ColouredGraph buildGraph1(){
        Grph graph = new InMemoryGrph();
        int v1 = graph.addVertex();
        int v2 = graph.addVertex();
        int v3 = graph.addVertex();
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v3);
        graph.addDirectedSimpleEdge(v2, v3);
        graph.addDirectedSimpleEdge(v2, v3);
        return new ColouredGraph(graph, null, null);
    }

    public ColouredGraph buildGraph2(){
        Grph graph = new InMemoryGrph();
        int v1 = graph.addVertex();
        int v2 = graph.addVertex();
        graph.addDirectedSimpleEdge(v1, v1);
        graph.addDirectedSimpleEdge(v1, v2);
        graph.addDirectedSimpleEdge(v1, v2);
        return new ColouredGraph(graph, null, null);
    }

    public ColouredGraph removeEdge(ColouredGraph graph, int edge){
        Grph grph = graph.getGraph();
        grph.removeEdge(edge);
        return new ColouredGraph(grph, null, null);
    }

    public ColouredGraph addEdge(ColouredGraph graph, int tail, int head){
        Grph grph = graph.getGraph();
        grph.addDirectedSimpleEdge(tail, head);
        return new ColouredGraph(grph, null, null);
    }

}

