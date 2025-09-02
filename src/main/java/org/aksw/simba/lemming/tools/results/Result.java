package org.aksw.simba.lemming.tools.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aksw.simba.lemming.metrics.single.UpdatableMetricResult;
import org.aksw.simba.lemming.tools.parameters.GraphGenerationArgs;


/**
 * Parses and saves the generation results in a CSV file.
 */
public class Result {

	// program arguments
	private GraphGenerationArgs args;
	// saved file name
	private String fileName;
	// generation runtime
	private long runtime;
	// generated graph results
	private List<UpdatableMetricResult> results;

	/**
	 * 
	 * @param args
	 * @param fileName
	 * @param runtime
	 * @param results
	 */
	public Result(GraphGenerationArgs args, String fileName, long runtime, List<UpdatableMetricResult> results) {
		this.args = args;
		this.fileName = fileName;
		this.runtime = runtime;
		this.results = results;
		Collections.sort(this.results, Comparator.comparing(UpdatableMetricResult::getMetricName));
	}

	/**
	 * Creates headers and values as CSV
	 * 
	 * @return {@link String} representation of the CSV 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public String getResultsAsCSV() throws IllegalArgumentException, IllegalAccessException {

		Field[] fields = args.getClass().getDeclaredFields();
		Arrays.sort(fields, (f1, f2) -> f1.getName().compareTo(f2.getName()));

		// create header
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("fileName,runtime,");
		for (Field field: fields) {
			stringBuilder.append(field.getName()).append(",");
		}
		for (int i = 0; i < results.size(); i++) {
			stringBuilder.append(results.get(i).getMetricName());
			if (i < results.size() - 1) {
				stringBuilder.append(",");
			}
		}
		stringBuilder.append("\n");

		// Append values
		stringBuilder.append(fileName).append(",");
		stringBuilder.append(runtime).append(",");
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Object fieldValue = field.get(args);
			stringBuilder.append(fieldValue);
			stringBuilder.append(",");
		}
		
		for (int i = 0; i < results.size(); i++) {
			stringBuilder.append(results.get(i).getResult());
			if (i < results.size() - 1) {
				stringBuilder.append(",");
			}
		}
		stringBuilder.append("\n");
		
		return stringBuilder.toString();
	}
	
	/**
	 * Retrieves only the generation argument values sorted by field
	 * 
	 * @return {@link String} representation of the generation arguments
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public String getFieldValues() throws IllegalArgumentException, IllegalAccessException {
		// retrieve and sort field classes
		Field[] fields = args.getClass().getDeclaredFields();
		Arrays.sort(fields, (f1, f2) -> f1.getName().compareTo(f2.getName()));
		
		// create and append field values
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(fileName).append(",");
		stringBuilder.append(runtime).append(",");
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Object fieldValue = field.get(args);
			stringBuilder.append(fieldValue);
			stringBuilder.append(",");
		}
		
		for (int i = 0; i < results.size(); i++) {
			stringBuilder.append(results.get(i).getResult());
			if (i < results.size() - 1) {
				stringBuilder.append(",");
			}
		}
		stringBuilder.append("\n");
		return stringBuilder.toString();
	}
	

	/**
	 * Save the results as a sorted CSV file
	 */
	public void saveResults(String resultLogFile) {
		File file = new File(resultLogFile);
		String result = "";
		
		// if the file doesn't exist, create the header as well
		// dismiss otherwise
		try {
			if (file.exists()) {
				result = getFieldValues();
			} else {
				result = getResultsAsCSV();
			}
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} 
		
		
		// write to file in append
		try (FileWriter writer = new FileWriter(file, true)) { 
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
