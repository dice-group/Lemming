package org.aksw.simba.lemming.tools.main;

import org.aksw.simba.lemming.tools.GraphGenerationTest;
import org.aksw.simba.lemming.tools.PrecomputingValues;
import org.aksw.simba.lemming.tools.SingleGraphGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Entry point
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = "org.aksw.simba.lemming")
public class EntryPoint {

	/** Logging object */
	private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

	public static void main(String[] args) {
		if (args.length == 0) {
			LOGGER.error("Please provide one of the currently allowed launch keywords: {graph, single-graph, store}.");
			return;
		}

		String keyword = args[0];
		String[] remainingArgs = new String[args.length - 1];
		System.arraycopy(args, 1, remainingArgs, 0, remainingArgs.length);

		switch (keyword) {
		case "graph":
			GraphGenerationTest.main(remainingArgs);
			break;
		case "single-graph":
			SingleGraphGeneration.main(remainingArgs);
			break;
		case "store":
			PrecomputingValues.main(remainingArgs);
			break;
		default:
			LOGGER.warn("Unknown keyword: " + keyword);
			break;
		}
	}

}
