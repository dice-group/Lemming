package org.aksw.simba.lemming.creation;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import junit.framework.Assert;

public class InfererTest {

	@Test
	public void test() {
		String ttlFileName = "test.ttl";
		Inferer inferer = new Inferer();

		Model personModel = ModelFactory.createDefaultModel();
		personModel.read(ttlFileName, "TTL");
		
		Map <String, String> map = inferer.mapModel2Ontology();
		
		Model actualModel = inferer.process(personModel, map, ttlFileName, null);
		Model expModel = ModelFactory.createDefaultModel();
		
		expModel.read("expected.ttl", "TTL");
		
		Assert.assertTrue(actualModel.isIsomorphicWith(expModel));

	}
}
