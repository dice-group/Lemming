package org.aksw.simba.lemming.tools.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.metrics.single.SingleValueMetricResult;
import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.beust.jcommander.JCommander;

@RunWith(Parameterized.class)
public class ResultWriterTest {
	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> testConfigs = new ArrayList<Object[]>();
		String[] a1S = {"-nv", "5", "-ds", "testDataset", "-dp", "testPath", "-seed", "42"};
		GraphGenerationArgs a1 = new GraphGenerationArgs();
		JCommander.newBuilder().addObject(a1).build().parse(a1S);
		List<UpdatableMetricResult> results1 = new ArrayList<>();
		results1.add(new SingleValueMetricResult("#edges", 10));
		results1.add(new SingleValueMetricResult("#vertices", 5));
		String expectedHeader = 
				"fileName,runtime,baselineModel,classSelector,dataset,datasetPath,loadMimicGraph,mode,noOptimizationSteps,noThreads,noVertices,seed,simplexClass,simplexProperty,vertexSelector,#edges,#vertices\n";
		String expectedValues = 
				"savedFile1,7,null,UCS,testDataset,testPath,null,Binary,0,1,5,42,null,null,UIS,10.0,5.0\n";
		String[] a2S = {"-nv", "6621", "-ds", "testDataset2", "-dp", "testPath2", "-seed", "422", "-thrs", "128", "-l", "true", "-op", "100", "-bl", "baseline"};
		GraphGenerationArgs a2 = new GraphGenerationArgs();
		JCommander.newBuilder().addObject(a2).build().parse(a2S);String expectedValues2 = 
				"savedFile2,133,baseline,UCS,testDataset2,testPath2,true,Binary,100,128,6621,422,null,null,UIS,10.0,5.0\n";
		
		testConfigs.add(new Object[] {a1, "savedFile1", 7, results1 , expectedHeader,expectedValues});
		testConfigs.add(new Object[] {a2, "savedFile2", 133, results1 , expectedHeader,expectedValues2});
		return testConfigs;
	}

	private GraphGenerationArgs pArgs;
	private String savedFile;
	private long elapsedTime;
	private List<UpdatableMetricResult> results;
	private String expectedHeader;
	private String expectedValues;

	public ResultWriterTest(GraphGenerationArgs pArgs, String savedFile,long elapsedTime,List<UpdatableMetricResult> results, String expectedHeader, String expectedValues) {
		this.pArgs = pArgs;
		this.savedFile = savedFile;
		this.elapsedTime = elapsedTime;
		this.results = results;
		this.expectedHeader = expectedHeader;
		this.expectedValues = expectedValues;
	}

	@Test
	public void run() {
		Result result = new Result(pArgs, savedFile, elapsedTime, results);
		String fullCSV = null;
		try {
			fullCSV = result.getResultsAsCSV();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		String expectedCSV = expectedHeader+expectedValues;
		Assert.assertEquals(expectedCSV, fullCSV);
		
		String values = null;
		try {
			values = result.getFieldValues();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(expectedValues, values);

	}

}
