package org.aksw.simba.lemming.metrics.single.expressions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GeologyDataset;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.updateDegree.UpdateMetricTest;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.mimicgraph.generator.GraphOptimization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

import grph.Grph.DIRECTION;

public class EdgeTriangleExpressionAddTest extends UpdateMetricTest {
    private static HashMap<IGraphGeneration,ColouredGraph> inputGenerator;
    String datasetPath;
    
    public EdgeTriangleExpressionAddTest() {
        inputGenerator = getGraphGeo();
    }
    
    public HashMap<IGraphGeneration,ColouredGraph> getGraphGeo() {
        int mNumberOfDesiredVertices = 1281;

        ColouredGraph graphs[] = new ColouredGraph[20];
        IDatasetManager mDatasetManager;
        mDatasetManager = new GeologyDataset();
        datasetPath = "GeologyGraphs/";
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);

        int iNumberOfThreads = -1;
        long seed = System.currentTimeMillis();

        IGraphGeneration mGrphGenerator;
        mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);

        mGrphGenerator.generateGraph();

        // ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        long secSeed = mGrphGenerator.getSeed() + 1;
        ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);

        List<SingleValueMetric> metrics = new ArrayList<>();
        // these are two fixed metrics: NodeTriangleMetric and EdgeTriangleMetric
        metrics.add(new NodeTriangleMetric());
        metrics.add(new EdgeTriangleMetric());

        // these are optional metrics
        metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
        metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
        metrics.add(new AvgVertexDegreeMetric());
        metrics.add(new StdDevVertexDegree(DIRECTION.in));
        metrics.add(new StdDevVertexDegree(DIRECTION.out));
        metrics.add(new NumberOfEdgesMetric());
        metrics.add(new NumberOfVerticesMetric());

        GraphOptimization grphOptimizer = new GraphOptimization(graphs, mGrphGenerator, metrics, valuesCarrier,
                secSeed);
        ColouredGraph clonedGraph = grphOptimizer.getmEdgeModifier().getGraph();
        
        //ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();
        
        HashMap<IGraphGeneration,ColouredGraph> mapToReturn = new HashMap<>();
        mapToReturn.put(mGrphGenerator, clonedGraph);
        return mapToReturn;
    }
    
    public HashMap<IGraphGeneration,ColouredGraph> getGraphSWDF() {
        int mNumberOfDesiredVertices = 45420;

        ColouredGraph graphs[] = new ColouredGraph[20];
        IDatasetManager mDatasetManager;
        mDatasetManager = new SemanticWebDogFoodDataset();
        datasetPath = "SemanticWebDogFood/";
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);

        int iNumberOfThreads = -1;
        long seed = System.currentTimeMillis();

        IGraphGeneration mGrphGenerator;
        mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);

        mGrphGenerator.generateGraph();

        // ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();

        long secSeed = mGrphGenerator.getSeed() + 1;
        ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);

        List<SingleValueMetric> metrics = new ArrayList<>();
        // these are two fixed metrics: NodeTriangleMetric and EdgeTriangleMetric
        //metrics.add(new NodeTriangleMetric());
        //metrics.add(new EdgeTriangleMetric());

        // these are optional metrics
        metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
        metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
        metrics.add(new AvgVertexDegreeMetric());
        metrics.add(new StdDevVertexDegree(DIRECTION.in));
        metrics.add(new StdDevVertexDegree(DIRECTION.out));
        metrics.add(new NumberOfEdgesMetric());
        metrics.add(new NumberOfVerticesMetric());

        GraphOptimization grphOptimizer = new GraphOptimization(graphs, mGrphGenerator, metrics, valuesCarrier,
                secSeed);
        ColouredGraph clonedGraph = grphOptimizer.getmEdgeModifier().getGraph();
        
        HashMap<IGraphGeneration,ColouredGraph> mapToReturn = new HashMap<>();
        mapToReturn.put(mGrphGenerator, clonedGraph);
        return mapToReturn;
    }

    @Test
    public void testcase1() {

        HashMap<IGraphGeneration,ColouredGraph> map = inputGenerator;
        Set<IGraphGeneration> keySet = map.keySet();
        
        IGraphGeneration mGrphGenerator = keySet.iterator().next();
        
        ColouredGraph clonedGraph = map.get(mGrphGenerator);

        //ColouredGraph graph = inputGraph;

        EdgeTriangleMetric edgeTriangleMetric = new EdgeTriangleMetric();
        UpdatableMetricResult edgeTriangleMetricResult = edgeTriangleMetric.applyUpdatable(clonedGraph);
        double oldMetricResult = edgeTriangleMetricResult.getResult();
        System.out.println("Select a triple to reduce the metric : ");
        System.out.println("Edge Triangle metric (Old Result) : " + edgeTriangleMetricResult.getResult());
        
        List<UpdatableMetricResult> previousResultList = new ArrayList<>();
        previousResultList.add(edgeTriangleMetricResult);
        
        TripleBaseSingleID triple = edgeTriangleMetric.getTripleAdd(clonedGraph, mGrphGenerator, false,previousResultList,  true);

        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

        edgeTriangleMetricResult = edgeTriangleMetric.update(clonedGraph, triple, Operation.REMOVE, edgeTriangleMetricResult);
        //System.out.println("Triple details: head - " + triple.headId + ", tail - " + triple.tailId);
        System.out.println("Edge Triangle metric (New Result) : " + edgeTriangleMetricResult.getResult());
        System.out.println();

        Assert.assertTrue("Metric not decreased!", edgeTriangleMetricResult.getResult() < oldMetricResult);

    }

    @Test
    public void testcase2() {

        HashMap<IGraphGeneration,ColouredGraph> map = inputGenerator;
        Set<IGraphGeneration> keySet = map.keySet();
        
        IGraphGeneration mGrphGenerator = keySet.iterator().next();
        
        ColouredGraph clonedGraph = map.get(mGrphGenerator);
        //ColouredGraph graph = buildGraph1();

        EdgeTriangleMetric edgeTriangleMetric = new EdgeTriangleMetric();
        UpdatableMetricResult edgeTriangleMetricResult = edgeTriangleMetric.applyUpdatable(clonedGraph);
        double oldMetricResult = edgeTriangleMetricResult.getResult();
        System.out.println("Select a triple to increase metric : ");
        System.out.println("Edge Triangle metric (Old Result) : " + edgeTriangleMetricResult.getResult());
        
        List<UpdatableMetricResult> previousResultList = new ArrayList<>();
        previousResultList.add(edgeTriangleMetricResult);
        
        TripleBaseSingleID triple = edgeTriangleMetric.getTripleAdd(clonedGraph, mGrphGenerator, false,previousResultList, false);

        clonedGraph.addEdge(triple.tailId, triple.headId, triple.edgeColour);

        edgeTriangleMetricResult = edgeTriangleMetric.update(clonedGraph, triple, Operation.REMOVE, edgeTriangleMetricResult);
        //System.out.println("Triple details: head - " + triple.headId + ", tail - " + triple.tailId);
        System.out.println("Edge Triangle metric (New Result) : " + edgeTriangleMetricResult.getResult());
        System.out.println();

        Assert.assertTrue("Metric decreased!", edgeTriangleMetricResult.getResult() >= oldMetricResult);

    }
}

