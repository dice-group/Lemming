package org.aksw.simba.lemming.metrics.single.nodetriangles;

import java.util.Random;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

/**
 * This class models an algorithm for computing the amount of node triangles in
 * a given (large) graph. This is done using the Duolion algorithm proposed by
 * Tsourakakis et al. (see below) which applies a sparsification process onto
 * the original graph, such that each edge is removed with a certain
 * probability. Once this is done, a standard node triangle counting algorithm
 * is used to compute the amount of triangles on the resulting graph. Finally
 * the amount of triangles computed on the smaller graph is multiplied by a
 * factor based on the edge removal probability.
 *
 * @see <a href=
 *      "https://www.researchgate.net/publication/221654480_DOULION_Counting_triangles_in_massive_graphs_with_a_coin/">https://www.researchgate.net/publication/221654480_DOULION_Counting_triangles_in_massive_graphs_with_a_coin</a>.
 *
 * @author Alexander Hetzer
 *         https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/metrics/single/triangle/DuolionNumberOfTrianglesMetric.java
 *
 */
public class DuolionMetric extends AbstractMetric implements SingleValueMetric {

    private SingleValueMetric triangleCountingAlgorithm;

    private double edgeSurvivalProbability;

    private Random random;

    private ColouredGraph graphCopy;

    public DuolionMetric(SingleValueMetric triangleCountingMetric, double edgeSurvivalProbability, long seed) {
        super("#nodetriangles");
        this.triangleCountingAlgorithm = triangleCountingMetric;
        this.edgeSurvivalProbability = edgeSurvivalProbability;
        random = new Random(seed);
    }

    @Override
    public double apply(IColouredGraph graph) {
        graphCopy = graph.copy();
        for (int edge : graph.getGraph().getEdges()) {
            if (random.nextDouble() > edgeSurvivalProbability) {
                graphCopy.getGraph().removeEdge(edge);
            }
        }

        return triangleCountingAlgorithm.apply(graphCopy) * (1 / Math.pow(edgeSurvivalProbability, 3));
    }

}
