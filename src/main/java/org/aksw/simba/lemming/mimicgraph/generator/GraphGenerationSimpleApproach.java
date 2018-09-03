package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgColouredIEDistPerVColour;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgColouredOEDistPerVColour;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.DefaultIntSet;
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
	
	private Map<BitSet, IOfferedItem<BitSet>> mMapOEColoToTailColoProposer;
	
	private Map<BitSet, IOfferedItem<BitSet>> mMapIEColoToHeadColoProposer;
	
	private int maxIterationFor1EdgeColo ;
	
	public GraphGenerationSimpleApproach(int iNumberOfVertices,
			ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		
		mMapOEColoToTailColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		mMapIEColoToHeadColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		
		maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;

		computeAvrgIOEdgeDistPerVertColo(origGrphs);
	}

	public ColouredGraph generateGraph(){
		
		/*
		 * the map mMapColourToEdgeIDs just contains normal edges
		 * (not datatype property edges, or rdf:type edge) 
		 */
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		for(BitSet edgeColo : keyEdgeColo){
			
			IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
			IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
			
			if(tailColourProposer != null && headColourProposer!= null ){
				
				/* the setFakeEdgeIDs helps us to know how many edges existing
				 * in a specific edge's colour
				 */
				IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
				
				// use each edge to connect vertices
				int i = 0 ; 
				while (i < setFakeEdgeIDs.size()){
						
					boolean isFoundVerticesConnected = false;
					
					// get tail colour based on distribution of the edge colour over the vertex colour 
					BitSet tailColo = tailColourProposer.getPotentialItem();
					// get a set of head colours associated with the edge colour and the tail colour
					Set<BitSet> setRestrictedHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
					
					// base on the possible linked head's colours got from the tailColo we get the potential headColo
					BitSet headColo = headColourProposer.getPotentialItem(setRestrictedHeadColours);
					
					if(headColo!=null && tailColo!= null){
						// get tailIds based on the tailColo
						IntSet tailIDs = mMapColourToVertexIDs.get(tailColo);
						
			    		
			    		if(tailIDs!=null && !tailIDs.isEmpty()){
			    			int[] arrTailVertices = tailIDs.toIntArray();
				    		int tailId = arrTailVertices[mRandom.nextInt(arrTailVertices.length)];
				    		
				    		IntSet setHeadIDs = new DefaultIntSet();
							if(mMapColourToVertexIDs.containsKey(headColo)){
								setHeadIDs = mMapColourToVertexIDs.get(headColo).clone();
							}
				    		
							if(setHeadIDs == null || setHeadIDs.size() == 0 ){
								continue;
							}
							
							int[] arrConnectedHeads = getConnectedHeads(tailId,edgeColo).toIntArray(); 
							for(int connectedHead: arrConnectedHeads){
								if(setHeadIDs.contains(connectedHead))
									setHeadIDs.remove(connectedHead);
							}
							
							if(setHeadIDs.size() == 0){
								continue;
							}
							int[] arrHeadVertices = setHeadIDs.toIntArray();
							
				    		int headId = arrHeadVertices[mRandom.nextInt(arrHeadVertices.length)];
				    		
				    		if(connectableVertices(tailId, headId, edgeColo)){
								mMimicGraph.addEdge(tailId, headId, edgeColo);
								isFoundVerticesConnected = true;
								i++;
							}
			    		}
//			    		else{
//			    			System.err.println("Could not find any vertices with the tail's or head's colours!");
//							LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
//			    		}
					}
					else{
						System.err.println("Could not find matching tail's and head's colours to connect!");
						LOGGER.error("Could not find matching tail's and head's colours to connect!");
					}
					
					if (!isFoundVerticesConnected) {
						// System.err.println("Could not link " + tailColo +
						// " - " + edgeColo + " - "+ headColo);
						maxIterationFor1EdgeColo--;
						if (maxIterationFor1EdgeColo == 0) {
							LOGGER.error("Could not create "
									+ (setFakeEdgeIDs.size() - i)
									+ " edges (" 
									+ setFakeEdgeIDs.size()
									+") in the "
									+ edgeColo
									+ " colour since it could not find any approriate tail and head to connect.");
							
							System.err.println("Could not create "
									+ (setFakeEdgeIDs.size() - i)
									+ " edges (" 
									+ setFakeEdgeIDs.size()
									+") in the "
									+ edgeColo
									+ " colour since it could not find any approriate tail and head to connect.");
							break;
						}
					}
				}
				
				
				maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
				
			}else{
				
				/*
				 * this case may never happen since in RDF graph, an edge is always is used to connect 2 vertices 
				 */
				
				LOGGER.warn("Not process the"
						+ edgeColo
						+ " edge's colour since it could not find any approriate vertex's colours.");
				
				System.err.println("Not process the"
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
		AvrgColouredOEDistPerVColour avrgOutEdgeDistPerVertColoMetric = new AvrgColouredOEDistPerVColour(origGrphs);
		Map<BitSet, ObjectDistribution<BitSet>> avrgOutEdgeDistPerVertColo = avrgOutEdgeDistPerVertColoMetric.getMapAvrgOutEdgeDist(mMapColourToEdgeIDs.keySet(), mMapColourToVertexIDs.keySet());
		
		Set<BitSet> outEdgeColours = avrgOutEdgeDistPerVertColo.keySet();
		for(BitSet edgeColo : outEdgeColours){
			ObjectDistribution<BitSet> outEdgeDistPerVertColo = avrgOutEdgeDistPerVertColo.get(edgeColo);
			if(outEdgeDistPerVertColo != null){
				IOfferedItem<BitSet> vertColoProposer = new OfferedItemByRandomProb<>(outEdgeDistPerVertColo);
				mMapOEColoToTailColoProposer.put(edgeColo, vertColoProposer);
			}
		}
		
		// in degree colour distribution associated with edge colours
		AvrgColouredIEDistPerVColour avrgInEdgeDistPerVertColoMetric = new AvrgColouredIEDistPerVColour(origGrphs);
		Map<BitSet, ObjectDistribution<BitSet>> avrgInEdgeDistPerVertColo = avrgInEdgeDistPerVertColoMetric.getMapAvrgInEdgeDist(mMapColourToEdgeIDs.keySet(), mMapColourToVertexIDs.keySet());
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
			//System.out.println("using override function getProposedTriple(");
			LOGGER.info("Using the override function getProposedTriple");
			while(true){
				BitSet edgeColo = mEdgeColoProposer.getPotentialItem();
				if(edgeColo != null && !edgeColo.equals(mRdfTypePropertyColour)){
					
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
							
							
							if(connectableVertices(tailId, headId, edgeColo)){
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
			}
		}else{
			//System.out.println("using base function getProposedTriple(");
			LOGGER.info("Using the base function getProposedTriple of abstract class");
			return super.getProposedTriple(true);
		}
	}
	
	
}
