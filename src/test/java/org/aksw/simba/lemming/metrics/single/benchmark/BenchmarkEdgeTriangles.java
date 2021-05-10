package org.aksw.simba.lemming.metrics.single.benchmark;

import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.iterations;
import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.numberOfForks;
import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.warmUpIterations;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.MultiThreadedNodeNeighborsCommonEdgesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.forward.ForwardEdgeTriangleMetric;
import org.aksw.simba.lemming.util.ColouredGraphConverter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

/**
 * @author DANISH AHMED on 6/27/2018
 */

public class BenchmarkEdgeTriangles {

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void nodeIteratorMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = ColouredGraphConverter.convertFileToGraph(graphs.fileName);

        NodeIteratorMetric metric = new NodeIteratorMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void edgeIteratorMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = ColouredGraphConverter.convertFileToGraph(graphs.fileName);

        EdgeIteratorMetric metric = new EdgeIteratorMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void edgeNumberOfSimpleTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = ColouredGraphConverter.convertFileToGraph(graphs.fileName);

        EdgeNumberOfSimpleTrianglesMetric metric = new EdgeNumberOfSimpleTrianglesMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void multiThreadedNodeNeighborsCommonEdgesMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = ColouredGraphConverter.convertFileToGraph(graphs.fileName);

        MultiThreadedNodeNeighborsCommonEdgesMetric metric = new MultiThreadedNodeNeighborsCommonEdgesMetric();
        metric.apply(graphs.graph);
    }

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void forwardMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = ColouredGraphConverter.convertFileToGraph(graphs.fileName);

        ForwardEdgeTriangleMetric metric = new ForwardEdgeTriangleMetric();
        metric.apply(graphs.graph);
    }
}