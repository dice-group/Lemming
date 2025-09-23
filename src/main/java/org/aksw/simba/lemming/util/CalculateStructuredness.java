package org.aksw.simba.lemming.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Calculates the structuredness
 * 
 * @see https://github.com/dice-group/triplestore-benchmarks/blob/master/src/main/java/org/aksw/simba/dataset/stats/Structuredness.java
 */
public class CalculateStructuredness {

	public static void main(String[] args) {
		String folderPath = args[0];
		Path startPath = Paths.get(folderPath);
		Path outputFile = Paths.get(folderPath+"/structuredness.tsv");

		try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
			writer.write("File\tSubject\tPredicates\tObjects\tTriples\tStructuredness\n");

			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					try {
						String path = file.toAbsolutePath().toString();
						Model model = ModelFactory.createDefaultModel();
						model.read(path);
						double coherence = getStructurednessValue(model);
						StringBuilder builder = new StringBuilder();
						builder.append(path).append("\t");
						builder.append(totalSubjects(model)).append("\t");
						builder.append(totalPredicates(model)).append("\t");
						builder.append(totalObjeects(model)).append("\t");
						builder.append(totalTriples(model)).append("\t");
						builder.append(coherence).append("\n");
						writer.write(builder.toString());
						writer.flush();
						System.out.println(path+"\t"+coherence);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the structuredness/coherence value [0,1] of a dataset
	 * 
	 * @param model model
	 */
	public static double getStructurednessValue(Model model) {

		Set<String> types = getRDFTypes(model);
		System.out.println("Total rdf:types: " + types.size());
		double weightedDenomSum = getTypesWeightedDenomSum(types, model);
		double structuredness = 0;
		long count = 1;
		for (String type : types) {
			long occurenceSum = 0;
			Set<String> typePredicates = getTypePredicates(type, model);
			long typeInstancesSize = getTypeInstancesSize(type, model);
			;
			for (String predicate : typePredicates) {
				long predicateOccurences = getOccurences(predicate, type, model);
				occurenceSum = (occurenceSum + predicateOccurences);
			}

			double denom = typePredicates.size() * typeInstancesSize;
			if (typePredicates.size() == 0)
				denom = 1;
			double coverage = occurenceSum / denom;
			System.out.println("\n" + count + " : Type: " + type);
			System.out.println("Coverage : " + coverage);
			double weightedCoverage = (typePredicates.size() + typeInstancesSize) / weightedDenomSum;
			System.out.println("Weighted Coverage : " + weightedCoverage);
			structuredness = (structuredness + (coverage * weightedCoverage));
			count++;
		}

		return structuredness;
	}

	/**
	 * Get the denominator of weighted sum all types. Please see Duan et. all paper
	 * apple oranges
	 * 
	 * @param types Set of rdf:types
	 * @return sum Sum of weighted denominator
	 */
	public static double getTypesWeightedDenomSum(Set<String> types, Model model) {
		double sum = 0;
		for (String type : types) {
			long typeInstancesSize = getTypeInstancesSize(type, model);
			long typePredicatesSize = getTypePredicates(type, model).size();
			sum = sum + typeInstancesSize + typePredicatesSize;
		}
		return sum;
	}

	/**
	 * Get occurences of a predicate within a type
	 * 
	 * @param predicate Predicate
	 * @param type      Type
	 * @return model model
	 */
	public static long getOccurences(String predicate, String type, Model model) {
		String queryString = "SELECT (Count(Distinct ?s) as ?total) \n" + "			WHERE { \n" + "            ?s a <"
				+ type + "> . " + "            ?s <" + predicate + "> ?o" + "           }";
		return queryModel(queryString, model);

	}

	/**
	 * Get the number of distinct instances of a specfici type
	 * 
	 * @param type  Type or class name
	 * @param model model
	 * @return typeInstancesSize No of instances of type
	 */
	public static long getTypeInstancesSize(String type, Model model) {
		String queryString = "SELECT (Count(DISTINCT ?s)  as ?total ) \n" + "			WHERE { \n" + "            ?s a <"
				+ type + "> . " + "            ?s ?p ?o" + "           }";
		return queryModel(queryString, model);
	}

	/**
	 * Get all distinct predicates of a specific type
	 * 
	 * @param type  Type of class
	 * @param model model
	 * @return typePredicates Set of predicates of type
	 */
	public static Set<String> getTypePredicates(String type, Model model) {
		Set<String> typePredicates = new HashSet<String>();
		String queryString = "SELECT DISTINCT ?typePred \n" + "			WHERE { \n" + "            ?s a <" + type
				+ "> . " + "            ?s ?typePred ?o" + "           }";

		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			String predicate = res.next().get("typePred").toString();
			if (!predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
				typePredicates.add(predicate);
		}
		return typePredicates;
	}

	/**
	 * Get distinct set of rdf:type
	 * 
	 * @param model model
	 * @return types Set of rdf:types
	 */
	public static Set<String> getRDFTypes(Model model) {
		Set<String> types = new HashSet<String>();
		String queryString = "SELECT DISTINCT ?type  \n" + "			WHERE { \n" + "            ?s a ?type"
				+ "           }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			types.add(res.next().get("type").toString());
		}
		return types;
	}

	public static long totalSubjects(Model model) {
		String queryString = "SELECT (Count(DISTINCT ?s) as ?total) \n" + "			WHERE { \n"
				+ "            ?s ?p ?o }";
		return queryModel(queryString, model);
	}

	public static long totalTriples(Model model) {
		String queryString = "SELECT (Count(*) as ?total) \n" + "			WHERE { \n" + "            ?s ?p ?o }";
		return queryModel(queryString, model);
	}

	public static long totalPredicates(Model model) {
		String queryString = "SELECT (Count(DISTINCT ?p) as ?total) \n" + "			WHERE { \n"
				+ "            ?s ?p ?o }";
		return queryModel(queryString, model);
	}

	public static long totalObjeects(Model model) {
		String queryString = "SELECT (Count(DISTINCT ?o) as ?total) \n" + "			WHERE { \n"
				+ "            ?s ?p ?o }";
		return queryModel(queryString, model);
	}

	public static long queryModel(String queryString, Model model) {
		long count = 0;
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			count = Long.parseLong(res.next().get("total").asLiteral().getString());
		}
		return count;
	}
}
