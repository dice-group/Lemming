package org.aksw.simba.lemming.creation;

import org.aksw.simba.lemming.ColouredGraph;

public interface IDatasetManager {
	public String getDatasetPath();
	public ColouredGraph[] readGraphsFromFiles();
	public String writeGraphsToFile(ColouredGraph grph);
	
	/**
	 * Reads the intermediate graph from a specific file path
	 * @param filePath the full path of the mimic graph
	 * @return ColouredGraph object
	 */
	public ColouredGraph readIntResults(String filePath);
	
	/**
	 * Saves the intermediate graph in a specific location
	 * @param curMimicGraph object to be saved
	 * @param filePath where to save the file
	 */
	public void persistIntResults(ColouredGraph curMimicGraph, String filePath);
}
