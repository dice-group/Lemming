package org.aksw.simba.lemming.creation.PrecomputingValues;

import org.aksw.simba.lemming.creation.*;
import org.aksw.simba.lemming.util.ModelUtil;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;


public class PrecomputingValuesTest{
    private static final String GEOLOGY_DATASET_FOLDER_PATH = "GeologyGraphs/";
    private static final String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
    private static final String LINKED_GEO_DATASET_FOLDER_PATH = "LinkedGeoGraphs/";

    @Test
    public void testGeologyGraphs(){
        //create ontology model
        OntModel ontModel = createOntModelForGeology();

        File folder = new File(GEOLOGY_DATASET_FOLDER_PATH);
        List<String> lstSortedFilesByName = Arrays.asList(folder.list());
        Collections.sort(lstSortedFilesByName);

        System.out.println("Test GeologyGraphs:");
        System.out.println("Compute with old Inferer......");
        long startTime = System.currentTimeMillis();
        List<Long> sizesForOldInferer = new ArrayList<>();
        OldInferer oldInferer = new OldInferer(true);
        for (String fileName : lstSortedFilesByName) {
            File file = new File(GEOLOGY_DATASET_FOLDER_PATH + "/" + fileName);
            Model geologyModel = ModelFactory.createDefaultModel();
            geologyModel.read(file.getAbsolutePath(), "TTL");
            sizesForOldInferer.add(geologyModel.size());

            // returns a new model with the added triples
            geologyModel = oldInferer.process(geologyModel, ontModel);
            sizesForOldInferer.add(geologyModel.size());
        }

        long endTime = System.currentTimeMillis();
        long timeForOldInferer = endTime-startTime;
        System.out.println("OldTime: " + timeForOldInferer);

        System.out.println("Compute with new Inferer......");
        startTime = System.currentTimeMillis();
        List<Long> sizesForNewInferer = new ArrayList<>();
        Inferer inferer = new Inferer(true, ontModel);
        for (String fileName : lstSortedFilesByName) {
            File file = new File(GEOLOGY_DATASET_FOLDER_PATH + "/" + fileName);
            Model geologyModel = ModelFactory.createDefaultModel();
            geologyModel.read(file.getAbsolutePath(), "TTL");
            sizesForNewInferer.add(geologyModel.size());

            // returns a new model with the added triples
            geologyModel = inferer.process(geologyModel);
            sizesForNewInferer.add(geologyModel.size());
        }
        endTime = System.currentTimeMillis();
        long timeForNewInferer = endTime-startTime;
        System.out.println("NewTime: " + timeForNewInferer);

        assertArrayEquals(sizesForOldInferer.toArray(), sizesForNewInferer.toArray());
        assertTrue(timeForNewInferer<(timeForOldInferer));
    }

    @Test
    public void testSWDF(){
        //create ontology model
        OntModel ontModel = createOntModelForSWDF();

        System.out.println("Test SemanticWebDogFood::");
        System.out.println("Compute with old Inferer......");
        long startTime = System.currentTimeMillis();
        List<Long> sizesForOldInferer = new ArrayList<>();

        Model dogFoodModel = ModelFactory.createDefaultModel();
        long oldModelSize;
        File folder;
        for (int y = 2001; y <= 2019; ++y) {
            folder = new File(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH + y);
            if (folder.exists()) {
                oldModelSize = dogFoodModel.size();
                addToModel(folder, dogFoodModel);
                if(oldModelSize < dogFoodModel.size()){
                    OldInferer oldInferer = new OldInferer(true);
                    dogFoodModel = oldInferer.process(dogFoodModel, ontModel);
                }
                for(File file: folder.listFiles()){
                    dogFoodModel.read(file.getAbsolutePath());
                }
            }
            sizesForOldInferer.add(dogFoodModel.size());
        }

        long endTime = System.currentTimeMillis();
        long timeForOldInferer = endTime-startTime;
        System.out.println("OldTime: " + timeForOldInferer);


        System.out.println("Compute with new Inferer......");
        startTime = System.currentTimeMillis();
        List<Long> sizesForNewInferer = new ArrayList<>();

        dogFoodModel = ModelFactory.createDefaultModel();
        Inferer inferer = new Inferer(true, ontModel);
        for (int y = 2001; y <= 2019; ++y) {
            folder = new File(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH + y);
            if (folder.exists()) {
                oldModelSize = dogFoodModel.size();
                addToModel(folder, dogFoodModel);
                if(oldModelSize < dogFoodModel.size()){
                    dogFoodModel = inferer.process(dogFoodModel);
                }
                for(File file: folder.listFiles()){
                    dogFoodModel.read(file.getAbsolutePath());
                }
            }
            sizesForNewInferer.add(dogFoodModel.size());
        }

        endTime = System.currentTimeMillis();
        long timeForNewInferer = endTime-startTime;
        System.out.println("NewTime: " + timeForNewInferer);

        assertArrayEquals(sizesForOldInferer.toArray(), sizesForNewInferer.toArray());
        assertTrue(timeForNewInferer<(timeForOldInferer));
    }

    @Test
    public void testLinkedGeo(){
        //create ontology model
        OntModel ontModel = createOntModelForLGeo();

        File folder = new File(LINKED_GEO_DATASET_FOLDER_PATH);
        List<String> lstSortedFilesByName = Arrays.asList(folder.list());
        Collections.sort(lstSortedFilesByName);

        System.out.println("Test LinkedGeo:");
        System.out.println("Compute with old Inferer......");
        long startTime = System.currentTimeMillis();
        List<Long> sizesForOldInferer = new ArrayList<>();

        for (String fileName : lstSortedFilesByName) {
            File file = new File(LINKED_GEO_DATASET_FOLDER_PATH + "/" + fileName);
            Model geoModel = ModelFactory.createDefaultModel();
            for (File subFile : file.listFiles()) {
                // read file to model
                geoModel.read(subFile.getAbsolutePath(), "TTL");
            }
            sizesForOldInferer.add(geoModel.size());
            OldInferer oldInferer = new OldInferer(true);
            // returns a new model with the added triples
            geoModel = oldInferer.process(geoModel, ontModel);
            sizesForOldInferer.add(geoModel.size());
        }

        long endTime = System.currentTimeMillis();
        long timeForOldInferer = endTime-startTime;
        System.out.println("OldTime: " + timeForOldInferer);

        System.out.println("Compute with new Inferer......");
        startTime = System.currentTimeMillis();
        List<Long> sizesForNewInferer = new ArrayList<>();
        Inferer inferer = new Inferer(true, ontModel);
        for (String fileName : lstSortedFilesByName) {
            File file = new File(LINKED_GEO_DATASET_FOLDER_PATH + "/" + fileName);
            Model geoModel = ModelFactory.createDefaultModel();
            for (File subFile : file.listFiles()) {
                // read file to model
                geoModel.read(subFile.getAbsolutePath(), "TTL");
            }
            sizesForNewInferer.add(geoModel.size());

            // returns a new model with the added triples
            geoModel = inferer.process(geoModel);
            sizesForNewInferer.add(geoModel.size());
        }
        endTime = System.currentTimeMillis();
        long timeForNewInferer = endTime-startTime;
        System.out.println("NewTime: " + timeForNewInferer);

        assertArrayEquals(sizesForOldInferer.toArray(), sizesForNewInferer.toArray());
        assertTrue(timeForNewInferer<(timeForOldInferer));
    }

    private OntModel createOntModelForGeology(){
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.getDocumentManager().setProcessImports(false);
        ontModel.read("22-rdf-syntax-ns", "TTL");
        ontModel.read("rdf-schema", "TTL");
        File ontFolder = new File("geology");
        for(File file : ontFolder.listFiles()){
            ontModel.read(file.getAbsolutePath(), "ttl");
        }
        return ontModel;
    }

    private OntModel createOntModelForSWDF(){
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.getDocumentManager().setProcessImports(false);
        ontModel.read("22-rdf-syntax-ns", "TTL");
        ontModel.read("rdf-schema", "TTL");
        File ontFolder = new File("swdf-owls");
        for (File file : ontFolder.listFiles()) {
            ontModel.read(file.getAbsolutePath(), "TTL");
        }
        return ontModel;
    }

    private OntModel createOntModelForLGeo(){
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.getDocumentManager().setProcessImports(false);
        ontModel.read("22-rdf-syntax-ns", "TTL");
        ontModel.read("rdf-schema", "TTL");
        ontModel.read("lgeo/foaf.ttl");
        ontModel.read("lgeo/skos.ttl");
        ontModel.read("lgeo/purl_dcterms.ttl");
        ontModel.read("lgeo/owl.ttl");
        ontModel.read("lgeo/terms.ttl");
        ontModel.read("lgeo/wgs84_pos.ttl");
        ontModel.read("lgeo/2014-09-09-ontology.sorted.nt");
        ontModel.read("lgeo/geosparql.ttl");
        ontModel.read("lgeo/geovocab_geometry.ttl");
        ontModel.read("lgeo/geovocab_spatial.ttl");
        ontModel.read("lgeo/LGD-Dump-110406-Ontology.nt");
        ontModel.read("lgeo/rdfs-ns-void.rdf");
        ontModel.read("lgeo/custom_ontology.nt");
        return ontModel;
    }


    private void addToModel(File folder, Model dogFoodModel) {
        for (File file : folder.listFiles()) {
            try {
                dogFoodModel.read(file.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Exception while reading file \"" + file.toString() + "\". Aborting." + " " + e.toString() );
                System.exit(1);
            }
        }
    }

    class OldInferer {

        private boolean isMat = false;

        public OldInferer(boolean isMat) {
            this.isMat = isMat;
        }

        public Model process(Model sourceModel, OntModel ontModel) {
            Model newModel = ModelFactory.createDefaultModel();
            newModel.add(sourceModel);
            Set<Resource> set = extractUniqueResources(newModel);
            if (ontModel != null) {
                // collect the equivalent properties and classes information from the ontology
                Set<OntClass> ontClasses = ontModel.listClasses().toSet();
                Map<String, Equivalent> classes = searchEquivalents(ontClasses); // searchClassesInOntology(ontModel);

                Set<OntProperty> ontProperties = ontModel.listAllOntProperties().toSet();
                Map<String, Equivalent> uriNodeMap = searchEquivalents(ontProperties);// searchEqPropertiesInOnt(ontModel);

                if(isMat) {
                    GraphMaterializer materializer = new GraphMaterializer(ontProperties);
                    while(true){
                        long size = newModel.size();
                        List<Statement> symmetricStmts = materializer.deriveSymmetricStatements(newModel);
                        List<Statement> transitiveStmts = materializer.deriveTransitiveStatements(newModel);
                        List<Statement> inverseStmts = materializer.deriveInverseStatements(newModel);

                        newModel.add(symmetricStmts);
                        newModel.add(transitiveStmts);
                        newModel.add(inverseStmts);

                        //if the model didn't grow, break the loop
                        if(size==newModel.size())
                            break;
                    }
                }

                // infer type statements, a single property name is also enforced here
                iterateStmts(newModel, sourceModel, ontModel, uriNodeMap);
                checkEmptyTypes(set, newModel);

                // uniform the names of the classes
                renameClasses(newModel, classes);

            }
            return newModel;
        }

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
        public void iterateStmts(Model newModel, Model sourceModel, OntModel ontModel, Map<String, Equivalent> uriNodeMap) {
            List<Statement> stmts = sourceModel.listStatements().toList();
            for (Statement curStatement : stmts) {
                Set<Statement> newStmts = searchType(curStatement, newModel, uriNodeMap);
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
        private Set<Statement> searchType(Statement statement, Model newModel, Map<String, Equivalent> uriNodeMap) {
            Set<Statement> newStmts = new HashSet<>();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();

            Equivalent<OntProperty> node = uriNodeMap.get(predicate.toString());
            OntProperty property = null;

            if (node != null) {
                property = (OntProperty) node.getAttribute();
                Property newPredicate = ResourceFactory.createProperty(node.getName());

                if (!newPredicate.getURI().equals(predicate.getURI()))
                    ModelUtil.replaceStatement(newModel, statement,
                            ResourceFactory.createStatement(subject, newPredicate, object));
            }

            if (property != null) {
                List<? extends OntResource> domain = property.listDomain().toList();
                for (OntResource curResource : domain) {
                    Statement subjType = ResourceFactory.createStatement(subject, RDF.type, curResource);
                    if (!newModel.contains(subjType) && !curResource.isAnon()) {
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
            }

            return ontModel;
        }

        /**
         * Searches for the equivalents in an ontology and maps them to our Equivalent<T
         * extends OntResource> class, producing a map of the Equivalent objects with
         * the URIs as keys.
         *
         * @see Equivalent<T extends OntResource>
         * @param <T>
         * @param ontElements the ontology classes or properties
         * @return
         */
        public <T extends OntResource> Map<String, Equivalent> searchEquivalents(Set<T> ontElements) {

            Map<String, Equivalent> uriNodeMap = new HashMap<String, Equivalent>();

            Stack<T> stack = new Stack<T>();
            stack.addAll(ontElements);

            Set<Equivalent> elements = new HashSet<Equivalent>();

            while (stack.size() > 0) {
                T currentResource = stack.pop();
                String curURI = currentResource.getURI();
                boolean isSame = false;

                if (curURI!=null && !uriNodeMap.containsKey(curURI)) {
                    List eqsList = null;
                    try {
                        if (currentResource.isProperty())
                            eqsList = currentResource.asProperty().listEquivalentProperties().toList();
                        if (currentResource.isClass())
                            eqsList = currentResource.asClass().listEquivalentClasses().toList();
                    } catch (ConversionException e) {

                    }

                    if (eqsList != null && !eqsList.isEmpty()) {
                        stack.addAll(eqsList);
                    }

                    //node to where we want to add the info to
                    Equivalent curNode = null;

                    // check to which node do we need to add this info to
                    Iterator<Equivalent> propIterator = elements.iterator();
                    while (propIterator.hasNext()) {
                        curNode = propIterator.next();
                        isSame = curNode.containsElement(currentResource);
                        if (isSame) {
                            curNode.addEquivalent(currentResource);
                            break;
                        }
                    }

                    // if not, create new one
                    if (!isSame) {
                        curNode = new Equivalent(currentResource);
                        elements.add(curNode);
                    }

                    // add the node to the map with the URI and add the equivalents (if existing) to the node object
                    if (curNode != null) {
                        uriNodeMap.put(curURI, curNode);
                        if(eqsList != null) {
                            curNode.addEquivalentGroup((Set) eqsList.stream().collect(Collectors.toSet()));
                        }
                    }
                }
            }
            /*System.out.println(uriNodeMap.keySet().size());
            for(String uri : uriNodeMap.keySet()){
                System.out.print(uri + "  --> ");
                System.out.println("\t" + uriNodeMap.get(uri).getEquivalents().toString());

            }*/
            return uriNodeMap;
        }

        /**
         * Renames all the equivalent resources to one uniform URI
         *
         * @param model   the RDF Model
         * @param classes the map between the different URIs and the class object
         */
        public void renameClasses(Model model, Map<String, Equivalent> classes) {
            Iterator<Map.Entry<String, Equivalent>> it = classes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Equivalent> pair = it.next();
                String newName = pair.getValue().getName();
                Resource mResource = model.getResource(pair.getKey());
                if (mResource != null && !mResource.getURI().equals(newName)) {
                    ResourceUtils.renameResource(mResource, newName);
                }
            }
        }
    }
}
