package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import com.carrotsearch.hppc.BitSet;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueClusteringCoefficientMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author DANISH AHMED on 8/10/2018
 */
public class EdgeModification {
    private ColouredGraph graph;
    private Config metricConfiguration;
    private int numberOfInitialNodes;
    private int numberOfInitialEdges;

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeModification.class);

    EdgeModification(ColouredGraph graph, Config config) {
        this.graph = graph;
        this.metricConfiguration = config;

        this.numberOfInitialNodes = this.graph.getGraph().getNumberOfVertices();
        this.numberOfInitialEdges = this.graph.getGraph().getNumberOfEdges();
    }

    void removeEdgeFromGraph(int edgeId) {
        LOGGER.info(String.format("Initial Graph:\nNumber of nodes:\t%s\nNumber of edges:\t%s\n\n",
                numberOfInitialNodes,
                numberOfInitialEdges));
        getNumberOfNodeTriangles();
        getNumberOfEdgeTriangles();
//        analyseClusteringCoefficient();

        this.graph.removeEdge(edgeId);

        LOGGER.info(String.format("Removed edge id:\t%s", edgeId));
        LOGGER.info(String.format("After Graph Modification:\nNumber of nodes:\t%s\nNumber of edges:\t%s\n",
                this.graph.getGraph().getNumberOfVertices(),
                this.graph.getGraph().getNumberOfEdges()));

//        analyseClusteringCoefficient();
    }

    private void getNumberOfNodeTriangles() {
        List<SingleValueMetric> nodeTriangleMetrics = this.metricConfiguration.getNodeTriMetrics();
        for (SingleValueMetric metric : nodeTriangleMetrics) {
            double numberOfTriangles = metric.apply(graph);
            LOGGER.info(String.format("Metric:\t%s", metric.getName()));
            LOGGER.info(String.format("Number of Node Triangles:\t%s", numberOfTriangles));
        }
    }

    private void getNumberOfEdgeTriangles() {
        List<SingleValueMetric> edgeTriangleMetrics = this.metricConfiguration.getEdgeTriMetrics();
        for (SingleValueMetric metric : edgeTriangleMetrics) {
            double numberOfTriangles = metric.apply(graph);
            LOGGER.info(String.format("Metric:\t%s", metric.getName()));
            LOGGER.info(String.format("Number of Edge Triangles:\t%s", numberOfTriangles));
        }
    }

    void addEdgeToGraph(int tail, int head, BitSet color) {
        LOGGER.info(String.format("Initial Graph:\nNumber of nodes:\t%s\nNumber of edges:\t%s\n\n",
                numberOfInitialNodes,
                numberOfInitialEdges));
        getNumberOfNodeTriangles();
        getNumberOfEdgeTriangles();
//        analyseClusteringCoefficient();

        int edgeId = graph.addEdge(tail, head, color);
        LOGGER.info(String.format("Added edge id:\t%s", edgeId));
        LOGGER.info(String.format("After Graph Modification:\nNumber of nodes:\t%s\nNumber of edges:\t%s\n",
                this.graph.getGraph().getNumberOfVertices(),
                this.graph.getGraph().getNumberOfEdges()));

//        analyseClusteringCoefficient();
    }



    private double calculateAvgCC(List<Double> clusteringCoefficient) {
        double ccSum = 0.0;
        for (double cc : clusteringCoefficient)
            ccSum += cc;
        return (ccSum / this.graph.getGraph().getNumberOfVertices());
    }

//    private void analyseClusteringCoefficient() {
//        /*
//        * Note:
//        * The implementation of clustering coefficient is done simultaneously while computing number of Node Triangles
//        * */
//
//        List<SingleValueClusteringCoefficientMetric> clusteringCoefficientMetrics = this.metricConfiguration.getClusteringCoefficientMetrics();
//        for (SingleValueClusteringCoefficientMetric metric : clusteringCoefficientMetrics) {
//            double numberOfTriangles = metric.apply(this.graph);
//            List<Double> individualVertexCC = metric.getClusteringCoefficient();
//
//            double cc = calculateAvgCC(individualVertexCC);
//            LOGGER.info(String.format("Metric:\t%s", metric.getName()));
//            LOGGER.info(String.format("Number of node triangles while computing clustering coefficient:\t%s", numberOfTriangles));
//            LOGGER.info(String.format("Clustering Coefficient:\t%s\n", cc));
//        }
//    }

}
