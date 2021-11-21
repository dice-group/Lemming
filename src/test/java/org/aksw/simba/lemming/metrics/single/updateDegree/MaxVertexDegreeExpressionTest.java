package org.aksw.simba.lemming.metrics.single.updateDegree;

import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import grph.Grph.DIRECTION;
import junit.framework.Assert;

public class MaxVertexDegreeExpressionTest extends UpdateMetricTest {
    private static final String GRAPH_FILE = "email-Eu-core.n3"; // graph_loop_2.n3
    private static final String GRAPH_FILE1 = "email-Eu-core.n3";
    
    @SuppressWarnings("deprecation")
    @Test
    public void testcase1() {
        
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        
        //Test case for in-degree, don't remove edge for vertex having maximum degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        //ColouredGraph buildGraph1 = buildGraph1();
        
        
        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);
        double metricValue = metric.apply(graph);
        Assert.assertEquals(metricValue, prevResult.getResult());
        
        

        //delete an edge
        long seed = System.currentTimeMillis();
        TripleBaseSingleID triple = metric.getTripleRemove(graph, prevResult, seed, false);
        graph = removeEdge(graph, triple.edgeId);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertTrue("Edge removed for candidate", newResult.getResult() <= metricValue); 
        
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testcase2() {
        //Test case for in-degree, remove edge for vertex having maximum degree
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        
        //Test case for in-degree, don't remove edge for vertex having maximum degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.in);
        //ColouredGraph buildGraph1 = buildGraph1();
        
        
        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);
        double metricValue = metric.apply(graph);
        Assert.assertEquals(metricValue, prevResult.getResult());
        
        

        //delete an edge
        long seed = System.currentTimeMillis();
        TripleBaseSingleID triple = metric.getTripleRemove(graph, prevResult, seed, true);
        graph = removeEdge(graph, triple.edgeId);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertTrue("Edge not removed for candidate", newResult.getResult() < metricValue); 
        
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testcase3() {
        
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE1);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        
        //Test case for in-degree, don't remove edge for vertex having maximum degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        //ColouredGraph buildGraph1 = buildGraph1();
        
        
        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);
        double metricValue = metric.apply(graph);
        Assert.assertEquals(metricValue, prevResult.getResult());
        
        

        //delete an edge
        long seed = System.currentTimeMillis();
        TripleBaseSingleID triple = metric.getTripleRemove(graph, prevResult, seed, false);
        graph = removeEdge(graph, triple.edgeId);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertTrue("Edge removed for candidate", newResult.getResult() <= metricValue); 
        
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testcase4() {
        //Test case for in-degree, remove edge for vertex having maximum degree
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE1);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        
        //Test case for in-degree, don't remove edge for vertex having maximum degree
        MaxVertexDegreeMetric metric = new MaxVertexDegreeMetric(DIRECTION.out);
        //ColouredGraph buildGraph1 = buildGraph1();
        
        
        UpdatableMetricResult prevResult = metric.applyUpdatable(graph);
        double metricValue = metric.apply(graph);
        Assert.assertEquals(metricValue, prevResult.getResult());
        
        /*IntSet outEdges = graph.getOutEdges(2);
        System.out.println(outEdges);
        for(int edge: outEdges) {
            System.out.println(graph.getHeadOfTheEdge(edge));
            System.out.println(graph.getTailOfTheEdge(edge));
            System.out.println();
        }
        
        IntSet inEdges = graph.getInEdges(2);
        System.out.println(inEdges);
        for(int edge:inEdges) {
            System.out.println(graph.getHeadOfTheEdge(edge));
            System.out.println(graph.getTailOfTheEdge(edge));
            System.out.println();
        }*/
        

        //delete an edge
        long seed = System.currentTimeMillis();
        TripleBaseSingleID triple = metric.getTripleRemove(graph, prevResult, seed, true);
        graph = removeEdge(graph, triple.edgeId);
        UpdatableMetricResult newResult = metric.update(graph, triple, Operation.REMOVE, prevResult);
        Assert.assertTrue("Edge not removed for candidate", newResult.getResult() < metricValue); 
        
    }
}
