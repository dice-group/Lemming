package org.aksw.simba.lemming.mimicgraph.generator.factory;

import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.generator.GraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGenerator;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.springframework.context.ApplicationContext;

/**
 * Factory implementation for binary graph generation
 * 
 * @author Ana Silva
 */
public class BinaryGraphGeneratorFactory implements IGraphGeneratorFactory {
	@Override
	/**
	 * Creates a binary graph generator using the provided initializer and
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
		IClassSelector classSelector = (IClassSelector) application.getBean(args.classSelector, initializer);
		IVertexSelector vertexSelector = (IVertexSelector) application.getBean(args.vertexSelector, initializer);
		return new GraphGenerator(initializer, classSelector, vertexSelector);
	}
}