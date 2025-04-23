package org.aksw.simba.lemming.tools;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.single.AvgClusteringCoefficientMetric;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
import org.aksw.simba.lemming.metrics.single.EmptyVertices;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MinVertexOutDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.metrics.single.edgetriangles.EdgeTriangleMetric;
import org.aksw.simba.lemming.metrics.single.nodetriangles.NodeTriangleMetric;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import grph.Grph.DIRECTION;

/**
 * This class is meant to compute the metrics of an input graph.
 * 
 */
public class GraphAnalysisTool {

	public static void main(String[] args) {

		// specify the metrics you want to compute
		List<SingleValueMetric> metrics = new ArrayList<>();
		metrics.add(new NumberOfVerticesMetric());
		metrics.add(new NumberOfEdgesMetric());
		metrics.add(new EmptyVertices());
		metrics.add(new AvgVertexDegreeMetric());
		metrics.add(new AvgClusteringCoefficientMetric());
		metrics.add(new DiameterMetric());
		metrics.add(new MinVertexOutDegreeMetric());
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.in));
		metrics.add(new MaxVertexDegreeMetric(DIRECTION.out));
		metrics.add(new StdDevVertexDegree(DIRECTION.in));
		metrics.add(new StdDevVertexDegree(DIRECTION.out));
		metrics.add(new EdgeTriangleMetric());
		metrics.add(new NodeTriangleMetric());

		// read the model and create the internal graph object
		Model model = ModelFactory.createDefaultModel();
		model.read("/home/aams/Desktop/lemming/src/test/resources/graph1.n3");
		GraphCreator creator = new GraphCreator(true);
		ColouredGraph graph = creator.processModel(model);
		
		// compute the metrics
		metrics.parallelStream().forEach(m -> {
			System.out.println(m.getName() + "," + m.apply(graph));
		});
	}
}
