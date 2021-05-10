package org.aksw.simba.lemming.creation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticWebDogFoodDataset extends AbstractDatasetManager implements IDatasetManager{

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticWebDogFoodDataset.class);

    private static final int START_YEAR = 2001;
    private static final int END_YEAR = 2015;

    public SemanticWebDogFoodDataset() {
		super("SemanticWebDogFood");
	}
    
    @Override
    public ColouredGraph[] readGraphsFromFiles(String dataFolderPath) {
        Model dogFoodModel = ModelFactory.createDefaultModel();
        
        List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
        ColouredGraph graph;
        GraphCreator creator = new GraphCreator();
        long oldModelSize;
        File folder;
        
        Map<Integer, List<String>> modelOntMap = map2Ontology();

    	//start by loading the common ontologies to all models
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.getDocumentManager().setProcessImports(false);
    	ontModel.read("22-rdf-syntax-ns", "TTL");
    	ontModel.read("rdf-schema", "TTL");
    	ontModel.read("swdf-owls/foaf", "TTL");
    	ontModel.read("swdf-owls/elements-1.1", "TTL");
    	ontModel.read("swdf-owls/ical", "TTL");
    	ontModel.read("swdf-owls/owl", "TTL");
    	ontModel.read("swdf-owls/conference-ontology", "TTL" );
    	ontModel.read("swdf-owls/swrc", "TTL");
    	ontModel.read("swdf-owls/conference-ontology-alignments", "TTL");
    	
        for (int y = START_YEAR; y <= END_YEAR; ++y) {
        	OntModel curOntModel = ModelFactory.createOntologyModel();
        	curOntModel.getDocumentManager().setProcessImports(false);
        	curOntModel.add(ontModel);
        	
            LOGGER.info("Adding year {}...", y);
            folder = new File(dataFolderPath + Integer.toString(y));
            if (folder.exists()) {
                oldModelSize = dogFoodModel.size();
                addToModel(folder, dogFoodModel);
                if (oldModelSize < dogFoodModel.size()) {
                    LOGGER.info("Read data. Model has {} triples. Creating graph...", dogFoodModel.size());
                    
                    Inferer inferer = new Inferer(true);
                    List <String> all = modelOntMap.get(y);
                    
                    for(String cur: all) {
                		ontModel.read("src/main/resources/swdf-owls/"+cur, "TTL");
                	}
                                       
    				 //returns a new model with the added triples
    				dogFoodModel = inferer.process(dogFoodModel, curOntModel);
                    
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
    
    public static Map<Integer, List<String>> map2Ontology(){

    	Map<Integer, List<String>> modelOntMap = new HashMap<>();	

        List<String> list = new ArrayList<String>();
        list.add("bibo");
        list.add("terms");
        list.add("dcmitype");
        list.add("skos");

		List<String> list2006 = new ArrayList<String>();
		list2006.add("skos");
		
		List<String> list2007 = new ArrayList<String>();
		list2007.add("wgs84_pos");

		List<String> list2008 = new ArrayList<String>();
		list2007.add("wgs84_pos");
		list2008.add("terms");
		list2008.add("vcard");
		list2008.add("bibo");
		list2008.add("icaltzd");
		list2008.add("skos");
		list2008.add("cfp");
		list2008.add("rss");
		list2008.add("sioc");

		List<String> list2009 = new ArrayList<String>();
		list2009.add("terms");
		list2009.add("wgs84_pos");
		list2009.add("log");
		list2009.add("vcard");
		list2009.add("bibo");

		List<String> list2010 = new ArrayList<String>();
		list2010.add("bibo");
		list2010.add("icaltzd");
		list2010.add("wgs84_pos");
		list2010.add("skos");

		List<String> list2011 = new ArrayList<String>();
		list2011.add("terms");
		list2011.add("bibo");
		list2011.add("icaltzd");
		list2011.add("wgs84_pos");
		list2011.add("skos");

		List<String> list2012 = new ArrayList<String>();
		list2012.add("icaltzd");
		list2012.add("wgs84_pos");
		list2012.add("terms");
		list2012.add("vcard");
		list2012.add("bibo");
		list2012.add("dcmitype");	
		list2012.add("skos");
		list2012.add("timeline");
		list2012.add("deri-rooms");
		list2012.add("event");

		List<String> list2013 = new ArrayList<String>();
		list2013.add("icaltzd");
		list2013.add("bibo");
		list2013.add("dcmitype");
		list2013.add("terms");
		list2013.add("skos");
		list2013.add("wgs84_pos");
		list2013.add("org");
		
		List<String> list2014 = new ArrayList<String>();
		list2014.add("bibo");
		list2014.add("terms");
		list2014.add("dbpedia-ont");
		list2014.add("icaltzd");
		list2014.add("org");
		list2014.add("wgs84_pos");
		list2014.add("vcard-ns");
		
		List<String> list2015 = new ArrayList<String>();
		list2015.add("creator");
		list2015.add("bibo");
		list2015.add("frbr");
		list2015.add("fabio");
		list2015.add("dbpedia-ont");
		list2015.add("icaltzd");
		list2015.add("wgs84_pos");
		list2015.add("vcard-ns");
		

		modelOntMap.put(2001, list);
		modelOntMap.put(2002, list);
		modelOntMap.put(2003, list);
		modelOntMap.put(2004, list);
		modelOntMap.put(2005, list);
		modelOntMap.put(2006, list2006);
		modelOntMap.put(2007, list2007);
		modelOntMap.put(2008, list2008);
		modelOntMap.put(2009, list2009);
		modelOntMap.put(2010, list2010);
		modelOntMap.put(2011, list2011);
		modelOntMap.put(2012, list2012);
		modelOntMap.put(2013, list2013);
		modelOntMap.put(2014, list2014);
		modelOntMap.put(2015, list2015);
		
		return modelOntMap;

    }

//    public static void main(String[] args) {
//        String DATA_FOLDER_PATH = "SemanticWebDogFood/";
//        new SemanticWebDogFoodDataset().readGraphsFromFiles(DATA_FOLDER_PATH);
//    }
}
