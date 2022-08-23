
package org.aksw.simba.lemming.tools;

import grph.Grph;
import grph.Grph.DIRECTION;
import grph.algo.topology.ClassicalGraphs;
import grph.algo.topology.StarTopologyGenerator;
import grph.in_memory.InMemoryGrph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.refinement.CharacteristicExpressionSearcher;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.aksw.simba.lemming.algo.refinement.fitness.FitnessFunction;
import org.aksw.simba.lemming.algo.refinement.fitness.LengthAwareMinSquaredError;
import org.aksw.simba.lemming.algo.refinement.fitness.ReferenceGraphBasedFitnessDecorator;
import org.aksw.simba.lemming.algo.refinement.operator.LeaveNodeReplacingRefinementOperator;
import org.aksw.simba.lemming.algo.refinement.redberry.RedberryBasedFactory;
import org.aksw.simba.lemming.creation.GeologyDataset;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.LinkedGeoDataset;
import org.aksw.simba.lemming.creation.PersonGraphDataset;
import org.aksw.simba.lemming.creation.SemanticWebDogFoodDataset;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class PrecomputingValues {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrecomputingValues.class);

    private static final double MIN_FITNESS = 100000.0;
    private static final int MAX_ITERATIONS = 50;
    private static boolean USE_SEMANTIC_DOG_FOOD = false;
    private static boolean USE_PERSON_GRAPH = true;
    private static boolean USE_LINKED_GEO = false;
    private static boolean USE_GEOLOGY = false;
    private static boolean RECALCULATE_METRICS = true;
    private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
    private static final String PERSON_GRAPH = "PersonGraph/";
    private static final String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";
    private static final String GEOLOGY_DATASET_FOLDER_PATH = "GeologyGraphs/";

    public static void main(String[] args) {
        LOGGER.info("Start precomputing metric and constant expressions!");
        // MultiThreadProcessing.defaultNumberOfThreads = 1;

        // For this test, we do not need assertions
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);
        String dataset = args[0];
        if (dataset.equalsIgnoreCase("pg")) {
            USE_SEMANTIC_DOG_FOOD = false;
            USE_PERSON_GRAPH = true;
            USE_LINKED_GEO = false;
            USE_GEOLOGY = false;
        } else if (dataset.equalsIgnoreCase("swdf")){
            USE_SEMANTIC_DOG_FOOD = true;
            USE_PERSON_GRAPH = false;
            USE_LINKED_GEO = false;
            USE_GEOLOGY = false;
        } else if (dataset.equalsIgnoreCase("lgeo")){
        	USE_SEMANTIC_DOG_FOOD = false;
            USE_PERSON_GRAPH = false;
            USE_LINKED_GEO = true;
            USE_GEOLOGY = false;
        } else if(dataset.equalsIgnoreCase("geology")) {
        	USE_SEMANTIC_DOG_FOOD = false;
            USE_PERSON_GRAPH = false;
            USE_LINKED_GEO = false;
            USE_GEOLOGY = true;
        } else {
        	LOGGER.error("Got an unknown dataset name: \"{}\". Aborting", dataset);
        	return;
        }

        List<SingleValueMetric> metrics = new ArrayList<>();
        metrics.add(new NodeTriangleMetric());
        metrics.add(new EdgeTriangleMetric());
        metrics.add(new AvgVertexDegreeMetric());
        metrics.add(new StdDevVertexDegree(DIRECTION.in));
        metrics.add(new StdDevVertexDegree(DIRECTION.out));
        metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
        metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
        metrics.add(new NumberOfEdgesMetric());
        metrics.add(new NumberOfVerticesMetric());
        metrics.add(new DiameterMetric());

        ColouredGraph graphs[] = new ColouredGraph[20];
        IDatasetManager mDatasetManager = null;
        String datasetPath = "";
        if (USE_SEMANTIC_DOG_FOOD) {
            datasetPath = SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH;
            mDatasetManager = new SemanticWebDogFoodDataset();
        } else if (USE_PERSON_GRAPH) {
            datasetPath = PERSON_GRAPH;
            mDatasetManager = new PersonGraphDataset();
        } else if (USE_LINKED_GEO) {
        	datasetPath = LINKED_GEO_DATASET_FOLDER_PATH;
            mDatasetManager = new LinkedGeoDataset();
        } else if(USE_GEOLOGY) {
        	datasetPath = GEOLOGY_DATASET_FOLDER_PATH;
        	mDatasetManager = new GeologyDataset();
        }
        graphs = mDatasetManager.readGraphsFromFiles(datasetPath);

        // compute metrics for each graph here
        ConstantValueStorage valueCarrier = new ConstantValueStorage(datasetPath);
        boolean havingData = valueCarrier.havingData();
        LOGGER.info("Compute metric values for graph ......");
        Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues = null;
        if (havingData && !RECALCULATE_METRICS) {
            mapMetricValues = new HashMap<String, ObjectDoubleOpenHashMap<String>>();
            for (ColouredGraph grph : graphs) {
                mapMetricValues.put(ConstantValueStorage.generateGraphKey(grph), valueCarrier.getMetricValues(grph, metrics));
            }
        } else {
            mapMetricValues = getMapMetricValues(graphs, metrics);
            if (mapMetricValues == null) {
                valueCarrier.setMetricValues(mapMetricValues);
            } else {
                valueCarrier.addMetricValues(mapMetricValues, false);
            }
        }
        ObjectDoubleOpenHashMap<String> graphVectors[] = valueCarrier.getGraphMetricsVector(graphs, metrics);

        LOGGER.info("Compute constant expressions ......");
        // FitnessFunction fitnessFunc = new MinSquaredError();
        FitnessFunction fitnessFunc = new LengthAwareMinSquaredError();
        // fitnessFunc = new DivisionCheckingFitnessDecorator();
        fitnessFunc = new ReferenceGraphBasedFitnessDecorator(fitnessFunc,
                createReferenceGraphVectors(graphs, metrics));

        CharacteristicExpressionSearcher searcher = new CharacteristicExpressionSearcher(metrics,
                new LeaveNodeReplacingRefinementOperator(metrics), new RedberryBasedFactory(), fitnessFunc, MIN_FITNESS,
                MAX_ITERATIONS);
        searcher.setDebug(true);

        // SortedSet<RefinementNode> bestNodes = searcher.findExpression(graphs, 5);
        SortedSet<RefinementNode> bestNodes = searcher.findExpression(graphVectors, 5);

        for (RefinementNode n : bestNodes) {
            System.out.print(n.getFitness());
            System.out.print(" --> ");
            System.out.println(n.toString());

            for (int i = 0; i < graphs.length; ++i) {
                ObjectDoubleOpenHashMap<String> metricValues = valueCarrier.getMetricValues(graphs[i], metrics);
                double constValue = n.getExpression().getValue(metricValues);
                System.out.print(constValue);
                System.out.print('\t');
                valueCarrier.addConstantValue(n.getExpression(), graphs[i], constValue);
            }
            System.out.println();
        }

        // save to file
        valueCarrier.storeData();
        LOGGER.info("Precomputation is DONE");
    }

    /**
     * create reference graph to compute constant expressions
     * 
     * @param graphs
     *            input dataset graphs
     * @param metrics
     *            list of exploited metrics
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
        for (SingleValueMetric metric : metrics) {
            if (metric.getName().equalsIgnoreCase("#edgetriangles")
                    || metric.getName().equalsIgnoreCase("#nodetriangles")
                    || metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
                costlyMetrics.add(metric);
                LOGGER.info("Costly metric: " + metric.getName());
            } else {
                naiveMetrics.add(metric);
                LOGGER.info("Naive metric: " + metric.getName());
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
            ObjectDoubleOpenHashMap<String> starGraphMetrics = MetricUtils.calculateGraphMetrics(startColouredGraph,
                    naiveMetrics);
            for (SingleValueMetric metric : costlyMetrics) {
                if (metric.getName().equalsIgnoreCase("#edgetriangles")
                        || metric.getName().equalsIgnoreCase("#nodetriangles")
                        || metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
                    starGraphMetrics.putOrAdd(metric.getName(), 0, 0);
                } else {
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
            ObjectDoubleOpenHashMap<String> gridGraphMetrics = MetricUtils.calculateGraphMetrics(gridColouredGraph,
                    naiveMetrics);

            for (SingleValueMetric metric : costlyMetrics) {
                if (metric.getName().equalsIgnoreCase("#edgetriangles")
                        || metric.getName().equalsIgnoreCase("#nodetriangles")
                        || metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
                    gridGraphMetrics.putOrAdd(metric.getName(), 0, 0);
                } else {
                    double val = metric.apply(gridColouredGraph);
                    gridGraphMetrics.putOrAdd(metric.getName(), val, val);
                }
            }

            vectors.add(gridGraphMetrics);

            /*------------------
             *  Ring
             ------------------*/
            ColouredGraph ringColouredGraph = new ColouredGraph(ClassicalGraphs.cycle(numberOfNodes), null, null);
            ObjectDoubleOpenHashMap<String> ringGraphMetrics = MetricUtils.calculateGraphMetrics(ringColouredGraph,
                    naiveMetrics);

            for (SingleValueMetric metric : costlyMetrics) {
                if (metric.getName().equalsIgnoreCase("#edgetriangles")
                        || metric.getName().equalsIgnoreCase("#nodetriangles")
                        || metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
                    if (numberOfNodes == 3) {
                        ringGraphMetrics.putOrAdd(metric.getName(), 1, 1);
                    } else {
                        ringGraphMetrics.putOrAdd(metric.getName(), 0, 0);
                    }
                } else {
                    double val = metric.apply(gridColouredGraph);
                    ringGraphMetrics.putOrAdd(metric.getName(), val, val);
                }
            }

            vectors.add(ringGraphMetrics);

            /*------------------
             *  Clique
             ------------------*/
            ColouredGraph cliqueColouredGraph = new ColouredGraph(ClassicalGraphs.completeGraph(partSize), null, null);
            ObjectDoubleOpenHashMap<String> cliqueGraphMetrics = MetricUtils.calculateGraphMetrics(cliqueColouredGraph,
                    naiveMetrics);

            for (SingleValueMetric metric : costlyMetrics) {
                if (metric.getName().equalsIgnoreCase("#edgetriangles")
                        || metric.getName().equalsIgnoreCase("#nodetriangles")
                        || metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {

                    if (partSize < 3) {
                        cliqueGraphMetrics.putOrAdd(metric.getName(), 0, 0);
                    } else {
                        int noOfValues = partSize * (partSize - 1) * (partSize - 2) / 6;
                        cliqueGraphMetrics.putOrAdd(metric.getName(), noOfValues, noOfValues);
                    }
                } else {
                    double val = metric.apply(gridColouredGraph);
                    cliqueGraphMetrics.putOrAdd(metric.getName(), val, val);
                }
            }

            vectors.add(cliqueGraphMetrics);

            /*------------------
             *  Bipartite
             ------------------*/
            // partSize = numberOfNodes / 2;
            partSize = numberOfNodes > 100000? numberOfNodes / 128 : numberOfNodes / 8;
            
            ColouredGraph bipartiteColouredGraph = new ColouredGraph(
                    ClassicalGraphs.completeBipartiteGraph(partSize, partSize), null, null);
            ObjectDoubleOpenHashMap<String> bipartiteGraphMetrics = MetricUtils
                    .calculateGraphMetrics(bipartiteColouredGraph, naiveMetrics);

            for (SingleValueMetric metric : costlyMetrics) {
                if (metric.getName().equalsIgnoreCase("#edgetriangles")
                        || metric.getName().equalsIgnoreCase("#nodetriangles")
                        || metric.getName().equalsIgnoreCase("avgClusterCoefficient")) {
                    bipartiteGraphMetrics.putOrAdd(metric.getName(), 0, 0);
                } else {
                    double val = metric.apply(gridColouredGraph);
                    bipartiteGraphMetrics.putOrAdd(metric.getName(), val, val);
                }
            }
            vectors.add(bipartiteGraphMetrics);
        }
        return vectors.toArray(new ObjectDoubleOpenHashMap[vectors.size()]);
    }

    /**
     * get value of each metric applied on each graph
     * 
     * @param origGrphs
     * @param lstMetrics
     * @return
     */
    private static Map<String, ObjectDoubleOpenHashMap<String>> getMapMetricValues(ColouredGraph origGrphs[],
            List<SingleValueMetric> lstMetrics) {
        Map<String, ObjectDoubleOpenHashMap<String>> mapMetricValues = new HashMap<String, ObjectDoubleOpenHashMap<String>>();

        for (ColouredGraph grph : origGrphs) {
            String key = ConstantValueStorage.generateGraphKey(grph);
            LOGGER.info("Consider graph: " + key);
            ObjectDoubleOpenHashMap<String> metricValues = MetricUtils.calculateGraphMetrics(grph, lstMetrics);
            mapMetricValues.put(key, metricValues);
        }
        return mapMetricValues;
    }

}
