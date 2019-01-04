package org.aksw.simba.lemming.metrics.single.benchmark;

import org.aksw.simba.lemming.ColouredGraph;
import org.openjdk.jmh.annotations.*;

/**
 * @author DANISH AHMED on 6/27/2018
 */
public class BenchmarkGraphs {
    @State(Scope.Benchmark)
    public static class Graphs {

        @Param({"graph1.n3", "graph_loop.n3", "graph_loop_2.n3", "email-Eu-core.n3"})
        public String fileName;

        public ColouredGraph graph;

        @Setup(Level.Invocation)
        public void setUp() {
            graph = null;
        }
    }
}
