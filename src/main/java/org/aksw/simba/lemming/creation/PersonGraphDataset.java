package org.aksw.simba.lemming.creation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonGraphDataset extends AbstractDatasetManager implements IDatasetManager{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonGraphDataset.class);
	
	public PersonGraphDataset() {
		super("PersonGraph");
	}
	
	@Override
	public ColouredGraph[] readGraphsFromFiles(String dataFolderPath) {
		
		 List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
		 GraphCreator creator = new GraphCreator();		
		 
		 File folder = new File(dataFolderPath);
		 if(folder != null && folder.isDirectory() && folder.listFiles().length > 0){
			 List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			 //sort ascendently
			 Collections.sort(lstSortedFilesByName);
			 
			 //key needs only the file name, whereas value needs the full path to the corresponding Ontology
			 Map<String, String> modelOntMap = new HashMap<>();	
			 modelOntMap.put("outputfile_2015-2004.ttl", "dbpedia_2015-04.owl");
			 modelOntMap.put("outputfile_2015-2010.ttl", "dbpedia_2015-10.owl");
			 modelOntMap.put("outputfile_2016-2004.ttl", "dbpedia_2016-04.owl");
			 modelOntMap.put("outputfile_2016-2010.ttl", "dbpedia_2016-10.owl");

			 for (String fileName : lstSortedFilesByName) {
				 File file = new File(dataFolderPath+"/"+fileName);
				 
				 if(file != null && file.isFile() && file.getTotalSpace() > 0){
					 Model personModel = ModelFactory.createDefaultModel();
					 //read file to model
					 personModel.read(file.getAbsolutePath(), "TTL");
					 LOGGER.info("Read data to model - "+ personModel.size() + " triples");			 
					 
					 Inferer inferer = new Inferer(true);
					 OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
					 ontModel.getDocumentManager().setProcessImports(false);
					 ontModel.read(modelOntMap.get(fileName));					 
					 ontModel.read("22-rdf-syntax-ns", "TTL");
					 ontModel.read("rdf-schema", "TTL");
					 
					 //returns a new model with the added triples
					 personModel = inferer.process(personModel, ontModel);
					 
					 ColouredGraph graph = creator.processModel(personModel);
					 if (graph != null) {
						LOGGER.info("Generated graph of " + personModel.size() + " triples");
						graphs.add(graph);
					 }
				 }
			 }
		 }else{
			 LOGGER.error("Find no files in \"" + folder.getAbsolutePath() + "\". Aborting.");
             System.exit(1);
		 }
		 
		 return graphs.toArray(new ColouredGraph[graphs.size()]);
	}
	
	
//	public static void main(String[] args) {
//		String DATA_FOLDER_PATH = "PersonGraph/";
//		new PersonGraphDataset().readGraphsFromFiles(DATA_FOLDER_PATH);
//    }
}
