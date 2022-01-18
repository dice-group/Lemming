package org.aksw.simba.lemming.metrics;

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import org.aksw.simba.lemming.util.IntSetUtil;

/**
 * This class contains helper methods for the usage of {@link Metric}s.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MetricUtils {

    /**
     * For every graph, the values of the metrics are calculated, added to a map and
     * stored in an array. The i-th map of the result array contains the values for
     * the i-th graph.
     * 
     * @param graphs {@link ColouredGraph} for which the values should be
     *               calculated.
     * @return array containing the mappings from metric name to metric value for
     *         the single graphs
     */
    public static ObjectDoubleOpenHashMap<String>[] calculateGraphMetrics(ColouredGraph[] graphs,
            List<SingleValueMetric> metrics) {
        @SuppressWarnings("unchecked")
        ObjectDoubleOpenHashMap<String>[] vectors = new ObjectDoubleOpenHashMap[graphs.length];
        for (int i = 0; i < vectors.length; ++i) {
            vectors[i] = calculateGraphMetrics(graphs[i], metrics);
        }
        return vectors;
    }

    /**
     * The values of the metrics are calculated for the given graph and put into a
     * map.
     * 
     * @param graph {@link ColouredGraph} for which the values should be calculated.
     * @return a mapping from metric name to metric value for the given graph
     */
    public static ObjectDoubleOpenHashMap<String> calculateGraphMetrics(ColouredGraph graph,
            List<SingleValueMetric> metrics) {
        ObjectDoubleOpenHashMap<String> vector = new ObjectDoubleOpenHashMap<>(2 * metrics.size());
        for (SingleValueMetric metric : metrics) {
            vector.put(metric.getName(), metric.apply(graph));
        }
        return vector;
    }

    /**
     * This method is used to calculate the common neighbour-vertices of two given
     * vertices. The returned common neighbour-vertices set should exclude the both
     * given vertices.
     * 
     * @param graph a decorated graph object
     * @param v1    one given vertex
     * @param v2    another given vertex
     * @return a set of common neighbour-vertices of the two given vertices.
     */
    public static IntSet getVerticesInCommon(IColouredGraph graph, int v1, int v2) {
        IntSet v1Neighbours = graph.getInNeighbors(v1);
        v1Neighbours.addAll(graph.getOutNeighbors(v1));

        IntSet v2Neighbours = graph.getInNeighbors(v2);
        v2Neighbours.addAll(graph.getOutNeighbors(v2));

        IntSet intersection = IntSetUtil.intersection(v1Neighbours, v2Neighbours);

        if (intersection.contains(v1)) {
            intersection.remove(v1);
        }

        if (intersection.contains(v2)) {
            intersection.remove(v2);
        }
        return intersection;
    }
}
