package org.aksw.simba.lemming.creation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

public class GraphReverter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphReverter.class);
	
	private ColouredGraph mGraph;
	//private Model mLatestModel; 
	private Model mNewModel;
	
	private int mResourceID = 1;
	
	public GraphReverter(ColouredGraph graph, Model origModel){
		mGraph = graph;
		mNewModel = origModel;
		//mLatestModel = origModel;
		//initialize new model
		//initializeModel(origModel);
	}
	
//	private void initializeModel(Model origModel){
//		mNewModel = ModelFactory.createDefaultModel();
//		mNewModel.setNsPrefixes(origModel.getNsPrefixMap());
//	}
	
	public Model processGraph(){
		
		IntSet setOfVIds = mGraph.getVertices();
		int [] arrOfVIds = setOfVIds.toIntArray();
		for(int tId: arrOfVIds){
			/*
			 *  process resources vertices
			 */
			String tDummyURI = mGraph.getResourceDummyURI(tId);
			Resource tRes = mNewModel.createResource(tDummyURI);
			
			IntSet setOfOEIds = mGraph.getOutEdges(tId);
			if(setOfOEIds != null && setOfOEIds.size() > 0 ){
				//array of out edges (object properties)
				int [] arrOfOEIds = setOfOEIds.toIntArray();
				
				//iterate each out edge id
				for(int oeId: arrOfOEIds){
					// get the out edge's colour 
					BitSet oeColo = mGraph.getEdgeColour(oeId);
					// get the corresponding URI
					String propURI = mGraph.getPropertyURI(oeColo);
					if(propURI == null || propURI.isEmpty()){
						propURI = "http://org.apache.jena.rdfxml/blankProp#";
						LOGGER.warn("the property's URI is null or empty!");
					}
					
					Property propRes = mNewModel.createProperty(propURI);
					
					int hId = mGraph.getHeadOfTheEdge(oeId);
					// get resources associated with the colours
					String hDummyURI = "";
					
					if(propRes.equals(RDF.type)){
						BitSet tColo = mGraph.getVertexColour(tId);
						hDummyURI = mGraph.getResourceClass(tColo);
						System.err.println(tDummyURI + "(" +tId +")" +" - " + propRes + " - " + hDummyURI + "("+hId+")");
					}else{
						hDummyURI = mGraph.getResourceDummyURI(hId);
						System.out.println(tDummyURI + "(" +tId +")" +" - " + propRes + " - " + hDummyURI + "("+hId+")");
					}
					
					if(hDummyURI == null || hDummyURI.isEmpty()){
						hDummyURI = Constants.SIMULATED_BLANK_OBJECT_RESOURCE;
						LOGGER.warn("the object's URI is null or empty!");
					}
					
					Resource hRes = mNewModel.createResource(hDummyURI);
					mNewModel.add(tRes, propRes, hRes);
					
					Triple triple = new Triple(tRes.asNode(), propRes.asNode(), hRes.asNode());
					mNewModel.getGraph().add(triple);
				}
			}
			/*
			 *  process literals 
			 */
			
			// get associated data typed edges
			Map<BitSet, List<String>> mapDTEColoursToLiterals = mGraph.getMapDTEdgeColoursToLiterals(tId); 
			if(mapDTEColoursToLiterals!= null){
				Set<BitSet> setOfDTEColours = mapDTEColoursToLiterals.keySet();
				for(BitSet dteColo : setOfDTEColours){
					List<String> lstOfLiterals = mapDTEColoursToLiterals.get(dteColo);
					
					String dtpropURI = mGraph.getDataTypedPropertyURI(dteColo);
					for(String literal : lstOfLiterals){
						Property datatypedProperty = mNewModel.createProperty(dtpropURI);
						Literal litObj = mNewModel.createLiteral(literal);
						mNewModel.add(tRes, datatypedProperty, litObj);
						
						Triple triple = new Triple(tRes.asNode(), datatypedProperty.asNode(), litObj.asNode());
						mNewModel.getGraph().add(triple);
					}
				}
			}
		}
		
		return mNewModel;
	}
	
	private void printGraphInfo(){
		Map<String, String> ns = mNewModel.getNsPrefixMap();
		Set<String> setOfKeys = ns.keySet();
		System.err.println(">>>>>"); 
		for(String key : setOfKeys){
			String uri = ns.get(key);
			System.out.println(key + " - " + uri); 
		}
		System.err.println(">>>>>");
		System.out.println("");
	}
	
	
	private void processGraphWithMultiThreads(Model model){
		new grph.algo.MultiThreadProcessing(mGraph.getVertices()) {
			
			@Override
			protected void run(int threadID, int vId) {
				
				/*
				 *  process resources vertices
				 */
				BitSet tColo = mGraph.getVertexColour(vId);
				String tURI = mGraph.getResourceDummyURI(vId);
				Resource tRes = model.createResource(tURI);
				
				IntSet setOfOEIds = mGraph.getOutEdges(vId);
				if(setOfOEIds != null && setOfOEIds.size() > 0 ){
					
					int [] arrOfOEIds = setOfOEIds.toIntArray();
					
					for(int oeId: arrOfOEIds){
						BitSet oeColo = mGraph.getEdgeColour(oeId);
						int hId = mGraph.getHeadOfTheEdge(oeId);
						BitSet hColo = mGraph.getVertexColour(hId);
						
						// get resources associated with the colours
						
						String propURI = mGraph.getPropertyURI(oeColo);
						String hURI = mGraph.getResourceDummyURI(hId);
						
						
						Resource hRes = model.createResource(hURI);
						Property propRes = model.createProperty(propURI);
						synchronized(model){
							model.add(tRes, propRes, hRes);
						}
					}
				}
				/*
				 *  process literals 
				 */
				
				// get associated data typed edges
				Map<BitSet, List<String>> mapDTEColoursToLiterals = mGraph.getMapDTEdgeColoursToLiterals(vId); 
				Set<BitSet> setOfDTEColours = mapDTEColoursToLiterals.keySet();
				for(BitSet dteColo : setOfDTEColours){
					List<String> lstOfLiterals = mapDTEColoursToLiterals.get(dteColo);
					
					String dtpropURI = mGraph.getDataTypedPropertyURI(dteColo);
					for(String literal : lstOfLiterals){
						Property datatypedProperty = model.createProperty(dtpropURI);
						Literal litObj = model.createLiteral(literal);
						synchronized(model){
							model.add(tRes, datatypedProperty, litObj);
						}
					}
				}
				
			}
		}; 

		
	}
	
	
}
