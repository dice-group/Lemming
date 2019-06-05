package org.aksw.simba.lemming.util;

import java.io.File;
import java.io.IOException;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.creation.PersonGraphDataset;
import org.junit.Assert;
import org.junit.Test;


public class PersistenceTest {
	
	@Test
	public void test() {
		ColouredGraph [] graphs = new ColouredGraph[5];
		graphs[0] = IOHelper.readGraphFromResource(this.getClass().getClassLoader(), "graph_loop_2.n3", "N3");
		graphs[1] = IOHelper.readGraphFromResource(this.getClass().getClassLoader(), "graph1.n3", "N3");
		graphs[2] = IOHelper.readGraphFromResource(this.getClass().getClassLoader(), "graph2.n3", "N3");
		graphs[3] = IOHelper.readGraphFromResource(this.getClass().getClassLoader(), "email-Eu-core.n3", "N3");
		graphs[4] = IOHelper.readGraphFromResource(this.getClass().getClassLoader(), "graph1_1.n3", "N3");
		
		/**
		 * for each graph
		 * 1) store the graph
		 * 2) read the same graph
		 * 3) check if the graph object retrieved is the same as the one we stored
		 */
		for (ColouredGraph curGraph : graphs) {
			IDatasetManager mDatasetManager = new PersonGraphDataset();
			
			File tempFile = null;
			try {
				tempFile = File.createTempFile("Initialized_MimicGraph", ".ser");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(tempFile != null) {
				String filePath = tempFile.getAbsolutePath();
				
		        mDatasetManager.persistIntResults(curGraph, filePath);
		        
		        ColouredGraph actual = mDatasetManager.readIntResults(filePath);
		        Assert.assertTrue(actual.equals(curGraph));
			}
		}
	}
}
