package org.aksw.simba.lemming.metrics.dummytest;

import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.SimpleMetricResult;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.VertexDegrees;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import junit.framework.Assert;

public class AvgVertexDegreeTest {

	private static final String GRAPH_FILE = "email-Eu-core.n3"; // graph1.n3, email-Eu-core.n3, dummygraph.n3

	// @Test
	@SuppressWarnings("deprecation")
	public void metricCalculate() {
		Model model = ModelFactory.createDefaultModel();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
		model.read(is, null, "N3");
		IOUtils.closeQuietly(is);

		GraphCreator creator = new GraphCreator();
		ColouredGraph graph = creator.processModel(model);

		VertexDegrees vertexDegreesObj = new VertexDegrees(graph);

		// Old method
		SingleValueMetric metric = new AvgVertexDegreeMetric();
		double metricValueExpected = metric.apply(graph);

		UpdatableMetricResult update = metric.update(new TripleBaseSingleID(), graph, false,
				new SimpleMetricResult(metric.getName(), 0.0), vertexDegreesObj);
		double metricValueActual = update.getResult();

		Assert.assertTrue("Metric values for Avg Vertex Degree do not match!",
				metricValueExpected == metricValueActual);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void metricCalculateGraphOperation() {
		Model model = ModelFactory.createDefaultModel();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
		model.read(is, null, "N3");
		IOUtils.closeQuietly(is);

		GraphCreator creator = new GraphCreator();
		ColouredGraph graph = creator.processModel(model);

		SingleValueMetric metric = new AvgVertexDegreeMetric();

		double apply = metric.apply(graph);

		System.out.println("Original Max Vertex degrees. ");
		System.out.println("Avg Vertex Degree : " + apply);

		TripleBaseSingleID triple = new TripleBaseSingleID();
		VertexDegrees vertexDegreesObj = new VertexDegrees(graph);
		// int edgeId = 1;// for edge id 0, the degrees are not impacted

		// Using the old Logic
		System.out.println("Old Logic : ");
		triple = removeAnEdge(graph, triple, 300, vertexDegreesObj);
		double oldLogic = oldLogic(graph, triple, metric);
		UpdatableMetricResult newLogic = newLogic(graph, triple, metric, vertexDegreesObj, false);
		// metric.applyUpdatable(graph, false, triple, newLogic);
		Assert.assertTrue("AVg vertex degree mismatch! (Old vs new Logic)", oldLogic == newLogic.getResult());
		triple = addAnEdge(graph, triple, 300, vertexDegreesObj);

		triple = removeAnEdge(graph, triple, 0, vertexDegreesObj);
		oldLogic = oldLogic(graph, triple, metric);
		newLogic = newLogic(graph, triple, metric, vertexDegreesObj, false);
		newLogic = metric.applyUpdatable(graph, false, triple, newLogic);
		Assert.assertTrue("AVg vertex degree mismatch! (Old vs new Logic)", oldLogic == newLogic.getResult());
		// triple = addAnEdge(graph, triple, 0, vertexDegreesObj);

		triple = removeAnEdge(graph, triple, 10, vertexDegreesObj);
		oldLogic = oldLogic(graph, triple, metric);
		newLogic = newLogic(graph, triple, metric, vertexDegreesObj, false);
		newLogic = metric.applyUpdatable(graph, false, triple, newLogic);
		Assert.assertTrue("AVg vertex degree mismatch! (Old vs new Logic)", oldLogic == newLogic.getResult());
		// triple = addAnEdge(graph, triple, 0, vertexDegreesObj);

		triple = removeAnEdge(graph, triple, 100, vertexDegreesObj);
		oldLogic = oldLogic(graph, triple, metric);
		newLogic = newLogic(graph, triple, metric, vertexDegreesObj, false);
		newLogic = metric.applyUpdatable(graph, false, triple, newLogic);
		Assert.assertTrue("AVg vertex degree mismatch! (Old vs new Logic)", oldLogic == newLogic.getResult());

		triple = removeAnEdge(graph, triple, 200, vertexDegreesObj);
		oldLogic = oldLogic(graph, triple, metric);
		newLogic = newLogic(graph, triple, metric, vertexDegreesObj, false);
		newLogic = metric.applyUpdatable(graph, false, triple, newLogic);
		Assert.assertTrue("AVg vertex degree mismatch! (Old vs new Logic)", oldLogic == newLogic.getResult());

		triple = addAnEdge(graph, triple, 200, vertexDegreesObj);
		oldLogic = oldLogic(graph, triple, metric);
		newLogic = newLogic(graph, triple, metric, vertexDegreesObj, true);
		newLogic = metric.applyUpdatable(graph, true, triple, newLogic);
		Assert.assertTrue("AVg vertex degree mismatch! (Old vs new Logic)", oldLogic == newLogic.getResult());

	}

	private TripleBaseSingleID removeAnEdge(ColouredGraph graph, TripleBaseSingleID triple, int edgeId,
			VertexDegrees vertexDegreesObj) {
		triple.tailId = graph.getTailOfTheEdge(edgeId);
		triple.headId = graph.getHeadOfTheEdge(edgeId);
		triple.edgeId = edgeId;
		triple.edgeColour = graph.getEdgeColour(edgeId);

		graph.removeEdge(edgeId);

		vertexDegreesObj.updateVertexIndegree(triple.headId, -1);
		vertexDegreesObj.updateVertexOutdegree(triple.tailId, -1);

		return triple;
	}

	private TripleBaseSingleID addAnEdge(ColouredGraph graph, TripleBaseSingleID triple, int edgeId,
			VertexDegrees vertexDegreesObj) {
		graph.addEdge(triple.tailId, triple.headId);

		vertexDegreesObj.updateVertexIndegree(triple.headId, 1);
		vertexDegreesObj.updateVertexOutdegree(triple.tailId, 1);

		return triple;
	}

	private double oldLogic(ColouredGraph graph, TripleBaseSingleID triple, SingleValueMetric metric) {

		double apply = metric.apply(graph);
		// Assert.assertTrue("Avg Vertex degree mismatch, Old Logic.", expectedDegree ==
		// apply);
		System.out.println("Avg Vertex Degree (Old Logic) : " + apply);

		// System.out.println("Max Vertex Out Degree : " + apply2);

		return apply;
	}

	private UpdatableMetricResult newLogic(ColouredGraph graph, TripleBaseSingleID triple, SingleValueMetric metric,
			VertexDegrees vertexDegreesObj, boolean graphOperation) {

		SimpleMetricResult update = (SimpleMetricResult) metric.update(triple, graph, graphOperation, null,
				vertexDegreesObj);
		double metVal = update.getResult();
		System.out.println("Avg Vertex degree (New Logic) : " + metVal);
		// Assert.assertTrue("Avg Vertex degree mismatch, New Logic.", expectedDegree ==
		// metVal);
		update.setResult(metVal);

		return update;
	}

}
