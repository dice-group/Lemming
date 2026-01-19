package org.aksw.simba.lemming.creation.datasets;

import org.aksw.simba.lemming.ColouredGraph;

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
	 * Reads a single graph from a file, or concatenates all the files in a folder 
	 * to a single graph object.
	 * 
	 * @return The {@link ColouredGraph} object
	 */
	public ColouredGraph readSingleGraphFromFileOrFolder();

	
}
