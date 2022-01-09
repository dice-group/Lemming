package org.aksw.simba.lemming.metrics.single;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.Metric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGeneration;

import com.carrotsearch.hppc.BitSet;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A metric that generates a single double value.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface SingleValueMetric extends Metric {

    /**
     * Applies the metric to the given graph.
     * 
     * @param graph the graph for which the metric should be calculated.
     * @return the value of the metric.
     */
    double apply(ColouredGraph graph);

    /**
     * Returns metric results that can be reused for further computations.
     * 
     * @param graph - input graph.
     * @return - metric result.
     */
    default UpdatableMetricResult applyUpdatable(ColouredGraph graph) {
        return new SingleValueMetricResult(getName(), apply(graph));
    }

    /**
     * Returns metric results which is computed as per the following inputs.
     *
     * @param graph          - input graph.
     * @param triple         - edge on which graph operation is performed.
     * @param graphOperation - Enum indicating graph operation. ("ADD" for adding an
     *                       edge and "REMOVE" for removing an edge)
     * @param previousResult - UpdatableMetricResult object containing the previous
     *                       computed results.
     * @return - metric result.
     */
    default UpdatableMetricResult update(ColouredGraph graph, TripleBaseSingleID triple,
            Operation graphOperation, UpdatableMetricResult previousResult) {
        return applyUpdatable(graph);
    }
    
    /**
     * The method returns the random triple to remove.
     * 
     * @param graph
     *            - Input Graph
     * @param seed
     *            - Seed Value used to generate random triple.
     * @return
     */
    default TripleBaseSingleID getTripleRemove(ColouredGraph graph, long seed) {
        int edgeId = -1;
        BitSet edgeColour = null;
        Random rand = new Random(seed);
        seed++;
        while (true) {
            IntSet setOfEdges = graph.getEdges();
            int[] arrEdges = setOfEdges.toIntArray();
            // randomly choose edge id to remove
            edgeId = arrEdges[rand.nextInt(arrEdges.length)];
            edgeColour = graph.getEdgeColour(edgeId);
            if (!edgeColour.equals(graph.getRDFTypePropertyColour())) {
                break;
            }
        }

        // track the head and tail of the removed edge
        TripleBaseSingleID triple = new TripleBaseSingleID();
        triple.tailId = graph.getTailOfTheEdge(edgeId);
        triple.headId = graph.getHeadOfTheEdge(edgeId);
        triple.edgeId = edgeId;
        triple.edgeColour = edgeColour;

        return triple;
    }

    /**
     * The method returns the random triple to remove.
     * 
     * Note:- The implementation of this method in particular metric class could use
     * the previous UpdatableMetricResult to generate a different triple.
     * 
     * @param graph
     *            - Input Graph
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param seed
     *            - Seed Value used to generate random triple.
     * @param changeMetricValue
     *            - boolean variable to indicate if the metric value should be decreased
     *            or not. If the variable is true, then the method will return a
     *            triple that reduces the metric value.
     * @return
     */
    default TripleBaseSingleID getTripleRemove(ColouredGraph graph, List<UpdatableMetricResult> previousResult, long seed, boolean changeMetricValue) {
        return getTripleRemove(graph, seed);
    }

    /**
     * The method returns the triple to add by calling getProposedTriple method
     * defined in Graph generator class.
     * @param graph TODO
     * @param mGrphGenerator
     *            - Graph Generator used during execution.
     * @param mProcessRandomly
     *            - boolean value If the variable is false, then it will generate a
     *            triple as per the implementation defined in the Generator class,
     *            else the triple is generated as per the default implementation.
     * 
     * @return
     */
    default TripleBaseSingleID getTripleAdd(ColouredGraph graph, IGraphGeneration mGrphGenerator, boolean mProcessRandomly) {
        return mGrphGenerator.getProposedTriple(mProcessRandomly);
    }

    /**
     * The method returns the triple to add.
     * 
     * Note:- The implementation of this method in particular metric class could use
     * the previous UpdatableMetricResult to generate a different triple.
     * @param graph 
     * @param mGrphGenerator
     *            - Graph Generator used during execution
     * @param mProcessRandomly
     *            - boolean value If the variable is false, then it will generate a
     *            triple as per the implementation defined in the Generator class,
     *            else the triple is generated as per the default implementation.
     * @param previousResult
     *            - UpdatableMetricResult object containing the previous computed
     *            results.
     * @param changeMetricValue
     *            - boolean variable to indicate if the metric value should be decreased
     *            or not. If the variable is true, then the method will return a
     *            triple that reduces the metric value.
     * 
     * @return
     */
    default TripleBaseSingleID getTripleAdd(ColouredGraph graph, IGraphGeneration mGrphGenerator, boolean mProcessRandomly, List<UpdatableMetricResult> previousResult, boolean changeMetricValue) {
        return getTripleAdd(graph, mGrphGenerator, mProcessRandomly);
    }
    
    /**
     * If a metric depends on some other metrics then this method will return those
     * metrics. This is the default implementation which returns an empty List.
     * 
     * @return - List of metrics on which the specific metric depends.
     */
    default List<SingleValueMetric> getDependentMetricsList(){
        return new ArrayList<>();
    }

}
