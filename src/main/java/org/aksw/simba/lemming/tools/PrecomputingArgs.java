package org.aksw.simba.lemming.tools;

import com.beust.jcommander.Parameter;

/**
 * Program arguments expected by the Metrics store.
 */
public class PrecomputingArgs {
	
	@Parameter(names = { "-ds" }, description = "Dataset name.", required = true)
	String dataset;
	
	@Parameter(names = { "--min-fitness" }, description = "Minimum Fitness", required = false)
	double minFitness = 100000.0;
	
	@Parameter(names = { "--max-iterations" }, description = "Maximum number of iterations", required = false)
	int maxIterations = 50;
}
