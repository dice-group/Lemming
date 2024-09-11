package org.aksw.simba.lemming.util;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class ParseIguanaResults {

	private static final Property QPS_PROP = ResourceFactory
			.createProperty("http://iguana-benchmark.eu/properties/penalizedQPS");
	private static final Property QMPH_PROP = ResourceFactory
			.createProperty("http://iguana-benchmark.eu/properties/QMPH");
	private static final Property NOQPH_PROP = ResourceFactory
			.createProperty("http://iguana-benchmark.eu/properties/NoQPH");

	private static final Resource CONNECTION_CLASS = ResourceFactory
			.createResource("http://iguana-benchmark.eu/class/Connection");

	private static final Resource DATASET_CLASS = ResourceFactory
			.createResource("http://iguana-benchmark.eu/class/Dataset");

	public static void main(String[] args) {

		String folder = "iguana_results/";

		// linked geo
		String dataset = "LinkedGeo";
		int[] order = { 1, 7, 13, 2, 8, 14, 3, 9, 15, 4, 10, 16, 5, 11, 17, 6, 12, 18, 19, 23, 27, 20, 24, 28, 21, 25,
				29, 22, 26, 30 };
		int queryCount = 42;

//		 swdf
//		String dataset = "SemanticWeb";
//		int[] order = { 1, 7, 13, 2, 8, 14, 3, 9, 15, 4, 10, 16, 5, 11, 17, 6, 12, 18, 19, 23, 27, 20, 24, 28, 21, 25, 29, 22, 26, 30 };
//		int queryCount = 21;

		// icc
//		String dataset = "Geology";
//		int[] order = { 1, 11, 21, 2, 12, 22, 5, 15, 25, 6, 16, 26, 3, 13, 23, 4, 14, 24, 7, 17, 27, 8, 18, 28, 9, 19, 29, 10, 20, 30 };
//		int queryCount = 27;

		int[] blOrder = { 1, 3, 5, 2, 4, 6 };

		double[][] qmphTable = new double[37][5];
		double[][] noqphTable = new double[37][5];
		double[][][] qpsTables = new double[queryCount][37][5];

		// read files from folder
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folder))) {
			for (Path entry : stream) {
				if (Files.isRegularFile(entry)) {
					Model resultFile = ModelFactory.createDefaultModel();
					resultFile.read(entry.toString());
					String inputFile = getFromModel(resultFile, DATASET_CLASS.toString());
					String triplestore = getFromModel(resultFile, CONNECTION_CLASS.toString());
					int column = getColumnIndex(triplestore);
					Integer rowIndex;

					if (!inputFile.contains(dataset)) {
						continue;
					}

					// check if baseline before hand
					if (inputFile.contains("bl_")) {
						rowIndex = findOrderIndex(inputFile, blOrder, 31);
					} else {
						// find row index, if no integer, might mean it's the first one
						rowIndex = findOrderIndex(inputFile, order, 1);
						if (rowIndex == null) {
							if (inputFile.contains("Mimic_" + dataset)) {
								rowIndex = 1;
							}
							if (inputFile.contains("Target_" + dataset)) {
								rowIndex = 0;
							}
						}
					}

					// if it's still not assigned, then something is wrong
					if (rowIndex == null) {
						System.err.println("Something went wrong, skipping");
						continue;
					}

					// parse qmph and noqph
					double qmph = getMetric(resultFile, QMPH_PROP);
					double noqph = getMetric(resultFile, NOQPH_PROP);

					qmphTable[rowIndex][column] = qmph;
					noqphTable[rowIndex][column] = noqph;

					// parse qps
					String experimentID = resultFile
							.listSubjectsWithProperty(ResourceFactory
									.createProperty("http://iguana-benchmark.eu/properties/workerResult"))
							.next().toString();
					for (int i = 0; i < queryCount; i++) {
						Resource queryRes = ResourceFactory.createResource(experimentID + "/sparql" + i);
						try {
							Literal qpsStr = resultFile.listObjectsOfProperty(queryRes, QPS_PROP).next().asLiteral();
							qpsTables[i][rowIndex][column] = qpsStr.getDouble();
						}catch(Exception e) {
							System.err.println("Something wrong with "+queryRes);
						}
						
						
					}
				}
			}
		} catch (IOException | DirectoryIteratorException e) {
			e.printStackTrace();
		}
		System.out.println("QMPH Table:");
		printArrayAsTable(qmphTable);
		System.out.println("NoQPH Table:");
		printArrayAsTable(noqphTable);
		System.out.println("QPS Tables:");
		printArrayAsTable(qpsTables);

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

	private static double getMetric(Model model, Property metric) {
		Literal metricStr = model.listObjectsOfProperty(metric).next().asLiteral();
		return metricStr.getDouble();
	}

	private static String getFromModel(Model model, String type) {
		String queryStr = "SELECT ?p WHERE { ?s a <" + type + "> . "
				+ "?s <http://www.w3.org/2000/01/rdf-schema#label> ?p . }";
		Query query = QueryFactory.create(queryStr);
		String fileName = null;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			if (results.hasNext()) {
				QuerySolution sol = results.nextSolution();
				fileName = sol.get("p").toString();
			}
		}
		return fileName;
	}

}
