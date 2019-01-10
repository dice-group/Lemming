package org.aksw.simba.lemming.creation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inferer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Inferer.class);
	
	public Inferer() {
		// TODO Auto-generated constructor stub
	}
	
	public void process (Model personModel, Map<String, String> map, String fileName, String dataFolderPath) {
		List<Property> propList = extractProperties(personModel);
		OntModel ontModel = null;
		try {
			ontModel = readOntology(map, fileName, dataFolderPath);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if(ontModel != null) {
			ExtendedIterator<OntProperty> iterator = ontModel.listAllOntProperties();		
			while(iterator.hasNext()) {
				OntProperty ontProperty = iterator.next();
				OntResource domain = ontProperty.getDomain();
				OntResource range = ontProperty.getRange();
			}
			
			LOGGER.info("as");
		}		
	}
	
	public List <Property> extractProperties(Model model) {
		StmtIterator iterator = model.listStatements();
		List <Property> propList = new ArrayList<>();
		while(iterator.hasNext()) {
			Statement statement = iterator.nextStatement();
			Property  predicate = statement.getPredicate();
			propList.add(predicate);
		}
		return propList;	
	}

//	public Map<String, String> mapModel2Ontology(String path, List<String> model) {
//		String fullFolderPath = path+"\\ontologies";
//		File folder = new File(fullFolderPath);
//		List <String> ontList = new ArrayList<>();
//		Map<String, String> modelOwlMap = new HashMap<String, String>();
//		if(folder != null && folder.isDirectory() && folder.listFiles().length > 0){
//			ontList = Arrays.asList(folder.list());
//			Collections.sort(ontList);
//			Iterator<String> i1 = model.iterator();
//			Iterator<String> i2 = ontList.iterator();
//			while (i1.hasNext() && i2.hasNext()) {
//				modelOwlMap.put(i1.next(), i2.next());
//			}
//		} 
//		return modelOwlMap;
//	}
	
	public Map<String, String> mapModel2Ontology (String path){
		Map<String, String> modelOwlMap = new HashMap<String, String>();
		modelOwlMap.put("outputfile_2015-2004.ttl", "dbpedia_2015-04.owl");
		modelOwlMap.put("outputfile_2015-2010.ttl", "dbpedia_2015-10.owl");
		modelOwlMap.put("outputfile_2016-2004.ttl", "dbpedia_2016-04.owl");
		modelOwlMap.put("outputfile_2016-2010.ttl", "dbpedia_2016-10.owl");
		return modelOwlMap;
	}	

	public OntModel readOntology(Map<String, String> map, String fileName, String dataFolderPath) throws IOException {
		// not sure of the spec passed to the factory? reasoner?
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		String fullPath = dataFolderPath+"\\ontologies\\"+map.get(fileName);
		InputStream inputStream = null;
		try {
			inputStream = FileManager.get().open(fullPath);
			ontModel.read(inputStream, "RDF/XML");
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
		}		
		return ontModel;
	}
}
