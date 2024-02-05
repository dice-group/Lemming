package org.aksw.simba.lemming.tools;

import com.beust.jcommander.Parameter;

public class PrecomputingArgs {
	
	@Parameter(names = { "-ds" }, description = "Dataset name.", required = true)
	String dataset;
	
	@Parameter(names = { "-overwrite" }, description = "Flag if we want to overwrite the results.", required = false)
	boolean recalculateMetrics = false;
	
	@Parameter(names = { "--min-fitness" }, description = "Minimum Fitness", required = false)
	double minFitness = 100000.0;
	
	@Parameter(names = { "--max-iterations" }, description = "Maximum number of iterations", required = false)
	int maxIterations = 50;
}
