package org.aksw.simba.lemming.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseIguanaResults {

	public static void main(String[] args) throws IOException {

		String folder = args[0];

		// UCS-UIS1 UCS-UIS2 UCS-UIS3 UCS-BIS1 UCS-BIS2 UCS-BIS3
		// CCS-UIS1 CCS-UIS2 CCS-UIS3 CCS-BIS1 CCS-BIS2 CCS-BIS3
		// BCS-UIS1 BCS-UIS2 BCS-UIS3 BCS-BIS1 BCS-BIS2 BCS-BIS3
		// BPBC1 BPBC2 BPBC3 BPUC1 BPUC2 BPUC3
		// UPBC1 UPBC2 UPBC3 UPUC1 UPUC2 UPUC3

		// initial or optimized
//		String subfolder = "initial";
		String subfolder = "results";
		
		// dataset
		String dataset = "LinkedGeo";
//		String dataset = "SemanticWebDogFood";
//		String dataset = "Geology";
		int queryCount = 0;
		int[] order = null;
		if(dataset.contentEquals("LinkedGeo")) {
			queryCount = 69;
			int[] order2 = { 1,	7,	13,
			2,	8,	14,
			5,	11,	17,
			6,	12,	18,
			3,	9,	15,
			4,	10,	16,
			1,	5,	9, // simplex and baseline
			2,	6,	10,
			3,	7,	11,
			4,	8,	12,
			19,	21,	23,
			20,	22,	24 
			};
			order = order2;
		} else if (dataset.contentEquals("SemanticWebDogFood")) {
			queryCount = 83;
			int[] order2 = { 1,	11,	21,
			2,	12,	22,
			5,	15,	25,
			6,	16,	26,
			3,	13,	23,
			4,	14,	24,
			7,	17,	27,
			8,	18,	28,
			9,	19,	29,
			10,	20,	30,
			37,	39,	41,
			38,	40,	42};
			order = order2;
		} else if (dataset.contentEquals("Geology")) {
			queryCount = 83;
			int[] order2 = { 1,	11,	21,
			2,	12,	22,
			5,	15,	25,
			6,	16,	26,
			3,	13,	23,
			4,	14,	24,
			7,	17,	27,
			8,	18,	28,
			9,	19,	29,
			10,	20,	30,
			37,	39,	41,
			38,	40,	42};
			order = order2;
		} else {
			System.out.println("Dataset Unknown.");
			System.exit(0);
		}

		double[][] qmphTable = new double[37][5];
		double[][] noqphTable = new double[37][5];
		double[][] avgQPSTable = new double[37][5];
		double[][][] qpsTables = new double[queryCount][37][5];

		String suiteSummary = "suite-summary.csv";
		String taskConfig = "task-configuration.csv";

		// list all suite folders
		List<Path> subfolders = listSubfolders(folder);
		
		// read each experiment
		fileLoop: for (Path entry : subfolders) {
			// read task configuration
			Map<String, String> config = readCsvHeaderAsKeys(entry.toString() + "/" + taskConfig);
			String triplestore = config.get("connection");
			String inputFile = config.get("dataset");

			// likely a running experiment
			if (inputFile == null)
				continue;
			
			if (!inputFile.contains("target-10-09-2025")) {
				if (!inputFile.contains(dataset) && !inputFile.contains(dataset.toLowerCase())) {
					continue;
				}
			}
			int column = getColumnIndex(triplestore);
			Integer rowIndex;

			// find row index, if no integer, might mean it's either the target or the first one
			int displacement = 0;
			if(inputFile.contains("simplex")) {
				displacement = 18;
			} 
			
			rowIndex = findOrderIndex(inputFile, order, displacement);
			
			if (rowIndex == null) {
				if (inputFile.contains("target-10-09-2025")) {
					rowIndex = 0;
				} else if (inputFile.substring(inputFile.length() - 6 - dataset.length()).contains("Mimic_" + dataset)) {
					if(inputFile.contains("simplex")) {
						rowIndex = 19;
					} else {
						rowIndex = 1;
					}
				}
			} 
			
			// if it's still not assigned, then something is wrong
			if (rowIndex == null) {
				System.err.println("Something went wrong, skipping: " + inputFile);
				continue;
			}
			
			// except the target graph, does it match initial or optimized?
			if (rowIndex != 0 && !inputFile.contains(subfolder))
				continue;
			

			// read metrics from single csv files
			Map<String, String> summary = readCsvHeaderAsKeys(entry.toString() + "/" + suiteSummary);
			double avgQPS = Double.valueOf(summary.get("AvgQPS"));
			double noQPH = Double.valueOf(summary.get("NoQPH"));
			double qmph = Double.valueOf(summary.get("QMPH"));

			// assign metrics to corresponding table cell
			if (qmphTable[rowIndex][column] != 0 && qmphTable[rowIndex][column] != qmph) {
				System.out.println("Current value: " + qmphTable[rowIndex][column]);
				System.out.println("New value: " + qmph);
				continue fileLoop;
			}
			qmphTable[rowIndex][column] = qmph;
			noqphTable[rowIndex][column] = noQPH;
			avgQPSTable[rowIndex][column] = avgQPS;

			// read queries
			Path path = Paths.get(folder + getQueryFile(dataset));
			List<String> linesQueries = Files.readAllLines(path);
			String q = entry + "/task-0/query-summary-worker-0.csv";
			Map<Integer, Double> resultMap = readFileAndCreateMap(q);
			for (int j = 0; j < linesQueries.size(); j++) {
				double queryQPS = resultMap.get(j);
				qpsTables[j][rowIndex][column] = queryQPS;
			}
		}
		
		// calculate RMSE on single query qps values
		double[][] rmseTable = new double[queryCount][12];
		double[][] rmsreTable = new double[queryCount][12];
		double[][] nrmseTable = new double[queryCount][12];
		for(int qID = 0; qID<rmseTable.length;qID++) {
			 rmseTable[qID]=calculateApproachRMSE(qpsTables[qID]);
			 rmsreTable[qID]=calculateApproachRMSRE(qpsTables[qID]);
			 nrmseTable[qID]=calculateApproachNRMSE(qpsTables[qID]);
		}
		
		System.out.println("QMPH Table:");
		printArrayAsTable(qmphTable);
		System.out.println("NoQPH Table:");
		printArrayAsTable(noqphTable);
		System.out.println("AvgQPS Table:");
		printArrayAsTable(avgQPSTable);
		System.out.println("RMSE QPS Tables:");
		printArrayAsTable(rmseTable);
		System.out.println("NRMSE QPS Tables:");
		printArrayAsTable(nrmseTable);
		System.out.println("RMSRE QPS Tables:");
		printArrayAsTable(rmsreTable);

//		System.out.println("QPS Tables:");
//		printArrayAsTable(qpsTables);

	}
	
	public static double[] calculateApproachRMSRE(double[][] qps) {
        int numApproaches = 12;
        int numRuns = 3;
        int numMetrics = 5;
        double[] rmse = new double[numApproaches];

        // Calculate average QPS for each approach across 3 runs
        for (int approach = 0; approach < numApproaches; approach++) {
            double[] approachSum = new double[numMetrics];
            for (int run = 0; run < numRuns; run++) {
            	int rowIndex = 1 + approach * numRuns + run;
                for (int j = 0; j < numMetrics; j++) {
                    approachSum[j] += qps[rowIndex][j];
                }
            }
            for (int j = 0; j < numMetrics; j++) {
                approachSum[j] /= numRuns;
            }

            // Calculate RMSE against target (row 0)
            double sumSquaredError = 0.0;
            for (int j = 0; j < numMetrics; j++) {
            	if(approachSum[j]==0) {
            		continue;
            	}
                double error = (approachSum[j] - qps[0][j])/qps[0][j];
                sumSquaredError += error * error;
            }
            rmse[approach] = Math.sqrt(sumSquaredError / numMetrics);
        }
        return rmse;
    }
	
	public static double[] calculateApproachRMSE(double[][] qps) {
        int numApproaches = 12;
        int numRuns = 3;
        int numMetrics = 5;
        double[] rmse = new double[numApproaches];

        // Calculate average QPS for each approach across 3 runs
        for (int approach = 0; approach < numApproaches; approach++) {
            double[] approachSum = new double[numMetrics];
            for (int run = 0; run < numRuns; run++) {
            	int rowIndex = 1 + approach * numRuns + run;
                for (int j = 0; j < numMetrics; j++) {
                    approachSum[j] += qps[rowIndex][j];
                }
            }
            for (int j = 0; j < numMetrics; j++) {
                approachSum[j] /= numRuns;
            }

            // Calculate RMSE against target (row 0)
            double sumSquaredError = 0.0;
            for (int j = 0; j < numMetrics; j++) {
            	if(approachSum[j]==0) {
            		continue;
            	}
                double error = approachSum[j] - qps[0][j];
                sumSquaredError += error * error;
            }
            rmse[approach] = Math.sqrt(sumSquaredError / numMetrics);
        }
        return rmse;
    }
	
	public static double[] calculateApproachNRMSE(double[][] qps) {
        int numApproaches = 12;
        int numRuns = 3;
        int numMetrics = 5;
        double[] rmse = new double[numApproaches];

        // Calculate average QPS for each approach across 3 runs
        for (int approach = 0; approach < numApproaches; approach++) {
            double[] approachSum = new double[numMetrics];
            for (int run = 0; run < numRuns; run++) {
            	int rowIndex = 1 + approach * numRuns + run;
                for (int j = 0; j < numMetrics; j++) {
                    approachSum[j] += qps[rowIndex][j];
                }
            }
            for (int j = 0; j < numMetrics; j++) {
                approachSum[j] /= numRuns;
            }

            // Calculate RMSE against target (row 0)
            double sumSquaredError = 0.0;
            for (int j = 0; j < numMetrics; j++) {
            	if(approachSum[j]==0) {
            		continue;
            	}
                double error = approachSum[j] - qps[0][j];
                sumSquaredError += error * error;
            }
            rmse[approach] = Math.sqrt(sumSquaredError / numMetrics);
            rmse[approach] /= (Arrays.stream(qps[0]).max().getAsDouble()-Arrays.stream(qps[0]).min().getAsDouble());
        }
        return rmse;
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
			return "queries/lgeo.benchmark-no.txt";
		case "SemanticWebDogFood":
			return "queries/swdf.benchmark-no.txt";
		case "Geology":
			return "queries/icc.benchmark-no.txt";
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

//	private static void printArrayAsTable(double[][][] array) {
//		for (int i = 0; i < array.length; i++) {
//			System.out.println("Query " + i);
//			printArrayAsTable(array[i]);
//		}
//	}

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
		for (int i = displacement; i < integers.length; i++) {
			int num = integers[i];
			String numStr = "(" + String.valueOf(num) + ")";
			if (input.contains(numStr)) {
				return i+1;
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
