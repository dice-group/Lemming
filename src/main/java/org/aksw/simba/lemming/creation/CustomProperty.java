package org.aksw.simba.lemming.creation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;

//TODO: needs restructuring

public class CustomProperty {

	/**
	 * Uniform name to be used in the generated graph
	 */
	private String name;

	/**
	 * Property with all the updated information of itself and of the equivalent
	 * properties
	 */
	private OntProperty property;

	/**
	 * URIs of the equivalent properties and of the first property to be added
	 */
	private Set<String> equivalentProperties = new HashSet<String>();

	public CustomProperty(OntProperty property) {
		super();
		this.name = property.getURI();
		this.property = property;
		equivalentProperties.add(name);

	}

	/**
	 * @param eqProperties list to add to the node
	 */
	public void addEquivalentGroup(List<? extends OntProperty> eqProperties) {
		for (OntProperty prop : eqProperties) {
			addEquivalent(prop);
		}
	}

	/**
	 * This method does the following 1. Updates the OnProperty object by adding the
	 * domain and range of all the equivalent properties 2. Updates the name to be
	 * used
	 * 
	 * @param ontProperty property to add to the node
	 */
	public void addEquivalent(OntProperty ontProperty) {
		equivalentProperties.add(ontProperty.getURI());

		List<? extends OntResource> domainList = ontProperty.listDomain().toList();
		for (OntResource domain : domainList) {
			if (!property.hasDomain(domain)) {
				property.addDomain(domain);
			}
		}

		List<? extends OntResource> rangeList = ontProperty.listRange().toList();
		for (OntResource range : rangeList) {
			if (!property.hasRange(range)) {
				property.addRange(range);
			}
		}

		// if the property name is "smaller" than the current name, we take it as the
		// new name
		String newName = ontProperty.getURI();
		if (newName.compareTo(name) < 0) {
			setName(newName);
		}
	}

	public boolean isEquivalent(String uri) {
		if (equivalentProperties.contains(uri)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if node already contains a given property or if that given's
	 * property equivalents match the node's equivalents
	 * 
	 * @param ontProperty
	 * @return
	 */
	public boolean isSameProperty(OntProperty ontProperty) {
		if (equivalentProperties.contains(ontProperty.getURI())) {
			return true;
		}
		List<? extends OntProperty> list = null;
		try {
			list = ontProperty.listEquivalentProperties().toList();
		} catch (ConversionException e) {

		}
		if (list != null && !list.isEmpty()) {
			for (OntProperty property : list) {
				if (equivalentProperties.contains(property.toString())) {
					return true;
				}
			}
		}
		return false;
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
	 * @return the equivalentProperties
	 */
	public Set<String> getEquivalentProperties() {
		return equivalentProperties;
	}

	/**
	 * @param equivalentProperties the equivalentProperties to set
	 */
	public void setEquivalentProperties(Set<String> equivalentProperties) {
		this.equivalentProperties = equivalentProperties;
	}

	/**
	 * @return the property
	 */
	public OntProperty getProperty() {
		return property;
	}

	/**
	 * @param property the property to set
	 */
	public void setProperty(OntProperty property) {
		this.property = property;
	}
}
