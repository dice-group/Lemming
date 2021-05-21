package org.aksw.simba.lemming.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.creation.GraphReverter;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

public class PrintDegreeUtil {
	
	/**
	 * Calculates in and out-degree for all resources in a model,
	 * by converting them to a coloured graph and removing the type edges.
	 * and prints it to .csv file using {@link printMap(Map<String, Integer> map, String header, String fileName)}

	 * @param model
	 * @param dataset
	 */
	public static void printInOutDegrees(Model model, String dataset) {
		GraphCreator creator = new GraphCreator();
		ColouredGraph colouredGraph = creator.processModel(model);
		GraphReverter reverter = new GraphReverter(colouredGraph, ModelFactory.createDefaultModel());
		Model revertedGraph = reverter.processGraph();
		
		Map<String, Integer> outDegree = new HashMap<String, Integer>();
		Map<String, Integer> outDegreeLiteralIncluded = new HashMap<String, Integer>();
		Map<String, Integer> inDegree = new HashMap<String, Integer>();
		
		Model removedTypeModel = ModelFactory.createDefaultModel();
		removedTypeModel.add(revertedGraph);
		StmtIterator iter = revertedGraph.listStatements();
		while(iter.hasNext()) {
			Statement curStmt = iter.next();
			if(curStmt.getPredicate().equals(RDF.type))
				removedTypeModel.remove(curStmt);
		}

		Set<Resource> resources = removedTypeModel.listSubjects().toSet();
		NodeIterator nodeIterator = removedTypeModel.listObjects();
		while(nodeIterator.hasNext()) {
			RDFNode curRes = nodeIterator.next();
			if(curRes.isResource())
				resources.add(curRes.asResource());
		}
		
		for(Resource curRes: resources) {
			Set<Statement> outTriples = removedTypeModel.listStatements(curRes, null, (RDFNode)null).toSet();
			int tripleCount = outTriples.size();
			outDegreeLiteralIncluded.put(curRes.toString(), tripleCount);
			
			//dismiss the statements whose objects are literals
			for(Statement curStmt: outTriples) {
				if(curStmt.getObject().isLiteral())
					tripleCount--;
			}
			outDegree.put(curRes.toString(), tripleCount);
			inDegree.put(curRes.toString(), Iterators.size(removedTypeModel.listStatements(null, null, curRes)));
		}
		
		printMap(outDegreeLiteralIncluded, "outDegree", dataset+"_outDegree.csv");
		printMap(outDegree, "outDegree without Literals", dataset+"_outDegreeWithoutLiterals.csv");
		printMap(inDegree, "inDegree", dataset+"_inDegree.csv");
	}
	
	/**
	 * Prints map into csv file, string delimited by double quotes
	 * @param map
	 * @param header csv file header
	 * @param fileName file path
	 */
	public static void printMap(Map<String, Integer> map, String header, String fileName) {
		Set<Integer> ordf = new HashSet<Integer>(map.values());
		List<Integer> orderedCounts = new ArrayList<Integer>(ordf);
		Collections.sort(orderedCounts, Collections.reverseOrder());
		
		FileWriter fileWriter = null;
		String NEW_LINE_SEPARATOR="\n";
		String COMMA_DELIMITER=", ";
		String QUOTES = "\"";

        try {
            fileWriter = new FileWriter(fileName);
            fileWriter.append(header).append(NEW_LINE_SEPARATOR);
            StringBuilder builder = new StringBuilder();
            for (int curCount : orderedCounts) {
            	map.forEach((key, value)->{
                	if(value==curCount) {
                		if(key!=null)
                			key.replace("\"","\\\"");
                		builder.append(value).append(COMMA_DELIMITER).append(QUOTES).append(key).append(QUOTES).append(NEW_LINE_SEPARATOR);	
                	}
                });
            }
            fileWriter.append(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
}
