package org.aksw.simba.lemming.metrics.testdummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.Operation;
import org.aksw.simba.lemming.algo.expression.Operator;
import org.aksw.simba.lemming.creation.GeologyDataset;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.LinkedGeoDataset;
import org.aksw.simba.lemming.creation.PersonGraphDataset;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.EdgeModifier;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.ErrorScoreCalculator;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.ExpressionChecker;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.GetMetricsFromExpressions;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationClusteringBased;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationClusteringBased2;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationRandomly2;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationSimpleApproach;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerationSimpleApproach2;
import org.aksw.simba.lemming.mimicgraph.generator.GraphOptimization;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import grph.Grph.DIRECTION;

public class CharacteristicExpressionsTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CharacteristicExpressionsTest.class);

	private static final String GEOLOGY_DATASET_FOLDER_PATH = "GeologyGraphs/";
	private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
	private static final String PERSON_GRAPH = "PersonGraph/";
	private static final String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";

	// Change below as per data set
	private String dataset = "geology";
	//private String dataset = "swdf";
	
	 Map<String, String> mapArgs = new HashMap<>();
	 
	 ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();
    

	 //Creating a set of Expressions for manually creating expressions.
	 Set<Expression> setOfExpressions = new HashSet<>();
	 
	@Test
	public void testToEvaluateCharacteristicExpressions() {

		// Initialize dataset path
		String datasetPath = GEOLOGY_DATASET_FOLDER_PATH;

		// Initialize ConstantValueStorage class
		ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);

		// Get the constant expressions
		Set<Expression> constantExpressions = valuesCarrier.getConstantExpressions();

		// Initialize the Expression optimization class
		GetMetricsFromExpressions clasExpOpt = new GetMetricsFromExpressions();
		clasExpOpt.compute(constantExpressions);

		System.out.println("Direct Proportional metrics : " + clasExpOpt.getDirectProportionalMetricsSet());
		System.out.println("Inverse Proportional metrics : " + clasExpOpt.getInverseProportionalMetricsSet());

	}

	
	@Test
	public void checkMetricsFromExpressions() {

		List<SingleValueMetric> metrics = new ArrayList<>();
		metrics.add(new NodeTriangleMetric());
		metrics.add(new EdgeTriangleMetric());
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
		metrics.add(new AvgVertexDegreeMetric());
		metrics.add(new StdDevVertexDegree(DIRECTION.in));
		metrics.add(new StdDevVertexDegree(DIRECTION.out));
		metrics.add(new NumberOfEdgesMetric());
		metrics.add(new NumberOfVerticesMetric());

		int mNumberOfDesiredVerticesGeology = 1281;
		int mNumberOfDesiredVerticeslgeo = 591649;
		int mNumberOfDesiredVerticesswdf = 45420;
		int mNumberOfDesiredVerticespg = 792923;
		int iNumberOfThreads = -1;

		long seed = System.currentTimeMillis();
		LOGGER.info("Current Seed is " + seed);

		ColouredGraph graphs[] = getInputGraphs();
		IGraphGeneration mGraphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVerticesGeology, graphs,
				iNumberOfThreads, seed);
		ColouredGraph clonedGrph = mGraphGenerator.getMimicGraph().clone();

		EdgeModifier mEdgeModifier = new EdgeModifier(clonedGrph, metrics);

		// Initialize dataset path
		String datasetPath = GEOLOGY_DATASET_FOLDER_PATH;

		// Initialize ConstantValueStorage class
		ConstantValueStorage valuesCarrier = new ConstantValueStorage(datasetPath);

		// Get the constant expressions
		Set<Expression> constantExpressions = valuesCarrier.getConstantExpressions();

		// Initialize Error Score Calculator
		ErrorScoreCalculator mErrScoreCalculator = new ErrorScoreCalculator(graphs, valuesCarrier);

		// Initialize Expression Checker
		ExpressionChecker expressionChecker = new ExpressionChecker(mErrScoreCalculator.getmMapOfMeanValues(), valuesCarrier.getMapConstantValues());

		ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();
		mapMetricValues.put("#vertices", 1281.0);
		mapMetricValues.put("stdDevInDegree", 16.556235861947844);
		mapMetricValues.put("#edgetriangles", 163699.0);
		mapMetricValues.put("maxInDegree", 219.0);
		mapMetricValues.put("avgDegree", 6.82903981264637);
		mapMetricValues.put("maxOutDegree", 539.0);
		mapMetricValues.put("stdDevOutDegree", 24.148731826015553);
		mapMetricValues.put("#nodetriangles", 17163.0);
		mapMetricValues.put("#edges", 8748.0);

		expressionChecker.storeExpressions(mapMetricValues);

		expressionChecker.checkExpressions();

		System.out.println("Metric Details : ");
		System.out.println("Metric to Increase : " + expressionChecker.getMetricToIncrease()
				+ ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceIncreaseMetric());

		System.out.println("Metric to Decrease : " + expressionChecker.getMetricToDecrease()
				+ ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceDecreaseMetric());

	}
	 
	 private void initializeInput() {
	     
	     //SWDF parameters
	     
	      
	     mapArgs.put("-ds", "swdf");
	     mapArgs.put("-nv", "45420");
	     mapArgs.put("-t", "R");
	     mapArgs.put("-op", "50000");
	     //mapArgs.put("-l", "Initialized_MimicGraph.ser");
	     
	     mapMetricValues.put("#vertices", 45420.0);
         mapMetricValues.put("stdDevInDegree", 69.55651745881998);
         mapMetricValues.put("#edgetriangles", 978980.0);
         mapMetricValues.put("maxInDegree", 9365.0);
         mapMetricValues.put("avgDegree", 6.4538529282254515);
         mapMetricValues.put("maxOutDegree", 28179.0);
         mapMetricValues.put("stdDevOutDegree", 179.7017899058663);
         mapMetricValues.put("#nodetriangles", 373086.0);
         mapMetricValues.put("#edges", 293134.0);
         
         //Expressions for SWDF from paper
         Operation operation1 = new Operation(
                 new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                 new Operation(
                         new Operation(new AtomicVariable(new NumberOfVerticesMetric()), 
                                 new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), 
                                 Operator.TIMES), 
                         new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS)
                 , Operator.DIV);
         //System.out.println(operation1.toString());
         setOfExpressions.add(operation1);
         
         Operation operation2 = new Operation(
                 new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                 new Operation(
                         new Operation(new AtomicVariable(new NumberOfVerticesMetric()), 
                                 new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), 
                                 Operator.PLUS), 
                         new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), Operator.MINUS)
                 , Operator.DIV);
         //System.out.println(operation2.toString());
         setOfExpressions.add(operation2);
         
         Operation operation3 = new Operation(
                 new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                 new Operation(
                         new Operation(new AtomicVariable(new NumberOfVerticesMetric()), 
                                 new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), 
                                 Operator.DIV), 
                         new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS)
                 , Operator.DIV);
         //System.out.println(operation3.toString());
         setOfExpressions.add(operation3);
         
         Operation operation4 = new Operation(
                 new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                 new Operation(
                         new Operation(new AtomicVariable(new NumberOfEdgesMetric()), 
                                 new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), 
                                 Operator.PLUS), 
                         new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), Operator.MINUS)
                 , Operator.DIV);
         //System.out.println(operation4.toString());
         setOfExpressions.add(operation4);
         
         Operation operation5 = new Operation(
                 new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)) , 
                 new Operation(
                         new Operation(new AtomicVariable(new NumberOfVerticesMetric()), 
                                 new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), 
                                 Operator.DIV), 
                         new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS)
                 , Operator.DIV);
         //System.out.println(operation5.toString());
         setOfExpressions.add(operation5);
         mapMetricValues.put(operation1.toString(), 0.0926);
         mapMetricValues.put(operation2.toString(), -0.1751);
         mapMetricValues.put(operation3.toString(), 0.9997);
         mapMetricValues.put(operation4.toString(), 0.1294);
         mapMetricValues.put(operation5.toString(), 0.9965);
	     
	     /*
	     
	     //Geology parameters
	     mapArgs.put("-ds", "geology");
         mapArgs.put("-nv", "1281");
         mapArgs.put("-t", "R");
         mapArgs.put("-op", "30000");
         //mapArgs.put("-l", "Initialized_MimicGraph.ser"); // commented so that -l parameter is null
         
         //Initializing metrics for mimic graph
         mapMetricValues.put("#vertices", 1281.0);
         mapMetricValues.put("stdDevInDegree", 16.556235861947844);
         mapMetricValues.put("#edgetriangles", 163699.0);
         mapMetricValues.put("maxInDegree", 219.0);
         mapMetricValues.put("avgDegree", 6.82903981264637);
         mapMetricValues.put("maxOutDegree", 539.0);
         mapMetricValues.put("stdDevOutDegree", 24.148731826015553);
         mapMetricValues.put("#nodetriangles", 17163.0);
         mapMetricValues.put("#edges", 8748.0);
         
         */
         
         
	 }
	
	//@Test
	public void checkMetricsFromExpressionsSWDF() {

	    int mNumberOfDesiredVertices = 10000;
	    
	    IGraphGeneration mGrphGenerator;
        IDatasetManager mDatasetManager;
        //boolean isStop = true;
        
        /*---------------------------------------------------
        Collect input arguments
         * -ds: dataset
         *      value: swdf (semanticwebdogfood), pg (persongraph) 
         *              , lgeo (linkedgeo) or geology
         * 
         * -nv: number of vertices
         *      value: integer number - denoting number of given vertices
         * 
         * -thr: number of threads using
         *      value: integer number - denoting number of threads used/
         * 
         * -t: methods of generator
         *      value:  R: random approach, 
         *              RD: random with degree approach
         *      value:  D: distribution approach, 
         *              DD: distribution and degree approach
         *      value:  C: clustering approach, 
         *              CD: clustering and degree approach
         * 
         * -r: random optimization 
         * -op: (optional) number of optimization steps 
         * -s:  (optional) seed
         * -l:  (optional) path to the mimic graph to be loaded,
         * this skips the mimic graph generation process and loads it directly from file
         * 
        ----------------------------------------------------*/
        initializeInput();
        
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
        }else if(dataset.equalsIgnoreCase("swdf")){
            LOGGER.info("Loading SemanticWebDogFood...");
            mDatasetManager = new SemanticWebDogFoodDataset();
            datasetPath = SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH;
        } else if(dataset.equalsIgnoreCase("lgeo")) {
            LOGGER.info("Loading LinkedGeo...");
            mDatasetManager = new LinkedGeoDataset();
            datasetPath = LINKED_GEO_DATASET_FOLDER_PATH;
        } else if(dataset.equalsIgnoreCase("geology")) {
            LOGGER.info("Loading Geology Dataset...");
            mDatasetManager = new GeologyDataset();
            datasetPath = GEOLOGY_DATASET_FOLDER_PATH;
        } else {
            LOGGER.error("Got an unknown dataset name: \"{}\". Aborting", dataset);
            return;
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
        
        String seedString = mapArgs.get("-s");
        long seed = System.currentTimeMillis();
//              new Random().nextLong();
        if(seedString!= null){
            try{
                seed = Long.parseLong(seedString);              
            }catch(Exception e){}
        }
        LOGGER.info("Current Seed is "+seed);
       
        //define generator
        String typeGenerator = mapArgs.get("-t");
        if(typeGenerator == null || typeGenerator.isEmpty() || typeGenerator.equalsIgnoreCase("R")){
            mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);         
        }else if(typeGenerator.equalsIgnoreCase("RD")){
            mGrphGenerator = new GraphGenerationRandomly2(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
        }else if(typeGenerator.equalsIgnoreCase("D")){
            mGrphGenerator = new GraphGenerationSimpleApproach(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
        }else if(typeGenerator.equalsIgnoreCase("DD")){
            mGrphGenerator = new GraphGenerationSimpleApproach2(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
        }else if(typeGenerator.equalsIgnoreCase("C")){
            mGrphGenerator = new GraphGenerationClusteringBased(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);          
        }else if(typeGenerator.equalsIgnoreCase("CD")){
            mGrphGenerator = new GraphGenerationClusteringBased2(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);
        } else{
            mGrphGenerator = new GraphGenerationRandomly(mNumberOfDesiredVertices, graphs, iNumberOfThreads, seed);         
        }

        double startTime = System.currentTimeMillis();
        String loadMimicGraph = mapArgs.get("-l");
        boolean isLoaded = false;
        //if the file path exists, it will read from it otherwise, it will write on it
        if (loadMimicGraph != null) {
            LOGGER.info("Loading previously determined Mimic Graph from file.");
            ColouredGraph colouredGraph = mDatasetManager.readIntResults(loadMimicGraph);
            if (colouredGraph != null) {
                mGrphGenerator.setMimicGraph(colouredGraph);
                isLoaded = true;
            } 
        } 
        
        // in case the mimic graph is not loaded, regenerate it anyways
        if (isLoaded == false) {
            LOGGER.info("Generating a first version of mimic graph ...");
            // create a draft graph
            mGrphGenerator.generateGraph();
            // estimate the costed time for generation
            double duration = System.currentTimeMillis() - startTime;
            LOGGER.info("Finished graph generation process in " + duration + " ms");
            if(loadMimicGraph == null) {
                loadMimicGraph = "Initialized_MimicGraph.ser";
            }
            mDatasetManager.persistIntResults(mGrphGenerator.getMimicGraph(), loadMimicGraph);
            LOGGER.info("Intermediate results saved under: "+loadMimicGraph);

        }
        
        /*---------------------------------------------------
        Optimization with constant expressions
        ----------------------------------------------------*/
        long secSeed = mGrphGenerator.getSeed()+1;
        GraphOptimization grphOptimizer = new GraphOptimization(graphs, mGrphGenerator, metrics, valuesCarrier, secSeed);
        LOGGER.info("Optimizing the mimic graph ...");

     // Initialize Error Score Calculator
        ErrorScoreCalculator mErrScoreCalculator = new ErrorScoreCalculator(graphs, valuesCarrier);
        
		// Initialize Expression Checker
		ExpressionChecker expressionChecker = new ExpressionChecker(mErrScoreCalculator.getmMapOfMeanValues(), valuesCarrier.getMapConstantValues());
		expressionChecker.setManualExpressionsSet(setOfExpressions);
		expressionChecker.setmMapOfMeanValues(mapMetricValues);

		expressionChecker.storeExpressions(mapMetricValues);

		expressionChecker.checkExpressions();

		System.out.println("Metric Details : ");
		System.out.println("Metric to Increase : " + expressionChecker.getMetricToIncrease()
				+ ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceIncreaseMetric());

		System.out.println("Metric to Decrease : " + expressionChecker.getMetricToDecrease()
				+ ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceDecreaseMetric());

	}

	private ColouredGraph[] getInputGraphs() {

		IDatasetManager mDatasetManager = null;

		ColouredGraph graphs[] = new ColouredGraph[20];

		// load RDF data to coloured graph

		String datasetPath = "";
		if (dataset != null && dataset.equalsIgnoreCase("pg")) {
			LOGGER.info("Loading PersonGraph...");
			mDatasetManager = new PersonGraphDataset();
			datasetPath = PERSON_GRAPH;
		} else if (dataset.equalsIgnoreCase("swdf")) {
			LOGGER.info("Loading SemanticWebDogFood...");
			mDatasetManager = new SemanticWebDogFoodDataset();
			datasetPath = SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH;
		} else if (dataset.equalsIgnoreCase("lgeo")) {
			LOGGER.info("Loading LinkedGeo...");
			mDatasetManager = new LinkedGeoDataset();
			datasetPath = LINKED_GEO_DATASET_FOLDER_PATH;
		} else if (dataset.equalsIgnoreCase("geology")) {
			LOGGER.info("Loading Geology Dataset...");
			mDatasetManager = new GeologyDataset();
			datasetPath = GEOLOGY_DATASET_FOLDER_PATH;
		} else {
			LOGGER.error("Got an unknown dataset name: \"{}\". Aborting", dataset);

		}

		graphs = mDatasetManager.readGraphsFromFiles(datasetPath);
		return graphs;
	}

}
