package org.aksw.simba.lemming.tools;

import grph.Grph;
import grph.algo.topology.ClassicalGraphs;
import grph.algo.topology.StarTopologyGenerator;
import grph.in_memory.InMemoryGrph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.PersonGraphDataset;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.single.AvgClusteringCoefficientMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexInDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexOutDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationGroupingTriple;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly2;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationSimpleApproach;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationSimpleApproach2;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.generator.GraphOptimization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.mimicgraph.metricstorage.MetricAndConstantValuesCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class GraphGenerationTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationTest.class);
	private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
	private static final String PERSON_GRAPH = "PersonGraph/";
	
	//default number of vertices is 10000
	private static int NUMBER_OF_DESIRED_VERTICES = 10000;
	
	public static void main(String[] args) {
		IGraphGeneration mGrphGenerator;
		IDatasetManager mDatasetManager;
		boolean isStop = true;
		
		// For this test, we do not need assertions
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);
        
        /*---------------------------------------------------
        Collect input arguments
		 * -ds: dataset
		 * 		value: swdf (semanticwebdogfood), or pg (persongraph)  
		 * 
		 * -nv: number of vertices
		 * 
		 * -t: methods of generator
		 * 		value: R: random approach, RD: random with degree approach
		 * 		value: D: distribution approach, DD: disitrbution and degree approach
		 * 		value: C: clustering approach, CD: clustering and degree approach
		 * -r: is random optimization 
        ----------------------------------------------------*/
        Map<String, String> mapArgs = parseArguments(args);
        
        /*---------------------------------------------------
         Definition of metrics to form constant expression
         ----------------------------------------------------*/
        List<SingleValueMetric> metrics = new ArrayList<>();
        //these are two fixed metrics: NodeTriangleMetric and EdgeTriangleMetric
        metrics.add(new NodeTriangleMetric());
        metrics.add(new EdgeTriangleMetric());
        
        //these are optional metrics
        metrics.add(new AvgClusteringCoefficientMetric());
        metrics.add(new MaxVertexOutDegreeMetric());
        metrics.add(new MaxVertexInDegreeMetric());
        metrics.add(new NumberOfEdgesMetric());
        metrics.add(new NumberOfVerticesMetric());
        //metrics.add(new DiameterMetric());
        
        /*---------------------------------------------------
        Loading RDF graphs into ColouredGraph models
        ----------------------------------------------------*/
        ColouredGraph graphs[] = new ColouredGraph[20];
        
        //load RDF data to coloured graph
        String dataset = mapArgs.get("-ds");
        String datasetPath = "";
        if(dataset != null && dataset.equalsIgnoreCase("pg")){
        	LOGGER.info("Loading PersonGraph...");
        	mDatasetManager = new PersonGraphDataset();
        	datasetPath = PERSON_GRAPH;
        }else{
        	LOGGER.info("Loading SemanticWebDogFood...");
        	mDatasetManager = new SemanticWebDogFoodDataset();
        	datasetPath = SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH;
        }
        
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);
        
        /*---------------------------------------------------
        Loading metrics values and constant expressions 
        ----------------------------------------------------*/
        MetricAndConstantValuesCarrier valuesCarrier = new MetricAndConstantValuesCarrier(datasetPath);
        if(!valuesCarrier.isComputableMetrics(metrics)){
        	LOGGER.error("The list of metrics has some metrics that are not existing in the precomputed metric values.");
        	LOGGER.warn("Please generate the file [value_store.val] again!");
        	return ;
        }
        
        /*---------------------------------------------------
        Generation for a draft graph
        ----------------------------------------------------*/
        //number of vertices
        String strNoOfVertices = mapArgs.get("-nv");
        if(strNoOfVertices!= null){
        	try{
        		NUMBER_OF_DESIRED_VERTICES = Integer.parseInt(strNoOfVertices);
        	}catch(Exception e){}
        }
        
        //define generator
        String typeGenerator = mapArgs.get("-t");
        if(typeGenerator == null || typeGenerator.isEmpty() || typeGenerator.equalsIgnoreCase("R")){
        	mGrphGenerator = new GraphGenerationRandomly(NUMBER_OF_DESIRED_VERTICES, graphs);        	
        }else if(typeGenerator.equalsIgnoreCase("RD")){
        	mGrphGenerator = new GraphGenerationRandomly2(NUMBER_OF_DESIRED_VERTICES, graphs);
        }else if(typeGenerator.equalsIgnoreCase("D")){
        	mGrphGenerator = new GraphGenerationSimpleApproach(NUMBER_OF_DESIRED_VERTICES, graphs);
        }else if(typeGenerator.equalsIgnoreCase("DD")){
        	mGrphGenerator = new GraphGenerationSimpleApproach2(NUMBER_OF_DESIRED_VERTICES, graphs);
        }else if(typeGenerator.equalsIgnoreCase("C")){
        	mGrphGenerator = new GraphGenerationGroupingTriple(NUMBER_OF_DESIRED_VERTICES, graphs);        	
        }else{
        	//TODO CHECK THIS ONE AGAIN
        	//mGrphGenerator = new GraphGenerationGroupingTriple2(NUMBEROFDESIREDVERTICES, graphs);
        	mGrphGenerator = new GraphGenerationGroupingTriple(NUMBER_OF_DESIRED_VERTICES, graphs);
        }

        LOGGER.info("Generating a first version of mimic graph ...");
        //create a draft graph
        double startTime = System.currentTimeMillis();
        // generate the new graph
        ColouredGraph tempGrph =  mGrphGenerator.generateGraph();
        // estimate the costed time for generation
        double duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Finished graph generation process in " + duration +" ms");
        
        /*---------------------------------------------------
        Optimization with constant expressions
        ----------------------------------------------------*/
        GraphOptimization grphOptimizer = new GraphOptimization(graphs, mGrphGenerator, metrics, valuesCarrier);
        LOGGER.info("Optimizing the mimic graph ...");
        // TODO check if it is necessary to randomly refine graph 
        grphOptimizer.setRefineGraphRandomly(false);
        grphOptimizer.refineGraph();
        
        //output result to files       
        grphOptimizer.printResult(startTime);
        
        
        /*---------------------------------------------------
        Lexicalization with word2vec
        ----------------------------------------------------*/
        GraphLexicalization graphLexicalization = new GraphLexicalization(graphs, mGrphGenerator);
        mDatasetManager.writeGraphsToFile(graphLexicalization.lexicalizeGraph());
        LOGGER.info("Application exits!!!");
	}
	
	/**
	 * 
	 * @param args list of input arguments
	 * @return a map of key and values 
	 */
	private static Map<String, String> parseArguments(String[] args){
		/*
		 * -ds: dataset
		 * 		value: swdf (semanticwebdogfood), or pg (persongraph)  
		 * 
		 * -nv: number of vertices
		 * 
		 * -t: methods of generator
		 * 		value: R: random approach, RD: random with degree approach
		 * 		value: D: distribution approach, DD: disitrbution and degree approach
		 * 		value: C: clustering approach, CD: clustering and degree approach
		 * 
		 * -r: is random optimization 
		 */
		Map<String, String> mapArgs = new HashMap<String, String>();
		
		if(args.length != 0){
			for(int i = 0 ; i < args.length ; i++){
				String param = args[i];
				if((i+1) < args.length){
					String value = args[i+1];
					// target dataset
					if(param.equalsIgnoreCase("-ds") ){
						mapArgs.put("-ds", value);
					}
					// number of vertices
					else if (param.equalsIgnoreCase("-nv")){
						mapArgs.put("-nv", value);
					} 
					//type of graph generator
					else if(param.equalsIgnoreCase("-t")){
						mapArgs.put("-t", value);
					}
					else if(param.equalsIgnoreCase("-r")){	
						mapArgs.put("-r", value);
					}
				}
			}
		}
		return mapArgs;
	}
	
	/**
	 * create reference graph to compute constant expressions
	 * 
	 * @param graphs input dataset graphs
	 * @param metrics list of exploited metrics
	 * 
	 * @return map of metric values of reference graphs 
	 */
	@SuppressWarnings("unchecked")
    private static ObjectDoubleOpenHashMap<String>[] createReferenceGraphVectors(ColouredGraph[] graphs,
                                                                                 List<SingleValueMetric> metrics) {
        Grph temp;
        int numberOfNodes, partSize;
        List<ObjectDoubleOpenHashMap<String>> vectors = new ArrayList<ObjectDoubleOpenHashMap<String>>(
                5 * graphs.length);
        
        /*------------------
         * FILTER metrics
         * costly metrics: node triangles, edge triangles, clustering coefficient
         * naive metrics: others
         ------------------*/
        List<SingleValueMetric> costlyMetrics = new ArrayList<SingleValueMetric>();
        List<SingleValueMetric> naiveMetrics = new ArrayList<SingleValueMetric>();
        for(SingleValueMetric metric : metrics){
        	if(metric.getName().equalsIgnoreCase("edgeTriangles") || 
        			metric.getName().equalsIgnoreCase("nodetriangles")||
        			metric.getName().equalsIgnoreCase("avgClusterCoefficient")){
        		costlyMetrics.add(metric);
        	} else {
        		naiveMetrics.add(metric);
        	}
        }
        
        
        
        for (int i = 0; i < graphs.length; ++i) {
            numberOfNodes = graphs[i].getGraph().getNumberOfVertices();
            partSize = (int) Math.sqrt(numberOfNodes);
            
            LOGGER.info("Generating reference graphs with " + numberOfNodes + " nodes.");
            /*------------------
             *  Star
             ------------------*/
            StarTopologyGenerator starGenerator = new StarTopologyGenerator();
            temp = new InMemoryGrph();
            temp.addNVertices(numberOfNodes);
            starGenerator.compute(temp);
            ColouredGraph startColouredGraph = new ColouredGraph(temp, null, null);
            ObjectDoubleOpenHashMap<String> starGraphMetrics = MetricUtils.calculateGraphMetrics(startColouredGraph, naiveMetrics);
            for(SingleValueMetric metric: costlyMetrics){
            	if(metric.getName().equalsIgnoreCase("edgeTriangles")|| 
            			metric.getName().equalsIgnoreCase("nodetriangles")||
            			metric.getName().equalsIgnoreCase("avgClusterCoefficient")){
            		starGraphMetrics.putOrAdd(metric.getName(), 0, 0);
            	}else{
            		double val = metric.apply(startColouredGraph);
            		starGraphMetrics.putOrAdd(metric.getName(), val, val);
            	}
            }
            vectors.add(starGraphMetrics);
            temp = null;

            /*------------------
             *  Grid
             ------------------*/
            
            ColouredGraph gridColouredGraph = new ColouredGraph(ClassicalGraphs.grid(partSize, partSize), null, null);
            ObjectDoubleOpenHashMap<String> gridGraphMetrics = MetricUtils.calculateGraphMetrics(gridColouredGraph, naiveMetrics);
            
            for(SingleValueMetric metric: costlyMetrics){
            	if(metric.getName().equalsIgnoreCase("edgeTriangles")|| 
            			metric.getName().equalsIgnoreCase("nodetriangles")||
            			metric.getName().equalsIgnoreCase("avgClusterCoefficient")){
            		gridGraphMetrics.putOrAdd(metric.getName(), 0, 0);
            	}else{
            		double val = metric.apply(gridColouredGraph);
            		gridGraphMetrics.putOrAdd(metric.getName(), val, val);
            	}
            }
            
            vectors.add(gridGraphMetrics);
            

            /*------------------
             *  Ring
             ------------------*/
            ColouredGraph ringColouredGraph = new ColouredGraph(ClassicalGraphs.cycle(numberOfNodes), null, null);
            ObjectDoubleOpenHashMap<String> ringGraphMetrics = MetricUtils.calculateGraphMetrics(ringColouredGraph, naiveMetrics);
            
            for(SingleValueMetric metric: costlyMetrics){
            	if(metric.getName().equalsIgnoreCase("edgeTriangles")|| 
            			metric.getName().equalsIgnoreCase("nodetriangles")||
            			metric.getName().equalsIgnoreCase("avgClusterCoefficient")){
            		if(numberOfNodes == 3){
            			ringGraphMetrics.putOrAdd(metric.getName(), 1, 1);
            		} else{
            			ringGraphMetrics.putOrAdd(metric.getName(), 0, 0);
            		}
            	}else{
            		double val = metric.apply(gridColouredGraph);
            		ringGraphMetrics.putOrAdd(metric.getName(), val, val);
            	}
            }
            
            vectors.add(ringGraphMetrics);

            /*------------------
             *  Clique
             ------------------*/
            ColouredGraph cliqueColouredGraph = new ColouredGraph(ClassicalGraphs.completeGraph(partSize), null, null);
            ObjectDoubleOpenHashMap<String> cliqueGraphMetrics = MetricUtils.calculateGraphMetrics(cliqueColouredGraph, naiveMetrics);
            
            for(SingleValueMetric metric: costlyMetrics){
            	if(metric.getName().equalsIgnoreCase("edgeTriangles")|| 
            			metric.getName().equalsIgnoreCase("nodetriangles")||
            			metric.getName().equalsIgnoreCase("avgClusterCoefficient")){
            		
            			if(partSize <3){
            				cliqueGraphMetrics.putOrAdd(metric.getName(), 0, 0);
            			}else{
            				int noOfValues = partSize *( partSize - 1 ) *( partSize - 2)/6;
            				cliqueGraphMetrics.putOrAdd(metric.getName(), noOfValues, noOfValues);
            			}
            	}else{
            		double val = metric.apply(gridColouredGraph);
            		cliqueGraphMetrics.putOrAdd(metric.getName(), val, val);
            	}
            }
            
            vectors.add(cliqueGraphMetrics);

            /*------------------
             *  Bipartite
             ------------------*/
            // partSize = numberOfNodes / 2;
            partSize = numberOfNodes / 8;
            ColouredGraph bipartiteColouredGraph = new ColouredGraph(ClassicalGraphs.completeBipartiteGraph(partSize, partSize), null, null);
            ObjectDoubleOpenHashMap<String> bipartiteGraphMetrics = MetricUtils.calculateGraphMetrics(bipartiteColouredGraph, naiveMetrics);
            
            for(SingleValueMetric metric: costlyMetrics){
            	if(metric.getName().equalsIgnoreCase("edgeTriangles")|| 
            			metric.getName().equalsIgnoreCase("nodetriangles")||
            			metric.getName().equalsIgnoreCase("avgClusterCoefficient")){
            		bipartiteGraphMetrics.putOrAdd(metric.getName(), 0, 0);
            	}else{
            		double val = metric.apply(gridColouredGraph);
            		bipartiteGraphMetrics.putOrAdd(metric.getName(), val, val);
            	}
            }
            vectors.add(bipartiteGraphMetrics);
        }
        return vectors.toArray(new ObjectDoubleOpenHashMap[vectors.size()]);
    }
}

