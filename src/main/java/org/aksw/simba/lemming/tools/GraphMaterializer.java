package org.aksw.simba.lemming.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.SymmetricProperty;
import org.apache.jena.ontology.TransitiveProperty;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;

/**
 * Adds transient and symmetric property relations to the graph
 *
 */
public class GraphMaterializer {
	
	public GraphMaterializer() {
	}

	/**
	 * Adds a statement to the model:
	 * prop is Symmetric and a prop b , then b prop a
	 * @param symmetricProps
	 * @param graph
	 * @return 
	 */
	public List<Statement> deriveSymmetrics(ArrayList<ObjectProperty> symmetricProps, Model graph) {
		List<Statement> stmts = new ArrayList<Statement>();
		for(ObjectProperty curProperty: symmetricProps) {
			StmtIterator iterator = graph.listStatements(null, curProperty, (RDFNode)null);
			while(iterator.hasNext()) {
				Statement curStmt = iterator.next();
				stmts.add(ResourceFactory.createStatement(curStmt.getObject().asResource(), curProperty, curStmt.getSubject()));
			}
		}
		return stmts;
		
	}
	
	/**
	 * Adds a statement to the model:
	 * a prop b, b prop c
	 * , then a prop c is added
	 * @param transitiveProps
	 * @param graph
	 * @return 
	 */
	public List<Statement> deriveTransitives(ArrayList<ObjectProperty> transitiveProps, Model graph) {
		List<Statement> stmts = new ArrayList<Statement>();
		for(ObjectProperty curProperty: transitiveProps) {
			StringBuilder builder = new StringBuilder("select * where { ?a <");
			builder.append(curProperty.getURI()).append("> ?b . ?b <").append(curProperty.getURI()).append("> ?c . }");
			List<QuerySolution> results = queryModel(builder.toString(), graph);
			for(QuerySolution solution: results) {
				RDFNode a = solution.get("a");
				RDFNode c = solution.get("c");
				if(!a.asResource().getURI().equals(c.asResource().getURI())) {
					stmts.add(ResourceFactory.createStatement(a.asResource(), curProperty, c));
				}		
			}
		}
		return stmts;
	}
		
	/**
	 * Executes a select sparql query over the graph
	 * @param queryStr
	 * @param graph
	 * @return
	 */
	public List<QuerySolution> queryModel (String queryStr, Model graph){
		Query query = QueryFactory.create(queryStr);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, graph);
		List<QuerySolution> querySolutionList = new ArrayList<QuerySolution>();
	    ResultSet resultSet = queryExecution.execSelect();
	    while(resultSet.hasNext()) {
			querySolutionList.add(resultSet.next());
		}
		queryExecution.close();	
		return querySolutionList;
	}
	
	/**
	 * This method identifies the symmetric and transitive properties in the ontology
	 * @param ontology
	 * @return
	 */
	public Map<Resource, ArrayList<ObjectProperty>> identifyProperties(OntModel ontology) {
		Map<Resource, ArrayList<ObjectProperty>> map = new HashMap<Resource, ArrayList<ObjectProperty>>();
		map.put(OWL.SymmetricProperty, new ArrayList<ObjectProperty>());
		map.put(OWL.TransitiveProperty, new ArrayList<ObjectProperty>());
		
		ExtendedIterator<ObjectProperty> objProps = ontology.listObjectProperties();
		while(objProps.hasNext()) {
			ObjectProperty curProp = objProps.next();
			
			//returns null if not
			SymmetricProperty symmetric = ontology.getSymmetricProperty(curProp.getURI());
			if(symmetric!=null) {
				map.get(OWL.SymmetricProperty).add(symmetric);
			}
			
			TransitiveProperty transitive = ontology.getTransitiveProperty(curProp.getURI());
			if(transitive!=null) {
				map.get(OWL.TransitiveProperty).add(transitive);
			}
		}
		return map;
	}

}
