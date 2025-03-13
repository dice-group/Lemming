package org.aksw.simba.lemming.mimicgraph.generator.factory;

import org.aksw.simba.lemming.mimicgraph.generator.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGenerator;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.springframework.context.ApplicationContext;

/**
 * Interface for the factory classes of graph generators
 * 
 * @author Ana Silva
 */
public interface IGraphGeneratorFactory {

	/**
	 * Initializes the correct graph generator given the mode specified by the user
	 * 
	 * @param initializer The {@link GraphInitializer} to be used
	 * @param context     The application context
	 * @param args        Additional arguments required for creating the specific
	 *                    IGraphGenerator.
	 * @return An instance of IGraphGenerator
	 */
	IGraphGenerator createGraphGenerator(GraphInitializer initializer, ApplicationContext context,
			GraphGenerationArgs args);
}
