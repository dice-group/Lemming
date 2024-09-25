package org.aksw.simba.lemming.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseIguanaResults {

	public static void main(String[] args) throws IOException {

		String folder = "v410-iguana-limit10/";

		String subfolder = "initial";
//		String subfolder = "mimic";

		// linked geo
		String dataset = "LinkedGeo";
		int[] order = { 1, 7, 13, 2, 8, 14, 3, 9, 15, 4, 10, 16, 5, 11, 17, 6, 12, 18, 19, 23, 27, 20, 24, 28, 21, 25,
				29, 22, 26, 30 };
		int queryCount = 33;

//		 swdf
//		String dataset = "SemanticWeb";
//		int[] order = { 1, 7, 13, 2, 8, 14, 3, 9, 15, 4, 10, 16, 5, 11, 17, 6, 12, 18, 19, 23, 27, 20, 24, 28, 21, 25,
//				29, 22, 26, 30 };
//		int queryCount = 15;

		// icc
//		String dataset = "Geology";
//		int[] order = { 1, 11, 21, 2, 12, 22, 5, 15, 25, 6, 16, 26, 3, 13, 23, 4, 14, 24, 7, 17, 27, 8, 18, 28, 9, 19, 29, 10, 20, 30 };
//		int queryCount = 23;
//
		int[] blOrder = { 1, 3, 5, 2, 4, 6 };

		double[][] qmphTable = new double[37][5];
		double[][] noqphTable = new double[37][5];
		double[][] avgQPSTable = new double[37][5];
		double[][][] qpsTables = new double[queryCount][37][5];

		String suiteSummary = "suite-summary.csv";
		String taskConfig = "task-configuration.csv";

		// list all suite folders
		List<Path> subfolders = listSubfolders(folder);
		// read each experiment
		for (Path entry : subfolders) {
			String prefix = "";
			// read task configuration
			Map<String, String> config = readCsvHeaderAsKeys(entry.toString() + "/" + taskConfig);
			String triplestore = config.get("connection");
			String inputFile = config.get("dataset");

			// likely a running experiment
			if (inputFile == null)
				continue;

			if (!inputFile.contains(dataset)) {
				continue;
			}

			int column = getColumnIndex(triplestore);
			Integer rowIndex;

			// check if baseline before hand
			if (inputFile.contains("baseline")) {
				rowIndex = findOrderIndex(inputFile, blOrder, 31);
				if (rowIndex == null) {
					if (inputFile.contains("bl_Mimic_" + dataset)) {
						rowIndex = 31;
					}
				}
			} else {
				// find row index, if no integer, might mean it's the first one
				rowIndex = findOrderIndex(inputFile, order, 1);
				if (rowIndex == null) {
					if (inputFile.contains("Mimic_" + dataset)) {
						rowIndex = 1;
						if (!inputFile.contains(subfolder))
							continue;
						prefix = subfolder;
					}
					if (inputFile.contains("Target_" + dataset)) {
						rowIndex = 0;
					}
				} else {
					if (!inputFile.contains(subfolder))
						continue;
					prefix = subfolder;
				}

			}

			// if it's still not assigned, then something is wrong
			if (rowIndex == null) {
				System.err.println("Something went wrong, skipping");
				continue;
			}

			// read metrics from single csvs
			Map<String, String> summary = readCsvHeaderAsKeys(entry.toString() + "/" + suiteSummary);
			double avgQPS = Double.valueOf(summary.get("AvgQPS"));
			double noQPH = Double.valueOf(summary.get("NoQPH"));
			double qmph = Double.valueOf(summary.get("QMPH"));

			// assign metrics to corresponding table cell
			qmphTable[rowIndex][column] = qmph;
			noqphTable[rowIndex][column] = noQPH;
			avgQPSTable[rowIndex][column] = avgQPS;
			

			// read queries
			Path path = Paths.get(folder + getQueryFile(dataset));
			List<String> linesQueries = Files.readAllLines(path);

			// read query instances
			String instance;
			if (prefix.isEmpty()) {
				instance = inputFile.substring(inputFile.lastIndexOf('/') + 1) + "_queries.txt";
			} else {
				instance = prefix + "_" + inputFile.substring(inputFile.lastIndexOf('/') + 1) + "_queries.txt";
			}
			
			String filePath = folder + "queries/" + instance;
			File file = new File(filePath);
			if (!file.exists())
				continue;
			List<String> linesInstances = Files.readAllLines(Paths.get(filePath));

			Map<Integer, Set<Integer>> queries2Instances = new HashMap<>();

			// map instances to queries
			Set<String> procQueries = new HashSet<>();
			for (int j = 0; j < linesQueries.size(); j++) {
				String query = linesQueries.get(j);
				Set<Integer> instances = new HashSet<>();

				// create regex for query pattern
				String regexTemplate = escapeRegexSpecialChars(query);
				regexTemplate = regexTemplate.replaceAll(Pattern.quote("%%var") + "\\d+" + Pattern.quote("%%"),
						"(.*?)");
				Pattern pattern = Pattern.compile(regexTemplate, Pattern.DOTALL);

				for (int i = 0; i < linesInstances.size(); i++) {
					String candidateInstance = linesInstances.get(i);
					// if exactly the same, we found it, no need to search more
					if (query.contentEquals(candidateInstance)&& !procQueries.contains(candidateInstance)) {
						procQueries.add(candidateInstance);
						instances.add(i);
						break;
					}
					// then it's a pattern
					else {
						Matcher matcher = pattern.matcher(candidateInstance);
						if (matcher.matches() && !procQueries.contains(candidateInstance)) {
							procQueries.add(candidateInstance);
							instances.add(i);
						}
					}
					
				}
				
				queries2Instances.put(j, instances);
			}

			// sanity check
			// sum of map keys should be the same as length of instances
			int totalElements = queries2Instances.values().stream().mapToInt(Set::size) // Get the size of each set
					.sum();
			if (totalElements != linesInstances.size())
				System.out.println("Something wrong happened.");

			// read query-level metrics
			String q = entry + "/task-0/query-summary-worker-0.csv";
			Map<Integer, Double> resultMap = readFileAndCreateMap(q);

			// compute average of instances
			Set<Integer> keys = queries2Instances.keySet();
			for (int qID : keys) {
				Set<Integer> instances = queries2Instances.get(qID);
				double avg = 0;
				if (!instances.isEmpty()) {
					for (int qInst : instances) {
						double queryQPS = resultMap.get(qInst);
						avg += queryQPS;
					}
					avg /= instances.size();
				}
				qpsTables[qID][rowIndex][column] = avg;
			}

		}

		System.out.println("QMPH Table:");
		printArrayAsTable(qmphTable);
		System.out.println("NoQPH Table:");
		printArrayAsTable(noqphTable);
		System.out.println("AvgQPS Table:");
		printArrayAsTable(avgQPSTable);
		System.out.println("QPS Tables:");
		printArrayAsTable(qpsTables);

	}

	public static Map<Integer, Double> readFileAndCreateMap(String filePath) {
		Map<Integer, Double> queryIDToQPS = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			// Skip the header
			br.readLine();

			// Read the file line by line
			while ((line = br.readLine()) != null) {
				// Split the line by comma
				String[] values = line.split(",");

				// Parse queryID (Integer) and QPS (Double)
				int queryID = Integer.parseInt(values[0]);
				double QPS = Double.parseDouble(values[8]);

				// Add to map
				queryIDToQPS.put(queryID, QPS);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return queryIDToQPS;
	}

	public static String escapeRegexSpecialChars(String str) {
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			// Escape regex special characters
			if ("\\.^$|?*+()[]{}".indexOf(c) != -1) {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}

	private static String getQueryFile(String dataset) {
		switch (dataset) {
		case "LinkedGeo":
			return "queries/lgeo_queries.txt";
		case "SemanticWeb":
			return "queries/swdf_queries.txt";
		case "Geology":
			return "queries/icc_queries.txt";
		default:
			return null;
		}
	}

	public static Map<String, String> readCsvHeaderAsKeys(String csvFilePath) {
		Map<String, String> csvData = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
			String headerLine = br.readLine(); // Read the header line
			String dataLine = br.readLine(); // Read the data line

			if (headerLine != null && dataLine != null) {
				String[] headers = headerLine.split(","); // Split the header line by comma
				String[] values = dataLine.split(","); // Split the data line by comma

				// Map headers to corresponding values
				for (int i = 0; i < headers.length; i++) {
					String value = values[i].trim().replaceAll("^\"|\"$", ""); // Remove surrounding quotes
					String key = headers[i].trim().replaceAll("^\"|\"$", "");
					csvData.put(key, value);
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading CSV file: " + e.getMessage());
		}

		return csvData;
	}

	public static List<Path> listSubfolders(String folderPath) throws IOException {
		List<Path> subfolders = new ArrayList<>();
		Path path = Paths.get(folderPath);

		// Traverse the folder to get only directories
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					subfolders.add(entry);
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading folder: " + e.getMessage());
			throw e;
		}
		return subfolders;
	}

	private static void printArrayAsTable(double[][][] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.println("Query " + i);
			printArrayAsTable(array[i]);
		}
	}

	private static void printArrayAsTable(double[][] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.printf("%f\t", array[i][j]);
			}
			// next row
			System.out.println();
		}
	}

	private static Integer findOrderIndex(String input, int[] integers, int displacement) {
		for (int i = 0; i < integers.length; i++) {
			int num = integers[i];
			String numStr = "(" + String.valueOf(num) + ")";
			if (input.contains(numStr)) {
				return i + displacement;
			}
		}
		return null;
	}

	private static int getColumnIndex(String triplestore) {
		switch (triplestore) {
		case "Tentris":
			return 0;
		case "Virtuoso":
			return 1;
		case "Blazegraph":
			return 2;
		case "Fuseki":
			return 3;
		case "GraphDB":
			return 4;
		default:
			return 5;
		}
	}
}
