package org.aksw.simba.lemming.grph.generator;

import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

public class GraphGenerationRandomly extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationRandomly.class);
	private int maxIterationFor1EdgeColo ;
	
	public GraphGenerationRandomly(int iNumberOfVertices,
			ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		maxIterationFor1EdgeColo = 1000;
	}

	public ColouredGraph generateGraph(){
		
		Set<BitSet> keyVertColo = mMapColourToVertexIDs.keySet();
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		for(BitSet edgeColo : keyEdgeColo){
			
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);
			
			/* the setFakeEdgeIDs helps us to know how many edges existing
			 * in a specific edge's colour*/ 
			IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
			// use each edge to connect vertices
			for(int i = 0 ; i < setFakeEdgeIDs.size() ; i++){
				
				boolean isFoundVerticesConnected = false;
				
				BitSet tailColo = arrTailColours[mRandom.nextInt(arrTailColours.length)];	
				Set<BitSet> setHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
				
				if(setHeadColours.size() > 0){
					BitSet [] arrHeadColours = setHeadColours.toArray(new BitSet[0]);
					
					BitSet headColo = arrHeadColours[mRandom.nextInt(arrHeadColours.length)];
					
					IntSet setTailIDs = mMapColourToVertexIDs.get(tailColo);
					IntSet setHeadIDs = mMapColourToVertexIDs.get(headColo);
					
					if(setTailIDs!= null && setHeadIDs!=null){
						int[] arrTailIDs = setTailIDs.toIntArray();
						int[] arrHeadIDs = setHeadIDs.toIntArray();
						
						int tailId = arrTailIDs[mRandom.nextInt(arrTailIDs.length)];
						int headId = arrHeadIDs[mRandom.nextInt(arrHeadIDs.length)];
						
						mMimicGraph.addEdge(tailId, headId, edgeColo);
						isFoundVerticesConnected = true;
					}else{
						System.err.println("Could not find any vertices with the tail's or head's colours!");
						LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
					}
				}
				
				if (!isFoundVerticesConnected) {
					i--;
					maxIterationFor1EdgeColo--;
					if (maxIterationFor1EdgeColo == 0) {
						LOGGER.warn("Could not create "
								+ setFakeEdgeIDs.size()
								+ " edges in the "
								+ edgeColo
								+ " colour since it could not find any approriate vertices to connect.");
						
						System.err.println("Could not create "
								+ setFakeEdgeIDs.size()
								+ " edges in the "
								+ edgeColo
								+ " colour since it could not find any approriate vertices to connect.");
						break;
					}
				}
			}
		}
		return mMimicGraph;
	}
	
}
