package org.aksw.simba.lemming.creation;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.GlobalDataCollecter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticWebDogFoodReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticWebDogFoodReader.class);

    private static final String DATA_FOLDER_PATH = "SemanticWebDogFood/";
    private static final int START_YEAR = 2001;
    private static final int END_YEAR = 2015;

    public static ColouredGraph[] readGraphsFromFile() {
        return readGraphsFromFile(DATA_FOLDER_PATH);
    }

	public static void writeGraphsToFile(ColouredGraph grph) {
		Model dogFoodModel = ModelFactory.createDefaultModel();
		try {
			// graph reverter: generate a new model from a coloured graph
			GraphReverter reverter = new GraphReverter(grph, dogFoodModel);
			Model newDogFoodModel = reverter.processGraph();

			Writer writerforOutModel = new FileWriter(new File("mimic_rdf_graph.rdf"));
			// newDogFoodModel.write(writerforOutModel);
			newDogFoodModel.write(writerforOutModel, "TURTLE");
			writerforOutModel.close();
		} catch (Exception ex) {
			LOGGER.error("Failed to write to file: " + ex.getMessage());
			System.err.println("Failed to write to file: " + ex.getMessage());
			System.exit(1);
		}
	}
    
    public static ColouredGraph[] readGraphsFromFile(String dataFolderPath) {
        Model dogFoodModel = ModelFactory.createDefaultModel();

        List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
        ColouredGraph graph;
        GraphCreator creator = new GraphCreator();
        long oldModelSize;
        File folder;
        for (int y = START_YEAR; y <= END_YEAR; ++y) {
            LOGGER.info("Adding year {}...", y);
            folder = new File(dataFolderPath + Integer.toString(y));
            if (folder.exists()) {
                oldModelSize = dogFoodModel.size();
                addToModel(folder, dogFoodModel);
                if (oldModelSize < dogFoodModel.size()) {
                    LOGGER.info("Read data. Model has {} triples. Creating graph...", dogFoodModel.size());
                    graph = creator.processModel(dogFoodModel);
                    if (graph != null) {
                        LOGGER.info("Generated graph.", dogFoodModel.size());
                        graphs.add(graph);
                        
                        //collect information of current loaded graph
                        GlobalDataCollecter.getInstance().addGraphs(folder.getName(), graph);
                        GlobalDataCollecter.getInstance().setDatatypeEdgePalette(graph.getDataTypedEdgePalette());
                        GlobalDataCollecter.getInstance().setVertexPallete(graph.getVertexPalette());
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

    public static void main(String[] args) {
        SemanticWebDogFoodReader.readGraphsFromFile();
    }
}
