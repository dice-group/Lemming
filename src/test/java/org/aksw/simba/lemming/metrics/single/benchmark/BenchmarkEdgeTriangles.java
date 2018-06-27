package org.aksw.simba.lemming.metrics.single.benchmark;

import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeIteratorNumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeNumberOfSimpleTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NumberOfTrianglesMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.forward.EdgeForwardNumberOfTriangleMetric;
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
    public void EdgeIteratorNumberOfTriangles(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        EdgeIteratorNumberOfTrianglesMetric metric = new EdgeIteratorNumberOfTrianglesMetric();
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
    public void EdgeForwardNumberOfTriangle(BenchmarkGraphs.Graphs graphs) {
        graphs.graph = getColouredGraph(graphs.fileName);

        EdgeForwardNumberOfTriangleMetric metric = new EdgeForwardNumberOfTriangleMetric();
        metric.apply(graphs.graph);
    }
}