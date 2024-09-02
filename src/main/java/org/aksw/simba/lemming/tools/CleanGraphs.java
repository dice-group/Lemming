package org.aksw.simba.lemming.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class CleanGraphs {

	public static void main(String[] args) {

		// read
		Model model = RDFDataMgr.loadModel(args[0]);

		// remove literal statements
		List<Statement> literalStmts = new ArrayList<>();
		StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			if (stmt.getObject().isLiteral()) {
				 literalStmts.add(stmt);
			}
		}
		model.remove(literalStmts);
		
		// write to file
		try (OutputStream out = new FileOutputStream(args[1])) {
			RDFDataMgr.write(out, model, RDFFormat.TTL);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
