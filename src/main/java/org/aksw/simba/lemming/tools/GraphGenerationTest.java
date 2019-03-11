package org.aksw.simba.lemming.tools;

import grph.Grph.DIRECTION;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.PersonGraphDataset;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationClusteringBased;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationClusteringBased2;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly2;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationSimpleApproach;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationSimpleApproach2;
import org.aksw.simba.lemming.mimicgraph.generator.GraphLexicalization;
import org.aksw.simba.lemming.mimicgraph.generator.GraphOptimization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class GraphGenerationTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationTest.class);
	private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
	private static final String PERSON_GRAPH = "PersonGraph/";
	
	//default number of vertices is 10000
	private static int mNumberOfDesiredVertices = 10000;
	
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
		 * 		value: integer number - denoting number of given vertices
		 * 
		 * -thr: number of threads using
		 * 		value: integer number - denoting number of threads used/
		 * 
		 * -t: methods of generator
		 * 		value: 	R: random approach, 
		 * 				RD: random with degree approach
		 * 		value: 	D: distribution approach, 
		 * 				DD: disitrbution and degree approach
		 * 		value: 	C: clustering approach, 
		 * 				CD: clustering and degree approach
		 * 
		 * -r: random optimization 
		 * -op: (optional) number of optimization steps 
		 * 
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
        metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
        metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
        metrics.add(new AvgVertexDegreeMetric());
        metrics.add(new StdDevVertexDegree(DIRECTION.in));
        metrics.add(new StdDevVertexDegree(DIRECTION.out));
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
        ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);
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
        		mNumberOfDesiredVertices = Integer.parseInt(strNoOfVertices);
        	}catch(Exception e){}
        }
        
        
        String numberOfThreads = mapArgs.get("-thrs");
        int iNumberOfThreads = -1;
        if(numberOfThreads!= null){
        	try{
        		iNumberOfThreads = Integer.parseInt(numberOfThreads);        		
        	}catch(Exception e){}
        }
        
        //define generator
        String typeGenerator = mapArgs.get("-t");
        if(typeGenerator == null || typeGenerator.isEmpty() || typeGenerator.equalsIgnoreCase("R")){
        	mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads);       	
        }else if(typeGenerator.equalsIgnoreCase("RD")){
        	mGrphGenerator = new GraphGenerationRandomly2(mNumberOfDesiredVertices, graphs, iNumberOfThreads);
        }else if(typeGenerator.equalsIgnoreCase("D")){
        	mGrphGenerator = new GraphGenerationSimpleApproach(mNumberOfDesiredVertices, graphs, iNumberOfThreads);
        }else if(typeGenerator.equalsIgnoreCase("DD")){
        	mGrphGenerator = new GraphGenerationSimpleApproach2(mNumberOfDesiredVertices, graphs, iNumberOfThreads);
        }else if(typeGenerator.equalsIgnoreCase("C")){
        	mGrphGenerator = new GraphGenerationClusteringBased(mNumberOfDesiredVertices, graphs, iNumberOfThreads);        	
        }else if(typeGenerator.equalsIgnoreCase("CD")){
        	mGrphGenerator = new GraphGenerationClusteringBased2(mNumberOfDesiredVertices, graphs, iNumberOfThreads);
        } else{
        	mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads);
        }

        LOGGER.info("Generating a first version of mimic graph ...");
        //create a draft graph
        double startTime = System.currentTimeMillis();
        
        mGrphGenerator.generateGraph();
        // estimate the costed time for generation
        double duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Finished graph generation process in " + duration +" ms");
        
        LOGGER.info("Storing the graph prior to optimization");
        GraphLexicalization graphLexicalization = new GraphLexicalization(graphs, mGrphGenerator);
        String preSaveFiled = mDatasetManager.writeGraphsToFile(graphLexicalization.lexicalizeGraph());
        LOGGER.info("Saved under: "+preSaveFiled);
        
        /*---------------------------------------------------
        Optimization with constant expressions
        ----------------------------------------------------*/
        GraphOptimization grphOptimizer = new GraphOptimization(graphs, mGrphGenerator, metrics, valuesCarrier);
        LOGGER.info("Optimizing the mimic graph ...");
        // TODO check if it is necessary to randomly refine graph 
        grphOptimizer.setRefineGraphRandomly(false);
        //number of optimizations
        String strNoOfOptimizations = mapArgs.get("-op");
        if(strNoOfOptimizations!= null){
        	try{
        		int iNumberOfOptimizationSteps = Integer.parseInt(strNoOfOptimizations);
        		grphOptimizer.setNumberOfOptimizations(iNumberOfOptimizationSteps);
        	}catch(Exception e){}
        }
        
        //optimize graph
        grphOptimizer.refineGraph();
        
        /*---------------------------------------------------
        Lexicalization with word2vec
        ----------------------------------------------------*/
        LOGGER.info("Lexicalize the mimic graph ...");
        graphLexicalization = new GraphLexicalization(graphs, mGrphGenerator);
        String saveFiled = mDatasetManager.writeGraphsToFile(graphLexicalization.lexicalizeGraph());
        
        //output results to file "LemmingEx.result"       
        grphOptimizer.printResult(mapArgs, startTime, saveFiled);
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
		 * -r: random optimization 
		 * -thrs: 	the number of threads
		 * 			by default, the application runs with a single thread
		 * 
		 * -op: (optional) number of optimization steps 
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
					else if(param.equalsIgnoreCase("-thrs")){
						mapArgs.put("-thrs", value);
					}
					else if(param.equalsIgnoreCase("-op")){
						mapArgs.put("-op", value);
					}
				}
			}
		}
		return mapArgs;
	}

	private static void printConstantResults(List<SingleValueMetric> lstMetrics, ConstantValueStorage constStorage, ColouredGraph[] origGraphs ){
		BufferedWriter fWriter ;
		try{
			LOGGER.warn("Output results to file!");
			
			fWriter = new BufferedWriter( new FileWriter("ConstantOfLatestGraph.result", true));
			
			for(ColouredGraph grph : origGraphs){
				if(grph.getVertices().size() == 45387 || grph.getVertices().size()==792921){
					
					ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<String>();
					//compute list of metrics
					
					for(SingleValueMetric metric: lstMetrics){
						double val = metric.apply(grph);
						mapMetricValues.putOrAdd(metric.getName(), 0, 0);
						mapMetricValues.put(metric.getName(), val);
					}
					
					Set<Expression> setExpressions = constStorage.getConstantExpressions();
					
					fWriter.write("#----------------------------------------------------------------------#\n");
					fWriter.write("# Graph "+ 45387 +".\n");
					for(Expression expr: setExpressions){
						double constVal = expr.getValue(mapMetricValues);
						fWriter.write("\t Expr: "+ expr.toString() +":"+constVal +"\n");
					}
					fWriter.write("#----------------------------------------------------------------------#\n");
				}			
			}
			// metric values of all graphs
			fWriter.close();
		}catch(Exception ex){
			LOGGER.warn("Cannot output results to file! Please check: " + ex.getMessage());
		}
	}
	
	
	private static void printMetricResults(List<SingleValueMetric> lstMetrics, ColouredGraph[] origGraphs ){
		BufferedWriter fWriter ;
		try{
			LOGGER.warn("Output results to file!");
			
			fWriter = new BufferedWriter( new FileWriter("MetricsOfLatestGraph.result", true));
			
			int iIndex = 1;			
			
			for(ColouredGraph grph : origGraphs){
				fWriter.write("#----------------------------------------------------------------------#\n");
				fWriter.write("# Graph "+iIndex +".\n");
				for(SingleValueMetric metric : lstMetrics){
					double val = metric.apply(grph);				
					fWriter.write("\t Metric: "+metric.getName() +": "+ val+"\n");					
				}
				iIndex ++;
				fWriter.write("#----------------------------------------------------------------------#\n");
			}
			// metric values of all graphs
			fWriter.close();
		}catch(Exception ex){
			LOGGER.warn("Cannot output results to file! Please check: " + ex.getMessage());
		}
	}
}

