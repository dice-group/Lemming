package org.aksw.simba.lemming.creation;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractDatasetManager implements IDatasetManager{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatasetManager.class);
	protected String mDatasetName;
	
	public AbstractDatasetManager(String datasetName){
		mDatasetName = datasetName;
	}
	
	public String getDatasetName(){
		return mDatasetName;
	}
	
	public void setDatasetName(String datasetName){
		mDatasetName = datasetName;
	}
	
	@Override
	public void writeGraphsToFile(ColouredGraph grph) {
		Model datasetModel = ModelFactory.createDefaultModel();
		try {
			new File("/results").mkdirs();
			
			String fileName = "/results/Mimic_" + mDatasetName + ".rdf";
			String[] parts = fileName.split(".");
			Path path = Paths.get(fileName);
			File f = null;
			int i = 1;
			while (Files.exists(path)) {
			    LOGGER.warn("File allready exists!");
			    i++;
			    path = Paths.get(parts[0] + "(" + i + ")" + parts[1]);
			} 
			f = new File(parts[0] + "(" + i + ")" + parts[1]);
			
			
			// graph reverter: generate a new model from a coloured graph
			GraphReverter reverter = new GraphReverter(grph, datasetModel);
			Model newModel = reverter.processGraph();

			Writer writerforOutModel = new FileWriter(f);
			// newDogFoodModel.write(writerforOutModel);
			newModel.write(writerforOutModel, "TURTLE");
			writerforOutModel.close();
		} catch (Exception ex) {
			LOGGER.error("Failed to write to file: " + ex.getMessage());
			System.err.println("Failed to write to file: " + ex.getMessage());
			System.exit(1);
		}
	}

	@Override
	public ColouredGraph[] readGraphsFromFiles(String dataFolderPath) {
		return null;
	}
	
}
