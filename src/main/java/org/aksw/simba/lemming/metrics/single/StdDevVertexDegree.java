package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;


import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class StdDevVertexDegree extends AvgVertexDegreeMetric {

    protected DIRECTION direction;

    public StdDevVertexDegree(DIRECTION direction) {
        super(direction == DIRECTION.in ? "stdDevInDegree" : "stdDevOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {
        IntArrayList degrees = null;
        if (direction == DIRECTION.in) {
            degrees = graph.getGraph().getAllInEdgeDegrees();
        } else {
            degrees = graph.getGraph().getAllOutEdgeDegrees();
        }
        return calculateStdDev(degrees, calculateAvg(degrees));
    }

    protected double calculateStdDev(int[] degrees, double avg) {
        double temp, sum = 0;
        for (int i = 0; i < degrees.length; ++i) {
            temp = avg - degrees[i];
            temp *= temp;
            sum += temp;
        }
        return Math.sqrt(sum / degrees.length);
    }
    
    /**
     * The method checks if we need to compute in degree or out-degree and then calls the metricComputationMaxDegree with correct parameters.
     * @param triple - edge on which graph operation is performed.
     * @param metric - input metric which needs to be computed.
     * @param graph - input graph.
     * @param graphOperation - boolean value indicating graph operation. ("true" for adding an edge and "false" for removing an edge)
     * @param previousResult - UpdatableMetricResult object containing the previous computed results.
     * @return
     */
    @Override
    public UpdatableMetricResult update(TripleBaseSingleID triple, SingleValueMetric metric, ColouredGraph graph,
            boolean graphOperation, UpdatableMetricResult previousResult, VertexDegrees mVertexDegrees) {
    	UpdatableMetricResult newMetricResult;
    	
		switch (metric.getName()) {
			case "stdDevInDegree":	

				newMetricResult = calculateStdDev(mVertexDegrees.getmMapVerticesinDegree(), calculateAvg(mVertexDegrees.getmMapVerticesinDegree()));
				break;

			case "stdDevOutDegree":
				newMetricResult = calculateStdDev(mVertexDegrees.getmMapVerticesinDegree(), calculateAvg(mVertexDegrees.getmMapVerticesinDegree()));
				break;
			
			default:// If metric is other than maxInDegree and maxOutDegree then apply the metric
				newMetricResult = applyUpdatable(graph, graphOperation, triple, previousResult);
		}
		
        return newMetricResult;
    }
}
