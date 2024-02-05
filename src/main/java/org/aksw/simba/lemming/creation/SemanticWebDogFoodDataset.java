package org.aksw.simba.lemming.creation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("swdf") 
//@Scope(value = "prototype")
public class SemanticWebDogFoodDataset extends AbstractDatasetManager implements IDatasetManager{

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticWebDogFoodDataset.class);

    private static final int START_YEAR = 2001;
    private static final int END_YEAR = 2019;
    
    @Value("${datasets.swdf.filepath}")
	String dataFolderPath;

    public SemanticWebDogFoodDataset() {
		super("SemanticWebDogFood");
	}
    
    @Override
    public ColouredGraph[] readGraphsFromFiles() {
        Model dogFoodModel = ModelFactory.createDefaultModel();
        
        List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
        ColouredGraph graph;
        GraphCreator creator = new GraphCreator();
        long oldModelSize;
        File folder;

    	//start by loading the common ontologies to all models
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.getDocumentManager().setProcessImports(false);
    	ontModel.read("22-rdf-syntax-ns", "TTL");
    	ontModel.read("rdf-schema", "TTL");
    	File ontFolder = new File("swdf-owls");
    	for (File file : ontFolder.listFiles()) {
    		ontModel.read(file.getAbsolutePath(), "TTL");
    	}
        Inferer inferer = new Inferer(true, ontModel);

        for (int y = START_YEAR; y <= END_YEAR; ++y) {
        	
            LOGGER.info("Adding year {}...", y);
            folder = new File(dataFolderPath + Integer.toString(y));
            if (folder.exists()) {
                oldModelSize = dogFoodModel.size();
                addToModel(folder, dogFoodModel);
                if (oldModelSize < dogFoodModel.size()) {
                    LOGGER.info("Read data. Model has {} triples. Creating graph...", dogFoodModel.size());

    				 //returns a new model with the added triples
    				dogFoodModel = inferer.process(dogFoodModel);
    				graph = creator.processModel(dogFoodModel);
                    if (graph != null) {
                        LOGGER.info("Generated graph." + dogFoodModel.size());
                        graphs.add(graph);
                    } else {
                        LOGGER.error("Couldn't generate coloured graph.");
                    }
                } else {
                    LOGGER.error("The model hasn't been grown after reading additional files.");
                }
            } else {
                LOGGER.error("The folder {} does not exist.", folder.toString());
            }
            
           
        }
        // try-and-error analysis of data typed literals in the current dataset.
        //LiteralDatatypeAnalyser literalAnalyser = new LiteralDatatypeAnalyser(SemanticWebDogFoodReader.class.getName());
        //literalAnalyser.analyzeDatatype(dogFoodModel);
        
        return graphs.toArray(new ColouredGraph[graphs.size()]);
    }

    private static void addToModel(File folder, Model dogFoodModel) {
        for (File file : folder.listFiles()) {
            try {
                dogFoodModel.read(file.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("Exception while reading file \"" + file.toString() + "\". Aborting.", e);
                System.exit(1);
            }
        }
    }
    
    @Override
    public String toString() {
    	return "swdf";
    }

	@Override
	public String getDatasetPath() {
		return dataFolderPath;
	}

//    public static void main(String[] args) {
//        String DATA_FOLDER_PATH = "SemanticWebDogFood/";
//        new SemanticWebDogFoodDataset().readGraphsFromFiles(DATA_FOLDER_PATH);
//    }
}
