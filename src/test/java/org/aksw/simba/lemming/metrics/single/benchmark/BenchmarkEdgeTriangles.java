package org.aksw.simba.lemming.metrics.single.benchmark;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.forward.ForwardMetric;
import org.openjdk.jmh.annotations.*;

import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.numberOfForks;
import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.warmUpIterations;
import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.iterations;

/**
 * @author DANISH AHMED on 6/27/2018
 */

public class BenchmarkEdgeTriangles extends NumberOfTrianglesMetricTest {

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void nodeIteratorMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NodeIteratorMetric metric = new NodeIteratorMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void edgeIteratorMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        EdgeIteratorMetric metric = new EdgeIteratorMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void edgeNumberOfSimpleTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        EdgeNumberOfSimpleTrianglesMetric metric = new EdgeNumberOfSimpleTrianglesMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void multiThreadedNodeNeighborsCommonEdgesMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        MultiThreadedNodeNeighborsCommonEdgesMetric metric = new MultiThreadedNodeNeighborsCommonEdgesMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void forwardMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        ForwardMetric metric = new ForwardMetric();
        metric.apply(graphs.graph);
    }
}