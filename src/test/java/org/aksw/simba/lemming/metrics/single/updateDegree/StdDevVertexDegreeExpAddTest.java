package org.aksw.simba.lemming.metrics.single.updateDegree;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GeologyDataset;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.junit.Test;

import grph.Grph.DIRECTION;
import junit.framework.Assert;

public class StdDevVertexDegreeExpAddTest {
    
    private static IGraphGeneration inputGenerator;
    
    public StdDevVertexDegreeExpAddTest() {
        inputGenerator = getGraphSWDF();
    }
    
    public IGraphGeneration getGraphGeo() {
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

        //ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();
        return mGrphGenerator;
    }
    
    public IGraphGeneration getGraphSWDF() {
        int mNumberOfDesiredVertices = 45420;

        ColouredGraph graphs[] = new ColouredGraph[20];
        IDatasetManager mDatasetManager;
        mDatasetManager = new SemanticWebDogFoodDataset();
        String datasetPath = "SemanticWebDogFood/";
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);

        int iNumberOfThreads = -1;
        long seed = System.currentTimeMillis();

        IGraphGeneration mGrphGenerator;
        mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);

        //ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();
        return mGrphGenerator;
    }

    @Test
    public void testcase1() {

        IGraphGeneration mGrphGenerator = inputGenerator;
        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        

        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        UpdatableMetricResult prevResult = metric.applyUpdatable(clonedGraph);

        // Standard Deviation metric calculation
        System.out.println("Decrease StdVertexDegree metric (In degree)");
        StdDevVertexDegree stdMetric = new StdDevVertexDegree(DIRECTION.in);
        UpdatableMetricResult stdprevResult = stdMetric.applyUpdatable(clonedGraph);
        double stdMetricValue = stdMetric.apply(clonedGraph);
        // System.out.println("StdDev metric (apply Uptable) : " +
        // stdprevResult.getResult());
        System.out.println("StdDev metric (Old Value) : " + stdMetricValue);

        TripleBaseSingleID triple = stdMetric.getTripleAdd(mGrphGenerator, false, prevResult, true);
        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

        // Standard Deviation
        UpdatableMetricResult updateStdMetric = stdMetric.update(clonedGraph, triple, Operation.REMOVE, stdprevResult);
        System.out.println("StdDev metric (New Result) : " + updateStdMetric.getResult());
        System.out.println();
        Assert.assertTrue("Metric not increased!", updateStdMetric.getResult() < stdMetricValue);

    }

    @Test
    public void testcase2() {
        
        IGraphGeneration mGrphGenerator = inputGenerator;
        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        UpdatableMetricResult prevResult = metric.applyUpdatable(clonedGraph);

        // Standard Deviation metric calculation
        System.out.println("Increase StdVertexDegree metric (In degree)");
        StdDevVertexDegree stdMetric = new StdDevVertexDegree(DIRECTION.in);
        UpdatableMetricResult stdprevResult = stdMetric.applyUpdatable(clonedGraph);
        double stdMetricValue = stdMetric.apply(clonedGraph);
        // System.out.println("StdDev metric (apply Uptable) : " +
        // stdprevResult.getResult());
        System.out.println("StdDev metric (Old Value) : " + stdMetricValue);

        TripleBaseSingleID triple = stdMetric.getTripleAdd(mGrphGenerator, false, prevResult, false);
        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

        // Standard Deviation
        UpdatableMetricResult updateStdMetric = stdMetric.update(clonedGraph, triple, Operation.REMOVE, stdprevResult);
        System.out.println("StdDev metric (New Result) : " + updateStdMetric.getResult());
        System.out.println();
        Assert.assertTrue("Metric not increased!", updateStdMetric.getResult() > stdMetricValue);

    }

    @Test
    public void testcase3() {

        IGraphGeneration mGrphGenerator = inputGenerator;
        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        UpdatableMetricResult prevResult = metric.applyUpdatable(clonedGraph);

        // Standard Deviation metric calculation
        System.out.println("Decrease StdVertexDegree metric (Out degree)");
        StdDevVertexDegree stdMetric = new StdDevVertexDegree(DIRECTION.out);
        UpdatableMetricResult stdprevResult = stdMetric.applyUpdatable(clonedGraph);
        double stdMetricValue = stdMetric.apply(clonedGraph);
        // System.out.println("StdDev metric (apply Uptable) : " +
        // stdprevResult.getResult());
        System.out.println("StdDev metric (Old Value) : " + stdMetricValue);

        TripleBaseSingleID triple = stdMetric.getTripleAdd(mGrphGenerator, false, prevResult, true);
        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

        // Standard Deviation
        UpdatableMetricResult updateStdMetric = stdMetric.update(clonedGraph, triple, Operation.REMOVE, stdprevResult);
        System.out.println("StdDev metric (New Result) : " + updateStdMetric.getResult());
        System.out.println();
        Assert.assertTrue("Metric not increased!", updateStdMetric.getResult() < stdMetricValue);

    }

    @Test
    public void testcase4() {
        
        IGraphGeneration mGrphGenerator = inputGenerator;
        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        UpdatableMetricResult prevResult = metric.applyUpdatable(clonedGraph);

        // Standard Deviation metric calculation
        System.out.println("Increase StdVertexDegree metric (Out degree)");
        StdDevVertexDegree stdMetric = new StdDevVertexDegree(DIRECTION.out);
        UpdatableMetricResult stdprevResult = stdMetric.applyUpdatable(clonedGraph);
        double stdMetricValue = stdMetric.apply(clonedGraph);
        // System.out.println("StdDev metric (apply Uptable) : " +
        // stdprevResult.getResult());
        System.out.println("StdDev metric (Old Value) : " + stdMetricValue);

        TripleBaseSingleID triple = stdMetric.getTripleAdd(mGrphGenerator, false, prevResult, false);
        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);
        
        // Standard Deviation
        UpdatableMetricResult updateStdMetric = stdMetric.update(clonedGraph, triple, Operation.REMOVE, stdprevResult);
        System.out.println("StdDev metric (New Result) : " + updateStdMetric.getResult());
        System.out.println();
        Assert.assertTrue("Metric not increased!", updateStdMetric.getResult() > stdMetricValue);
        
        
    }

}
