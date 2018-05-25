package org.aksw.simba.lemming.creation;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

public class GraphReverter {
	
	private ColouredGraph mGraph;
	private Model mLatestModel; 
	public GraphReverter(ColouredGraph graph, Model origModel){
		mGraph = graph;
		mLatestModel = origModel;
	}
	
	public void processGraph(Model model){
		
		IntSet setOfVIds = mGraph.getVertices();
		int [] arrOfVIds = setOfVIds.toIntArray();
		for(int tId: arrOfVIds){
			/*
			 *  process resources vertices
			 */
			BitSet tColo = mGraph.getVertexColour(tId);
			String tURI = mGraph.getResourceURI(tColo);
			Resource tRes = model.createResource(tURI);
			
			IntSet setOfOEIds = mGraph.getOutEdges(tId);
			if(setOfOEIds != null && setOfOEIds.size() > 0 ){
				
				int [] arrOfOEIds = setOfOEIds.toIntArray();
				
				for(int oeId: arrOfOEIds){
					BitSet oeColo = mGraph.getEdgeColour(oeId);
					int hId = mGraph.getHeadOfTheEdge(oeId);
					BitSet hColo = mGraph.getVertexColour(hId);
					
					// get resources associated with the colours
					
					String propURI = mGraph.getPropertyURI(oeColo);
					String hURI = mGraph.getResourceURI(hColo);
					
					
					Resource hRes = model.createResource(hURI);
					Property propRes = model.createProperty(propURI);
					model.add(tRes, propRes, hRes);
				}
			}
			/*
			 *  process literals 
			 */
			
			// get associated data typed edges
			
			// for each data typed edges get corresponding literals
			
			// create a statement for them
			
			
		}
	}
	
	public void processGraphWithMultiThreads(){
		
	}
	
	
}
