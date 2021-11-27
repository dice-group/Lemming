package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.Metric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.Operation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

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
    public double apply(ColouredGraph graph);

    /**
     * Returns metric results that can be reused for further computations.
     * 
     * @param graph - input graph.
     * @return - metric result.
     */
    public default UpdatableMetricResult applyUpdatable(ColouredGraph graph) {
        return new SimpleMetricResult(getName(), apply(graph));
    }

    /**
     * Returns metric results which is computed as per the following inputs.
     * 
     * @param triple         - edge on which graph operation is performed.
     * @param metric         - input metric which needs to be computed.
     * @param graph          - input graph.
     * @param graphOperation - Enum indicating graph operation. ("ADD" for adding an
	 *                     edge and "REMOVE" for removing a
     * @param previousResult - UpdatableMetricResult object containing the previous
     *                       computed results.
     * @return - metric result.
     */
    public default UpdatableMetricResult update(ColouredGraph graph, TripleBaseSingleID triple, Operation graphOperation,
                                                UpdatableMetricResult previousResult){
        return applyUpdatable(graph);
    }
}
