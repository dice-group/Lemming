package org.aksw.simba.lemming.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

public class ModelUtil {

	/**
	 * Since, renaming resources in predicate position is not allowed, this method
	 * removed the old statement and adds a new one
	 * 
	 * @param model the RDF Model
	 * @param oldStatement the old statement to be deleted
	 * @param newStatement the new statement to be added
	 */
	public static void replaceStatement(Model model, Statement oldStatement, Statement newStatement) {
		model.remove(oldStatement);
		model.add(newStatement);
	}
	
	public static int combineHash(Object... others) {
		int hash = 1;
	    for (Object cur : others) {
	        hash = hash + (cur != null ? cur.toString().hashCode() : 0);
	    }
	    return hash;
	}
	
	public static int combineHash(Integer... others) {
		int hash = 1;
	    for (int cur : others) {
	        hash += cur;
	    }
	    return hash;
	}

}
