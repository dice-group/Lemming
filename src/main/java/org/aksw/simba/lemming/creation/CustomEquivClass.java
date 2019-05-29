package org.aksw.simba.lemming.creation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.OntClass;

//TODO: needs restructuring

public class CustomEquivClass {
	
	private String name;
	private OntClass ontClass;
	private Set<String> equivalentClasses = new HashSet<>();
	
	public CustomEquivClass(OntClass ontClass) {
		super();
		this.name = ontClass.getURI();
		this.ontClass = ontClass;
		this.equivalentClasses.add(name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the equivalentClasses
	 */
	public Set<String> getEquivalentClasses() {
		return equivalentClasses;
	}
	
	
	public void addEquivalentClass(OntClass curClass) {
		String classURI = curClass.getURI();
		if (classURI != null) {
			this.equivalentClasses.add(classURI);
			setName(getStandardName(classURI, name));
		}
		
	}
	
	public void addEquivalentClass(Set<OntClass> equivalentClass) {
//		this.equivalentClasses.addAll(equivalentClass);
		for(OntClass eqClass: equivalentClass) {
			addEquivalentClass(eqClass);
		}
	}
	
	public boolean containsClass(OntClass ontClass) {
		if(ontClass.isResource() && equivalentClasses.contains(ontClass.getURI()))
			return true;
		
		List<OntClass> list = null;
		try {
			list = ontClass.listEquivalentClasses().toList();
		} catch (ConversionException e) {

		}
		if (list != null && !list.isEmpty()) {
			for (OntClass curClass : list) {
				if (equivalentClasses.contains(curClass.toString())) {
					return true;
				}
			}
		}
		return false;		
	}
	
	public String getStandardName(String newName, String oldName) {
		if (newName.compareTo(oldName) < 0) {
			return oldName;
		}
		return newName;
	}

	/**
	 * @return the ontClass
	 */
	public OntClass getOntClass() {
		return ontClass;
	}	
}
