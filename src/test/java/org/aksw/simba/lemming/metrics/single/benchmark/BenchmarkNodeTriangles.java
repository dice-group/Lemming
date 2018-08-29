package org.aksw.simba.lemming.metrics.single.benchmark;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.nodetriangles.EdgeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorCoreMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.MultiThreadedNodeNeighborTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.ayz.ListingAyzMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardMetric;
import org.openjdk.jmh.annotations.*;

import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.numberOfForks;
import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.warmUpIterations;
import static org.aksw.simba.lemming.metrics.single.benchmark.BenchmarkConfig.iterations;

/**
 * @author DANISH AHMED on 6/27/2018
 */
public class BenchmarkNodeTriangles extends NumberOfTrianglesMetricTest {

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void listingAyzMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        ListingAyzMetric metric = new ListingAyzMetric();
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

    @Fork(value = numberOfForks, warmups = warmUpIterations)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = iterations)
    public void nodeIteratorCoreMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        NodeIteratorCoreMetric metric = new NodeIteratorCoreMetric();
        metric.apply(graphs.graph);
    }

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
    public void multiThreadedNodeNeighborTrianglesMetric(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        MultiThreadedNodeNeighborTrianglesMetric metric = new MultiThreadedNodeNeighborTrianglesMetric();
        metric.apply(graphs.graph);
    }
}
