package org.aksw.simba.lemming.creation;

import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.Assert;
import org.junit.Test;

public class GraphMaterializerTest {
	
	@Test
	public void test() {
		Model graph = ModelFactory.createDefaultModel();
		graph.read("materialization_graph.n3");
		
		OntModel ontology = ModelFactory.createOntologyModel();
		ontology.read("skos_snippet.ttl");
		
		GraphMaterializer materializer = new GraphMaterializer(ontology.listAllOntProperties().toSet());
		while(true){
			long size = graph.size();
			List<Statement> symmetricStmts = materializer.deriveSymmetricStatements(graph);
			List<Statement> transitiveStmts = materializer.deriveTransitiveStatements(graph);
			List<Statement> inverseStmts = materializer.deriveInverseStatements(graph);
			
			graph.add(symmetricStmts);
			graph.add(transitiveStmts);
			graph.add(inverseStmts);
			
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
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/t"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#narrower"), ResourceFactory.createResource("http://example.org/a")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/o"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#topConceptOf"), ResourceFactory.createResource("http://example.org/p")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/y"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#narrower"), ResourceFactory.createResource("http://example.org/w")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/w"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#broader"), ResourceFactory.createResource("http://example.org/y")));
		graph.add(ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/a"), ResourceFactory.createProperty("http://example.org/test#broaderTest"), ResourceFactory.createResource("http://example.org/t")));
		
		return graph;
	}
}
