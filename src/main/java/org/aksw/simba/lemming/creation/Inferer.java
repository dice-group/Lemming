package org.aksw.simba.lemming.creation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Stack;

import org.aksw.simba.lemming.util.ModelUtil;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Inferer class implements an inference of the type of subjects and objects
 * of a given RDF Model from a given Ontology. To use this class, one could
 * first use the readOntology() method and then use the process() method.
 *
 */
public class Inferer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Inferer.class);

	public Inferer() {

	}

	/**
	 * This method creates a new model with all the statements as sourceModel and
	 * goes on to populate it further with inferred triples
	 * 
	 * @param sourceModel RDF Model where we want the inference to take place
	 * @param ontModel    Ontology Model
	 * @return The new model with the same triples as the sourceModel plus the
	 *         inferred triples.
	 */
	public Model process(Model sourceModel, OntModel ontModel) {
		Model newModel = ModelFactory.createDefaultModel();
		newModel.add(sourceModel);
		Set<Resource> set = extractUniqueResources(newModel);
		if (ontModel != null) {
			// collect the equivalent properties and classes information from the ontology
			Map<String, Equivalent<OntClass>> classes = searchClassesInOntology(ontModel);
			Map<String, Equivalent<OntProperty>> uriNodeMap = searchEqPropertiesInOnt(ontModel);

			// infer type statements, a single property name is also enforced here
			iterateStmts(newModel, sourceModel, ontModel, uriNodeMap);
			checkEmptyTypes(set, newModel);

			// uniform the names of the classes
			renameClasses(newModel, classes);

		}
		return newModel;
	}

	/**
	 * 
	 * This method gets all the unique subjects and objects of a model with the
	 * exception of the objects that are not resources. It is mainly used to do a
	 * before and after count of how many resources do not have a type.
	 * 
	 * @param model RDF Model from where the resources are extracted
	 * @return the set of resources of the given model
	 */
	private Set<Resource> extractUniqueResources(Model model) {
		Set<Resource> set = new HashSet<>();
		List<Statement> statements = model.listStatements().toList();
		for (Statement curStat : statements) {
			set.add(curStat.getSubject());
			if (curStat.getObject().isURIResource()) {
				set.add(curStat.getObject().asResource());
			}
		}
		checkEmptyTypes(set, model);
		return set;
	}

	/**
	 * This method simply logs the count of how many resources without a type exist
	 * in a given model
	 * 
	 * @param set   group of resources that we want to check in the model if a type
	 *              relation is existing or not
	 * @param model RDF Model where this needs to be checked in
	 */
	private void checkEmptyTypes(Set<Resource> set, Model model) {
		int emptyTypeCount = 0;
		for (Resource resource : set) {
			if (!model.contains(resource, RDF.type)) {
				emptyTypeCount++;
			}
		}
		LOGGER.info("Number of resources without type : " + emptyTypeCount);
	}

	/**
	 * This method iterates through the model's statements, continuously searching
	 * for each property in the ontology and adding the inferred triples to the new
	 * model
	 * 
	 * @param newModel    model where we will add the new triples
	 * @param sourceModel provided model where we iterate through the statements
	 * @param ontModel    the ontology model
	 */
	public void iterateStmts(Model newModel, Model sourceModel, OntModel ontModel, Map<String, Equivalent<OntProperty>> uriNodeMap) {
		List<Statement> stmts = sourceModel.listStatements().toList();
		for (Statement curStatement : stmts) {
			Set<Statement> newStmts = searchType(curStatement, newModel, uriNodeMap);
			// searchType(curStatement, ontModel, newModel);
			newModel.add(newStmts.toArray(new Statement[newStmts.size()]));
		}
	}

	/**
	 * For a given statement, this method searches for the predicate of a model
	 * inside the Ontology. If found in the Ontology, it then extracts the domain
	 * and range. Creating and adding a new triple with the inferred type to the
	 * model.
	 * 
	 * @param statement statement in which we want to check the predicate in the
	 *                  ontology
	 * @param ontModel  the ontology model
	 * @param newModel  where we add the new triples and therefore, where we check
	 *                  if the statement is already existing in the model or not
	 * @return a set of statements inferred from one property
	 */
	private Set<Statement> searchType(Statement statement, OntModel ontModel, Model newModel) {
		Set<Statement> newStmts = new HashSet<>();
		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();

		// search for the predicate of the model in the ontology
		OntProperty property = ontModel.getOntProperty(predicate.toString());

		if (property != null) {
			List<? extends OntResource> domain = property.listDomain().toList();
			for (OntResource curResource : domain) {
				Statement subjType = ResourceFactory.createStatement(subject, RDF.type, curResource);
				if (!newModel.contains(subjType)) {
					newStmts.add(subjType);
				}
			}
			if (object.isResource()) {
				List<? extends OntResource> range = property.listRange().toList();
				for (OntResource curResource : range) {
					Statement objType = ResourceFactory.createStatement(object.asResource(), RDF.type, curResource);
					if (!newModel.contains(objType)) {
						newStmts.add(objType);
					}
				}
			}
		}
		return newStmts;
	}

	/**
	 * Same as searchType(Statement statement, OntModel ontModel, Model newModel),
	 * but in our custom objects: For a given statement, this method searches for
	 * the predicate of a model inside the Ontology. If found in the Ontology, it
	 * then extracts the domain and range. Creating and adding a new triple with the
	 * inferred type to the model.
	 * 
	 * @param statement  statement in which we want to check the predicate in the
	 *                   ontology
	 * @param newModel   where we add the new triples and therefore, where we check
	 *                   if the statement is already existing in the model or not
	 * @param uriNodeMap
	 * @return a set of statements inferred from a property
	 */
	private Set<Statement> searchType(Statement statement, Model newModel, Map<String, Equivalent<OntProperty>> uriNodeMap) {
		Set<Statement> newStmts = new HashSet<>();
		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();

		Equivalent<OntProperty> node = uriNodeMap.get(predicate.toString());
		OntProperty property = null;

		if (node != null) {
			property = (OntProperty) node.getAttribute();
			Property newPredicate = ResourceFactory.createProperty(node.getName());

			ModelUtil.replaceStatement(newModel, statement,
					ResourceFactory.createStatement(subject, newPredicate, object));
		}

		if (property != null) {
			List<? extends OntResource> domain = property.listDomain().toList();
			for (OntResource curResource : domain) {
				Statement subjType = ResourceFactory.createStatement(subject, RDF.type, curResource);
				if (!newModel.contains(subjType)) {
					newStmts.add(subjType);
				}
			}
			if (object.isResource()) {
				List<? extends OntResource> range = property.listRange().toList();
				for (OntResource curResource : range) {
					Statement objType = ResourceFactory.createStatement(object.asResource(), RDF.type, curResource);
					if (!newModel.contains(objType)) {
						newStmts.add(objType);
					}
				}
			}
		}
		return newStmts;
	}

	/**
	 * This method reads the ontology file with an InputStream
	 * 
	 * @param filePath path to the ontology file
	 * @return OntModel Object
	 */
	public OntModel readOntology(String filePath, String base) {
		if (base == null)
			base = "RDF/XML";
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		try (InputStream inputStream = FileManager.get().open(filePath)) {
			if (inputStream != null) {
				ontModel.read(inputStream, "RDF/XML");
			}
		} catch (IOException e) {
			LOGGER.error("Couldn't read ontology file. Returning empty ontology model.", e);
		}

		return ontModel;
	}

	/**
	 * This method aims to collect the properties information in a given ontology
	 * such as: equivalentProperty, domain and range. It collects it into
	 * CustomProperty objects that can later be accessed directly.
	 * 
	 * @param ontModel the ontology model
	 * @return the map between each URI and the respective object
	 */
	public Map<String, Equivalent<OntProperty>> searchEqPropertiesInOnt(OntModel ontModel) {

		Map<String, Equivalent<OntProperty>> uriNodeMap = new HashMap<String, Equivalent<OntProperty>>();

		List<OntProperty> ontProperties = ontModel.listAllOntProperties().toList();

		Stack<OntProperty> stack = new Stack<OntProperty>();
		stack.addAll(ontProperties);

		Map<String, Boolean> visitedMap = ontProperties.stream()
				.collect(Collectors.toMap(OntProperty::toString, visited -> false));

		Set<Equivalent<OntProperty>> properties = new HashSet<Equivalent<OntProperty>>();

		while (stack.size() > 0) {
			OntProperty curProperty = stack.pop();
			boolean isSame = false;
			boolean isVisited = visitedMap.get(curProperty.toString());
			if (!isVisited) {
				List<? extends OntProperty> eqsList = null;
				try {
					eqsList = curProperty.listEquivalentProperties().toList();
				} catch (ConversionException e) {
					LOGGER.warn("The Equivalent classes of {} probably do not have rdf:type owl:Class or equivalent",
							curProperty.toString());
				}

				if (eqsList != null && !eqsList.isEmpty()) {
					eqsList.forEach(property -> {
						visitedMap.putIfAbsent(property.toString(), false);
					});
					stack.addAll(eqsList);
				}

				// check to which node do we need to add this info to
				Iterator<Equivalent<OntProperty>> propIterator = properties.iterator();
				while (propIterator.hasNext()) {
					Equivalent<OntProperty> curNode = propIterator.next();
					isSame = curNode.containsElement(curProperty);
					if (isSame) {
						curNode.addEquivalent(curProperty);
						uriNodeMap.put(curProperty.toString(), curNode);
						break;
					}
				}

				// if not, create new one
				if (!isSame) {
					Equivalent<OntProperty> node = new Equivalent<OntProperty>(curProperty);
					if (eqsList != null) {
						node.addEquivalentGroup(eqsList.stream().collect(Collectors.toSet()));
					}
					properties.add(node);
					uriNodeMap.put(curProperty.toString(), node);
				}
				visitedMap.put(curProperty.toString(), true);
			}
		}
		return uriNodeMap;
	}

	/**
	 * This method aims to collect the classes information in a given ontology such
	 * as: equivalentClass. It collects it into CustomEquivClass objects that can
	 * later be accessed directly.
	 * 
	 * @param ontModel the ontology model
	 * @return the map between each resource URI and the respective object
	 */
	public Map<String, Equivalent<OntClass>> searchClassesInOntology(OntModel ontModel) {

		Set<OntClass> classSet = ontModel.listClasses().toSet();

		Map<String, Equivalent<OntClass>> equivalentsMap = new HashMap<String, Equivalent<OntClass>>();

		Map<String, Boolean> visitedMap = classSet.stream()
				.collect(Collectors.toMap(OntClass::toString, visited -> false));

		Stack<OntClass> stack = new Stack<OntClass>();
		stack.addAll(classSet);

		while (stack.size() > 0) {
			boolean isExisting = false;
			OntClass curClass = stack.pop();
			String curName = curClass.getURI();

			if (curName != null && !visitedMap.get(curName)) {
				visitedMap.put(curName, true);
				List<OntClass> eqsList = null;
				try {
					eqsList = curClass.listEquivalentClasses().toList();
				} catch (ConversionException e) {
					LOGGER.warn("The Equivalent classes of {} probably do not have rdf:type owl:Class or equivalent",
							curName);
				}
				if (eqsList != null && !eqsList.isEmpty()) {
					eqsList.forEach(property -> {
						if (property.isResource()) {
							visitedMap.putIfAbsent(property.getURI(), false);
						}
					});
					stack.addAll(eqsList);
				}

				// does it exist already?
				for (String key : equivalentsMap.keySet()) {
					Equivalent<OntClass> curEquivalent = equivalentsMap.get(key);
					if (curEquivalent.containsElement(curClass)) {
						isExisting = true;
						curEquivalent.addEquivalent(curClass);
						if (eqsList != null && !eqsList.isEmpty()) {
							curEquivalent.addEquivalentGroup(eqsList.stream().collect(Collectors.toSet()));
						}
						equivalentsMap.put(curName, curEquivalent);
						break;
					}
				}

				// no? then, create it
				if (!isExisting) {
					Equivalent<OntClass> equivalent = new Equivalent<OntClass>(curClass);
					equivalentsMap.put(curName, equivalent);
				}
			}
		}
		return equivalentsMap;
	}

	/**
	 * Renames all the resources, which are implicitly of type rdfs:Class in a model,
	 * to one uniform URI
	 * 
	 * @param model   the RDF Model
	 * @param classes the map between the different URIs and the class object
	 */
	public void renameClasses(Model model, Map<String, Equivalent<OntClass>> classes) {
		ResIterator iterator = model.listResourcesWithProperty(RDF.type, RDFS.Class);
		while (iterator.hasNext()) {
			Resource curResource = iterator.next();
			String curResourceURI = curResource.getURI();
			Equivalent<OntClass> equiv = classes.get(curResourceURI);

			if (equiv != null && curResourceURI != null && !curResourceURI.equals(equiv.getName())) {
				ResourceUtils.renameResource(curResource, equiv.getName());
			}
		}
	}
}
