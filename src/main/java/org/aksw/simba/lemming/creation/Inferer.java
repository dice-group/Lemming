package org.aksw.simba.lemming.creation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.apache.jena.util.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inferer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Inferer.class);
	private int counter;

	public Inferer() {
		this.counter = 0;
	}

	public Model process(Model personModel, Map<String, String> map, String fileName, String dataFolderPath) {
		Model newModel = ModelFactory.createDefaultModel();
		newModel.add(personModel);

		Set<Resource> set = extractUniqueResources(newModel);

		OntModel ontModel = null;
		try {
			ontModel = readOntology(map, fileName, dataFolderPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (ontModel != null) {
			extractProperties(newModel, ontModel);
			checkEmptyTypes(set, newModel);
		}
		return newModel;
	}

	private Set<Resource> extractUniqueResources(Model newModel) {
		Set<Resource> set = new HashSet<>();
		List<Statement> statements = newModel.listStatements().toList();
		Property type = ResourceFactory.createProperty("https://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		for (Statement statement : statements) {
			if (statement.getPredicate().equals(type)) {

			} else {
				set.add(statement.getSubject());
				set.add(statement.getObject().asResource());
			}
		}
		checkEmptyTypes(set, newModel);
		return set;
	}

	private void checkEmptyTypes(Set<Resource> set, Model personModel) {
		Property type = ResourceFactory.createProperty("https://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		for (Resource resource : set) {
			if (personModel.contains(resource, type)) {

			} else {
				counter++;
			}
		}
		LOGGER.info("Number of resources without type : " + counter);
		counter = 0;
		LOGGER.info("Counter Reset");
	}

	public void extractProperties(Model model, OntModel ontModel) {
		List<Statement> curModelList = model.listStatements().toList();
		for (Statement statement : curModelList) {
			List<Statement> statements = searchType(statement, ontModel, model);
			model.add(statements);
		}
	}

	private List<Statement> searchType(Statement statement, OntModel ontModel, Model model) {
		List<Statement> stList = new ArrayList<>();
		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();
		Property type = ResourceFactory.createProperty("https://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		// search for the predicate of the model in the ontology
		OntProperty temp = ontModel.getOntProperty(predicate.toString());
		if (temp != null) {
			List<? extends OntResource> domain = temp.listDomain().toList();
			for (int i = 0; i < domain.size(); i++) {
				Statement subjType = ResourceFactory.createStatement(subject, type, domain.get(i));
				if (!model.contains(subjType)) {
					stList.add(subjType);
				}
			}
			
			List<? extends OntResource> range = temp.listRange().toList();
			for (int i = 0; i < range.size(); i++) {
				Statement objType = ResourceFactory.createStatement(object.asResource(), type, range.get(i));
				if (!model.contains(objType)) {
					stList.add(objType);
				}
			}
		}
		return stList;
	}

	public Map<String, String> mapModel2Ontology() {
		Map<String, String> modelOwlMap = new HashMap<String, String>();
		modelOwlMap.put("test.ttl", "dbpedia_2015-04.owl");
		modelOwlMap.put("outputfile_2015-2004.ttl", "dbpedia_2015-04.owl");
		modelOwlMap.put("outputfile_2015-2010.ttl", "dbpedia_2015-10.owl");
		modelOwlMap.put("outputfile_2016-2004.ttl", "dbpedia_2016-04.owl");
		modelOwlMap.put("outputfile_2016-2010.ttl", "dbpedia_2016-10.owl");
		return modelOwlMap;
	}

	public OntModel readOntology(Map<String, String> map, String fileName, String dataFolderPath) throws IOException {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		String fullPath = dataFolderPath + "\\ontologies\\" + map.get(fileName);
		InputStream inputStream = FileManager.get().open(fullPath);
		if (inputStream != null) {
			ontModel.read(inputStream, "RDF/XML");
			inputStream.close();
		}
		return ontModel;
	}
}
