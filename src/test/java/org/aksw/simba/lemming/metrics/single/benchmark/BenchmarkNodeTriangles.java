package org.aksw.simba.lemming.metrics.single.benchmark;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.AyzNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardNumberOfTriangleMetric;
import org.openjdk.jmh.annotations.*;

/**
 * @author DANISH AHMED on 6/27/2018
 */
public class BenchmarkNodeTriangles extends NumberOfTrianglesMetricTest {

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void AyzNumberOfTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        final double delta = 3.0;
        AyzNumberOfTrianglesMetric metric = new AyzNumberOfTrianglesMetric(delta);
        metric.apply(graphs.graph);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void ForwardNumberOfTriangle(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        ForwardNumberOfTriangleMetric metric = new ForwardNumberOfTriangleMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void NodeIteratorCoreNumberOfTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NodeIteratorCoreNumberOfTrianglesMetric metric = new NodeIteratorCoreNumberOfTrianglesMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void NodeIteratorNumberOfTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NodeIteratorNumberOfTrianglesMetric metric = new NodeIteratorNumberOfTrianglesMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void NumberOfSimpleTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NumberOfSimpleTrianglesMetric metric = new NumberOfSimpleTrianglesMetric();
        metric.apply(graphs.graph);
    }
}
