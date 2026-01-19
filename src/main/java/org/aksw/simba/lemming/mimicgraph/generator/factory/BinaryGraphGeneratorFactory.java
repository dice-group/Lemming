package org.aksw.simba.lemming.mimicgraph.generator.factory;

import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.generator.IGraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.binary.GraphGenerator;
import org.aksw.simba.lemming.mimicgraph.generator.binary.GraphInitializer;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Factory implementation for binary graph generation
 * 
 * @author Ana Silva
 */
@Component
public class BinaryGraphGeneratorFactory implements IGraphGeneratorFactory {
	
	/** Logging object */
	private static final Logger LOGGER = LoggerFactory.getLogger(BinaryGraphGeneratorFactory.class);

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
		LOGGER.debug("Entered factory...");
		IClassSelector classSelector = (IClassSelector) application.getBean(args.classSelector, initializer);
		LOGGER.debug("Got class selector...");
		IVertexSelector vertexSelector = (IVertexSelector) application.getBean(args.vertexSelector, initializer);
		LOGGER.debug("Got vertex selector...");
		return new GraphGenerator(initializer, classSelector, vertexSelector);
	}
}