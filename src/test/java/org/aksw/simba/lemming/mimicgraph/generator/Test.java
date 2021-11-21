package org.aksw.simba.lemming.mimicgraph.generator;
import org. junit.runner.JUnitCore;
import org. junit.runner. Result;
import org. junit.runner.notification.Failure;

public class Test {
	public static void main(String[] args) {
	
		Result result = JUnitCore.runClasses(GraphGenerationClusteringBased2Test.class);
		
		for (Failure failure : result.getFailures()) {
			System.out.println("at 12"+failure.toString());
		}
		
		System.out.println("At 15 : "+result.wasSuccessful());
	}
}
	