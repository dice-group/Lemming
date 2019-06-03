package org.aksw.simba.lemming.creation;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

import junit.framework.Assert;

public class InfererTest {

	@Test
	public void test() {
		String ttlFileName = "test_literal.ttl";
		String ontFilePath = "dbpedia_test.owl";

		Model personModel = ModelFactory.createDefaultModel();
		personModel.read(ttlFileName, "TTL");

		Inferer inferer = new Inferer();
		OntModel ontModel = inferer.readOntology(ontFilePath, null);
		ontModel.read("22-rdf-syntax-ns", "TURTLE");
		ontModel.read("rdf-schema", "TURTLE");

		Model actualModel = inferer.process(personModel, ontModel);

		Model expModel = ModelFactory.createDefaultModel();
		expModel.read("expected_literal.ttl", "TTL");

		// checks if the two models have the same set of statements
		Assert.assertTrue(actualModel.isIsomorphicWith(expModel));

	}

	@Test
	public void testSwdf() {
		String fileName = "snippet_swdf_2001.rdf";

		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		ontModel.read("src/test/resources/test_ontology", "TTL");

		Model confModel = ModelFactory.createDefaultModel();
		confModel.read(fileName);

		Inferer inferer = new Inferer();
		Model actualModel = inferer.process(confModel, ontModel);

		printModel(actualModel, "after");

		// check if the model contains properties or resources that should have been
		// replaced
		Property foafName = ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/name");
		Property subEventof = ResourceFactory
				.createProperty("http://data.semanticweb.org/ns/swc/ontology#isSubEventOf");
		Assert.assertFalse(actualModel.contains(null, foafName));
		Assert.assertFalse(actualModel.contains(null, subEventof));

		Resource breakEvent = ResourceFactory.createResource("http://data.semanticweb.org/ns/swc/ontology#BreakEvent");
		Assert.assertFalse(actualModel.containsResource(breakEvent));

		Resource replaced = ResourceFactory
				.createResource("https://w3id.org/scholarlydata/ontology/conference-ontology.owl#Break");
		Assert.assertTrue(actualModel.containsResource(replaced));

	}

	// for testing
	private void printModel(Model model, String name) {
		FileWriter out = null;
		try {
			out = new FileWriter(name);
			model.write(out, "TTL");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

}
