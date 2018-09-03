package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

public class GraphGenerationRandomly extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationRandomly.class);
	private int maxIterationFor1EdgeColo ;
	
	public GraphGenerationRandomly(int iNumberOfVertices,
			ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
	}

	public ColouredGraph generateGraph(){
		
		/*
		 * mMapColourToEdgeIDs contains only normal edges (not datatype property edges and
		 * rdf:type edges)
		 */
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setAvailableVertexColours = mMapColourToVertexIDs.keySet();
		for(BitSet edgeColo : keyEdgeColo){
			
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			setTailColours.retainAll(setAvailableVertexColours);
			
			BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);
			
			/* the setFakeEdgeIDs helps us to know how many edges existing
			 * in a specific edge's colour*/ 
			IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
			// use each edge to connect vertices
			int i = 0 ;
			while(i < setFakeEdgeIDs.size()){
					
				boolean isFoundVerticesConnected = false;
				
				BitSet tailColo = arrTailColours[mRandom.nextInt(arrTailColours.length)];	
				Set<BitSet> setHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
				
				if(setHeadColours == null || setHeadColours.size() ==0){
					continue;
				}
				
				setHeadColours.retainAll(setAvailableVertexColours);
				
				if(setHeadColours.size() ==0)
					continue;
				
				BitSet [] arrHeadColours = setHeadColours.toArray(new BitSet[0]);
				
				BitSet headColo = arrHeadColours[mRandom.nextInt(arrHeadColours.length)];
				IntSet setTailIDs = new DefaultIntSet();
				IntSet setHeadIDs = new DefaultIntSet();
				
				if(mMapColourToVertexIDs.containsKey(tailColo)){
					setTailIDs = mMapColourToVertexIDs.get(tailColo).clone();
				}
				
				if(mMapColourToVertexIDs.containsKey(headColo)){
					setHeadIDs = mMapColourToVertexIDs.get(headColo).clone();
				}
				
				if(setTailIDs!= null && setTailIDs.size()> 0 && setHeadIDs!=null && setHeadIDs.size()> 0){
					int[] arrTailIDs = setTailIDs.toIntArray();
					int tailId = -1;
					while(true){
						tailId = arrTailIDs[mRandom.nextInt(arrTailIDs.length)];
						if(!mReversedMapClassVertices.containsKey(tailColo))
							break;
					}
					
					int[] arrConnectedHeads = getConnectedHeads(tailId,edgeColo).toIntArray(); 
					for(int connectedHead: arrConnectedHeads){
						if(setHeadIDs.contains(connectedHead))
							setHeadIDs.remove(connectedHead);
					}
					
					if(setHeadIDs.size() == 0 ){
						continue;
					}
					
					int[] arrHeadIDs = setHeadIDs.toIntArray();
					
					int headId = arrHeadIDs[mRandom.nextInt(arrHeadIDs.length)];
					if(connectableVertices(tailId, headId, edgeColo)){
						mMimicGraph.addEdge(tailId, headId, edgeColo);
						isFoundVerticesConnected = true;	
						i++;
					}
//						else{
//							System.err.println("Found same vertices to connect");
//						}
				}
//					else{
//						System.err.println("Could not find any vertices with the tail's or head's colours!");
//						LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
//					}
				
				if (!isFoundVerticesConnected) {
					maxIterationFor1EdgeColo--;
					if (maxIterationFor1EdgeColo == 0) {
						LOGGER.error("Could not create "
								+ (setFakeEdgeIDs.size() - i)
								+ " edges in the "
								+ edgeColo
								+ " colour since it could not find any approriate vertices to connect.");
						
						System.err.println("Could not create "
								+ (setFakeEdgeIDs.size() - i)
								+ " edges in the "
								+ edgeColo
								+ " colour since it could not find any approriate vertices to connect.");
						break;
					}
				}
			}
			
			maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
		}
		return mMimicGraph;
	}
	
}
