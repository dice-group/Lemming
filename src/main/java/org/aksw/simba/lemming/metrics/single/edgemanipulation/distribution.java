package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.PoissonDistribution;
import grph.Grph.DIRECTION;

public class distribution extends VertexDegrees{
	
	
	private ColouredGraph graph;
	private SingleValueMetric nodeMetric;
	private SingleValueMetric edgeMetric;
	
	EdgeModification edge = new EdgeModification(graph, nodeMetric, edgeMetric);
	
	public distribution(ColouredGraph graph) {
		super(graph);
		super.computeVerticesDegree(graph);		
	}
	
	public int degree = getVertexDegree(0, DIRECTION.in);
	public double mean = 0.2;
	public double j = 0.1;
	PoissonDistribution pd = new PoissonDistribution();
	public double prob = pd.getProbabilityOf(0, mean);
	
}