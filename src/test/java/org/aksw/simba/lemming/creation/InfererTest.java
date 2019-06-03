package org.aksw.simba.lemming.creation;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
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
		String ttlFileName = "snippet_swdf_2001.ttl";
//		String ttlFileName = "iswc-2001-complete.rdf";

		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		ontModel.read("src/test/resources/test_ontology", "TTL");
//		ontModel.read("22-rdf-syntax-ns", "TTL");
//    	ontModel.read("rdf-schema", "TTL");
//    	ontModel.read("swdf-owls/foaf", "TTL");
//    	ontModel.read("swdf-owls/elements-1.1", "TTL");
//    	ontModel.read("swdf-owls/ical", "TTL");
//    	ontModel.read("swdf-owls/owl", "TTL");
//    	ontModel.read("swdf-owls/conference-ontology", "TTL" );
//    	ontModel.read("swdf-owls/swrc", "TTL");
//    	ontModel.read("swdf-owls/conference-ontology-alignments", "TTL");

		Model confModel = ModelFactory.createDefaultModel();
		confModel.read(ttlFileName);

		Inferer inferer = new Inferer();
		Model actualModel = inferer.process(confModel, ontModel);

		printModel(actualModel, "after");

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
