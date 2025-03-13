package org.aksw.simba.lemming.mimicgraph.generator.factory;


/**
 * 
 * 
 * @author Ana Silva
 */
public class GraphGeneratorFactoryRegistry {
	private static final IGraphGeneratorFactory BINARY_FACTORY = new BinaryGraphGeneratorFactory();
	private static final IGraphGeneratorFactory SIMPLEX_FACTORY = new SimplexGraphGeneratorFactory();
	private static final IGraphGeneratorFactory BASELINE_FACTORY = new BaselineGraphGeneratorFactory();

	/**
	 * Retrieves the appropriate factory based on the given mode.
	 *
	 * @param mode The mode of the graph generation (e.g., "binary", "simplex",
	 *             "baseline").
	 * @return An instance of IGraphGeneratorFactory suitable for the specified
	 *         mode.
	 */
	public static IGraphGeneratorFactory getFactory(String mode) {
		switch (mode.toLowerCase()) {
		case "binary":
			return BINARY_FACTORY;
		case "simplex":
			return SIMPLEX_FACTORY;
		case "baseline":
			return BASELINE_FACTORY;
		default:
			throw new IllegalArgumentException("Unsupported graph generation mode: " + mode);
		}
	}
}
