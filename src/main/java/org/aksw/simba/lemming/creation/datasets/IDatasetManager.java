package org.aksw.simba.lemming.creation.datasets;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Interface responsible for the graph reading and saving methods
 */
public interface IDatasetManager {
	/**
	 * 
	 * @return The dataset folder path
	 */
	public String getDatasetPath();

	/**
	 * Reads all graphs from the dataset folder path and creates the respective
	 * {@link ColouredGraph} in an array
	 * 
	 * @return
	 */
	public ColouredGraph[] readGraphsFromFiles();

	/**
	 * 
	 * @param folder
	 * @return An unique filepath from the given folder
	 */
	public String getSavedFileName(String folder);

	/**
	 * 
	 * @param grph     A {@link ColouredGraph} instance
	 * @param filePath File path where to save the instance
	 */
	public void writeGraphsToFile(ColouredGraph grph, String filePath);

	/**
	 * Reads the intermediate graph from a specific file path
	 * 
	 * @param filePath the full path of the mimic graph
	 * @return ColouredGraph object
	 */
	public ColouredGraph readIntResults(String filePath);

	/**
	 * Saves the intermediate graph in a specific location
	 * 
	 * @param curMimicGraph object to be saved
	 * @param filePath      where to save the file
	 */
	public void persistIntResults(ColouredGraph curMimicGraph, String filePath);

	/**
	 * Reads a single graph from file and creates the respective
	 * {@link ColouredGraph}
	 * 
	 * @param file File path
	 * @return {@link ColouredGraph} instance of the graph
	 */
	default public ColouredGraph readGraphFromFile(String file) {
		GraphCreator creator = new GraphCreator(false);
		Model model = ModelFactory.createDefaultModel();
		model.read(file);
		return creator.processModel(model);
	}

	/**
	 * Reads all graphs from a folder into a single {@link ColouredGraph} object
	 * 
	 * @param dataFolderPath Path to the folder
	 * @return {@link ColouredGraph} instance of the graph
	 */
	default public ColouredGraph readGraphsFromFolder(String dataFolderPath) {
		GraphCreator creator = new GraphCreator(false);
		ColouredGraph graph = null;
		File folder = new File(dataFolderPath);
		if (folder != null && folder.isDirectory() && folder.listFiles().length > 0) {
			List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			Collections.sort(lstSortedFilesByName);
			Model model = ModelFactory.createDefaultModel();
			for (String fileName : lstSortedFilesByName) {
				System.out.println("Reading file: " + fileName);
				File file = new File(dataFolderPath + "/" + fileName);
				model.read(file.getAbsolutePath());
			}
			graph = creator.processModel(model);
		}
		return graph;
	}
}
