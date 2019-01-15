package org.aksw.simba.lemming.creation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public void process (Model personModel, Map<String, String> map, String fileName, String dataFolderPath) {
		Model tempModel = ModelFactory.createDefaultModel();
		tempModel.add(personModel);
		
		checkEmptyTypes(tempModel);
		LOGGER.info("Before - Number of resources without type : " + counter);
		counter = 0;
		
		OntModel ontModel = null;
		try {
			ontModel = readOntology(map, fileName, dataFolderPath);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if(ontModel != null) {
			tempModel.add(extractProperties(tempModel, ontModel));
			checkEmptyTypes(tempModel);
			LOGGER.info("After - Number of resources without type : " + counter);
		}		
	}
	

	private void checkEmptyTypes(Model personModel) {
		
		List <Statement> curModelList = personModel.listStatements().toList();	
		Property type = ResourceFactory.createProperty("https://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		
		
		for(Statement statement: curModelList) {
			Resource subject = statement.getSubject();
			RDFNode object = statement.getObject();
			
			Statement sStat = personModel.getProperty(subject, type);
			checkEmpty(sStat);
			
			Statement oStat = personModel.getProperty(object.asResource(), type);
			checkEmpty(oStat);
		}
	}
	
	public void checkEmpty(Statement statement) {
		if(statement == null){
			counter++;
		}
	}
	

	public List <Statement> extractProperties(Model model, OntModel ontModel) {
		List <Statement> curModelList = model.listStatements().toList();
		List <Statement> newStats = new ArrayList<>();
		for(Statement statement: curModelList) {
			newStats.addAll(searchType(statement, ontModel));
		}
		return newStats;
	}

	private List <Statement> searchType(Statement statement, OntModel ontModel) {
		List <Statement> stList = new ArrayList<>();
		Resource subject = statement.getSubject();
		Property  predicate = statement.getPredicate();
		RDFNode object = statement.getObject();
		Property type = ResourceFactory.createProperty("https://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		
		//search for the predicate of the model in the ontology 
		OntProperty temp = ontModel.getOntProperty(predicate.toString());
		if(temp != null) {
			OntResource domain = temp.getDomain();
			OntResource range = temp.getRange();
			
			Statement subjType = ResourceFactory.createStatement(subject, type, domain);
			stList.add(subjType);
			
			Statement objType = ResourceFactory.createStatement(object.asResource(), type, range);
			stList.add(objType);
		}
		return stList;
	}
	
	public Map<String, String> mapModel2Ontology (String path){
		Map<String, String> modelOwlMap = new HashMap<String, String>();
		modelOwlMap.put("outputfile_2015-2004.ttl", "dbpedia_2015-04.owl");
		modelOwlMap.put("outputfile_2015-2010.ttl", "dbpedia_2015-10.owl");
		modelOwlMap.put("outputfile_2016-2004.ttl", "dbpedia_2016-04.owl");
		modelOwlMap.put("outputfile_2016-2010.ttl", "dbpedia_2016-10.owl");
		return modelOwlMap;
	}	

	public OntModel readOntology(Map<String, String> map, String fileName, String dataFolderPath) throws IOException {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		String fullPath = dataFolderPath+"\\ontologies\\"+map.get(fileName);
		InputStream inputStream = null;
		inputStream = FileManager.get().open(fullPath);
		ontModel.read(inputStream, "RDF/XML");
		if(inputStream != null) {
			inputStream.close();
		}	
		return ontModel;
	}
}
