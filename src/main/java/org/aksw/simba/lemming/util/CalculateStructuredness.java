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
 * @see https://github.com/dice-group/triplestore-benchmarks/blob/master/src/main/java/org/aksw/simba/dataset/stats/Structuredness.java
 */
public class CalculateStructuredness {

	public static void main(String[] args) {
		String folderPath = args[0];
		Path startPath = Paths.get(folderPath);
		Path outputFile = Paths.get("structuredness.csv");

		try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
			writer.write("File,Subject,Predicates,Objects,Triples,Structuredness\n");

			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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
	 * @param model      SPARQL endpoint URL
	 * @param namedGraph Named Graph of dataset. Can be null, in that case all named
	 *                   graphs will be considered
	 * @param types
	 * @return structuredness Structuredness or coherence value
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
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
			// System.out.println(typeInstancesSize);
			// System.out.println(type+" predicates: "+typePredicates);
			// System.out.println(type+" : "+typeInstancesSize+" x " +
			// typePredicates.size());
			for (String predicate : typePredicates) {
				long predicateOccurences = getOccurences(predicate, type, model);
				occurenceSum = (occurenceSum + predicateOccurences);
				// System.out.println(predicate+ " occurences: "+predicateOccurences);
				// System.out.println(occurenceSum);
			}

			double denom = typePredicates.size() * typeInstancesSize;
			if (typePredicates.size() == 0)
				denom = 1;
			// System.out.println("Occurence sum = " + occurenceSum);
			// System.out.println("Denom = " + denom);
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
	 * @param types      Set of rdf:types
	 * @param namedGraph Named graph
	 * @return sum Sum of weighted denominator
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
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
	 * @param predicate  Predicate
	 * @param type       Type
	 * @param namedGraph Named Graph
	 * @return predicateOccurences Predicate occurence value
	 * @throws NumberFormatException
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	public static long getOccurences(String predicate, String type, Model model) {
		long predicateOccurences = 0;
		String queryString;
		queryString = "SELECT (Count(Distinct ?s) as ?occurences) \n" + "			WHERE { \n" + "            ?s a <"
				+ type + "> . " + "            ?s <" + predicate + "> ?o" + "           }";

		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			predicateOccurences = Long.parseLong(res.next().get("occurences").asLiteral().getString());
		}
		return predicateOccurences;

	}

	/**
	 * Get the number of distinct instances of a specfici type
	 * 
	 * @param type       Type or class name
	 * @param namedGraph Named graph
	 * @return typeInstancesSize No of instances of type
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static long getTypeInstancesSize(String type, Model model) {
		long typeInstancesSize = 0;
		String queryString = "SELECT (Count(DISTINCT ?s)  as ?cnt ) \n" + "			WHERE { \n" + "            ?s a <"
				+ type + "> . " + "            ?s ?p ?o" + "           }";
		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			typeInstancesSize = Long.parseLong(res.next().get("cnt").asLiteral().getString());
		}
		return typeInstancesSize;
	}

	/**
	 * Get all distinct predicates of a specific type
	 * 
	 * @param type       Type of class
	 * @param namedGraph Named Graph can be null
	 * @return typePredicates Set of predicates of type
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
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
	 * @param namedGraph Named Graph of dataset can be null in that case all
	 *                   namedgraphs will be considered
	 * @param endpoint
	 * @return types Set of rdf:types
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static Set<String> getRDFTypes(Model model) {
		Set<String> types = new HashSet<String>();
		String queryString = "SELECT DISTINCT ?type  \n" + "			WHERE { \n" + "            ?s a ?type"
				+ "           }";

		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			types.add(res.next().get("type").toString());
		}
		return types;
	}

	public static long totalSubjects(Model model) {
		long count = 0;
		String queryString = "SELECT (Count(DISTINCT ?s) as ?total) \n" + "			WHERE { \n" + "            ?s ?p ?o"
				+ "           }";
		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			count = Long.parseLong(res.next().get("total").asLiteral().getString());
		}
		return count;
	}

	public static long totalTriples(Model model) {
		long count = 0;
		String queryString = "SELECT (Count(*) as ?total) \n" + "			WHERE { \n" + "            ?s ?p ?o"
				+ "           }";

		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			count = Long.parseLong(res.next().get("total").asLiteral().getString());
		}
		return count;
	}

	public static long totalPredicates(Model model) {
		long count = 0;
		String queryString = "SELECT (Count(DISTINCT ?p) as ?total) \n" + "			WHERE { \n" + "            ?s ?p ?o"
				+ "           }";

		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			count = Long.parseLong(res.next().get("total").asLiteral().getString());
		}
		return count;
	}

	public static long totalObjeects(Model model) {
		long count = 0;
		String queryString = "SELECT (Count(DISTINCT ?o) as ?total) \n" + "			WHERE { \n" + "            ?s ?p ?o"
				+ "           }";

		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qExe = QueryExecutionFactory.create(query, model);
		ResultSet res = qExe.execSelect();
		while (res.hasNext()) {
			count = Long.parseLong(res.next().get("total").asLiteral().getString());
		}
		return count;
	}
}
