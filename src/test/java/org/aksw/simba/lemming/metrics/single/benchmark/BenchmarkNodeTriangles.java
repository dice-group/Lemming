package org.aksw.simba.lemming.metrics.single.benchmark;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.nodetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.ListingAyzMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardMetric;
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
    public void ListingAyzMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        final double delta = 3.0;
        ListingAyzMetric metric = new ListingAyzMetric(delta);
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

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 4)
    public void NodeIteratorCoreMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NodeIteratorCoreMetric metric = new NodeIteratorCoreMetric();
        metric.apply(graphs.graph);
    }

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
    public void NumberOfSimpleTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NumberOfSimpleTrianglesMetric metric = new NumberOfSimpleTrianglesMetric();
        metric.apply(graphs.graph);
    }
}
