package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GeologyDataset;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.junit.Test;

import grph.Grph.DIRECTION;
import junit.framework.Assert;

//Test for triple add
public class MaxVertexDegreeExpressionTest2 extends UpdateMetricTest {

    @Test
    public void testcase1() {

        int mNumberOfDesiredVertices = 1281;

        ColouredGraph graphs[] = new ColouredGraph[20];
        IDatasetManager mDatasetManager;
        mDatasetManager = new GeologyDataset();
        String datasetPath = "GeologyGraphs/";
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);

        int iNumberOfThreads = -1;
        long seed = System.currentTimeMillis();

        IGraphGeneration mGrphGenerator;
        mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);

        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        UpdatableMetricResult prevResult = metric.applyUpdatable(clonedGraph);
        double metricValue = metric.apply(clonedGraph);
        Assert.assertEquals(metricValue, prevResult.getResult());

        TripleBaseSingleID triple = metric.getTripleAdd(mGrphGenerator, false, prevResult, false);
        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);
        UpdatableMetricResult newResult = metric.update(clonedGraph, triple, Operation.ADD, prevResult);
        Assert.assertTrue("Edge Added for candidate", newResult.getResult() <= metricValue);

    }

    @Test
    public void testcase2() {
        int mNumberOfDesiredVertices = 1281;

        ColouredGraph graphs[] = new ColouredGraph[20];
        IDatasetManager mDatasetManager;
        mDatasetManager = new GeologyDataset();
        String datasetPath = "GeologyGraphs/";
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);

        int iNumberOfThreads = -1;
        long seed = System.currentTimeMillis();

        IGraphGeneration mGrphGenerator;
        mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);

        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        UpdatableMetricResult prevResult = metric.applyUpdatable(clonedGraph);
        double metricValue = metric.apply(clonedGraph);
        Assert.assertEquals(metricValue, prevResult.getResult());

        TripleBaseSingleID triple = metric.getTripleAdd(mGrphGenerator, false, prevResult, true);
        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);
        UpdatableMetricResult newResult = metric.update(clonedGraph, triple, Operation.ADD, prevResult);
        Assert.assertTrue("Edge Added for candidate", newResult.getResult() > metricValue);
    }

    @Test
    public void testcase3() {

        int mNumberOfDesiredVertices = 1281;

        ColouredGraph graphs[] = new ColouredGraph[20];
        IDatasetManager mDatasetManager;
        mDatasetManager = new GeologyDataset();
        String datasetPath = "GeologyGraphs/";
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);

        int iNumberOfThreads = -1;
        long seed = System.currentTimeMillis();

        IGraphGeneration mGrphGenerator;
        mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);

        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        UpdatableMetricResult prevResult = metric.applyUpdatable(clonedGraph);
        double metricValue = metric.apply(clonedGraph);
        Assert.assertEquals(metricValue, prevResult.getResult());

        TripleBaseSingleID triple = metric.getTripleAdd(mGrphGenerator, false, prevResult, false);
        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);
        UpdatableMetricResult newResult = metric.update(clonedGraph, triple, Operation.ADD, prevResult);
        Assert.assertTrue("Edge Added for candidate", newResult.getResult() <= metricValue);
    }

    @Test
    public void testcase4() {
        int mNumberOfDesiredVertices = 1281;

        ColouredGraph graphs[] = new ColouredGraph[20];
        IDatasetManager mDatasetManager;
        mDatasetManager = new GeologyDataset();
        String datasetPath = "GeologyGraphs/";
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);

        int iNumberOfThreads = -1;
        long seed = System.currentTimeMillis();

        IGraphGeneration mGrphGenerator;
        mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);

        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        UpdatableMetricResult prevResult = metric.applyUpdatable(clonedGraph);
        double metricValue = metric.apply(clonedGraph);
        Assert.assertEquals(metricValue, prevResult.getResult());

        TripleBaseSingleID triple = metric.getTripleAdd(mGrphGenerator, false, prevResult, true);
        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);
        UpdatableMetricResult newResult = metric.update(clonedGraph, triple, Operation.ADD, prevResult);
        Assert.assertTrue("Edge Added for candidate", newResult.getResult() > metricValue);

    }
}
