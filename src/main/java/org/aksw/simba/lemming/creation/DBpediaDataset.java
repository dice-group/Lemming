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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("dbp") 
/**
 * TODO fix this
 */
public class DBpediaDataset extends AbstractDatasetManager implements IDatasetManager{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonGraphDataset.class);
	
	@Value("${datasets.dbp.filepath}")
	private String dataFolderPath;
	
	public DBpediaDataset() {
		super("DBpedia");
	}
	
	@Override
	public String getDatasetPath() {
		return dataFolderPath;
	}
	
	@Override
	public ColouredGraph[] readGraphsFromFiles() {
		
		 List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
		 GraphCreator creator = new GraphCreator();		
		 
		 File folder = new File(dataFolderPath);
		 if(folder != null && folder.isDirectory() && folder.listFiles().length > 0){
			 List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			 //sort ascendently
			 Collections.sort(lstSortedFilesByName);
			 
			 //key needs only the file name, whereas value needs the full path to the corresponding Ontology
			 Map<String, String> modelOntMap = new HashMap<>();	
			 modelOntMap.put("2016", "dbpedia_2016-10.owl");

			 for (String fileName : lstSortedFilesByName) {
				 File file = new File(dataFolderPath+"/"+fileName);
				 
				 if(file != null && file.isFile() && file.getTotalSpace() > 0){
					 Model model = ModelFactory.createDefaultModel();
					 //read file to model
					 model.read(file.getAbsolutePath(), "TTL");
					 LOGGER.info("Read data to model - "+ model.size() + " triples");			 

					 OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
					 ontModel.getDocumentManager().setProcessImports(false);
					 if(modelOntMap.containsKey(fileName))
						 ontModel.read(modelOntMap.get(fileName));
					 ontModel.read("22-rdf-syntax-ns", "TTL");
					 ontModel.read("rdf-schema", "TTL");
					 Inferer inferer = new Inferer(true, ontModel);
					 //returns a new model with the added triples
					 model = inferer.process(model);
					 
					 ColouredGraph graph = creator.processModel(model);
					 if (graph != null) {
						LOGGER.info("Generated graph of " + model.size() + " triples");
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
