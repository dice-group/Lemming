package org.aksw.simba.lemming.tools.parameters;

import com.beust.jcommander.Parameter;

/**
 * Program arguments for the GraphGenerationTest
 * 
 * @author Ana Silva
 *
 */
public class GraphGenerationArgs {

	@Parameter(names = { "-ds" }, description = "Dataset name.", required = true)
	public String dataset;

	@Parameter(names = { "-nv" }, description = "Number of vertices", required = true)
	public int noVertices;

	@Parameter(names = { "-thrs" }, description = "Number of threads", required = false)
	public int noThreads = 1;

	@Parameter(names = { "-seed" }, description = "Seed", required = false)
	public long seed = System.currentTimeMillis();

	@Parameter(names = { "-l" }, description = "Load mimic graph", required = false)
	public String loadMimicGraph;
	
	@Parameter(names = { "-m" }, description = "Mode", required = false)
	public String mode = "Binary";

	@Parameter(names = { "-c" }, description = "Class Selector Type", required = false)
	public String classSelector;
	
	@Parameter(names = { "-v" }, description = "Vertex Selector Type", required = false)
	public String vertexSelector;
	
	@Parameter(names = { "-sp" }, description = "Simplex Property", required = false)
	public String simplexProperty;
	
	@Parameter(names = { "-sc" }, description = "Simplex Class", required = false)
	public String simplexClass;

	@Parameter(names = {
			"-op" }, description = "Number of iterations during the graph optimization phase", required = true)
	public int noOptimizationSteps;
	
	@Parameter(names = { "-bl" }, description = "Baseline model", required = false)
	public String baselineModel;

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