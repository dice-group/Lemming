package org.aksw.simba.lemming.creation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import com.google.common.collect.Iterators;

import junit.framework.Assert;

public class InfererTest {

	@Test
	public void test() {
		String ttlFileName = "test_literal.ttl";
		String ontFilePath = "dbpedia_test.owl";

		Model personModel = ModelFactory.createDefaultModel();
		personModel.read(ttlFileName, "TTL");

		//TODO: use inferer to build ontModel
		Map<String, String> rdfsMap = new HashMap<>();
		rdfsMap.put("22-rdf-syntax-ns", "TURTLE");
		rdfsMap.put("rdf-schema", "TURTLE");
		Inferer inferer = new Inferer(false, ontFilePath, null, rdfsMap);
		OntModel ontModel = inferer.readOntology(ontFilePath, null);
		ontModel.read("22-rdf-syntax-ns", "TURTLE");
		ontModel.read("rdf-schema", "TURTLE");

		Model actualModel = inferer.process(personModel);

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

		Inferer inferer = new Inferer(false, ontModel);
		Model actualModel = inferer.process(confModel);

		//printModel(actualModel, "after");

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
	
	@Test
	public void testLinkedGeo() {
		OntModel ontModel = ModelFactory.createOntologyModel();
		ontModel.read("geo_ont_test.ttl");
		
		Model geoModel = ModelFactory.createDefaultModel();
		geoModel.read("snippet_linkedgeo.nt");
		
		Inferer inferer = new Inferer(false, ontModel);
		Model actualModel = inferer.process(geoModel);
		
		//prior to inference, 0 resources have a type stmt
		int count = Iterators.size(geoModel.listResourcesWithProperty(RDF.type));
		Assert.assertTrue(count==0);
		
		int afterCount = Iterators.size(actualModel.listResourcesWithProperty(RDF.type));
		Assert.assertTrue(afterCount==4);
		
		Model containerModel = ModelFactory.createDefaultModel();
		containerModel.read("container_graph.ttl");
		int size = Iterators.size(containerModel.listResourcesWithProperty(RDFS.member));
		Assert.assertEquals(size, 0);
		
		containerModel = inferer.process(containerModel);
		int afterSize = Iterators.size(containerModel.listStatements(null, RDFS.member, (RDFNode)null));
		Assert.assertEquals(afterSize, 9);
		
		
		
	}

}
