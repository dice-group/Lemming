package org.aksw.simba.lemming.creation;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;


import org.aksw.simba.lemming.util.ModelUtil;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The Inferer class implements an inference of the type of subjects and objects
 * of a given RDF Model from a given Ontology. To use this class, one could
 * first use the readOntology() method and then use the process() method.
 * 
 * @author Alexandra Silva
 *
 */

public class Inferer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Inferer.class);
	
	/**
	 * Do we also want materialization to be applied to the graph
	 */
	private boolean isMat;

	/**
	 * An ontology model for a dataset.
	 */
	private OntModel ontModel;

	private Map<OntClass, OntClass> classEquiMap;

	private Map<OntProperty, OntProperty> propertyEquiMap;

	private Set<OntProperty> ontProperties;

	public Inferer(boolean isMat, @Nonnull OntModel ontModel) {
		this.isMat = isMat;
		this.ontModel = ontModel;

		//collect the equivalent properties and classes information from the ontology
		Set<OntClass> ontClasses = this.ontModel.listClasses().toSet();
		Map<OntClass, Set<OntClass>> classesEquiSetMap = searchEquivalents(ontClasses);
		classEquiMap = findRepresentation(classesEquiSetMap);

		this.ontProperties = ontModel.listAllOntProperties().toSet();
		Map<OntProperty, Set<OntProperty>> propertiesEquiSetMap = searchEquivalents(this.ontProperties);
		propertyEquiMap = findRepresentationAndDR(propertiesEquiSetMap);
	}

	public Inferer(boolean isMat, @Nonnull String filePath, @Nullable String fileType, Map<String, String> rdfsFilesMap) {
		this.isMat = isMat;
		OntModel ontModel = this.readOntology(filePath, fileType);
		for(String fileName : rdfsFilesMap.keySet()){
			ontModel.read(fileName, rdfsFilesMap.get(fileName));
		}
		this.ontModel = ontModel;
		// collect the equivalent properties and classes information from the ontology
		Set<OntClass> ontClasses = this.ontModel.listClasses().toSet();
		Map<OntClass, Set<OntClass>> classesEquiSetMap = searchEquivalents(ontClasses);
		classEquiMap = findRepresentation(classesEquiSetMap);

		this.ontProperties = ontModel.listAllOntProperties().toSet();
		Map<OntProperty, Set<OntProperty>> propertiesEquiSetMap = searchEquivalents(this.ontProperties);
		propertyEquiMap = findRepresentationAndDR(propertiesEquiSetMap);
	}

	public Map<OntClass, OntClass> getClassEquiMap(){
		return this.classEquiMap;
	}
	public Map<OntProperty, OntProperty> getPropertyEquiMap(){
		return this.propertyEquiMap;
	}

	/**
	 * This method creates a new model with all the statements as sourceModel and
	 * goes on to populate it further with inferred triples
	 * 
	 * @param sourceModel RDF Model where we want the inference to take place
	 * @return The new model with the same triples as the sourceModel plus the
	 *         inferred triples.
	 */
	public Model process(Model sourceModel) {
		Model newModel = ModelFactory.createDefaultModel();
		newModel.add(sourceModel);
		Set<Resource> set = extractUniqueResources(newModel);

		if(isMat) {
			GraphMaterializer materializer = new GraphMaterializer(this.ontProperties);
			long curSize;
			long newSize;
			do{
				curSize = newModel.size();

				List<Statement> symmetricStmts = materializer.deriveSymmetricStatements(newModel);
				List<Statement> transitiveStmts = materializer.deriveTransitiveStatements(newModel);
				List<Statement> inverseStmts = materializer.deriveInverseStatements(newModel);
					
				newModel.add(symmetricStmts);
				newModel.add(transitiveStmts);
				newModel.add(inverseStmts);

				newSize = newModel.size();

			//if the model doesn't grow, terminate the loop
			}while(curSize != newSize);
		}

		// uniform the names of properties and infer type statements
		iterateStmts(newModel, sourceModel, this.propertyEquiMap);
		// uniform the names of the classes
		renameClasses(newModel, this.classEquiMap);
		checkEmptyTypes(set, newModel);

		return newModel;
	}

	/**
	 * This method gets all the unique subjects and objects of a model with the
	 * exception of the objects that are not resources. It is mainly used to do a
	 * before and after count of how many resources do not have a type.
	 * 
	 * @param model RDF Model from where the resources are extracted
	 * @return the set of resources of the given model
	 */
	private Set<Resource> extractUniqueResources(Model model) {
		Set<Resource> set = new HashSet<>();
		StmtIterator iterator = model.listStatements();
		while(iterator.hasNext()){
			Statement curStat = iterator.next();
			if(curStat.getSubject().isURIResource())
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
	 * @param propertyEquiMap map each property to its representative property
	 */
	private void iterateStmts(Model newModel, Model sourceModel, Map<OntProperty, OntProperty> propertyEquiMap) {
		List<Statement> stmts = sourceModel.listStatements().toList();
		for (Statement curStatement : stmts) {
			Set<Statement> newStmts = searchType(curStatement, newModel, propertyEquiMap);
			// searchType(curStatement, ontModel, newModel);
			newModel.add(newStmts.toArray(new Statement[newStmts.size()]));
			
			String pattern =  "^(http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#_)\\d+$";
					//"^(http:\\/\\/www.w3.org\\/1999\\/02\\/22-rdf-syntax-ns#_).*";
		
			if(curStatement.getPredicate().getURI().matches(pattern)) {
				ModelUtil.replaceStatement(newModel, 
						curStatement, 
						ResourceFactory.createStatement(curStatement.getSubject(), RDFS.member, curStatement.getObject()));
			}
		}
	}

	/**
	 * For a given statement, this method searches for the predicate of a model inside the Ontology. If found in the
	 * Ontology, it then extracts the domain and range. Creating and adding a new triple with the
	 * inferred type to the model.
	 * 
	 * @param statement  statement in which we want to check the predicate in the
	 *                   ontology
	 * @param model   where we add the new triples and therefore, where we check
	 *                   if the statement is already existing in the model or not
	 * @param propertyEquiMap map each property to its representative property
	 * @return a set of statements inferred from a property
	 */
	private Set<Statement> searchType(Statement statement, Model model, Map<OntProperty, OntProperty> propertyEquiMap) {
		Set<Statement> newStmts = new HashSet<>();
		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();

		OntProperty representation = propertyEquiMap.get(predicate);
		if(representation != null){
			if (!representation.getURI().equals(predicate.getURI())) {
				Property newPredicate = ResourceFactory.createProperty(representation.getURI());
				ModelUtil.replaceStatement(model, statement,
						ResourceFactory.createStatement(subject, newPredicate, object));
			}
			for (OntResource domain : representation.listDomain().toList()) {
				Statement subjNewStmt = ResourceFactory.createStatement(subject, RDF.type, domain);
				if (!domain.isAnon()) {
					newStmts.add(subjNewStmt);
				}
			}
			if (object.isResource()) {
				for (OntResource range : representation.listRange().toList()) {
					Statement objNewStmt = ResourceFactory.createStatement(object.asResource(), RDF.type, range);
					newStmts.add(objNewStmt);
				}
			}
		}
		return newStmts;
	}

	/**
	 * This method reads the ontology file with an InputStream
	 * 
	 * @param filePath path to the ontology file
	 * @param fileType type of ontology file
	 * @return OntModel Object
	 */
	private OntModel readOntology(String filePath, String fileType) {
		if (fileType == null)
			fileType = "RDF/XML";
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		try (InputStream inputStream = FileManager.get().open(filePath)) {
			if (inputStream != null) {
				ontModel.read(inputStream, fileType);
			}
		} catch (IOException e) {
			LOGGER.error("Couldn't read ontology file. Returning empty ontology model.", e);
		}

		return ontModel;
	}

	/**
	 * Searches for the equivalents in an ontology and maps each IRI to a set of equivalent IRIs
	 * @param ontElements the ontology classes or properties
	 */
	private <T extends OntResource> Map<T, Set<T>> searchEquivalents(Set<T> ontElements) {

		Map<T, Set<T>> iriEquiMap = new HashMap<>();

		for (T currentResource : ontElements) {

			if (currentResource.getURI()!=null) {

				//find equivalent resources if possible
				List<T> eqsList = null;
				try {
					if (currentResource.isProperty())
						eqsList = (List<T>) currentResource.asProperty().listEquivalentProperties().toList();
					else if (currentResource.isClass())
						eqsList = (List<T>) currentResource.asClass().listEquivalentClasses().toList();
				} catch (ConversionException e) {
					LOGGER.warn(
							"Cannot convert the equivalents. The ontology does not have any further info on the equivalents of {}.",
							currentResource);
				}

				Set<T> equiSet = new HashSet<>();
				equiSet.add(currentResource);
				if(eqsList == null || eqsList.isEmpty()){
					if(!iriEquiMap.containsKey(currentResource)){
						iriEquiMap.put(currentResource, equiSet);
					}
				}else{
					if(iriEquiMap.containsKey(currentResource)){
						equiSet = iriEquiMap.get(currentResource);
					}
					for(T re : eqsList){
						if(re.getURI() != null){
							if(iriEquiMap.containsKey(re)){
								equiSet.addAll(iriEquiMap.get(re));
							}else{
								equiSet.add(re);
							}
						}
					}
					for(T s : equiSet){
						iriEquiMap.put(s, equiSet);
					}
				}
			}
		}
		return iriEquiMap;
	}
	/**
	 * Renames all the equivalent resources to one uniform URI
	 * 
	 * @param model   the RDF Model
	 * @param classes the map between the different IRIs and a uri name representing their equivalent classes
	 */
	private void renameClasses(Model model, Map<OntClass, OntClass> classes) {
		Iterator<Entry<OntClass, OntClass>> it = classes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<OntClass, OntClass> pair = it.next();
			OntClass clazz = pair.getKey();
			OntClass replacement = pair.getValue();
			Resource mResource = model.getResource(clazz.getURI());
			if (!clazz.getURI().equals(replacement.getURI())) {
				ResourceUtils.renameResource(mResource, replacement.getURI());
			}
		}
	}

	/**
	 * Find a proper representation to represent each equivalent ontClasses set from the given map.
	 * @param classEquiSetMap a map that maps an ontClass to its equivalent ontClasses set.
	 * @return a map that maps an ontClass to its representative ontClass.
	 */
	private Map<OntClass, OntClass> findRepresentation(Map<OntClass, Set<OntClass>> classEquiSetMap){
		Map<OntClass, OntClass> iriToRepresentation = new HashMap<>();
		for(Map.Entry<OntClass, Set<OntClass>> entry : classEquiSetMap.entrySet()){
			OntClass clazz = entry.getKey();
			if(!iriToRepresentation.containsKey(clazz)){
				OntClass representation = clazz;
				Set<OntClass> equis = entry.getValue();
				for(OntClass eq : equis){
					if(representation.getURI().compareTo(eq.getURI())<0){
						representation = eq;
					}
				}
				for(OntClass eq : equis){
					iriToRepresentation.put(eq, representation);
				}
			}
		}
		return  iriToRepresentation;
	}

	/**
	 * Find a proper representation to represent each equivalent ontProperty set from the given map.
	 * And add domains and ranges of all equivalent ontProperties to the representative ontProperty.
	 * @param propertyEquiSetMap map each ontProperty to a set of properties which contains its equivalent ontProperties.
	 * @return a map that maps each ontProperty from the given map to its representative ontProperty
	 * which carries all domains and ranges.
	 */
	private Map<OntProperty, OntProperty> findRepresentationAndDR(Map<OntProperty, Set<OntProperty>> propertyEquiSetMap) {
		Map<OntProperty, OntProperty> propertyEquiMap = new HashMap<>();
		for(Map.Entry<OntProperty, Set<OntProperty>> entry : propertyEquiSetMap.entrySet()){
			OntProperty property = entry.getKey();
			if(!propertyEquiMap.containsKey(property)){
				OntProperty representation = property;
				Set<OntProperty> equis = entry.getValue();
				//determine the representation
				for(OntProperty eq : equis){
					if(representation.getURI().compareTo(eq.getURI())<0){
						representation = eq;
					}
				}
				//collect the domain and range and add into the representation
				for(OntProperty eq : equis){
					List<? extends OntResource> domainList = eq.listDomain().toList();
					for (OntResource domain : domainList) {
						if (!representation.hasDomain(domain)) {
							representation.addDomain(domain);
						}
					}

					List<? extends OntResource> rangeList = eq.listRange().toList();
					for (OntResource range : rangeList) {
						if (!representation.hasRange(range)) {
							representation.addRange(range);
						}
					}
				}
				//put each equivalent property and its representative property into the map
				for(OntProperty eq: equis){
					propertyEquiMap.put(eq, representation);
				}
			}
		}
		return propertyEquiMap;
	}
}
