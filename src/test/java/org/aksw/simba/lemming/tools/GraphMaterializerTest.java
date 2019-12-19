package org.aksw.simba.lemming.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.junit.Assert;
import org.junit.Test;

public class GraphMaterializerTest {
	
	@Test
	public void test() {
		Model graph = ModelFactory.createDefaultModel();
		graph.read("materialization_graph.n3");
		
		OntModel ontology = ModelFactory.createOntologyModel();
		ontology.read("skos_snippet.ttl");
		
		GraphMaterializer materializer = new GraphMaterializer();
		Map<Resource, ArrayList<OntProperty>> propertyMap = materializer.identifyProperties(ontology, ontology.listAllOntProperties().toSet());
		while(true){
			long size = graph.size();
			List<Statement> symmetricStmts = materializer.deriveSymmetrics(propertyMap.get(OWL.SymmetricProperty), graph);
			List<Statement> transitiveStmts = materializer.deriveTransitives(propertyMap.get(OWL.TransitiveProperty), graph);
			
			graph.add(symmetricStmts);
			graph.add(transitiveStmts);
			
			//if the model didn't grow, break the loop
			if(size==graph.size())
				break;
		}
		
		Model expected = generateExpected();
		Assert.assertTrue(graph.isIsomorphicWith(expected));
		
	}
	
	private Model generateExpected() {
		Model graph = ModelFactory.createDefaultModel();
		graph.read("materialization_graph.n3");
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/b"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#exactMatch"), ResourceFactory.createResource("http://example.org/a")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/c"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#exactMatch"), ResourceFactory.createResource("http://example.org/b")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/e"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#closeMatch"), ResourceFactory.createResource("http://example.org/d")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/g"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#related"), ResourceFactory.createResource("http://example.org/f")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/a"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#exactMatch"), ResourceFactory.createResource("http://example.org/c")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/c"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#exactMatch"), ResourceFactory.createResource("http://example.org/a")));
		return graph;
	}
}
