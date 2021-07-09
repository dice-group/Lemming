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

	private Map<OntClass, String> classesEquiNameMap;

	private Map<OntProperty, String> propertiesEquiNameMap;

	private Map<OntProperty, OntProperty> propertyDRPropertyMap;

	private Set<OntProperty> ontProperties;

	public Inferer(boolean isMat, @Nonnull OntModel ontModel) {
		this.isMat = isMat;
		this.ontModel = ontModel;

		//collect the equivalent properties and classes information from the ontology
		Set<OntClass> ontClasses = this.ontModel.listClasses().toSet();
		Map<OntClass, Set<OntClass>> classesEquiMap = searchEquivalents(ontClasses);
		classesEquiNameMap = findProperName(classesEquiMap);

		this.ontProperties = ontModel.listAllOntProperties().toSet();
		Map<OntProperty, Set<OntProperty>> propertiesEquiMap = searchEquivalents(this.ontProperties);
		propertiesEquiNameMap = findProperName(propertiesEquiMap);
		propertyDRPropertyMap = collectRangeDomain(propertiesEquiMap);

	}

	public Inferer(boolean isMat, @Nonnull String filePath, @Nullable String base, Map<String, String> rdfsFilesMap) {
		this.isMat = isMat;
		OntModel ontModel = this.readOntology(filePath, base);
		for(String fileName : rdfsFilesMap.keySet()){
			ontModel.read(fileName, rdfsFilesMap.get(fileName));
		}
		this.ontModel = ontModel;
		// collect the equivalent properties and classes information from the ontology
		Set<OntClass> ontClasses = this.ontModel.listClasses().toSet();
		Map<OntClass, Set<OntClass>> classesEquiMap = searchEquivalents(ontClasses);
		classesEquiNameMap = findProperName(classesEquiMap);


		this.ontProperties = ontModel.listAllOntProperties().toSet();
		Map<OntProperty, Set<OntProperty>> propertiesEquiMap = searchEquivalents(this.ontProperties);
		propertiesEquiNameMap = findProperName(propertiesEquiMap);
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

		// uniform the names of the classes
		renameClasses(newModel, this.classesEquiNameMap);

		// infer type statements, a single property name is also enforced here
		iterateStmts(newModel, sourceModel, this.propertiesEquiNameMap, this.propertyDRPropertyMap);
		checkEmptyTypes(set, newModel);

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
	 * @param propToName   map each resource uri to a string name which represents equivalent resources
	 * @param domainRangeMap map each ontproperty to a property which contains all domain and range of equivalent resources
	 */
	public void iterateStmts(Model newModel, Model sourceModel, Map<OntProperty, String> propToName, Map<OntProperty, OntProperty> domainRangeMap) {
		List<Statement> stmts = sourceModel.listStatements().toList();
		for (Statement curStatement : stmts) {
			Set<Statement> newStmts = searchType(curStatement, newModel, propToName, domainRangeMap);
			// searchType(curStatement, ontModel, newModel);
			newModel.add(newStmts.toArray(new Statement[newStmts.size()]));
			
			String pattern =  "^(http:\\/\\/www\\.w3\\.org\\/1999\\/02\\/22-rdf-syntax-ns#_)\\d+$";
					//"^(http:\\/\\/www.w3.org\\/1999\\/02\\/22-rdf-syntax-ns#_).*";
		
			if(curStatement.getPredicate().getURI().matches(pattern)) {
				ModelUtil.replaceStatement(newModel, 
						curStatement, 
						ResourceFactory.createStatement(curStatement.getSubject(), RDFS.member, curStatement.getObject()));
			}
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
	 * @param model  where we add the new triples and therefore, where we check
	 *                  if the statement is already existing in the model or not
	 * @param ontModel  the ontology model
	 * @return a set of statements inferred from one property
	 */
	private Set<Statement> searchType(Statement statement, Model model, OntModel ontModel) {
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
				newStmts.add(subjType);

			}
			if (object.isResource()) {
				List<? extends OntResource> range = property.listRange().toList();
				for (OntResource curResource : range) {
					Statement objType = ResourceFactory.createStatement(object.asResource(), RDF.type, curResource);
					newStmts.add(objType);
				}
			}
		}
		return newStmts;
	}

	/**
	 * Same as searchType(Statement statement, Model model, OntModel ontModel),
	 * but in our custom objects: For a given statement, this method searches for
	 * the predicate of a model inside the Ontology. If found in the Ontology, it
	 * then extracts the domain and range. Creating and adding a new triple with the
	 * inferred type to the model.
	 * 
	 * @param statement  statement in which we want to check the predicate in the
	 *                   ontology
	 * @param model   where we add the new triples and therefore, where we check
	 *                   if the statement is already existing in the model or not
	 * @param properties map each resource uri to a string name which represents equivalent resources
	 * @param domainRangeMap map each ontproperty to a property which contains all domain and range of equivalent resources
	 * @return a set of statements inferred from a property
	 */
	private Set<Statement> searchType(Statement statement, Model model, Map<OntProperty, String> properties, Map<OntProperty, OntProperty> domainRangeMap) {
		Set<Statement> newStmts = new HashSet<>();
		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();

		String name = properties.get(predicate);
		OntProperty property = domainRangeMap.get(predicate);
		if(name != null && property !=null){
			Property newPredicate = ResourceFactory.createProperty(name);
			if (!newPredicate.getURI().equals(predicate.getURI()))
				ModelUtil.replaceStatement(model, statement,
						ResourceFactory.createStatement(subject, newPredicate, object));

			List<? extends OntResource> domain = property.listDomain().toList();
			for (OntResource curResource : domain) {
				Statement subjNewStmt = ResourceFactory.createStatement(subject, RDF.type, curResource);
				if (!curResource.isAnon()) {
					newStmts.add(subjNewStmt);
				}
			}
			if (object.isResource()) {
				List<? extends OntResource> range = property.listRange().toList();
				for (OntResource curResource : range) {
					Statement objNewStmt = ResourceFactory.createStatement(object.asResource(), RDF.type, curResource);
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
	 * @return OntModel Object
	 */
	public OntModel readOntology(String filePath, String base) {
		if (base == null)
			base = "RDF/XML";
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		try (InputStream inputStream = FileManager.get().open(filePath)) {
			if (inputStream != null) {
				ontModel.read(inputStream, base);
			}
		} catch (IOException e) {
			LOGGER.error("Couldn't read ontology file. Returning empty ontology model.", e);
		}

		return ontModel;
	}

	/**
	 * Searches for the equivalents in an ontology and maps each IRI to a set of equivalent IRIs
	 * @param ontElements the ontology classes or properties
	 * @return
	 */
	public <T extends OntResource> Map<T, Set<T>> searchEquivalents(Set<T> ontElements) {

		Map<T, Set<T>> iriEquiMap = new HashMap<>();

		for (T currentResource : ontElements) {

			if (currentResource.getURI()!=null) {

				//find equivalent classes if possible
				List<T> eqsList = null;
				try {
					if (currentResource.isProperty())
						eqsList = (List<T>) currentResource.asProperty().listEquivalentProperties().toList();
					if (currentResource.isClass())
						eqsList = (List<T>) currentResource.asClass().listEquivalentClasses().toList();
				} catch (ConversionException e) {
					LOGGER.warn(
							"Cannot convert the equivalents. The ontology does not have any further info on the equivalents of {}.",
							currentResource.toString());
				}

				Set<T> equiSet = null;
				if(eqsList == null || eqsList.isEmpty()){
					if(!iriEquiMap.containsKey(currentResource)){
						equiSet = new HashSet<>();
						equiSet.add(currentResource);
						iriEquiMap.put(currentResource, equiSet);
					}
				}else{
					if(iriEquiMap.containsKey(currentResource)){
						equiSet = iriEquiMap.get(currentResource);
					}
					Map<T, Set<T>> localMap = new HashMap<>();
					for(T re : eqsList){
						if(re.getURI()!=null && iriEquiMap.containsKey(re)){
							localMap.put(re, iriEquiMap.get(re));
						}
					}

					for(T re : localMap.keySet()){
						if(equiSet == null){
							equiSet = localMap.get(re);
						}else if (equiSet != localMap.get(re)){
							equiSet.addAll(localMap.get(re));
						}
					}

					if(equiSet==null){
						equiSet = new HashSet<>();
						equiSet.add(currentResource);
					}else{
						equiSet.add(currentResource);
					}
					for(T eq : eqsList){
						if(eq.getURI() != null){
							equiSet.add(eq);
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
	public void renameClasses(Model model, Map<OntClass, String> classes) {
		Iterator<Entry<OntClass, String>> it = classes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<OntClass, String> pair = it.next();
			Resource mResource = model.getResource(pair.getKey().getURI());
			if (mResource != null && !mResource.getURI().equals(pair.getValue())) {
				ResourceUtils.renameResource(mResource, pair.getValue());
			}
		}
	}

	private <T extends OntResource> Map<T, String> findProperName(Map<T, Set<T>> iriEquiMap){
		Map<T, String> iriToName = new HashMap<>();
		for(T re : iriEquiMap.keySet()){
			if(!iriToName.containsKey(re)){
				String properName = re.getURI();
				Set<T> equis = iriEquiMap.get(re);
				for(T eq : equis){
					if(properName.compareTo(eq.getURI())<0){
						properName = eq.getURI();
					}
				}
				for(T eq : iriEquiMap.get(re)){
					iriToName.put(eq, properName);
				}
			}
		}
		return  iriToName;
	}

	private Map<OntProperty,OntProperty> collectRangeDomain(Map<OntProperty, Set<OntProperty>> propertyEquiMap){
		Map<OntProperty,OntProperty> ontProToOntProDR = new HashMap<>();
		for(OntProperty property : propertyEquiMap.keySet()){
			List<OntProperty> eqsProperties = new ArrayList<>(propertyEquiMap.get(property));
			OntProperty collectedProperty = eqsProperties.get(0);
			for(OntProperty currentProperty : eqsProperties){
				List<? extends OntResource> domainList = currentProperty.listDomain().toList();
				for (OntResource domain : domainList) {
					if (!collectedProperty.hasDomain(domain)) {
						currentProperty.addDomain(domain);
					}
				}

				List<? extends OntResource> rangeList = currentProperty.listRange().toList();
				for (OntResource range : rangeList) {
					if (!collectedProperty.hasRange(range)) {
						currentProperty.addRange(range);
					}
				}
			}
			ontProToOntProDR.put(property, collectedProperty);
		}
		return ontProToOntProDR;
	}
}
