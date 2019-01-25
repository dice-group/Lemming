package org.aksw.simba.lemming.creation;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
		OntModel ontModel = inferer.readOntology(ontFilePath);
		ontModel.read("22-rdf-syntax-ns", "TURTLE");
		ontModel.read("rdf-schema", "TURTLE");

		Model actualModel = inferer.process(personModel, ontModel);

		Model expModel = ModelFactory.createDefaultModel();
		expModel.read("expected_literal.ttl", "TTL");

		// checks if the two models have the same set of statements
		Assert.assertTrue(actualModel.isIsomorphicWith(expModel));

	}
}
