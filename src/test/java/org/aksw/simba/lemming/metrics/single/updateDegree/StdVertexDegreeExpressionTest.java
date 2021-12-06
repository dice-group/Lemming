package org.aksw.simba.lemming.metrics.single.updateDegree;

import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GeologyDataset;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

import grph.Grph.DIRECTION;

public class StdVertexDegreeExpressionTest extends UpdateMetricTest {
    private static final String GRAPH_FILE = "graph1_1.n3"; // graph_loop_2.n3 graph1_1.n3 email-Eu-core.n3
    //private static final String GRAPH_FILE1 = "graph1_1.n3";
    private static ColouredGraph inputGraph;

    public StdVertexDegreeExpressionTest() {
        inputGraph = getGraph();
    }
    
    public ColouredGraph getGraph() {
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
        return clonedGraph;
    }
    
    @SuppressWarnings("deprecation")
    public ColouredGraph getGraphFile() {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        return graph;
    }
    
    public ColouredGraph getGraphSWDF() {
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

        ColouredGraph clonedGraph = mGrphGenerator.getMimicGraph().clone();
        return clonedGraph;
    }
    
    
    @Test
    public void testcase1() {

        
        ColouredGraph graph = inputGraph;

        // Test case for in-degree, don't remove edge for vertex having maximum degree
        MaxVertexDegreeMetric maxMetric = new MaxVertexDegreeMetric(DIRECTION.in);
        // ColouredGraph buildGraph1 = buildGraph1();

        // Standard Deviation metric calculation
        System.out.println("Decrease StdVertexDegree metric (In degree)");
        StdDevVertexDegree stdMetric = new StdDevVertexDegree(DIRECTION.in);
        UpdatableMetricResult stdprevResult = stdMetric.applyUpdatable(graph);
        double stdMetricValue = stdMetric.apply(graph);
        //System.out.println("StdDev metric (apply Uptable) : " + stdprevResult.getResult());
        System.out.println("StdDev metric (Old Value) : " + stdMetricValue);

        UpdatableMetricResult prevResult = maxMetric.applyUpdatable(graph);
        
        AvgVertexDegreeMetric avgMetric = new AvgVertexDegreeMetric();
        double apply = avgMetric.apply(graph);
        System.out.println("Average Vertex Degree : " + apply);
        
       
        System.out.println("Maximum Vertex Degree : " + prevResult.getResult());

        // delete an edge
        long seed = System.currentTimeMillis();
        TripleBaseSingleID triple = stdMetric.getTripleRemove(graph, prevResult, seed, false);
        graph = removeEdge(graph, triple.edgeId);

        prevResult = maxMetric.update(graph, triple, Operation.REMOVE, prevResult);
        
        // Standard Deviation
        UpdatableMetricResult updateStdMetric = stdMetric.update(graph, triple, Operation.REMOVE, stdprevResult);
        System.out.println("StdDev metric (New Result) : " + updateStdMetric.getResult());
        System.out.println();
         
        Assert.assertTrue("Metric not decreased!", updateStdMetric.getResult() < stdMetricValue);

    }

    
    @Test
    public void testcase2() {
        // Test case for in-degree, remove edge for vertex having maximum degree

        ColouredGraph graph = inputGraph;

        // Test case for in-degree, don't remove edge for vertex having maximum degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        // ColouredGraph buildGraph1 = buildGraph1();

        // Standard Deviation metric calculation
        System.out.println("Increase StdVertexDegree metric (In degree)");
        StdDevVertexDegree stdMetric = new StdDevVertexDegree(DIRECTION.in);
        UpdatableMetricResult stdprevResult = stdMetric.applyUpdatable(graph);
        double stdMetricValue = stdMetric.apply(graph);
        //System.out.println("StdDev metric (apply Uptable) : " + stdprevResult.getResult());
        System.out.println("StdDev metric (Old Value) : " + stdMetricValue);

        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);
        
        AvgVertexDegreeMetric avgMetric = new AvgVertexDegreeMetric();
        double apply = avgMetric.apply(graph);
        System.out.println("Average Vertex Degree : " + apply);
        System.out.println("Maximum Vertex Degree : " + prevResult.getResult());


        // delete an edge
        long seed = System.currentTimeMillis();
        TripleBaseSingleID triple = stdMetric.getTripleRemove(graph, prevResult, seed, true);
        graph = removeEdge(graph, triple.edgeId);

        // Standard Deviation
        UpdatableMetricResult updateStdMetric = stdMetric.update(graph, triple, Operation.REMOVE, stdprevResult);
        System.out.println("StdDev metric (New Result) : " + updateStdMetric.getResult());
        Assert.assertTrue("Metric not increased!", updateStdMetric.getResult() > stdMetricValue);

        System.out.println();

    }

    
    @Test
    public void testcase3() {

        
        ColouredGraph graph = inputGraph;

        // Test case for in-degree, don't remove edge for vertex having maximum degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        // ColouredGraph buildGraph1 = buildGraph1();

        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);
        
        AvgVertexDegreeMetric avgMetric = new AvgVertexDegreeMetric();
        double apply = avgMetric.apply(graph);
        System.out.println("Average Vertex Degree : " + apply);
        System.out.println("Maximum Vertex Degree : " + prevResult.getResult());


        // Standard Deviation metric calculation
        System.out.println("Increase StdVertexDegree metric (Out degree)");
        StdDevVertexDegree stdMetric = new StdDevVertexDegree(DIRECTION.out);
        UpdatableMetricResult stdprevResult = stdMetric.applyUpdatable(graph);
        double stdMetricValue = stdMetric.apply(graph);
        //System.out.println("StdDev metric (apply Uptable) : " + stdprevResult.getResult());
        System.out.println("StdDev metric (Old Value) : " + stdMetricValue);

        // delete an edge
        long seed = System.currentTimeMillis();
        TripleBaseSingleID triple = stdMetric.getTripleRemove(graph, prevResult, seed, true);
        graph = removeEdge(graph, triple.edgeId);
        

        // Standard Deviation
        UpdatableMetricResult updateStdMetric = stdMetric.update(graph, triple, Operation.REMOVE, stdprevResult);
        System.out.println("StdDev metric (New Result) : " + updateStdMetric.getResult());
        Assert.assertTrue("Metric not decreased!", updateStdMetric.getResult() > stdMetricValue);

        System.out.println();

    }

    
    @Test
    public void testcase4() {
        // Test case for in-degree, remove edge for vertex having maximum degree
        
        ColouredGraph graph = inputGraph;

        // Test case for in-degree, don't remove edge for vertex having maximum degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        // ColouredGraph buildGraph1 = buildGraph1();

        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);
        
        AvgVertexDegreeMetric avgMetric = new AvgVertexDegreeMetric();
        double apply = avgMetric.apply(graph);
        System.out.println("Average Vertex Degree : " + apply);
        System.out.println("Maximum Vertex Degree : " + prevResult.getResult());

        // Standard Deviation metric calculation
        System.out.println("Decrease StdVertexDegree metric (Out degree)");
        StdDevVertexDegree stdMetric = new StdDevVertexDegree(DIRECTION.out);
        UpdatableMetricResult stdprevResult = stdMetric.applyUpdatable(graph);
        double stdMetricValue = stdMetric.apply(graph);
        //System.out.println("StdDev metric (apply Uptable) : " + stdprevResult.getResult());
        System.out.println("StdDev metric (Old Value) : " + stdMetricValue);

        /*
         * IntSet outEdges = graph.getOutEdges(2); System.out.println(outEdges); for(int
         * edge: outEdges) { System.out.println(graph.getHeadOfTheEdge(edge));
         * System.out.println(graph.getTailOfTheEdge(edge)); System.out.println(); }
         * 
         * IntSet inEdges = graph.getInEdges(2); System.out.println(inEdges); for(int
         * edge:inEdges) { System.out.println(graph.getHeadOfTheEdge(edge));
         * System.out.println(graph.getTailOfTheEdge(edge)); System.out.println(); }
         */

        // delete an edge
        long seed = System.currentTimeMillis();
        TripleBaseSingleID triple = stdMetric.getTripleRemove(graph, prevResult, seed, false);
        graph = removeEdge(graph, triple.edgeId);
        

        // Standard Deviation
        UpdatableMetricResult updateStdMetric = stdMetric.update(graph, triple, Operation.REMOVE, stdprevResult);
        System.out.println("StdDev metric (New Result) : " + updateStdMetric.getResult());
        Assert.assertTrue("Metric not decreased!", updateStdMetric.getResult() < stdMetricValue);

        System.out.println();

    }


}
