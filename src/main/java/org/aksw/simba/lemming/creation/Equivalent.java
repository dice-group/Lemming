package org.aksw.simba.lemming.creation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an OntClass or an OntProperty and its equivalent classes or
 * properties, respectively.
 *
 * @param <T> OntProperty, OntClass
 */
public class Equivalent<T extends OntResource> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Equivalent.class);

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

	public Equivalent(T current) {
		super();
		this.name = ((Resource) current).getURI();
		this.attribute = current;
		this.equivalents.add(name);
	}

	/**
	 * This method adds the URI to the this.equivalents set and updates the object's
	 * own name. If the object is an OntProperty, the domain and range information
	 * is also added to the class' object
	 * 
	 * @param current OntProperty / OntClass object
	 */
	public void addEquivalent(T current) {
		String uri = ((Resource) current).getURI();
		if (uri != null) {
			this.equivalents.add(uri);
			setName(compareNames(uri, name));

			if (current instanceof OntProperty) {
				List<? extends OntResource> domainList = ((OntProperty) current).listDomain().toList();
				for (OntResource domain : domainList) {
					if (!((OntProperty) this.attribute).hasDomain(domain)) {
						((OntProperty) this.attribute).addDomain(domain);
					}
				}

				List<? extends OntResource> rangeList = ((OntProperty) current).listRange().toList();
				for (OntResource range : rangeList) {
					if (!((OntProperty) this.attribute).hasRange(range)) {
						((OntProperty) this.attribute).addRange(range);
					}
				}
			}
		}
	}

	/**
	 * Adds the elements of a set to object's own set
	 * 
	 * @see addEquivalent(T attribute)
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
		if (((RDFNode) element).isResource() && equivalents.contains(((Resource) element).getURI()))
			return true;

		List<T> list = null;

		try {
			if (element instanceof OntClass) {
				list = (List<T>) ((OntClass) element).listEquivalentClasses().toList();
			}

			if (element instanceof OntProperty) {
				list = (List<T>) ((OntProperty) element).listEquivalentProperties().toList();
			}

		} catch (ConversionException e) {
			return false;
		}
		if (list != null && !list.isEmpty()) {
			for (T current : list) {
				if (equivalents.contains(current.toString())) {
					return true;
				}
			}
		}
		return false;
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
