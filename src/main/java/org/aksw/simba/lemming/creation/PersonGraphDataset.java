package org.aksw.simba.lemming.creation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.util.iterator.ExtendedIterator;
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
			 
			 Inferer inferer = new Inferer();
			 Map <String, String> map = inferer.mapModel2Ontology(dataFolderPath);
			 
			 for (String fileName : lstSortedFilesByName) {
				 File file = new File(dataFolderPath+"/"+fileName);
				 
				 if(file != null && file.isFile() && file.getTotalSpace() > 0){
					 Model personModel = ModelFactory.createDefaultModel();
					 //read file to model
					 personModel.read(file.getAbsolutePath(), "TTL");
					 LOGGER.info("Read data to model - "+ personModel.size() + " triples");			 
					 
					 inferer.process(personModel, map, fileName, dataFolderPath);
					 
					 ColouredGraph graph = creator.processModel(personModel);
					if (graph != null) {
						LOGGER.info("Generated graph of "+ personModel.size() +" triples");
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
