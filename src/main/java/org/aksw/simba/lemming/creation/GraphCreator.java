package org.aksw.simba.lemming.creation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.InMemoryPalette;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class GraphCreator {

	protected ColouredGraph graph;
	protected ObjectObjectOpenHashMap<Resource, HierarchyNode> classes;
	protected ObjectObjectOpenHashMap<Resource, HierarchyNode> properties;
	protected ColourPalette vertexPalette;
	protected ColourPalette edgePalette;

	protected ColourPalette datatypedEdgePalette;
	protected Map<Resource, Set<RDFDatatype>> dataTypedProperties;

	public GraphCreator() {
		// Initialize the classes
		classes = new ObjectObjectOpenHashMap<Resource, HierarchyNode>();
		classes.put(RDFS.Class, new HierarchyNode());
		classes.put(OWL.Class, new HierarchyNode());
		classes.put(RDF.Property, new HierarchyNode());
		vertexPalette = new InMemoryPalette();
		vertexPalette.addColour(RDFS.Class.getURI());
		vertexPalette.setColour(OWL.Class.getURI(), vertexPalette.getColour(RDFS.Class.getURI()));
		vertexPalette.addColour(RDF.Property.getURI());
		// Initialize the properties
		properties = new ObjectObjectOpenHashMap<Resource, HierarchyNode>();
		properties.put(RDF.type, new HierarchyNode());
		edgePalette = new InMemoryPalette();
		edgePalette.addColour(RDF.type.getURI());

		dataTypedProperties = new HashMap<Resource, Set<RDFDatatype>>();
		datatypedEdgePalette = new InMemoryPalette();
	}

	public ColouredGraph processModel(Model model) {
		ColourPalette vertexPalette = createVertexPalette(model);
		ColourPalette edgePalette = createEdgePalette(model);
		ColouredGraph graph = new ColouredGraph(vertexPalette, edgePalette);
		ObjectIntOpenHashMap<Resource> resourceIdMapping = new ObjectIntOpenHashMap<Resource>();
		StmtIterator iterator = model.listStatements();
		Statement statement;
		Resource subject, object;
		Property property;
		int subjectId, propertyId, objectId;
		String propertyUri;
		// Iterator over all statements
		while (iterator.hasNext()) {
			statement = iterator.next();
			subject = statement.getSubject();
			// Add the subject if it is not existing
			if (resourceIdMapping.containsKey(subject)) {
				subjectId = resourceIdMapping.get(subject);
			} else {
				subjectId = graph.addVertex();
				resourceIdMapping.put(subject, subjectId);
			}
			// if this statement has a resource as object
			if (statement.getObject().isResource()) {
				// Add the object if it is not existing
				object = statement.getObject().asResource();
				if (resourceIdMapping.containsKey(object)) {
					objectId = resourceIdMapping.get(object);
				} else {
					objectId = graph.addVertex();
					resourceIdMapping.put(object, objectId);
				}
				// Add the property if it is not existing
				property = statement.getPredicate();
				propertyId = graph.addEdge(subjectId, objectId);
				// Set the colour of the edge
				propertyUri = property.getURI();
				if (!edgePalette.containsUri(propertyUri)) {
					edgePalette.addColour(propertyUri);
				}
				graph.setEdgeColour(propertyId, edgePalette.getColour(propertyUri));

				// if this triple defines the class of the subject
				if (property.equals(RDF.type)) {
					graph.setVertexColour(subjectId,
							vertexPalette.addToColour(graph.getVertexColour(subjectId), object.getURI()));
				}
			}

			/*
			 * ------------------------------------------------- if this statement has an
			 * object as a literal -------------------------------------------------
			 */
			else {

				// data typed property
				property = statement.getPredicate();
				propertyUri = property.getURI();

				if (statement.getObject().isLiteral()) {
					// literal
					Literal literal = statement.getObject().asLiteral();
					RDFDatatype litType = literal.getDatatype();

					String datatype = litType != null ? litType.getURI() : "";

					// put datatype property to the palette
					if (!datatypedEdgePalette.containsUri(propertyUri)) {
						datatypedEdgePalette.addColour(propertyUri);
					}
					BitSet datatypedEdgeColour = datatypedEdgePalette.getColour(propertyUri);
					/*
					 * a trick for semantic web dog food
					 */
					String defaultDataType = "http://www.w3.org/2001/XMLSchema#string";
					if (propertyUri.contains("label"))
						datatype = defaultDataType;

					// add to the coloured graph
					graph.addLiterals(literal.toString(), subjectId, datatypedEdgeColour, datatype);
				}
			}
		}

		// set the datatypedEdgePalette to the graph
		graph.setDataTypeEdgePalette(datatypedEdgePalette);
		return graph;
	}

	protected ColourPalette createVertexPalette(Model model) {
		NodeIterator nIterator = model.listObjectsOfProperty(RDF.type);
		RDFNode node;
		Resource resource1, resource2;
		while (nIterator.hasNext()) {
			node = nIterator.next();
			if (node.isResource()) {
				resource1 = node.asResource();
				classes.put(resource1, null);
			}
		}
		StmtIterator sIterator = model.listStatements(null, RDFS.subClassOf, (RDFNode) null);
		Statement statement;
		HierarchyNode hNode1, hNode2;
		// Iterate over the class hierarchy triples
		while (sIterator.hasNext()) {
			statement = sIterator.next();
			resource1 = statement.getSubject();
			node = statement.getObject();
			if (node.isResource()) {
				resource2 = node.asResource();
				if (classes.containsKey(resource1)) {
					hNode1 = classes.get(resource1);
					// if the class is known but there is no hierarchy node,
					// create it
					if (hNode1 == null) {
						hNode1 = new HierarchyNode();
						classes.put(resource1, hNode1);
					}
				} else {
					// this class is not known, add it
					hNode1 = new HierarchyNode();
					classes.put(resource1, hNode1);
				}
				if (classes.containsKey(resource2)) {
					hNode2 = classes.get(resource2);
					// if the class is known but there is no hierarchy node,
					// create it
					if (hNode2 == null) {
						hNode2 = new HierarchyNode();
						classes.put(resource2, hNode2);
					}
				} else {
					// this class is not known, add it
					hNode2 = new HierarchyNode();
					classes.put(resource2, hNode2);
				}
				// add the hierarchy information
				// if there is no list of child nodes
				if (hNode1.childNodes == null) {
					hNode1.childNodes = new Resource[] { resource2 };
				} else {
					hNode1.childNodes = Arrays.copyOf(hNode1.childNodes, hNode1.childNodes.length + 1);
					hNode1.childNodes[hNode1.childNodes.length - 1] = resource2;
				}
				// if there is no list of parent nodes
				if (hNode2.parentNodes == null) {
					hNode2.parentNodes = new Resource[] { resource1 };
				} else {
					hNode2.parentNodes = Arrays.copyOf(hNode2.parentNodes, hNode2.parentNodes.length + 1);
					hNode2.parentNodes[hNode2.parentNodes.length - 1] = resource1;
				}
			} else {
				// this triple seems to be wrong
				if (!classes.containsKey(resource1)) {
					classes.put(resource1, null);
				}
			}
		}

		// All classes have been collected
		// The colours can be defined

		String uri;
		for (int i = 0; i < classes.allocated.length; ++i) {
			if (classes.allocated[i]) {
				uri = ((Resource) ((Object[]) classes.keys)[i]).getURI();
				if (!vertexPalette.containsUri(uri)) {
					vertexPalette.addColour(uri);
				}
			}
		}

		// The hierarchy can be used to create colour mixtures that contain the
		// hierarchy
		// Search for all root nodes that have child nodes
		for (int i = 0; i < classes.allocated.length; ++i) {
			if (classes.allocated[i]) {
				hNode1 = (HierarchyNode) ((Object[]) classes.values)[i];
				if ((hNode1 != null) && (hNode1.childNodes != null) && (hNode1.parentNodes == null)) {
					mixColours((Resource) ((Object[]) classes.keys)[i], hNode1, classes, vertexPalette);
				}
			}
		}

		return vertexPalette;
	}

	protected ColourPalette createEdgePalette(Model model) {
		RDFNode node;
		Resource resource1, resource2;
		StmtIterator sIterator = model.listStatements(null, RDFS.subPropertyOf, (RDFNode) null);
		Statement statement;
		HierarchyNode hNode1, hNode2;
		// Iterate over the class hierarchy triples
		while (sIterator.hasNext()) {
			statement = sIterator.next();
			resource1 = statement.getSubject();
			node = statement.getObject();
			if (node.isResource()) {
				resource2 = node.asResource();
				if (properties.containsKey(resource1)) {
					hNode1 = properties.get(resource1);
				} else {
					// this property is not known, add it
					hNode1 = new HierarchyNode();
					properties.put(resource1, hNode1);
				}
				if (properties.containsKey(resource2)) {
					hNode2 = properties.get(resource2);
				} else {
					// this property is not known, add it
					hNode2 = new HierarchyNode();
					properties.put(resource2, hNode2);
				}
				// add the hierarchy information
				// if there is no list of child nodes
				if (hNode1.childNodes == null) {
					hNode1.childNodes = new Resource[] { resource2 };
				} else {
					hNode1.childNodes = Arrays.copyOf(hNode1.childNodes, hNode1.childNodes.length + 1);
					hNode1.childNodes[hNode1.childNodes.length - 1] = resource2;
				}
				// if there is no list of parent nodes
				if (hNode2.parentNodes == null) {
					hNode2.parentNodes = new Resource[] { resource1 };
				} else {
					hNode2.parentNodes = Arrays.copyOf(hNode2.parentNodes, hNode2.parentNodes.length + 1);
					hNode2.parentNodes[hNode2.parentNodes.length - 1] = resource1;
				}
			}
		}

		// All properties have been collected
		// The colours can be defined
		for (int i = 0; i < properties.allocated.length; ++i) {
			if (properties.allocated[i]) {
				edgePalette.addColour(((Resource) ((Object[]) properties.keys)[i]).getURI());
			}
		}

		// The hierarchy can be used to create colour mixtures that contain the
		// hierarchy
		// Search for all root nodes that have child nodes
		for (int i = 0; i < properties.allocated.length; ++i) {
			if (properties.allocated[i]) {
				hNode1 = (HierarchyNode) ((Object[]) properties.values)[i];
				if ((hNode1.childNodes != null) && (hNode1.parentNodes == null)) {
					mixColours((Resource) ((Object[]) properties.keys)[i], hNode1, properties, edgePalette);
				}
			}
		}

		return edgePalette;
	}

	private void mixColours(Resource resource, HierarchyNode hNode,
			ObjectObjectOpenHashMap<Resource, HierarchyNode> classes, ColourPalette palette) {
		// keep track of the already visited nodes
		Set<HierarchyNode> visitedChildren = new HashSet<HierarchyNode>();
		
		// initialize with the starting node's children
		Stack<HierarchyNode> childrenStack = new Stack<HierarchyNode>();
		for (int i = 0; i < hNode.childNodes.length; ++i) {
			HierarchyNode childNode = classes.get(hNode.childNodes[i]);
			childrenStack.add(childNode);
		}

		// go through the stack and iteratively add every node's children
		while (!childrenStack.isEmpty()) {
			HierarchyNode curNode = childrenStack.pop();
			if(curNode.childNodes == null) {
				continue;
			}
			for (int i = 0; i < curNode.childNodes.length; ++i) {
				HierarchyNode childNode = classes.get(curNode.childNodes[i]);
				palette.mixColour(resource.getURI(), curNode.childNodes[i].getURI());
				visitedChildren.add(childNode);
				if(!visitedChildren.contains(childNode)) {
					childrenStack.add(childNode);
				}
			}
		} 

//		for (int i = 0; i < hNode.childNodes.length; ++i) {
//			childNode = classes.get(hNode.childNodes[i]);
//			palette.mixColour(resource.getURI(), hNode.childNodes[i].getURI());
//			// if this child has additional children
//			if (childNode.childNodes != null) {
//				mixColours(hNode.childNodes[i], childNode, classes, palette);
//			}
//		}
	}

//    protected ColourPalette createDatatypedPalette(Model model){
//    	RDFNode object;
//        Resource subject;
//        Literal resource2 ;
//        Property property;
//        StmtIterator sIterator = model.listStatements();
//        Statement statement;
//        // Iterate over the class hierarchy triples
//        while (sIterator.hasNext()) {
//            statement = sIterator.next();
//            subject = statement.getSubject();
//            object = statement.getObject();
//            property = statement.getPredicate();
//            if (object.isLiteral()) {
//            	
//            	Set<RDFDatatype> setDatatypes = dataTypedProperties.get(property);
//            	if(setDatatypes == null){
//            		setDatatypes = new HashSet<RDFDatatype>();
//            		dataTypedProperties.put(property, setDatatypes);
//            	}
//            	RDFDatatype type = object.asLiteral().getDatatype();
//            	// get data type 
//            	setDatatypes.add(type);
//            }
//        }
//
//     // All properties have been collected
//        // The colours can be defined
//        Set<Resource> setOfResources = dataTypedProperties.keySet();
//        for (Resource res : setOfResources) {
//        	datatypedEdgePalette.addColour(res.getURI());
//        }
//        
//    	return datatypedEdgePalette;
//    }
}
