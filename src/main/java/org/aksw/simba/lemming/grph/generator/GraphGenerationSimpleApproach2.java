package org.aksw.simba.lemming.grph.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.dist.utils.IOfferedItem;
import org.aksw.simba.lemming.dist.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.dist.utils.PoissonDistribution;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgInDegreeDistBaseVEColo;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgColouredIEDistPerVColour;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgOutDegreeDistBaseVEColo;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgColouredOEDistPerVColour;
import org.aksw.simba.lemming.rules.ColourMappingRules;
import org.aksw.simba.lemming.rules.TripleBaseSingleID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class GraphGenerationSimpleApproach2 extends AbstractGraphGeneration implements IGraphGeneration{
private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationSimpleApproach2.class);
	
	//key: out-edge's colours, the values are the distribution of edges per vertex's colours
	//private Map<BitSet, ObjectDistribution<BitSet>> mMapAvrgOutEdgeDistPerVertColo;
	
	//key: in-edge's colours, the values are the distribution of edges per vertex's colours
	//private Map<BitSet, ObjectDistribution<BitSet>> mMapAvrgInEdgeDistPerVertColo;


	private Map<BitSet, IOfferedItem<BitSet>> mMapOEColoToTailColoProposer;
	
	private Map<BitSet, IOfferedItem<BitSet>> mMapIEColoToHeadColoProposer;

	private int maxIterationFor1EdgeColo ;
	
	public GraphGenerationSimpleApproach2(int iNumberOfVertices,
			ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		
		maxIterationFor1EdgeColo = 10000;
		
		mMapOEColoToTailColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		mMapIEColoToHeadColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		
		computeAvrgIOEdgeDistPerVertColo(origGrphs);
		
		// extend step compared to the class GraphGenerationSimpleApproach
		computePotentialIODegreePerVert(origGrphs);
	}

	public ColouredGraph generateGraph(){
		
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		for(BitSet edgeColo : keyEdgeColo){
			
			IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
			IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
			
			ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapTailColoToTailIDs = mapPossibleODegreePerOEColo.get(edgeColo);
			ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapHeadColoToHeadIDs = mapPossibleIDegreePerIEColo.get(edgeColo);
			
			if(headColourProposer != null && tailColourProposer!= null && mapTailColoToTailIDs != null && mapHeadColoToHeadIDs != null){
				
				// the setFakeEdgeIDs helps us to know how many edges existing in a specific edge's colour
				IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
				for(int i = 0 ; i < setFakeEdgeIDs.size() ; i++){
					
					boolean isFoundVerticesConnected = false;
					
					BitSet tailColo = tailColourProposer.getPotentialItem();
					Set<BitSet> setPossHeadColours = mColourMapper.getHeadColours(tailColo);
					BitSet headColo = headColourProposer.getPotentialItem(setPossHeadColours);
										
					if(headColo!=null && tailColo!= null){
						
						/*
						 *  this step is different to the GraphGenerationSimpleApproach
						 *  since it apply poisson distribution to compute potential degree 
						 *  for each vertex
						 */
						IOfferedItem<Integer> tailIDsProposer = mapTailColoToTailIDs.get(tailColo);
						IOfferedItem<Integer> headIDsProposer = mapHeadColoToHeadIDs.get(headColo);
						
						if(tailIDsProposer !=null && headIDsProposer != null ){
							Integer tailId = tailIDsProposer.getPotentialItem();
							Integer headId = headIDsProposer.getPotentialItem();
							if(tailId != null && headId != null){
								mMimicGraph.addEdge(tailId, headId, edgeColo);
								isFoundVerticesConnected = true;
							}
						}else{
							System.err.println("Could not find any vertices with the tail's or head's colours!");
							LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
						}
					}else{
						System.err.println("Could not find matching tail's and head's colours to connect!");
						LOGGER.warn("Could not find matching tail's and head's colours to connect!");
					}
					
					if(!isFoundVerticesConnected){
						i--;
		    			//System.err.println("Could not link " + tailColo + " - " + edgeColo + " - "+ headColo);
		    			maxIterationFor1EdgeColo--;
		    			if(maxIterationFor1EdgeColo== 0){
		    				LOGGER.warn("Could not create "
									+ setFakeEdgeIDs.size()
									+ " edges in the "
									+ edgeColo
									+ " colour since it could not find any approriate tail and head to connect.");
							
							System.err.println("Could not create "
									+ setFakeEdgeIDs.size()
									+ " edges in the "
									+ edgeColo
									+ " colour since it could not find any approriate tail and head to connect.");
		    				break;
		    			}
					}
				}
				
				if(maxIterationFor1EdgeColo==0){
					maxIterationFor1EdgeColo = 10000;
					continue;
				}else{
					maxIterationFor1EdgeColo = 10000;
				}
			}else{
				LOGGER.warn("Could not consider the"
						+ edgeColo
						+ " edge's colour since it could not find any approriate vertices to connect.");
				
				System.err.println("Could not consider the"
						+ edgeColo
						+ " edge's coloursince it could not find any approriate vertices to connect");
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
	
	private void computePotentialIODegreePerVert(ColouredGraph[] origGrphs){
		// compute for each vertex's colour, the average out-degree associated with a specific edge's colour
		AvrgInDegreeDistBaseVEColo avrgInDegreeAnalyzer = new AvrgInDegreeDistBaseVEColo(origGrphs);
		// compute for each vertex's colour, the average in-degree associated with a specific edge's colour
		AvrgOutDegreeDistBaseVEColo avrgOutDegreeAnalyzer = new AvrgOutDegreeDistBaseVEColo(origGrphs);
		
		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
		
		for(BitSet edgeColo : setEdgeColours){
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			
			for(BitSet tailColo : setTailColours){
				if(setVertexColours.contains(tailColo)){
					double avrgOutDegree = avrgOutDegreeAnalyzer.getAvarageOutDegreeOf(tailColo, edgeColo);
					
					// get list tailIDs 
					int[] arrTailIDs = mMapColourToVertexIDs.get(tailColo).toIntArray();
					double[] possOutDegreePerTailIDs = new double[arrTailIDs.length];
					Integer[] objTailIDs = new Integer[arrTailIDs.length];
					// for each tail id, we compute the potential out degree for it
					Random random = new Random();
					
					for(int i = 0 ; i < arrTailIDs.length ; i++){
						objTailIDs[i] = arrTailIDs[i];
						// generate a random out degree for each vertex in its set based on the computed average out-degree
						int possDeg = PoissonDistribution.randomXJunhao(avrgOutDegree, random);
						if(possDeg == 0)
							possDeg = 1;
						
						possOutDegreePerTailIDs[i] = (double)possDeg;
					}
					
					ObjectDistribution<Integer> potentialOutDegree = new ObjectDistribution<Integer>(objTailIDs, possOutDegreePerTailIDs);
					OfferedItemByRandomProb<Integer> potentialDegreeProposer = new OfferedItemByRandomProb<Integer>(potentialOutDegree, random);
					
					// put to map potential degree proposer
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapPossODegree = mapPossibleODegreePerOEColo.get(edgeColo);
					if(mapPossODegree == null){
						mapPossODegree = new ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> ();
						mapPossibleODegreePerOEColo.put(edgeColo, mapPossODegree);
					}
					
					IOfferedItem<Integer> outDegreeProposer = mapPossODegree.get(tailColo);
					if(outDegreeProposer == null){
						mapPossODegree.put(tailColo, potentialDegreeProposer);
					}else{
						LOGGER.error("Something is seriously happening");
					}
				}
			}
			
			Set<BitSet> setHeadColours = mColourMapper.getHeadColoursFromEdgeColour(edgeColo);
			for(BitSet headColo : setHeadColours){
				if(setVertexColours.contains(headColo)){
					double avrgInDegree = avrgInDegreeAnalyzer.getAvarageInDegreeOf(edgeColo, headColo);
					
					int [] arrHeadIDs = mMapColourToVertexIDs.get(headColo).toIntArray();
					
					double[] possOutDegreePerHeadDs = new double[arrHeadIDs.length];
					Integer[] objHeadIDs = new Integer[arrHeadIDs.length];
					Random random = new Random();
					
					// for each head id, we compute the potential in degree for it
					for(int i = 0; i < arrHeadIDs.length ; i++){
						objHeadIDs[i] = arrHeadIDs[i];
						// generate a random in degree for each vertex in its set based on the computed average in-degree
						int possDeg = PoissonDistribution.randomXJunhao(avrgInDegree, random);
						if(possDeg == 0)
							possDeg = 1;
						possOutDegreePerHeadDs[i] = (double)possDeg;
					}
					
					ObjectDistribution<Integer> potentialInDegree = new ObjectDistribution<Integer>(objHeadIDs, possOutDegreePerHeadDs);
					OfferedItemByRandomProb<Integer> potentialDegreeProposer = new OfferedItemByRandomProb<Integer>(potentialInDegree, random);
					
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapPossIDegree = mapPossibleIDegreePerIEColo.get(edgeColo);
					if(mapPossIDegree == null){
						mapPossIDegree = new ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> ();
						mapPossibleIDegreePerIEColo.put(edgeColo, mapPossIDegree);
					}
					
					IOfferedItem<Integer> inDegreeProposer = mapPossIDegree.get(headColo);
					if(inDegreeProposer == null){
						mapPossIDegree.put(headColo, potentialDegreeProposer);
					}else{
						LOGGER.error("Something is seriously happening");
					}
				}
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
					
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapTailColoToTailIDs = mapPossibleODegreePerOEColo.get(edgeColo);
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapHeadColoToHeadIDs = mapPossibleIDegreePerIEColo.get(edgeColo);
					
					
					if(tailColourProposer!=null && headColourProposer !=null && mapTailColoToTailIDs !=null && mapHeadColoToHeadIDs!=null ){
						
						BitSet tailColo = tailColourProposer.getPotentialItem();
						Set<BitSet> setPossHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
						BitSet headColo = headColourProposer.getPotentialItem(setPossHeadColours);
						
						IOfferedItem<Integer> tailIDsProposer = mapTailColoToTailIDs.get(tailColo);
						IOfferedItem<Integer> headIDsProposer = mapHeadColoToHeadIDs.get(headColo);
						
						if(tailIDsProposer !=null && headIDsProposer != null ){
							Integer tailId = tailIDsProposer.getPotentialItem();
							Integer headId = headIDsProposer.getPotentialItem();
							if(tailId != null && headId != null){
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
			System.out.println("using base function getProposedTriple(");
			return super.getProposedTriple(true);
		}
	}
}
