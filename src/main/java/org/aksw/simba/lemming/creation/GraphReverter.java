package org.aksw.simba.lemming.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	private Model mDataModel;
	
	public GraphReverter(ColouredGraph graph, Model origModel){
		mGraph = graph;
		mDataModel = origModel;
	}
	
	public Model processGraph(){
		
		int [] arrOfVIds = mGraph.getVertices().toIntArray();
		Map<Integer, String> mapResourcesURLs = new HashMap<Integer, String>();
		
		for(int tId: arrOfVIds){
			/*
			 *  process resources vertices
			 */
			String tDummyURI = mapResourcesURLs.containsKey(tId)?  mapResourcesURLs.get(tId): mGraph.getResourceDummyURI(tId); 
			Resource tRes = mDataModel.createResource(tDummyURI);
			//System.err.println("Connect " + tId +" to");
			IntSet setOfOEIds = mGraph.getOutEdges(tId);
			if(setOfOEIds != null && setOfOEIds.size() > 0 ){
				//array of out edges (object properties)
				int [] arrOfOEIds = setOfOEIds.toIntArray();
				
				
				BitSet tColo = mGraph.getVertexColour(tId);
				Set<String> setOfClassURIs = new HashSet<String>(mGraph.getResourceClass(tColo));
				//List<String> lstClassURIs = new ArrayList<String>(setOfClassURIs);

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
					
					Property propRes = mDataModel.createProperty(propURI);
					
					int hId = mGraph.getHeadOfTheEdge(oeId);
					// get resources associated with the colours
					String hDummyURI = mapResourcesURLs.containsKey(hId)?  mapResourcesURLs.get(tId):"";
					
					if(hDummyURI.isEmpty()){
						if(propRes.equals(RDF.type)){
							List<String> lstClassURIs = new ArrayList<String>(setOfClassURIs);
							
							if(lstClassURIs.size()> 0){
								hDummyURI = lstClassURIs.get(0);
								setOfClassURIs.remove(hDummyURI);
							}else{
								hDummyURI = mGraph.getResourceDummyURI(hId);
							}
						}else{
							hDummyURI = mGraph.getResourceDummyURI(hId);
						}	
					}				
					
					//System.err.println("\t " + propRes +" --> " + hDummyURI);
					
					if(hDummyURI == null || hDummyURI.isEmpty()){
						hDummyURI = Constants.SIMULATED_BLANK_OBJECT_RESOURCE;
						LOGGER.warn("the object's URI is null or empty!");
					}
					
					
					if(!mapResourcesURLs.containsKey(tId))
						mapResourcesURLs.put(tId, tDummyURI);
					if(!mapResourcesURLs.containsKey(hId))
						mapResourcesURLs.put(hId, hDummyURI);
					
					Resource hRes = mDataModel.createResource(hDummyURI);
					mDataModel.add(tRes, propRes, hRes);
					System.err.println("\t " + tDummyURI +" <"+propURI+"> " + hDummyURI);
					Triple triple = new Triple(tRes.asNode(), propRes.asNode(), hRes.asNode());
					mDataModel.getGraph().add(triple);
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
						Property datatypedProperty = mDataModel.createProperty(dtpropURI);
						Literal litObj = mDataModel.createLiteral(literal);
						mDataModel.add(tRes, datatypedProperty, litObj);
						
						Triple triple = new Triple(tRes.asNode(), datatypedProperty.asNode(), litObj.asNode());
						mDataModel.getGraph().add(triple);
					}
				}
			}
		}
		
		return mDataModel;
	}
	
//	private void processGraphWithMultiThreads(Model model){
//		new grph.algo.MultiThreadProcessing(mGraph.getVertices()) {
//			
//			@Override
//			protected void run(int threadID, int vId) {
//				
//				/*
//				 *  process resources vertices
//				 */
//				BitSet tColo = mGraph.getVertexColour(vId);
//				String tURI = mGraph.getResourceDummyURI(vId);
//				Resource tRes = model.createResource(tURI);
//				
//				IntSet setOfOEIds = mGraph.getOutEdges(vId);
//				if(setOfOEIds != null && setOfOEIds.size() > 0 ){
//					
//					int [] arrOfOEIds = setOfOEIds.toIntArray();
//					
//					for(int oeId: arrOfOEIds){
//						BitSet oeColo = mGraph.getEdgeColour(oeId);
//						int hId = mGraph.getHeadOfTheEdge(oeId);
//						BitSet hColo = mGraph.getVertexColour(hId);
//						
//						// get resources associated with the colours
//						
//						String propURI = mGraph.getPropertyURI(oeColo);
//						String hURI = mGraph.getResourceDummyURI(hId);
//						
//						
//						Resource hRes = model.createResource(hURI);
//						Property propRes = model.createProperty(propURI);
//						synchronized(model){
//							model.add(tRes, propRes, hRes);
//						}
//					}
//				}
//				/*
//				 *  process literals 
//				 */
//				
//				// get associated data typed edges
//				Map<BitSet, List<String>> mapDTEColoursToLiterals = mGraph.getMapDTEdgeColoursToLiterals(vId); 
//				Set<BitSet> setOfDTEColours = mapDTEColoursToLiterals.keySet();
//				for(BitSet dteColo : setOfDTEColours){
//					List<String> lstOfLiterals = mapDTEColoursToLiterals.get(dteColo);
//					
//					String dtpropURI = mGraph.getDataTypedPropertyURI(dteColo);
//					for(String literal : lstOfLiterals){
//						Property datatypedProperty = model.createProperty(dtpropURI);
//						Literal litObj = model.createLiteral(literal);
//						synchronized(model){
//							model.add(tRes, datatypedProperty, litObj);
//						}
//					}
//				}
//				
//			}
//		}; 
//	}
}
