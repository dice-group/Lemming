package org.aksw.simba.lemming.tools;

import com.beust.jcommander.Parameter;

/**
 * Program arguments for the GraphGenerationTest
 * 
 * @author Ana Silva
 *
 */
public class GraphGenerationArgs {

	@Parameter(names = { "-ds" }, description = "Dataset name.", required = true)
	String dataset;

	@Parameter(names = { "-nv" }, description = "Number of vertices", required = true)
	int noVertices;

	@Parameter(names = { "-thrs" }, description = "Number of threads", required = false)
	int noThreads = 1;

	@Parameter(names = { "-seed" }, description = "Seed", required = false)
	long seed = System.currentTimeMillis();

	@Parameter(names = { "-l" }, description = "Load mimic graph", required = false)
	String loadMimicGraph;

	@Parameter(names = { "-c" }, description = "Class Selector Type", required = true)
	String classSelector;
	
	@Parameter(names = { "-v" }, description = "Vertex Selector Type", required = true)
	String vertexSelector;

	@Parameter(names = {
			"-op" }, description = "Number of iterations during the graph optimization phase", required = true)
	int noOptimizationSteps;

	/**
	 * 
	 * @return A String with the parameters and respective values.
	 */
	public String getArguments() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Parameters and values:\n");
		for (java.lang.reflect.Field field : this.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				stringBuilder.append(field.getName()).append(": ").append(field.get(this)).append("\n");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return stringBuilder.toString();
	}

}
