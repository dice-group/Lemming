package org.aksw.simba.lemming.metrics.single.benchmark;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.forward.ForwardMetric;
import org.openjdk.jmh.annotations.*;

/**
 * @author DANISH AHMED on 6/27/2018
 */

public class BenchmarkEdgeTriangles extends NumberOfTrianglesMetricTest {

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void NodeIteratorMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NodeIteratorMetric metric = new NodeIteratorMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void EdgeIteratorMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        EdgeIteratorMetric metric = new EdgeIteratorMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void EdgeNumberOfSimpleTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        EdgeNumberOfSimpleTrianglesMetric metric = new EdgeNumberOfSimpleTrianglesMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void NumberOfTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NumberOfTrianglesMetric metric = new NumberOfTrianglesMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void ForwardMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        ForwardMetric metric = new ForwardMetric();
        metric.apply(graphs.graph);
    }
}