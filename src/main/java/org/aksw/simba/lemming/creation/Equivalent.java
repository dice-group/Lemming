package org.aksw.simba.lemming.creation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;

/**
 * Represents an OntClass or an OntProperty and its equivalent classes or
 * properties, respectively.
 *
 * @param <T> OntProperty, OntClass
 */
public class Equivalent<T extends OntResource> {

	/**
	 * single name to characterize all its equivalents
	 */
	private String name;

	/**
	 * should be instanceof OntProperty or OntClass
	 */
	private T attribute;

	/**
	 * URIs of all equivalents
	 */
	private Set<String> equivalents = new HashSet<>();

	/**
	 * all equivalent-resources
	 */
	private Set<T> equiResources = new HashSet<>();

	public Equivalent(T current) {
		super();
		this.name = current.getURI();
		this.attribute = current;
		this.equivalents.add(name);
		this.equiResources.add(current);
	}

	/**
	 * This method adds the URI to the this.equivalents set and updates the object's
	 * own name. If the object is an OntProperty, the domain and range information
	 * is also added to the class' object
	 * 
	 * @param current OntProperty / OntClass object
	 */
	public void addEquivalent(T current) {
		String uri = current.getURI();
		
		if (uri != null) {
			if(this.equivalents.add(uri)){
				this.equiResources.add(current);
			}
			setName(compareNames(uri, name));

			if (current.isProperty()) {
				OntProperty currentProperty = current.asProperty();
				
				List<? extends OntResource> domainList = currentProperty.listDomain().toList();
				for (OntResource domain : domainList) {
					if (!attribute.asProperty().hasDomain(domain)) {
						attribute.asProperty().addDomain(domain);
					}
				}

				List<? extends OntResource> rangeList = currentProperty.listRange().toList();
				for (OntResource range : rangeList) {
					if (!attribute.asProperty().hasRange(range)) {
						attribute.asProperty().addRange(range);
					}
				}
			}
		}
	}

	/**
	 * Adds the elements of a set to object's own set
	 *
	 * @param set set of OntProperty / OntClass objects to be added
	 */
	public void addEquivalentGroup(Set<T> set) {
		for (T element : set) {
			addEquivalent(element);
		}
	}

	/**
	 * Returns <code>true</code> if the object contains any relation to the given
	 * element.
	 * 
	 * @param element given element to check for comparison
	 * @return <code>true</code> if the set contains the element's URI or any of the
	 *         equivalents classes/properties URI <code>false</code> otherwise
	 */
	@SuppressWarnings("unchecked")
	public boolean containsElement(T element) {
		if (element.isURIResource() && equivalents.contains(element.getURI()))
			return true;

		List<T> list = null;

		try {
			if (element.isClass()) {
				list = (List<T>) element.asClass().listEquivalentClasses().toList();
			}

			if (element.isProperty()) {
				list = (List<T>) element.asProperty().listEquivalentProperties().toList();
			}

		} catch (ConversionException e) {
			return false;
		}
		if (list != null && !list.isEmpty()) {
			for (T current : list) {
				if (equivalents.contains(current.getURI())) {
					return true;
				}
			}
		}
		return false;
	}

	public Set<T> getEquiResources(){
		return this.equiResources;
	}

	/**
	 * @param newName
	 * @param oldName
	 * @return the lexicographically first String
	 */
	public String compareNames(String newName, String oldName) {
		if (newName.compareTo(oldName) < 0) {
			return oldName;
		}
		return newName;
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
	 * @return the attribute
	 */
	public T getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(T attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the equivalents
	 */
	public Set<String> getEquivalents() {
		return equivalents;
	}

	/**
	 * @param equivalents the equivalents to set
	 */
	public void setEquivalents(Set<String> equivalents) {
		this.equivalents = equivalents;
	}
}
