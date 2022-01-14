package org.aksw.simba.lemming.creation.PrecomputingValues;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.aksw.simba.lemming.creation.Inferer;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import com.google.common.collect.Iterators;

import junit.framework.Assert;

public class InfererTest {

	@Test
	public void testSimpleCase() {
		String ttlFileName = "test_literal.ttl";
		String ontFilePath = "dbpedia_test.owl";

		Model personModel = ModelFactory.createDefaultModel();
		personModel.read(ttlFileName, "TTL");

		Map<String, String> rdfsMap = new HashMap<>();
		rdfsMap.put("22-rdf-syntax-ns", "TURTLE");
		rdfsMap.put("rdf-schema", "TURTLE");
		Inferer inferer = new Inferer(false, ontFilePath, null, rdfsMap);

		Model actualModel = inferer.process(personModel);

		Model expModel = ModelFactory.createDefaultModel();
		expModel.read("expected_literal.ttl", "TTL");

		// checks if the two models have the same set of statements
		Assert.assertTrue(actualModel.isIsomorphicWith(expModel));
	}

	@Test
	public void testTransitiveCase() {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		ontModel.read("src/test/resources/test_tran_ontology", "TTL");
		Inferer inferer = new Inferer(false, ontModel);
		//Test case: In ontology file (turtle file), there's Class A equivalent to Class B, but there's no B equivalent to A.
		//result of EquiMap: {A -> {A,B}, B -> {A,B}}
		Map<OntClass, OntClass> classEquiMap = inferer.getClassEquiMap();
		Assert.assertEquals(3, classEquiMap.size());
		Assert.assertEquals(2, new HashSet<>(classEquiMap.values()).size());
		for(OntClass clazz : classEquiMap.keySet()){
			if(clazz.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#C")){
				Assert.assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#C", classEquiMap.get(clazz).getURI());
			}else{
				Assert.assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#B", classEquiMap.get(clazz).getURI());
			}
		}
		//Test case: In ontology file (turtle file), there's Property PA equivalent to Property PB and PB equivalent to Property PC.
		//result of EquiMap: {PA -> {PA,PB,PC}, PB -> {PA,PB,PC}, PC -> {PA,PB,PC}}
		Map<OntProperty, OntProperty>  propertyEquiMap= inferer.getPropertyEquiMap();
		Assert.assertEquals(3, propertyEquiMap.size());
		Assert.assertEquals(1, new HashSet<>(propertyEquiMap.values()).size());
		for(OntProperty property : propertyEquiMap.keySet()){
			Assert.assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#PC", propertyEquiMap.get(property).getURI());
		}

		Model model = ModelFactory.createDefaultModel();
		model.read("test_tran_literal.ttl", "TTL");

		Model actualModel = inferer.process(model);

		Model expModel = ModelFactory.createDefaultModel();
		expModel.read("expected_tran_literal.ttl", "TTL");

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

		Map<OntClass, OntClass> classEquiMap = inferer.getClassEquiMap();
		Assert.assertEquals(3, classEquiMap.size());

		int numOfBreak = 0;
		int numOfBreakEvent = 0;
		int numOfAgent = 0;

		for(OntClass ontClass: classEquiMap.keySet()){
			switch (classEquiMap.get(ontClass).getURI()){
				case "https://w3id.org/scholarlydata/ontology/conference-ontology.owl#Break":
					numOfBreak++;
					break;
				case "http://data.semanticweb.org/ns/swc/ontology#BreakEvent":
					numOfBreakEvent++;
					break;
				case "https://w3id.org/scholarlydata/ontology/conference-ontology.owl#Agent":
					numOfAgent++;
					break;
				default:
					break;
			}
		}
		Assert.assertEquals(2,numOfBreak);
		Assert.assertEquals(1,numOfAgent);
		Assert.assertEquals(0,numOfBreakEvent);

		Map<OntProperty, OntProperty>  propertyStringMap = inferer.getPropertyEquiMap();
		Assert.assertEquals(12, propertyStringMap.size());
		Assert.assertEquals(7, new HashSet<>(propertyStringMap.values()).size());

		Model actualModel = inferer.process(confModel);

		//test method renameClasses()
		Resource breakEvent = ResourceFactory.createResource("http://data.semanticweb.org/ns/swc/ontology#BreakEvent");
		Assert.assertFalse(actualModel.containsResource(breakEvent));

		Resource replaced = ResourceFactory
				.createResource("https://w3id.org/scholarlydata/ontology/conference-ontology.owl#Break");
		Assert.assertTrue(actualModel.containsResource(replaced));


		//test iterateStmts()
		Property subEventof = ResourceFactory
				.createProperty("http://data.semanticweb.org/ns/swc/ontology#isSubEventOf");
		Property hasLocation = ResourceFactory
				.createProperty("http://data.semanticweb.org/ns/swc/ontology#hasLocation");
		Property foafName = ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/name");
		Property dtstart = ResourceFactory
				.createProperty("http://www.w3.org/2002/12/cal/icaltzd#dtstart");
		Property superEventOf = ResourceFactory
				.createProperty("http://data.semanticweb.org/ns/swc/ontology#isSuperEventOf");

		Assert.assertFalse(actualModel.contains(null, subEventof));
		Assert.assertFalse(actualModel.contains(null, hasLocation));
		Assert.assertFalse(actualModel.contains(null, foafName));
		Assert.assertFalse(actualModel.contains(null, dtstart));
		Assert.assertFalse(actualModel.contains(null, foafName));
		Assert.assertFalse(actualModel.contains(null, superEventOf));

	   Assert.assertEquals(33, actualModel.size());
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
		Assert.assertEquals(0, count);
		
		int afterCount = Iterators.size(actualModel.listResourcesWithProperty(RDF.type));
		Assert.assertEquals(4, afterCount);
		
		Model containerModel = ModelFactory.createDefaultModel();
		containerModel.read("container_graph.ttl");
		int size = Iterators.size(containerModel.listResourcesWithProperty(RDFS.member));
		Assert.assertEquals(size, 0);
		
		actualModel = inferer.process(containerModel);
		int afterSize = Iterators.size(actualModel.listStatements(null, RDFS.member, (RDFNode)null));
		Assert.assertEquals(afterSize, 9);

		Property p1883 = ResourceFactory
				.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1883");
		Assert.assertFalse(actualModel.contains(null, p1883));
		Property p190 = ResourceFactory
				.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#_190");
		Assert.assertFalse(actualModel.contains(null, p190));

		Assert.assertEquals(containerModel.size() + 1, actualModel.size());
	}
}
