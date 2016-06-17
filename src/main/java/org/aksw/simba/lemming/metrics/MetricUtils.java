package org.aksw.simba.lemming.metrics;

import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * This class contains helper methods for the usage of {@link Metric}s.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MetricUtils {

    /**
     * For every graph, the values of the metrics are calculated, added to a map
     * and stored in an array. The i-th map of the result array contains the
     * values for the i-th graph.
     * 
     * @param graphs
     *            {@link ColouredGraph} for which the values should be
     *            calculated.
     * @return array containing the mappings from metric name to metric value
     *         for the single graphs
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
     * The values of the metrics are calculated for the given graph and put into
     * a map.
     * 
     * @param graph
     *            {@link ColouredGraph} for which the values should be
     *            calculated.
     * @return a mapping from metric name to metric value for the given graph
     */
    public static ObjectDoubleOpenHashMap<String> calculateGraphMetrics(ColouredGraph graph,
            List<SingleValueMetric> metrics) {
        ObjectDoubleOpenHashMap<String> vector = new ObjectDoubleOpenHashMap<String>(2 * metrics.size());
        for (SingleValueMetric metric : metrics) {
            vector.put(metric.getName(), metric.apply(graph));
        }
        return vector;
    }
}
