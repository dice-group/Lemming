package org.aksw.simba.lemming.mimicgraph.generator.factory;

import org.aksw.simba.lemming.mimicgraph.generator.IGraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.baseline.BaselineGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.baseline.BaselineInitializer;
import org.aksw.simba.lemming.mimicgraph.generator.baseline.IGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.binary.GraphInitializer;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.springframework.context.ApplicationContext;

/**
 * Factory implementation for baseline graph generation
 * 
 * @author Ana Silva
 */
public class BaselineGraphGeneratorFactory implements IGraphGeneratorFactory {
	@Override
	/**
	 * Creates a baseline graph generator using the provided initializer and
	 * arguments.
	 *
	 * @param initializer The {@link GraphInitializer} to be used
	 * @param context     The application context
	 * @param args        Additional arguments required for creating the specific
	 *                    IGraphGenerator.
	 * @return An instance of IGraphGenerator
	 */
	public IGraphGenerator createGraphGenerator(GraphInitializer initializer, ApplicationContext application,
			GraphGenerationArgs args) {
		IGenerator baseline = (IGenerator) application.getBean(args.baselineModel.toLowerCase());
		return new BaselineGenerator((BaselineInitializer) initializer, baseline);
	}
}