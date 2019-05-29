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
 * 
 * @author ana
 *
 * @param <T>  OntProperty, OntClass
 */
public class Equivalent<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Equivalent.class);
	
	/**
	 * single name to characterize all its equivalents
	 */
	private String name;
	
	/**
	 * should be instanceof OntProperty / OntClass
	 */
	private T attribute;
	
	/**
	 * URIs of all equivalents
	 */
	private Set<String> equivalents = new HashSet<>();
	
	
	public Equivalent(T attribute) {
		super();
		this.name = ((Resource) attribute).getURI();
		this.attribute = attribute;
		this.equivalents.add(name);
	}
	
	public void addEquivalent(T attribute) {
		String classURI = ((Resource) attribute).getURI();
		if (classURI != null) {
			this.equivalents.add(classURI);
			setName(getStandardName(classURI, name));
			
			if(attribute instanceof OntProperty) {
				List<? extends OntResource> domainList = ((OntProperty) attribute).listDomain().toList();
				for (OntResource domain : domainList) {
					if (!((OntProperty) this.attribute).hasDomain(domain)) {
						((OntProperty) this.attribute).addDomain(domain);
					}
				}

				List<? extends OntResource> rangeList = ((OntProperty) attribute).listRange().toList();
				for (OntResource range : rangeList) {
					if (!((OntProperty) this.attribute).hasRange(range)) {
						((OntProperty) this.attribute).addRange(range);
					}
				}
			}			
		}
	}
	
	public void addEquivalentGroup(Set<T> set) {
		for(T element: set) {
			addEquivalent(element);
		}
	}
	
	public boolean containsElement(T element) {
		if(((RDFNode) element).isResource() && equivalents.contains(((Resource) element).getURI()))
			return true;
		
		List<T> list = null;
		
		try {
			if(element instanceof OntClass) {
				list = (List<T>) ((OntClass) element).listEquivalentClasses().toList();
			}
			
			if(element instanceof OntProperty) {
				list = (List<T>) ((OntProperty) element).listEquivalentProperties().toList();
			}
			
		} catch (ConversionException e) {
			LOGGER.warn("The equivalents of "+ element.toString()+" are not specified as " +element.getClass().getCanonicalName());
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
	
	public boolean isSameProperty(OntProperty ontProperty) {
		if (equivalents.contains(ontProperty.getURI())) {
			return true;
		}
		List<? extends OntProperty> list = null;
		try {
			list = ontProperty.listEquivalentProperties().toList();
		} catch (ConversionException e) {

		}
		if (list != null && !list.isEmpty()) {
			for (OntProperty property : list) {
				if (equivalents.contains(property.toString())) {
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
