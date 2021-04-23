package org.aksw.simba.lemming.creation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * The class identifies transient, symmetric and inverseOf property relations in
 * the graph
 *
 */
public class GraphMaterializer {

	private Set<OntProperty> symmetricProperties;
	private Set<OntProperty> transitiveProperties;
	private SetMultimap<OntProperty, OntProperty> inverseProperties;

	public GraphMaterializer(Set<OntProperty> ontProperties) {
		this.symmetricProperties = new HashSet<OntProperty>();
		this.transitiveProperties = new HashSet<OntProperty>();
		this.inverseProperties = HashMultimap.create();
		identifyProperties(ontProperties);
	}

	/**
	 * Generates new statements based on the identified symmetric properties. If
	 * prop is Symmetric and (a prop b .) , then (b prop a .)
	 * 
	 * @param symmetricProps
	 * @param graph
	 * @return
	 */
	public List<Statement> deriveSymmetricStatements(Model graph) {
		List<Statement> stmts = new ArrayList<Statement>();
		for (OntProperty curProperty : symmetricProperties) {
			StmtIterator iterator = graph.listStatements(null, curProperty, (RDFNode) null);
			while (iterator.hasNext()) {
				Statement curStmt = iterator.next();
				stmts.add(ResourceFactory.createStatement(curStmt.getObject().asResource(), curProperty,
						curStmt.getSubject()));
			}
		}
		return stmts;

	}

	/**
	 * Generates new statements based on the identified transient properties. a prop
	 * b, b prop c , then a prop c is added
	 * 
	 * @param transitiveProps
	 * @param graph
	 * @return
	 */
	public List<Statement> deriveTransitiveStatements(Model graph) {
		List<Statement> stmts = new ArrayList<Statement>();
		for (OntProperty curProperty : transitiveProperties) {
			StringBuilder builder = new StringBuilder("select * where { ?a <");
			builder.append(curProperty.getURI()).append("> ?b . ?b <").append(curProperty.getURI()).append("> ?c . }");
			List<QuerySolution> results = queryModel(builder.toString(), graph);
			for (QuerySolution solution : results) {
				RDFNode a = solution.get("a");
				RDFNode c = solution.get("c");
				if (!a.asResource().getURI().equals(c.asResource().getURI())) {
					stmts.add(ResourceFactory.createStatement(a.asResource(), curProperty, c));
				}
			}
		}
		return stmts;
	}

	/**
	 * Generates new statements based on the identified inverse properties. p1
	 * inverseOf p2, a p1 b, then b p2 a
	 * 
	 * @param map
	 * @param graph
	 * @return
	 */
	public List<Statement> deriveInverseStatements(Model graph) {
		List<Statement> stmts = new ArrayList<Statement>();
		inverseProperties.keySet().forEach(key -> {
			Set<OntProperty> inverses = inverseProperties.get(key);
			StmtIterator iter = graph.listStatements(null, key, (RDFNode) null);
			while (iter.hasNext()) {
				Statement curStmt = iter.next();
				for (OntProperty inverse : inverses) {
					if (curStmt.getObject().isResource())
						stmts.add(ResourceFactory.createStatement(curStmt.getObject().asResource(), inverse,
								curStmt.getSubject()));
				}
			}
		});
		return stmts;
	}

	/**
	 * Executes a select sparql query over the graph
	 * 
	 * @param queryStr
	 * @param graph
	 * @return
	 */
	public List<QuerySolution> queryModel(String queryStr, Model graph) {
		Query query = QueryFactory.create(queryStr);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, graph);
		List<QuerySolution> querySolutionList = new ArrayList<QuerySolution>();
		ResultSet resultSet = queryExecution.execSelect();
		while (resultSet.hasNext()) {
			querySolutionList.add(resultSet.next());
		}
		queryExecution.close();
		return querySolutionList;
	}

	/**
	 * This method identifies the symmetric, transitive and inverse properties
	 * present in the ontology
	 * 
	 * @param ontProperties
	 * @return
	 */
	public void identifyProperties(Set<OntProperty> ontProperties) {
		for (OntProperty curProp : ontProperties) {
			if (curProp.isSymmetricProperty()) {
				this.symmetricProperties.add(curProp.asSymmetricProperty());
			}

			if (curProp.isTransitiveProperty()) {
				this.transitiveProperties.add(curProp.asTransitiveProperty());
			}

			ExtendedIterator<? extends OntProperty> iterator = curProp.listInverseOf();
			while (iterator.hasNext()) {
				OntProperty inverse = iterator.next();
				inverseProperties.put(curProp, inverse);
				inverseProperties.put(inverse, curProp);
			}
		}
	}

	public Set<OntProperty> getSymmetricProperties() {
		return symmetricProperties;
	}

	public void setSymmetricProperties(Set<OntProperty> symmetricProperties) {
		this.symmetricProperties = symmetricProperties;
	}

	public Set<OntProperty> getTransitiveProperties() {
		return transitiveProperties;
	}

	public void setTransitiveProperties(Set<OntProperty> transitiveProperties) {
		this.transitiveProperties = transitiveProperties;
	}

	public SetMultimap<OntProperty, OntProperty> getInverseProperties() {
		return inverseProperties;
	}

	public void setInverseProperties(SetMultimap<OntProperty, OntProperty> inverseProperties) {
		this.inverseProperties = inverseProperties;
	}

}
