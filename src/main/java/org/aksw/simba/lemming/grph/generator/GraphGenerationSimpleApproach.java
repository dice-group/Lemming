package org.aksw.simba.lemming.grph.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.dist.utils.IOfferedItem;
import org.aksw.simba.lemming.dist.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgInEdgeDistBaseEColoPerVColo;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgOutEdgeDistBaseEColoPerVColo;
import org.aksw.simba.lemming.rules.TripleBaseSingleID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

/**
 * The simplest approach, we randomly select a vertex in its set (the set with a
 * specific colour)
 * 
 * @author nptsy
 *
 */
public class GraphGenerationSimpleApproach extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationSimpleApproach.class);
	
	//key: out-edge's colours, the values are the distribution of vertices per vertex's colours
	//private Map<BitSet, ObjectDistribution<BitSet>> mMapAvrgOutEdgeDistPerVertColo;
	
	//key: in-edge's colours, the values are the distribution of vertices per vertex's colours
	//private Map<BitSet, ObjectDistribution<BitSet>> mMapAvrgInEdgeDistPerVertColo;

	private Map<BitSet, IOfferedItem<BitSet>> mMapOEColoToTailColoProposer;
	
	private Map<BitSet, IOfferedItem<BitSet>> mMapIEColoToHeadColoProposer;
	
	private int maxIterationFor1EdgeColo ;
	
	public GraphGenerationSimpleApproach(int iNumberOfVertices,
			ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		
		mMapOEColoToTailColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		mMapIEColoToHeadColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		
		maxIterationFor1EdgeColo = 10000;

		computeAvrgIOEdgeDistPerVertColo(origGrphs);
	}

	public ColouredGraph generateGraph(){
		
		Set<BitSet> keyEdgeColo = mapColourToEdgeIDs.keySet();
		for(BitSet edgeColo : keyEdgeColo){
			
			IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
			IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
			
			if(headColourProposer != null && headColourProposer!= null ){
				
				/* the setFakeEdgeIDs helps us to know how many edges existing
				 * in a specific edge's colour*/ 
				IntSet setFakeEdgeIDs = mapColourToEdgeIDs.get(edgeColo);
				// use each edge to connect vertices
				for(int i = 0 ; i < setFakeEdgeIDs.size() ; i++){
					
					boolean isFoundVerticesConnected = false;
					
					// tail colour
					BitSet tailColo = tailColourProposer.getPotentialItem();
					Set<BitSet> setRestrictedHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
					// base on the possible linked head's colours got from the tailColo we get the potential headColo
					BitSet headColo = headColourProposer.getPotentialItem(setRestrictedHeadColours);
					
					if(headColo!=null && tailColo!= null){
						// get tailIds based on the tailColo
						IntSet tailIDs = mMapColourToVertexIDs.get(tailColo);
						
						// get headIds based on the headColo
						IntSet headIDs = mMapColourToVertexIDs.get(headColo);
			    		
			    		if(headIDs!= null && !headIDs.isEmpty() && tailIDs!=null && !tailIDs.isEmpty()){
			    			int[] arrTailVertices = tailIDs.toIntArray();
				    		int[] arrHeadVertices = headIDs.toIntArray();
				    		int tailId = arrTailVertices[mRandom.nextInt(arrTailVertices.length)];
				    		int headId = arrHeadVertices[mRandom.nextInt(arrHeadVertices.length)];
				    		mMimicGraph.addEdge(tailId, headId, edgeColo);
				    		isFoundVerticesConnected = true;
			    		}else{
			    			System.err.println("Could not find any vertices with the tail's or head's colours!");
							LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
			    		}
					}else{
						System.err.println("Could not find matching tail's and head's colours to connect!");
						LOGGER.warn("Could not find matching tail's and head's colours to connect!");
					}
					
					if (!isFoundVerticesConnected) {
						i--;
						// System.err.println("Could not link " + tailColo +
						// " - " + edgeColo + " - "+ headColo);
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
				if(maxIterationFor1EdgeColo==0){
					maxIterationFor1EdgeColo = 10000;
					continue;
				}else{
					maxIterationFor1EdgeColo =10000;
				}
			}else{
				LOGGER.warn("Could not consider the"
						+ edgeColo
						+ " edge's colour since it could not find any approriate vertex's colours.");
				
				System.err.println("Could not consider the"
						+ edgeColo
						+ " edge's colours ince it could not find any approriate vertex's colours.");
			}
		}
		return mMimicGraph;
	}
	
	/**
	 * compute complex distribution
	 * 
	 * @param origGrphs list of all versions of graphs
	 */
	private void computeAvrgIOEdgeDistPerVertColo(ColouredGraph[] origGrphs){
		// out degree colour distribution associated with edge colours
		AvrgOutEdgeDistBaseEColoPerVColo avrgOutEdgeDistPerVertColoMetric = new AvrgOutEdgeDistBaseEColoPerVColo(origGrphs);
		Map<BitSet, ObjectDistribution<BitSet>> avrgOutEdgeDistPerVertColo = avrgOutEdgeDistPerVertColoMetric.getMapAvrgOutEdgeDist(mapColourToEdgeIDs.keySet(), mMapColourToVertexIDs.keySet());
		
		Set<BitSet> outEdgeColours = avrgOutEdgeDistPerVertColo.keySet();
		for(BitSet edgeColo : outEdgeColours){
			ObjectDistribution<BitSet> outEdgeDistPerVertColo = avrgOutEdgeDistPerVertColo.get(edgeColo);
			if(outEdgeDistPerVertColo != null){
				IOfferedItem<BitSet> vertColoProposer = new OfferedItemByRandomProb<>(outEdgeDistPerVertColo);
				mMapOEColoToTailColoProposer.put(edgeColo, vertColoProposer);
			}
		}
		
		// in degree colour distribution associated with edge colours
		AvrgInEdgeDistBaseEColoPerVColo avrgInEdgeDistPerVertColoMetric = new AvrgInEdgeDistBaseEColoPerVColo(origGrphs);
		Map<BitSet, ObjectDistribution<BitSet>> avrgInEdgeDistPerVertColo = avrgInEdgeDistPerVertColoMetric.getMapAvrgInEdgeDist(mapColourToEdgeIDs.keySet(), mMapColourToVertexIDs.keySet());
		Set<BitSet> inEdgeColours = avrgInEdgeDistPerVertColo.keySet();
		for(BitSet edgeColo : inEdgeColours){
			ObjectDistribution<BitSet> inEdgeDistPerVertColo = avrgInEdgeDistPerVertColo.get(edgeColo);
			if(inEdgeDistPerVertColo != null){
				IOfferedItem<BitSet> vertColoProposer = new OfferedItemByRandomProb<>(inEdgeDistPerVertColo);
				mMapIEColoToHeadColoProposer.put(edgeColo, vertColoProposer);
			}
		}
	}
	
	public TripleBaseSingleID getProposedTriple(boolean isRandom){
		
		if(!isRandom){
			System.out.println("using override function getProposedTriple(");
			while(true){
				BitSet edgeColo = mEdgeColoProposer.getPotentialItem();
				if(edgeColo != null){
					
					IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
					IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
					
					if(tailColourProposer!=null && headColourProposer !=null){
						
						BitSet tailColo = tailColourProposer.getPotentialItem();
						Set<BitSet> setPossHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
						
						BitSet headColo = headColourProposer.getPotentialItem(setPossHeadColours);
						
						// get vertex's ids according to the vertex's colours
						if(mMapColourToVertexIDs.get(tailColo) != null && mMapColourToVertexIDs.get(headColo) != null){
							int[] arrTailIDs = mMapColourToVertexIDs.get(tailColo).toIntArray();
							int[] arrHeadIDs = mMapColourToVertexIDs.get(headColo).toIntArray();
							
							int tailId = arrTailIDs[mRandom.nextInt(arrTailIDs.length)];
							int headId = arrHeadIDs[mRandom.nextInt(arrHeadIDs.length)];
							
							TripleBaseSingleID triple = new TripleBaseSingleID();
							triple.tailId = tailId;
							triple.tailColour = tailColo;
							triple.headId = headId;
							triple.headColour = headColo;
							triple.edgeColour = edgeColo;
							
							return triple;
						}
					}
				}
			}
		}else{
			System.out.println("using base function getProposedTriple(");
			return super.getProposedTriple(true);
		}
	}
	
	
}
