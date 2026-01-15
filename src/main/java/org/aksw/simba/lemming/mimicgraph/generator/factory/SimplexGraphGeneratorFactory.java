package org.aksw.simba.lemming.mimicgraph.generator.factory;

import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.binary.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.generator.simplex.SimplexGraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.simplex.SimplexGraphInitializer;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexClass;
import org.aksw.simba.lemming.simplexes.distribution.ISimplexProperty;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.springframework.context.ApplicationContext;

/**
 * Factory implementation for simplex graph generation
 * 
 * @author Ana Silva
 */
public class SimplexGraphGeneratorFactory implements IGraphGeneratorFactory {
	@Override
	/**
	 * Creates a simplex graph generator using the provided initializer and
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
		ISimplexClass simplexClass = (ISimplexClass) application.getBean(args.simplexClass, initializer);
		ISimplexProperty simplexProperty = (ISimplexProperty) application.getBean(args.simplexProperty, initializer);
		return new SimplexGraphGenerator((SimplexGraphInitializer) initializer, simplexClass, simplexProperty,
				classSelector, vertexSelector);
	}
}