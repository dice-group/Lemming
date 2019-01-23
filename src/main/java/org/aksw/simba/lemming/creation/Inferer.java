package org.aksw.simba.lemming.creation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Inferer class implements an inference of the type of subjects and objects of a given RDF Model 
 * from a given Ontology.
 * To use this class, one could first use the readOntology() method and then use the process() method.
 *
 */
public class Inferer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Inferer.class);

	public Inferer() {
		
	}
	
	/**
	 * This method creates a new model with all the statements as sourceModel and
	 * goes on to populate it further with inferred triples
	 * @param sourceModel RDF Model where we want the inference to take place
	 * @param ontModel Ontology Model
	 * @return The new model with the same triples as the sourceModel plus
	 * the inferred triples. 
	 */
	public Model process(Model sourceModel, OntModel ontModel) {
		Model newModel = ModelFactory.createDefaultModel();
		newModel.add(sourceModel);

		Set<Resource> set = extractUniqueResources(newModel);
		if (ontModel != null) {
			iterateStmts(newModel, sourceModel, ontModel);
			checkEmptyTypes(set, newModel);
		}
		return newModel;
	}

	/**
	 * 
	 * This method gets all the unique subjects and objects of a model
	 * with the exception of the objects that are not resources.
	 * It is mainly used to do a before and after count of how many resources 
	 * do not have a type.
	 * @param newModel RDF Model from where the resources are extracted
	 * @return the set of resources of the given model
	 */
	private Set<Resource> extractUniqueResources(Model model) {
		Set<Resource> set = new HashSet<>();
		StmtIterator statements = model.listStatements();
		while(statements.hasNext()) {
			Statement curStat = statements.next();
			set.add(curStat.getSubject());
			if(curStat.getObject().isResource()) {
				set.add(curStat.getObject().asResource());
			} 
		}
		checkEmptyTypes(set, model);
		return set;
	}
	
	/**
	 * This method simply logs the count of how many resources without a type exist in a given 
	 * model
	 * @param set group of resources that we want to check in the model
	 * if a type relation is existing or not
	 * @param model RDF Model where this needs to be checked in
	 */
	private void checkEmptyTypes(Set<Resource> set, Model model) {
		int emptyTypeCount = 0;
		for (Resource resource : set) {
			if (!model.contains(resource, RDF.type)) {
				emptyTypeCount++;
			} 
		}
		LOGGER.info("Number of resources without type : " + emptyTypeCount);
	}
	
	/**
	 * This method iterates through the model's statements, continuously searching for each 
	 * property in the ontology and adding the inferred triples to the new model
	 * @param newModel model where we will add the new triples
	 * @param sourceModel provided model where we iterate through the statements
	 * @param ontModel the ontology model
	 */
	public void iterateStmts(Model newModel, Model sourceModel, OntModel ontModel) {
		StmtIterator stmts = sourceModel.listStatements();
		while(stmts.hasNext()) {
			Statement curStatement = stmts.next();
			List<Statement> stmtsList = new ArrayList<>();
			Set<Statement> newStmts = searchType(curStatement, ontModel, newModel);
			stmtsList.addAll(newStmts);
			newModel.add(stmtsList);
		}
	}

	/**
	 * For a given statement, this method searches for the predicate of a model inside 
	 * the Ontology. If found in the Ontology, it then extracts the domain and range.
	 * Creating and adding a new triple with the inferred type to the model.
	 * @param statement statement in which we want to check the predicate in the ontology
	 * @param ontModel the ontology model
	 * @param newModel where we add the new triples and therefore, where we check if the 
	 * statement is already existing in the model or not
	 * @return a set of statements inferred from one property
	 */
	private Set<Statement> searchType(Statement statement, OntModel ontModel, Model newModel) {
		Set<Statement> newStmts = new HashSet<>();
		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();

		// search for the predicate of the model in the ontology
		OntProperty temp = ontModel.getOntProperty(predicate.toString());
		if (temp != null) {
			List<? extends OntResource> domain = temp.listDomain().toList();
			for(OntResource curResource: domain){
				Statement subjType = ResourceFactory.createStatement(subject, RDF.type, curResource);
				if (!newModel.contains(subjType)) {
					newStmts.add(subjType);
				}
			}
			
			List<? extends OntResource> range = temp.listRange().toList();
			for(OntResource curResource: range){
				if(object.isResource()) {
					Statement objType = ResourceFactory.createStatement(object.asResource(), RDF.type, curResource);
					if (!newModel.contains(objType)) {
						newStmts.add(objType);
					}
				} 
			}
		}
		return newStmts;
	}

	/**
	 * This method reads the ontology file 
	 * @param filePath full path to the ontology file 
	 * @return OntModel Object
	 */
	public OntModel readOntology(String filePath) {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		
		try(InputStream inputStream = FileManager.get().open(filePath)) {
			if (inputStream != null) {
				ontModel.read(inputStream, "RDF/XML");
			}
		} catch (IOException e) {
		    LOGGER.error("Couldn't read ontology file. Returning empty ontology model.",e);
		}
		
		
		return ontModel;
	}
}
