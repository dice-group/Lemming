package org.aksw.simba.lemming.tools.parameters;

import com.beust.jcommander.Parameter;

/**
 * Program arguments expected by the Metrics store.
 */
public class PrecomputingArgs {
	
	@Parameter(names = { "-ds" }, description = "Dataset name.", required = true)
	public String dataset;
	
	@Parameter(names = { "--min-fitness" }, description = "Minimum Fitness", required = false)
	public double minFitness = 100000.0;
	
	@Parameter(names = { "--max-iterations" }, description = "Maximum number of iterations", required = false)
	public int maxIterations = 50;
}
