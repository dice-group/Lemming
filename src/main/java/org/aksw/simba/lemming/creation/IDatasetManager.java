package org.aksw.simba.lemming.creation;

import org.aksw.simba.lemming.ColouredGraph;

public interface IDatasetManager {
	public ColouredGraph[] readGraphsFromFiles(String dataFolderPath);
	public String writeGraphsToFile(ColouredGraph grph);
}
